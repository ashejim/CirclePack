# CirclePack `color` Command — Set Colors on Packing Objects

## Overview

`color` sets the **stored colors** on circles, faces, edges, and
tiles. These stored colors are what `disp` reads when you draw with
`fc` or `cc` (fill or colored border) and don't specify an explicit
color code. Typical workflow:

1. Compute or solve the packing (`repack`, `layout`).
2. Use `color` to annotate objects — by property, by a function, by
   comparison with another packing, or by a fixed code.
3. Use `disp -c fc a` (or similar) to display with the stored
   colors.

`color` is passive about display: it modifies the stored color
values and returns, but doesn't redraw. You need `disp -wr` or
`disp -w -c fc a` (etc.) to see the result.

---

## Synopsis

```
color -c {spec} {v..}           # circles (also: -v for "vertex")
color -f {spec} {f..}           # faces
color -e {spec} {v1 w1 ...}     # edges
color -T {spec} {t..}           # tiles
color -D {spec} {t..}           # dual tiles
color -Q {spec} {t..}           # quad tiles
```

The object-type flag (`-c`/`-v`/`-f`/`-e`/`-T`/`-D`/`-Q`) selects the
object family. The first argument after the flag is a **color
specification** (a letter code or an integer), and the remaining
arguments are a standard object list (see **List Specifiers**).

---

## The CirclePack color table

CirclePack uses a **256-entry color table** (indices 0–255). All
color references — both inside `color` commands and inside `disp`
sub-flags like `fc80` or `cc200` — resolve through this table.

### Structure

| Range | Contents |
|-------|----------|
| 0     | **Foreground** (default: black). Also `fg`. |
| 1–99  | Blue ramp (light to dark blue), graduated. |
| 100   | **White** (middle of the blue-red ramp). |
| 101–199 | Red ramp (pink to dark red), graduated. |
| 200–208 | Mixed grayscale / accent slots. |
| 201   | Light pink |
| 202   | Light blue |
| 203   | Light green |
| 204   | Light orange |
| 205   | Light purple |
| 206   | Dark green |
| 207   | Purple |
| 208   | Lime |
| 209   | Yellow |
| 210   | Red |
| 211–217 | Orange ramp (dark to light) |
| 218   | Bright green |
| 219   | Forest green |
| 220   | Turquoise |
| 221   | Cyan |
| 222   | Sky blue |
| 223   | Cadet blue |
| 224   | Tan / sienna |
| 225   | Sienna |
| 226   | Salmon |
| 227   | Orange |
| 228   | Plum |
| 229   | Magenta |
| 230   | Light grey |
| 231–254 | Dark grey ramp (graduated) |
| 255   | **Background** (default: white). Also `bg`. |

The blue-red ramp (indices 1–199) is set up by `blue_to_red_ramp()`
at class-load time and is designed for numerical gradations — useful
when you want "negative → blue, zero → white, positive → red"
semantics.

### Two special color codes

- **`fg`** — current foreground color (index 0).
- **`bg`** — current background color (index 255).

Both can be used wherever a numeric code is accepted. They respond
to the user's GUI foreground/background settings, so scripts using
`fg`/`bg` adapt to whatever theme is active.

---

## `-c` (or `-v`) — Color Circles

Set the stored color on each listed circle.

```
color -c {spec} {v..}
color -v {spec} {v..}        # synonym; "vertex" instead of "circle"
```

### Specifications

| Spec | Meaning |
|------|---------|
| *integer 0–255* | Explicit color code from the table. |
| `fg`   | Foreground color. |
| `bg`   | Background color. |
| `d`    | **Color by degree** (number of neighbors). 5-degree = blue, 6 = white, 7 = red; other degrees are mapped around that center. |
| `a`    | **Color by argument of center** — uses the argument (θ) of each circle's center as an angle on a color wheel. |
| `s`    | **Spread** — cycle through distinct colors (one per vertex, good for distinguishing objects). |
| `S`    | Same spread color for every vertex in the list (one color for the group). |
| `s0` / `S0` | Reset the spread counter to 0 before starting. |
| `rad`  | Color by radius comparison within the current packing. |
| `q {p}` or `q{p}` | Compare to packing `p`, color by **radius ratio** (default). Both packings must be same geometry. |
| `q {p} angsum ...` | Compare to packing `p` by **angle sum** instead. |
| `p {p}` or `p{p}` | **Copy** colors from packing `p`'s circles. |

### Examples

