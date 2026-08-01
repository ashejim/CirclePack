#!/usr/bin/env python3
"""circlepack_magic - IPython/Jupyter cell magic for CirclePack.

Lets you mix **CirclePack commands, Python, and Markdown in one notebook**
running on the ordinary Python kernel. CirclePack itself runs as a separate
GUI process; this magic pipes commands to it over its command socket
(see circlepack_client.py) and renders the resulting canvas inline.

Load it in a notebook:

    %load_ext circlepack_magic
    %circlepack connect                 # connect to CirclePack (localhost:3736)

Then:

    %%circlepack
    seed 8
    max_pack
    disp -w -c

Line magic forms:
    %circlepack connect [host] [port]   connect (CirclePack must be running)
    %circlepack packdir <path>          dir where CirclePack writes files (for images)
    %circlepack packdir auto            re-enable auto-detection of that dir
    %circlepack name <name>             client name for the handshake
    %circlepack status                  show current settings/connection
    %circlepack disconnect
    %circlepack <command...>            run a one-off command

Notes / current limits:
* Inline images need to read the SVG CirclePack exports. The magic has
  CirclePack write it (via ``svg -f``) to CirclePack's own **packings**
  directory, then **auto-detects** that directory by finding the freshly
  written file -- searching the notebook's folder and its parents (each and
  their ``packings/`` subdir), then ``~`` and ``~/packings``. So the notebook
  can live in any subfolder; you normally do NOT need to set ``packdir`` by
  hand. Override with ``%circlepack packdir <path>`` if your layout is unusual.
* The socket returns only a result count per command; query *text*
  (e.g. ``?rad 1``) appears in CirclePack's own shell, not the notebook.
"""
import os
import re
import tempfile
import time

from circlepack_client import CirclePackClient, CirclePackError

try:
    from IPython.core.magic import Magics, magics_class, line_cell_magic
    from IPython.display import SVG, display
    _HAVE_IPYTHON = True
except Exception:  # allow import without IPython (e.g. for unit tests)
    _HAVE_IPYTHON = False
    def magics_class(c):
        return c
    def line_cell_magic(f):
        return f
    Magics = object


_SVG_NAME = "__jupyter_cell__.svg"


class _State:
    def __init__(self):
        self.host = "127.0.0.1"
        self.port = 3736
        self.name = "jupyter"
        # packdir: directory the magic reads the exported SVG back from. Left as
        # a best-effort seed; it is auto-detected on the first %%circlepack cell
        # by finding where CirclePack actually wrote the file (_find_fresh).
        self.packdir = os.environ.get("CIRCLEPACK_PACKDIR") or _auto_packdir()
        # if the user set packdir by hand, don't silently move it elsewhere
        self.packdir_manual = False
        # so we announce the detected dir only once per session
        self.announced_dir = None
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


def _auto_packdir():
    """Best-effort initial guess at CirclePack's packings directory: env var,
    then ./packings, then ../packings. This is only a seed -- the real
    directory is discovered from where CirclePack writes (see _find_fresh).
    Returns an abs path or None."""
    for cand in ("packings", os.path.join("..", "packings")):
        if os.path.isdir(cand):
            return os.path.abspath(cand)
    return None


def _candidate_dirs():
    """Ordered, de-duplicated list of existing directories to search for the
    SVG CirclePack just wrote. Covers the notebook's folder and up to a few
    parents (each plus its ``packings/`` subdir), then the home directory and
    ``~/packings`` (CirclePack's default packings location), then the system
    temp dir. STATE.packdir, if known, is checked first as a fast path."""
    seen = []

    def add(d):
        if not d:
            return
        try:
            ad = os.path.abspath(d)
        except Exception:
            return
        if ad not in seen and os.path.isdir(ad):
            seen.append(ad)

    if STATE.packdir:
        add(STATE.packdir)
    d = os.getcwd()
    for _ in range(5):
        add(os.path.join(d, "packings"))
        add(d)
        parent = os.path.dirname(d)
        if parent == d:
            break
        d = parent
    home = os.path.expanduser("~")
    add(os.path.join(home, "packings"))
    add(home)
    add(tempfile.gettempdir())
    return seen


def _clear_stale(fname):
    """Best-effort delete of any leftover ``fname`` in the candidate dirs, so
    the file that appears next is unambiguously from this run (and can't be a
    stale image we'd show by mistake)."""
    for d in _candidate_dirs():
        try:
            p = os.path.join(d, fname)
            if os.path.isfile(p):
                os.remove(p)
        except OSError:
            pass


