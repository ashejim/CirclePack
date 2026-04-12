# CirclePack `|GB|` Extension — Generalized Branching

## Overview

The `|GB|` (Generalized Branching) extension provides commands for
incorporating **generalized branch points** into a circle packing.
A generalized branch point is a small subcomplex within the
triangulation where local modifications (to angle sums, overlaps, or
combinatorics) produce a global effect analogous to a classical
branch point of an analytic function.

The extension maintains a reference copy of the original packing
(`refPack`) so that experiments can be reverted. Branch points are
numbered by ID starting from 1.

## Activating the Extension

```
extender p0 GENERALIZED_BRANCHING_MOD
```

This attaches the `|GB|` extender to packing `p0` and stores a
reference copy of the current packing data.

## Branch Point Types

### Traditional (`bp_trad`, type 1)

The simplest kind: a single vertex `v` is given an angle-sum target
greater than 2π (e.g. 4π for a double winding). The subcomplex is
just the flower of `v`.

### Singular (`bp_sing`, type 4)

The branch point lives in the **interstice** of a single face
`{v, u, w}`. The extra 2π of winding is distributed among the three
vertices via overlap parameters. The event horizon is the link of
the face's edges.

### Chaperone (`bp_chap`, type 6)

The flower of vertex `v` is augmented with a "sister" vertex and
two "chaperone" circles. The chaperone construction uses two
designated "jump petals" and overlap parameters to control the
geometry. This is the most flexible type and can model fractional
winding.

---

## Commands

### Creating Branch Points

#### `bp_trad` — Traditional branch point

Create a classical branch point at a vertex.

```
|GB| bp_trad -a {aim} -i {vertex}
```

| Flag | Description |
|------|-------------|
| `-a {aim}` | Target angle sum as a multiple of π (e.g. `4` means 4π) |
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

#### `bp_sing` — Singular branch point

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

#### `bp_chap` — Chaperone branch point

Create a branch point at a vertex using the chaperone construction.

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

#### `click` — Interactive branch point placement

Click a point in the plane to automatically determine the type of
branch point and create it. CirclePack examines whether the point
is near a vertex (→ traditional or chaperone), inside a face
(→ singular), or on an edge.

```
|GB| click {x y}
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

### Managing Branch Points

#### `delete` — Remove a branch point

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

#### `revert` — Restore original packing

Discards all branch points and reverts the packing to the stored
reference copy.

```
|GB| revert
```

---

#### `copy` — Copy reference packing

Copy the original (unmodified) reference packing to another pack
slot.

```
|GB| copy {pnum}
```

**Examples:**

```
# Copy the original packing to pack slot 2
|GB| copy 2
```

---

### Querying State

#### `status` — Report branch point status

```
|GB| status
|GB| status -b{n}
```

Reports the type, vertex/face, holonomy error, and other data for
the specified branch point (or all branch points). Also reports the
parent packing's overall angle-sum error.

**Examples:**

```
# Status of all branch points
|GB| status

# Status of branch point 1 only
|GB| status -b1
```

---

#### `angsum_err` — Report angle-sum error

```
|GB| angsum_err
```

Reports the L² angle-sum error of the current packing (the sum of
squared deviations of actual angle sums from their aims).

---

#### `get_param` — Display parameters

```
|GB| get_param -b{n}
```

Displays the current parameters for the specified branch point (aim,
overlaps, jump petals, etc., depending on type).

**Examples:**

```
# Get parameters for branch point 1
|GB| get_param -b1

# Without -b flag, defaults to branch point 1
|GB| get_param
```

---

#### `event` — Append event horizon edges

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

---

### Adjusting Parameters

#### `set_param` — Set parameters

```
|GB| set_param -b{n} {parameters}
```

Sets type-specific parameters for the designated branch point. The
parameter format depends on the branch type.

**Examples:**

```
# Set parameters for branch point 1
|GB| set_param -b1 0.3 0.5

# Adjust aim for branch point 2
|GB| set_param -b2 -a 6
```

---

#### `reset_overlaps` — Reset overlap values

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

### Layout and Display

#### `holonomy` — Check holonomy

Computes the holonomy Möbius transformation around the packing's
red chain. The Frobenius norm of the result measures how far the
packing is from closing up properly. A norm near zero indicates
the packing is consistent.

```
|GB| holonomy
```

---

#### `disp` — Display branch point

Uses the packing's display subsystem to draw a specific branch
point or all branch points.

```
|GB| disp -b{n} {display_flags}
```

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

## Typical Workflow

```
# 1. Start with a hex packing of 5 generations
max_pack
repack

# 2. Attach the GB extender
extender p0 GENERALIZED_BRANCHING_MOD

# 3. Place a traditional double branch point at vertex 1
|GB| bp_trad -a 4 -i 1

# 4. Repack with the branch point
repack 2000

# 5. Check the angle-sum error
|GB| angsum_err

# 6. Check holonomy around the boundary
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

## Multiple Branch Points

Multiple branch points can coexist as long as their event horizons
do not overlap. Each is identified by its ID number (assigned
sequentially starting from 1).

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

## Notes

- The `-b{n}` flag can be written as `-b{n}` (no space) or `-b {n}`
  (with space). For example, both `-b2` and `-b 2` target branch
  point 2.
- Aim values in commands are multiples of **π**, not 2π. So `-a 4`
  means an angle sum of 4π (= 2 × 2π, a double winding).
- Overlap parameters should be in [0, 1]. Values of 1/3 correspond
  to the symmetric case (equal distribution).
- Branch points cannot be placed where they would interfere with an
  existing branch point's event horizon. The extension checks for
  conflicts and rejects the placement if detected.
- After creating or deleting branch points, the packing generally
  needs to be repacked (`repack`) and laid out (`layout`) before
  the results are visible.
