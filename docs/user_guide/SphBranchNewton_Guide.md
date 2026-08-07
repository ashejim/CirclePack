# |sn| — Spherical Branched Newton/Perron Extender

`SphBranchNewton` computes **branched circle packings directly on the round
sphere** — something CirclePack's standard machinery cannot do (its `SphPacker`
only builds *maximal* packings by puncturing, packing in the hyperbolic plane,
and projecting; the usual Perron iteration loses monotonicity in positive
curvature).

The extender abbreviation is **`sn`**; commands are issued as `|sn| <cmd>`.

## Geometry and radius convention

Radii are **spherical (angular) radii** internally. When sizes are reported for
discussion we use the convention **radius = the circle's actual 3-D radius on
the unit sphere = sin(angular radius)**, so a **great circle / equator = 1**, a
point = 0. (A branch circle collapses toward 0.)

The packing must be **spherical** (`geom_to_s`) before starting.

## Starting the extender

```
extender sn
```
Start it once per packing. Re-running on the same packing reports
"already has 'sn'"; that just means it is already loaded.

## Command summary

| Command | Purpose |
|---|---|
| `newton [-i maxits] [-t tol] [-l v1 v2 ...]` | Newton only — certify/polish; `-l` holds circles fixed (anchor a basin) |
| `perron [-k K] [-l list] [-n passes]` | Perron develop only (container held fixed) |
| `solve [-k K] [-l list] [-i its] [-t tol]` | Combined: perron then newton (discovery). `discover` is an alias |
| `winding [-L] [-r v1 v2 ...]` | ball-bearing face-chain winding number |
| `residual` | report max \|anglesum − aim\| over interior vertices |
| `bigcircles [-n k] [-l v1 v2 ...]` | fix circles to isolate a solution (uniqueness studies) |

Aims are the usual CirclePack aims: set branch vertices with e.g.
`set_aim 4.0 9 22 63 75` (cone angle 4π) and the rest with `set_aim -d` (2π).

---

## `newton` — certify / polish (Levenberg–Marquardt)

Damped Newton on the spherical angle-sum system `F(r) = anglesum(r) − aim = 0`,
from the current radii. No circles are fixed; the `λI` damping absorbs the
3-dimensional conformal degeneracy (radii are rotation-invariant), so it needs
no gauge-fixing. Best when the current packing is already **near** a solution.

```
# Certify Crane's Blaschke construction directly on the sphere:
act 0;infile_read Bl_double.p;geom_to_s
set_aim -d;set_aim 4.0 9 22 63 75
extender sn
|sn| residual            # ~1e-3 construction slack
|sn| newton              # -> CONVERGED, max|F| ~ 1e-11
|sn| residual            # ~1e-11
```
Flags: `-i` max iterations (default 60), `-t` tolerance (default 1e-10),
`-l v1 v2 ...` **hold those circles fixed** (anchor).

### `-l` anchoring — select/pin a solution's basin

`-l` holds the listed circles at their **current** radii (all residual equations
stay; only the *free* radii move). Because the system is rank-deficient by exactly
**3** (the conformal freedom), holding **3** well-separated circles removes that
degeneracy — the reduced system is full-rank, so Newton converges cleanly and to a
**unique** packing. This both **stabilizes** the solve and lets you **choose which
solution** you land on: `set_rad` a few large circles to a target solution's radii,
then anchor them.

```
# Discover the POLAR packing from the UNBRANCHED max-packing by anchoring 3 big circles:
Cleanse;act 0;infile_read univ_sphere.p;geom_to_s   # unbranched seed (flows to Blaschke on its own)
set_aim -d;set_aim 4.0 9 22 63 75
set_rad 1.488993 39;set_rad 1.337963 1;set_rad 0.902499 6   # 3 largest polar circles (angular radii)
extender sn
|sn| newton -l 39 1 6       # hold them fixed -> converges to the exact polar packing
layout;|sn| winding         # -> 1 (polar)
```

Notes: **3** anchors pin the *exact* packing; **2** reach the right *class* (winding)
but a different Möbius representative; **1** typically stalls. Seeding alone
(`set_rad` then plain `newton`, no `-l`) biases toward the basin but can **stall** on
the flat conformal directions — anchoring is the robust version. For an *unknown*
complex, sweep trial radii on 2–3 big circles (with `-l`) to probe which classes exist.

