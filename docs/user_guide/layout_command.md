# CirclePack `layout` Command — Compute Circle Centers

## Overview

`layout` computes the **centers** of the circles in a packing, given
their **radii** and the combinatorial structure. In circle-packing
terminology, `repack` sets up radii that satisfy the angle-sum
conditions, and `layout` turns those radii into an actual geometric
embedding — a concrete position in the plane, disk, or sphere for
every circle.

The algorithm walks the packing in a **layout order** (a spanning
sequence of faces), placing each new circle relative to circles
already laid out. The result depends on:

- The **radii** (usually from `repack`).
- The **layout order** (stored as a `HalfLink` in `packData.packDCEL.layoutOrder`).
- Two special vertices: `alpha` (placed at the origin) and `gamma`
  (rotated to the positive y-axis). These fix an otherwise-arbitrary
  rigid motion.

A typical workflow is `repack; layout; disp`. The `rld` shortcut
does all three in one. You rarely need to think about `layout`'s
internals, but when you do (debugging, partial re-layouts, switching
to schwarzian-based layout), the flags below give you fine control.

---

## Synopsis

```
layout                                    # default: lay out the whole packing
layout [hlink]                            # lay out following the given HalfLink
layout -s                                 # lay out using schwarzians (not radii)
layout -[a|c|d|dt|e|F|K|r|s|t|T|v|x] args # various flag-driven variants
```

With no flags, `layout` uses the stored `layoutOrder` and radii to
place every circle. This is the form you'll use 95% of the time.

---

## What `layout` actually does

The default call is equivalent to:

1. Normalize: place `alpha` at the origin with the first face in a
   canonical position.
2. Walk `layoutOrder` (a sequence of half-edges); for each half-edge
   whose face isn't yet placed, compute the new corner's center from
   the two already-placed corners and its radius.
3. Rotate the entire result so that `gamma` lies on the positive
   y-axis.

Along the way, side-pairing maps (for multiply-connected packings)
are updated. Some variants recompute `layoutOrder` itself, or
recompute the underlying DCEL combinatorics first.

**Prerequisites.** `layout` expects:

- Valid radii for every vertex. Usually from `repack`, but could be
  from `set_rad`, `max_pack`, or a file.
- A valid combinatorial structure (DCEL). If the combinatorics are
  stale or broken, use `-F` or `-K` to refresh them first.
- For schwarzian layout (`-s`), a tangency packing in Euclidean or
  spherical geometry — hyperbolic schwarzian layout is not
  supported.

**What `layout` does NOT do.** It does not:

- Change radii. That's `repack`.
- Change colors, marks, or display state. That's `color`, `mark`,
  and the `disp` family.
- Draw anything. You still need `disp` to see the result.

---

## Flags

### `-a` — Default aims

Set aim values to their defaults before laying out: `2π` for interior
vertices, `-1` (unconstrained) for boundary vertices. Useful after
editing combinatorics or when aims have been scribbled over by an
experiment.

```
layout -a
```

This *only* sets aims; it does not recompute radii or centers. To
follow through, pair with `repack` and another `layout`:

```
layout -a; repack; layout
```

---

### `-c` — Compute centers

Recompute centers using the current layout order. Equivalent to the
bare `layout` call, but exists as an explicit flag so it can be
combined with `-s`:

```
layout -c                                # same as: layout
layout -s -c                             # lay out using schwarzians
```

---

### `-d [v]` — Layout by drawing order, report on vertex v

Lay out in the current drawing order and, along the way, report the
computed location of vertex `v` each time it is placed. Because a
vertex may appear in multiple faces, this gives you insight into
placement consistency — if `v` comes out at slightly different
positions each time, the packing has a mismatch.

```
layout -d 5                              # lay out, report vertex 5's locations
```

This reports to the message panel and also normalizes the result.

---

### `-dt [v]` — Torus 2-side-pair layout

Special-case layout for a torus (genus 1, no boundary). Attempts a
2-side-pairing arrangement suitable for visualizing the torus as a
parallelogram. Does nothing if the packing isn't a torus.

```
layout -dt
```

