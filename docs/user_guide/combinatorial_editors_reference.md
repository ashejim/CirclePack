# CirclePack Combinatorial Editors Reference

## About this page

CirclePack's combinatorics can be edited — vertices and edges added
and removed, faces flipped, regions doubled or slit, whole packings
combined or torn apart. This page collects those commands.

Most of these commands modify the underlying triangulation
(the DCEL — see **concepts**). After a combinatorial edit the
radii may no longer match their aims, so a typical workflow is to
edit, then `repack; layout` to re-solve.

Commands covered: `Cleanse`, `cleanse`, `add_cir`, `add_edge`,
`add_bary`, `add_face_triple`, `add_ideal`, `add_layer`, `add_gen`,
`rm_cir`, `rm_bary`, `rm_edge`, `rm_quad`, `split_edge`, `split_flower`,
`bary_refine`, `flip`, `unflip`, `swap`, `meld_edge`, `migrate`,
`double`, `puncture`, `slit`, `zip`, `prune`, `renumber`, `reorient`,
`adjoin`, `blend`, `canonical`, `cookie`, `frack`, `adjust_rad`,
`adjust_sch`.

Not covered here: `weld`, `unweld` — these live in the `WeldManager`
extender, not the core parser; they'll be documented with the
extenders. See also `hex_refine` and `hex_slide` in the
**geometry/layout** reference.

---

## Sections