```
color -c 80 a                   # all circles in color 80 (green)
color -c 210 b                  # all boundary circles in red
color -c fg i                   # all interior circles in foreground color
color -c d a                    # color every circle by its degree
color -c s a                    # spread — each circle a different color
color -c S 1 2 3 4              # one shared color (next in spread) for 4 circles
color -c s0 a                   # spread starting from color 0
color -c a a                    # color wheel by argument of center
color -c q 1 a                  # compare radii with pack 1, color all circles
color -c q 1 angsum a           # compare angle sums with pack 1
color -c p 2 a                  # copy all circle colors from pack 2
color -c rad                    # radius-based coloring within current pack
color -c 200 {c: d .gt. 6}      # color circles of degree > 6 (set-builder)
color -c 80 Iv 5 12             # color the petal-neighbors of verts 5 and 12
```

---

## `-f` — Color Faces

Set the stored color on each listed face.

```
color -f {spec} {f..}
```

### Specifications

| Spec | Meaning |
|------|---------|
| *integer 0–255* | Explicit color code. |
| `fg` / `bg` | Foreground / background. |
| `area` | **Color by relative face area** within this packing. |
| `s`    | Spread (distinct color per face). |
| `S`    | One shared spread color for the whole list. |
| `s0` / `S0` | Reset spread counter. |
| `x`    | **3D/2D area ratio** — compare Euclidean face area to 3D area (requires `xyzpoints` data to be present; sets colors via a Richter-red ramp). |
| `a`    | Color by argument of incircle center. |
| `q {p}` or `q{p}` | Compare **area** with packing `p` (same geometry required). Results color faces using ramp. |
| `p {p}` or `p{p}` | Copy face colors from packing `p`. |
| `qc`   | **Quasi-conformal** coloring — dispatches to `color_qc`, which colors faces by quasi-conformal distortion. Consumes the remaining flag-segment. |

### Examples

```
color -f 80 a                   # all faces in color 80
color -f area a                 # color faces by their area
color -f s a                    # spread of colors across faces
color -f S {f: b}               # one color for all boundary faces
color -f q 1 a                  # area comparison with pack 1
color -f p 2 Iv 5               # copy face colors from pack 2, for faces at vert 5
color -f qc                     # quasi-conformal coloring
color -f a A                    # color by incircle argument, on alternating faces
```

---

## `-e` — Color Edges

Set the stored color on each listed edge.

```
color -e {spec} {v1 w1 v2 w2 ...}
```

### Specifications

| Spec | Meaning |
|------|---------|
| *integer 0–255* | Explicit color code. |
| `fg` / `bg` | Foreground / background. |
| `r`    | **Reset** edge colors to foreground. |
| `s` / `S` / `s0` / `S0` | Spread of colors (per-edge or shared). |
| `z`    | **Color by schwarzian** — builds a blue-red ramp over all interior edges' schwarzian values and applies colors based on each edge's schwarzian. Useful for visualizing discrete conformal distortion. |

### Examples

```
color -e 210 b                  # boundary edges in red
color -e r a                    # reset all edges to foreground
color -e s a                    # spread colors across all edges
color -e z i                    # schwarzian-based coloring on interior edges
color -e 80 Iv 5                # all edges from vertex 5 in color 80
color -e 100 Ra                 # all red-chain edges in color 100
```

---

## `-T` / `-D` / `-Q` — Color Tiles

Set the stored color on each listed tile. Variants target different
tile data structures:

- **`-T`** — the packing's primary `tileData` (tiles).
- **`-D`** — the `dualTileData` (dual tiles).
- **`-Q`** — the `quadTileData` (quad tiles).

All three take the same specifications and list syntax. The packing
must have the corresponding tile structure — `color -T` is a no-op
if `tileData` is null, and similarly for the others.

```
color -T {spec} {t..}
color -D {spec} {t..}
color -Q {spec} {t..}
```

### Specifications

| Spec | Meaning |
|------|---------|
| *integer 0–255* | Explicit color code. |
| `fg` / `bg` | Foreground / background. |
| `v`    | Copy color from each tile's **baryCenter vertex**. |
| `d`    | Color by **number of corners** (tile "degree"). |
| `s` / `S` / `s0` / `S0` | Spread of colors. |

### Examples

```
color -T 80 a                   # all tiles in color 80
color -T v a                    # tiles take colors from their baryCenter vertex
color -T d a                    # color tiles by how many corners they have
color -T s a                    # spread for distinguishability
color -D 210 a                  # dual tiles in red
color -Q 221 a                  # quad tiles in cyan
```

---

## Typical workflow

