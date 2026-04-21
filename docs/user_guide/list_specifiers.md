# CirclePack List Specifiers

## Overview

Many CirclePack commands — `disp`, `color`, `set_rad`, `set_aim`,
`mark`, `repack`, and most others that take a list of packing objects
— accept a compact **list specifier** language for describing which
objects to operate on. Rather than always requiring explicit integer
indices, you can write things like `a` for "all", `b` for "boundary",
`i` for "interior", or more elaborate forms such as `D 0.5 z 0 0`
meaning "all vertices within distance 0.5 of the origin."

The specifier language is parsed by one of the list classes depending
on what kind of object is expected:

| List type | Handled by | Used with |
|-----------|------------|-----------|
| Vertex / circle list | `listManip.NodeLink` | `-c`, `-h`, `-P`, `-t`, `-n`, `set_rad`, `set_aim`, `color -c`, `mark`, ... |
| Face list            | `listManip.FaceLink` | `-f`, `-a`, `-y`, `-T` (some), `color -f`, ... |
| Edge list (simple)   | `listManip.EdgeLink` | `-e` (via `HalfLink`), edge commands |
| Half-edge list       | `listManip.HalfLink` | `-e`, `-r`, `-dp`, `-dg`, `-s` |
| Tile list            | `listManip.TileLink` | `-T`, tile commands |
| Graph edge list      | `listManip.GraphLink` | `-dg`, graph-related commands |
| Barycentric-point list | `listManip.BaryLink` | `-tf`, bary-coord drawing |
| Complex-point list   | `listManip.PointLink`, `listManip.PathLink` | `-sz`, `-tz`, curves |

The vocabulary is largely parallel across list types — `a`, `b`, `i`,
`m`, `I{subtype}`, paren-ranges, bracket operators, and stored-list
references behave consistently — but each list type adds a few
type-specific tokens (e.g., `A` for "alternating faces" only exists
in `FaceLink`). Type-specific tokens are noted in their respective
sections below.

---

## Universal conventions

These apply to every list type.

### Explicit integer indices

A bare integer (or a space-separated run of integers) is the most basic
form:

```
disp -c 3 5 7                   # circles 3, 5, 7
disp -f 1 2 3 4                 # faces 1-4
disp -e 5 6 5 8 12 20           # edges (5,6), (5,8), (12,20)
```

Edge lists expect **pairs** of vertex indices; the parser consumes them
two at a time.

### Leading `-` is forgiven

A token accidentally starting with `-` has the dash stripped. This
means `disp -c -b` and `disp -c b` both work — the second `-` before
`b` is treated as a typo and ignored.

### Variable substitution with `_`

A token starting with `_` is interpreted as a user variable name; its
stored value is expanded in place:

```
set_var mylist "3 5 7 9"
disp -c _mylist                 # same as: disp -c 3 5 7 9
```

### Set-builder notation `{...}`

Any list accepts a `{...}` set-builder expression, handled by
`SetBuilderParser`. This lets you describe objects by properties
rather than by index. Details are beyond this page — see
`util.SetBuilderParser`.

### Stored-list references (`vlist`, `Vlist`, etc.)

Every CirclePack packing keeps its own stored lists (`vlist`, `elist`,
`flist`, `glist`, `hlist`, `tlist`, `blist`), and there are global
counterparts in `CPBase` (`Vlist`, `Elist`, `Flist`, `Glist`, `Hlist`,
`Tlist`, `Blist`). A specifier of the form `?list` inserts the entire
stored list. Capitalized forms pull from the global list **and** check
validity against the current packing.

```
disp -c vlist                   # all vertices in this pack's vlist
disp -c Vlist                   # all vertices in the global Vlist
disp -e elist                   # edges in this pack's elist
disp -e Elist                   # edges in the global Elist
disp -f flist                   # faces in this pack's flist
disp -T tlist                   # tiles in this pack's tlist
```

Stored-list references accept two optional suffixes:

**Paren range** — take a slice `(a, b)` of the stored list (0-indexed):

```
disp -c vlist(0,5)              # first six entries of vlist
disp -c Vlist(10,20)            # entries 10 through 20 of global Vlist
```

**Bracket selector** — apply a selection operator:

| Form         | Effect |
|--------------|--------|
| `vlist[r]`   | rotate: append first entry to end |
| `vlist[n]`   | next: consume (and return) the first entry |
| `vlist[l]`   | last entry only |
| `vlist[f]`   | first entry only |
| `vlist[N]`   | the entry at index N (0-based) |

Example:

```
disp -c vlist[l]                # just the last vertex in vlist
disp -c vlist[0]                # first entry
```

---

## Vertex / circle list (NodeLink)

