# CirclePack I/O Reference

## About this page

CirclePack reads and writes several kinds of data: packing files
(combinatorics, radii, centers, colors, schwarzians, etc.),
triangulations, paths, tilings, custom text data, and images
(PostScript, SVG, PNG). This page covers the command vocabulary
for all of it.

Commands covered: `read`, `Read`, `read_CT`, `Read_CT`, `read_path`,
`Read_path`, `infile_read`, `infile_path`, `load_pack`, `write`,
`Write`, `write_custom`, `Write_custom`, `write_path`, `Write_path`,
`write_tiling`, `Write_tiling`, `output`, `post`, `svg`, `screendump`.

---

## Sections

1. [File-location conventions](#file-location-conventions)
2. [Filename flag syntax](#filename-flag-syntax)
3. [Reading packings](#reading-packings) — `read`, `Read`, `infile_read`, `load_pack`
4. [Reading other data](#reading-other-data) — `read_CT`, `read_path`, `infile_path`
5. [Writing packings](#writing-packings) — `write`, `Write`, the content-flag table
6. [Specialized writes](#specialized-writes) — `write_custom`, `write_path`, `write_tiling`
7. [Structured data output](#structured-data-output) — `output`
8. [Images](#images) — `post` (PostScript), `svg`, `screendump`
9. [Typical workflows](#typical-workflows)
10. [Notes and gotchas](#notes-and-gotchas)
11. [Source](#source)

---

## File-location conventions

CirclePack's file manager tracks three directories:

| Name                       | Purpose |
|----------------------------|---------|
| **`CurrentDirectory`**     | The user's current working directory (set by `cd`). |
| **`HomeDirectory`**        | The user's home directory. `~/...` paths resolve here. |
| **`PackingDirectory`**     | Where packing files are looked for / saved by default. |

Most I/O commands come in two case-sensitive variants:

- **Lowercase** (e.g., `read`, `write`) — uses `PackingDirectory`.
- **Uppercase** (e.g., `Read`, `Write`) — accepts an absolute or
  relative pathname, typically resolving from `HomeDirectory`
  if the filename starts with `~/`.

This convention is pervasive: `read`/`Read`, `write`/`Write`,
`read_path`/`Read_path`, `read_CT`/`Read_CT`, `write_custom`/
`Write_custom`, `write_path`/`Write_path`, `write_tiling`/
`Write_tiling`.

You can change `PackingDirectory` and `CurrentDirectory` via the
GUI or with the `cd` command (not documented here).

---

## Filename flag syntax

Most write commands end with a `-f` or `-a` flag plus a filename:

```
-f {filename}                    # normal write (overwrite if exists)
-a {filename}                    # append to existing file
-s                               # (as an additional flag) write to script
```

Under the hood, `CPFileManager.trailingFile` parses this sequence
and returns a bit-encoded code:

| Bit | Meaning |
|-----|---------|
| 01  | Valid filename present |
| 02  | Append mode (`-a`) |
| 04  | Script mode (`-s` — write to the currently-loaded script file) |

The `-s` flag is incompatible with uppercase variants (e.g., `Write`)
— you can't write into a script when specifying an absolute path.

---

## Reading packings

### `read` / `Read` / `infile_read`

Read a packing file from disk.

```
read {filename}                  # from PackingDirectory
Read {path/to/file}              # absolute or relative path (from ~ or CWD)
infile_read {filename}           # from the currently-loaded script
read -s {filename}               # read from script (alternate form)
```

The file can contain:
- A full **packing file** (standard CirclePack format).
- A **triangulation** file (combinatorics only; radii/centers defaulted).
- A **point set** (used to generate a Delaunay triangulation).

CirclePack tries each format in order, falling back on failure.

### Flags

| Flag     | Meaning |
|----------|---------|
| `-s`     | Read from the currently-loaded script. |
| *nothing* | Read from `PackingDirectory` (lowercase) or given path (uppercase). |

### Examples

```
read unit.p                      # read unit.p from PackingDirectory
Read ~/work/my_packing.p         # absolute path (home directory)
infile_read experiment.p         # from within a loaded script
read -s templates.p              # same as infile_read
```

### What happens after a successful read

- `packData.setName(filename)` — the packing is named.
- If display options are stored in the file, `disp -wr` is called
  automatically.
- `chooseAlpha` / `chooseGamma` run to pick rooting vertices.
- `set_aim_default` and `set_rad_default` set defaults.
- Small constant default radii are set (0.025 Eucl, `1 - exp(-1)` hyp).
- Any embedded `xyzpoints` are preserved; centers set accordingly.

### `load_pack`

Open a **file-chooser dialog** (GUI-only) to select a packing file,
then `Read` it.

```
load_pack                        # change to file's directory after loading
load_pack -f                     # don't change directory
```

| Flag | Meaning |
|------|---------|
| `-f` | "Fix" current directory — don't change after loading. |

Use in interactive sessions; scripts should use `read` or `Read`
instead.

---

## Reading other data

### `read_CT` / `Read_CT`

Read a **combinatorial tiling** file — a custom format describing
a tiling by n-gons rather than triangles.

```
read_CT {filename}
read_CT -q{p} {filename}         # also create simple packing in pack p
read_CT -s {filename}            # read from script
Read_CT {path/to/file}
```

### Flags

| Flag       | Meaning |
|------------|---------|
| `-s`       | Read from script. |
| `-q{p}`    | After reading, build a "simple packing" from the tiling in pack `p` (requires the tiling's dual to be trivalent). |

### File format

The file starts with the vertex count (either a bare integer or
a `CHECKCOUNT: n` line), then per-vertex neighbor lists. See the
source for format details.

### Examples

```
read_CT pent.tile
read_CT -q1 pent.tile            # also populate pack 1 with a packing
```

### `read_path` / `Read_path` / `infile_path`

Read a planar **path** (a curve) from a file into `CPBase.ClosedPath`.

```
read_path {filename}
read_path -s {filename}          # read from script
read_path -a {filename}          # append to existing ClosedPath
Read_path {path}
infile_path {filename}
```

The resulting path is used by `set_grid -g`, `disp -g`,
`elist_to_path`, and other path-aware commands.

### Flags

| Flag | Meaning |
|------|---------|
| `-s` | Read from script. |
| `-a` | Append to existing `ClosedPath` rather than replacing. |

---

## Writing packings

### `write` / `Write`

Save the current packing (or parts of it) to a file.

```
write -{flags} -f {filename}     # write with specified content
Write -{flags} -f {path}         # same, arbitrary directory
write -m -f {filename}           # standard "packing" (combinatorics + radii + centers)
write -s -f {filename}           # write into script
```

The content is controlled by a single flag string whose individual
letters each add a data type to the output.

### Content flag letters

Every content letter is a single character you can combine inside
one flag like `-cgir`. Order doesn't matter; duplicates are harmless.

| Letter | Content added |
|--------|---------------|
| `c`    | Circle colors |
| `g`    | Geometry (Eucl/hyp/sph code) |
| `i`    | Inversive distances |
| `r`    | Radii |
| `z`    | Centers |
| `a`    | Aims |
| `v`    | Vertex/face lists, plot flags |
| `l`    | Face colors |
| `L`    | Expanded face-color list |
| `o`    | Overlaps (older form; use `i` now) |
| `f`    | Face coloring data |
| `t`    | Triangle list |
| `T`    | Tile data (if `tileData` exists) |
| `F`    | Dual faces as a tiling |
| `h`    | Intrinsic schwarzians |
| `e`    | Side-pairing Möbius maps (if present) |
| `x`    | xyz points (requires `set_xyz`) |
| `n`    | Utility list of integers |
| `y`    | Utility list of doubles |
| `w`    | Utility list of complexes |
| `S`    | Schwarzians (if they exist) |
| `A`    | Append mode (same as `-a` on filename) |
| `s`    | Write into the currently-loaded script |
| `m`    | Shortcut: equivalent to `cgirzv` (the "main" packing content) |
| `M`    | Shortcut: like `m`, but adds aims, face colors, and more |

### Examples

```
write -m -f myPacking.p          # standard save (most common)
write -M -f myPacking.p          # save everything
write -crz -f radii_only.p       # just colors + radii + centers
write -i -f invdist.p            # inversive distances only
Write -m -f ~/work/save.p        # save to home directory
write -s -m -f experiment.p      # write into the loaded script
```

### Notes

- **Lowercase `write`** writes to `PackingDirectory`.
- **Uppercase `Write`** accepts an arbitrary path.
- **`-s`** (or the `s` letter) works only with lowercase `write`.
- The default content if no flag letter is given is `020017` —
  roughly equivalent to the `m` shortcut.

---

## Specialized writes

### `write_custom` / `Write_custom`

Write a **custom** data format. Currently supports:

```
write_custom -G {v..} -f {filename}      # 3D-printing grid output
Write_custom -G {v..} -f {path}
```

### Flags

| Flag     | Meaning |
|----------|---------|
| `-G {v..}` | Write the grid (dual graph) structure. Euclidean only. Vertices default to all interior. |
| `-f {file}` | Output filename (required). |
| `-a {file}` | Append instead of overwrite. |

The output contains two sections: `Nodes:` (face centers as (x, y)
pairs) and `Edge pairs:` (index pairs for the dual graph).

### Examples

```
write_custom -G a -f grid.dat
write_custom -G i -f interior_grid.dat
```

### `write_path` / `Write_path`

Write the current `CPBase.ClosedPath` to a file.

```
write_path -f {filename}
Write_path -f {path}
```

Typically used after `set_path` or `read_path` to archive the
working path.

### `write_tiling` / `Write_tiling`

Write a combinatorial tiling to a file. The companion of
`read_CT`.

```
write_tiling -f {filename}
Write_tiling -f {path}
```

---

## Structured data output

### `output`

Write structured text data to a file, driven by a custom
per-object formatting syntax.

```
output {prefix} :: {data} :: {loop} :: {suffix} -f {filename}
```

The four components are separated by `::`:

- **prefix** — text at the start of the file.
- **data** — repeated once, after prefix.
- **loop** — repeated per-object (vertex, face, edge, etc.),
  using format variables.
- **suffix** — text at the end.

The formatting language (variables like `%v`, `%r`, `%c` etc.) is
processed by `OutPanel.outputter`. This is a flexible way to export
packing data in any text format a downstream tool expects.

### Flags

| Flag       | Meaning |
|------------|---------|
| `-f {file}` | Output filename (required). |
| `-a {file}` | Append. |
| `-s`       | Output into script. |

### Examples

```
# Write a CSV of vertex indices and radii
output "vertex,radius" :: "" :: "%v,%r" :: "" -f radii.csv

# Write Mathematica-style face list
output "{" :: "" :: "{%v1,%v2,%v3}" :: "}" -f faces.m
```

See `OutPanel.outputter` source for the full variable list — this
is a specialized micro-DSL.

---

## Images

### `post` — PostScript output

`post` generates PostScript files. It has an **open/close
lifecycle**: first open a file with `-o`, then issue drawing flags
(the same flags as `disp`), then close with `-x`.

```
post                             # GUI-driven: use panel settings
post -o {name} "{description}"   # open a new PostScript file
post -oa {name}                  # open for append
post -oi {name}                  # open for insertion
post -{flags}                    # drawing flags (like disp)
post -x                          # close the file
post -xl                         # close and send to printer (not implemented)
post -xj                         # close and convert to JPEG (not implemented)
post -xg                         # close and open in Ghostview (not implemented)
```

### Flags

| Flag         | Meaning |
|--------------|---------|
| `-o {name}` | Open a new PostScript file. Optional quoted description. |
| `-oa`       | Open for append. |
| `-oi`       | Open for insertion. |
| `-x`        | Close the file. |
| *other*     | Drawing flags — same vocabulary as `disp` (`-c`, `-f`, `-e`, etc.). |

### Typical use

```
post -o output "My Figure"       # open
post -c a -e a -f a              # draw circles, edges, faces
post -x                          # close and write
```

Multi-phase drawing is possible — issue several `post -{flags}`
calls before closing.

### `svg` — SVG output

Write the canvas as an SVG file.

```
svg -f {filename}
```

Simpler than `post` — no open/close cycle. The current canvas view
is written to the file using whatever display state is active.

### Flags

| Flag       | Meaning |
|------------|---------|
| `-f {file}` | Output filename (required). |

### Examples

```
svg -f figure.svg
```

### `screendump` — Raster image dump

Save the canvas as a raster image (PNG by default; format set by
`set_dump_format`).

```
screendump                       # immediate dump to configured file
screendump -m                    # dump the Map Pair frame instead
screendump -d {dir}              # set output directory
screendump -b {basename}         # set output file base name
screendump -n {count}            # set the image counter
```

Successive `screendump` calls produce numbered files (`base_0.png`,
`base_1.png`, etc.) for creating animations or frame sequences.

### Flags

| Flag       | Meaning |
|------------|---------|
| *(none)*   | Immediate dump of active canvas. |
| `-m`       | Dump the Map Pair frame instead. |
| `-d {dir}` | Set output directory (use `~/...` for home). |
| `-b {base}` | Set file base name. |
| `-n {k}`   | Set image counter (caution: may overwrite). |

### Typical use

```
set_dump_format png
screendump -d ~/frames -b anim
screendump                       # anim_0.png
for (t:=0, 9, 1) move -something; screendump
# produces anim_0.png ... anim_9.png
```

---

## Typical workflows

### Save a packing for later

```
max_pack
repack
layout
write -m -f mypack.p             # save everything important
```

### Reload a saved packing

```
read mypack.p
disp -wr                         # view it
```

### Export circle data as CSV

```
output "x,y,r" :: "" :: "%x,%y,%r" :: "" -f circles.csv
```

### Publication-quality PostScript figure

```
post -o figure "Circle packing of disc"
post -w -c a -f a -e a           # same as disp
post -c fc80 b                   # boundary in color 80
post -x
# result: figure.ps in PackingDirectory
```

### Animation frames

```
set_dump_format png
screendump -d ~/anim -b frame -n 0
for (t:=0.0, 1.0, 0.05)
  set_rad _t 5;
  repack;
  layout;
  disp -wr;
  screendump
```

### Load a tiling and build its packing

```
read_CT -q1 mytiling.tile        # build simple packing in pack 1
act 1
max_pack
```

---

## Notes and gotchas

- **Case matters.** `read` uses `PackingDirectory`; `Read` uses
  an absolute or `~/`-relative path. Same for `write`/`Write`,
  `write_custom`/`Write_custom`, etc.
- **`-s` flag requires a loaded script.** Without one, the flag
  silently has no effect (or errors, depending on the command).
- **Uppercase variants can't write to scripts.** `Write -s` is
  explicitly rejected — use lowercase `write -s`.
- **`write` with no flag letter defaults to `020017`** which is
  essentially `-m` (main packing contents).
- **`read` tries three formats** (packing → triangulation → point
  set), so a "successful" read may not give you what you expected
  if the file was corrupt or in a different format.
- **`screendump` format is controlled globally** by
  `set_dump_format`, not per-call.
- **`post` files aren't written until `-x` closes them.** If you
  exit CirclePack or forget `-x`, you may lose output.
- **`svg` is simpler but less capable than `post`.** Only one
  pass, no append or multi-phase drawing.
- **`output`'s format language is a mini-DSL.** Its exact
  variable set isn't fully documented here — see `OutPanel.outputter`
  in the source.
- **`write_path` and `write_tiling` write the **stored** path
  and tiling state** — make sure `set_path` or the equivalent
  has been called first.
- **`load_pack` is GUI-only.** In scripts, use `read` or `Read`
  with an explicit filename.

---

## Source

- `input.ReadWrite` — the main reader/writer
  (`src/input/ReadWrite.java`). Handles standard packing file
  format.
- `combinatorics.komplex.Triangulation.readTriFile` — triangulation
  reader fallback.
- `allMains.CPFileManager` — directory handling, file-open
  helpers, `trailingFile` parser.
- `packing.PackData.writeSVG` — SVG output.
- `frames.OutPanel.outputter` — the `output` command's formatting
  engine.
- `allMains.CPBase.postManager` — the PostScript open/close
  lifecycle manager.
- `input.PathManager.readpath` / `writepath` — path I/O.
- `GUI.ScreenShotPanel.imagePanel` — `screendump` image writer.
- `input.CommandStrParser` — top-level dispatch for each of
  these commands.
