# CirclePack `disp` Command — Display Packing Objects

## Overview

The `disp` command draws objects — circles, faces, edges, labels, dual
objects, tiles, paths, trinkets, and more — onto the active packing's
canvas. It is the main visualization command in CirclePack and the one
that closes out most workflows after `repack` and `layout`.

A single `disp` call takes one or more **flag segments**, each beginning
with a flag letter (e.g. `-c` for circles, `-f` for faces, `-e` for
edges), followed by optional **display sub-flags** (color, thickness,
fill, labels), followed optionally by a **list of objects** to draw. If
no objects are listed, a reasonable default is used (usually "all").

```
disp -c                    # draw all circles using stored circle colors
disp -c fc80n 3 7 9        # fill circles 3, 7, 9 with color 80, labeled
disp -w -cn a              # clear canvas, draw all circles with labels
```

Four related forms of the command exist:

| Command | Purpose |
|---------|---------|
| `disp`  | Normal display; flags apply, all linked canvasses repaint |
| `Disp`  | Same as `disp` but also **records** the flag string as the stored "display text" on the DisplayPanel — subsequent bare `disp` calls will reuse it |
| `dISp`  | Internal "disp-lite": paint only the active canvas, not secondary canvasses |
| `DISp`  | Combination of `Disp` + `dISp` — records display text and paints only the active canvas |

Users typically type `disp` or `Disp`; `dISp` / `DISp` are invoked by
internal CirclePack code to minimize repaint cost when several pack
views share a drawing thread.

## Activation

`disp` is a built-in command and requires no extender. It operates on
the active packing unless redirected with `-q`.

## Synopsis

```
disp [-w | -wr] [-q{n}] -{flag}[{disp_flags}] [object_list]  [-{flag}...]
Disp [-w | -wr] [-q{n}] -{flag}[{disp_flags}] [object_list]  [-{flag}...]
```

With no flag segments, `disp` uses whatever is currently set in the
DisplayPanel (its checkboxes or its "tailored" flag-string field, if
non-empty).

## Preamble Flags

These flags, when present, must appear **first** and are consumed before
per-object drawing begins.

| Flag | Description |
|------|-------------|
| `-w` | Wipe (clear) the canvas before drawing. Common first flag. |
| `-wr`| Wipe and **re-display** using the DisplayPanel's stored flags. Useful for a quick refresh. |
| `-q{n}` | Target the canvas of packing `n` instead of the active packing. |

**Examples:**

```
# Clear canvas, then draw all circles and all edges
disp -w -c -e

# Clear, then redraw whatever the DisplayPanel says should be drawn
disp -wr

# Draw on packing 1's canvas from the active packing
disp -q1 -c
```

---

## Display Sub-Flags (the "inner" flag language)

After the main flag letter, CirclePack reads an optional compact string
that sets color, fill, labels, thickness, and depth for that drawing
call. Sub-flags can be combined freely.