Used by any flag taking a `{v..}` argument: `-c`, `-h`, `-P`, `-n`,
`-t` (point form), `color -c`, `set_rad`, `set_aim`, and many more.

### Selection tokens

| Token            | Meaning |
|------------------|---------|
| `a`              | **all** vertices. `a(v1,v2)` restricts to index range [v1, v2]. |
| `b`              | **boundary** vertices. `b(v1,v2)` gives the arc of the boundary component from v1 to v2 (both must lie on the same component). |
| `i`              | **interior** vertices. `i(v1,v2)` restricts to index range. |
| `r`              | one **random** vertex (from `a` if no list given, else from given list). |
| `A`              | the **alpha** vertex (the packing's designated root). |
| `v`              | the **active** vertex (`packData.activeNode`). |
| `B`              | one vertex per **boundary component** (the starting vertex of each). |
| `M`              | vertex with **maximum index** (i.e., `nodeCount`). |
| `nan`            | vertices with NaN radius or center — useful for debugging. |

### Path- and position-based selection

| Token                    | Meaning |
|--------------------------|---------|
| `c {x} {y} {v..}`        | vertex in `{v..}` whose center is **closest** to plane point (x,y). |
| `D {r} z {x} {y} [{v..}]`| vertices inside disc of radius `r` centered at (x,y); restrict to `{v..}` if given. |
| `D v {v0} r {r}`         | older form: disc centered at vertex `v0` with radius `r`. |
| `z {x} {y}`              | vertex whose circle contains plane point (x,y). |
| `Z {theta} {phi}`        | on the sphere, actual (θ,φ) point (`z` assumes visual plane). |
| `g`                      | vertices on same side of `CPBase.ClosedPath` as alpha. |
| `G {x1 y1 x2 y2 ...}`    | vertex path approximating the given curve (Euclidean only). |
| `Gv {v} {x1 y1 ...}`     | same but starting at vertex `v`. |
| `o {v..}`                | **antipodal** vertex to the given seeds. |
| `h {v} {w} {n}`          | **hex-walk**: `n` steps starting v→w (used for Berger's vector). |

### Structural selection

| Token            | Meaning |
|------------------|---------|
| `m`              | **marked** vertices (`mark` > 0). |
| `mc`             | not-marked vertices (mark == 0). |
| `mq{p}`          | use marks from packing `p` (0, 1, or 2). `mcq{p}` combines. |
| `p`              | vertices with plotFlag set. `pc` = plotFlag not set. |
| `V {w..}`        | for each `w`, vertices `v` such that (v,w) is in `vertexMap`. |
| `W {v..}`        | for each `v`, vertices `w` such that (v,w) is in `vertexMap`. |
| `R` / `Ra`       | **red chain** vertices. `R {side..}` picks by side-pair indices; `Ra` = all. |
| `q {thresh} {v..}` | quality: vertices in `{v..}` whose visual error exceeds `thresh` (default 0.01). |
| `Q {thresh} {v..}` | same. |

### "Incident to" selection

`I{subtype} {list}` gathers the vertices incident to objects of
another kind. Sub-letter picks the kind:

| Form              | Meaning |
|-------------------|---------|
| `If {f..}`        | vertices that are **corners of** the given faces. |
| `Ie {v1 w1 ...}`  | vertices that are **endpoints of** the given edges. |
| `Iv {v..}` or `Ic {v..}` | **petal neighbors** of the given vertices. |
| `It {t..}`        | **corner vertices** of the given tiles. |
| `Ig {x} {v..}`    | vertices in `{v..}` within distance `x` of `CPBase.ClosedPath`. |

### Flower-neighbor selection

For each vertex in a list, take a specific petal:

| Token            | Meaning |
|------------------|---------|
| `n+ {v..}`       | **next** petal after the boundary step (boundary verts only). |
| `n- {v..}`       | previous petal (boundary verts only). |
| `nj {j} {v..}`   | the `j`-th petal (index ≥ 0). |

### Tile-derived

| Token       | Meaning |
|-------------|---------|
| `T {t}`     | **tile-augmented vertices** of tile `t`. |

---

## Face list (FaceLink)

Used by `-f`, `-a`, `-y`, some `-T` cases, `color -f`, and others.

### Selection tokens

Common with vertex list vocabulary: `a`, `a(f1,f2)`, `b`, `m`, `mc`,
`mq{p}`, `r`, `R`, `z {x} {y}`, `Z {theta} {phi}`, `q/Q {thresh}`,
`{...}` (set-builder), `_varname`, `vlist`/`Vlist`/…, bracketed
and paren-ranged stored lists.

Face-specific tokens:

| Token              | Meaning |
|--------------------|---------|
| `a`                | all faces. `a(f1,f2)` = index range. |
| `b`                | **boundary faces** — faces with at least one boundary edge. |
| `A [{startface}]`  | **alternating** faces, 2-coloring starting from `startface` (default 1). Fails if the packing is not 2-colorable. |
| `j`                | **negatively oriented** faces (should be empty in a well-formed packing). |
| `n`                | faces with NaN data. |
| `F`                | **full layout order** — faces in draw order, possibly including "stragglers". |
| `G {x1 y1 x2 y2 ...}` | face chain approximating the given curve (eucl only). |
| `Gv {v} {x1 y1 ...}`  | same, starting at vertex `v`. |

### "Incident to" selection

| Form               | Meaning |
|--------------------|---------|
| `Iv {v..}` or `Ic {v..}` | faces having given vertex as a **corner**. |
| `If {f..}`         | faces sharing a **full edge** with given faces. |
| `Ie {v1 w1 ...}`   | interior faces containing given **edges**. |

Plus the same quality, path, set-builder, and stored-list forms
described in NodeLink.

---

## Edge list (EdgeLink, HalfLink)

Used by `-e`, `-r`, `-dp`, `-dg`, and others. Edges are given as
**pairs** of vertex indices (for `EdgeLink`) or identified as half-edges
(for `HalfLink`, which is the richer form and used by most `disp` flags).

### Selection tokens

| Token              | Meaning |
|--------------------|---------|
| `a`                | all edges. |
| `b`                | **boundary edges**. |
| `i`                | **interior** edges (at least one end interior in the EdgeLink version; both ends interior is a finer form). |
| `r`                | one random edge. |
| `m` / `mv`         | marked edges. `mv` = both ends marked. |
| `o`                | edges with **non-trivial inversive distance** (overlap / separation set). |
| `e {v..}`          | edges **from** the given vertices. |
| `ee {v..}`         | **hex-extended** edges from the given vertices. |
| `d {v1 w1 ...}`    | degrees of the edge's "quad vertices" (for quad-flip analysis). Consumes remaining flag-segment. |
| `g {v w}`          | **combinatorial geodesic path** from v to w. |
| `L`                | **layoutOrder** — edges in the packing's current layout draw order (HalfLink only). |
| `F` / `Fs`         | layoutOrder for drawing faces (HalfLink). `s` variant uses schwarzians. |
| `s {side..}`       | edges from a **side-pair**; `sa` = all red-chain sides. |
| `R`                | **red chain** edges. `R {side..}` picks by side index; `Ra` = all. |
| `z {x} {y}`        | **closest** edge to plane point (x,y). |
| `Z {theta} {phi}`  | same, on sphere using actual (θ,φ). |
| `G {x1 y1 x2 y2 ...}` | edge path approximating a curve. |
| `q/Q {thresh}`     | quality filter. |
| `{...}`            | set-builder. |

### "Incident to" (edges)

| Form               | Meaning |
|--------------------|---------|
| `Iv {v..}` / `Ic {v..}` | all edges **from** given vertices. |
| `If {f..}`         | all three edges of each given face. |
| `Ie {v1 w1 ...}`   | edges **sharing an end** with given edges. |

---

## Tile list (TileLink)

Used by `-T` and tile-specific commands. Requires the packing to have
a populated `tileData` structure.

### Selection tokens

| Token              | Meaning |
|--------------------|---------|
| `a`                | all tiles. |
| `b`                | tiles that have a boundary vertex. |
| `i`                | interior tiles (no boundary vertices). |
| `m` / `mc` / `mq{p}` | marked / not marked / marked in pack p. |
| `c {t..}`          | **children** of the given tiles (uses canonical packing). |
| `B {v..}`          | convert `baryVert` indices back to tile indices. |
| `M {n}`            | only when `ConformalTiling` extender is active and `gradedTileData` exists; selects tiles at depth `n`. |
| `t {t..}`          | **edge-neighboring** tiles of the given tiles. |
| `type {t..}`       | tiles of a given **type** (must be last flag segment). |
| `Iv {v..}` / `Ic {v..}` | tiles whose **corner vertices** include the given ones. |
| `It {t..}`         | (pass-through; same as naming those tiles directly). |
| `{...}`            | set-builder. |

---

## Graph-edge list (GraphLink)

Used by `-dg` and `-dG`. A graph edge is a pair `<f, g>` of face
indices, meaning "the dual edge shared by faces f and g."

Graph edges are given as pairs of face indices:

```
disp -dg 1 2 1 5 2 7             # three dual edges: <1,2>, <1,5>, <2,7>
```

`-dG` uses the stored global `CPBase.Glink` without needing an explicit
list.

---

## Barycentric-point list (BaryLink)

Used by `-tf {face} {b1 b2 b3}` for trinkets placed at barycentric
coordinates within a specified face. Each point is given as a face
index followed by three barycentric weights:

```
disp -t3f 4 0.3 0.3 0.4          # trinket 3 inside face 4 at centroid
disp -t3f 4 0.3 0.3 0.4 7 0.5 0.3 0.2   # two barycentric points
```

---

## Complex-point list (PointLink, PathLink)

Used by `-sz` (shape) and `-tz` (trinket at plane point):

```
disp -sz 0 0 1 0 0.5 0.8         # triangle at three plane points
disp -t5z 0.5 0.5                # trinket 5 at (0.5, 0.5)
```

Points are given as pairs of doubles. PathLink additionally supports
smooth interpolation between control points; see the `path_construct`
command.

---

## Examples by scenario

**"Draw all boundary circles filled with the stored color."**

```
disp -c fc b
```

**"Label just the interior vertices."**

```
disp -nv i
```

**"Show the five vertices closest to plane point (0.5, 0.3)."**

The `c` specifier returns just one vertex, and `D` is the way to do
neighborhoods. For "five closest" you'd use the more general
`D {r} z {x} {y}` with a radius that produces ~5 hits, or do it in
two steps via set-builder. The native list language doesn't do
"nearest-k".

**"Color all faces that share an edge with face 12."**

```
color -f fc80 If 12
```

**"Draw a dual edge for every pair in the global Glink."**

```
disp -dG
```

(equivalently `disp -dg Glist` if you want the explicit list-form.)

**"Display vertices whose visual error exceeds 0.05."**

```
disp -c n q 0.05 a
```

**"Draw the alternating faces, filled, starting at face 1."**

```
disp -f fc A
```

(This only works if the triangulation is 2-colorable.)

**"Trinket 3 at every corner of tiles 5, 6, 7."**

```
disp -t3 Iv It 5 6 7
```

(Nested `I` — `It` gives corner verts of those tiles, and `Iv` would be
redundant here. Simpler: `disp -t3 It 5 6 7`.)

---

## Notes and gotchas

- **Tokens are consumed left-to-right.** Some tokens (like `c x y`,
  `D r z x y`, `G x1 y1 x2 y2 ...`) eat multiple tokens from the
  remaining list. Everything after such a token is consumed by it.
  This is why a combined specifier like `b 3 5 7` is parsed as "all
  boundary" followed by three explicit extras (3, 5, 7) — both are
  in the list.
- **Range syntax uses parentheses**, not brackets. `a(3,17)` gives
  vertices 3 through 17; `a[3,17]` would be interpreted as a bracket
  operator (and most likely rejected).
- **Bracket operators apply to stored-list references** (`vlist`,
  `Vlist`, etc.), not to range expressions. They're single-letter
  selectors that mutate the stored list: `[r]`, `[n]`, `[l]`, `[f]`,
  `[N]`.
- **Uppercase/lowercase matters.** `b` vs `B`, `R` vs `r`, `v` vs `V`
  select different things. Capitalized global-list names
  (`Vlist`/`Elist`/etc.) check validity against the active packing;
  lowercase (`vlist`/`elist`/etc.) trust the stored list as-is.
- **Empty list returns empty.** A command expecting a non-empty list
  (most `disp` flags) will do nothing (and return 0) if the specifier
  resolves to no objects. This is usually silent.
- **Specifiers can be chained** within one flag segment by separating
  with spaces. `disp -c a 3 5 7` draws all circles *and also* circles
  3, 5, 7 — which produces duplicates, drawn twice. For most flags
  this is harmless; for fills with partial opacity, it doubles up.
- **Not every token works in every list type.** `A` means "alpha
  vertex" in `NodeLink` but "alternating faces" in `FaceLink`. `r`
  means "one random vertex" in `NodeLink` but "one random edge" in
  `EdgeLink`. When in doubt, check the source or test with a short
  example.

---

## Source

- `listManip.NodeLink` (vertex vocabulary; main method
  `addNodeLinks`).
- `listManip.FaceLink` (face vocabulary).
- `listManip.EdgeLink` and `listManip.HalfLink` (edge / half-edge
  vocabulary; HalfLink is richer and is what most drawing code uses).
- `listManip.TileLink` (tile vocabulary).
- `listManip.GraphLink`, `listManip.BaryLink`, `listManip.PointLink`,
  `listManip.PathLink` (specialized forms).
- `util.SetBuilderParser` (the `{...}` notation).
- `util.StringUtil.get_paren_range`, `get_bracket_strings`,
  `get_int_range` (shared helpers for range / bracket parsing).
