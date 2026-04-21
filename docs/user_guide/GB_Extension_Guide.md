# CirclePack `|GB|` Extension — Generalized Branching

## About this page

The `|GB|` (Generalized Branching) extension provides commands
for incorporating **generalized branch points** into a circle
packing. A generalized branch point is a small subcomplex within
the triangulation where local modifications — to angle sums,
overlaps, or combinatorics — produce a global effect analogous
to a classical branch point of an analytic function.

The extension maintains a reference copy of the original packing
(`refPack`) so that experiments can be reverted. Branch points
are numbered by ID starting from 1.

Commands covered: `bp_trad`, `bp_sing`, `bp_chap`, `click`,
`delete`, `revert`, `copy`, `status`, `angsum_err`, `get_param`,
`event`, `set_param`, `reset_overlaps`, `holonomy`, `disp`.

This page is part of the CirclePack user guide. See **INDEX** for
the full document set. For the `extender` command that starts
this and other extenders, see **miscellaneous_reference**.

---

## Sections

1. [Activating the extension](#activating-the-extension)
2. [Aim-convention note](#aim-convention-note)
3. [Branch-point types](#branch-point-types)
4. [Creating branch points](#creating-branch-points) —
   `bp_trad`, `bp_sing`, `bp_chap`, `click`
5. [Managing branch points](#managing-branch-points) —
   `delete`, `revert`, `copy`
6. [Querying state](#querying-state) — `status`, `angsum_err`,
   `get_param`, `event`
7. [Adjusting parameters](#adjusting-parameters) — `set_param`,
   `reset_overlaps`
8. [Layout and display](#layout-and-display) — `holonomy`, `disp`
9. [Typical workflow](#typical-workflow)
10. [Multiple branch points](#multiple-branch-points)
11. [Notes](#notes)
12. [Source](#source)

---

## Activating the extension

The `|GB|` extension is started by the core `extender` command,
using the abbreviation `gb`:

```
act 0                            # make pack 0 active (if not already)
extender gb                      # start |GB| on the active pack
```

Once active, the extension attaches to the current pack and
stores a reference copy of its packing data for later reversion.
Its commands become available via the **`|GB|` prefix**:

```
|GB| bp_trad -a 4 -i 5
|GB| status
```

To dispose of the extension (releasing its reference copy):

```
extender -x gb
```

Extenders are scoped per-pack — starting `|GB|` on pack 0 does
not make its commands available on pack 1.

See **miscellaneous_reference** for the full `extender` command
reference, including lifecycle flags (`?`, `-x`, `-r`) and the
complete abbreviation table.

---

## Aim-convention note

**This is important to internalize before reading further.**

Branch-point commands in `|GB|` interpret their `-a {x}` flag
as a **multiple of π**, not as raw radians:

```
|GB| bp_trad -a 4 -i 5           # angle sum = 4π at vertex 5
|GB| bp_sing -a 6 -i 10          # angle sum = 6π at face 10
```

This differs from the core `set_aim` command, where the value is
in raw radians:

```
set_aim 4 5                      # aim at vertex 5 = 4 radians (~1.27π)
```

Both commands are about target angle sums, but the numerical
meaning of "4" is different. When switching between core and
extender commands, keep the distinction in mind. See
**setters_reference** for the radian convention used by core
setters.

---

## Branch-point types

Three types of generalized branch points are supported.

### Traditional (`bp_trad`, type 1)

The simplest kind: a single vertex `v` is given an angle-sum
target greater than 2π (e.g., 4π for a double winding). The
subcomplex is just the flower of `v`.

### Singular (`bp_sing`, type 4)

The branch point lives in the **interstice** of a single face
`{v, u, w}`. The extra 2π of winding is distributed among the
three vertices via overlap parameters. The event horizon is the
link of the face's edges.

### Chaperone (`bp_chap`, type 6)

The flower of vertex `v` is augmented with a "sister" vertex and
two "chaperone" circles. The chaperone construction uses two
designated "jump petals" and overlap parameters to control the
geometry. This is the most flexible type and can model
fractional winding.

---

## Creating branch points

### `bp_trad` — Traditional branch point

Create a classical branch point at a vertex.

```
|GB| bp_trad -a {aim} -i {vertex}
```

| Flag | Description |
|------|-------------|
| `-a {aim}` | Target angle sum as a multiple of π (e.g., `4` means 4π) |
| `-i {vertex}` | Index of the interior vertex |
| `-X` | (optional) Wipe all existing branch points first |

**Examples:**

```
# Double winding (4π) at vertex 5
|GB| bp_trad -a 4 -i 5

# Triple winding (6π) at vertex 12
|GB| bp_trad -a 6 -i 12

# Wipe existing branch points, then create at vertex 8
|GB| bp_trad -X -a 4 -i 8
```

---

### `bp_sing` — Singular branch point

Create a branch point in the interstice of a face.

```
|GB| bp_sing -a {aim} -i {face} -o {o1 o2}
```

| Flag | Description |
|------|-------------|
| `-a {aim}` | Target angle sum as a multiple of π (default: 4) |
| `-i {face}` | Index of the face |
| `-o {o1 o2}` | Overlap parameters in [0, 1] (default: 1/3, 1/3) |
| `-b {barylink}` | (alternative) Position via barycentric coordinates |
| `-X` | (optional) Wipe all existing branch points first |

**Examples:**

```
# Singular branch point in face 10, default overlaps
|GB| bp_sing -a 4 -i 10

# Singular branch point with custom overlaps
|GB| bp_sing -a 4 -i 10 -o 0.25 0.5

# Using barycentric coordinates to position within the face
|GB| bp_sing -a 4 -b 7 0.3 0.3

# Higher winding in face 23
|GB| bp_sing -a 6 -i 23 -o 0.4 0.4
```

---

### `bp_chap` — Chaperone branch point

Create a branch point at a vertex using the chaperone
construction.

```
|GB| bp_chap -a {aim} -i {vertex} -j {w1 w2} -o {o1 o2}
```

| Flag | Description |
|------|-------------|
| `-a {aim}` | Target angle sum as a multiple of π (default: 4) |
| `-i {vertex}` | Index of the center vertex |
| `-j {w1 w2}` | Indices of the two "jump" petals (default: petals 1 and 3) |
| `-o {o1 o2}` | Overlap parameters in [0, 1] (default: 1/3, 1/3) |
| `-X` | (optional) Wipe all existing branch points first |

**Examples:**

```
# Chaperone branch at vertex 7 with default jump petals
|GB| bp_chap -a 4 -i 7

# Chaperone branch at vertex 7, specifying jump petals 3 and 9
|GB| bp_chap -a 4 -i 7 -j 3 9

# Chaperone with custom overlaps
|GB| bp_chap -a 4 -i 7 -j 3 9 -o 0.2 0.5

# Higher-order branching
|GB| bp_chap -a 6 -i 15 -j 4 10 -o 0.3 0.3
```

---

### `click` — Interactive branch point placement

Click a point in the plane to automatically determine the type
of branch point and create it. CirclePack examines whether the
point is near a vertex (→ traditional or chaperone), inside a
face (→ singular), or on an edge.

```
|GB| click [flags] {x} {y}
```

| Flag | Description |
|------|-------------|
| `-a {aim}` | Target angle sum as multiple of π (default: 4) |
| `-x` | Wipe existing branch points that are contiguous to the click location |
| `-X` | Revert everything before placing |

**Examples:**

```
# Click at coordinates (1.5, 0.8) — auto-detect type
|GB| click 1.5 0.8

# Click with aim = 6π
|GB| click -a 6 1.5 0.8

# Clear neighboring branch points, then place
|GB| click -x 1.5 0.8

# Full revert, then place
|GB| click -X 1.5 0.8
```

---

## Managing branch points

### `delete` — Remove a branch point

```
|GB| delete -b{n}
|GB| delete all
```

**Examples:**

```
# Delete branch point with ID 2
|GB| delete -b2

# Delete all branch points (equivalent to 'revert')
|GB| delete all
```

---

### `revert` — Restore original packing

Discards all branch points and reverts the packing to the stored
reference copy.

```
|GB| revert
```

Semantically similar to `Cleanse` for the current packing (see
**combinatorial_editors_reference**), but scoped to branch-point
modifications only.

---

### `copy` — Copy reference packing

Copy the original (unmodified) reference packing — the snapshot
made when `|GB|` was activated — to another pack slot. This
lets you compare the pre-branching packing against the modified
version side-by-side.

```
|GB| copy {pnum}
```

**Examples:**

```
# Copy the original packing to pack slot 2
|GB| copy 2
```

See also the core `copy` command in
**session_scripting_reference**, which copies the current
(possibly modified) pack rather than the reference copy.

---

## Querying state

### `status` — Report branch point status

```
|GB| status
|GB| status -b{n}
```

Reports the type, vertex/face, holonomy error, and other data
for the specified branch point (or all branch points). Also
reports the parent packing's overall angle-sum error.

**Examples:**

```
# Status of all branch points
|GB| status

# Status of branch point 1 only
|GB| status -b1
```

---

### `angsum_err` — Report angle-sum error

```
|GB| angsum_err
```

Reports the L² angle-sum error of the current packing: the sum
of squared deviations of actual angle sums from their aims.

Comparable to the core `?anglesum` query (see
**queries_reference**), but specialized for the branch-point
aims. Useful as a convergence diagnostic after `repack`.

---

### `get_param` — Display parameters

```
|GB| get_param -b{n}
```

Displays the current parameters for the specified branch point
(aim, overlaps, jump petals, etc., depending on type).

**Examples:**

```
# Get parameters for branch point 1
|GB| get_param -b1

# Without -b flag, defaults to branch point 1
|GB| get_param
```

---

### `event` — Append event horizon edges

Appends the "event horizon" edges (the boundary of the branch
point's subcomplex) to the packing's edge list for display or
further processing.

```
|GB| event -b{n}
```

**Examples:**

```
# Get event horizon of branch point 2
|GB| event -b2

# Display the event horizon edges
|GB| event -b1
disp -e elist
```

See **lists_paths_reference** for the `elist` stored-list
mechanism that this populates, and **disp_command** for
`disp -e` to render it.

---

## Adjusting parameters

### `set_param` — Set parameters

```
|GB| set_param -b{n} {parameters}
```

Sets type-specific parameters for the designated branch point.
The parameter format depends on the branch type.

**Examples:**

```
# Set parameters for branch point 1
|GB| set_param -b1 0.3 0.5

# Adjust aim for branch point 2
|GB| set_param -b2 -a 6
```

---

### `reset_overlaps` — Reset overlap values

Works for chaperone and singular branch points only.

```
|GB| reset_overlaps -b{n} {o1 o2}
```

**Examples:**

```
# Reset overlaps for branch point 1
|GB| reset_overlaps -b1 0.25 0.5

# Set equal overlaps
|GB| reset_overlaps -b1 0.333 0.333
```

---

## Layout and display

### `holonomy` — Check holonomy

Computes the holonomy Möbius transformation around the packing's
red chain. The Frobenius norm of the result measures how far the
packing is from closing up properly. A norm near zero indicates
the packing is consistent.

```
|GB| holonomy
```

Compare with the core `holonomy_trace` command (see
**geometry_layout_reference**), which computes traces around
user-specified closed paths rather than the extender's stored
`holoBorder`.

---

### `disp` — Display branch point

Uses the packing's display subsystem to draw a specific branch
point or all branch points.

```
|GB| disp -b{n} {display_flags}
```

Accepts the standard display flags documented in
**disp_command**.

**Examples:**

```
# Display all branch points
|GB| disp

# Display branch point 2
|GB| disp -b2

# Display with specific color/style flags
|GB| disp -b1 -cf 220
```

---

## Typical workflow

```
# 1. Start with a seed packing of reasonable size
create hex 5
max_pack

# 2. Activate the |GB| extension on the active pack
act 0
extender gb

# 3. Place a traditional double branch point at vertex 1
|GB| bp_trad -a 4 -i 1

# 4. Repack to converge radii against the new aim
repack 2000

# 5. Check the angle-sum error
|GB| angsum_err

# 6. Check holonomy around the red chain
|GB| holonomy

# 7. Lay out and display
layout
disp -w -c

# 8. Display the event horizon
|GB| event -b1
disp -e elist

# 9. Check status
|GB| status

# 10. Try a different type at another location
|GB| bp_chap -a 4 -i 20 -j 5 15 -o 0.3 0.3
repack 2000
layout
disp -w -c

# 11. Revert to start over
|GB| revert
```

---

## Multiple branch points

Multiple branch points can coexist as long as their event
horizons do not overlap. Each is identified by its ID number
(assigned sequentially starting from 1).

```
# Create two branch points
|GB| bp_trad -a 4 -i 1
|GB| bp_chap -a 4 -i 30 -j 8 18

# Repack with both
repack 2000

# Check status of each
|GB| status -b1
|GB| status -b2

# Delete just the second one
|GB| delete -b2
repack 2000
```

---

## Notes

- The `-b{n}` flag can be written as `-b{n}` (no space) or
  `-b {n}` (with space). For example, both `-b2` and `-b 2`
  target branch point 2.
- **Aim values in `|GB|` commands are multiples of π**, not
  2π and not raw radians. So `-a 4` means an angle sum of 4π
  (= 2 × 2π, a double winding). See the
  [aim-convention note](#aim-convention-note) above.
- Overlap parameters should be in [0, 1]. Values of 1/3
  correspond to the symmetric case (equal distribution).
- Branch points cannot be placed where they would interfere
  with an existing branch point's event horizon. The extension
  checks for conflicts and rejects the placement if detected.
- After creating or deleting branch points, the packing
  generally needs to be repacked (**repack_command**) and laid
  out (**layout_command**) before the results are visible.
- `|GB|` commands operate on the *current* (possibly modified)
  packing. Use `|GB| revert` to restore the reference copy
  held from activation.

---

## Source

- `packExtensions.GenModBranching` — the extender class
  (`src/packExtensions/GenModBranching.java`).
  - `extensionType = "GENERALIZED_BRANCHING_MOD"` (the internal
    identifier; the `_MOD` suffix is a historical artifact).
  - `extensionAbbrev = "GB"` (the abbreviation matched by the
    `extender` command, case-insensitively).
  - `toolTip` describes it as "Generalized_Branching ...
    methods for incorporating various generalized branched
    points into the parent circle packing."
- Fields of interest:
  - `refPack` — the stored reference copy used by `revert`.
  - `branchPts : Vector<GenBrModPt>` — the list of active
    branch points, indexed from 1.
  - `holoBorder : HalfLink` — the red-chain border used by
    `holonomy`.
  - `exclusions : ArrayList<Vertex>` — vertices to avoid when
    choosing alpha during layout.
- `packExtensions.GenBrModPt` — the per-branch-point record,
  holding type (1, 4, or 6), aim, overlaps, jump petals, and
  associated subcomplex data.
- `cmdParser(cmd, flagSegs)` — handles the `-b{n}` flag
  upfront, then dispatches to per-command handlers by name.
- `input.CommandStrParser.extender` — the top-level command
  that starts this extender (via `str.equalsIgnoreCase("gb")`).