1. [Package-level clearing](#package-level-clearing) — `Cleanse`, `cleanse`
2. [Adding vertices and edges](#adding-vertices-and-edges) — `add_cir`, `add_edge`, `add_bary`, `add_face_triple`, `add_ideal`, `add_layer`, `add_gen`
3. [Removing vertices and edges](#removing-vertices-and-edges) — `rm_cir`, `rm_bary`, `rm_edge`, `rm_quad`, `meld_edge`
4. [Splitting and refining](#splitting-and-refining) — `split_edge`, `split_flower`, `bary_refine`, `frack`
5. [Flipping](#flipping) — `flip`, `unflip`
6. [Relabeling](#relabeling) — `swap`, `renumber`, `reorient`
7. [Edge operations](#edge-operations) — `migrate`
8. [Topology surgery](#topology-surgery) — `double`, `puncture`, `slit`, `zip`, `prune`, `cookie`
9. [Cross-pack operations](#cross-pack-operations) — `adjoin`, `blend`
10. [Canonical construction](#canonical-construction) — `canonical`
11. [Radius and schwarzian adjustment](#radius-and-schwarzian-adjustment) — `adjust_rad`, `adjust_sch`
12. [Typical workflows](#typical-workflows)
13. [Notes and gotchas](#notes-and-gotchas)
14. [Source](#source)

---

## Package-level clearing

### `Cleanse` — Clear all packings

```
Cleanse
```

Completely empty **every** pack slot (p0, p1, p2) — discarding
their packings and any attached extenders. Nothing is preserved.
Useful when starting a new session fresh or recovering from a
confused state.

### `cleanse` — Clear the current packing

```
cleanse
```

Empty **only the currently active pack slot**. Other packs are
untouched. The current pack is replaced with a fresh empty `PackData`
and its extenders are discarded.

Lower-case `cleanse` is specific; upper-case `Cleanse` is global.

---

## Adding vertices and edges

### `add_cir {v..}` — Add new circles off boundary

```
add_cir {v..}
```

For each listed **boundary** vertex, add a new vertex adjacent to
it along the boundary edge. Non-boundary vertices in the list are
silently skipped.

The new vertex gets a default radius; call `repack; layout` to
solve.

Returns the count of added circles.

### `add_edge {v w ..}` — Add edge(s) between boundary pairs

```
add_edge {v w}                   # one edge
add_edge {v1 w1 v2 w2 ..}        # multiple edges
```

Add an edge between each consecutive pair of boundary vertices.
Both vertices in a pair must be boundary, not already connected,
and share a common neighbor (so that adding the edge fills in a
valid triangle). The common neighbor becomes interior (aim = 2π).

Silently skips invalid pairs with error messages but continues
processing the rest.

### `add_bary {f..}` — Add a barycenter to each face

```
add_bary {f..}                   # specific faces
add_bary a                       # all faces
```

For each listed face, add a new interior vertex at its barycenter,
splitting the face into three. The new vertex is connected to all
three original face vertices.

Returns the count of barycenters added. Duplicate faces in the list
are handled safely (ignored).

### `add_face_triple {f..}` — Add three-vertex face refinement

```
add_face_triple {f..}
```

*Not yet implemented in the DCEL case.* Calling this currently
throws `"'face_triple' call is not yet available for dcel case."`.
When available, it will add three new vertices per face, one at
each edge midpoint.

### `add_ideal` — Add ideal vertices / faces on boundaries

```
add_ideal                        # ideal vertex at every boundary component
add_ideal {v..}                  # ideal vertices at specified boundary starts
add_ideal -b {v..}               # same as above, explicit flag
add_ideal -s {v} {w}             # one ideal vertex connecting from v to w along same bdry
add_ideal -f                     # ideal faces (not vertices) — for 3-sided bdry components
add_ideal -f {v..}               # ideal faces at specified vertices
```

Adds **ideal vertices** (vertices at infinity) or **ideal faces** to
boundary components. Used for canonical packing constructions and
universal covers.

| Flag       | Meaning |
|------------|---------|
| *(none)*   | One ideal vertex per boundary component. |
| `-b {v..}` | Ideal vertex on boundary components containing listed vertices. |
| `-s {v w}` | Single ideal vertex connecting from `v` to `w` (same bdry component). |
| `-f`       | Create ideal **faces** instead of vertices. Only works on 3-sided boundary components. |

Requires the packing to have at least one boundary component.

### `add_layer` — Add a concentric boundary layer

```
add_layer -t {v1} {v2}           # TENT mode (default)
add_layer -d {v1} {v2}           # DUPLICATE mode
add_layer {n} {v1} {v2}          # DEGREE mode, specified degree n
```

Adds a layer of new vertices along the boundary segment from `v1` to
`v2`. The three modes control how new vertices are placed:

| Flag       | Meaning |
|------------|---------|
| `-t`       | TENT mode: each bdry vertex gets one new neighbor above it (default). |
| `-d`       | DUPLICATE mode: replicates the degree pattern of each boundary vertex. |
| *{n}*      | DEGREE mode: each new vertex has degree `n` (must be 4 ≤ n ≤ PackData.MAX_PETALS). |

`v1` and `v2` must be on the boundary. Returns the number of
vertices added. Errors if the packing has no boundary.

### `add_gen {n} [{d}] [-dt] [-b {v..}]` — Add n generations

```
add_gen 3                        # add 3 TENT layers to (first) boundary
add_gen 2 6                      # add 2 DEGREE layers with degree 6
add_gen 5 -d                     # 5 DUPLICATE layers
add_gen 4 -b a                   # 4 layers to every boundary component
add_gen 3 6 -b 1 5 10            # 3 layers of degree 6, on bdry comps starting at 1, 5, 10
```

Iteratively call `add_layer` `n` times — each layer becomes the
basis for the next.

| Flag       | Meaning |
|------------|---------|
| `-t`       | TENT mode (default). |
| `-d`       | DUPLICATE mode. |
| `-b {v..}` | Apply to boundary components containing listed vertices (must be last flag). |
| `{d}`      | If present as the second integer argument, sets DEGREE mode with degree `d`. |

Requires boundary. Returns total vertices added.

---

## Removing vertices and edges

All `rm_*` commands are implemented under a common dispatcher: the
command string after `rm_` is examined to pick the sub-handler.

### `rm_cir {v..}` — Remove circles

```
rm_cir {v..}
```

Remove each listed vertex from the packing. The surrounding
triangulation is rewoven to fill the gap — outer edges of the
removed vertex's flower become the new boundary of the hole, which
is then filled in.

**Special case:** if the list contains exactly one vertex that is
interior and has degree 3, the call is routed to `rm_bary` (since
a degree-3 interior vertex is exactly a barycenter).

### `rm_bary {v..}` — Remove barycenters

```
rm_bary                          # all interior, degree-3 vertices
rm_bary {v..}                    # specific vertices
```

Remove each listed vertex as a "barycenter" — i.e., replace the
three faces around it with one face. Only works on
interior, degree-3 vertices. Fails loudly on any other type.

With no arguments, removes all interior degree-3 vertices.

### `rm_edge {u v ..}` — Remove edges

```
rm_edge {u v}                    # one edge
rm_edge {u1 v1 u2 v2 ..}         # multiple edges
rm_edge -c {u v ..}              # consolidate (meld) instead of removing
```

Remove each listed edge. Fails if the edge doesn't exist.

| Flag | Meaning |
|------|---------|
| `-c` | **Consolidate** mode: melds endpoints into one vertex (equivalent to `meld_edge`). |

Without `-c`, the edge is removed combinatorially, which may leave
the surrounding structure in an unusual state depending on
geometry.

### `rm_quad {u v ..}` — Remove degree-4 endpoints

```
rm_quad {u v}
```

For each edge `u v`, remove the endpoint (if degree-4 and interior)
and reconnect the remaining flower — a specific DCEL move
implemented by `RawManip.rmQuadNode`.

### `meld_edge {u v ..}` — Meld edges (alias for `rm_edge -c`)

```
meld_edge {u v ..}
```

Collapse each listed edge: the two endpoints become one vertex,
and the surrounding structure is rewoven. Identical to
`rm_edge -c`.

---

## Splitting and refining

### `split_edge {u v ..}` — Split edges in half

```
split_edge {u v ..}
```

For each listed edge, insert a new vertex at its midpoint,
splitting the two adjacent faces into four. Returns the number of
successful splits.

### `split_flower {v} {w} [{u}]` — Split a vertex's flower

```
split_flower {v} {w}             # v on boundary, w interior neighbor
split_flower {v} {w} {u}         # v interior; w and u are neighbors of v
```

Split the flower (star) of vertex `v` at the edges to `w` (and
optionally `u`):

- **Boundary case:** `v` is boundary, `w` is an interior neighbor.
  Inserts a new vertex duplicating part of `v`'s structure.
- **Interior case:** `v` is interior, `w` and `u` must both be
  neighbors of `v`. Duplicates part of `v`'s flower between the
  edges to `w` and `u`.

The new vertex is centered between the reference vertices and
inherits the radius.

Returns the new vertex's index.

### `bary_refine` — Full hex-barycentric refinement

```
bary_refine
```

Uniformly refine the packing by adding a barycenter to every face
— the hex-refinement variant that preserves hexagonal structure
where possible. More aggressive than `hex_refine` (see
**geometry/layout**) for certain complexes.

No arguments.

### `frack {v..}` — "Frack" vertices

```
frack {v..}
```

Applies `RawManip.frackVert` to each listed vertex — a
triangulation refinement move related to the packing's combinatorial
structure. (The exact combinatorial effect is terse in the source;
think of it as a local refinement that "fractures" a vertex's
neighborhood.)

Returns the new `nodeCount`.

---

## Flipping

### `flip` — Flip edges

```
flip {v w ..}                    # flip listed edges (default)
flip -v {v w ..}                 # same, explicit
flip -h {v w}                    # half-hex: flip-and-advance (one edge)
flip -r                          # flip one random interior edge
flip -cc {v w ..}                # for each edge, flip the next cclw edge
flip -hh {v w ..}                # deprecated alias for -cc
flip -cw {v w ..}                # for each edge, flip the next cw edge
```

**Edge flipping** is a basic combinatorial move: given an edge
shared by two triangles forming a quadrilateral, replace it with
the other diagonal.

| Flag       | Meaning |
|------------|---------|
| `-v`       | Explicit "flip given vertex pair" (default). |
| `-h {v w}` | Half-hex flip-and-advance: flip the clockwise neighbor of `{v,w}` and store the advanced edge in `elist`. Used iteratively along half-hex paths. |
| `-r`       | Flip one random interior edge (up to 20 attempts). |
| `-cc`      | For each listed edge, flip the counter-clockwise neighbor. |
| `-hh`      | Deprecated synonym for `-cc`. |
| `-cw`      | For each listed edge, flip the clockwise neighbor. |

Returns the number of successful flips.

### `unflip {v w ..}` — Flip an edge list (companion command)

```
unflip {v w ..}
```

Despite the name, `unflip` and `flip` (without flags) do the same
thing: they flip every edge in the list. `unflip` is provided as a
semantic companion — calling `flip` then `unflip` on the same
list returns to the original combinatorics.

Returns the number of successful flips.

---

## Relabeling

### `swap {v} {w}` — Swap two vertex indices

```
swap {v} {w}                     # swap; discard color/mark/aim
swap -c {v} {w}                  # preserve colors under the swap
swap -m {v} {w}                  # preserve marks
swap -a {v} {w}                  # preserve aims
swap -cma {v} {w}                # preserve all three (flags can combine)
```

Swap the indices of vertices `v` and `w`. By default, per-vertex
attributes (color, mark, aim) move with the vertex. The flags let
you control which attributes move:

| Flag | Meaning |
|------|---------|
| `-c` | Preserve colors (colors stay attached to the new index, not moved). |
| `-m` | Preserve marks. |
| `-a` | Preserve aims. |

Flags can combine: `-cma` preserves all three.

Useful for rearranging the order of vertices without changing the
combinatorial structure — e.g., to make a specific vertex be
`alpha` (vertex 1) or `gamma` (typically the last-indexed boundary
vertex).

### `renumber` — Rebuild the numbering

```
renumber
```

Renumber all vertices canonically (breadth-first from alpha,
typically). Useful after extensive combinatorial edits that have
left a sparse or confusing numbering.

No arguments. Returns success count.

### `reorient` — Reverse orientation

```
reorient
```

Reverse the orientation of the packing — swap clockwise and
counter-clockwise throughout. Useful when a packing has been
imported or constructed with the wrong orientation.

No arguments. Returns the new `vertCount`.

---

## Edge operations

### `migrate {u v}` — Migrate an edge

```
migrate {u v}
```

Apply `RawManip.migrate` to edge `u v` — a specific DCEL edge
modification move (essentially a local combinatorial rearrangement
that "migrates" the edge across a face).

Takes one edge only. Returns nonzero on success.

---

## Topology surgery

These commands modify the topological type or boundary structure of
the packing.

### `double [v..] [b(v,w)]` — Double across boundary

```
double                           # double across every boundary component
double {v..}                     # double across bdry comps containing listed verts
double b({v},{w})                # double across segment from v to w
```

**Doubling** identifies each boundary vertex with a reflected copy
across the boundary, producing a closed or partly-closed surface.

- **Default form:** doubles across every boundary component.
- **Vertex list form:** doubles only across components
  containing listed vertices.
- **Segment form `b(v,w)`:** doubles across the segment from `v`
  to `w` on a single boundary component.

Radii are duplicated from the original packing to its mirror. A
vertex map is stored so you can track original-to-doubled
correspondence.

Used heavily in perpendicular-packing constructions (see
`perp_pack`) and in symmetry-based experiments.

### `puncture` — Remove a vertex (opposite of `cleanse`)

```
puncture                         # puncture max-index vertex (default)
puncture {v}                     # puncture specified vertex
puncture -f {f}                  # puncture a face instead of a vertex
```

Remove a vertex (default) or a face, creating a new boundary
component where the removed element was. In contrast to `rm_cir`
(which reweaves the hole), `puncture` leaves a hole — the surface
gains a boundary.

| Flag     | Meaning |
|----------|---------|
| *(none)* | Puncture default vertex (max index). |
| `{v}`    | Puncture specified vertex. |
| `-f {f}` | Puncture face `f` instead. |

`xyzpoint` data is cleared when a vertex is punctured (since the
cross-reference is no longer valid).

### `slit {v..}` — Slit along a chain

```
slit {v..}
```

Cut the packing open along the chain of edges through the listed
interior vertices. Each interior vertex in the chain becomes two
boundary vertices; a new boundary component is created.

Returns success count (1 on success, 0 on failure). Reports the
index range of the new boundary edges.

### `zip {n} {v}` — Zip up boundary segment

```
zip {n} {v}                      # zip n edges starting at bdry vertex v
zip -1 {v}                       # zip the entire boundary component
```

The inverse of `slit` — identifies pairs of boundary edges along
a boundary component, zipping up the packing.

- `v` must be a boundary vertex.
- `n` = -1 (or out-of-range) means zip the entire component.
- `n` is clamped: if the boundary has `b` edges and `b` is even,
  `n ≤ b/2`; if odd, `n ≤ (b-1)/2`.

The edges pair up starting at `v`, moving in the standard
direction. Returns the new `nodeCount`.

Useful for building closed surfaces from cut-open planar packings.

### `prune` — Prune degree-0 and similar artifacts

```
prune
```

Remove any isolated or degenerate vertices left over from other
operations. Returns the count of vertices pruned.

Vertex-index-remapping information is stored in `packData.vertexMap`
so you can track how old indices map to new ones.

### `cookie` — Cut out a region

```
cookie {flags}
```

"Cookie cut" a subregion out of the packing, forming a new red
chain around the desired region. The flag set (handled by
`CombDCEL.cookieData`) specifies which edges to treat as
"forbidden" (i.e., the cut boundary).

Sets default aims after the cookie. Returns the new `vertCount`.

Typical uses:
- Extract a sub-disc from a larger packing.
- Cut around a specific vertex to create a smaller test case.
- Isolate a simply-connected region from a multiply-connected
  packing.

The exact flag vocabulary is handled internally by `CombDCEL`;
common usages include specifying edge lists defining the cut
boundary.

---

## Cross-pack operations

### `adjoin {p1} {p2} {v1} {v2} {N}` — Join two packings

```
adjoin {p1} {p2} {v1} {v2} {N}           # n edges clockwise from v1, v2
adjoin {p1} {p2} {v1} {v2} ({v1} {w})    # edges along bdry segment v1 to w
```

Glue pack `p2` to pack `p1` along a boundary segment of `N` edges
starting at `v1` in `p1` and `v2` in `p2`. The result is stored
in pack `p1`.

Two argument forms for the segment length:
- **Integer `N`:** join `N` edges clockwise from the start
  vertices.
- **`(v1 w)` form:** count edges clockwise on `p1`'s boundary from
  `v1` to `w`, using that count.

Both `v1` and `v2` must be on their respective packings'
boundaries.

A special case: if `p1 == p2` and `v1 == v2`, the command is
"zip up starting at `v1`" (requires at least 3 neighbors).

Returns packing size on success.

### `blend -q{p} {v} {n}` — Blend with another packing

```
blend -q1 5 3
```

Blend the local neighborhood of vertex `v` (out to generation `n`)
with the corresponding neighborhood in pack `p`. Useful for
combining results of different experiments or smoothing transitions
between related packings.

Arguments:
- `-q{p}`: specify the blending partner pack.
- `v`: vertex around which to blend.
- `n`: number of generations to include.

---

## Canonical construction

### `canonical` — Construct canonical packing

```
canonical
```

Produce the **canonical packing** for a multiply-connected planar
surface (Euler characteristic 0, has boundary). The algorithm:

1. Find the boundary components; identify the longest and
   shortest.
2. Add ideal vertices to all but these two components.
3. Add a final ideal vertex to the shortest component (making it
   the new alpha).
4. Now the packing is combinatorially a disc, so run `max_pack`.
5. Puncture back the added ideal vertices.
6. Convert to Euclidean.
7. Reset default aims.

For simply-connected packings (no multiple boundary components),
`canonical` just runs `max_pack`.

No arguments. The result is the canonical Euclidean form of the
input multiply-connected planar surface.

---

## Radius and schwarzian adjustment

These aren't strictly "combinatorial" edits, but they modify
packing data via vertex/edge lists in the same style.

### `adjust_rad {x} {v..}` — Multiply radii

```
adjust_rad 2.0 5 7               # double radii at vertices 5 and 7
adjust_rad 0.5 a                 # halve all radii
```

Multiply the radius of each listed vertex by the factor `x`.
Factor must be positive.

Differs from `scale` (which scales the entire packing uniformly):
`adjust_rad` operates per-vertex.

### `adjust_sch {x} {v w ..}` — Multiply schwarzians

```
adjust_sch 1.5 v1 w1 v2 w2
```

Multiply the **schwarzian** at each listed edge by factor `x`.
Factor must be positive. See **concepts** for the schwarzian
definition.

---

## Typical workflows

### Refine a triangulation uniformly

```
bary_refine                      # every face gets a barycenter
set_aim_default                  # reset aims for new vertices
repack
layout
disp -wr
```

### Add a layer to every boundary

```
add_gen 3 -b a                   # 3 TENT layers on every bdry component
repack
layout
```

### Cut and re-solve

```
slit 10 15 20                    # slit along this chain of vertices
set_aim_default
repack
layout
```

### Glue two packings together

```
# pack 1 and pack 2 both loaded
adjoin 0 1 5 10 20               # join 20 edges, p0 and p1 starting at v=5 and v=10
act 0                            # pack 0 now holds the result
repack
layout
```

### Make a branching experiment

```
copy 1                           # save to pack 1
flip 3 5                         # flip the edge
repack
layout
disp -wr                         # see the result
act 1                            # back to the saved version
```

### Create a canonical multiply-connected packing

```
# starting with a surface with several boundary components
canonical
disp -wr
```

### Reset to empty

```
Cleanse                          # all packs empty
```

---

## Notes and gotchas

- **Combinatorial edits don't re-solve.** After almost any edit,
  you'll need `repack; layout` (or `rld`) to see a valid geometric
  embedding. Operations that only relabel (like `renumber`,
  `reorient`, `swap`) don't require re-solving.

- **`puncture` clears `xyzpoint`.** Don't use `puncture` if you
  depend on xyz data for downstream steps — copy the data out first.

- **`rm_cir` with a single interior degree-3 vertex is rerouted to
  `rm_bary`.** This is intentional — that's precisely what a
  barycenter is — but be aware if you're reading logs.

- **`add_face_triple` is not yet implemented** in the DCEL case.
  The source throws an exception. Use `add_bary` instead.

- **`flip -h` advances state.** The call stores the advanced edge
  in `packData.elist`, so calling `flip -h elist` repeatedly walks
  along a half-hex path, flipping as you go. Not a pure function.

- **`flip -r` only flips one edge per call.** Call repeatedly (in
  a loop) for extensive random perturbation.

- **`unflip` is a misnomer.** It does the same thing as `flip`
  (without flags). Calling `flip 3 5; unflip 3 5` flips the edge
  twice, returning to the original — hence the companion naming.

- **`double` changes the genus/boundary structure.** After
  doubling across a boundary, that boundary is gone and the
  surface becomes closed or partly closed. The `vertexMap` tracks
  the doubling.

- **`slit` and `zip` are inverses** — `slit` cuts a chain open;
  `zip` closes up boundary edges. Using them together, you can
  re-partition how the surface is cut.

- **`zip` requires a starting boundary vertex.** Errors if given
  an interior one.

- **`Cleanse` is irreversible.** All packs are emptied; make sure
  you've saved anything you need first.

- **`adjoin` stores the result in `p1`.** If `p1` had a packing
  already, it's replaced.

- **`canonical` always converts to Euclidean.** If you want the
  canonical hyperbolic form, use `max_pack` directly on a
  simply-connected packing.

- **`cookie` requires a red chain setup.** The command interprets
  its flags as specifying cut edges; passing an arbitrary edge
  list may fail silently. For simple use cases, prefer `slit`.

- **`frack` is thinly documented in source.** The effect is a
  specific DCEL move; if you're uncertain, test on a copy first
  (`copy` to another pack, then `frack`).

- **`renumber` can break external references.** If you're tracking
  specific vertex indices in a script, they may change after a
  renumber — save a `vertexMap` first via appropriate bookkeeping.

- **`blend` doesn't re-solve.** The blended values are installed;
  run `repack; layout` afterward.

---

## Source

- `dcel.RawManip` — most combinatorial edits call into static
  methods here (`addVert_raw`, `rmBary_raw`, `rmEdge_raw`,
  `meldEdge_raw`, `rmQuadNode`, `splitEdge_raw`, `splitFlower_raw`,
  `flipEdge_raw`, `flipAdvance_raw`, `migrate`, `addBary_raw`,
  `addIdeal_raw`, `frackVert`, `hexBaryRefine_raw`).
- `combinatorics.komplex.CombDCEL` — higher-level combinatorial
  operations (`addlayer`, `doubleDCEL`, `slitComplex`, `pruneDCEL`,
  `reNumber`, `reorient`, `cookieData`, `redchain_by_edge`,
  `extractDCEL`).
- `packing.PackData` — packing-level methods (`add_ideal`,
  `puncture_vert`, `puncture_face`, `flipList`, `blend`,
  `swap_nodes`, `adjust_rad`, `adjust_uzian`, `adjoinCall`).
- `input.CommandStrParser` — top-level dispatch for each of these
  commands.