---

### `-e {v..}` — Layout with a given poison edge set

Use the given vertex list to define a new red chain (the set of
"poison edges" separating fundamental domain copies in multiply-
connected packings), then lay out. This is primarily for advanced
use in surface packings.

```
layout -e 5 8 12 20 15 10
```

---

### `-F` — Full redo

Force a complete recombinatorial pass, then lay out. Use this when
the packing's combinatorial structure may have been disturbed —
after adding/removing vertices, after a manual edit — and you want
everything regenerated from scratch.

```
layout -F
```

The `-F` variant also resets aims to defaults and refills the
curvature table. This is the heaviest form of layout.

---

### `-K` — Redo combinatorics only

Rebuild the DCEL (doubly-connected edge list) combinatorial structure
without necessarily redoing the layout. Useful after manual
topological surgery.

```
layout -K
```

Usually followed by a plain `layout` to draw the result.

---

### `-r {f..}` — Recompute along face list

Recompute centers along the specified face list (see **List
Specifiers**) without laying out the whole packing. This is useful
for localized updates — you've changed a few radii and want to
propagate their effect only through a specific region, not redo
the entire embedding.

```
layout -r 10 11 12 13 14
layout -r a                              # all faces (equivalent to layout)
```

**Note:** Partial re-layouts can produce visually inconsistent
results if the edited region and the un-edited region disagree at
their shared boundary. Consider a full `layout` when in doubt.

---

### `-s` — Use schwarzians

Compute the layout using **schwarzian derivatives** rather than
radii. This is an alternative algorithm appropriate for tangency
packings in Euclidean or spherical geometry. When the schwarzians
have been set (e.g., from `set_schwarzian`), this can produce a
more accurate embedding than the radius-based method.

```
layout -s                                # schwarzian-based layout
```

The `-s` flag also works as a **leading modifier** — if it appears
as the first flag segment, later flags in the same `layout` call
will use schwarzians:

```
layout -s -c                             # schwarzian layout, compute centers
layout -s -r 1 2 3 4                     # schwarzian-based partial re-layout
```

**Requires:** Euclidean or spherical geometry. Attempting schwarzian
layout in hyperbolic mode gives an error and returns 0.

---

### `-T` — Respect top-level tiles

For packings that are part of a conformal-tiling experiment (with
the `ConformalTiling` extender active), this variant rebuilds the
red chain so it respects the top-level tile boundaries, then lays
out. Only meaningful with tiled packings; no-op otherwise.

```
layout -T
```

---

### `-t` — Tailored drawing order

Construct a drawing order that avoids using vertices with mark set
(to the extent possible), then lay out. This lets you control which
vertices the layout algorithm uses as "anchors" — a subtle but
powerful tool for working around problematic regions.

```
# First mark the vertices to avoid
mark 3 7 12 15
layout -t
```

If no vertices are marked when `-t` is invoked, the command reports
an error and does nothing.

The uppercase `-T` variant above is unrelated to this.

There is also a related flag that forces the tailored order to
strictly exclude marked vertices (rather than merely avoid them):
the internal `tflag` is set, resulting in the algorithm stopping
early if it cannot proceed without using a marked vertex.

---

### Positional `HalfLink` argument (no dash)

As a special form, a non-flag first argument is parsed as a
**HalfLink** (a sequence of half-edges; see **List Specifiers**) and
used directly as the layout order. This lets you lay out along a
custom path without modifying the stored `layoutOrder`:

```
layout L                                 # explicitly use layoutOrder
layout a                                 # use all edges
layout e 1 2 2 3 3 4                     # custom edge sequence
```

Use with care — the layout algorithm assumes the sequence connects
the packing into a walkable tree. An arbitrary edge list may produce
unexpected results.

---

## Typical workflow

