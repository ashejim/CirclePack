# Getting Started with CirclePack

## About this page

This is a guided tour of CirclePack for someone who has the program
running and wants to get productive. It walks through a realistic
working session — loading a packing, solving it, transforming it,
editing it, saving results — with cross-references into the
reference pages for the details.

If you need to first **install** CirclePack and set up the build,
see `getting_CP_started.md` (the IDE/Git setup guide). This page
picks up where that leaves off: CirclePack is running, the command
line is waiting for input, and you want to know what to type.

The intended audience is mathematicians who think about circle
packings, maps, and conformal structures. The commands shown here
are the ones you'll use daily; the depth lives in the reference
pages.

---

## Contents

1. [Orientation: what you're looking at](#orientation-what-youre-looking-at)
2. [First packing: generate and view](#first-packing-generate-and-view)
3. [The display command](#the-display-command)
4. [Solving: `max_pack`, `repack`, `layout`](#solving-max_pack-repack-layout)
5. [Loading and saving](#loading-and-saving)
6. [Working with multiple packings](#working-with-multiple-packings)
7. [Transformations: rotate, scale, Möbius](#transformations-rotate-scale-möbius)
8. [Branch points: using aims](#branch-points-using-aims)
9. [Editing combinatorics](#editing-combinatorics)
10. [Asking the packing about itself](#asking-the-packing-about-itself)
11. [Writing a short script](#writing-a-short-script)
12. [Where to go next](#where-to-go-next)

---

## Orientation: what you're looking at

CirclePack has a GUI with a **canvas** (where packings are drawn),
a **command line** at the bottom, and various panels for scripts,
display options, and preferences. Most serious work happens by
typing commands into the command line, even when the GUI offers
button equivalents.

The program keeps up to **three packings in memory at once**,
stored in *pack slots* numbered `0`, `1`, `2`. One slot is always
the **active** one — commands that don't specify a target operate
on it. The active slot is shown in the GUI's pack selector.

A packing has several pieces of data:
- **Combinatorics**: a triangulation (or tiling).
- **Geometry**: hyperbolic, Euclidean, or spherical.
- **Radii**: one per vertex.
- **Centers**: one per vertex.
- **Aims**: one per vertex — the target total angle around it (usually 2π).
- **Inversive distances**: one per edge — usually 1 (tangency).
- **Colors, marks, and other per-object data.**

When you load or generate a packing, you get combinatorics and
radii; you then **solve** to get centers (a valid embedding).

For the vocabulary — `alpha`, `gamma`, red chain, DCEL, schwarzian,
branch points — see the **concepts** page.

---

## First packing: generate and view

Let's make a packing immediately, without needing any sample file:

```
random_pack 100
```

CirclePack generates 100 random points, builds a Delaunay
triangulation of them, and runs `max_pack` to produce the maximal
hyperbolic packing filling the unit disc. You'll see the result on
the canvas.

If nothing appears, try:

```
disp -wr
```

This **wipes** the canvas (`-w`) and **redraws** using stored
defaults (`-r`). You should now see ~100 circles filling the unit
disc.

**What just happened.** `random_pack` chose some number of random
points, built their combinatorial Delaunay triangulation, and
computed the maximal hyperbolic packing — the unique packing (up
to Möbius transformation) of that combinatorics filling the unit
disc tangentially. The result is the default hyperbolic view.

For all that `random_pack` can do, see **solvers**; for the
full `disp` vocabulary, see **disp**.

---

## The display command

`disp` is the command you'll use most. The short version:

```
disp -w                          # wipe canvas
disp -wr                         # wipe and redraw all
disp -c                          # draw circles (using stored colors)
disp -f                          # draw faces
disp -e                          # draw edges
disp -c a -e a                   # circles and edges, all vertices
disp -cfc180 5 7 9               # circles of vertices 5, 7, 9 in color 180
```

The anatomy of a disp flag is `-{object}{modifiers} {list}`:
- Object: `c` (circles), `f` (faces), `e` (edges), `t` (tiles).
- Modifier examples: `f` (filled), `c{n}` (color n), `t` (thick),
  `n` (with index labels).
- List: a list specifier — `a` (all), `b` (boundary), `i`
  (interior), explicit numbers, or a selector like `{c:(d.eq.6)}`
  (vertices of degree 6).

Two examples that show up often:

```
# Red-filled boundary circles, everything else default
disp -cfc200 b -c i

# Faces colored by index, small vertex labels on boundary
disp -ff a -cn b
```

For the full flag vocabulary and the color-number table, see
**disp** and **color**. For list specifiers, see
**list_specifiers**.

---

## Solving: `max_pack`, `repack`, `layout`

CirclePack distinguishes three steps:

1. **Radii-solve** (`repack`): given aims and combinatorics, find
   radii so that angle sums match aims.
2. **Layout** (`layout`): given radii, place the circles in the
   plane (or disc, or sphere).
3. **Display** (`disp`): draw the result.

`max_pack` does the whole cycle at once, starting from default
aims and radii — it's the first thing to run on a new
combinatorics.

```
max_pack                         # full solve from scratch
repack                           # resolve with current aims/modifications
layout                           # lay out using current radii
```

A common shortcut is `rld` — "repack, layout, display":

```
rld                              # repack; layout; disp -wr
```

This is the iteration workhorse. Change something; `rld`; observe.

For the iterative variant using schwarzians, use `rlsd`.

For depth on each of these, see **repack**, **layout**,
**solvers**.

---

## Loading and saving

CirclePack loads packings with `read`:

```
read unit.p                      # from the default packing directory
Read ~/work/mypack.p             # from an arbitrary path
```

The **lowercase** `read` looks in `PackingDirectory` (settable via
GUI preferences); the **uppercase** `Read` accepts an absolute or
`~/`-relative path. Same convention for other I/O commands.

To save:

```
write -m -f mypack.p             # save main packing data
write -M -f mypack.p             # save everything (colors, schwarzians, etc.)
Write -m -f ~/save.p             # to an arbitrary path
```

The `-m` flag writes the standard "main" set (centers, geometry,
inversive distances, radii, centers, vertex lists). The `-M` flag
adds aims, face colors, and other secondary data. For the full
content-flag table, see **io_reference**.

A typical pattern:

```
random_pack 50
write -m -f my_first.p           # save before experimenting
```

---

## Working with multiple packings

Three pack slots let you compare, contrast, or preserve states.

```
copy 1                           # copy active packing to pack 1
act 1                            # switch to pack 1 as active
act 0                            # back to pack 0
```

A typical "branching experiment" pattern:

```
random_pack 100                  # in pack 0
copy 1                           # preserve a copy in pack 1
flip 5 10                        # modify pack 0
rld                              # re-solve and see the result
act 1                            # switch to the preserved original
disp -wr                         # redraw
```

You now have two packings side-by-side (in different slots) for
comparison.

To target a pack without switching to it, use the `-p{n}` flag
available on most commands:

```
disp -p1 -wr                     # draw pack 1 even though we're on pack 0
```

---

## Transformations: rotate, scale, Möbius

Once a packing is laid out, you can transform it without re-solving.

```
rotate 0.5                       # rotate by 0.5 * π = 90°
scale 1.5                        # uniform Eucl scale by 1.5
```

**Watch out:** `rotate` takes radians in units of π, so `rotate 1`
is a half turn, not 1 radian. And `scale` defaults to 1.1 if
called without an argument — not 1.0.

For canonical normalizations:

```
norm_scale -a 1.0                # scale packing so total area = 1 (Eucl)
norm_scale -h 1 5                # rotate so 1 → 5 is horizontal
norm_scale -i 3                  # put vertex 3 at z = i
```

For Möbius transformations, either define one inline:

```
appMob 0 0 1 0 0 0 1 0           # identity (no-op) — a=1, b=0, c=0, d=1
```

or set one up and apply it:

```
set_Mobius -xyzXYZ 0 0 1 0 0 1 0 0 2 0 0 2
Mobius                           # apply stored Möbius to all vertices
```

For a packing with side-pairings (multiply-connected), apply a
named pair:

```
pair_mob {name}
```

All of these are in **geometry_layout_reference**.

---

## Branch points: using aims

Branch points are one of the main reasons to use CirclePack — they
let you construct maps with prescribed singularities.

A branch point of order `n` at vertex `v` means the map has angle
`2π(n+1)` at `v`. To create one:

```
set_aim 2 5                      # aim 2π at vertex 5 (this is default, trivial)
set_aim 4 5                      # branch point of order 1 at vertex 5
set_aim 6 10                     # branch point of order 2 at vertex 10
```

Then re-solve:

```
rld
```

You'll see the packing deform — circles near the branch point
accommodate the extra angle.

To reset to default:

```
set_aim_default                  # all interior aims → 2π
rld
```

For deeper treatment, see **concepts** (for the theory) and
**setters_reference** (for all `set_*` commands).

---

## Editing combinatorics

You can modify the triangulation directly. A few examples:

**Flip an edge:**

```
flip 3 5                         # flip the edge between vertices 3 and 5
rld
```

**Add a barycenter inside a face:**

```
add_bary 17                      # split face 17 into three via its barycenter
rld
```

**Split an edge in half:**

```
split_edge 3 5
rld
```

**Refine the whole packing:**

```
bary_refine                      # hex-baryrefine: every face gets a bary
set_aim_default                  # new vertices default to aim 2π
rld
```

**Remove a boundary vertex:**

```
rm_cir 42
rld
```

**Cut open a packing:**

```
slit 3 5 7 9                     # slit along this chain
set_aim_default                  # new bdry vertices need aims reset
rld
```

These operations all require a re-solve afterward. `rld` is your
friend. For the full editor vocabulary, see
**combinatorial_editors_reference**.

---

## Asking the packing about itself

The `?` commands (queries) report information without modifying.
Useful for debugging or analysis:

```
?alpha                           # which vertex is alpha?
?count                           # how many vertices, faces, edges?
?rad 5                           # radius at vertex 5
?center 5                        # complex center at vertex 5
?aim 5                           # aim at vertex 5
?deg 5                           # degree at vertex 5
?bdry                            # boundary info
?qual                            # packing quality (error estimates)
```

For the full query vocabulary, see **queries_reference**.

---

## Writing a short script

Any sequence of commands can be put in a script (a text file with
commands). Scripts are saved with a `.cps` extension by tradition.

Example (save as `mytest.cps`):

```
# Create a branch-point experiment.
random_pack 200
copy 1                           # save the original

set_aim 4 5                      # branch point at vertex 5
rld
disp -cfc180 5                   # highlight vertex 5 in red

# Compare with the original
act 1
disp -wr

# Reset and save both
act 0
set_aim_default
write -m -f with_branch.p

act 1
write -m -f original.p
```

Load and run:

```
script mytest.cps
```

Or load interactively with the GUI's script manager. Scripts also
support parameterization, loops, and variables — see
**command_syntax** for the grammar.

Within a script, you can prompt for input, pause for display, and
pass control to sub-routines. See the Scripts panel in the GUI
for interactive script editing.

---

## Where to go next

You now have a working vocabulary. Here are the reference pages
to dive into as needs arise:

- **concepts** — the mathematical ideas: alpha, gamma, aim, branch
  points, red chain, DCEL, schwarzian, tangency vs inversive
  distance, geometry codes, etc.
- **command_syntax** — the exact grammar of flag strings,
  variables, and control structures.
- **list_specifiers** — how to name sets of vertices, faces,
  edges, half-edges, tiles, in `a`/`b`/`i`/`{v..}`/`{c:...}` form.
- **disp** — every display option you might need.
- **color** — the stored color model and the 256-color palette.
- **layout** — precise control over how circles get placed.
- **repack** — iterative radius solvers.
- **solvers** — beyond `repack`: `max_pack`, `perp_pack`,
  `random_pack`, `polypack`, `torpack`, `smooth`.
- **geometry_layout_reference** — Möbius, rotation, scaling,
  normalization, geometry conversion, holonomy, hex operations.
- **combinatorial_editors_reference** — adding, removing,
  flipping, splitting, cutting, gluing.
- **setters_reference** — everything you can `set_*`.
- **queries_reference** — everything you can `?`.
- **io_reference** — `read`, `write`, `output`, images, scripts.

### Typical investigation patterns

**Studying a specific triangulation:** load or generate, `max_pack`,
play with aims, `rld`, save.

**Visualizing a conformal map:** two packings in separate slots
sharing combinatorics but different radii/aims; compare with
`disp -p{n}`.

**Experiments on branch-point behavior:** set aims systematically,
`rld` after each change, observe.

**Teaching or figure-making:** `post -o`, issue drawing commands,
`post -x`; see **io_reference**.

**Testing a hypothesis:** script the experiment end-to-end; repeat
with parameter variations; save outputs with structured names
(`out_n=100.p`, `out_n=200.p`, etc.).

---

## A closing note on style

CirclePack's command language grew over decades and shows its
history. Commands that look similar sometimes aren't: `flip`
and `unflip` do the same thing; `Mobius` and `inv_Mobius` are
*not* mathematical inverses; `scale` with no argument scales by
1.1, not 1.0. Read the relevant reference page before committing
to a command you're not sure about.

On the other hand, the commands that matter — `disp`, `repack`,
`layout`, `max_pack`, `set_aim`, `rld`, `Mobius`, `flip`, `write`
— are well-tested and do what you'd expect. You'll be productive
quickly.

Welcome to CirclePack.
