# CirclePack Query Reference

## About this page

A **query** is a command that reports information about a packing
without modifying it. Queries are written with a leading `?` and
cover roughly 30 different pieces of data — radii, centers, aims,
angle sums, schwarzians, counts, Möbius transformations, function
values, and more.

Every query has two return modes:

- **Message mode** — when the query is issued as a standalone
  command, the result is formatted and printed to the message
  panel. Long lists are truncated to 12 items with a `...`
  indicator.
- **Value mode** — when the query is wrapped in braces and used
  in an expression (e.g., `myrad := {?rad 5}` or `set_aim {?aim 3} 7`),
  the result is a plain string suitable for storage in a variable.
  Lists are limited to 1000 items.

This page documents every core query. Extender-specific queries
(e.g., `?beac`, tile-specific queries from `ConformalTiling`) are
in their respective extender guides.

---

## Sections

1. [Stored list queries (`?vlist` etc.)](#stored-list-queries)
2. [Vertex data](#vertex-data)
3. [Object detail queries](#object-detail-queries)
4. [Edge geometry](#edge-geometry)
5. [Function and path values](#function-and-path-values)
6. [Energy](#energy)
7. [Counting and packing structure](#counting-and-packing-structure)
8. [Vertex map](#vertex-map)
9. [Möbius transformations](#möbius-transformations)
10. [Variables and math expressions](#variables-and-math-expressions)
11. [Typical patterns](#typical-patterns)
12. [Notes and gotchas](#notes-and-gotchas)
13. [Source](#source)

---

## Stored list queries

Every stored list has a corresponding `?` query that dumps the
list's contents. Per-packing lists are lowercase; global lists
(on `CPBase`) are uppercase.

| Query     | Source | Contents |
|-----------|--------|----------|
| `?vlist`  | `packData.vlist` | Vertex indices |
| `?Vlist`  | `CPBase.Vlink` | Vertex indices |
| `?elist`  | `packData.elist` | Edges (as `v w` pairs) |
| `?Elist`  | `CPBase.Elink` | Edges |
| `?hlist`  | `packData.hlist` | Half-edges |
| `?Hlist`  | `CPBase.Hlink` | Half-edges |
| `?flist`  | `packData.flist` | Face indices |
| `?Flist`  | `CPBase.Flink` | Face indices |
| `?tlist`  | `packData.tlist` | Tile indices |
| `?Tlist`  | `CPBase.Tlink` | Tile indices |
| `?glist`  | `packData.glist` | Graph edges (with optional "root" marker) |
| `?Glist`  | `CPBase.Glink` | Graph edges |
| `?zlist`  | `packData.zlist` | Complex points |
| `?Zlist`  | `CPBase.Zlink` | Complex points |
| `?Dlist`  | `CPBase.Dlink` | Doubles |

All list queries print `empty` in message mode if the list is null
or empty. In message mode lists are truncated at 12 items; in value
mode at 1000.

Examples:

```
?vlist                          # print current packing's vlist
?Flist                          # print global face list

n := {?vlist}                   # capture list as string
msg "_n"                        # use it
```

---

## Vertex data

Queries that report a single numeric value for a specified vertex.
All follow the form `?query {v}`.

### `?aim {v}` — Aim / π

Reports the target angle sum at vertex `v`, divided by π. So `2`
means a regular interior vertex (aim = 2π); `4` means a degree-2
branch point.

```
?aim 5                          # → 2.0 for a regular interior vert
?aim 5                          # → 4.0 for a double-branch vert
?aim 7                          # → -0.318... for boundary (-1 / π)
```

### `?anglesum {v}` — Actual angle sum / π

The **actual** (current) angle sum at vertex `v`, from the current
radii, divided by π.

Compare `?anglesum` with `?aim`: if they match, the vertex is at
equilibrium. If not, `repack` hasn't converged at that vertex.

```
?anglesum 5                     # → near 2.0 for a solved interior vert
```

### `?rad {v}` — Radius

Returns the **actual** radius of vertex `v`. In hyperbolic packings
this is the hyperbolic radius (not the stored x-radius).

```
?rad 1                          # → e.g., 0.178453...
```

### `?cent {v}` — Center

Returns the center of vertex `v` as two space-separated doubles
(`x y`).

```
?cent 1                         # → 0.0 0.0 (for alpha)
?cent 7                         # → 0.234 -0.156
```

### `?flower {v}` — Flower

Returns the cyclic (or boundary-open) sequence of petals around
`v`. Truncated to 12 in message mode.

```
?flower 1                       # → 2 3 4 5 6 7 2
                                # (degree-6 interior, closes back)
?flower 12                      # → 8 11 15 (boundary vertex)
```

### `?angles_at {v}` — Angles at vertex, face by face

For each face containing `v`, reports both the **actual** angle
(computed from current centers) and the **intended** angle
(computed from radii and inversive distances). Differences between
the two reveal layout errors or incompletely-solved packings.

Output is always a message — cannot be captured as a variable.

```
?angles_at 5
# → ... actual (intended) angle sum = 6.283 (6.283) ...
```

### `?mark` — Integer mark

Reports the mark on a vertex (default), face, or tile.

Forms:

```
?mark {v}                       # vertex mark (default)
?mark -f {f}                    # face mark
?mark -t {t}                    # tile mark
```

The `mark` is an integer tag, settable via the `mark` command,
useful for custom selection and in set-builder expressions like
`{c: m .gt. 0}`.

---

## Object detail queries

These queries always produce a formatted message (they cannot be
captured as variable values). Each prints a compact human-readable
summary of one object's state.

### `?edge {v w}` — Edge details

```
?edge 3 7
# → p0; edge (3,7); inv distance=1.0; Schwarzian=0.0123;
#   edgelength=0.234; intended edgelength=0.236
```

Reports inversive distance, schwarzian, and current vs intended
edge lengths. Useful for diagnosing why an edge isn't closing up
properly.

### `?face {f}` — Face details

```
?face 10
# → p0; face 10; vertices={3,5,7}; colorcode=80; mark=0
```

### `?vert {v}` — Full vertex dump

```
?vert 5
# → p0; vert=5; rad=0.234; center=(0.1,-0.2); flower={4,6,8,9,10,11,4};
#   sum=2.0 Pi; aim=2.0 Pi; boundary?=false; degree=6; mark=0;
#   colorCode=100
```

Comprehensive single-vertex summary — radius, center, flower, aim,
angle sum, boundary flag, degree, mark, color.

### `?tile {t}` — Tile details

```
?tile 3
# → p0; tile indx=3; degree=4; tileflower={2,4,6,5}; mark=0;
#   colorCode=80
```

Requires `packData.tileData` to be non-null. Useful in tiling
experiments.

---

## Edge geometry

### `?invdist {v w}` — Inversive distance between two circles

Unlike `?edge`, this does **not** require that `v` and `w` be
neighbors — it reports the inversive distance computed from
current radii and centers for **any** pair of circles.

```
?invdist 3 7                    # neighbors: → 1.0 (tangent)
?invdist 5 20                   # non-neighbors: computed ivd
```

Recall the sign convention: 1 = tangent, <1 = overlap, >1 = separated.

### `?sch {v w}` or `?sch` — Schwarzians

```
?sch                            # schwarzians on interior edges (up to 10)
?sch 3 5 3 7 5 7                # schwarzians on the specified edges
```

Reports schwarzian values, one per edge. Limited to 10 in message
mode. Schwarzians must have been computed previously
(`set_schwarzian`).

### `?sch_flower {v}` — Sum of schwarzians around a vertex

Adds up the schwarzians on every half-edge out of vertex `v`. In a
maximal packing this is 0; nonzero values indicate how the packing
differs from flat at that vertex.

```
?sch_flower 5                   # → 0.000023 for a near-flat vertex
```

---

## Function and path values

### `?f(z) {x [y]}` — Function-panel value

Evaluates the current expression in the Function panel at the
complex point `x + iy`. If `y` is omitted or zero, the argument is
treated as real.

```
# After: set_function "z*z + 1"
?f(z) 2                         # → 5.0
?f(z) 1 1                       # → 1.0 2.0 (i.e., 1 + 2i)
```

The function must be set via `set_function` / `set_ftn` or the
Function panel GUI.

### `?gam(t) {t}` — Parameterized path value

Evaluates the parameterized path function γ(t) at the given
real parameter.

```
# After: set_path_text "cos(t) + sin(t)*i, 0, 2*pi"
?gam(t) 0                       # → 1.0 0.0
?gam(t) 1.5708                  # → 0.0 1.0 (approximately)
```

---

## Energy

### `?energy [-c|-h|-l|-m]` — Packing energy

Computes an energy functional over all circle centers.

| Flag  | Energy | Description |
|-------|--------|-------------|
| `-c`  | Coulomb | Default; sum of `1 / dist(p, q)` over all pairs |
| `-h`  | Hilbert (L²) | Square root of sum-of-squares |
| `-l`  | Logarithmic | Sum of `-log(dist(p, q))` |
| `-m`  | Min distance | Minimum of all pairwise distances |

```
?energy                         # Coulomb (default)
?energy -h                      # Hilbert / L2
?energy -l                      # Logarithmic
?energy -m                      # minimum distance
```

Used in energy-minimization experiments and for diagnosing
ill-distributed packings. For a well-distributed packing, higher
Coulomb energy or lower minimum distance both indicate crowding.

---

## Counting and packing structure

### `?count` — Count vertices, faces, edges, or tiles

Forwards to the `count` command internally.

```
?count a                        # all vertices
?count b                        # boundary vertices
?count i                        # interior vertices
?count -f a                     # all faces
?count -e a                     # all edges
?count -t a                     # all tiles (requires tileData)
?count {c: d .gt. 6}            # via set-builder
```

Default (no flag) is vertices. Returns an integer. Very useful in
conditionals and for loop bounds:

```
n := {?count b}                 # how many boundary vertices?
```

### `?area {f..}` — Sum of face areas

```
?area a                         # total area
?area Iv 5                      # area of faces around vertex 5
?area {f: b}                    # area of boundary faces
```

### `?antip {v..}` — Antipodal vertex

Returns the vertex furthest (in combinatorial distance) from the
listed seeds, using `gen_mark` internally.

```
?antip 1                        # furthest vertex from vertex 1
?antip 1 5                      # furthest from either 1 or 5
```

### `?qual [-flags]` — Quality diagnostics

Reports numerical quality of the current packing.

| Flag | Check |
|------|-------|
| `-v {e..}` | (Default) Worst visual edge error. Eucl/hyp only. |
| `-n` | Check for NaN in any radius or center. |
| `-r {e..}` | Worst relative edge error. |
| `-o {f..}` | Orientation error (faces wrongly oriented). |
| `-a {v..}` | Worst and average angle-sum error. |
| `-e {v..}` | Worst relative effective-radius error. |

Lists default to "all." Useful after `repack` to assess
convergence:

```
?qual                           # worst visual edge error
?qual -n                        # any NaN in the packing?
?qual -a                        # angle-sum errors
```

### `?pnum` — Current pack number

```
?pnum                           # → 0, 1, or 2
```

### `?nodecount` — Number of vertices

```
?nodecount                      # → e.g., 127
```

### `?status` — Packing status

```
?status                         # → true or false
```

True means the packing is in a valid, usable state. False usually
means it hasn't been loaded or something went wrong.

### `?DCEL` — Does a DCEL exist?

```
?DCEL                           # → "yes, DCEL exists" or "no, ..."
```

Basic health check after combinatorial edits.

### `?Redchain` — Red chain dump

Lists the first 12 half-edges of the red chain. For
multiply-connected packings, this shows the boundary of the
fundamental domain. Errors if the red chain is empty.

```
?Redchain                       # → <he1> <he2> ... <he12> ...
```

### `?screen` — Canvas viewbox

Reports the current viewbox as a `set_screen -b` command (and, for
spherical packings, the sphere view matrix as `set_sv -t`). Useful
for saving view state into a script.

```
?screen
# → set_screen -b -1.2 -1.2 1.2 1.2
#   set_sv -t 0.707 0 0.707 0 1 0 -0.707 0 0.707
```

### `?socket` — Socket server info

Reports the socket server's host, port, and connected sources
count. Only meaningful after `socketServe`.

```
?socket
```

---

## Vertex map

For packings with a stored `vertexMap` — pairs `(v, w)` recording
a correspondence from this packing to another.

### `?map {v..}` — Forward map

For each listed vertex `v`, report the image `w` such that
`(v, w) ∈ vertexMap`.

```
?map 3 5 7                      # → {3,12} {5,17} {7,20}
```

### `?map_i {v..}` — Inverse map

For each listed `w`, report the preimage `v`.

```
?map_i 12                       # → {3,12}
```

Errors if `vertexMap` is null.

---

## Möbius transformations

### `?Mobius` (or `?mob`) — Current stored Möbius

Prints the current `CPBase.Mob` — the Möbius transformation set
by `set_Mobius`.

```
?Mobius
# → Current Mobius:
#   [ a b ; c d ] = ...
```

With arguments, interprets them as **side-pair labels** and prints
the named side-pair Möbius instead:

```
?Mobius A                       # Mobius for side-pair named 'A'
?Mobius A B                     # both
```

Case-insensitive: `?mob`, `?Mob`, `?MOBIUS` all work.

---

## Variables and math expressions

### `?_{varname}` — Variable value

Report the current stored value of a variable. Useful for
debugging scripts.

```
myrad := 0.15
?_myrad                         # → variable 'myrad' = "0.15"
```

If the variable name contains no value or doesn't exist, throws
an error.

### `?$expr$` — Math expression

Evaluate a math expression and return its string value. Useful
for arithmetic inside scripts.

```
?$ 2 * pi $                     # → 6.28318...
?$ sin(0.5) $                   # → 0.4794...
```

Uses the built-in math-string evaluator.

---

## Typical patterns

### Capture a value

```
myrad := {?rad 5}               # store radius of vertex 5
n := {?count b}                 # store boundary count
msg "radius at 5 = _myrad"
```

### Check convergence after repack

```
repack
?qual -a                        # how accurate are angle sums?
?qual -n                        # any NaN?
```

### Compare aim vs angle sum

```
?aim 5                          # → 2.0 (target)
?anglesum 5                     # → 1.999999 (actual)
```

### Sanity-check a packing

```
?status                         # valid state?
?DCEL                           # combinatorics set up?
?count a                        # how many circles?
?Redchain                       # multiply-connected? red chain OK?
```

### Inspect a suspicious vertex

```
?vert 17                        # full state dump
?flower 17                      # who are its neighbors?
?angles_at 17                   # face angles, actual vs intended
```

### Save canvas state for a script

```
?screen                         # print set_screen -b ... command
# copy-paste into your script
```

---

## Notes and gotchas

- **Detail queries (`?edge`, `?face`, `?vert`, `?tile`) always
  go to the message pane.** They cannot be captured as variable
  values — wrapping them in `{}` doesn't help.
- **`?aim` and `?anglesum` divide by π.** A regular interior
  vertex returns 2, not 2π. Multiply by π if you need radians.
- **`?rad` returns actual radius.** In hyperbolic packings this
  is *not* the x-radius (the internal storage form).
- **`?count` without any flag defaults to vertices.** If you want
  faces, include `-f`; for edges, `-e`; for tiles, `-t`.
- **`?qual` without flags runs `-v` (worst visual edge error).**
  Different flags compute different things; read the table.
- **Message mode limits lists to 12 items.** Value mode allows
  1000. If you need more than 1000, use `count` and then a loop.
- **`?uerr` is present in the source but not yet functional.**
  The handler returns immediately with a TODO comment.
- **`?Mobius` with no arguments prints `CPBase.Mob`**; with
  arguments, interprets each as a side-pair label.
- **Many advanced queries exist as commented-out stubs** in the
  source (e.g., `pack_ext`, `param`, `alt_rad`, `kap`,
  `pk_stat`, `bdry_dist`, `ratio_ftn`, `conduct`, `bdry_length`,
  `script`) — these are planned but not yet implemented.
- **Case matters on most queries.** `?Mobius` works case-insensitively
  but `?rad` and `?RAD` do not; use lowercase.

---

## Source

- `input.QueryParser` — the main dispatcher
  (`src/input/QueryParser.java`). The `queryParse` method handles
  all standard queries; `processQuery` is the entry point called
  from `TrafficCenter`.
- `input.CommandStrParser.valueExecute` — handles `count` and
  `qual`, which `?count` and `?qual` forward to.
- `dataObject.EdgeData`, `FaceData`, `NodeData`, `TileData` —
  packaging classes used by `?edge`, `?face`, `?vert`, `?tile`
  to assemble their formatted output.
- `ftnTheory.PointEnergies` — used by `?energy`.
- `packing.QualMeasures` — computes the various `?qual` variants.
- `allMains.CPBase` — stores global lists (`Vlink`, `Elink`, etc.)
  and the Möbius transformation (`Mob`) that queries read.