| Sub-flag | Meaning |
|----------|---------|
| `f`      | Fill the object (using the object's own color, unless overridden) |
| `c`      | Draw the outline **in color** (same color source as fill) |
| `c{N}`   | Use color code `N` (0–255) for the object itself |
| `fc{N}`  | Fill with color `N` |
| `fcc{N}` | Fill **and** draw outline with color `N` |
| `fg`     | Use the current foreground color |
| `bg`     | Use the current background color (e.g. `cbg` draws in background color) |
| `n`      | Label the object with its index |
| `t{N}`   | Line thickness `N` (integer 0–15) |
| `d{N}`   | Depth `N` (used with subdivision tilings) |

When no `c{N}` is given but fill or colored border is requested, the
object's **stored color** is used (e.g. `face.color`, `circle.color`).

**Examples:**

```
disp -c f              # fill circles using stored circle colors
disp -c fc220          # fill all circles with color 220
disp -c fcc220         # fill and outline with color 220
disp -c n              # draw circle outlines and labels
disp -c fc80nt3        # fill color 80, labeled, thickness 3
disp -f fg             # fill faces with foreground color
disp -e cbg t4         # thick edges in background color (erase effect)
```

---

## Display Flags Reference

Flag letters are listed roughly in order of common use. Each section
documents the flag's object type, recognized sub-flags, object list
format, and examples.

### `-c` — Circles

Draw packing circles. Takes a vertex list (see **List Specifiers** for
the full vocabulary of tokens like `a`, `b`, `i`, integer lists, and
stored-list references). Without a list, nothing is drawn.

```
disp -c [disp_flags] {v..}
```

If no color is set and fill (or colored border) is requested, each
circle's stored color is used.

**Examples:**

```
disp -c a                       # all circles (outlines)
disp -c b                       # all boundary circles
disp -c i                       # all interior circles
disp -c fc a                    # all circles filled with stored color
disp -c fc80 a                  # all circles filled with color 80
disp -c n 1 5 12                # circles 1, 5, 12 with labels
disp -c ft2 b                   # boundary circles, filled, thickness 2
disp -c fc Iv 3 7               # fill petal-neighbors of verts 3 and 7
disp -c vlist                   # circles in this packing's stored vlist
disp -w -c a -e a               # clear, draw all circles and all edges
```

---

### `-f` — Faces

Draw packing faces as filled or outlined triangles. Takes a face list
(see **List Specifiers**).

```
disp -f [disp_flags] {f..}
```

The letter `b` plays **two different roles** around this flag — be
aware of the distinction:

1. **`b` as a face-list token** (inside the object list): selects
   all boundary faces.
2. **`b` as a sub-flag** (inside the display-flag string): tells `-f`
   to also draw the **responsible circle** for each face (the circle
   at the vertex opposite the base edge). The parser special-cases
   this so that `bg` (background color) is *not* mistaken for the
   "circle too" directive.

So:

```
disp -f a                       # all face outlines
disp -f b                       # all boundary faces (b is the list token)
disp -fb a                      # all faces, plus their responsible circles
disp -fb 1 2 3                  # faces 1,2,3 plus responsible circles
disp -f bg a                    # all faces drawn in background color
                                # (bg here is a color directive, not "boundary")
```

**Examples:**

```
disp -f a                       # all face outlines
disp -f fc a                    # all faces filled with stored face color
disp -f fc80n a                 # all faces color 80, labeled
disp -f b                       # boundary faces only (list token)
disp -fb 1 2 3                  # faces 1,2,3 plus their responsible circles
disp -f fc A                    # alternating faces, filled (2-coloring)
disp -f If 12                   # faces sharing a full edge with face 12
disp -f fc flist                # faces in the packing's stored flist
```

---

### `-e` — Edges

Draw edges of the triangulation. Takes an edge list (see **List
Specifiers** — edge lists accept `a` for all, `b` for boundary edges,
`Iv {v..}` for edges incident to given vertices, and more). A
sub-character `e` (so `-ee`) means axis-extended edges. With fill,
each edge's stored color is used.

```
disp -e [disp_flags] {v1 w1 v2 w2 ...}
```

**Examples:**

```
disp -e a                       # all edges
disp -e b                       # boundary edges only
disp -e t2 a                    # all edges, thickness 2
disp -e fc120 5 6 5 8           # edges (5,6) and (5,8) in color 120
disp -e Iv 3                    # all edges from vertex 3 (the flower of v=3)
disp -e Ra                      # all red chain edges
disp -e s 1                     # edges of side-pairing 1
disp -ee a                      # axis-extended edges (for sphere/hyp)
```

---

### `-r` — Radial Edge Segments

Draw a halfedge as a radial segment — from the center of the origin
vertex to the tangency point with the other vertex. Assumes a
tangency packing. Takes an edge list (see **List Specifiers**).

```
disp -r [disp_flags] {v1 w1 ...}
```

**Examples:**

```
disp -r a                       # radial segments for all edges
disp -r b                       # radial segments along the boundary
disp -r t2 5 6                  # thick radial segment for edge (5,6)
disp -r Iv 3                    # radials for all edges from vertex 3
```

---

### `-n` — Labels

`-n` is a specialized flag (not a sub-flag modifier of another flag) —
it writes labels onto the canvas.

```
disp -nf {f..}                  # face-index labels at face centers
disp -nv {v..}   |  -nc {v..}   # vertex/circle index labels
disp -nl {v} {str}              # custom label 'str' at center of vertex v
disp -nz {x} {y} {str}          # custom label 'str' at plane point (x,y)
```

**Examples:**

```
disp -nv a                      # label every vertex by index
disp -nf 1 2 3 4                # label faces 1-4
disp -nl 7 "origin"             # write "origin" at vertex 7's center
disp -nz 0.0 0.0 "O"            # write "O" at the origin of the plane
```

Note: `-n` as a **sub-flag** (e.g. `-c n a`) also produces labels;
`-nf`/`-nv`/`-nc`/`-nl`/`-nz` are dedicated label-only flags that do
not draw the underlying object.

---

### `-x` — Coordinate Axes

Toggle the display of coordinate axes.

```
disp -x           # turn axes on
disp -xu          # turn axes off
```

---

### `-C`, `-F`, `-B` — Layout-Order Display

Display circles (`-C`), faces (`-F`), or both (`-B`) in **layout
order**, recomputing each object's position from previously laid-out
neighbors. This is useful for animations and for debugging layout. An
optional `s` after the letter (e.g. `-Cs`) causes schwarzians to be
used in place of radii.

```
disp -C {f..}          # circles in face order, recomputed
disp -F {f..}          # faces in layout order, recomputed
disp -B {f..}          # both circles and faces
disp -Cs {f..}         # use schwarzians
```

With no face list, the packing's `layoutOrder` is used (full layout).
Because a single color is used for the whole pass, `-B` inherits that
color for both circles and faces.

**Examples:**

```
disp -F a                       # replay full layout in face order
disp -Cs 1 2 3 4                # show circles via schwarzians for these faces
```

> **Note:** `-C`, `-F`, `-B` may **change stored centers** as they
> proceed. Use a copy (`copy 0 1`) if you need to preserve the current
> layout.

---

### `-d` — Dual Objects

Draw objects from the packing's dual graph. A second letter selects the
subtype.

| Sub-flag | Dual object drawn |
|----------|------------------|
| `-dc`    | **Dual circles** — face-incircles, indexed by face |
| `-df`    | **Dual faces** — polygons through tangency points around a vertex |
| `-de`    | **Dual edges** — default if no sub-letter given |
| `-dg`    | **Dual edges from face pairs** — takes `<f,g>` graph-edge list |
| `-dG`    | Same as `-dg` but uses the stored `Glink` |
| `-dh`    | **Hull** of dual circles — polygon through tangency points of a face |
| `-dp{N}` | **Trinket** at tangency points, code `N` (0–9), indexed by edge |
| `-dt{N}` | **Trinket** at dual centers, code `N` (0–9), indexed by face |

**Examples:**

```
disp -dc a                      # all face-incircles
disp -dc fc a                   # incircles filled, stored colors
disp -df 5                      # dual face around vertex 5
disp -de a                      # all dual edges
disp -dg 1 2 1 5                # dual edges between face pairs (1,2),(1,5)
disp -dG                        # dual edges from current Glink
disp -dh 3                      # tangency-point hull of face 3
disp -dp3 a                     # trinket 3 at every tangency point
disp -dt5 a                     # trinket 5 at every face's dual center
```

---

### `-D` — Dual Faces, Drawing-Order (reserved)

Currently a placeholder in the source. Draws dual faces recomputed by
drawing order. Not yet implemented.

---

### `-h` — Vertex Hulls

Polygons defined by the tangency points of `v` with its neighbors.
Takes a vertex list (see **List Specifiers**).

```
disp -h [disp_flags] {v..}
```

**Examples:**

```
disp -h a                       # hulls of all vertices
disp -h b                       # hulls of boundary vertices only
disp -h fc a                    # filled with stored circle colors
disp -h n 1 5 9                 # outlined hulls, labeled, for verts 1,5,9
```

---

### `-P` — Pavers

Polygons defined by the **outer edges** of faces surrounding a vertex
(see the `pave` command). Takes a vertex list (see **List Specifiers**).

```
disp -P [disp_flags] {v..}
```

**Examples:**

```
disp -P a                       # all pavers
disp -P i                       # pavers of interior vertices only
disp -P fc n a                  # filled and labeled
```

---

### `-R` — Side Pairings

Display the side-pairings of the red chain. Accepts sub-options `p`,
`n`, `c`.

| Sub-option | Effect |
|-----------|--------|
| `p`       | Also draw the paired ("mate") side |
| `n`       | Label the side indices |
| `c`       | Also draw the boundary circles of each side |

Side list: integer indices, or `a` / empty for all.

```
disp -R {side..}
disp -R[pnc] {side..}
```

**Examples:**

```
disp -R a                       # all side-pairings
disp -Rp a                      # each side and its mate
disp -Rpnc a                    # sides + mates + labels + circles
disp -R 1 3                     # only sides 1 and 3
```

---

### `-s` — Shape from Vertex Geodesic

Draw a closed polygon defined by the combinatorial geodesics through
a vertex list. Useful for outlining combinatorial regions.

A variant `-sz` reads a list of complex points (not vertices) and
draws the resulting polygon.

```
disp -s {v..}                   # combinatorial shape through verts
disp -sz {z..}                  # shape through points z1, z2, ...
```

**Examples:**

```
disp -s 1 5 10 15               # polygon with corners at those verts
disp -sz 0 0 1 0 0.5 0.8        # triangle at three points
```

---

### `-t` — Trinkets at Points

Draw small icons ("trinkets" — dots, crosses, squares, etc.; codes 0–9)
at specified points. Trinket code is part of the flag letter itself
(`-t3` = trinket code 3).

```
disp -t{N} [disp_flags] {v..}           # at vertex centers (vertex list)
disp -t{N}f {face} {b1 b2 b3} ...       # at barycentric coords in a face
disp -t{N}z {x y} ...                   # at plane point(s)
```

With a vertex list, a trinket is drawn at each vertex center — so the
vertex list accepts any of the vertex-list tokens (see **List
Specifiers**). The `-t{N}f` form takes a barycentric-point list
(`BaryLink`); `-t{N}z` takes a complex-point list.

**Examples:**

```
disp -t3 a                      # trinket 3 at every vertex center
disp -t3 b                      # trinket 3 at every boundary vertex
disp -t5 1 7 12                 # trinket 5 at verts 1, 7, 12
disp -t2f 4 0.3 0.3 0.4         # trinket 2 inside face 4 at bary coords
disp -t1z 0.5 0.5               # trinket 1 at plane point (0.5, 0.5)
```

---

### `-T` — Tiles

Display tiles (only applies if the packing has a `tileData` structure).
Takes a tile list (see **List Specifiers** — tile lists accept `a`,
`b`, `i`, `c {t..}` for children, `It {t..}`, and more).
Defaults to all tiles if no list given. The `ConformalTiling`
extender offers richer tile display.

```
disp -T [disp_flags] {t..}
```

**Examples:**

```
disp -T a                       # all tiles
disp -T b                       # boundary tiles only
disp -T i                       # interior tiles only
disp -T fc a                    # tiles filled with stored tile colors
disp -T n a                     # tile outlines, labeled
disp -T c 1 2 3                 # children of tiles 1, 2, 3
```

---

### `-u` — Unit Circle

Draw the unit circle (centered at origin, radius 1 in Euclidean /
hyperbolic; `π/2` in spherical). No object list.

```
disp -u [disp_flags]
```

**Examples:**

```
disp -u                         # thin unit circle
disp -u t3                      # thick unit circle
disp -u c160                    # unit circle in color 160
```

---

### `-y` — Circumscribing Circles (Delaunay-style)

For each face in the list, draw the Euclidean / hyperbolic / spherical
circle through its three vertex centers. Takes a face list (see
**List Specifiers**). Defaults to all faces.

```
disp -y [disp_flags] {f..}
```

**Examples:**

```
disp -y a                       # circumscribing circles of all faces
disp -y c80 1 5 9               # for faces 1,5,9 in color 80
disp -y b                       # boundary faces only
```

---

### `-a` — Face Sectors (Euclidean only)

For each face, draw the three circular sectors meeting at face corners.
Takes a face list (see **List Specifiers**). Only meaningful in
Euclidean geometry.

```
disp -a [disp_flags] {f..}
```

**Examples:**

```
disp -a a                       # sectors for all faces
disp -a c200 1 2 3              # sectors for faces 1,2,3 in color 200
disp -a b                       # sectors for boundary faces only
```

---

### `-b` — Bary-Coord Paths

Draw paths defined by barycentric coordinates, stored in
`CPBase.gridLines`. A `-bs` variant uses `CPBase.streamLines` instead.

```
disp -b [disp_flags]            # gridLines
disp -bs [disp_flags]           # streamLines
```

Takes the current stored color/thickness if `disp_flags` include them.

**Examples:**

```
disp -b                         # draw grid lines
disp -bs c100 t2                # stream lines, color 100, thickness 2
```

---

### `-g` — Closed Path

Draw the packing's stored `ClosedPath` (set by `path_construct` and
related commands). Defaults to blue, thickness 3 if no color/thickness
given.

