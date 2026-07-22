#!/usr/bin/env python3
"""circlepack_client - a small Python client for CirclePack's command socket.

CirclePack runs a TCP command server (default port 3736; it starts
automatically with the GUI, or explicitly via ``runCP -socket [port]``).
The line protocol is:

* client connects, then sends ``MYNAME <name>`` once;
  server replies ``Your name is '<host> <name>``.
* thereafter, each line the client sends is run as a CirclePack command
  string (``;``-separated commands allowed), and the server replies with
  exactly one line: ``cmd result: <N>`` (N = success count, negative on error).

This is the bridge used by the ``%%circlepack`` Jupyter magic, but it is
usable on its own:

    from circlepack_client import CirclePackClient
    cp = CirclePackClient()
    cp.connect()
    print(cp.run("seed 8;disp -w -c"))   # -> 'cmd result: ...'
    cp.close()
"""
import socket


class CirclePackError(RuntimeError):
    pass


class CirclePackClient:
    def __init__(self, host="127.0.0.1", port=3736, name="jupyter", timeout=30.0):
        self.host = host
        self.port = port
        self.name = name
        self.timeout = timeout
        self.sock = None
        self._rf = None  # read file wrapper

    def connect(self):
        """Open the socket and perform the MYNAME handshake. Returns the
        server greeting line."""
        try:
            self.sock = socket.create_connection((self.host, self.port),
                                                 timeout=self.timeout)
        except OSError as ex:
            raise CirclePackError(
                "could not connect to CirclePack at %s:%d (%s). Is CirclePack "
                "running with its socket server? (it starts with the GUI, or "
                "use 'runCP -socket')" % (self.host, self.port, ex))
        self._rf = self.sock.makefile("r", encoding="utf-8", newline="\n")
        greeting = self._exchange("MYNAME " + self.name)
        if greeting is None:
            raise CirclePackError("no response to MYNAME handshake")
        return greeting

    def _send_line(self, line):
        self.sock.sendall((line + "\n").encode("utf-8"))

    def _read_line(self):
        line = self._rf.readline()
        if line == "":
            return None  # connection closed
        return line.rstrip("\r\n")

    def _exchange(self, line):
        self._send_line(line)
        return self._read_line()

    def run(self, command):
        """Send a CirclePack command string; return the server's response
        line (e.g. 'cmd result: 12'). Multi-line input is flattened to a
        single ';'-separated command line."""
        if self.sock is None:
            raise CirclePackError("not connected; call connect() first")
        flat = ";".join(s.strip() for s in command.strip().splitlines()
                        if s.strip())
        if not flat:
            return "cmd result: 0"
        resp = self._exchange(flat)
        if resp is None:
            raise CirclePackError("connection closed by CirclePack")
        return resp

    @staticmethod
    def result_count(resp):
        """Parse the integer N out of a 'cmd result: N' response, or None."""
        if resp and resp.startswith("cmd result:"):
            try:
                return int(resp.split(":", 1)[1].strip())
            except ValueError:
                return None
        return None

    def close(self):
        try:
            if self._rf:
                self._rf.close()
        except OSError:
            pass
        try:
            if self.sock:
                self.sock.close()
        except OSError:
            pass
        self.sock = None
        self._rf = None

    def __enter__(self):
        self.connect()
        return self

    def __exit__(self, *exc):
        self.close()


if __name__ == "__main__":
    import sys
    cmd = " ".join(sys.argv[1:]) or "seed 8;disp -w -c"
    with CirclePackClient() as cp:
        print(cp.run(cmd))
