# CirclePack Lists and Paths Reference

## About this page

CirclePack maintains **stored lists** of objects — vertices, faces,
edges, half-edges, tiles, graph edges, barycentric coordinates,
complex points — and **paths** (planar curves) that can be read
from files, constructed from edge sequences, drawn, and transformed.

This page documents the commands that create, manipulate, and use
those stored lists and paths. The **vocabulary** for specifying
list contents (`a`, `b`, `i`, `{v..}`, `b(v,w)`, `{c:...}`, etc.)
is covered in the **list_specifiers** page; this page is about
the commands that *operate on* lists as data structures.

Commands covered: `set_vlist`, `set_Vlist`, `set_flist`, `set_Flist`,
`set_elist`, `set_Elist`, `set_hlist`, `set_Hlist`, `set_tlist`,
`set_Tlist`, `set_glist`, `set_Glist`, `set_blist`, `set_Blist`,
`set_zlist`, `set_Zlist`, `set_Dlist`, `elist_to_path`,
`path_construct`, `path_Mob` (`path_mob`), `enclose`.

Not covered here (but cross-referenced):
- `set_path`, `set_path_text`, `set_grid` — path construction
  commands, documented in **setters_reference**.
- `read_path`, `Read_path`, `infile_path`, `write_path`,
  `Write_path` — path I/O, documented in **io_reference**.
- `disp -g` — draw the stored path, documented in **disp**.

---

## Sections

