# CirclePack Core Concepts

## What this page is for

Every CirclePack command page uses terms that assume you know what
they mean: *alpha*, *gamma*, *aim*, *schwarzian*, *red chain*, *hes*,
*flower*, *tangency packing*, *DCEL*, and a dozen others. This page
defines those terms once so the command pages don't have to.

If you're new to circle packing, read through this page once
linearly — the definitions build on each other. If you're looking
something up, use the table of contents.

---

## Table of contents

1. [Circle packings: what they are](#circle-packings-what-they-are)
2. [The packing data structure](#the-packing-data-structure)
3. [Vertices, faces, edges](#vertices-faces-edges)
4. [Flower and petals](#flower-and-petals)
5. [Geometry: hyperbolic, Euclidean, spherical](#geometry-hyperbolic-euclidean-spherical)
6. [Alpha and gamma: the rooting vertices](#alpha-and-gamma-the-rooting-vertices)
7. [Aim and angle sum](#aim-and-angle-sum)
8. [Radii and centers](#radii-and-centers)
9. [Tangency packings and overlap packings](#tangency-packings-and-overlap-packings)
10. [Inversive distance and overlap](#inversive-distance-and-overlap)
11. [Maximal packings](#maximal-packings)
12. [Boundary and interior](#boundary-and-interior)
13. [Branch points](#branch-points)
14. [DCEL and `packDCEL`](#dcel-and-packdcel)
15. [Layout order](#layout-order)
16. [Red chain and side pairings](#red-chain-and-side-pairings)
17. [Holonomy](#holonomy)
18. [Schwarzians](#schwarzians)
19. [Tiles and tile data](#tiles-and-tile-data)
20. [Barycenters and barycentric coordinates](#barycenters-and-barycentric-coordinates)
21. [Repacking and the riffle algorithm](#repacking-and-the-riffle-algorithm)
22. [Glossary / quick reference](#glossary--quick-reference)

---

## Circle packings: what they are

A **circle packing** in CirclePack's sense is a configuration of
circles in the plane (or disk, or sphere) arranged so that their
pattern of tangencies matches a prescribed **combinatorial
triangulation**. You specify *which* circles should touch *which*
— a combinatorial graph — and the software finds radii and centers
that realize those tangencies geometrically.

This is the Thurston-style notion of circle packing, not the
related "pack N circles into a square" optimization problem.

Two pieces of data define a packing:

- **Combinatorial data** — the triangulation: which circles exist,
  and which pairs are supposed to be tangent.
- **Geometric data** — the radii (one per circle) and centers (one
  per circle) that satisfy the tangency pattern.

The combinatorial data is fixed once the triangulation is chosen.
The geometric data is what `repack` and `layout` compute.

---

## The packing data structure

CirclePack holds up to **three packings** simultaneously, in slots
`p0`, `p1`, `p2`. Each packing is represented internally by a
`PackData` object, which stores:

- The combinatorial structure, via a `PackDCEL` (see **DCEL**).
- Per-vertex data: radius, center, aim, color, mark, plot-flag.
- Per-face data: color, center.
- Per-edge data: color, overlap / inversive distance.
- Tile data (optional) — the `tileData` field, if populated.
- Side-pairing data for multiply-connected surfaces.
- A "layout order" (`layoutOrder`) — a sequence of half-edges
  defining how the packing is drawn.

You rarely interact with these fields by name. Instead, commands
read and modify them on your behalf.

---

## Vertices, faces, edges

A circle packing is described by a **triangulation** — a
combinatorial complex of:

- **Vertices** — the circles. Each vertex is identified by a
  positive integer index (1, 2, 3, …).
- **Faces** — the triangles between three mutually tangent
  circles. Also integer-indexed.
- **Edges** — pairs of tangent circles (equivalently, shared
  sides of faces).

Throughout the documentation and CirclePack's command vocabulary,
**"vertex" and "circle" mean the same thing**. The `-c` display
flag draws circles. The `-v` color-flag colors vertices. Same
objects, different names, historical reasons.

When you see `{v..}` in command syntax, it means "a list of
vertex/circle indices." Similarly `{f..}` for faces and `{e..}` or
`{v1 w1 v2 w2 ...}` for edges.

---

## Flower and petals

The **flower** of a vertex `v` is the cyclic sequence of vertices
adjacent to `v` — i.e., the vertices whose circles are tangent to
`v`'s circle. Each neighbor is called a **petal**.

- For an interior vertex, the flower is a closed cycle; the petals
  go all the way around.
- For a boundary vertex, the flower is an open sequence; it starts
  and ends at boundary vertices.

The **degree** (or combinatorial degree) of a vertex is the number
of petals in its flower.

In a hexagonal packing (like `max_pack` of a disc triangulated
hexagonally), every interior vertex has degree 6. Branch points,
singular vertices, or triangulations with variable combinatorics
have vertices of other degrees.

**Terminology note.** Some CirclePack commands and docs use the
words "neighbor" and "petal" interchangeably. They're the same
thing.

---

## Geometry: hyperbolic, Euclidean, spherical

CirclePack operates in three geometries. The current geometry is
stored as a single integer `hes`:

| `hes` value | Geometry    | Typical use |
|-------------|-------------|-------------|
| `-1`        | Hyperbolic  | Default for simply-connected packings; infinite-type circle packings live in the hyperbolic disc. |
| `0`         | Euclidean   | Finite packings, torus packings, most applied work. |
| `+1`        | Spherical   | Sphere packings, rational-map experiments. |

You switch geometry with `geom_to_h`, `geom_to_e`, `geom_to_s`.

Closely related is `intrinsicGeom` — the geometry *determined by
the combinatorics* (based on boundary components and genus).
`hes` is what's currently in use; `intrinsicGeom` is what's
natural. They usually agree but can diverge when you
deliberately switch (e.g., viewing a hyperbolic packing on the
sphere).

**Why it matters:** many operations are geometry-specific. `repack`
does not currently support spherical solving directly. Schwarzian
layout (`layout -s`) is not supported in the hyperbolic setting.
Some display flags apply only in Euclidean geometry (e.g., the
sector-drawing `disp -a`).

---

## Alpha and gamma: the rooting vertices

A circle packing has a **continuous family** of valid embeddings —
if `(radii, centers)` is a solution, so is any rigid motion
(translation, rotation, reflection, Möbius transformation) of it.
To pin down a unique embedding we need to fix a reference.

CirclePack does this with two distinguished vertices:

- **`alpha`** — the **root** vertex. Its center is placed at the
  **origin**. Alpha is usually an interior vertex.
- **`gamma`** — the **orientation** vertex. The layout rotates the
  whole packing so gamma's center lies on the **positive
  y-axis**.

Together, alpha + gamma fix the embedding up to reflection.

You'll see alpha and gamma referenced in:

- `layout` — normalizes so alpha → 0 and gamma → positive y-axis.
- `max_pack` — places alpha at origin, chooses gamma automatically.
- Several queries and display commands.

To change them: `alpha {v}` sets a new alpha; `gamma {v}` sets a
new gamma. `chooseAlpha` / `chooseGamma` reset them automatically.

---

## Aim and angle sum

The **aim** of an interior vertex is its **target angle sum** — the
value the angles at that vertex should sum to. For a "normal"
interior vertex in a flat packing, the aim is `2π` (one full
turn). For boundary vertices, the aim is `-1`, which means
**unconstrained** — the vertex's angle sum is free.

The **actual angle sum** at a vertex is computed from the current
radii: it's the sum of face angles at that vertex, one per face
containing the vertex.

`repack` iteratively adjusts radii until every vertex's actual
angle sum matches its aim. When they match, we say the packing is
**solved** or **in equilibrium**.

Aims are stored per-vertex and modified with:
- `set_aim {x} {v..}` — set aim to x radians for listed vertices.
- `scale_aims {x} {v..}` — multiply aims by factor x.
- `set_aim_default` (or `layout -a`) — reset to 2π interior,
  -1 boundary.

**Nonstandard aims.** When you set an aim ≠ 2π at an interior
vertex, you're creating a **branch point** (see below). An aim of
4π gives a degree-2 branch point (doubled winding); 6π is
degree-3, and so on. Fractional or negative aims correspond to
exotic singular points studied in generalized branching.

---

## Radii and centers

Each vertex has a stored **radius** and **center** (a complex
number).

- **Radius** is what `repack` computes. It is what determines the
  combinatorial packing at a local level.
- **Center** is what `layout` computes. Given radii and a rooting,
  the embedding walks through `layoutOrder` and places each new
  circle relative to previously-placed neighbors.

Commands that modify radii directly: `set_rad`, `scale_rad`,
`max_pack`, `repack`.

Commands that modify centers directly: `set_center`, `Mobius`
(applies a Möbius transformation), `rotate`, `scale`, `layout`.

**Radii and centers may be out of sync.** If you set a radius and
don't run `layout`, centers still reflect the old configuration.
If you apply `Mobius`, centers change but radii also update
accordingly (a Möbius transformation is a valid symmetry of the
packing). The `disp` command reads centers — so if you don't see
what you expect, `layout` may be stale.

**In the hyperbolic setting,** radii are stored as **x-radii**
(specifically, `x = 1 - exp(-2r)` where `r` is the hyperbolic
radius). This is an internal detail but surfaces in some commands
and file formats.

---

## Tangency packings and overlap packings

A **tangency packing** is one where neighboring circles are
exactly tangent — they meet at a single point. This is the
classical case.

An **overlap packing** is a generalization where neighboring
circles are allowed to overlap (intersect at two points) or to
separate (be disjoint) by prescribed amounts. The amount of
overlap/separation is stored per-edge as an **inversive distance**
or equivalently an **overlap angle**.

When no overlaps are specified, a packing is tangency by default.
When overlaps are set (`set_invdist`, `set_over`), `repack` and
related commands use generalized algorithms.

---

## Inversive distance and overlap

Both terms describe the same per-edge quantity with different
conventions:

**Inversive distance** `ivd` between two circles:

| `ivd` value | Relationship |
|-------------|--------------|
| `ivd = 1`   | Tangent (the standard case). |
| `ivd < 1`   | Circles **intersect** (overlap). `ivd = 0` means orthogonal. |
| `ivd > 1`   | Circles are **disjoint** (separation). |
| `ivd = -1`  | Internally tangent (one inside the other). |

**Overlap angle** `α` in radians: the angle at which the two
circles meet. Tangent = 0, orthogonal = π/2.

CirclePack stores inversive distance. Conversion: if both circles
have radii r₁ and r₂ and their centers are distance d apart, then

```
ivd = (d² - r₁² - r₂²) / (2 r₁ r₂)
```

You rarely do this arithmetic by hand — commands handle it. The
relevant commands: `set_invdist`, `set_over`, `?invdist {v w}`.

**Default** is `ivd = 1` for every edge (tangency). `repack`
honors whatever is set.

---

## Maximal packings

A **maximal packing** is the canonical "round" packing of a given
triangulation:

- **For a simply-connected triangulation** (disc topology): the
  unique packing (up to Möbius transformation) filling the unit
  disc, with boundary circles horocyclic (infinite hyperbolic
  radius).
- **For a torus**: the unique Euclidean packing (up to affine
  maps).
- **For a sphere-like combinatorial structure**: the unique
  spherical packing.

The command `max_pack` computes the maximal packing for the current
combinatorial structure. This is the usual starting point for any
experiment: start from `max_pack`, then modify aims, radii, or
combinatorics from there.

---

## Boundary and interior

A vertex is on the **boundary** if its flower is an open chain —
i.e., it has petals on two "ends" that don't close up. Otherwise
it is **interior**.

A face is a **boundary face** if any of its three vertices is on
the boundary.

The boundary of a packing may have **multiple components** (e.g.,
an annular packing has two boundary circles). The count is stored
as `bdryCompCount`.

Boundary vertices have aim = -1 (unconstrained) by default. Their
radii are typically set by the user or chosen by `max_pack` (which
puts them at "infinity" in the hyperbolic sense — horocyclic).

---

## Branch points

A **branch point** is an interior vertex whose aim is a multiple of
2π greater than 2π. Geometrically, the packing "winds" around
that vertex more than once.

- Aim = 2π: regular interior vertex.
- Aim = 4π: degree-2 branch point (double winding).
- Aim = 6π: degree-3 branch point.

Branch points are the discrete analogue of branch points of
analytic functions. They model ramification: the packing viewed
as a map to the plane branches over the branch-point vertex.

**Generalized branching** (the `|GB|` extender) extends this to
branch points that live in face interstices or use chaperone
circles — allowing fractional or non-integer branch orders.

See `GB_Extension_Guide.md` for the full treatment.

---

## DCEL and `packDCEL`

**DCEL** = *Doubly-Connected Edge List*. It's the combinatorial
data structure CirclePack uses to represent the triangulation.

A DCEL stores:

- **Vertices** (objects, not just indices).
- **Half-edges** — each edge of the triangulation is represented
  as two directed half-edges (one in each direction).
- **Faces** (including the "ideal" or outside faces on the
  boundary).

Each half-edge knows:
- Its origin vertex.
- Its "next" half-edge around the incident face.
- Its "twin" (the opposite half-edge).
- Its incident face.

This structure makes local navigation efficient: given a vertex
you can walk its flower; given a face you can walk its corners;
given a half-edge you can jump to the adjacent face via `twin`.

The `packDCEL` field on a packing holds its DCEL. Most modern
CirclePack code works with `packDCEL` rather than the older
per-array representation.

**Ideal faces** (also called outside faces) represent the
unbounded exterior of boundary components. They have high face
indices (at the end of the face list) and usually shouldn't be
drawn. You can think of them as the "faces" you'd see if you
closed up each boundary component with a single ideal polygon.

---

## Layout order

The **layout order** is a sequence of half-edges that tells the
layout algorithm the order in which to place circles. It's stored
as `packDCEL.layoutOrder` (a `HalfLink`).

Starting from alpha, the algorithm:

1. Places alpha at the origin.
2. Places alpha's first petal along the positive y-axis.
3. For each subsequent half-edge in `layoutOrder`, finds the
   vertex not yet placed at the end of that edge, and computes
   its center from the two already-placed vertices in the face.

The layout order is recomputed by `max_pack`, by `layout -F`, and
by some combinatorial edits. Manual experiments can override it
by passing an explicit `HalfLink` to `layout`.

A well-formed layout order is a **spanning tree of faces** — it
visits every face exactly once, and each face's placement depends
only on earlier faces.

---

## Red chain and side pairings

For a **multiply-connected** packing (surface with nontrivial
genus or multiple boundary components — e.g., an annulus, torus,
or higher-genus surface), the triangulation lifts to a
**fundamental domain** in the universal cover. The **red chain**
is a closed counterclockwise chain of half-edges bounding that
fundamental domain.

The red chain is how CirclePack tracks which edges "close up" the
surface. When you walk around a boundary of the fundamental
domain and meet the red chain, you jump to the paired side.

**Side pairings** partition the red chain into segments that are
identified with each other. For a torus, there are typically 4
red-chain segments paired in two pairs (A-A' and B-B'). For
higher genus you get more.

The Möbius transformations that map a side to its mate form the
**side-pairing maps**, which describe the surface's deck
transformations.

**Holonomy** (below) is computed by composing these maps.

In CirclePack:

- `packDCEL.redChain` holds the red chain.
- `packDCEL.pairLink` holds the side-pairing data.
- `disp -R` displays side pairings (see the `disp` reference).
- `disp -e Ra` displays all red-chain edges.

For simply-connected packings, there is effectively no red chain
(or a trivial one).

---

## Holonomy

**Holonomy** is the composition of side-pairing maps around a
closed loop in the red chain. Geometrically, it measures how far
the packing fails to "close up" — whether the discrete surface is
truly compatible with its intended topology.

A **closed** multiply-connected packing has holonomy equal to the
identity (or very close to it, numerically). A **broken** packing
— one where the radii or combinatorics don't quite match —
produces a non-identity holonomy.

The relevant command is `holonomy_trace`, which reports the trace
of the holonomy Möbius transformation around given faces or the
full red chain. A trace far from 2 (for a Möbius transformation
in SL₂) indicates a non-closing packing.

---

## Schwarzians

The **schwarzian** (from the Schwarzian derivative of classical
complex analysis) is a per-edge quantity computed from the
geometry of a tangency packing. For each interior edge shared
between two faces, there's a unique Möbius transformation mapping
one face to its mirror image across the edge; the schwarzian
measures how much this map differs from the one you'd get in a
uniform hexagonal packing.

Intuitively, schwarzians measure the **discrete conformal
distortion** along each edge.

- In a maximal packing, interior schwarzians are all zero.
- In a deformed or boundary-deformed packing, schwarzians
  indicate where and how much the packing bends.

Schwarzians are the input to an alternative layout algorithm:
`layout -s` uses schwarzians (not radii) to compute centers.
Under suitable conditions, this layout is more numerically stable
and produces higher-fidelity embeddings for subdivision-based
experiments.

Commands that touch schwarzians:

- `set_schwarzian {v w ...}` — compute and store schwarzians.
- `layout -s` — schwarzian-based layout.
- `color -e z` — color edges by schwarzian value.
- `?schwarz {v w}` — query the schwarzian of an edge.
- `set_sch` — convenience resetter used inside `rlsd`.

**Limitation:** schwarzian-based layout is only supported for
Euclidean and spherical geometries.

---

## Tiles and tile data

A **tile** is a combinatorial polygon — a generalization of a face
from 3 vertices to *n* vertices. Tiles group faces into larger
units for subdivision, conformal tiling experiments, and graded
structures.

Each tile has:

- A list of **corner vertices** (a cyclic ordering).
- Optionally, a **baryVert** — a single vertex at its barycenter
  (interior, one per tile).
- Optionally, **augmented vertices** along its edges (edge
  subdivisions).
- A color, an index, a type code.

Tile structures are held in the packing's `tileData` field. There
can additionally be:

- `tileData` — the primary tiles.
- `dualTileData` — the dual tiling (one tile per vertex, with the
  original tile centers as corners).
- `quadTileData` — a "quad" subdivision of the primary tiling.

Commands interact with tile structure via `-T`, `-D`, `-Q` flags
on `color` and `disp`. The full tile machinery is primarily
exposed through the `ConformalTiling` extender.

---

## Barycenters and barycentric coordinates

The **barycenter** of a triangular face is a point computed from
its three corner centers — typically the mean, though in hyperbolic
and spherical geometries the appropriate weighted average.

**Barycentric coordinates** inside a face `{v₁, v₂, v₃}` are a
triple of weights `(b₁, b₂, b₃)` with `b₁ + b₂ + b₃ = 1`,
specifying a point

```
P = b₁·c(v₁) + b₂·c(v₂) + b₃·c(v₃)
```

where `c(v)` is the center of vertex `v`.

Uses in CirclePack:

- `disp -tf {face} {b₁ b₂ b₃}` — draw a trinket at a barycentric
  point.
- `add_bary` — insert new vertices at face barycenters.
- `rm_bary` — remove degree-3 barycenter vertices (the inverse
  operation).
- **BaryLink** — a list type for barycentric-point specifications.
- **BaryCoordLink** — a list of (face, bary-coords) pairs used
  for paths.

"Barycenter" also refers to the optional **`baryVert`** on each
tile — the interior vertex, if any, at the tile's center.

---

## Repacking and the riffle algorithm

"Repack" means: given the current combinatorics, aims, and
(possibly) inversive distances, find radii that satisfy all the
angle-sum constraints.

The basic algorithm is a **riffle**:

1. Pick a vertex.
2. Adjust its radius so the sum of angles at that vertex equals
   its aim (given the current radii of its neighbors).
3. Repeat for every vertex, cycling through the packing.
4. Continue cycling until the total error drops below tolerance
   or the cycle cap is reached.

Each full sweep through all vertices is a "cycle." Most packings
converge in tens to hundreds of cycles.

Modern CirclePack uses several solvers under the hood:

- **HypPacker** (hyperbolic).
- **EuclPacker** (Euclidean, including tori).
- **SphPacker** (spherical, only via `max_pack`).
- **GOpacker** — Orick's linearized method, a faster
  reformulation based on solving a linear system rather than
  cycling. Used when available.
- **Old reliable** — the original per-vertex riffle, slow but
  robust. Invoked with `repack -o`.

See the `repack` command reference for usage and flags.

---

## Glossary / quick reference

| Term | Meaning |
|------|---------|
| **aim** | Target angle sum at a vertex (radians). `-1` means unconstrained (boundary). |
| **alpha** | The root vertex; placed at origin during layout. |
| **angle sum** | Actual sum of face angles at a vertex, from current radii. |
| **baryVert** | Optional barycenter vertex at the center of a tile. |
| **barycentric coordinates** | Weights `(b₁, b₂, b₃)` summing to 1, locating a point inside a face. |
| **boundary vertex** | A vertex with an open (non-cyclic) flower. |
| **branch point** | Interior vertex with aim ≠ 2π. |
| **combinatorial degree** | Number of petals at a vertex. |
| **DCEL** | Doubly-Connected Edge List; the combinatorial data structure. |
| **fundamental domain** | Simply-connected region whose red-chain-bounded copies tile the universal cover. |
| **flower** | Cyclic (interior) or open (boundary) sequence of petals. |
| **gamma** | Orientation vertex; placed on positive y-axis during layout. |
| **hes** | Geometry code: `-1` hyp, `0` eucl, `+1` sph. |
| **holonomy** | Composition of side-pairing maps around a loop; measures "closure error." |
| **ideal face** | Outside/exterior face on a boundary component; high face indices. |
| **intrinsicGeom** | Geometry determined by combinatorics (vs. `hes` which is in-use). |
| **inversive distance** | Per-edge measure of how two circles relate. 1 = tangent, <1 = overlap, >1 = separated. |
| **layout order** | Sequence of half-edges defining circle placement order. |
| **maximal packing** | Canonical "round" packing of a triangulation. |
| **overlap** | Same concept as inversive distance, signed the other way. |
| **packDCEL** | A packing's DCEL field. |
| **PackData** | The full packing record (one per pack slot). |
| **petal** | A neighbor of a vertex in its flower. |
| **radius (x-radius)** | Stored radius value; hyperbolic packings use `x = 1 - exp(-2r)`. |
| **red chain** | Closed CCW half-edge chain bounding the fundamental domain. |
| **riffle** | The per-vertex iterative radius-adjustment algorithm. |
| **schwarzian** | Per-edge conformal-distortion measure used for alternative layout. |
| **side pairing** | Identification of red-chain segments in pairs; with Möbius maps. |
| **tangency packing** | One where neighbors are exactly tangent (ivd = 1 everywhere). |
| **tile** | Combinatorial n-gon grouping faces. |
| **vertex (= circle)** | A single circle; used interchangeably with "circle." |

---

## See also

- **`command_syntax.md`** — how to write CirclePack commands
  (flags, lists, variables, loops).
- **`list_specifiers.md`** — vocabulary for object lists (`a`,
  `b`, `i`, `Iv`, etc.) across all commands.
- **Command references** — `disp`, `layout`, `color`, `repack`,
  and others, each treating these concepts operationally.
- **`GB_Extension_Guide.md`** — the Generalized Branching extender,
  which extends the branch-point concept substantially.

For deeper theory: Ken Stephenson, *Introduction to Circle
Packing: The Theory of Discrete Analytic Functions*, Cambridge
University Press (2005) — the canonical reference for the
mathematics underlying everything on this page.
