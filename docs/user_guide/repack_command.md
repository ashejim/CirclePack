# CirclePack `repack` Command — Solve for Circle Radii

## Overview

`repack` computes the **radii** of a circle packing so that each
vertex's angle sum equals its aim. This is the core numerical
solver of CirclePack: it takes the combinatorial structure of a
packing plus a set of target angle sums ("aims"), and iteratively
adjusts radii until the angle sums match.

Conceptually, `repack` is where the mathematics happens. `layout`
turns the computed radii into a geometric embedding. `disp` shows
the result. So the idiomatic cycle is:

```
repack    # compute radii
layout    # compute centers from radii
disp      # display
```

The shortcut `rld` runs all three.

---

## What `repack` solves

Given:

- A triangulation (vertices, edges, faces).
- For each interior vertex `v`, a target **aim** `a(v)` — usually
  2π for a flat packing, but any positive value is valid.
- For each boundary vertex, either a fixed radius or a
  "free" boundary (aim = −1).

`repack` finds radii `r(v)` such that, for every interior vertex,
the sum of face angles at `v` equals `a(v)`. The algorithm — "riffle"
packing, due to Collins and Stephenson and generalized by later
contributors — iterates over vertices, adjusting each radius
locally until the global angle-sum condition is met within
tolerance.

The geometry (Euclidean, hyperbolic, spherical) is determined by
the packing's stored type (`packData.hes`). `repack` automatically
dispatches to the geometry-appropriate solver.

**Limitation:** `repack` does **not** currently support spherical
packings directly. For those, use `max_pack`, which solves via a
hyperbolic detour.

---

## Synopsis

```
repack                           # default: full repack, default cycles
repack {k}                       # max k iteration cycles
repack -v {v..}                  # repack only the listed vertices
repack -o {k}                    # use "old reliable" algorithm, k cycles
```

With no arguments, `repack` runs the default algorithm for up to
`CPBase.RIFFLE_COUNT` cycles (currently 2000).

---

## What `repack` actually does

1. Checks that the packing is not spherical (if it is, error and
   return).
2. Creates a `RePacker` appropriate for the packing's geometry,
   topology, boundary conditions, and overlap settings.
3. If the native C library (`HeavyC`) is available, uses it for
   speed.
4. Iterates the riffle algorithm up to the specified cycle count
   or until convergence.
5. Returns the number of cycles actually used (0 if no adjustment
   was needed, i.e., the packing was already correct to tolerance).

Possible solvers:

- **HypPacker** for hyperbolic packings.
- **EuclPacker** for Euclidean packings, including tori.
- **SphPacker** for spherical (only reached via `max_pack`, not
  `repack`).
- **GOpacker** (Orick's linearized method) when available — this
  is the fast default path for large packings.
- **Old reliable** (`-o` flag) — a slower but more robust
  per-vertex riffle, used when the modern solvers fail or for
  pathological inputs.

---

## Flags

### `{k}` — cycle count (positional integer)

A bare integer after `repack` is the maximum number of iteration
cycles.

```
repack                           # use default cycles (2000)
repack 500                       # cap at 500 cycles
repack 10000                     # more aggressive
repack 100000                    # hard maximum (higher values clamped)
```

The cycle count is clamped to the range `[1, 100000]`. The default
is `CPBase.RIFFLE_COUNT = 2000`, which is enough for most packings
up to several thousand vertices. For large packings or tight
tolerances, bump it up.

---

### `-v {v..}` — Repack specific vertices only

Instead of a full repack, adjust only the listed vertices' radii
(one pass each).

```
repack -v 5 10 15
repack -v Iv 7                   # neighbors of vertex 7
repack -v b                      # all boundary vertices (rarely useful)
```

The object list follows the standard vocabulary — see **List
Specifiers**.

**When to use:** small local tweaks, or custom iteration schemes
where you want to manually control which vertices to update. For
a full solve, prefer the plain `repack` form.

**Note:** Because only the listed vertices are adjusted, the
result is **not** a globally-solved packing. Angle sums elsewhere
will generally be wrong. This flag is a tool for experimentation,
not a substitute for a full repack.

---

### `-o {k}` — Old-reliable algorithm

Use the original per-vertex riffle method instead of the modern
solvers. Slower but sometimes more robust on degenerate or
difficult packings.

```
repack -o 5000                   # old-reliable for up to 5000 cycles
```

The `{k}` count is the maximum number of cycles; same clamp rules
as the default form.

**When to use:**
- Modern solvers report failure or non-convergence.
- You suspect numerical issues with the GOpacker / linearized
  methods on your specific packing.
- Reproducibility with older CirclePack versions.

For routine work, the default solver (no `-o`) is faster and
adequate.

---

## Prerequisites and preconditions

- **Valid combinatorics.** The packing must have a consistent DCEL
  structure. If you've recently modified topology (`add_cir`,
  `rm_cir`, `split_edge`, etc.), run `layout -K` or `layout -F`
  first to refresh the structure.
- **Aims set.** Every interior vertex needs an aim. The default
  (`set_aim_default` or `layout -a`) sets interior aims to 2π and
  boundary aims to −1 (free). Use `set_aim` to override
  individuals for branch points, boundary conditions, etc.