1. [Stored lists: concepts](#stored-lists-concepts)
2. [The `set_*list` family](#the-set_list-family)
3. [Per-pack vs global lists](#per-pack-vs-global-lists)
4. [List types and their contents](#list-types-and-their-contents)
5. [Using stored lists elsewhere](#using-stored-lists-elsewhere)
6. [Paths: concepts](#paths-concepts)
7. [`elist_to_path`](#elist_to_path)
8. [`path_construct`](#path_construct)
9. [`path_Mob`](#path_mob)
10. [`enclose`](#enclose)
11. [Typical workflows](#typical-workflows)
12. [Notes and gotchas](#notes-and-gotchas)
13. [Source](#source)

---

## Stored lists: concepts

A **stored list** is a named handle to a collection of objects
(vertices, faces, edges, etc.) that persists across commands. Once
set, a stored list can be:

- Named in any command that takes a list of that type — e.g.,
  `disp -c vlist` draws the stored vertex list.
- Displayed, transformed, or operated on collectively.
- Reset or replaced with new contents.

Stored lists exist because many CirclePack workflows need to refer
to "the same set of objects" across several commands: you identify
an interesting set once (via a selector, a boundary segment, or an
operation's output) and then work with it repeatedly.

There are nine list types and two scopes, giving eighteen distinct
stored lists at any given moment.

---

## The `set_*list` family

The setter commands all follow a uniform syntax:

```
set_vlist {selector}             # set packData.vlist
set_Vlist {selector}             # set CPBase.Vlink (global)
set_vlist                        # clear packData.vlist
set_Vlist                        # clear CPBase.Vlink

# same pattern for flist/Flist, elist/Elist, hlist/Hlist, etc.
```

The `{selector}` is any valid list specifier for the appropriate
type (see **list_specifiers** for the full vocabulary). If no
argument is given, the list is cleared.

Returns the size of the new list on success, or `1` on clear.

### Uniform syntax

Every list type supports:

| Form                 | Effect |
|----------------------|--------|
| `set_xlist`          | Clear the list |
| `set_xlist a`        | Store all objects of that type |
| `set_xlist b`        | Store boundary objects (where applicable) |
| `set_xlist i`        | Store interior objects (where applicable) |
| `set_xlist {n1 n2}`  | Store specific objects by index |
| `set_xlist b(v,w)`   | Store a boundary segment |
| `set_xlist {c:...}`  | Store objects matching a predicate |

Not every specifier makes sense for every list type — see
**list_specifiers** for type-specific vocabulary.

---

## Per-pack vs global lists

**Case matters.** Every list has a lowercase per-pack version and
an uppercase global version:

| Command name    | Storage location        | Scope |
|-----------------|-------------------------|-------|
| `set_vlist`     | `packData.vlist`        | Per-pack |
| `set_Vlist`     | `CPBase.Vlink`          | Global (across all packs) |
| `set_elist`     | `packData.elist`        | Per-pack |
| `set_Elist`     | `CPBase.Elink`          | Global |

(and so on for every list type)

**Per-pack lists** live on the packing's data and are forgotten
when you switch to another pack or load a new packing in the same
slot.

**Global lists** live on the singleton `CPBase` and persist across
pack switches. They're useful for cross-packing workflows — e.g.,
identifying a set of "corresponding" vertices you want to compare
across multiple packings.

When you pass a list name to a command, you can specify which scope
you want. By convention (common to display flags and other
list-consumers): a lowercase list name (`vlist`, `elist`) refers to
the per-pack version; an uppercase name (`Vlist`, `Elist`) refers
to the global version.

---

## List types and their contents

### `v` / `V` — Vertex lists (`NodeLink`)

```
set_vlist a                      # all vertices → packData.vlist
set_vlist b                      # boundary vertices
set_vlist {c:(d.eq.6)}           # degree-6 vertices
set_Vlist 1 5 10 20              # global: these four vertices
```

Each entry is a single integer vertex index.

### `f` / `F` — Face lists (`FaceLink`)

```
set_flist a                      # all faces → packData.flist
set_flist Iv 5                   # faces incident to vertex 5
set_Flist {f:(col.eq.180)}       # global: red faces
```

Each entry is a face index.

### `e` / `E` — Edge lists (`EdgeLink`)

```
set_elist {u v}                  # one edge → packData.elist
set_elist b                      # boundary edges
set_Elist {u1 v1 u2 v2}          # global: two edges
```

Each entry is an `EdgeSimple` — an unordered pair `{u, v}`.

### `h` / `H` — Half-edge lists (`HalfLink`)

```
set_hlist {u v}                  # half-edge from u toward v
set_hlist eh e5 7                # half-hex chain through edge (5,7)
```

Each entry is an oriented half-edge (a `HalfEdge` object). The
half-edge specifier vocabulary includes traversal operators like
`eh` (hex-chain extension); see **list_specifiers**.

### `t` / `T` — Tile lists (`TileLink`)

```
set_tlist a                      # all tiles (if tileData exists)
set_Tlist 1 5 10                 # global tile list
```

Only meaningful when the packing has `tileData` set (via `pave` or
`read_CT`).

### `g` / `G` — Graph lists (`GraphLink`)

```
set_glist {f1 f2 ..}             # graph edges (face pairs)
```

Each entry is an edge in the dual graph — a pair of face indices.

### `b` / `B` — Barycentric lists (`BaryLink`)

```
set_blist {f;b0 b1 b2}           # one barycentric point
```

Each entry is a face index plus barycentric coordinates. Useful
for specifying points inside faces (as distinct from vertices).

### `z` / `Z` — Point lists (`PointLink`)

```
set_zlist {x y}                  # complex point (x,y)
set_zlist {0 0} {1 0} {0 1}      # three complex points
```

Each entry is a complex number (pair of doubles). Not tied to
packing combinatorics — raw points in the plane.

### `D` — Double list (uppercase only)

```
set_Dlist 1.5 2.3 0.7            # three doubles
```

Each entry is a double-precision number. Global only (no per-pack
version). Used for parameter sweeps and utility storage.

---

## Using stored lists elsewhere

Stored lists can be passed as arguments to most commands that take
a list of the corresponding type:

```
set_vlist b                      # store boundary vertices
disp -c vlist                    # draw the stored list
rm_cir vlist                     # delete them
set_color -cc180 vlist           # color them red
```

The standard rule is: wherever a command's argument position
accepts `{v..}`, `{f..}`, `{e..}`, or similar, you can substitute
the list name directly — `vlist`, `flist`, `elist`, `hlist`,
`tlist`, `glist`, `blist`, `zlist`. For the global versions, use
the capitalized forms.

Some commands can also modify a stored list as a side effect —
e.g., `path_construct` appends to `CPBase.Vlink`; `hh_path`
stores its result in `packData.elist`.

---

## Paths: concepts

A **path** in CirclePack is a planar curve (`java.awt.geom.Path2D`)
stored as `CPBase.ClosedPath`. Paths are used for:

- Drawing (`disp -g` renders the path on the canvas).
- Cookie-cutting regions of the plane.
- Constructing grids (`set_grid -g` builds a grid from a path).
- Input to algorithms that need curve data (contour integration,
  region selection).

There is one active path at a time (`CPBase.ClosedPath`). To work
with multiple paths, save and restore them via
`read_path`/`write_path`.

---

## `elist_to_path`

Convert a stored edge list into a planar path by walking the edges
and linking their vertex centers.

### Synopsis

```
elist_to_path {e..}
```

### What it does

1. Builds an `EdgeLink` from the supplied specifier.
2. Starts the path at the first vertex center.
3. For each edge, calls `lineTo` with the next vertex's center.
4. Closes the path at the end.
5. **Replaces** `CPBase.ClosedPath` with the new path.

For spherical packings, vertex centers are converted from sphere
to plane coordinates.

### Examples

```
set_elist b                      # boundary edges
elist_to_path elist              # make boundary path

# direct — same result
elist_to_path b
```

### Notes

- The path is always closed (`gpath.closePath()`), so the last
  point is connected back to the first.
- For non-closed chains, the visual path will still close the loop
  automatically — this may produce an unexpected connector
  segment.

---

## `path_construct`

Build a path (or sequence of vertices) from a prescribed sequence
of vertex moves, and append the result to `CPBase.Vlink`.

### Synopsis

```
path_construct {v1} {v2} {v3} ... # -m mode (default): build edge path
path_construct -m {v1} {v2} ...   # same, explicit
path_construct -i {v1} {v2} ...   # incremental-turns mode
```

### What it does

Given a starting vertex `v1` and a sequence of targets, walks
through the combinatorics producing a vertex path:

- **`-m` mode (default):** treat the sequence as a specification of
  an edge path — each consecutive pair is connected by an edge
  walk.
- **`-i` mode:** interpret the sequence as incremental turns at
  each stage — useful for constructing paths along hex or related
  regular structures.

The resulting path (as a `NodeLink` of vertices visited) is
appended to `CPBase.Vlink` (the global vertex list). If `Vlink` was
empty, the starting vertex `v1` is placed first; otherwise, `v1`
is added only if the current end of `Vlink` isn't already `v1`
(avoiding duplicates at join points).

### Flags

| Flag  | Meaning |
|-------|---------|
| `-m`  | Edge-path mode (default). |
| `-i`  | Incremental-turns mode. |

### Examples

```
# Build a path from vertex 1 through 50 through 100
path_construct 1 50 100

# Same, explicit mode flag
path_construct -m 1 50 100

# Incremental turns
path_construct -i 1 3 3 3         # starting at 1, turn right 3 times, etc.
```

### Notes

- Results go to `CPBase.Vlink`, not `packData.vlist`. If you want
  the per-pack version, copy manually: `set_vlist Vlink`.
- Multiple `path_construct` calls concatenate into `CPBase.Vlink`
  rather than replacing — useful for building complex paths from
  simpler pieces.
- The exact semantics of `-i` mode depend on `packData.path_construct`
  — see source for the precise turn vocabulary.

---

## `path_Mob`

Apply the stored Möbius transformation (`CPBase.Mob`) to the
stored path (`CPBase.ClosedPath`).

### Synopsis

```
path_Mob
path_mob                         # same (lowercase alias)
```

### What it does

Calls `Mobius.path_Mobius(CPBase.Mob, CPBase.ClosedPath, true)` —
applies `CPBase.Mob` to every point of the path and replaces
`CPBase.ClosedPath` with the result.

No arguments. Uses whatever is currently in `CPBase.Mob` (set by
`set_Mobius` or another command that produces a Möbius).

### Examples

```
set_Mobius -xyzXYZ 0 0 1 0 0 1  0 0 2 0 0 2   # define a Mobius
path_Mob                                       # apply to the path
disp -g                                        # draw the transformed path
```

### Notes

- The transformation is one-way — there's no `inv_path_Mob`. For
  the inverse, set up the inverse Möbius first via `set_Mobius`.
- If `CPBase.ClosedPath` is null, the call fails silently (or
  produces a degenerate result).

---

## `enclose`

Add circles around specified boundary vertices to ensure each has
a minimum number of neighbors.

### Synopsis

```
enclose {n} {v..}                # n new neighbors per vertex
enclose -t {n} {v..}             # total degree n (including existing)
```

### What it does

For each listed boundary vertex `v`, add enough new circles around
`v` to bring its neighbor count up to either:

- Its current neighbors plus `n` (default), or
- Total degree `n` (with the `-t` flag).

Only operates on boundary vertices. Interior vertices in the list
are skipped.

### Flags

| Flag  | Meaning |
|-------|---------|
| *(none)* | Add `n` new neighbors to each boundary vertex. |
| `-t`  | Total degree `n` — bring each vertex up to degree `n`. |

### Arguments

| Arg    | Meaning |
|--------|---------|
| `n`    | Number of neighbors (as per-vertex count or total). Must satisfy `0 ≤ n < MAX_PETALS-2`. |
| `{v..}`| Vertex list. Typically `b` (all boundary) or specific indices. |

### Examples

```
# Give every boundary vertex 2 more neighbors
enclose 2 b

# Make every boundary vertex have total degree 6
enclose -t 6 b

# Specific vertices only
enclose 3 5 10 15
```

### Notes

- After `enclose`, run `repack; layout` (or `rld`) to re-solve with
  the new combinatorics.
- Useful for conformal-map experiments where you want a prescribed
  boundary-vertex structure.
- Related to `add_layer` (which adds whole layers) and `add_gen`
  (which adds multiple layers) — `enclose` gives finer per-vertex
  control.

---

## Typical workflows

### Identify and mark a set

```
set_vlist {c:(d.eq.6)}           # all degree-6 vertices
?count                           # how many total?
disp -cfc200 vlist               # color them red
set_color -cc200 vlist           # permanently
```

### Cross-packing comparison

```
# In pack 0: pick an interesting subset
set_Vlist b(5,25)                # boundary segment → global

# Switch to pack 1 (same combinatorics, different radii)
act 1
disp -cfc180 Vlist               # same vertices, red, in pack 1
```

### Build a boundary path and use it

```
set_elist b                      # boundary edges
elist_to_path elist              # make it a path
disp -g                          # draw the path
```

### Construct a hex-path experiment

```
# Walk along hex structure
path_construct -i 1 3 3 3 3 3 3  # 6 turns of type 3
disp -cc180 Vlink                # highlight the path vertices
```

### Transform a region via Möbius

```
# Set up an initial path
set_elist b(5, 20)               # boundary segment
elist_to_path elist
disp -g                          # original path

# Define a Möbius and apply
set_Mobius -xyzXYZ 0 0 1 0 0 1  0 0 2 0 0 2
path_Mob
disp -g                          # transformed path
```

### Enclose a boundary

```
# Make every boundary vertex have at least 5 neighbors
enclose -t 5 b
rld                              # re-solve and redraw
```

---

## Notes and gotchas

- **List names are case-sensitive and refer to different storage.**
  `vlist` ≠ `Vlist`. The first is per-pack; the second is global.
- **Stored lists survive across commands** until you explicitly
  clear or overwrite them. A common source of confusion: running a
  command that reads `vlist` but you set `Vlist`, or vice versa.
- **`set_xlist` with no argument clears the list** — it doesn't
  "unset" it to some default; it sets it to null.
- **`path_construct` appends** to `CPBase.Vlink`. If you want a
  fresh start, clear it first: `set_Vlist`.
- **`elist_to_path` replaces** `CPBase.ClosedPath` entirely. Save
  the old path first with `write_path` if you need it.
- **`elist_to_path` always closes the path.** For open curves, the
  visual has a "wrap-around" connector from last point to first.
- **`enclose` only acts on boundary vertices.** Listed interior
  vertices are silently skipped.
- **`path_Mob` needs both `CPBase.Mob` and `CPBase.ClosedPath`
  to be set.** No argument means "use whatever's there"; null
  values produce silent failure.
- **Stored lists are affected by combinatorial edits.** After
  `rm_cir`, `swap`, `renumber`, or other edits that renumber
  vertices, existing lists may point to wrong indices. Typically
  re-set the list after such edits.

---

## Source

- `listManip.NodeLink`, `FaceLink`, `EdgeLink`, `HalfLink`,
  `TileLink`, `GraphLink`, `BaryLink`, `PointLink`, `DoubleLink`
  — the list classes; each has its own parser vocabulary. See
  **list_specifiers**.
- `packing.PackData.vlist`, `flist`, `elist`, `hlist`, `tlist`,
  `glist`, `blist`, `zlist` — the per-pack storage fields.
- `allMains.CPBase.Vlink`, `Flink`, `Elink`, `Hlink`, `Tlink`,
  `Glink`, `Blink`, `Zlink`, `Dlink` — the global storage fields.
- `allMains.CPBase.ClosedPath` — the stored path (`Path2D.Double`).
- `allMains.CPBase.Mob` — the stored Möbius transformation.
- `math.Mobius.path_Mobius` — applies a Möbius to a path.
- `packing.PackData.path_construct` — the path-construction
  backend.
- `input.CommandStrParser` — top-level dispatch for each of these
  commands.