```
disp -g [disp_flags]
```

**Examples:**

```
disp -g                         # draw stored path in blue
disp -g c200 t5                 # draw it in color 200, thickness 5
```

---

## Typical Workflow

```
# 1. Max-pack, lay it out, display
max_pack
repack
layout
disp -w -c                      # clear, draw circles

# 2. Add edges on top
disp -e

# 3. Show faces filled with stored colors
disp -w -f fc a

# 4. Overlay the unit circle thick
disp -u t3

# 5. Redraw with a saved DisplayPanel configuration
disp -wr

# 6. Show side pairings with mates and labels
disp -Rpn a

# 7. Use Disp to remember the current settings for future bare `disp`
Disp -w -c fc a -e t1

# 8. Now a bare `disp` replays that:
disp

# 9. Quick scripted idiom: repack-layout-disp
rld            # shorthand for: repack; layout; disp -wr
```

---

## Tips & Common Patterns

**Clear before drawing.** Without `-w`, drawings accumulate on the
canvas — sometimes desirable (overlays), sometimes not. Prefer
`disp -w ...` when you want a clean frame.

**Full redraw after changes.** After `repack`, `layout`, or combinatorial
edits, use `rld` (repack + layout + disp -wr) to update everything.

**Persistent display text.** `Disp -w -f fc a -e t1` both draws and
stores the flags as display text; later bare `disp` (or `disp -wr`)
replays them.

