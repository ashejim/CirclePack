#!/usr/bin/env python3
"""cps2ipynb - convert a CirclePack script (.cps) to a Jupyter notebook (.ipynb).

A CirclePack ``.cps`` script is XML with this shape::

    <CP_Scriptfile date="...">
      <CPscript title="...">
        <text> narrative ... </text>
        <Section title="...">
          <text> ... </text>
          <cmd iconname="..." name="..." mnemonic="...">act 0;seed 8; </cmd>
          <Section title="..."> ... </Section>
        </Section>
      </CPscript>
      <CPdata>
        <circlepacking name="foo.p"> ...packing... </circlepacking>
      </CPdata>
    </CP_Scriptfile>

This maps cleanly onto a notebook:

* ``<text>``     -> markdown cell
* ``<Section>``  -> markdown heading cell ("##", "###", ... by depth)
* ``<cmd>``      -> code cell ``%%circlepack`` + the command line
* ``<CPdata>``   -> stored in notebook ``metadata.circlepack.cpdata`` (so it
                    round-trips and a future kernel/magic can materialize it)

The ``%%circlepack`` magic makes the code cells runnable in a normal Python
kernel alongside Python and Markdown cells (see circlepack_magic.py); strip it
back out with ipynb2cps.py.

Usage:
    python cps2ipynb.py INPUT.cps [OUTPUT.ipynb]
"""
import json
import sys
import xml.etree.ElementTree as ET


def _text(el):
    return (el.text or "").strip()


def md_cell(source, cp_meta):
    return {
        "cell_type": "markdown",
        "metadata": {"circlepack": cp_meta},
        "source": _split_lines(source),
    }


def code_cell(source, cp_meta):
    return {
        "cell_type": "code",
        "metadata": {"circlepack": cp_meta},
        "execution_count": None,
        "outputs": [],
        "source": _split_lines(source),
    }


def _split_lines(s):
    # nbformat stores source as a list of lines, each ending in '\n'
    # except (conventionally) the last.
    lines = s.split("\n")
    return [ln + "\n" for ln in lines[:-1]] + [lines[-1]]


def walk(node, depth, cells):
    """Recurse over CPscript children, emitting notebook cells in order."""
    for child in node:
        tag = child.tag
        if tag == "text":
            txt = _text(child)
            if txt:
                cells.append(md_cell(txt, {"type": "text"}))
        elif tag == "cmd":
            cmd = _text(child)
            attrs = {k: v for k, v in child.attrib.items()}
            meta = {"type": "cmd"}
            if attrs:
                meta["attrs"] = attrs
            cells.append(code_cell("%%circlepack\n" + cmd, meta))
        elif tag == "Section":
            title = child.attrib.get("title", "Section")
            level = min(depth + 2, 6)  # top Section -> "##"
            cells.append(md_cell("#" * level + " " + title,
                                 {"type": "section", "depth": depth,
                                  "title": title}))
            walk(child, depth + 1, cells)
        # ignore unknown/administrative tags (description, header, ...)


def parse_cpdata(root):
    data = []
    for cpdata in root.findall("CPdata"):
        for pk in cpdata:
            # element tag is e.g. 'circlepacking'; keep tag + name + body
            data.append({
                "tag": pk.tag,
                "name": pk.attrib.get("name", ""),
                "attrs": {k: v for k, v in pk.attrib.items()},
                "body": pk.text or "",
            })
    return data


def convert(cps_path):
    tree = ET.parse(cps_path)
    root = tree.getroot()  # CP_Scriptfile
    script = root.find("CPscript")
    if script is None:
        raise SystemExit("no <CPscript> element found; not a CirclePack script?")

    title = script.attrib.get("title", "CirclePack script")
    date = root.attrib.get("date", "")

    cells = []
    # leading intro text + sections, in document order
    walk(script, 0, cells)
    # nbformat 4.5 wants a unique id on each cell
    for i, c in enumerate(cells):
        c["id"] = "c%d" % i

    nb_meta = {
        "kernelspec": {
            "display_name": "Python 3",
            "language": "python",
            "name": "python3",
        },
        "language_info": {"name": "python"},
        "circlepack": {
            "title": title,
            "date": date,
            "cpdata": parse_cpdata(root),
        },
    }
    return {
        "cells": cells,
        "metadata": nb_meta,
        "nbformat": 4,
        "nbformat_minor": 5,
    }


def main(argv):
    if len(argv) < 2:
        print(__doc__)
        return 2
    inp = argv[1]
    out = argv[2] if len(argv) > 2 else (
        inp[:-4] if inp.lower().endswith(".cps") else inp) + ".ipynb"
    nb = convert(inp)
    with open(out, "w", encoding="utf-8") as f:
        json.dump(nb, f, indent=1, ensure_ascii=False)
    n_code = sum(1 for c in nb["cells"] if c["cell_type"] == "code")
    n_md = sum(1 for c in nb["cells"] if c["cell_type"] == "markdown")
    n_data = len(nb["metadata"]["circlepack"]["cpdata"])
    print(f"wrote {out}: {n_md} markdown + {n_code} code cells, "
          f"{n_data} embedded data file(s)")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