## `perron` — develop a branched packing (Thurston fixed-boundary)

Fixes a **container** of circles and relaxes the rest by the classic per-vertex
Perron step (set each free radius to the first value whose angle sum crosses its
aim from above). At a branch vertex this actively **collapses** the circle,
developing the cone point — which Newton will not do from a generic seed. The
container both pins the frame and keeps the free circles in Perron's contractive
regime.

The container is, by default, the **K largest circles** (`-k`, default 24), or
an explicit set via `-l` (a `vlist`, an explicit list, or a range).

Because Perron only relaxes the *free* vertices, the fixed container no longer
fits afterwards (a residual remains at the container circles) — follow with
`|sn| newton`, or just use `|sn| solve` (below), which does both.

```
|sn| perron                 # container = 24 largest, default
|sn| perron -k 30           # larger container
set_vlist 10 24 25 26 ...   # choose your own container
|sn| perron -l vlist
|sn| perron -l a(39 62)     # container = a vertex range
```
Flags: `-k` container size, `-l` explicit container, `-n` max passes (default 5000).

## `solve` (alias `discover`) — combined discovery

Runs `perron` (develop) then `newton` (polish) in one command. This **discovers**
a branched packing from a generic seed — e.g. an unbranched maximal packing.

```
# Discover the polar packing (winding 1) from the unbranched maximal packing:
Cleanse;act 0;infile_read univ_sphere.p;geom_to_s   # unbranched max-packing
set_aim -d;set_aim 4.0 9 22 63 75                    # impose branching
extender sn
|sn| solve -k 24            # Perron develops, Newton polishes -> ~1e-11
layout                      # lay out the discovered packing
|sn| winding                # -> 1 (polar)
```
Flags: `-k`,`-l`,`-n` (Perron phase) and `-i`,`-t` (Newton phase).

**Which solution you get is set by the seed's winding class.** A winding-1 seed
(the unbranched max-packing) yields the polar packing (winding 1); a winding-3
seed yields Blaschke (winding 3). Neither Perron nor Newton crosses the integer
winding barrier — that class comes from the doubling/seed.

## `winding` — the ball-bearing face-chain winding number

Reports the Möbius-invariant winding of the **ball-bearing face-chain**: for the
default ring of ball-bearing circles (vertices 77..84), it walks the chain of
**faces** (triangles) that contain the ball bearings and counts how many times
that chain winds about its own axis (right-hand oriented, so the sign is the
packing's orientation, not the layout seed). For Crane's example: **Blaschke = 3,
polar = 1**.

Requires a current **layout** (run `layout` after `newton`/`solve`/`perron`,
since those update radii only). `winding` prints the ring it used.

```
layout
|sn| winding                # uses ball bearings 77..84; prints the ring + value
|sn| winding -L             # relayout first, then compute
|sn| winding -r 77 78 ... 84  # use a different ring
```

## `residual` and `bigcircles`

```
|sn| residual               # max |anglesum - aim| over interior vertices
|sn| bigcircles -n 3        # pick 3 circles to fix (isolate a solution, nullity=3)
|sn| bigcircles -l 24 10 49 # fix an explicit set
```
The spherical radius-only angle-sum system is rank-deficient by exactly **3**
(the conformal freedom), so fixing 3 well-separated circles isolates a solution
for uniqueness studies. `newton`/`solve` do not need this (damping handles it).

---

## Worked example: non-uniqueness (Blaschke vs polar)

```
# Blaschke (winding 3)
act 0;infile_read Bl_double.p;geom_to_s
set_aim -d;set_aim 4.0 9 22 63 75
extender sn
|sn| newton                 # ~1e-11
layout;|sn| winding         # 3

# Polar (winding 1) -- a distinct packing of the SAME complex and aims
act 1;infile_read polar_closed.p;geom_to_s
set_aim -d;set_aim 4.0 9 22 63 75
extender sn
|sn| newton                 # ~1e-11
layout;|sn| winding         # 1
```
Same nerve, same four 4π branch vertices, winding 3 ≠ 1 ⇒ the two packings are
**not** Möbius-equivalent: Crane's degree-3 branched sphere packing is non-unique
(the two cubic rational maps counted by Catalan(2) = 2).

See the driver scripts `construction_alg.cps` (certify both, show winding) and
`discover_alg.cps` (discover polar from the unbranched packing) in the
`branched_sphere_example` project.
