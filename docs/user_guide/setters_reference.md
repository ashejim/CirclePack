# CirclePack Setters Reference

## About this page

CirclePack has roughly forty `set_*` commands for modifying various
pieces of packing data, display state, and environment settings.
Rather than a page per setter, they're grouped here by theme since
most are small and share vocabulary.

Each entry has a synopsis, a brief description, and a short example.
Setters that are substantial enough to warrant their own page
(`set_aim`, `set_rad`, `set_schwarzian`, `set_invdist`, `set_Mobius`,
`set_screen`) are summarized here and may get dedicated pages later.

Sections:

1. [List-state setters (`set_vlist` etc.)](#list-state-setters)
2. [Geometry setters](#geometry-setters)
3. [Display setters](#display-setters)
4. [Packing-data setters](#packing-data-setters)
5. [Path and function setters](#path-and-function-setters)
6. [Environment and preferences](#environment-and-preferences)
7. [Variables](#variables)

---

## List-state setters

CirclePack maintains named stored lists that many commands consume.
Each list has a lowercase (per-packing) and uppercase (global)
variant — the lowercase ones live on `packData` and are lost when
you switch packings; the uppercase ones live in `CPBase` and
persist across pack switches.

All of these go through a single unified parser. The syntax is
uniform:

```
set_{name}list {list specifier}
```

where `{list specifier}` uses the standard list vocabulary — see
**List Specifiers**. Empty arguments clear the list.

### The nine list setters

| Setter | Stored in | Type | Description |
|--------|-----------|------|-------------|
| `set_vlist` | `packData.vlist` | NodeLink | Per-packing vertex list |
| `set_Vlist` | `CPBase.Vlink`   | NodeLink | Global vertex list |
| `set_elist` | `packData.elist` | EdgeLink | Per-packing edge list |
| `set_Elist` | `CPBase.Elink`   | EdgeLink | Global edge list |
| `set_hlist` | `packData.hlist` | HalfLink | Per-packing half-edge list |
| `set_Hlist` | `CPBase.Hlink`   | HalfLink | Global half-edge list |
| `set_flist` | `packData.flist` | FaceLink | Per-packing face list |
| `set_Flist` | `CPBase.Flink`   | FaceLink | Global face list |
| `set_tlist` | `packData.tlist` | TileLink | Per-packing tile list |
| `set_Tlist` | `CPBase.Tlink`   | TileLink | Global tile list |
| `set_glist` | `packData.glist` | GraphLink | Per-packing dual-graph list |
| `set_Glist` | `CPBase.Glink`   | GraphLink | Global dual-graph list |
| `set_blist` | `packData.blist` | BaryLink | Per-packing barycentric-point list |
| `set_Blist` | `CPBase.Blink`   | BaryLink | Global barycentric-point list |
| `set_zlist` | `packData.zlist` | PointLink | Per-packing complex-point list |
| `set_Zlist` | `CPBase.Zlink`   | PointLink | Global complex-point list |
| `set_Dlist` | `CPBase.Dlink`   | DoubleLink | Global list of doubles |

Note: `z`, `D` have only a global form in the current dispatch;
`set_zlist` exists per-packing but `set_dlist` does not.

### Examples

```
set_vlist b                      # per-packing vlist = all boundary vertices
set_Vlist a                      # global Vlink = all vertices
set_vlist                        # clear per-packing vlist
set_elist {v,w: some condition}  # set via set-builder
set_flist Iv 5                   # faces at vertex 5
set_Tlist a                      # global tile list = all tiles
```

After setting, any command that reads `vlist` (like `disp -c vlist`
or `color -c vlist`) uses what you've stored.

### Return

All list setters return the size of the resulting list (or 1 if
cleared to empty). Useful in scripts:

```
n := {set_vlist {c: d .gt. 6}}   # n now holds the count
```

---

## Geometry setters

Modify the numerical packing data: aims, radii, centers, inversive
distances, schwarzians, and randomization.

### `set_aim` — Set target angle sums

```
set_aim [flag] {x} {v..}
```

Forms:

- `set_aim {x} {v..}` — set aim to `x` radians for listed vertices
- `set_aim -d` or `set_aim -d {v..}` — reset to default (2π interior, -1 boundary)
- `set_aim -c` or `set_aim -c {v..}` — freeze current angle sum as aim
- `set_aim -% {x} {v..}` — multiply current aims by factor `x` (x > 0)
- `set_aim -x {v..}` — use xyz data to compute aims
- `set_aim -t {x} {v..}` — move interior aims toward 2π by factor `x` (0 < x ≤ 1)
- `set_aim -a {x} {v..}` — add `x` to current aims (may be negative)

The most common uses are `set_aim -d` (reset) and the bare form
`set_aim 4*π 5` to create a branch point.

Examples:

```
set_aim -d                       # all defaults
set_aim 12.566 5                 # create ~4π branch point at vertex 5
set_aim -% 0.5 i                 # halve all interior aims
set_aim -c b                     # freeze boundary angle sums
```

---

### `set_rad` — Set radii

```
set_rad {x} {v..}                           # direct: set radii to x
set_rad -q{p} -{mode} {v..}                 # from another packing
```

Direct form sets the radius of every listed vertex to the value
`x`. The `-q{p}` form copies or transforms radii from another
packing; modes include raw copy and ratio-based transformations
(details in the `set_rad` handler).

Examples:

```
set_rad 0.2 a                    # all radii = 0.2
set_rad 0.05 b                   # boundary radii = 0.05
set_rad -q1 -r a                 # copy radii from pack 1
```

---

### `set_center` — Set circle centers

```
set_center {x y} {v..}           # absolute center (complex point)
set_center -f {v..}              # use function panel value at each center
set_center -x {v..}              # use xyz data (first two coords)
```

Directly sets centers without rerunning layout. Rarely needed for
normal work — prefer `layout` or `Mobius` for principled edits.

```
set_center 0.0 0.0 1             # put vertex 1 at origin
set_center -f a                  # apply function in Function panel to all centers
```

---

### `set_invdist` — Set inversive distances (overlaps)

```
set_invdist {x} {v w ...}        # set invdist to x for listed edges
set_invdist -d {v w ...}         # reset to default (1, tangency)
set_invdist -c {v w ...}         # freeze current invdist
set_invdist -t                   # tile-based
set_invdist -h                   # from xyz, local edge lengths
set_invdist -x [filename]        # from xyz data, optionally loaded from file
```

Recall the sign convention:

| `x` value | Meaning |
|-----------|---------|
| `1`       | Tangent (default) |
| `(0, 1)`  | Overlap (angle in (0, π/2)) |
| `[-1, 0)` | Deep overlap (angle in (π/2, π]) |
| `(1, ∞)`  | Separated (distance) |
| `-1`      | Internally tangent |

Examples:

```
set_invdist 1.0 a                # tangency everywhere
set_invdist 0.5 Iv 5             # modest overlap around vertex 5
set_invdist -d                   # back to tangency
```

### `set_overlaps` — Deprecated alias

Routes to `set_invdist`. Old scripts may use it. Prefer
`set_invdist` in new scripts.

---

### `set_schwarzian` — Set schwarzians on edges

```
set_schwarzian                   # no args: compute from current geometry
set_schwarzian {v w ...}         # compute for listed edges
set_schwarzian -s {x} {v w ...}  # set explicitly to x
set_schwarzian -u {x} {v w ...}  # set to (1 - x)
```

Without flags, computes schwarzians from the current packing
(requires a valid tangency packing). With `-s` or `-u`, sets to
an explicit value — useful for experiments. Boundary edges
always get schwarzian 0.

Often called through the shortcut `set_sch` used inside the
`rlsd` macro.

Examples:

```
set_schwarzian                   # compute all
set_schwarzian -s 0.0 a          # zero everything
set_schwarzian -s 0.1 Iv 5       # set a value near vertex 5
```

---

### `set_random` — Randomize radii or overlaps

```
set_random [-o] [-j {x}] [-f] [-r {low} {high}] {v..}
```

Flags:
- `-o` — randomize **overlaps** (inversive distances) instead of radii
- `-j {x}` — "jiggle": multiply by random factor near 1 with gaussian spread x% (clamped to ±10)
- `-f` — factor mode: multiply existing values by random factor
- `-r {low} {high}` — random value in range [low, high]

With no flags, sets radii uniformly in [0.005, 5.0] (default
range).

Examples:

```
set_random a                     # randomize all radii uniformly
set_random -j 5 a                # jiggle all by ~5%
set_random -o -j 2 a             # jiggle overlaps by ~2%
set_random -r 0.1 0.5 i          # interior radii in [0.1, 0.5]
```

Useful for perturbation experiments and for generating
initial-conditions diversity.

---

### `set_xyz` — Set xyz data

```
set_xyz                          # populate from current packing
```

Builds the `xyzpoint` array from current packing centers. Used
as input to commands with xyz modes (`set_aim -x`, `set_invdist -x`,
`set_center -x`, etc.).

---

### `set_ratio` — Boundary radii via function

```
set_ratio {p1} {p2}
```

Sets boundary radii of `p2` to boundary radii of `p1` multiplied
by the **current function** (from the Function panel) evaluated
at each center. Requires both packings Euclidean and the function
to be valid.

Used in conformal-deformation experiments. Not commonly seen
outside that context.

---

## Display setters

### `set_screen` — Canvas viewbox

```
set_screen -a                    # auto-adjust to see all circles (eucl only)
set_screen -b {lx ly rx ry}      # set real box (lower-left, upper-right)
set_screen -d                    # default canvas and sphere view
set_screen -f {x}                # scale current view by factor x
set_screen -i {dx dy}            # translate incrementally
```

Controls the visible region of the canvas without changing
packing data. `-a` is the easiest zoom-to-fit; `-d` resets to
the CirclePack default.

```
set_screen -a                    # make everything visible
set_screen -b -2 -2 2 2          # view [-2,2] x [-2,2]
set_screen -f 2                  # zoom in 2x
```

---

### `set_sphere_view` / `set_sv` — Sphere view matrix

```
set_sphere_view -d               # default view
set_sphere_view -t m00 m01 ... m22   # set viewMatrix (9 doubles)
set_sphere_view -N               # look at north pole
set_sphere_view -S               # look at south pole
set_sphere_view -i {xa ya za}    # incremental angles (× π)
set_sphere_view {xa ya za}       # absolute angles (× π)
```

`set_sv` is a shorter alias.

Spherical packings' apparent orientation is governed by this
matrix; these commands rotate the view without changing the
packing data itself.

```
set_sv -d                        # default orientation
set_sv -N                        # look down at north pole
set_sv -i 0.1 0 0                # rotate slightly about x
```

---

### `set_disp_flags` / `set_disp_text` — Default display flags

```
set_disp_flags {string}
```

Stores a display-flag string that will be used as the default
when `disp` is invoked without flags. Useful for consistent
styling across a session or script.

```
set_disp_flags "-w -c a -e a"
disp                             # uses the stored flags
```

---

### `set_brush` — Line thickness

```
set_brush {n}                    # n in 0-24
```

Sets the on-screen line thickness (slider values 0–24). Affects
subsequent `disp` calls.

---

### `set_ps_brush` — PostScript line thickness

```
set_ps_brush {n}                 # n in 0-12
```

Same idea as `set_brush`, but for open PostScript output files.
Requires an open PostScript file.

---

### `set_fill_opacity` — Face fill alpha

```
set_fill_opacity {n}             # integer opacity (0–255 typical)
```

Controls the alpha channel of face fills. Low values give
translucent faces (useful for overlaid packings); 255 is fully
opaque.

---

### `set_sph_opacity` — Sphere opacity

```
set_sph_opacity {n}
```

Controls opacity of the sphere's back hemisphere when drawing
spherical packings.

---

### `set_custom` — Custom PostScript header

```
set_custom {string}
```

Stores a custom PostScript string that gets injected into
generated PostScript output. Advanced use only.

---

### `set_grid` — Grid-line construction

```
set_grid -c {x y r} -n {k}       # concentric circles + k radial spokes, center (x,y), radius r
set_grid -r {x1 y1 x2 y2} -n {k} # rectangular grid with corners (x1,y1)-(x2,y2), k lines
set_grid -g -n {k}               # use current CPBase.ClosedPath, k subdivisions
```

Builds grid lines stored as `CPBase.gridLines` for use with
`disp -g`. The circle and rectangle modes are self-explanatory;
`-g` uses whatever path was most recently set by `set_path`.

```
set_grid -c 0 0 1 -n 10          # unit circle with 10 spokes
```

---

## Packing-data setters

### `set_active` — Active node (vertex)

```
set_active {v}
```

Sets `packData.activeNode` — the "active" vertex used by some
commands (e.g., `center`, `focus`) when no vertex is specified.

```
set_active 5
```

---

### `set_plot_flags` — Plot flag on vertices

```
set_plot_flags {pf} {v..}
```

Sets the integer plot flag (`packData.plotFlag[v]`) for each
listed vertex. Plot flag controls whether a circle is drawn —
0 means "don't draw," nonzero means "draw." Commands like
`disp -c a` skip vertices whose plot flag is 0.

```
set_plot_flags 0 b               # don't draw boundary circles
set_plot_flags 1 a               # restore (draw everything)
```

---

### `set_vertexMap` — Vertex map

```
set_vertexMap {v w v' w' ...}
```

Sets `packData.vertexMap` to a sequence of (v, w) pairs.
Used by some commands that need an explicit correspondence
between vertices (e.g., certain map experiments). Rarely edited
by hand.

---

### `set_Mobius` — A Möbius transformation in `CPBase.Mob`

Stores a Möbius transformation (not applied — storage only).
To apply, use the `Mobius` command.

Forms:

```
# Explicit 4 complex numbers (8 doubles) + optional orientation flag:
set_Mobius {a.r a.i b.r b.i c.r c.i d.r d.i [flip]}

# From 3-point-to-3-point correspondence:
set_Mobius -xyzXYZ {x.r x.i y.r y.i z.r z.i X.r X.i Y.r Y.i Z.r Z.i}

# From half-edge list (holonomy around loop):
set_Mobius {half-edge-list}
```

The 8-double form gives the matrix entries directly. The
`-xyzXYZ` form finds the unique Möbius sending (x,y,z) to (X,Y,Z).
The half-edge-list form computes `holonomyMobius` around a loop.

A trailing nonzero integer in the 8-double form flips orientation
(`oriented = false`).

---

## Path and function setters

### `set_function` / `set_ftn` — Function string

```
set_function {text}
```

Sets the expression in the Function panel. Parseable by the
internal function parser; used by `set_center -f`, `set_ratio`,
`disp -P`, and other commands that evaluate a function.

```
set_function "z*z + 1"
set_center -f a                  # apply z^2 + 1 to all centers
```

---

### `set_path_text` — Path-expression text

```
set_path_text {text}
```

Sets the parameterized-path text used by `disp -g` and
path-related commands. Parsed by a separate parser that
understands curve parameterizations.

---

### `set_path` — Build the closed path

```
set_path                         # use current path-text from Function panel
set_path {text}                  # build from given text
```

Builds `CPBase.ClosedPath` from text. Once set, the path is
available to `disp -g`, `set_grid -g`, `elist_to_path`, etc.

```
set_path_text "cos(t) + 2*sin(t)*i, 0, 2*pi"
set_path
disp -g                          # show the path
```

---

## Environment and preferences

### `set_accur` — Numerical tolerance

```
set_accur {x}
```

Sets `PackData.TOLER`, the numerical tolerance used by the
solver. Clamped to `[1e-16, 0.01]`. The default is sufficient
for most work; reduce only if you need extreme precision and
are willing to wait.

```
set_accur 1e-12                  # tighter than default
```

---

### `set_cycles` — Default riffle cycle count

```
set_cycles {n}
```

Sets `CPBase.RIFFLE_COUNT`, the default iteration cap for
`repack`. Clamped to `(0, 1000000)`.

```
set_cycles 10000                 # bigger default
repack                           # uses 10000 as default cap
```

---

### `set_dump_format` — Image dump format

```
set_dump_format {format}
```

Sets the image file format used by `screendump`. Common values:
`png`, `jpg`, `gif`.

---

### `set_display` — Display fraction

```
set_display -m {x}
```

Sets the full-screen display fraction (fraction of screen max).
GUI-only.

---

### `set_dir` — Directory

```
set_dir {path}
```

Sets the current working directory for read/write operations.

---

## Variables

### `set_variable` / `set_var` — Named variable assignment

```
set_variable {name} {value}
```

This is the command behind the `:=` preprocessor sugar. You
normally write:

```
myrad := 0.15
```

which is rewritten internally to:

```
set_variable myrad 0.15
```

Either form works. Names are letters and digits only; the first
character must not be `_` (which is reserved for variable
**references**). To use the stored value, prefix with `_`:

```
set_rad _myrad a
```

See the **command syntax** guide for the full variable story.

---

## Notes and gotchas

- **Some setters are GUI-only.** `set_display` requires the
  GUI; in headless mode it's a no-op.
- **List setters return sizes.** They can be composed with `{}`
  queries to capture counts.
- **Geometry setters may need follow-up.** Changing aims or radii
  doesn't automatically re-solve the packing — you need an
  explicit `repack` followed by `layout` to see the effect.
- **`set_invdist` and `set_overlaps` differ in convention.**
  `set_overlaps` interprets its argument as an angle (and converts
  to `cos(angle * π)`), while `set_invdist` takes the inversive
  distance directly. `set_overlaps` is deprecated — prefer
  `set_invdist`.
- **`set_sph_view` silently fails on non-spherical packings.**
  No error, just no visible effect.
- **`set_Mobius` only stores.** To actually apply the stored
  Möbius transformation, use the `Mobius` command.
- **The list setters' per-packing vs global distinction matters.**
  Lowercase setters affect only the active packing; uppercase
  setters persist across pack switches and are accessed via
  `CPBase.*link` names in code.

---

## Source

- `input.CommandStrParser.java` — all `set_*` dispatch handlers,
  split across two main blocks:
  - First block (around line 3855): environment, display,
    preferences, Möbius, paths, screen, sphere view.
  - Second block (around line 9555): aim, rad, center, invdist,
    schwarzian, random, xyz, plot flags, lists, active node.
- `packing.PackData.set_aim*` / `set_rad*` / `set_centers*` /
  `set_xyz_*` — the packing-data handlers.
- `allMains.CPBase` — storage for global lists (`Vlink`, `Elink`,
  `Hlink`, `Flink`, `Tlink`, `Glink`, `Blink`, `Zlink`, `Dlink`),
  constants (`RIFFLE_COUNT`, `DEFAULT_FILL_OPACITY`,
  `DEFAULT_SPHERE_OPACITY`), the stored Möbius (`Mob`), the
  closed path (`ClosedPath`), and grid lines (`gridLines`).
- Each list type's constructor — `NodeLink`, `EdgeLink`,
  `HalfLink`, `FaceLink`, `TileLink`, `GraphLink`, `BaryLink`,
  `PointLink`, `DoubleLink`. See **List Specifiers** for the
  vocabulary these parsers accept.