```
# Max-pack, lay out
max_pack; repack; layout

# Color by structure: circles by degree, faces by area
color -c d a
color -f area a

# Display with stored colors
disp -w -c fc a -f fc a -e a

# Now color specific features:
# boundary circles red, an interior flower orange
color -c 210 b
color -c 227 Iv 5
disp -wr

# Visualize a mapping: color this pack's faces from pack 1
color -f p 1 a
disp -wr

# Schwarzian-based edge coloring for a conformal map visualization
color -e z i
disp -w -e a -c a

# Spread for distinction (e.g., to tell copies of tiles apart)
color -T s0 a
disp -w -T fc a
```

---

## Tips & common patterns

**Colors persist until changed.** Once set by `color`, a stored color
lasts through most operations. Re-packing or laying out doesn't wipe
them. `copy` between pack slots carries colors along.

**`disp -c fc a` vs `disp -c fc80 a`.** The first uses whatever
colors `color` has set; the second overrides with color 80. Plan
your `color` pass if you want scheme-based display without keying
a color into every `disp` command.

**Spread is stateful.** `color -c s a` cycles through colors using
an internal counter. If you want reproducible spread colorings
across runs, reset with `s0` first.

**Comparison coloring (q) pairs with a second packing.** The `q` and
`p` forms are designed for visualizing *maps* between two packings
— solve a boundary-value problem in pack 0 and a reference in pack
1, then `color -c q 1 a` shows where radii agree or disagree.

**Schwarzian coloring (`-e z`) is calibrated to all interior
edges.** The blue-red ramp is built over *every* interior edge's
schwarzian, then applied to your chosen list. So the color of a
given edge is meaningful relative to the whole packing, not to
your edge list.

**Use set-builder for structural coloring.** `color -c 210 {c: d .eq. 5}`
colors all degree-5 circles red in one line.

---

## Relationship to other commands

- **`disp`** reads the colors set here. `disp -c fc a` (fill using
  stored colors) is the main consumer.
- **`color_qc`** is invoked internally by `color -f qc`. It handles
  quasi-conformal-distortion coloring as a sub-command.
- **`set_brush`** sets the current drawing color temporarily for
  manual drawing (not the stored colors here).
- **`screendump`** / PostScript output (`set_ps`, `post`, `output`)
  captures whatever `disp` drew, including these colors.
- **`copy`** carries stored colors along with the packing data.
- **`mark`** and `color` are independent — marking a vertex doesn't
  color it. But you can use marks to drive coloring:
  `color -c 210 {c: m .ne. 0}` colors all marked circles red.

---

## Notes & gotchas

- **Colors are stored per-object, not global.** Each circle, face,
  edge, and tile has its own color slot. `color -c 80 a` sets all
  256 circle colors (say) to code 80 individually.
- **`-c` and `-v` do the same thing.** Both mean "color circles."
  `-v` is a mnemonic for "vertex" if you prefer.
- **Geometry mismatch aborts comparison.** `color -c q 1 a` requires
  the current pack and pack 1 to be the same geometry (both
  Euclidean, both hyperbolic, etc.). Otherwise it throws an error.
- **Tile coloring requires the tile structure to exist.** `color -T`
  silently returns 0 if `tileData == null`.
- **Integer codes outside [0, 255] error out.** So do malformed
  letter codes — `color -c xx a` will throw "Error in color code."
- **Spread resets on `s0`/`S0`, not between commands.** Running
  `color -c s a` twice in a row produces different colorings the
  second time unless you preface with `s0`.
- **`-e z` (schwarzian) only makes sense with a tangency packing
  whose schwarzians have been computed.** Check with `set_schwarzian`
  first.

---

## Source

- `packing.PackData.color_circles()` — circles dispatch
  (`src/packing/PackData.java`).
- `packing.PackData.color_faces()` — faces dispatch.
- `packing.PackData.color_edges()` — edges dispatch.
- `packing.PackData.color_tiles()` — tiles dispatch.
- `util.ColorUtil` — the color table and helpers:
  `blue_to_red_ramp`, `coLor(int)`, `spreadColor(int)`,
  `colorByDegree(int)`, `ArgWheel(double)`,
  `blue_red_color_ramp`, `richter_red_ramp`, `cloneMe`, etc.
- `util.ColorCoding` — holds `face_area_comp`, `h_compare_area`,
  `e_compare_area`, `setXYZ_areas`, and the more specialized
  coloring routines.
- `input.CommandStrParser` — the top-level `color` case
  dispatch.
