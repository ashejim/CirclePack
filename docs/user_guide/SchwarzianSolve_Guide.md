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
| `seed [-d\|-c] [-p sigma]` | set the edge schwarzians: `-d` from vertex degrees (pure combinatorics), `-c` from the current geometry (via `set_sch`); `-p` adds Gaussian noise |
| `residual` | norm of the flower-closing (monodromy) residual; reports the worst flower |
| `solve [-i maxits] [-t tol]` | Levenberg–Marquardt on the closing system; continues from the current internal state, so repeated small `-i` calls step the solver visibly |
| `layout` | store the current schwarzians on the edges and develop circles via `dual_layout` |
| `check` | compare the solver's schwarzians against those recomputed from the current geometry |
| `cont` | per-flower continuant minima; a negative value flags a branched flower |
| `budget [-b v1 v2 ...]` | report the per-flower angular budgets $T_v$: the budget residual $\beta_v = T_v - \pi w_v$ and the predicted branch set (wraps $= \mathrm{round}(T_v/\pi)$) |
| `bsolve [-i maxits] [-t tol] [-b v1 v2 ...]` | **stage-1 budget-Newton**: drive every $T_v$ to its aim $\pi w_v$, selecting the solution *basin* before `solve` closes the flowers; `-b` lists the prescribed branch vertices ($w=2$), otherwise aims are read off the packing (`set_aim 4*pi v ...` as for `\|sn\|`) |
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
