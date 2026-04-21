# CirclePack Miscellaneous Commands Reference

## About this page

This page covers CirclePack commands that don't naturally fit
the other themed references: packing generation (beyond `max_pack`
and `random_pack`), animation, iterative algorithms, tiling,
statistical data generation, specialized reports, inter-packing
mapping, and the extender-activation command. Also: deprecated,
unimplemented, and test-routine commands, documented for honesty
about what's in the source.

Commands covered: `seed`, `create`, `rand_tri` (`random_tri`),
`rand_pt_read`, `motion`, `perron`, `pave`, `sch_data`, `aspect`,
`torus_t`, `doyle_point`, `gen_mark`, `embed`, `map`, `erf_ftn`,
`extender`, `sq_grid_overlaps`, `triG`, `pdata`, `gen_cut`,
`doyle_annulus`, `T_islandSurround`, `T_s_prop`, `T_layout`,
`T_bary`.

---

## Sections

1. [Packing generation](#packing-generation) — `seed`, `create`, `rand_tri`, `rand_pt_read`
2. [Animation](#animation) — `motion`
3. [Iterative algorithms](#iterative-algorithms) — `perron`
4. [Tiling and paving](#tiling-and-paving) — `pave`
5. [Statistical data generation](#statistical-data-generation) — `sch_data`
6. [Specialized reports](#specialized-reports) — `aspect`, `torus_t`, `doyle_point`, `gen_mark`
7. [Mapping between packings](#mapping-between-packings) — `embed`, `map`, `erf_ftn`
8. [The `extender` command](#the-extender-command)
9. [Minor GUI utilities](#minor-gui-utilities) — `sq_grid_overlaps`, `triG`, `pdata`
10. [Deprecated and unimplemented](#deprecated-and-unimplemented) — `gen_cut`, `doyle_annulus`, `T_layout`
11. [Test routines](#test-routines) — `T_islandSurround`, `T_s_prop`, `T_bary`
12. [Typical workflows](#typical-workflows)
13. [Notes and gotchas](#notes-and-gotchas)
14. [Source](#source)

---

## Packing generation

Commands that produce packings from scratch, complementing the
solvers (`max_pack`, `random_pack`, etc.) in
**solvers_reference**.

### `seed {n} [-h|-e|-s] [-m]` — Create a seed packing

```
seed                             # default: 6-petal euclidean seed
seed 7                           # 7-petal
seed -h 12                       # 12-petal hyperbolic
seed -s 8                        # 8-petal spherical
seed -m 6                        # 6-petal with vertex swap
```

Creates a simple n-flower packing: a central vertex surrounded by
a ring of `n` petal vertices.

### Flags

| Flag      | Meaning |
|-----------|---------|
| `-h`      | Hyperbolic geometry |
| `-e`      | Euclidean geometry (default) |
| `-s`      | Spherical geometry |
| `-m`      | After creation, swap so center index = `n+1`, and renumber the flower so alpha = `n+1`, gamma = `1` |
| *{n}*     | Number of petal vertices. Must be ≥ 3, ≤ 1000. Default 6. |

The resulting packing has `n+1` vertices total (one center plus
`n` petals).

### Examples

```
seed                             # 7 circles (1 center + 6 petals) in Eucl
seed 3                           # smallest: 4 circles (triangle)
seed -h 7                        # hyperbolic 7-flower
seed -s 12                       # spherical 12-flower
```

### Notes

- Seeds are the starting point for many constructions: you can
  build outward via `add_layer`, `add_gen`, `bary_refine`.
- `seed -m` is used when you need the center vertex to be at a
  specific later index (common in schwarzian experiments).

### `create {type} {params}` — Create a named packing pattern

A versatile command that produces packings of specific families.
The first argument is a type name; remaining arguments are
type-specific.

### Synopsis

```
create seed {n} [-s {schvec}]    # same as 'seed {n}', optionally with schwarzians
create hex {n}                   # hex lattice, n generations
create hex_tor {n}               # hex torus
create sq {n}                    # square lattice
create chain {spec}              # chain pattern
create tri_gr {p} {q} {r}        # triangle group (p,q,r)
create j {params}                # j-function related triangle group
create pent                      # pentagonal tiling (default)
create pent3                     # pentagonal, variant 3
create pent4                     # pentagonal, variant 4
create dyadic {n}                # dyadic subdivision
create fib {n}                   # Fibonacci
create tetra {n}                 # tetrahedral
create Kag {n}                   # Kagome lattice
create tilebary                  # barycenters of tiles (requires tileData)
create cyl {n}                   # cylinder
```

### Type names and their modes

Each `type` name is prefix-matched against a list of recognized
patterns:

| Type             | Meaning |
|------------------|---------|
| `seed`           | Seed packing (default mode 1) |
| `hex` / `Hex`    | Hexagonal lattice |
| `hex_tor`        | Hexagonal torus |
| `sq` / `Sq`      | Square lattice |
| `chai` / `Chai`  | Chain pattern |
| `tri_g` / `Tri_g`| Triangle group |
| `j` / `J`        | Triangle group (j-function related) |
| `pent3`          | Pentagonal variant 3 |
| `pent4`          | Pentagonal variant 4 |
| `pent`           | Pentagonal (general) |
| `dyadic`         | Dyadic subdivision |
| `fib` / `Fib`    | Fibonacci |
| `tetra` / `Tetra`| Tetrahedral |
| `Kag`            | Kagome lattice |
| `tilebary`       | Tile barycenters (requires tileData) |
| `cyl`            | Cylinder |

Prefixes must be long enough to be unambiguous (so `hex_tor` must
be spelled out — `hex` alone picks the lattice).

### Examples

```
create seed 7                    # same as 'seed 7'
create hex 4                     # 4-generation hex lattice
create hex_tor 5                 # hex torus, 5 generations
create sq 6                      # 6-generation square lattice
create tri_gr 2 3 7              # (2,3,7) triangle group
create pent3                     # pent-3 tiling
create fib 10                    # Fibonacci, 10 iterations
```

### Notes

- Some types ignore the numeric parameter; some require additional
  arguments.
- `create seed -s {...}` builds a seed packing with specified
  edge schwarzians rather than default aims.
- `create tilebary` requires existing `tileData` (from `pave` or
  `read_CT`).
- See source (`PackCreation.java`) for the precise semantics of
  each type's parameters.

### `rand_tri` (`random_tri`) — Random triangulation

Build a random triangulation with controlled geometry and
domain.

### Synopsis

```
rand_tri                         # default: 200 random points
rand_tri -N {n}                  # n random points
rand_tri -d                      # deterministic seed
rand_tri -A {a}                  # given aspect ratio (rectangle)
rand_tri -g {filename}           # from a boundary path
rand_tri -g[s] {filename}        # path from script file
rand_tri -u                      # unit disc boundary
rand_tri -S {n}                  # sphere with n points
rand_tri -T {x y}                # torus with modulus tau = x + y*i
rand_tri -Z {n}                  # zigzag method (requires a path)
```

### Flags

| Flag        | Meaning |
|-------------|---------|
| `-d`        | Deterministic seed (for reproducibility) |
| `-N {n}`    | Number of interior points (default 200, min 12) |
| `-A {a}`    | Aspect ratio (for rectangular domain) |
| `-g {file}` | Use a boundary path from a file |
| `-gs {file}`| Path from a script file |
| `-u`        | Unit disc boundary |
| `-S {n}`    | Spherical domain with `n` points (min 12) |
| `-T {x} {y}`| Torus with modulus τ = x + yi |
| `-Z {n}`    | Zigzag method (requires a current `ClosedPath`) |

Flags can combine (e.g., `-d -u -N 500` for a reproducible
500-point packing of the unit disc).

### Examples

```
rand_tri                         # default
rand_tri -N 500                  # 500 interior points
rand_tri -S 300                  # 300 points on sphere
rand_tri -T 0.5 0.866            # torus with specific modulus
rand_tri -u -N 1000              # 1000 points in unit disc
rand_tri -g mypath.path -N 300   # 300 points inside mypath.path
rand_tri -d -u -N 100            # reproducible 100-point disc packing
```

### Notes

- Like `random_pack`, `rand_tri` replaces the active pack. Use
  `copy` first if you want to preserve the previous packing.
- Unlike `random_pack` (which is always hyperbolic), `rand_tri`
  can produce Euclidean, spherical, or torus packings depending
  on flags.
- `-Z` (zigzag) requires a current `ClosedPath` (set via
  `set_path`, `read_path`, or `elist_to_path`).

### `rand_pt_read {filename}` — Read random points

```
rand_pt_read mypoints.txt
```

Read a file of points from `PackingDirectory` and build a
Delaunay triangulation of them. The file format is determined by
the points (plane vs. spherical).

Used when you have pre-computed points (from a simulation,
sampling procedure, or external tool) and want to wrap them in
a CirclePack packing.

If point data suggests (θ, φ) spherical coordinates, the
resulting packing is spherical; otherwise Euclidean.

---

## Animation

### `motion -d {x} -n {k} "{cmds}"` — Animate between packings

Capture the current packing state, run a sequence of commands,
then animate the interpolation between initial and final state
over `k` frames.

### Synopsis

```
motion "cmds"                    # 75 frames, 0.004s delay each
motion -n 50 "cmds"              # 50 frames
motion -d 0.02 "cmds"            # 0.02s between frames
motion -n 100 -d 0.01 "cmds"     # both
```

### Flags

| Flag      | Meaning |
|-----------|---------|
| `-n {k}`  | Number of frames (default 75, min 2) |
| `-d {x}`  | Delay between frames in seconds (default 0.004) |

### What it does

1. Captures current centers and radii (`bottom`).
2. Executes the quoted commands on the current packing.
3. Creates an `Interpolator` from `bottom` to current state.
4. Displays `k` intermediate interpolated frames, pausing
   between each.
5. Redraws the final state at full quality.

### Examples

```
# Animate a scale-up
motion "scale 2.0"

# Animate 50 frames, longer delay, into a Mobius transformation
motion -n 50 -d 0.05 "Mobius"

# Animate a repack after aim change
motion -n 100 "set_aim 4 5; repack; layout"
```

### Notes

- The quoted command string can be any valid command sequence.
- For simple transformations (Möbius, scale, rotate), motion
  produces smooth animation. For combinatorial changes (flips,
  refinement), the interpolation may not make visual sense.
- Each frame calls `disp`; display options follow current
  `dispOptions`.

---

## Iterative algorithms

### `perron -{d|D|u|U} {passes}` — Perron iteration

Run a Perron-type iterative packer with a specified direction
and pass count.

### Synopsis

```
perron                           # default: 2000 passes
perron {n}                       # n passes
perron -d {n}                    # downward only
perron -D {n}                    # down/up (Beardon/Stephenson upward Perron)
perron -u {n}                    # upward only
perron -U {n}                    # up/down (Bowers downward Perron)
```

### Flags

| Flag      | Meaning |
|-----------|---------|
| `-d`      | Downward only |
| `-D`      | Down/up (Beardon/Stephenson) |
| `-u`      | Upward only |
| `-U`      | Up/down (Bowers) |
| *{n}*     | Passes (default 2000) |

### Requirements

- Packing must be Euclidean or hyperbolic (errors on spherical).
- Must have no inversive distance overlaps (i.e., tangency
  packings only).

### What it returns

Reports four numbers: count of passes executed, and three
"deficiency" measures (up, down, error). Large deficiencies
indicate the method hasn't converged; near-zero means success.

### Examples

```
perron -D 5000                   # upward Perron, 5000 passes
perron -U 10000                  # downward Perron, 10000 passes
```

### Notes

- Perron methods are an alternative to the standard `repack`
  algorithm. Results should agree with `repack` for convergent
  packings but the convergence properties differ.
- Useful for studying packing-algorithm behavior or when
  `repack` struggles.
- If Perron fails (`perronResults[0]<0`), an error message
  reports the three deficiencies for diagnosis.

---

## Tiling and paving

### `pave {v}` — Build a tiling centered at a vertex

```
pave                             # use active node (default to alpha)
pave {v}                         # use vertex v as seed
```

Builds a `TileData` structure using vertex `v` as the seed tile's
barycenter. Stores the result in `packData.tileData` and sets the
tile's parent to the current packing.

Used for constructing tilings from a packing — a prerequisite
for the tiling-specific commands (`disp -t`, `set_tlist`, tile
queries).

### What it does

1. Gets a seed barycenter vertex (default: current active node,
   fallback: alpha).
2. Calls `TileData.paveMe(packData, v)` to build the tiling.
3. Stores result as `packData.tileData`.

Returns the tile count on success, `0` on failure.

### Examples

```
pave                             # pave from current active node
pave 5                           # pave from vertex 5
?tileData                        # confirm tile data exists
disp -t a                        # display all tiles
```

### Notes

- Paving is combinatorial — it's about choosing how the packing
  is organized into tiles, not about changing radii or centers.
- After `pave`, tile-based commands become available; without
  `tileData`, they're no-ops.
- Related to `read_CT` (which reads a combinatorial tiling from
  a file) in **io_reference**.

---

## Statistical data generation

### `sch_data {d|-rc} {N} -f {filename}` — Generate schwarzian data

Produce statistical data on schwarzians by running N trials of
random seed packings.

### Synopsis

```
sch_data {d} {N} -f {filename}   # N trials of degree-d seed
sch_data -r {N} -f {filename}    # randomize degree each trial
sch_data -c {N} -f {filename}    # randomize centers (no repack)
sch_data -rc {N} -f {filename}   # both
```

### Flags

| Flag     | Meaning |
|----------|---------|
| *{d}*    | Degree (≥3). Explicit degree mode (default 6). |
| `-r`     | Randomize degree per trial (uses `DegreeDistribution.getRandDegree()`) |
| `-c`     | Randomize centers (skip repack after random radii) |
| *{N}*    | Number of trials |
| `-f`     | Filename flag for output |
| `-a`     | Append to filename (standard write-flag) |

### What it does

For each trial:
1. Create (or recreate) a seed packing of the specified degree.
2. Set random boundary radii.
3. Optionally `repack` (unless `-c`).
4. Compute edge schwarzians via `set_sch`.
5. Write the schwarzians around vertex 1's flower to the file.

The output file gets a header (reconstituted flag string), then
one row per trial.

### Examples

```
sch_data 6 100 -f data.txt       # 100 trials of degree-6 seeds
sch_data -r 500 -f mixed.txt     # 500 trials, degrees randomized
sch_data 8 1000 -a hist.txt      # append 1000 trials to hist.txt
```

### Notes

- A useful research tool for statistical studies of schwarzians.
- Each row is space-separated; last column ends the trial.
- Without `-c`, every trial runs `repack`, which can be slow for
  large `N`.

---

## Specialized reports

### `aspect` — Log aspect ratio of a rectangular packing

```
aspect
```

Reports `log(width/height)` — the log-aspect — for a
rectangular packing. Prints the four corner vertex indices
along with the aspect value.

Useful for conformal-rectangle experiments where the modulus
(aspect ratio) of the packing is the quantity of interest.

Formerly called `rect_ratio`.

### `torus_t` — Torus modulus

```
torus_t
```

Reports the complex torus modulus τ and its reciprocal 1/τ for a
torus packing. Requires a valid torus combinatorics (genus 1,
no boundary).

### `doyle_point {f}` — Doyle point of a face

```
doyle_point {f}
```

Computes the **Doyle point** associated with face `f` — a
specific point in the plane determined by the face's three
radii and centers. Reports the point and the associated `a` and
`b` parameters.

### Requirements

- Packing must be Euclidean (`hes == 0`).
- `f` must be a valid face index.

### Notes

- Doyle points are used in the study of Doyle spirals and related
  conformal structures.
- This is a *reporting* command; it doesn't modify the packing.

### `gen_mark` — Generation marking

```
gen_mark {seeds}                 # mark generations from seed vertices
gen_mark -m {n} {seeds}          # stop at max generation n
```

Call the value-execute routine that records generations of
vertices or faces measured from specified seeds. Records the
generation in each vertex's `mark` field; returns the highest
index marked.

Details and the underlying machinery live in `valueExecute`; see
source for the flag vocabulary.

### Example

```
gen_mark 1                       # mark generations from vertex 1
?mark 50                         # report generation of vertex 50
```

---

## Mapping between packings

### `embed -q{q} a b A B` — Find embedding into another pack

```
embed -q1 1 2 1 2                # embed current pack into pack 1, starting a→A, b→B
```

Attempts to find a combinatorial embedding of the current
packing into the complex of pack `q`. The edge `(a,b)` of the
current pack is mapped to edge `(A,B)` of pack `q`; the
embedding extends from there.

Result is stored as a `VertexMap` in `packData.vertexMap`.

### Requirements

- Pack `q` must be a different, active pack.
- `a`, `b` must be vertex indices in current pack; `A`, `B` in
  pack `q`.

### Output

Reports:
- **Full embedding** if the entire current pack fits into pack `q`.
- **Partial embedding** (with vertex count) if only some vertices
  were matched.

Returns the count of embedded vertices.

### Use case

Verify that one triangulation is a sub-complex of another;
construct correspondences between related packings for
schwarzian or conformal-map work.

### `map` — Apply the stored vertexMap

```
map -f {f..}                     # apply map to faces
map -f -q{p} {f..}               # apply and transfer to pack p
map -r -f {f..}                  # reverse direction
```

Applies `packData.vertexMap` to the listed faces (or other
objects), optionally transferring the result into pack `p`.

### Flags

| Flag     | Meaning |
|----------|---------|
| `-r`     | Reverse direction (apply inverse map) |
| `-f {f..}`| Act on faces |
| `-q{p}`  | Transfer result to pack `p` |

### Requirements

- `packData.vertexMap` must be set (typically by `embed`,
  `prune`, `double`, or other combinatorial operations).

### Example

```
embed -q1 1 2 1 2                # establish map
map -f -q1 a                     # transfer all faces to pack 1 via map
```

### `erf_ftn {p1} {p2} {n}` — Erf-function packing

```
erf_ftn 0 1 2                    # from pack 0 → pack 1, parameter n=2
```

Build an "erf function" packing: from pack `p1`, construct a
new pack in slot `p2` using an error-function-based construction
with parameter `n` (1, 2, or 3).

### Requirements

- `p1 ≠ p2`, both valid pack indices.
- `n` must be 1, 2, or 3.

### Use case

Specialized construction in the study of entire functions and
their packings.

---

## The `extender` command

CirclePack's advanced functionality is accessed through
**extenders** — specialized modules that attach to a packing
and provide extra commands. The `extender` command manages them.

### Synopsis

```
extender ?                       # list extenders on current pack
extender {abbreviation}          # start an extender
extender -x {abbreviation}       # kill the named extender
extender -x                      # kill all extenders
extender -r {abbreviation}       # restart (kill + start)
```

### Extender lifecycle

Once an extender is started, its commands become available via
the **`|ABBREV|` prefix**:

```
extender gb                      # start the GB extender
|GB| bp_trad -a 4 -i 5           # call a GB command
|GB| status                      # another GB command

extender -x gb                   # dispose the GB extender
```

Each extender has its own command vocabulary documented
separately (see `GB_Extension_Guide.md` for the style reference).

### Basic examples

```
extender ?                       # see what's running
extender bvp                     # start BVP
|BVP| solve                      # invoke a BVP command
extender -x bvp                  # clean up when done
```

### Extender abbreviations

The following abbreviations are hard-coded in the `extender`
dispatcher (case-insensitive). Each starts a specific
`PackExtender` subclass.

| Abbrev     | Extender class | What it does |
|------------|----------------|--------------|
| `bvp`      | `BoundaryValueProblems` | Boundary-value experiments |
| `bf`       | `BeurlingFlow` | Beurling flow |
| `mg`       | `MicroGrid` | Microgrid (takes `-s {...}` or `-q{p}`) |
| `rh`       | `RiemHilbert` | Riemann-Hilbert problems |
| `gp`       | `Graphene` | Graphene structures |
| `pb`       | `PolyBranching` | Polynomial branching |
| `tc`       | `TileColoring` | Tile coloring |
| `cf`       | `CurvFlow` | Curvature flow |
| `ct`       | `ConformalTiling` | Conformal tiling |
| `TE`       | `TorusEnergy` | Torus energy |
| `JP`       | `JammedPack` | Jammed-packing experiments |
| `PR`       | `Percolation` | Percolation |
| `mc`, `mmc`| `MeanMove` | Mean move (both abbrevs start same extender) |
| `ca`       | `ComplexAnalysis` | Complex-analysis tools |
| `cs`       | `CylinderSpheres` | Cylinder / spheres constructions |
| `ps`       | `ProjStruct` | Projective structure |
| `ap`, `ss` | `AffinePack` | Affine-packing experiments |
| `sm`       | `SchwarzMap` | Schwarz map |
| `gb`       | `GenModBranching` | Generalized Modular Branching — the branch-point extender documented in `GB_Extension_Guide.md` |
| `nk`       | `Necklace` | Necklace construction |
| `fs`       | `FlipStrategy` | Flip strategies |
| `IG`       | `iGame` | iGame |
| `rm`       | `RationalMap` | Rational maps |
| `sp`       | `SchwarzPack` | Schwarz packings |
| `SS`       | `ShapeShifter` | ShapeShifter (see note below) |
| `WW`       | `WordWalker` | Word walker |
| `fk`       | `FeedBack` | Feedback experiments |
| `bq`       | `BrooksQuad` | Brooks quadrilateral |
| `bl`       | `BeltramiFlips` | Beltrami flips |
| `hd`       | `HypDensity` | Hyperbolic density |
| `sl`       | `SphereLayout` | Sphere layout |
| `cw`       | `WeldManager` | Weld manager (provides `weld` / `unweld`) |

### Notes on the abbreviations

- The match is **case-insensitive**. `extender gb`,
  `extender GB`, and `extender Gb` all start the same extender.
- Some extenders are listed under **multiple abbreviations**
  (`ap` and `ss` both start `AffinePack`; `mc` and `mmc` both
  start `MeanMove`).
- There is a **subtle collision**: lowercase `ss` is registered
  for `AffinePack`, and uppercase `SS` is registered separately
  for `ShapeShifter`. Because the match is case-insensitive and
  `AffinePack` is checked first, typing `extender SS` actually
  starts `AffinePack`, not `ShapeShifter`. If you specifically
  want `ShapeShifter`, this is a known wrinkle; in practice most
  users should check the startup message to confirm which
  extender was started.
- If the abbreviation doesn't match any of these, the extender
  command falls back to a file-chooser dialog for loading an
  external extender class. In a script context (no GUI) that
  fallback fails silently.

### Aim-convention gotcha

Many extenders — including `|GB|` (Generalized Modular
Branching) — interpret their `-a {x}` aim flag as a **multiple
of π** rather than as raw radians. For example, `|GB| bp_trad
-a 4 -i 5` creates a branch point with angle sum 4π.

This differs from the **core** `set_aim` command, where values
are in raw radians: `set_aim 4 5` sets vertex 5's aim to 4
(about 1.27π). See **setters_reference** for the radian
convention in core commands.

When switching between core and extender commands, keep the
distinction in mind:

```
set_aim 4 5                      # vertex 5 aim = 4 radians (~1.27π)
|GB| bp_trad -a 4 -i 5           # branch point with 4π angle sum
```

Both commands are about the target angle at vertex 5, but the
numerical meaning of "4" is different.

### Notes

- Extenders are scoped per-pack. Starting an extender on pack 0
  doesn't make it available on pack 1.
- Some extenders need specific preconditions (loaded packings,
  certain geometries, etc.). Start-up messages indicate problems.
- `extender -x` with no argument kills all extensions on the
  current pack — useful for cleanup between experiments.

---

## Minor GUI utilities

### `sq_grid_overlaps` — Set square-grid overlaps

```
sq_grid_overlaps
```

Dispatches to `packData.sq_grid_overlaps()`, which sets inversive
distances across the packing according to a square-grid pattern.

Takes no arguments. Used in conformal-map experiments involving
square-grid overlap structures.

### `triG` — Alias to `create tri_gr`

```
triG {p} {q} {r}                 # same as 'create tri_gr {p} {q} {r}'
```

Shorthand that aliases to `create tri_gr` followed by the
reconstituted flag string. Convenience for frequent use of
triangle-group constructions.

### `pdata` — Update pack-data hover

```
pdata
```

Forces a refresh of the pack-data hover frame in the GUI. Rarely
needed directly; normal commands refresh this panel as a side
effect.

---

## Deprecated and unimplemented

### `gen_cut` — Deprecated (throws exception)

```
gen_cut {v} {n}                  # no longer implemented
```

This command is **deprecated** and throws an exception:

```
'gen_cut' call is no longer implemented
```

It was intended to cut a packing at a specified generation from a
vertex. The RedChainer infrastructure it relied on has not been
updated to the DCEL setting.

### `doyle_annulus` — Unfinished

```
doyle_annulus {p} {q} {n}        # unfinished
```

Parses its arguments (three integers), validates them, then
throws:

```
'doyle_annulus' processing appears to be unfinished.
```

Documented here for completeness; don't rely on it.

### `T_layout` — Placeholder

```
T_layout
```

The handler is empty (a commented placeholder). No effect.

---

## Test routines

CirclePack has several commands prefixed `T_` — test or
development routines. These are unstable (may change or be
removed), but documented here for completeness.

### `T_islandSurround {v..}`

Identifies half-edge chains that "surround" a beach of listed
vertices. Displays each chain via `disp -ff hlist` as a
demonstration. Used for testing surround-chain algorithms in
`RawManip`.

### `T_s_prop {s} {v w}`

Given an interior edge `{v, w}` and a schwarzian value `s`,
propagates a new center/radius for the opposite vertex across
the edge based on intrinsic-schwarzian propagation.

Used for testing the schwarzian-propagation machinery.

### `T_bary`

Adds a barycenter to every interior face, resetting aims to 2π.
Similar to `bary_refine` (see **combinatorial_editors**) but
simpler — no hex preservation, just uniform barycenter insertion.

---

## Typical workflows

### Create a packing from scratch

```
create seed 7                    # create a 7-petal seed
add_layer -t 1 8                 # expand boundary
max_pack
disp -wr
```

### Random-packing experiment with a constraint

```
# Create a random packing inside a custom boundary
read_path my_boundary.path
rand_tri -g my_boundary.path -N 500 -d
max_pack
disp -wr
```

### Create a torus

```
rand_tri -T 0.5 0.866 -N 200     # torus with τ = 0.5 + 0.866i
torpack                          # solve the torus packing
torus_t                          # confirm modulus
disp -wr
```

### Animate a deformation

```
random_pack 100
motion -n 75 -d 0.02 "set_aim 4 5; repack"
```

### Start an extender

```
read mypack.p
extender bvp
|BVP| setup                      # extender-specific commands
|BVP| solve
extender -x bvp                  # clean up
```

### Generate statistical schwarzian data

```
# 500 trials of random degree-6 seeds, save to file
sch_data 6 500 -f seed_sch_d6.txt

# Or 500 trials with randomized degrees
sch_data -r 500 -f seed_sch_rand.txt
```

### Pave and tile

```
max_pack
pave                             # build tiling centered on alpha
disp -t a                        # show tiles
set_tlist a
?count                           # tile count
```

### Find an embedding

```
# pack 0 is the small complex; pack 1 is the large one
# map vertex 1→1, 2→2 at the start
embed -q1 1 2 1 2
# if successful, packData.vertexMap is set
map -f -q1 a                     # transfer faces to pack 1
act 1
disp -wr
```

---

## Notes and gotchas

- **`seed` and `create seed` overlap.** `seed {n}` is a convenience
  shortcut for `create seed {n}`. The two commands have the same
  effect when used with just a degree argument; `create seed -s
  {...}` adds schwarzian-based variants.

- **`create` type-matching is prefix-based.** `create hex` matches
  the lattice type; `create hex_tor` must be spelled out because
  `hex` alone already matches.

- **`rand_tri` replaces the active pack.** Like `random_pack`,
  this is destructive — `copy` first if the prior packing
  matters.

- **`motion` is not a pure function.** Each call captures initial
  state, runs commands, then animates. Nested `motion` calls
  don't nest cleanly.

- **`motion` delays add up.** `motion -n 1000 -d 1` takes 1000
  seconds to play back, blocking all other operation during
  that time.

- **`perron -U` vs `perron -u`.** Capital `-U` runs up/down
  (Bowers); lowercase `-u` is upward only. Easy to confuse in
  scripts.

- **`perron` requires no overlaps.** Errors immediately if
  `haveInvDistances()` is true.

- **`pave` requires an interior vertex.** Default falls back to
  alpha if the active node isn't interior or is invalid.

- **`sch_data` with `-r` changes the combinatorics per trial.**
  Each trial rebuilds a seed of a random degree — so comparisons
  across trials mix apples and oranges unless that's intended.

- **`aspect` assumes a rectangle.** For general packings with
  non-rectangular boundaries, `aspect` reports the log-aspect of
  the bounding box, which may not be meaningful.

- **`torus_t` errors on non-tori.** If you call it on a disc or
  sphere packing, it throws.

- **`doyle_point` is Euclidean-only.** No hyperbolic or spherical
  variant.

- **`embed` may produce partial maps.** Read the return message
  carefully — a "partial embedding, N vertices" result means only
  N vertices were successfully matched.

- **`map` requires `vertexMap` to be set.** Without a prior call
  to `embed`, `prune`, `double`, or similar, `map` errors.

- **`extender` abbreviations are case-insensitive.** `extender
  gb` and `extender GB` both start the Generalized Modular
  Branching extender (see the abbreviation table).

- **`extender -x` (no abbrev) kills everything.** Sometimes
  handy, sometimes a disaster. Be careful.

- **`T_*` commands may change without warning.** They're
  development aids, not stable API.

- **`gen_cut` and `doyle_annulus` are throws, not silent
  failures.** Calling them will interrupt a script with an
  exception.

---

## Source

- `packing.PackCreation` — the packing-family constructors:
  `seed`, hex/square/chain lattices, triangle groups, pentagonal
  tilings, Fibonacci, dyadic, tetrahedral, Kagome, cylinder.
- `rePack.RandomTriangulation` — random-triangulation
  generation; backing `rand_tri` and `rand_pt_read`.
- `packing.Interpolator` — frame interpolation for `motion`.
- `packing.HypPacker.hypPerron`,
  `packing.EuclPacker.euclPerron` — Perron iteration backends.
- `komplex.TileData.paveMe` — tiling construction for `pave`.
- `util.DegreeDistribution` — random degree sampling for
  `sch_data -r`.
- `packing.Schwarzian.comp_schwarz` — schwarzian computation
  used in `sch_data`.
- `packing.Erf_function.erf_ftn` — erf-function packing
  construction.
- `packing.Embedder.embed` — embedding of one pack into another.
- `packing.PackData.sq_grid_overlaps` — square-grid overlap
  setter.
- `dcel.RawManip.islandSurround`, `addBaryCents_raw` — T_-series
  test backends.
- `workshops.LayoutShop.schwPropogate` — schwarzian propagation
  used by `T_s_prop`.
- `packExtensions` package — extender classes started by the
  `extender` command. The 32 hard-coded abbreviations map to:
  `BoundaryValueProblems` (`bvp`), `BeurlingFlow` (`bf`),
  `MicroGrid` (`mg`), `RiemHilbert` (`rh`), `Graphene` (`gp`),
  `PolyBranching` (`pb`), `TileColoring` (`tc`), `CurvFlow`
  (`cf`), `ConformalTiling` (`ct`), `TorusEnergy` (`TE`),
  `JammedPack` (`JP`), `Percolation` (`PR`), `MeanMove`
  (`mc`/`mmc`), `ComplexAnalysis` (`ca`), `CylinderSpheres`
  (`cs`), `ProjStruct` (`ps`), `AffinePack` (`ap`/`ss`),
  `SchwarzMap` (`sm`), `GenModBranching` (`gb`), `Necklace`
  (`nk`), `FlipStrategy` (`fs`), `iGame` (`IG`), `RationalMap`
  (`rm`), `SchwarzPack` (`sp`), `ShapeShifter` (`SS` — but see
  abbreviation-collision note in the Extender section),
  `WordWalker` (`WW`), `FeedBack` (`fk`), `BrooksQuad` (`bq`),
  `BeltramiFlips` (`bl`), `HypDensity` (`hd`), `SphereLayout`
  (`sl`), `WeldManager` (`cw`).
- `input.CommandStrParser` — top-level dispatch for each of these
  commands.
