# CirclePack User Guide

Welcome to the CirclePack user guide. This is a reference and
tutorial set for using CirclePack — Ken Stephenson's program for
computing and visualizing circle packings, maximal packings,
branch-point constructions, and related conformal geometry.

If you're completely new, start with the
[getting-started tutorial](#getting-started-tutorial) below. If
you're looking up a specific command or concept, jump to the
relevant [reference page](#reference-pages).

---

## Who this guide is for

The intended reader is a mathematician using CirclePack in research
or teaching — someone who thinks about triangulations, Riemann
surfaces, branched covers, or discrete conformal structures, and
wants to compute with them. Familiarity with the underlying
mathematics is assumed; familiarity with CirclePack's command
vocabulary is not.

Programmers wanting to extend CirclePack will find some of the
pages helpful (especially the **Source** sections at the end of
each reference), but the guide is oriented toward *using* the
program rather than *modifying* it. For extender writing, see
`GB_Extension_Guide.md` and the forthcoming extender references.

---

## What's here

Eighteen pages, grouped by role:

- A **getting-started tutorial** — a guided session from blank
  CirclePack to productive use.
- **Foundational pages** — concepts, grammar, object-list
  vocabulary. Read these when something in a reference page
  uses terminology you don't recognize.
- **Core command references** — dedicated pages for the commands
  you'll use most: `disp`, `layout`, `repack`, `color`.
- **Themed references** — collected treatment of related command
  families: solvers, I/O, geometry/layout, combinatorial editors,
  setters, queries, lists and paths, session and scripting, and
  miscellaneous.

What's **not** here yet: the extenders (`|GB|`, `|BF|`, `|BQ|`,
`ConformalTiling`, `ShapeShifter`, `AffinePack`, `WeldManager`,
and others in `src/ftnTheory/`). Those will be documented in a
separate pass. Extender-specific commands are invoked via the
`|ABBREV|` prefix once an extender has been started with the
`extender` command (documented in **miscellaneous_reference**).

---

## Reading order for newcomers

If you're new to CirclePack, the recommended path is:

1. **[Getting-started tutorial](getting_started_tutorial.md)** —
   hands-on introduction. ~15 minutes.
2. **[Concepts](concepts.md)** — the mathematical vocabulary
   (alpha, gamma, aims, branch points, red chain, DCEL,
   schwarzian, geometries). Read what's new to you.
3. **[Command syntax](command_syntax.md)** — the grammar of
   flags, variables, loops. Skim.
4. **[List specifiers](list_specifiers.md)** — how to name sets
   of vertices, faces, edges. Essential once you're writing
   commands beyond trivial cases.
5. Reference pages as needs arise.

Experienced users can skip to the references directly.

---

## Getting-started tutorial

- **[getting_started_tutorial.md](getting_started_tutorial.md)** —
  A guided session: generate a packing, display it, solve it,
  transform it, edit it, save it. Introduces `disp`, `max_pack`,
  `repack`, `layout`, `rld`, `set_aim`, `Mobius`, `flip`,
  `write`, and basic script structure. Cross-references point
  into the reference pages for depth.

---

## Foundational pages

The three foundational pages cover vocabulary shared across the
entire command surface.

- **[concepts.md](concepts.md)** — Mathematical terminology: alpha
  and gamma root vertices, aim and branch points, red chain, DCEL
  structure, the three geometries (hyperbolic/Euclidean/spherical)
  and their codes, schwarzians, tangency vs inversive-distance
  packings, tiles vs triangulations, x-radii, and more.
  Includes a glossary.

- **[command_syntax.md](command_syntax.md)** — CirclePack's
  command grammar: the flag-segment structure, positional
  arguments, variables, `[x]` substitution, loops (`for`, `while`,
  `if`), command chaining with `;`, pack-targeting with `-p{n}`,
  and script constructs.

- **[list_specifiers.md](list_specifiers.md)** — The object-list
  vocabulary shared across `NodeLink`, `FaceLink`, `EdgeLink`,
  `HalfLink`, `TileLink`, `GraphLink`, `BaryLink`, `PointLink`,
  `PathLink`. Covers `a` (all), `b` (boundary), `i` (interior),
  explicit index lists, ranges, boundary segments `b(v,w)`,
  stored lists, selector notation `{c:...}`, and the
  qualifier-prefix variants (`Iv`, `If`, `Ie`, `N`, `G`,
  `R`, `A`, etc.).

---

## Core command references

One page each for the commands that come up most often.

- **[disp_command.md](disp_command.md)** — The `disp` command (and
  its `Disp` / `dISp` / `DISp` variants): every flag, every
  modifier, the `DispFlags` mini-language for color/fill/thickness.

- **[color_command.md](color_command.md)** — The `color` command
  for setting stored per-object colors, with the 256-color palette
  reference.

- **[layout_command.md](layout_command.md)** — The `layout`
  command: lay out circles given radii. Flag vocabulary for
  controlling layout order, single-edge vs average-edge placement,
  schwarzian layout, layout trees.

- **[repack_command.md](repack_command.md)** — The `repack`
  command: iteratively solve for radii given aims. Covers the
  default algorithm, Orick's method, specified-vertex re-packing,
  and cycle caps.

---

## Themed references

Grouped treatments of related command families.

- **[solvers_reference.md](solvers_reference.md)** — Beyond
  `repack`: `max_pack`, `perp_pack`, `random_pack`, `polypack`,
  `torpack`, `smooth`, `fix`, `rld`, `rlsd`. Decision table for
  choosing the right solver.

- **[io_reference.md](io_reference.md)** — Reading and writing:
  `read`, `Read`, `infile_read`, `load_pack`, `write`, `Write`,
  `output`, `post` (PostScript), `svg`, `screendump`, `read_CT`,
  `read_path`, `write_custom`, `write_path`, `write_tiling`. Also
  covers the file-location conventions and the filename-flag
  syntax.

- **[geometry_layout_reference.md](geometry_layout_reference.md)** —
  Geometric and layout manipulations: `Mobius`, `inv_Mobius`,
  `appMob`, `pair_mob`, `cir_invert`, `rotate`, `scale`,
  `norm_scale`, `NSpole`, `focus`, `alpha`, `gamma`, `geom_to_h`,
  `geom_to_e`, `geom_to_s`, `dual_layout`, `holonomy_trace`,
  `hex_refine`, `hex_slide`, `hh_path`, `flat_hex`, `face_err`,
  `square_fit`.

- **[combinatorial_editors_reference.md](combinatorial_editors_reference.md)** —
  Editing the triangulation itself: `Cleanse`, `cleanse`,
  `add_cir`, `add_edge`, `add_bary`, `add_face_triple`,
  `add_ideal`, `add_layer`, `add_gen`, `rm_cir`, `rm_bary`,
  `rm_edge`, `rm_quad`, `split_edge`, `split_flower`,
  `bary_refine`, `flip`, `unflip`, `swap`, `meld_edge`, `migrate`,
  `double`, `puncture`, `slit`, `zip`, `prune`, `renumber`,
  `reorient`, `adjoin`, `blend`, `canonical`, `cookie`, `frack`,
  `adjust_rad`, `adjust_sch`.

- **[setters_reference.md](setters_reference.md)** — The `set_*`
  commands (about 40 of them) grouped by theme: core packing data
  (`set_rad`, `set_aim`, `set_cent`, `set_alpha`, `set_gamma`,
  `set_invDist`), display and color, stored lists, paths and grids,
  schwarzians, and transformation state.

- **[queries_reference.md](queries_reference.md)** — The `?*`
  commands (about 30 of them) grouped by theme: packing identity
  (`?alpha`, `?gamma`, `?geom`), geometry and counts (`?count`,
  `?bdry`, `?deg`), per-object data (`?rad`, `?cent`, `?aim`,
  `?color`), diagnostics (`?qual`, `?anglesum`), and session
  state.

- **[lists_paths_reference.md](lists_paths_reference.md)** —
  Stored lists and planar paths: the `set_*list` family (for
  vertex, face, edge, half-edge, tile, graph, barycentric,
  point, and double lists, in both per-pack and global scopes),
  `elist_to_path`, `path_construct`, `path_Mob`, `enclose`.
  Covers per-pack vs. global list storage and how to use stored
  lists as arguments to other commands.

- **[session_scripting_reference.md](session_scripting_reference.md)** —
  Session-level infrastructure: pack slot management (`act`,
  `copy`, the `-p{n}` flag), filesystem navigation (`cd`, `pwd`),
  GUI windows (`open`, `close`, `Map`, `chgScreen`,
  `chgPaired`), scripts (`script`, `scroll`), messages and
  evaluation (`msg`, `eval`, `evalp`), timing (`timer`,
  `delay`), debugging (`debug`), remote access
  (`socketServer`), and quitting (`quit`, `exit`).

- **[miscellaneous_reference.md](miscellaneous_reference.md)** —
  Packing generation (`seed`, `create`, `rand_tri`,
  `rand_pt_read`), animation (`motion`), iterative algorithms
  (`perron`), tiling (`pave`), statistical data (`sch_data`),
  specialized reports (`aspect`, `torus_t`, `doyle_point`,
  `gen_mark`), inter-pack mapping (`embed`, `map`, `erf_ftn`),
  the `extender` command for activating extenders, deprecated
  and unimplemented commands (`gen_cut`, `doyle_annulus`), and
  test routines (`T_*`).

---

## Finding something specific

If you know the **command name**, search the reference pages —
every command is indexed in at least one, and important commands
have cross-references from multiple.

If you know the **mathematical concept**, start with
[concepts.md](concepts.md) — most entries there point to the
commands that operate on them.

If you're looking for **how to do X**, the
[getting-started tutorial](getting_started_tutorial.md)'s
"Where to go next" section has typical investigation patterns,
and each reference page has a "Typical workflows" section.

If you can't find it here, the command may be an extender
command (invoked via a prefix like `|GB|` or `|BF|` once the
extender is started — see **miscellaneous_reference** for the
`extender` command). Extender-specific commands are documented
separately. Or it may be a test/development routine — commands
prefixed `T_` are documented in **miscellaneous_reference** but
are not considered stable API.

---

## About command conventions

A few conventions recur across the command set; keeping them in
mind makes everything easier to read.

**Case matters.** Many commands have a lowercase and an uppercase
variant — `read` / `Read`, `write` / `Write`, `cleanse` /
`Cleanse`. The distinction is usually about scope or directory:
lowercase works within the current context; uppercase operates
more broadly (absolute paths, all packs, etc.).

**Flags are single letters after a dash.** `disp -c a -e a`.
Compound modifiers stack inside one flag: `-cfc180` means "circles
(`c`), filled (`f`), color 180 (`c180`)."

**Object lists are their own grammar.** See
[list_specifiers.md](list_specifiers.md). The short version: `a`
means "all," `b` means "boundary," `i` means "interior," numbers
pick specific objects, `v1 v2` lists two, `{v1, v2}` lists two
with a selector-ish feel, and `{c:(d.eq.6)}` picks vertices
matching a predicate.

**Commands chain with `;`.** `repack; layout; disp -wr` is three
commands in sequence.

**Pack-targeting with `-p{n}`.** Most commands accept `-p0`,
`-p1`, or `-p2` as their first flag to target a specific pack
without switching the active one.

---

## Known limitations and rough edges

Some things to keep in mind while using the commands:

- **`rotate`** takes radians in units of π: `rotate 0.5` means 90°,
  not 0.5 radians.
- **`scale`** with no argument scales by 1.1, not 1.0.
- **`inv_Mobius`** is not the mathematical inverse of `Mobius` —
  it's the orientation-reversed version of the same Möbius.
- **`unflip`** does the same thing as `flip` (without flags); the
  two names are semantic companions, not functional opposites.
- **`add_face_triple`** is not yet implemented in the DCEL case.
- **`gen_cut`** is deprecated and throws an exception.
- **`doyle_annulus`** is marked unfinished in the source.
- **Spherical `repack`** is not supported; use `max_pack` for
  spherical packings.
- **`load_pack`** is GUI-only; scripts should use `read` or
  `Read`.
- **`Cleanse`** (capital C) clears all packs irreversibly — make
  sure you've saved first.
- **`puncture`** clears `xyzpoint` data.
- **`motion`** blocks the command thread during animation
  playback. A long animation (many frames × long delay) can
  freeze the interface for its entire duration.
- **Some commands are stateful** (e.g., `smooth`, `flip -h`,
  `path_construct`). Their behavior on consecutive calls
  depends on prior calls.
- **`T_*` commands** are test/development routines and may change
  without warning.

Each gotcha is called out in context on the relevant reference
page.

---

## Conventions used in these pages

Each reference page follows the same skeleton:

- **About this page** — scope, commands covered, what's not
  covered.
- **Sections** — linked contents.
- **Per-command entries** — overview, synopsis, flag table,
  examples, requirements, notes.
- **Typical workflows** — realistic sequences showing the
  commands in use.
- **Notes and gotchas** — caveats, common mistakes, gotchas.
- **Source** — the `.java` classes and methods backing the
  commands, for readers who want to trace into the code.

Code blocks show command-line input (one command per line, no
language tag — these are CirclePack-script, not Java or shell):

```
max_pack
set_aim 4 5
rld
```

Flag tables use pipe-delimited markdown:

| Flag | Meaning |
|------|---------|
| `-a` | Apply to all objects |
| `-b` | Apply to boundary objects |

File paths and inline commands are in backticks: `disp -wr`,
`src/packing/PackData.java`.

Cross-references use page names in bold: **concepts**,
**list_specifiers**, **solvers_reference**.

---

## Source of this guide

These pages were produced from CirclePack's source code
(the `ashejim/CirclePack` fork, forked from `kensmath/CirclePack`)
and from Doxygen-generated documentation. Every command entry
was written from direct reading of the relevant Java source;
every example was constructed by hand and audited against the
grammar specified in `CommandStrParser` and the various `...Link`
parsers.

Expect occasional rough edges. Report issues, corrections, or
gaps in the relevant place.

---

## Acknowledgements

CirclePack is by **Ken Stephenson** (University of Tennessee,
Knoxville), with contributions from many collaborators and
students over the program's long history. The mathematics behind
it draws on decades of work on discrete conformal geometry by
Stephenson, Rodin, Sullivan, Schramm, He, and others. See
[www.circlepack.com](https://www.circlepack.com) for the project
home, and Ken Stephenson's book *Introduction to Circle Packing:
The Theory of Discrete Analytic Functions* (Cambridge, 2005) for
the mathematical foundation.

This user guide builds on the `GB_Extension_Guide.md` style
reference and aims, long-term, for something closer to the
scikit-learn user guide in organization — prose-driven sections,
worked examples, a real navigable reference rather than a pile
of unconnected command entries.