**Canvas redirection.** `disp -q2 -c a` draws the active packing's
circles into packing 2's canvas, without touching packing 2's actual
data. Good for comparing packings side-by-side.

**Combining flag segments.** One `disp` call can take many segments:

```
disp -w -c fc a -e t2 -nv a -u
```

draws (in order) a clear canvas, filled circles, thick edges, vertex
labels, and the unit circle.

**Color codes.** CirclePack color indices run 0–255 and come from
`ColorUtil.coLor()`. Standard colors:
- `0` ≈ red, `80` ≈ green, `160` ≈ blue, `200` ≈ yellow
- `fg` = foreground (usually black), `bg` = background (usually white)

**Labels can trip tight diagrams.** `n` sub-flag adds an index number
at the object's center; for dense packings consider using `t1`
(minimum thickness) and skipping labels, or labeling only selected
objects.

---

## Relationship to Other Commands

- **`layout`** computes positions; `disp` reads them. If `disp` shows
  nothing or shows garbage, `layout` may be stale.
- **`set_disp_flags {flags}`** sets the stored "tailored" display
  string the DisplayPanel uses when `disp` is called with no flags.
  The `Disp` command variant does this automatically from the flags
  you give it.
- **`color`** sets stored colors on circles, faces, and edges; `disp`
  uses those when `f` or `c` is requested without an explicit `c{N}`.
