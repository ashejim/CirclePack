#!/usr/bin/env python3
"""circlepack_magic - IPython/Jupyter cell magic for CirclePack.

Lets you mix **CirclePack commands, Python, and Markdown in one notebook**
running on the ordinary Python kernel. CirclePack itself runs as a separate
GUI process; this magic pipes commands to it over its command socket
(see circlepack_client.py) and renders the resulting canvas inline.

Load it in a notebook:

    %load_ext circlepack_magic
    %circlepack connect                 # connect to CirclePack (localhost:3736)
    %circlepack packdir D:/CirclePack_code/CirclePack/packings   # enable inline images

Then:

    %%circlepack
    seed 8
    max_pack
    disp -w -c

Line magic forms:
    %circlepack connect [host] [port]   connect (CirclePack must be running)
    %circlepack packdir <path>          dir where CirclePack writes files (for images)
    %circlepack name <name>             client name for the handshake
    %circlepack status                  show current settings/connection
    %circlepack disconnect
    %circlepack <command...>            run a one-off command

Notes / current limits:
* Inline images need ``packdir`` set to the directory CirclePack writes to
  (its "packings" directory); the magic has CirclePack write an SVG there
  via ``svg -f`` and displays it. Without ``packdir`` you still get the
  command result code, just no picture.
* The socket returns only a result count per command; query *text*
  (e.g. ``?rad 1``) appears in CirclePack's own shell, not the notebook.
"""
import os
import re
import time

from circlepack_client import CirclePackClient, CirclePackError

try:
    from IPython.core.magic import (Magics, magics_class, line_magic,
                                    cell_magic)
    from IPython.display import SVG, display
    _HAVE_IPYTHON = True
except Exception:  # allow import without IPython (e.g. for unit tests)
    _HAVE_IPYTHON = False
    def magics_class(c):
        return c
    def line_magic(f):
        return f
    def cell_magic(f):
        return f
    Magics = object


_SVG_NAME = "__jupyter_cell__.svg"


class _State:
    def __init__(self):
        self.host = "127.0.0.1"
        self.port = 3736
        self.name = "jupyter"
        self.packdir = os.environ.get("CIRCLEPACK_PACKDIR")
        self.client = None

    def ensure_client(self):
        if self.client is None:
            self.client = CirclePackClient(self.host, self.port, self.name)
            greeting = self.client.connect()
            return greeting
        return None

    def disconnect(self):
        if self.client is not None:
            self.client.close()
            self.client = None


STATE = _State()


def _prep_svg(html, px=420):
    """CirclePack writes HTML-wrapped SVG sized in packing units (e.g.
    width="2.2"), which would render a couple of pixels. Extract the
    <svg>...</svg>, give it a viewBox from the original units and a real
    pixel size so it displays at a usable scale. Returns SVG text or None."""
    m = re.search(r"<svg\b.*?</svg>", html, re.S | re.I)
    if not m:
        return None
    svg = m.group(0)
    om = re.match(r"<svg\b([^>]*)>", svg, re.S | re.I)
    attrs = om.group(1) if om else ""
    wm = re.search(r'width\s*=\s*"([\d.eE+-]+)"', attrs)
    hm = re.search(r'height\s*=\s*"([\d.eE+-]+)"', attrs)
    w = float(wm.group(1)) if wm else 1.0
    h = float(hm.group(1)) if hm else 1.0
    w = w if w > 0 else 1.0
    h = h if h > 0 else 1.0
    ph = int(px * h / w)
    new_open = ('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 %g %g" '
                'width="%d" height="%d">' % (w, h, px, ph))
    return re.sub(r"<svg\b[^>]*>", new_open, svg, count=1, flags=re.S | re.I)


def _show_svg_file(path):
    """Poll for the SVG file CirclePack wrote, then display it inline.
    Returns True if an image was shown."""
    # CirclePack flushes the file slightly after the socket reply; poll for it
    deadline = time.time() + 4.0
    while time.time() < deadline:
        if os.path.isfile(path) and os.path.getsize(path) > 0:
            break
        time.sleep(0.1)
    if not (os.path.isfile(path) and os.path.getsize(path) > 0):
        return False
    try:
        with open(path, "r", encoding="utf-8") as f:
            data = f.read()
        svg = _prep_svg(data)
        if svg and _HAVE_IPYTHON:
            display(SVG(svg))
        return svg is not None
    except OSError:
        return False


@magics_class
class CirclePackMagics(Magics):

    @line_magic
    def circlepack(self, line):
        args = line.split()
        if not args:
            return self._status()
        sub = args[0].lower()

        if sub == "connect":
            if len(args) > 1:
                STATE.host = args[1]
            if len(args) > 2:
                STATE.port = int(args[2])
            STATE.disconnect()
            try:
                greeting = STATE.ensure_client()
                print("connected: %s" % greeting)
            except CirclePackError as e:
                print("ERROR: %s" % e)
            return
        if sub == "disconnect":
            STATE.disconnect()
            print("disconnected")
            return
        if sub == "packdir":
            STATE.packdir = " ".join(args[1:]) or None
            print("packdir = %s" % STATE.packdir)
            return
        if sub == "name":
            STATE.name = args[1] if len(args) > 1 else "jupyter"
            print("name = %s (reconnect to apply)" % STATE.name)
            return
        if sub == "status":
            return self._status()

        # otherwise: run the whole line as a one-off command
        return self._run(line)

    @cell_magic
    def circlepack(self, line, cell):  # noqa: F811  (name reused for cell magic)
        return self._run(cell)

    def _status(self):
        print("CirclePack magic:")
        print("  host:port = %s:%d" % (STATE.host, STATE.port))
        print("  name      = %s" % STATE.name)
        print("  packdir   = %s%s" % (
            STATE.packdir,
            "" if STATE.packdir else "   (set it to enable inline images)"))
        print("  connected = %s" % (STATE.client is not None))

    def _run(self, command):
        try:
            STATE.ensure_client()
        except CirclePackError as e:
            print("ERROR: %s" % e)
            return
        path = None
        full = command
        if STATE.packdir:
            path = os.path.join(STATE.packdir, _SVG_NAME)
            # clear any stale image so we never show a previous cell's picture
            try:
                if os.path.isfile(path):
                    os.remove(path)
            except OSError:
                pass
            # append the SVG export to the SAME command line: when all
            # commands run in one parseWrapper call they finish in order, so
            # the SVG reflects this cell's final packing (separate lines lag).
            full = command.rstrip() + "\nsvg -f " + _SVG_NAME
        try:
            resp = STATE.client.run(full)
        except CirclePackError as e:
            print("ERROR: %s" % e)
            STATE.disconnect()
            return
        if path is not None and _show_svg_file(path):
            return
        # no image: surface the result so the cell isn't silent
        n = CirclePackClient.result_count(resp)
        if n is not None and n < 0:
            print("CirclePack error (result %d)" % n)
        else:
            print(resp)


def load_ipython_extension(ipython):
    ipython.register_magics(CirclePackMagics)


def unload_ipython_extension(ipython):
    STATE.disconnect()
