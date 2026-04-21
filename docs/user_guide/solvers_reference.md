# CirclePack Packing Solvers Reference

## About this page

CirclePack has several commands that solve for radii and centers
under various conditions, beyond the fundamental `repack` command.
This page collects them in one place. Each shares DNA with `repack`
but serves a specific niche — maximal packings, perpendicular
packings, random triangulations, polygonal-boundary packings, affine
torus packings, and post-solve smoothing.

Commands covered: `max_pack`, `perp_pack`, `random_pack`, `polypack`,
`torpack`, `smooth`, `fix`, `rld`, `rlsd`.

Not covered (the fundamental): `repack` — see its dedicated page.

---

## Sections

1. [`max_pack` — maximal packing](#max_pack)
2. [`perp_pack` — perpendicular packing](#perp_pack)
3. [`random_pack` — random hyperbolic packing](#random_pack)
4. [`polypack` — polygonal-boundary packing](#polypack)
5. [`torpack` — affine torus packing](#torpack)
6. [`smooth` — post-solve smoothing](#smooth)
7. [`fix` — deprecated alias for `layout`](#fix)
8. [`rld` and `rlsd` — solve-and-display shortcuts](#rld-and-rlsd)
9. [Choosing the right solver](#choosing-the-right-solver)
10. [Source](#source)

---

## `max_pack`

### Overview

Compute the **maximal packing** for the current combinatorial
structure. The maximal packing is the canonical "round" embedding:
for a simply-connected triangulation it fills the unit disc
(hyperbolic); for a torus it is the unique Euclidean affine
packing; for a sphere-like triangulation it's the unique spherical
packing.

`max_pack` is typically the first computation after loading or
generating a triangulation. Unlike `repack` (which takes radii as
input), `max_pack` sets up radii and aims from scratch, then solves.

### Synopsis

```
max_pack                         # up to default cycles
max_pack {k}                     # cap at k iteration cycles
max_pack -v {v}                  # spherical only: puncture at vertex v
max_pack -v {v} {k}              # both: puncture and cycle cap
```

### What it does

Based on the packing's intrinsic geometry:

- **Hyperbolic case** (simply-connected with boundary, or implicit
  hyperbolic): converts to hyperbolic if needed, sets default
  aims (2π interior, −1 boundary), sets boundary radii to 9.0
  (horocyclic), runs `HypPacker.maxPack(cycles)`.
- **Euclidean case** (1-torus only): converts to Euclidean, sets
  default aims, runs `EuclPacker.genericRePack` (Orick's method
  if available), lays out.
- **Spherical case** (sphere-like intrinsic geometry): converts
  to spherical, sets default aims, runs `SphPacker.maxPack(cycles)`.
  Spherical packings must **puncture** a vertex (set to infinity)
  to have a well-defined embedding; `-v {v}` controls which.

### Flags

| Flag        | Meaning |
|-------------|---------|
| *int k*     | Cycle cap (default = `CPBase.RIFFLE_COUNT` = 2000). Clamped to [1, 100000]. |
| `-v {v}`    | **Spherical only**: vertex to puncture at infinity. |

The `-v {v}` flag is ignored (with a warning) for non-spherical
packings.

### Examples

```
max_pack                         # most common: solve to defaults
max_pack 10000                   # allow more iterations
max_pack -v 1                    # sphere: puncture vertex 1
max_pack -v 1 5000               # sphere: puncture 1, 5000 cycles
```

### When to use

- After generating or loading a new triangulation.
- To reset a packing back to a known canonical state.
- As a starting point for deformation experiments.

Once a packing has been modified (aims, inversive distances,
branch points), use `repack` instead — `max_pack` would wipe
your modifications.

### Notes

- `max_pack` is the only way to get a spherical packing solved;
  `repack` does not support spherical directly.
- After `max_pack`, the packing is already laid out — you don't
  need a separate `layout` call.
- A return of 0 cycles usually means the packing was already at
  the maximal configuration.

---

## `perp_pack`

### Overview

Compute a **perpendicular packing**: a Euclidean packing where the
boundary circles are perpendicular to the unit circle. Equivalently,
a packing whose "double across the boundary" is a sphere packing.

This is a specialized algorithm used in studies of circle packings
of planar domains with controlled boundary behavior.

### Synopsis

```
perp_pack                        # default cycles
perp_pack {k}                    # cap at k cycles
```

### What it does

1. Verifies the packing is a topological disc (Euler characteristic
   1, genus 0).
2. Converts to Euclidean.
3. Creates a doubled copy (reflecting across the boundary).
4. `max_pack`s the doubled sphere (with the antipode of alpha as
   the puncture vertex).
5. Projects the result back to the plane.
6. Copies the resulting radii and centers into the current
   packing.

### Flags

| Flag    | Meaning |
|---------|---------|
| *int k* | Cycle cap for the underlying `max_pack`. Default 2000 or 5000 depending on path. Clamped to [1, 100000]. |

### Requirements

- Packing must be a topological disc (one boundary component,
  genus 0). Errors otherwise.

### Examples

```
perp_pack                        # default
perp_pack 10000                  # more cycles
```

### Notes

- Uses `max_pack` internally; the doubled packing is sphere-based
  and relies on the spherical solver.
- Unlike `max_pack` in the hyperbolic case (which sends boundary
  to the unit disc), `perp_pack` gives boundary circles
  perpendicular to the unit circle — a different canonical form.
- The original packing's combinatorics are preserved; only radii
  and centers are overwritten.

---

## `random_pack`

### Overview

Generate a **random hyperbolic packing** — produce a random
triangulation and then `max_pack` it. Useful for experiments
needing a generic example, or for testing algorithms against
varied combinatorics.

### Synopsis

```
random_pack                      # 200 random points, random seed
random_pack {n}                  # n random points (minimum 4)
random_pack -d                   # deterministic seed (seed = 1) for reproducibility
random_pack -d {n}               # both
```

### What it does

1. Generates `n` random points (n ≥ 4; default and minimum enforced).
2. Builds a Delaunay-style random hyperbolic complex from them.
3. Installs the random packing in the current pack slot.
4. Chooses `alpha` far from the boundary (via `gen_mark`).
5. Runs `max_pack 2000`.

Tries up to 12 times if triangulation generation fails. Throws
an exception if all 12 attempts fail.

### Flags

| Flag     | Meaning |
|----------|---------|
| *int n*  | Number of random vertices (min 4, default 200). |
| `-d`     | Use deterministic seed (for debugging / reproducibility). |

### Examples

```
random_pack                      # 200 random points
random_pack 500                  # 500 random points
random_pack -d 100               # reproducible 100-vertex packing
```

### Notes

- **Overwrites the current pack slot** — the previous packing
  is lost. Use `copy` first if you want to preserve it.
- The result is always hyperbolic.
- For deterministic reproducibility in scripts, always use `-d`.

---

## `polypack`

### Overview

Compute a **polygonal-boundary packing**: a Euclidean packing where
the specified boundary circles form the vertices of a specified
polygon. Used for studying boundary-value problems on planar
circle packings.

### Synopsis

```
polypack {v..}                   # use C solver (faster)
polypack -o {v..}                # use Orick's method instead
```

### What it does

1. Verifies the listed vertices are all boundary.
2. Verifies at least 3 boundary vertices are listed.
3. Calls `EuclPacker.polyPack(packData, clink, useC)` to solve.

The listed vertices become the "corners" of the polygon; the
other boundary vertices lie along the polygon's sides.

### Flags

| Flag   | Meaning |
|--------|---------|
| `-o`   | Use Orick's method rather than the default C solver. |
| `{v..}`| List of boundary vertices to use as polygon corners (≥ 3). |

### Examples

```
polypack 1 50 100                # triangle with vertices 1, 50, 100
polypack 1 25 50 75 100          # pentagon
polypack -o 1 50 100             # same triangle, Orick solver
```

### Requirements

- All listed vertices must be boundary.
- Must have at least 3 corners.

### Notes

- The boundary vertices between corners are placed along the
  polygon's sides automatically.
- Useful for Riemann-mapping experiments and for polygonal
  conformal maps.

---

## `torpack`

### Overview

Solve a **torus packing** with specified affine side-pairing
factors. For Euclidean tori (genus 1, no boundary), the packing
is determined up to an affine transformation; `torpack` lets you
specify that transformation through two scaling factors for the
side-pairing maps.

### Synopsis

```
torpack                          # default factors (1.0, 1.0)
torpack {A}                      # A = side-pair factor 1; B = 1.0
torpack {A} {B}                  # both factors
```

### What it does

1. Sets side-pairing factors on the affine projective structure
   via `ProjStruct.affineSet`.
2. Creates a `EuclPacker` and runs `affinePack`.
3. Stores the results as radii.
4. Lays out the packing.

### Flags

| Input   | Meaning |
|---------|---------|
| *A*     | First side-pairing factor (default 1.0). |
| *B*     | Second side-pairing factor (default 1.0). |

### Examples

```
torpack                          # square torus (factors = 1, 1)
torpack 1.5                      # A=1.5, B=1.0
torpack 1.0 2.0                  # rectangular torus
torpack 1.5 0.8                  # general affine torus
```

### Requirements

- Packing must be a topological 1-torus (genus 1, no boundary).
- Silently does nothing sensible if the topology doesn't match.

### Notes

- The torus's modulus τ is determined by the factors A and B
  together with the combinatorics. See the `torus_t` command
  for computing and reporting τ.
- Use `?holonomy_trace` or related queries to verify the
  resulting packing closes up correctly.

---

## `smooth`

### Overview

Create or control a **Smoother**: an iterative post-solve routine
that nudges radii for better visual uniformity or specific
optimization goals. Operates as a stateful object attached to
the packing (`packData.smoother`), so calls build on each other.

`smooth` is not a one-shot solver; it's a control interface for
an ongoing smoothing process.

### Synopsis

```
smooth                           # initialize (if needed)
smooth -a                        # accept the smoother's pending adjustments
smooth -b {x}                    # set radius-pressure balance parameter
smooth -c {n}                    # run n cycles
smooth -d {disp_flags}           # display current smoother state
smooth -r                        # reset smoother
smooth -s {x}                    # set smoother speed
smooth -x                        # kill the smoother
```

### Flag details

| Flag    | Meaning |
|---------|---------|
| `-a`    | Accept current smoother adjustments — commit them to the packing. |
| `-b {x}` | Set radius-pressure balance (a tuning parameter). |
| `-c {n}` | Run n cycles of smoothing. |
| `-d {flags}` | Display using the given disp-flag string. |
| `-r`    | Reset the smoother's state. |
| `-s {x}` | Set smoother speed (an iteration-rate parameter). |
| `-x`    | Destroy the smoother object. |

### Typical workflow

```
max_pack                         # get a baseline solve
smooth                           # initialize the smoother
smooth -c 100                    # run 100 cycles
smooth -d -w -c fc a             # show intermediate state
smooth -c 500                    # more cycles
smooth -a                        # accept the changes
smooth -x                        # clean up
```

### Notes

- The smoother is stateful — you initialize it once, then issue
  multiple commands against it. Use `-x` to dispose when done.
- If smoothing is part of a `MicroGrid` experiment, the smoother
  is initiated there, not here.
- `-b` and `-s` are tuning parameters; their natural ranges
  depend on the specific packing. Start with defaults and adjust.

---

## `fix`

### Overview

**Deprecated.** Alias for `layout`. In current versions of
CirclePack, `fix` is silently rewritten to `layout` before
dispatch. Use `layout` directly.

```
fix                              # same as: layout
```

For details, see the `layout` command reference.

---

## `rld` and `rlsd`

### Overview

Compound commands that run a standard solve-and-display sequence
in one step. These are convenience shortcuts — no new
functionality, just a habit-forming three-step combo.

### `rld` — Repack, Layout, Display

```
rld
```

Runs:

```
repack
layout
disp -wr
```

This is the iteration workhorse for experiments: change
something, `rld`, observe. No arguments.

### `rlsd` — Repack, Schwarzians, Layout, Display

```
rlsd
```

Runs:

```
repack
set_sch
layout -s
disp -wr
```

Schwarzian-based variant. Useful when you're studying schwarzian
deformations and want the schwarzian layout after each solve.

### Requirements

- `rld` works for any geometry.
- `rlsd` errors in the **hyperbolic** setting (schwarzian
  layout is not supported there).

### Examples

```
set_aim 12.566 5                 # create a branch point
rld                              # re-solve and display

set_rad 0.2 i
rlsd                             # schwarzian version
```

---

## Choosing the right solver

| Situation | Use |
|-----------|-----|
| Fresh triangulation, want canonical packing | `max_pack` |
| Existing packing, modified aims or radii, want to resolve | `repack; layout` or `rld` |
| Want a planar packing with boundary perpendicular to unit circle | `perp_pack` |
| Need a random example | `random_pack` |
| Planar packing with polygonal boundary | `polypack` |
| Torus with specific affine modulus | `torpack` |
| After solve, want cosmetic radius smoothing | `smooth` |
| Schwarzian-based iteration cycle (Eucl/Sph) | `rlsd` |
| Standard iteration cycle | `rld` |

---

## Notes and gotchas

- **Spherical packings can only be solved via `max_pack`.** The
  direct `repack` command does not support spherical geometry.
- **`max_pack` recomputes from scratch.** It sets default aims
  and boundary radii; any prior custom aims or branch points are
  lost. Use `repack` to preserve them.
- **`perp_pack`, `polypack`, `random_pack`, and `torpack` have
  topological requirements.** They fail with errors if the
  packing isn't the right type.
- **`random_pack` overwrites the current pack slot.** Use `copy`
  first if the existing packing matters.
- **`smooth` is stateful.** Dispose with `-x` before switching
  contexts; otherwise residual smoother state may persist.
- **`rld` and `rlsd` always redraw.** If you want to iterate
  silently, use the individual commands (`repack; layout`)
  without `disp -wr`.
- **`fix` is deprecated** but remains an alias for `layout` —
  won't throw an error, but use `layout` in new scripts.

---

## Source

- `packing.PackData.repack_call()` — dispatches to
  geometry-specific solvers.
- `packing.HypPacker.maxPack()` — hyperbolic solver
  (`src/packing/HypPacker.java`).
- `packing.EuclPacker.genericRePack()`,
  `EuclPacker.polyPack()`, `EuclPacker.affinePack()` —
  Euclidean solver and its variants.
- `packing.SphPacker.maxPack()` — spherical solver.
- `packing.GOpacker` — Orick's linearized method.
- `combinatorics.komplex.CombDCEL.doubleDCEL()` — used by
  `perp_pack` for the sphere-doubling step.
- `rePack.RandomTriangulation.randomHypKomplex()` — random
  triangulation generation for `random_pack`.
- `projstruct.ProjStruct.affineSet()` — affine structure setup
  for `torpack`.
- `packing.Smoother` — the stateful smoother class used by
  `smooth`.
- `input.CommandStrParser` — top-level dispatch for each of
  these commands.
