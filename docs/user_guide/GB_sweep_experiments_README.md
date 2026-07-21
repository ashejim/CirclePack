# GB Sweep Experiment — Holonomy Error Landscape

## Overview

Maps the holonomy error as a function of branch point position on
an annular circle packing. A fixed traditional branch point is placed
at a chosen interior vertex, then a second branch point is swept over
a grid of (x,y) coordinates. At each point the packing is repacked
and the holonomy around the generating chain is measured.

## Files

- `src/ftnTheory/GBExperiments.java` — **NEW.** Experiment logic (4/15/2027).
- `src/ftnTheory/GenModBranching.java` — **MODIFIED.** Two changes (4/15/2027):
  1. `holonomy` command now outputs the Frobenius norm value
  2. New `sweep` command delegates to `GBExperiments`
- `plot_sweep.py` — Python script for 3D surface and contour plots.

## Installation

1. Copy `GBExperiments.java` into `src/ftnTheory/` alongside
   `GenModBranching.java`.
2. Replace `GenModBranching.java` with the modified version
   (or apply the two small changes manually — see below).
3. Rebuild CirclePack.

### Manual changes to GenModBranching.java (if preferred)

**Change 1** — In the `holonomy` command block (~line 302), add
after `double frobNorm=Mobius.frobeniusNorm(holomob);`:
```java
msg("holonomy_error: "+frobNorm);
```

**Change 2** — After the holonomy block, before the `delete` block, add:
```java
// =========== sweep ===================
if (cmd.startsWith("sweep")) {
    GBExperiments exp=new GBExperiments(this);
    return exp.runSweep(flagSegs);
}
```

**Change 3** — In `initCmdStruct()`, add:
```java
cmdStruct.add(new CmdStruct("sweep",
    "-v {vert} -a {aim1} -A {aim2} "+
    "-b {xmin xmax ymin ymax} -n {gridsize} -o {file}",null,
    "Sweep grid of (x,y) positions for second branch point. "+
    "Fixed traditional BP at vertex 'vert' with aim 'aim1'*Pi. "+
    "Sweep BP aim is 'aim2'*Pi. "+
    "Outputs CSV of (x,y,holonomy_error). "+
    "Defaults: aim1=aim2=4.0, grid=20, file=sweep_output.csv"));
```

## Usage

### 1. Create or load an annular packing

```
seed 8           # 8-petal flower
add_gen 4        # 4 generations
puncture 1       # remove center vertex to make annulus
geom_to_e        # euclidean geometry
repack; layout
```
'seed 8; add_gen 4; repack; layout; puncture 1; geom_to_h;' 


### 2. Start the |GB| extension

```
extender GB
```

### 3. Run the sweep

```
|GB| sweep -v 18 -a 4.0 -A 4.0 -b -0.5 0.5 -0.5 0.5 -n 30 -o holonomy_sweep.csv
```

Parameters:
- `-v 18`     : fixed traditional branch point at interior vertex 18
- `-a 4.0`   : aim = 4π at the fixed branch point
- `-A 4.0`   : aim = 4π at the sweep branch point
- `-b -0.5 0.5 -0.5 0.5` : sweep region [xmin,xmax]×[ymin,ymax]
- `-n 30`    : 30×30 grid (900 points)
- `-o holonomy_sweep.csv` : output file

### 4. Plot the results

```bash
python3 plot_sweep.py holonomy_sweep.csv --log --contour -o landscape.png
```

Options:
- `--log`     : log scale on z-axis (useful when errors span orders of magnitude)
- `--contour` : also produce a 2D contour plot
- `-o file`   : save to file instead of displaying
- `--title "My Title"` : custom plot title

## Key implementation notes

### holoBorder reinitialization
`revert()` swaps `extenderPD` with a fresh copy of `refPack`, but does
NOT reinitialize `holoBorder`. Since `holoBorder` is a `HalfLink` built
from `extenderPD.packDCEL`, the old link references stale half-edges.
`GBExperiments` reinitializes `holoBorder` after every `revert()`.

### Error handling
Grid points where branching fails (point outside packing, on boundary,
branch point creation fails, repack diverges, etc.) are silently
skipped and not included in the output CSV. The final summary reports
success/failure counts.

### Performance
Each grid point requires: revert + installBrPt + repack + layout +
installBrPt + repack + layout + holonomy computation. For small
packings this is fast. For a 30×30 grid on a ~200 vertex packing,
expect roughly 1–5 minutes depending on hardware.