- **Initial radii.** Any positive radii work as starting values,
  but closer-to-solution starting radii converge faster. The
  hyperbolic solver is tolerant; the Euclidean one can be
  sensitive to very poor initial values.

---

## Typical workflow

```
# Standard full solve
max_pack                          # build a maximal packing (radii + layout)
repack                            # re-solve (should be a no-op; 0 cycles)
layout                            # re-lay out
disp -w -c a

# Modified aims: create a branch point
set_aim 12.566 5                  # 4π at vertex 5 (double branch)
repack                            # solve for radii that match the new aim
layout                            # embed
disp -wr                          # show

# Quick iteration during experimentation
rld                               # repack + layout + disp -wr

# Recover from "stuck" state
repack -o 10000                   # old-reliable with extra cycles

# Local repack for a tweaked region
set_rad 0.15 7                    # manually bump vertex 7's radius
repack -v Iv 7                    # adjust vertex 7's immediate neighbors
layout
disp -wr

# Use schwarzian-based layout after repack
rlsd                              # repack + set_sch + layout -s + disp -wr
```

---

## Tips & common patterns

**`repack` doesn't touch centers.** It changes radii only. The
centers (embedding) reflect the previous solution until you call
`layout`. This is why `layout` always follows `repack` in normal
workflows.

**Return value = cycles used.** A return of 0 means "already
solved to tolerance" — not a failure, just a successful
no-op. Return values near your cycle cap suggest either a
near-convergent but slow problem or non-convergence.

**Iteration caps are safety rails, not targets.** For a
well-conditioned packing, `repack` typically converges in tens to
hundreds of cycles, well under the default 2000. Hitting the cap
is a sign something is off — check aims, check combinatorics,
consider `-o`.

**Max_pack versus repack.** `max_pack` computes a maximal packing
from scratch (setting defaults and solving). Use it after changing
combinatorics. Use `repack` after changing aims or radii of an
already-good packing.

**Torus packings are special.** Euclidean tori have no boundary
but also no natural "root"; `repack` handles them via a specialized
Euclidean solver. Expect slower convergence and sometimes
sensitivity to the torus's modulus.

**Inversive distance packings.** If you've set non-trivial
inversive distances (`set_invdist`), `repack` uses a modified
algorithm. Convergence may be slower and the solution may not be a
true circle packing in the tangency sense.

---

## Relationship to other commands

- **`layout`** consumes the radii that `repack` produces. Nearly
  always run right after `repack`.
- **`max_pack`** builds a maximal packing from scratch. Essentially
  `set_aim_default; set_rad ... ; repack; layout` with the correct
  setup for the packing's topology and geometry.
- **`rld`** = `repack; layout; disp -wr`. The standard three-step
  shortcut.
- **`rlsd`** = `repack; set_sch; layout -s; disp -wr`. Same but
  using schwarzians for layout (Euclidean/spherical only).
- **`set_aim`** modifies the targets that `repack` tries to achieve.
- **`set_rad`** modifies the starting radii. `repack` then adjusts
  them.
- **`set_invdist`** sets inversive distances; `repack` then solves
  the generalized packing problem.
- **`test_repack`** runs diagnostic variants of `repack`.
- **`quality`** reports per-vertex errors in the current solution;
  useful to verify a `repack` converged.

---

## Notes & gotchas

- **Spherical packings are not supported by `repack`.** The command
  errors out with "no spherical algorithm yet exists." Use
  `max_pack` instead; for sphere packings it routes through a
  hyperbolic solver.
- **Cycle cap is [1, 100000].** Requests below 1 become 1; above
  100000 become 100000. The default is 2000.
- **A zero return does not mean failure.** It means "no repacking
  was needed" — the packing was already within tolerance. For
  freshly-made packings this happens routinely after `max_pack`.
- **`-v {v..}` does not produce a globally solved packing.** It
  locally adjusts the listed vertices only. Use it for
  experiments, not for a finished solve.
- **Overlap packings may need `-o`.** With non-trivial overlaps
  (inversive distances), the GOpacker sometimes fails to
  converge; falling back to old-reliable is a routine workaround.
- **Repack honors geometry.** If the packing is hyperbolic, the
  hyperbolic solver is used; Euclidean gets the Euclidean one.
  You do not need to (and usually should not) switch geometries
  around a `repack` call. Use `geom_to_h`, `geom_to_e`, or
  `geom_to_s` when you *intentionally* want to change geometries.
- **After radii change, centers are stale.** Always follow `repack`
  with `layout` before displaying or writing output.

---

## Source

- `packing.PackData.repack_call()` — the entry-point;
  dispatches to the appropriate solver based on geometry, topology,
  and flags (`src/packing/PackData.java`).
- `packing.HypPacker.maxPack()` and related hyperbolic solver
  methods.
- `packing.EuclPacker.genericRePack()` and related Euclidean
  solver methods.
- `packing.RePacker` — the common interface.
- `packing.GOpacker` — Orick's linearized algorithm, fast path
  for many cases.
- `packing.PackData.h_riffle_vert()` and
  `packing.PackData.e_riffle_vert()` — per-vertex riffle routines
  used by `-v`.
- `allMains.CPBase.RIFFLE_COUNT` — default cycle count constant
  (value: 2000).
- `input.CommandStrParser` — the top-level `repack` case dispatch.
