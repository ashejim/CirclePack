# cptools — CirclePack ↔ Jupyter tools

Treat CirclePack scripts like notebooks, and drive CirclePack from a Jupyter
notebook **alongside Python and Markdown**.

A CirclePack `.cps` script is essentially a notebook already: narrative
`<text>` blocks interleaved with executable `<cmd>` cells, grouped into
`<Section>`s, with embedded data in `<CPdata>`. These tools make that
correspondence concrete.

| Tool | What it does |
|------|--------------|
| `cps2ipynb.py` | `.cps` → `.ipynb` (Jupyter notebook) |
| `ipynb2cps.py` | `.ipynb` → `.cps` (round-trips with the above) |
| `circlepack_client.py` | Python client for CirclePack's command socket |
| `circlepack_magic.py` | `%%circlepack` cell magic: run CP commands in a notebook, see the canvas inline |

Requirements: Python 3.8+. The converters use only the standard library.
The magic additionally needs IPython/Jupyter (`pip install jupyterlab`) and a
**running CirclePack** (see below).

> **New here? Open [`demo_circlepack.ipynb`](demo_circlepack.ipynb)** — a guided,
> runnable notebook that walks through setup and shows Markdown + Python +
> `%%circlepack` cells working together.

---

## Quick start (Windows, no git needed)

Grab two files from the
[latest release](https://github.com/ashejim/CirclePack/releases): the app jar
`CirclePack-<version>.jar` and the demo bundle `circlepack-jupyter-demo.zip`.
**Unzip the bundle and drop the jar into the unzipped folder** (so the jar sits
next to the notebook and the `packings/` folder). Then, in that folder
(substitute the actual jar filename for `CirclePack-<version>.jar` below):

```powershell
# one-time tools (signed installers; Smart App Control friendly)
winget install Microsoft.OpenJDK.17
winget install astral-sh.uv
```

CirclePack and Jupyter are **two long-running processes**, so use **two
terminals** (each command below stays running and holds its window).

```powershell
# TERMINAL 1 — start CirclePack (this window is now busy running the GUI)
java -jar CirclePack-<version>.jar
```

```powershell
# TERMINAL 2 — start JupyterLab (launched via signed python -m to avoid Smart App Control)
uv run --python 3.12 --with jupyterlab --with ipykernel python -m jupyterlab
```

Prefer one terminal? Launch CirclePack **detached** (or just double-click the
jar in Explorer) so the window frees up for the Jupyter command:

```powershell
Start-Process java -ArgumentList '-jar','CirclePack-<version>.jar'
```

Open `demo_circlepack.ipynb` and run the cells. Because CirclePack and Jupyter
share the folder, `packdir` **auto-detects** `./packings` — no path to edit.

(pip users instead of uv: `pip install -r requirements.txt`, then
`python -m jupyterlab`.)

---

## 1 & 2. Converting between `.cps` and `.ipynb`

```bash
python cps2ipynb.py ../scripts/First_script.cps        # -> First_script.ipynb
python ipynb2cps.py First_script.ipynb my_script.cps   # back to .cps
```

Mapping:

| `.cps` | `.ipynb` |
|--------|----------|
| `<text>` | markdown cell |
| `<Section title=…>` (nested) | markdown heading cell (`##`, `###`, …) |
| `<cmd …>cmds;</cmd>` | code cell: `%%circlepack` + the command line |
| `<CPdata>` embedded packings | notebook `metadata.circlepack.cpdata` |
| `title`, command attrs (`iconname`/`name`/`mnemonic`) | preserved in metadata |

The conversion round-trips: `cps → ipynb → cps` reproduces the commands,
section structure, and embedded data. Code cells are written with a
`%%circlepack` first line so the notebook is runnable (see below); pure-Python
code cells you add are skipped on the way back to `.cps`.

---

## 3. Running CirclePack inside Jupyter (alongside Python + Markdown)

CirclePack runs as its own GUI process; the notebook talks to it over its
command socket. So one notebook can freely mix **Markdown cells, Python
cells, and `%%circlepack` cells**.

### Setup

1. **Start CirclePack with its socket server** (it listens on port 3736):

   ```
   runCP                 # the GUI starts the socket server automatically
   # or explicitly:  runCP -socket 3736
   ```

2. **Start Jupyter** in a **second terminal**, from this `cptools/` folder (so
   the `circlepack_*.py` modules import). Either frontend works — JupyterLab is
   not required:

   ```
   uv run --python 3.12 --with jupyterlab --with ipykernel python -m jupyterlab
   # ...or classic Notebook 7:  ...--with notebook ... python -m notebook
   # pip alternative: pip install -r requirements.txt && python -m jupyterlab
   ```

3. In a notebook cell:

   ```python
   %load_ext circlepack_magic
   %circlepack connect
   %circlepack status     # shows the auto-detected packdir
   ```

   `packdir` (the directory CirclePack writes files to, its **packings**
   directory — often `~/packings`, *not* the notebook's folder) is
   **auto-detected**: on the first `%%circlepack` cell the magic finds the SVG
   CirclePack just wrote by searching this notebook's folder and its parents
   (each plus a `packings/` subdir), then `~` and `~/packings`, and locks onto
   it. So the notebook may live in any subfolder — no path to configure. If
   detection fails (the cell prints `no image: could not find …`), read the
   `Wrote SVG image to <PATH>` line in CirclePack's message window and set it:
   `%circlepack packdir <path>` (`%circlepack packdir auto` re-enables
   auto-detection). Without a resolvable directory you still get each command's
   result code, just no inline picture.

### Use

````markdown
This is a Markdown cell explaining the experiment.
````

```python
# a normal Python cell
import math
print(math.pi)
```

```
%%circlepack
seed 8
max_pack
disp -w -c
```

The `%%circlepack` cell runs the commands and renders the resulting packing
inline. Line-magic forms:

```
%circlepack connect [host] [port]
%circlepack packdir <path>
%circlepack name <name>
%circlepack status
%circlepack disconnect
%circlepack <one-off command>
```

### How it works / limitations

* The magic sends a cell's commands **plus an `svg` export as one command
  line**, so the image reflects that cell's final packing. (Sent as separate
  lines, CirclePack replies before the state settles and the image would lag
  by one cell.)
* The socket returns only a result count per command. Query **text**
  (e.g. `?rad 1`, `count -v`) goes to CirclePack's own shell window, not the
  notebook. A richer protocol could pipe messages back later.
* CirclePack serves one socket client at a time; the magic holds one
  persistent connection for the whole notebook session.
* Inline images use the active packing's circles (via CirclePack's `svg`
  command). Faces/edges/colors beyond circle outlines aren't in the SVG yet.

---

## Status

Verified: the converters (round-trip on `First_script.cps`, output validates
as nbformat 4.5); the socket client and magic against a live CirclePack
(commands execute and per-cell SVGs render correctly).

Related fix: `src/input/SocketSource.java` had a null-dereference that killed
the socket server when a client disconnected — fixed so the server survives
reconnects (needed for repeated notebook sessions).