def _find_fresh(fname, since, timeout=12.0):
    """Poll the candidate directories for a non-empty ``fname`` written at or
    after ``since`` (a time.time() stamp taken just before the command ran).
    The freshness check ignores a leftover file from an earlier cell. Returns
    the full path once found, else None after ``timeout`` seconds. The window
    is generous because a cold first packing can take several seconds to lay
    out and flush the SVG."""
    deadline = time.time() + timeout
    while time.time() < deadline:
        for d in _candidate_dirs():
            p = os.path.join(d, fname)
            try:
                if (os.path.isfile(p) and os.path.getsize(p) > 0
                        and os.path.getmtime(p) >= since - 2.0):
                    return p
            except OSError:
                pass
        time.sleep(0.1)
    return None


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
    # CirclePack writes stroke-width in packing units (~0.5), which becomes a
    # huge line once the ~w-unit drawing is scaled to px pixels. Rescale every
    # stroke to ~1.2 device pixels, expressed in user units (1 px = w/px units).
    sw = 1.2 * w / px
    svg = re.sub(r'stroke-width="[^"]*"', 'stroke-width="%.5f"' % sw, svg)
    new_open = ('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 %g %g" '
                'width="%d" height="%d">' % (w, h, px, ph))
    return re.sub(r"<svg\b[^>]*>", new_open, svg, count=1, flags=re.S | re.I)


def _display_svg_file(path):
    """Read the SVG at ``path`` and display it inline. Returns True if shown."""
    try:
        with open(path, "r", encoding="utf-8") as f:
            data = f.read()
        svg = _prep_svg(data)
        if svg and _HAVE_IPYTHON:
            display(SVG(svg))
        return svg is not None
    except OSError:
        return False


STATE = _State()


@magics_class
class CirclePackMagics(Magics):

    @line_cell_magic
    def circlepack(self, line, cell=None):
        # cell magic ( %%circlepack ): run the cell body as commands
        if cell is not None:
            return self._run(cell)

        # line magic ( %circlepack ... ): sub-commands or a one-off command
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
            STATE.announced_dir = None
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
            rest = " ".join(args[1:]).strip()
            if rest.lower() in ("auto", ""):
                STATE.packdir_manual = False
                STATE.packdir = os.environ.get("CIRCLEPACK_PACKDIR") or _auto_packdir()
                STATE.announced_dir = None
                print("packdir = auto (detected from where CirclePack writes)")
            else:
                STATE.packdir = rest
                STATE.packdir_manual = True
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

    def _status(self):
        print("CirclePack magic:")
        print("  host:port = %s:%d" % (STATE.host, STATE.port))
        print("  name      = %s" % STATE.name)
        print("  packdir   = %s  (%s)" % (
            STATE.packdir if STATE.packdir else "<unset>",
            "manual" if STATE.packdir_manual else "auto-detected on first image"))
        print("  connected = %s" % (STATE.client is not None))

    def _run(self, command):
        try:
            STATE.ensure_client()
        except CirclePackError as e:
            print("ERROR: %s" % e)
            return
        # Append an SVG export to the SAME command line so it reflects this
        # cell's final packing (separate lines lag by one). CirclePack writes it
        # to its own packings directory; we then find it wherever that is.
        full = command.rstrip() + "\nsvg -f " + _SVG_NAME
        _clear_stale(_SVG_NAME)
        since = time.time()
        try:
            resp = STATE.client.run(full)
        except CirclePackError as e:
            print("ERROR: %s" % e)
            STATE.disconnect()
            return
        path = _find_fresh(_SVG_NAME, since)
        if path:
            found_dir = os.path.dirname(path)
            # lock onto wherever CirclePack actually wrote it (unless the user
            # pinned packdir by hand), and announce it once per session
            if not STATE.packdir_manual and STATE.packdir != found_dir:
                STATE.packdir = found_dir
            if STATE.announced_dir != found_dir:
                print("[circlepack] images from packings dir: %s" % found_dir)
                STATE.announced_dir = found_dir
            if _display_svg_file(path):
                return
        # no image: surface the result so the cell isn't silent
        n = CirclePackClient.result_count(resp)
        if n is not None and n < 0:
            print("CirclePack error (result %d)" % n)
        elif path is None:
            print(resp)
            print("[circlepack] no image: could not find %s that CirclePack "
                  "wrote. Set it explicitly with:  %%circlepack packdir <path>"
                  % _SVG_NAME)
        else:
            print(resp)


def load_ipython_extension(ipython):
    ipython.register_magics(CirclePackMagics)


def unload_ipython_extension(ipython):
    STATE.disconnect()
