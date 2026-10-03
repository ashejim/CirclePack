# |sz| — SchwarzianSolve Extender

`SchwarzianSolve` computes spherical circle packings **directly in
intrinsic-schwarzian coordinates**. The unknowns are not radii but one real
number per interior edge — Stephenson's intrinsic schwarzian $s_e$ — and the
equations are the per-flower **closing conditions**: the monodromy of each
flower's development must be scalar.

Why bother? On the sphere, the radius-coordinate system is hostile: the angle
sum $\theta_v(r_v)$ is U-shaped (it *folds*), radii are Möbius-variant, and
monotonicity — the engine of the classical Perron method — fails. In
schwarzian coordinates the closing condition is **affine in each variable**
(fold-free), the unknowns are Möbius-invariant (no gauge freedom at all), and
the Jacobian is computed **exactly** by unit probes, no finite differencing.
The result: a damped-Newton solve that converges in a handful of iterations
from a seed built out of **pure combinatorics** — the vertex degrees.

The extender abbreviation is **`sz`**; commands are issued as `|sz| <cmd>`.
The packing should be **spherical** (`geom_to_s`). Compare `|sn|`
(SphBranchNewton), which solves the same problems in radius coordinates.

## Starting the extender

```
extender sz
```

## Command summary

| Command | Purpose |
|---|---|
| `seed [-d\|-c\|-z] [-p sigma]` | set the edge schwarzians: `-d` from vertex degrees (pure combinatorics), `-c` from the current geometry (via `set_sch`), `-z` identically zero (the blind seed — no information at all); `-p` adds Gaussian noise |
| `residual [-n] [-a best\|worst\|mean] [-r] [-t [K]]` | flower-closing residual; reports the worst flower. With no flags: the defect from **every** base petal, worst per flower (base-petal free), followed by the native-base number for reference. `-n`: native base only (the pre-October-2026 default; what plain `solve` minimizes). `-a`, `-r`, `-t`: see [`residual`](#residual--what-is-being-solved) below. Flags combine |
| `solve [-i maxits] [-t tol] [-a] [-b v1 v2 ... \| -w]` | Levenberg–Marquardt on the closing system; continues from the current internal state, so repeated small `-i` calls step the solver visibly. `-a` minimizes the defect from **every** base petal instead of the native one (base-petal-free objective; about deg × the rows, same Jacobian size). `-b v..` uses the **signed** all-bases objective: every base's product is driven to $-I$, or to $+I$ at the listed $4\pi$ branch vertices. `-w` does the same with the wraps read from the packing aims, as `bsolve`. Either implies `-a` |
| `layout` | store the current schwarzians on the edges and develop circles via the intrinsic Möbius-frame development (exact per-edge transfer products; reports the holonomy spread, which is near zero iff the schwarzians close up) |
| `check` | compare the solver's schwarzians against those recomputed from the current geometry |
| `cont` | per-flower continuant minima; a negative value flags a branched flower |
| `budget [-a] [-b v1 v2 ...]` | report the per-flower angular budgets $T_v$: the budget residual $\beta_v = T_v - \pi w_v$ and the predicted branch set (wraps $= \mathrm{round}(T_v/\pi)$). `-a`: budget averaged over all base petals |
| `bsolve [-i maxits] [-t tol] [-a] [-b v1 v2 ...]` | **stage-1 budget-Newton**: drive every $T_v$ to its aim $\pi w_v$, selecting the solution *basin* before `solve` closes the flowers; `-b` lists the prescribed branch vertices ($w=2$), otherwise aims are read off the packing (`set_aim 4*pi v ...` as for `\|sn\|`). `-a`: use the budget **averaged over all base petals**, $\bar T_v = \frac1n\sum_k T_v^{(k)}$ (base-petal free) |
| `tsolve [-i maxits] [-t tol] [-a] [-b v1 v2 ...]` | **tail-coordinate solver**: LM on the exact three closing conditions per flower — budget $\beta_v$, stretch $\Lambda_v - 1$, shear $\mu_v$ — with the wrap vector as input (aims/`-b`, as `bsolve`). `-a` stacks **every base petal's own** triple $(\beta^{(k)}, \Lambda^{(k)}-1, \mu^{(k)})$ (base-petal free). `bsolve -a` followed by `tsolve -a` is the best recipe found (October 2026) |
| `newton`, `perron`, `discover`, `bigcircles`, `winding`, `windfaces`, `basin1` | **radius chapter**: the full SphBranchNewton solver, embedded — these forward to a private `\|sn\|` instance on the same packing (see below) |
| `rsolve`, `rresidual` | radius chapter, renamed to avoid collision: `rsolve` = `\|sn\| solve` (Perron develop + Newton polish), `rresidual` = max \|anglesum − aim\| |

## The degree seed — solving from combinatorics alone

`seed -d` sets every edge to $s_e = \tfrac12\,(s_{n(v)} + s_{n(w)})$, where
$n(v), n(w)$ are the endpoint degrees and $s_n = 1 -
\tfrac{2}{\sqrt3}\cos\tfrac{\pi}{n}$ is the uniform $n$-flower value. No
radii, no centers, no stored geometry — the seed is read off the abstract
complex. On the 84-vertex Crane complex, `solve` reaches the maximal packing
from this seed in about 8–12 iterations.

## `solve` in steps — watching the packing form

`solve` keeps its state between calls: `|sz| solve -i 1` performs one damped
Newton step and stores the updated schwarzians on the edges, so

```
|sz| solve -i 1;|sz| residual;|sz| layout;disp -wr
```

is one *visible* step of the algorithm: solve a little, develop, redraw.
Repeating it shows the initial tangle annealing into the packing as the
residual drops. The script `sz_univalent.cps` (in the
`branched_sphere_example` project) is built around exactly this loop:

1. load the complex and **destroy the geometry** (`set_rad .25 a`) — display
   the resulting mess on the sphere;
2. `|sz| seed -d` — seed from degrees only; `|sz| layout` displays what the
   combinatorial seed looks like (still a mess, but a principled one);
3. step the solver (`solve -i 1`, then `-i 2`, `-i 4`, ...), redeveloping and
   redisplaying after each step;
4. finish with `|sz| check` (solved schwarzians vs the developed geometry —
   should agree to ~1e-6) and `|sz| cont` (all continuants positive =
   univalent regime).

## `residual` — what is being solved

For each flower the extender multiplies out the monodromy
$P_v = \prod (M_{s_e}^{-1} D)$ over the spokes ($M_s$ is the standard edge
Möbius `Mobius.stdBaseMobius`; $D$ is the universal base advance, an exact
rotation by $\pi/3$ of the base center-circle) and measures how far $P_v$ is
from scalar. Univalent target: every flower scalar with all continuants
positive. Typical values on the Crane complex: degree seed ≈ 24; solution
≈ 1e-6 (the precision of the stored packing).

### Options: measuring the error without a base petal

The product $P_v$ has to start at some spoke: the default uses the flower's
native base half-edge. Starting elsewhere conjugates $P_v$, so whether $P_v$
is scalar does **not** depend on that choice, but how far from scalar it
looks off-solution **does**. On a perturbed 84-vertex field the per-flower
value typically changes by a factor of ~3 between base petals, and by 30× or
more at some flowers. Three options remove the dependence:

| flag | measures | base petal |
|---|---|---|
| *(none)* | **default**: `-a worst` (below), followed by the native-base number for reference | free (first line) |
| `-n` | 6-real defect of $P_v$ from the native base only: $\|R\|_2$ and the worst flower. This was the default before October 2026, and it is what plain `solve` drives to zero | dependent |
| `-a best\|worst\|mean` | the same defect computed from **every** base petal of each flower, then the best, worst or mean of these per flower; also reports the largest worst/best ratio at one flower. `-a` alone means `worst` | free |
| `-r` | the **relation residual**: for every centre spoke $j$ of every flower, the three forced-label relations of [St] Thm 4.5 in signed form, $\sqrt3u_j + \varepsilon\kappa = 0$ and $\sqrt3\kappa u_{j\pm1} - (\mathcal K(\text{run}\setminus\text{nearest}) - \varepsilon) = 0$, where $\kappa = \mathcal K(\text{far run } j{+}2..j{-}2)$. $\varepsilon = -1$ (closes to $-I$) or $+1$ (closes to $+I$, a $4\pi$ branch flower) is chosen per flower to fit best, so like the default it is sign-free; the flowers fitting $+I$ are listed | free (all rotations used) |
| `-t [K]` | the **rotation defect** $\arccos(\min(1, \lvert\operatorname{tr} M_v\rvert/2))$, with $M_v = B(u_1)\cdots B(u_n)$: the angle from the flower's projective rotation to the nearest multiple of $\pi$, i.e. the rotation-number defect. Also reports the wraps $\ge 2$, from a Sturm sign-change count over $K$ trips around each flower (default $K = 256$) | free (exactly: the trace is invariant under cyclic shifts) |

Notes:

- `-t` measures **only the rotation** condition. It is $0$ whenever $M_v$ is
  hyperbolic or parabolic, so it does not see stretch or shear: a flower can
  have rotation defect $0$ and still be open. Near closure it behaves like the
  *square root* of the trace error, so an exactly closed flower reads about
  $10^{-8}$, not $10^{-16}$.
- `-r` is the residual of the Lemma-1 relations used by the Kaczmarz
  relaxation; its zero set is the same as the default's (all flowers closed),
  but its size is measured symmetrically over all rotations.
- At a genuine solution every option reports (near) zero; they differ in how
  they rank *non*-solutions, which matters for seeds, stalls and stopping
  rules.
- The budget $T_v$ (`budget`) is **not** base-free off-solution either:
  "$T_v = \pi$" says that $P_v$ fixes the *base* petal's direction.

```
|sz| residual                 (default: all base petals, worst; + native)
|sz| residual -n              (classical, native base only)
|sz| residual -a mean -r -t   (all three base-free measures at once)
|sz| solve -a                 (LM on the all-base-petals objective)
```

### Recommended recipe (October 2026): fully base-petal-free

```
|sz| seed -d                        (or -z, or anything sane)
|sz| bsolve -a -b v1 v2 ...         (averaged budget; omit -b for univalent)
|sz| tsolve -a -b v1 v2 ...         (all-bases tail solver: stretch + shear together)
|sz| residual;|sz| cont;|sz| layout
```

`solve -b v1 v2 ...` (the signed all-bases closing residual; `-w` reads wraps
from the aims) is the alternative second stage. On the 84-vertex Crane
complex (Python mirror, 12 seeds × {univalent, DEG9}, exact target hits):

| second stage after `bsolve -a` | hits |
|---|---|
| `solve -b` | 21/24 |
| **`tsolve -a -b`** | **23/24** (never worse on any seed) |
| `tsolve -b` (pinned, no stage 1) | 13/24 |

The same comparison on four other spheres (geodesic icosahedra with 42 and 92
vertices; hex-disc complexes capped by one vertex of degree 18 or 24, the
second with irregular degrees 4–8), targeting the univalent packing with 12
seeds each:
- previous production `bsolve` → `solve`: **26/48**;
- `bsolve -a` → `solve -w`: 42/48;
- `bsolve -a` → `tsolve -a`: **45/48**.

The gap is largest on the complex with a degree-24 vertex: 1/12 for the old
recipe, 9/12 for the new one.

These counts are after the exact-bite fix to the budget (commit `60905485`).
Before it, on very stretched flowers a bite of about 1e-20 could round to π,
and the counts read 24/36/47. The fix computes each bite as
atan2(1, V_{k-1}·V_k), which is exact because consecutive contact vectors
have cross product 1. The 84-vertex counts above are unchanged.

**Why the averaged budget is safe.** Each $T_v^{(k)}$ is nondecreasing in
every spoke (Lemma 0), so the average is too. The budget Jacobian keeps the
structure of Theorem C (positive off-diagonal entries whose row sums equal
the diagonal), so budget-Newton stays well posed. At a closed flower every
$T_v^{(k)} = \pi w_v$, so the targets are unchanged.

On the 84-vertex Crane complex (Python mirror of these commands, 12 seeds,
counting only exact hits on the *prescribed* packing; targets univalent and
DEG9):

| recipe | → univalent | → DEG9 |
|---|---|---|
| `solve` (native, sign-free) | 2/12 | 0/12 |
| `bsolve` → `solve` (previous production) | 7/12 | 7/12 |
| `bsolve` → `solve -b` | 8/12 | 9/12 |
| **`bsolve -a` → `solve -b`** | **10/12** | **11/12** |
| `bsolve -a` → `solve` (mixed) | 6/12 | 6/12 |

Both stages need to be base-free. The mixed combination is worse than
either consistent one.

**Why the default changed (October 2026).** At stalls of plain `solve`, the
native base is typically the best-looking base: measured from all bases the
defect was 8–70× larger on the 84-vertex Crane complex, and LM had been
lowering its own number while the all-bases defect rose. An all-bases
objective (`solve -a`) converged from 11 of 21 test seeds against 7 for the
native objective, including the blind zero seed, which it took to DEG9
without `bsolve`. Like plain `solve` it is sign-free, so it can also converge
to branched closing solutions that still need certifying (`cont`, `layout`).
Details: `RESIDUAL_OPTIONS.md` in the `branched_sphere_example` project.

## `bsolve` — choosing the branch structure (the budget stage)

The closing system itself is aim-agnostic: which solution `solve` finds is
governed by the seed's basin. The **budget stage** makes the basin a
*choice*. Around each flower the spoke transfer recursion advances a
projective tangency direction by a positive bite per spoke; the total
advance — the **angular budget** $T_v$ — equals $\pi \cdot \mathrm{wrap}$ at
any closed flower ($\pi$ unbranched, $2\pi$ at a 4π branch flower). $T_v$ is
the projective shadow of the radius-coordinate angle sum, and unlike the
angle sum it is **monotone** in every own-spoke schwarzian, with a provably
nonsingular Newton system (Theorem C, `BUDGET_FLOW_PROOF.md`).

`bsolve` runs a damped Newton on the $V$-dimensional system
$\beta_v = T_v - \pi w_v = 0$: solve $M y = -\beta$ with $M = J A^{\mathsf T}$
and step $s \leftarrow s + \lambda A^{\mathsf T} y$ (so each edge moves by
$y_v + y_w$). Typical behavior on the 84-vertex Crane complex: $\|\beta\|$
drops to ~1e-12 in 10–25 iterations even from seeds where raw LM stalls
between basins. It is a strict *relaxation* of closing — budget balance is
exactly one of the three closing polynomials per flower — so `solve` still
finishes the job; `bsolve` decides *which* job.

Production recipe (prescribed branching at vertices v1 v2 ...):

```
|sz| seed -d
|sz| bsolve -b v1 v2 ...
|sz| solve
|sz| layout
```

With no `-b` the aims come from the packing (`set_aim 4*pi v ...` first,
exactly the `|sn|` convention; budget aim $= \mathrm{aim}/2$), defaulting to
univalent ($w \equiv 1$) — useful as **basin rescue**: from a wild seed,
`bsolve` then `solve` lands on the univalent packing where raw `solve`
would stall or wander. `budget` (same flags, no solve) reports the current
budgets and predicted branch set — the wrap vector is the basin coordinate,
so this predicts where `solve` will land before running it.

## `tsolve` — the one-stage tail-coordinate solver

The three closing conditions of a flower *triangularize* in the pinned
(base-spoke) frame. Tracking the fundamental pair $(X, Y)$ of the spoke
recursion ($Y$ starts $0, 1$, so it never sees the base spoke):

- **budget** balance $T_v = \pi w_v$ is the Dirichlet condition $Y_n = 0$
  (wrap $= 1 +$ interior sign changes of $Y$ — Sturm oscillation);
- the **stretch** is $\Lambda_v = (-1)^{w+1} Y_{n-1}$ (closed $\iff
  \Lambda_v = 1$);
- the **shear** $\mu_v = (-1)^w(\sqrt3\, u_1 Y_{n-1} + H_{n-1})$ is *affine*
  in the base spoke.

`tsolve` runs damped Levenberg–Marquardt on the exact $3V$-row system
$(\beta_v, \Lambda_v - 1, \mu_v)$ against the $E = 3V - 6$ edges, with the
wrap vector as **input** (aims or `-b`, exactly as `bsolve`). Since the wraps
sit inside the residual, basin selection is no longer the seed's job: on the
84-vertex Crane complex `tsolve` converges to the *prescribed* twin from
every seed tried — degree seed, blind zero seed, and the seeds where the
classical LM stalls at a spurious $J^{\mathsf T}F = 0$ point or wanders to a
far solution. One command replaces the `bsolve` → `solve` pipeline:

```
|sz| seed -z
|sz| tsolve -b v1 v2 ...
|sz| layout
```

`residual` and `check` still measure the classical closing system and serve
as the independent certificate after `tsolve` finishes.

## `cont` — branch detection

The continuants $\mathfrak C_j$ built from $u = 1 - s$ around a flower are
positive exactly when the flower is unbranched (Stephenson, Thm 4.5). A
negative minimum flags a branched (4π) flower — this is how branched
solutions announce themselves, locally and Möbius-invariantly, without any
layout or winding computation.

## The radius chapter — one extender, every solver

`|sz|` now hosts the complete radius-space solver as well: an embedded
(non-registered) `SphBranchNewton` instance on the same packing. `newton`,
`perron`, `discover`, `bigcircles`, `winding`, `windfaces`, and `basin1`
behave exactly as documented in `SphBranchNewton_Guide.md`; the two names
that collide with the schwarzian commands are prefixed (`rsolve`,
`rresidual`). The standalone `|sn|` extender is unchanged and still loads
on its own.

The two chapters talk to each other through the packing:

- **radius → schwarzian**: after `newton`/`rsolve` produces a packing,
  `seed -c` imports its schwarzians (via `set_sch`) — e.g. to certify it
  Möbius-invariantly with `residual`/`cont`, or as a warm seed.
- **schwarzian → radius**: after `solve`, `layout` develops the circles;
  `rresidual` then certifies the same solution in radius coordinates —
  the independent cross-check used throughout the
  `branched_sphere_example` project.

Choosing branch structure works in either chapter and agrees by
construction: `set_aim 4*pi v ...` feeds both `newton` (angle-sum aims)
and `bsolve` (budget aims $=$ aim$/2$); `bsolve -b v ...` is the
aim-free shortcut. The schwarzian chapter needs no gauge anchoring
(`bigcircles` is only for the radius chapter — schwarzian coordinates
already quotient the Möbius freedom).

## Notes and limitations

- `layout` delegates to `dual_layout`, which seeds an equilateral first face
  and develops along the layout order; on difficult (e.g. branched) packings
  it can leave degenerate faces. The schwarzians themselves are already
  stored on the edges (`sch_report`, `sch_out` can inspect them) even when
  the layout is imperfect.
- The closing system is aim-agnostic between branch regimes: without the
  budget stage, which solution `solve` finds is governed by the seed
  (`seed -d` targets the univalent packing). With `bsolve` the branch
  structure is prescribed input, as with `|sn|` aims — see the budget
  section above. `bsolve` accepts arbitrary real aims via the packing's
  aims (cone-type data), but a genuine closed packing needs integer wraps;
  `solve` imposes the remaining two closing conditions per flower.
- Theory, proofs, and the reference Python implementation:
  `SCHWARZIAN_MONOTONE_THEORY.md`, `ARM_LEMMA.md`, and `experiments/` in the
  `branched_sphere_example` project (monotonicity/fold-freeness of the
  closing system, exact-probe Jacobian, certified convergence constants).