```
# Start from scratch: max-pack then lay out
max_pack
repack
layout
disp -w -c a -e a

# After changing an aim
set_aim 2.5 5                  # give vertex 5 aim = 2.5 radians
repack                         # adjust radii to the new aim
layout                         # recompute the embedding
disp -wr                       # redraw using the stored display state

# The idiomatic shortcut
rld                            # repack + layout + disp -wr

# Switch to schwarzian-based layout
layout -s
disp -wr

# Partial re-layout after editing a small region
set_rad 0.2 5                  # bump vertex 5's radius
layout -r Iv 5                 # re-layout just the faces around vertex 5
disp -wr

# Recover from a stale combinatorial state
layout -F                      # full refresh: combinatorics, aims, layout
disp -wr
```

---

## Tips & common patterns

**Layout is idempotent for an unchanged packing.** Running `layout`
twice in a row produces the same result the second time as the
first, assuming nothing else has changed. Use this for sanity
checks.

**If `disp` shows nothing or garbage, `layout` may be stale.** A
common failure mode: you've run `repack` but forgot `layout`. The
radii are correct but the centers still reflect the old
configuration. Run `layout` and re-display.

**Custom layout orders are powerful but risky.** The positional
`HalfLink` argument lets you force an order, but the algorithm
assumes the order is tree-like and connected. Non-tree orders can
produce double-placed circles.

**Schwarzian layout is not a drop-in replacement.** `-s` uses
different data and requires that schwarzians be set (not just
radii). It is most useful when you've been computing with
`set_schwarzian` and want to see the result visually.

**Multiply-connected packings need the red chain.** Tori, annuli,
and higher-genus surfaces rely on `packDCEL.redChain` and
side-pairings. `-F` and `-K` rebuild these; `-e` lets you
hand-specify them.

---

## Relationship to other commands

- **`repack`** produces the radii that `layout` consumes. The pair
  is the fundamental "solve, then draw" sequence of CirclePack.
- **`disp`** reads the centers that `layout` writes. Display
  commands after `layout` show the new embedding.
- **`rld`** / **`rlsd`** are shortcuts: `rld` = `repack; layout;
  disp -wr`.
- **`max_pack`** produces a maximal (Beltrami-Laplace) packing,
  setting radii and calling `layout` as part of its own process.
  After `max_pack` the packing is already laid out — you don't
  need to follow up with `layout` unless you change something.
- **`set_schwarzian`** sets the data that `layout -s` consumes.
- **`set_aim`**, **`set_rad`**, **`set_center`** modify the inputs
  to layout; after using them, re-run `layout` to propagate changes.

---

## Notes & gotchas

- The **bare `layout` call** is the right call the vast majority of
  the time. Flags exist for specific scenarios.
- **Schwarzian layout is not supported in hyperbolic geometry.**
  `layout -s` in a hyperbolic packing errors out.
- **`-F` is the heaviest form.** It rebuilds combinatorics, resets
  aims, and lays out. Only use it when you suspect the structure
  is genuinely broken.
- **`-r {f..}`'s partial-layout results can visibly disagree with
  the rest of the packing** along the boundary of the re-laid-out
  region. For publication-quality output, prefer a full `layout`.
- **`-t` requires vertices be marked beforehand.** Use the `mark`
  command first.
- **After topological surgery (`split_edge`, `add_cir`, `rm_cir`,
  etc.), you almost always need `-F` or `-K`.** The combinatorics
  need to catch up before centers can be recomputed meaningfully.
- **Layout changes stored centers.** If you need to preserve the
  current centers, copy the packing (`copy 0 1`) before laying out.

---

## Source

- `dcel.PackDCEL.layoutPacking()` (several overloads) —
  the core algorithm.
- `dcel.PackDCEL.layoutFactory()` — the face-walking
  engine used by `disp -C`, `disp -F`, `disp -B`.
- `dcel.PackDCEL.layoutOrder` — the stored `HalfLink` that
  defines the walk.
- `workshops.LayoutShop.layoutFaceList()` — partial layout
  along a face list (used by `-r`).
- `workshops.LayoutShop.layoutPolygon()` — polygon layout
  used for shape display.
- `input.CommandStrParser` — the `layout` case dispatch
  (look for `cmd.startsWith("layout")`).
- Alpha and gamma vertex accessors: `packing.PackData.getAlpha()`,
  `getGamma()`, `setAlpha()`, `setGamma()`.