- **`screendump`** (and PostScript output via `set_ps` / `output`)
  captures whatever `disp` most recently drew.

---

## Notes

- All `disp` flags can appear in any order **after** any preamble
  (`-w`, `-wr`, `-q{n}`).
- Sub-flag letters inside one flag segment are order-free: `fc80n`,
  `nfc80`, and `c80fn` are equivalent.
- `-c` without an object list does nothing; always supply a list or
  use `a` for all.
- `-T` requires an existing `tileData` structure; otherwise it is a
  no-op.
- The `d{N}` depth sub-flag is primarily consumed by the tiling
  extensions and has no effect on standard circle/face drawing.
- For bulk custom rendering beyond what `disp` provides, see the
  PostScript output commands (`set_ps`, `post`, `output`) and the
  screen-capture `screendump`.

---

## Source

- `canvasses.DisplayParser.dispParse()` — the per-segment flag
  dispatch (`/src/canvasses/DisplayParser.java`).
- `util.DispFlags` — parses the inner sub-flag language
  (color, fill, thickness, labels).
- `util.DispOptions` — holds DisplayPanel defaults used when
  `disp` is called with no flags.
- `input.CommandStrParser` — top-level dispatch for `disp` /
  `Disp` / `dISp` / `DISp`, including `-w`, `-wr`, `-q`.
