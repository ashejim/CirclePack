#!/usr/bin/env python3
"""ipynb2cps - convert a Jupyter notebook (.ipynb) to a CirclePack script (.cps).

Inverse of cps2ipynb.py. Walks the notebook cells in order and rebuilds the
``<CP_Scriptfile>`` XML:

* markdown cell, ``metadata.circlepack.type == 'section'`` -> ``<Section>`` (nested by depth)
* markdown cell (text, or a bare ``#`` heading with no metadata) -> ``<text>`` or ``<Section>``
* code cell with ``%%circlepack`` (or marked ``type == 'cmd'``)         -> ``<cmd>``
* code cells that are NOT circlepack (plain Python) are skipped, with a warning
* ``metadata.circlepack.cpdata`` -> ``<CPdata>`` block

Section nesting is reconstructed from ``metadata.circlepack.depth`` when present,
otherwise from markdown heading level (``##`` = depth 0, ``###`` = depth 1, ...).

Usage:
    python ipynb2cps.py INPUT.ipynb [OUTPUT.cps]
"""
import json
import sys


def xml_escape(s):
    return (s.replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace('"', "&quot;"))


def cell_src(cell):
    src = cell.get("source", "")
    return src if isinstance(src, str) else "".join(src)


def cp_meta(cell):
    return (cell.get("metadata", {}) or {}).get("circlepack", {}) or {}


def heading_depth(text):
    """For a bare markdown heading, return (depth, title) or None."""
    t = text.strip()
    if not t.startswith("#"):
        return None
    i = 0
    while i < len(t) and t[i] == "#":
        i += 1
    title = t[i:].strip()
    # '##' -> depth 0, '###' -> depth 1, ...
    return max(0, i - 2), title


class Builder:
    def __init__(self):
        self.lines = []
        self.section_depth = -1  # number of currently-open <Section> = depth+1

    def emit(self, s):
        self.lines.append(s)

    def open_sections_to(self, depth):
        # close deeper sections, then open up to requested depth
        while self.section_depth >= depth:
            self.emit("</Section>")
            self.section_depth -= 1

    def start_section(self, depth, title):
        # close any sections at >= this depth, then open one here
        self.open_sections_to(depth)
        # if we skipped levels, just open at current+1
        self.emit('<Section title="%s">' % xml_escape(title))
        self.section_depth = depth

    def close_all_sections(self):
        while self.section_depth >= 0:
            self.emit("</Section>")
            self.section_depth -= 1


def convert(nb):
    meta = (nb.get("metadata", {}) or {}).get("circlepack", {}) or {}
    title = meta.get("title", "CirclePack script")
    date = meta.get("date", "")
    cpdata = meta.get("cpdata", []) or []

    b = Builder()
    b.emit('<?xml version="1.0"?>')
    b.emit('<CP_Scriptfile date="%s">' % xml_escape(date))
    b.emit('<CPscript title="%s">' % xml_escape(title))

    skipped = 0
    for cell in nb.get("cells", []):
        ct = cell.get("cell_type")
        m = cp_meta(cell)
        src = cell_src(cell)

        if ct == "markdown":
            if m.get("type") == "section":
                b.start_section(int(m.get("depth", 0)),
                                m.get("title", src.lstrip("# ").strip()))
            else:
                hd = heading_depth(src) if not m else None
                if m.get("type") == "text" or m.get("type") is None and hd is None:
                    b.emit("<text> %s </text>" % xml_escape(src.strip()))
                elif hd is not None:  # bare heading, no metadata -> Section
                    b.start_section(hd[0], hd[1])
                else:
                    b.emit("<text> %s </text>" % xml_escape(src.strip()))

        elif ct == "code":
            body = src
            is_cp = m.get("type") == "cmd"
            # strip a leading %%circlepack / %circlepack magic line
            first, _, rest = body.partition("\n")
            if first.strip() in ("%%circlepack", "%circlepack"):
                is_cp = True
                body = rest
            if not is_cp:
                skipped += 1
                continue
            # a cps <cmd> is a single line; join multi-line bodies
            cmd = " ".join(ln.strip() for ln in body.strip().splitlines() if ln.strip())
            attrs = m.get("attrs", {}) or {}
            attr_str = "".join(' %s="%s"' % (k, xml_escape(str(v)))
                               for k, v in attrs.items())
            b.emit("<cmd%s>%s </cmd>" % (attr_str, xml_escape(cmd)))

    b.close_all_sections()
    b.emit("</CPscript>")

    if cpdata:
        b.emit("<CPdata>")
        for d in cpdata:
            tag = d.get("tag", "circlepacking")
            attrs = d.get("attrs") or ({"name": d["name"]} if d.get("name") else {})
            attr_str = "".join(' %s="%s"' % (k, xml_escape(str(v)))
                               for k, v in attrs.items())
            b.emit("<%s%s>%s</%s>" % (tag, attr_str, d.get("body", ""), tag))
        b.emit("</CPdata>")

    b.emit("</CP_Scriptfile>")
    return "\n".join(b.lines) + "\n", skipped


def main(argv):
    if len(argv) < 2:
        print(__doc__)
        return 2
    inp = argv[1]
    out = argv[2] if len(argv) > 2 else (
        inp[:-6] if inp.lower().endswith(".ipynb") else inp) + ".cps"
    with open(inp, encoding="utf-8") as f:
        nb = json.load(f)
    xml, skipped = convert(nb)
    with open(out, "w", encoding="utf-8") as f:
        f.write(xml)
    msg = f"wrote {out}"
    if skipped:
        msg += f" ({skipped} non-CirclePack code cell(s) skipped)"
    print(msg)
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
