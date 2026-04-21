# CirclePack Session and Scripting Reference

## About this page

This page covers the **session-level commands** — the ones that
control CirclePack itself rather than the packings inside it:
switching between pack slots, changing directories, opening and
closing GUI windows, loading scripts, timing, debugging, and
quitting.

These commands don't do circle-packing mathematics — they
manage the environment the mathematics happens in. They're
indispensable for day-to-day use and essential for any script
longer than a few lines.

Commands covered: `act`, `copy`, `cd`, `pwd`, `open`, `close`,
`Map`, `chgScreen`, `chgPaired`, `script`, `scroll`, `msg`
(`message`), `timer`, `delay`, `debug`, `socketServer`, `quit`,
`exit`, `eval`, `evalp`.

Not covered here (but cross-referenced):
- `read` / `Read` / `write` / `Write` / `output` / `post` /
  `svg` / `screendump` — file I/O, documented in
  **io_reference**.
- `cleanse` / `Cleanse` — packing resets, documented in
  **combinatorial_editors_reference**.
- `set_var` — variable storage, documented in
  **setters_reference**.

---

## Sections

1. [Pack slot management](#pack-slot-management) — `act`, `copy`
2. [Filesystem navigation](#filesystem-navigation) — `cd`, `pwd`
3. [GUI windows](#gui-windows) — `open`, `close`, `Map`,
   `chgScreen`, `chgPaired`
4. [Scripts](#scripts) — `script`, `scroll`
5. [Messages and evaluation](#messages-and-evaluation) — `msg`,
   `eval`, `evalp`
6. [Timing and delay](#timing-and-delay) — `timer`, `delay`
7. [Debugging](#debugging) — `debug`
8. [Remote access](#remote-access) — `socketServer`
9. [Quitting](#quitting) — `quit`, `exit`
10. [Typical workflows](#typical-workflows)
11. [Notes and gotchas](#notes-and-gotchas)
12. [Source](#source)

---

## Pack slot management

CirclePack maintains three pack slots (`0`, `1`, `2`). One is
always **active** — commands that don't specify a target pack
operate on it.

### `act {n}` — Switch active pack

```
act 0                            # activate pack 0
act 1                            # activate pack 1
act 2                            # activate pack 2
```

Switches the active pack to slot `n`. In GUI mode, updates the
visible frame. In shell mode, updates `ShellControl`.

Returns `1` on success, `0` if `n` is out of range (must satisfy
`0 ≤ n < 3`).

### `copy {n}` — Copy active pack to another slot

```
copy 1                           # copy active pack to slot 1
copy 2                           # copy active pack to slot 2
```

Duplicates the currently active pack into slot `n`. The active
pack stays active; slot `n`'s old contents are replaced.

If `n` equals the current active pack, returns `1` without
action. Otherwise deep-copies via `PackData.copyPackTo()` and
installs the copy at slot `n`.

Returns `1` on success; throws a `ParserException` on failure.

### Pack-targeting from within commands

Most commands accept `-p{n}` as their first flag to target a
specific pack without switching the active one:

```
disp -p1 -wr                     # draw pack 1 even if active is pack 0
repack -p2                       # repack pack 2
```

Use `act` when you want to switch; use `-p{n}` when you want a
one-off.

---

## Filesystem navigation

CirclePack tracks several directory paths (see **io_reference**
for the full list). `cd` and `pwd` let you inspect and change
them interactively.

### `cd {dir}` — Change working directory

```
cd                               # go to home directory (~)
cd ~                             # same as above
cd ~/work/CirclePack             # home-relative path
cd /absolute/path                # absolute path
cd ../other                      # relative to current directory
cd scripts                       # relative subdirectory
```

Changes `CPFileManager.CurrentDirectory`. Path conventions:

| Prefix | Meaning |
|--------|---------|
| `~` or `~/...` | Home directory, then optional subpath. |
| `/...`         | Absolute path. |
| *other*        | Relative to current directory. |

If the target is a file (not a directory), `cd` uses the
containing directory instead.

**Side effects on related directories.** After changing, `cd`
inspects the new directory for subdirectories and auto-resets
linked paths:

| Subdirectory found | Path updated |
|--------------------|--------------|
| `scripts/` | `ScriptDirectory` |
| `packings/` | `PackingDirectory` |
| `pics/` | `ImageDirectory` |

This convention means that a well-organized project directory
with `scripts/`, `packings/`, and `pics/` subfolders gets all
three paths set correctly with one `cd` command.

Also updates the title bar of the main frame to show the new
directory.

Errors if the path doesn't exist.

### `pwd` — Print working directory

```
pwd
```

Prints `CurrentDirectory` to the message pane (via
`CirclePack.cpb.msg`). No arguments.

---

## GUI windows

CirclePack has multiple top-level windows (active-pack canvas,
paired canvas, script panel, preferences, etc.). `open` and
`close` control their visibility by name.

### `open {windows}` — Show GUI windows

```
open                             # default: show active canvas
open active                      # single-canvas view
open pair                        # dual-canvas "map pair" view
open script                      # script panel
open msg                         # message pane
open conf                        # preferences/configuration
open screen                      # screen-control frame (screenshots)
open fun                         # function panel
open mob                         # Möbius frame
open info                        # packing-info frame
open help                        # help window
open about                       # "about" splash
open sav                         # save/output frame
open adv                         # advanced (main) frame
```

Multiple window names can be combined:

```
open active script msg           # three windows at once
```

Name prefix-matches; common prefixes:

| Prefix | Window |
|--------|--------|
| `adv`  | Advanced / main frame |
| `act`  | Active-canvas frame |
| `pair` / `map` | Dual-pack (map pair) frame |
| `scre` | Screen-control frame (screenshots) |
| `msg` or `mes` | Message pane |
| `conf` | Preferences |
| `scr`  | Script panel |
| `fun`  | Function panel |
| `mob`  | Möbius frame |
| `inf`  | Info frame |
| `hel`  | Help window |
| `abo`  | About frame |
| `sav`  | Save / output frame |

Only works in GUI mode (errors silently in shell mode).

### `close {windows}` — Hide GUI windows

```
close                            # close advanced frame (default)
close active                     # iconify active-canvas frame
close pair                       # iconify map-pair frame
close script                     # hide script panel
close conf                       # close preferences (disposes it)
```

Same window-name vocabulary as `open`. Most windows become
iconified or invisible; `conf` (preferences) is fully disposed
and re-created on next `open`.

### `Map {p} {q} {flags}` — Control the map-pair view

```
Map 0 1                          # open dual-pack view with packs 0 and 1
Map 0 1 -o                       # same, explicit open flag
Map -x                           # close map-pair view
Map -o                           # open with current packings (no change)
Map -tYES                        # enable teleporting
Map -tNO                         # disable teleporting
```

The **map-pair view** is the dual-canvas mode used for visualizing
conformal maps — one packing on each side, with mouse motions on
one side mirrored ("teleported") on the other.

### Flags for `Map`

| Flag    | Meaning |
|---------|---------|
| `-o`    | Open (with current pack assignments) |
| `-x`    | Close |
| `-tYES` | Enable teleporting between the paired canvases |
| `-tNO`  | Disable teleporting |
| `p q`   | Pack numbers to assign to left and right |

### `chgScreen {n}`, `chgPaired {n}` — Resize canvases

```
chgScreen 600                    # resize main canvas to 600x600
chgPaired 400                    # resize each paired canvas to 400x400
```

Change canvas dimensions. Values are clamped to the allowed range:
- `chgScreen`: `[MinActiveSize, MaxActiveSize]`
- `chgPaired`: `[MinMapSize, MaxMapSize]`

Forces a relayout of the affected frames.

---

## Scripts

Scripts in CirclePack are text files containing sequences of
commands, typically with `.cps` or `.xmd` extensions. Scripts
support parameters, variables, loops, and control flow — see
**command_syntax** for the grammar.

### `script {filename}` — Load a script

```
script                           # open file chooser (GUI only)
script mytest.cps                # load by name (from ScriptDirectory)
script ~/work/analysis.cps       # full path
```

Loads a script into the script panel. The script doesn't run
automatically — it's loaded and ready for you to execute stages,
nodes, or the whole script via the script panel's controls.

If no filename is given and the GUI is running, pops up a file
dialog.

Returns the number of script items loaded (0 or negative on
failure).

### `scroll` — Reset script panel scrolling

```
scroll
```

Forces the script panel's scroll pane to revalidate and scroll
back to the top. Useful after programmatically modifying a script
in ways that confuse the panel's layout.

No arguments. Minor GUI utility; you're unlikely to need it
directly except in complex script-generation workflows.

---

## Messages and evaluation

### `msg` / `message` — Print to the message pane

```
msg Hello world
msg Packing loaded; ready to proceed
message Starting experiment
```

Prints the concatenated arguments to the message pane with a
space between each token. Useful for progress announcements in
scripts or when annotating interactive sessions.

Returns `1` on success; `0` if no arguments.

### `eval {z}` — Evaluate the current function at a complex point

```
eval 0.5 0.3                     # evaluate at z = 0.5 + 0.3i
```

Evaluates the current ParamParser function (set via `set_path` or
the function panel) at the given complex point. Prints the result
to the message pane.

Used for testing function definitions before running a script
that depends on them.

### `evalp {t}` — Evaluate the path at a parameter value

```
evalp 0.25                       # path value at t = 0.25
```

Evaluates the current path function at real parameter `t`. Prints
the result.

Useful for understanding parametric path definitions — what does
the path look like at t = 0, 0.5, 1?

---

## Timing and delay

### `timer` — Report elapsed time

```
timer                            # report time since last reset
timer -s                         # reset the timer
timer -x                         # reset the timer (same as -s)
```

Reports the time elapsed since the last reset via
`PackControl.cpTimer.singleTime()`. With `-s` or `-x`, resets
first (but still reports).

Useful for benchmarking: `timer -s; [commands]; timer` prints the
elapsed time for the commands in between.

### `delay {seconds}` — Sleep

```
delay 0.5                        # pause 0.5 seconds
delay 2                          # pause 2 seconds
```

Blocks the command thread for the specified number of seconds
(Java `Thread.sleep`). Useful in animation scripts to produce
visible frame delays:

```
# Show a sequence of packings with a visible delay
for (i:=1, 10, 1)
  set_rad _i 5;
  rld;
  delay 0.2                      # 0.2s between frames
```

Accepts fractional seconds. No upper limit enforced — `delay 60`
pauses a full minute.

---

## Debugging

### `debug` — Debugging controls

```
debug -e                         # echo every command to stderr before execution
debug -x                         # turn off command echoing
debug -s                         # print stackbox layout info
debug                            # default: -d (no-op)
```

Meta-commands for debugging CirclePack itself or scripts that
aren't behaving as expected.

| Flag  | Meaning |
|-------|---------|
| `-e`  | Enable command echoing to stderr before execution. Useful for tracing what a script does. |
| `-x`  | Disable command echoing. |
| `-s`  | Print stackbox layout info (script panel sizing). GUI-development diagnostic. |
| *(none)* | Default flag `d` — no-op. |

The echo mode (`-e`) is especially helpful for understanding
which step in a script is failing or slow.

---

## Remote access

### `socketServer {port}` — Start a socket server

```
socketServer                     # use default port (3736)
socketServer 5000                # use port 5000
```

Starts a CirclePack socket server that accepts command strings
over TCP. Useful for remote control or integration with other
tools (Python drivers, GUI front-ends, batch processing systems).

| Arg | Meaning |
|-----|---------|
| *(none)* | Use current `CPBase.cpSocketPort` if set, else port 3736. |
| *int*    | Use the specified port. |

Errors if a socket server is already running.

The socket server accepts the same command vocabulary as the
interactive command line; each connection can issue commands
asynchronously.

---

## Quitting

### `quit` — Exit CirclePack

```
quit
```

Prompts the user to confirm before exiting. In GUI mode, this is
a modal dialog. Returns `0` if not exited (user cancelled);
doesn't return if exit succeeds.

### `exit` — Same as `quit`

```
exit
```

Identical behavior to `quit` — both call `queryUserForQuit()`.

---

## Typical workflows

### Start a session with a project

```
cd ~/work/myProject              # auto-sets script/packing/pics dirs
pwd                              # confirm
open script msg                  # show script panel and message pane
script analysis.cps              # load the main script
```

### Multi-pack experiment

```
random_pack 100                  # in active pack (0)
copy 1                           # preserve in pack 1
flip 5 10                        # modify pack 0
rld                              # re-solve and draw
Map 0 1                          # open dual view to compare
act 1                            # switch back to original
disp -wr                         # redraw
```

### Benchmarking

```
timer -s                         # reset timer
max_pack 10000
timer                            # reports elapsed time

# Compare two approaches
timer -s
repack 5000
timer                            # first approach's time
timer -s
repack 5000 -o                   # Orick's method
timer                            # second approach's time
```

### Animation with delays

```
random_pack 50
rld

# Animate aim perturbation
for (t:=2.0, 8.0, 0.2)
  set_aim _t 5;                  # aim _t at vertex 5
  rld;
  delay 0.1
```

### Debugging a misbehaving script

```
debug -e                         # echo every command
script broken.cps
# ... watch stderr for the failing command
debug -x                         # stop echoing when done
```

### Multi-user / remote setup

```
socketServer 5000
# Now external tools can connect to localhost:5000
# and issue CirclePack commands
```

### Reorganize the workspace

```
close script msg                 # hide panels not needed
open active                      # single-canvas focused view
chgScreen 800                    # larger canvas
disp -wr
```

---

## Notes and gotchas

- **`act` is fast; `copy` is slow.** Switching active packs is
  near-instantaneous; copying a large packing takes time
  proportional to the packing size.

- **`cd` auto-updates linked directories.** If you `cd` to a
  folder containing `scripts/`, `packings/`, or `pics/`
  subfolders, those paths change too. If you don't want that,
  either use specific directory-setting commands or `cd` to a
  parent without those subfolders.

- **GUI-only commands fail silently in shell mode.** `open`,
  `close`, `Map`, `chgScreen`, `chgPaired`, `script` (with no
  filename), and others check `CPBase.GUImode` and return
  without effect if it's zero.

- **`open` and `close` use prefix matching.** `open sc` is
  ambiguous between `script` and `screen`; but `open scr` picks
  `script` and `open scre` picks `screen`. The source checks
  `scre` before `scr` explicitly to resolve this.

- **`script` doesn't execute the script automatically.** It just
  loads the file into the script panel. To run stages, use the
  panel's buttons or call individual script nodes via their
  execute mechanism.

- **`debug -e` output goes to `System.err`**, not the message
  pane. In the packaged app, this may be invisible unless
  CirclePack is launched from a terminal.

- **`delay` blocks the command thread.** During a `delay`, no
  other commands run. In GUI mode, this means the UI is frozen
  for the duration — don't `delay 60` in an interactive session
  expecting the GUI to stay responsive.

- **`timer` is a wall-clock stopwatch.** It measures elapsed time,
  not CPU time. Backgrounded processes or GUI events between
  `timer -s` and `timer` affect the result.

- **`socketServer` has no authentication.** Anyone who can reach
  the port can issue commands. Don't expose the port beyond
  `localhost` without a layer of network security.

- **`Map` argument order matters.** `Map 0 1` puts pack 0 on the
  left and pack 1 on the right. `Map 1 0` swaps them.

- **`quit` and `exit` don't quit silently.** They always query for
  confirmation via a dialog. For programmatic exit without a
  prompt, you'd need to modify CirclePack or exit the JVM
  externally.

---

## Source

- `allMains.CPBase` — holds `cpSocketHost`, `cpSocketPort`,
  `cpMultiServer`, `GUImode`, `NUM_PACKS`, `packings[]`,
  `cpDrawing[]`.
- `allMains.CPFileManager` — holds `CurrentDirectory`,
  `HomeDirectory`, `PackingDirectory`, `ScriptDirectory`,
  `ImageDirectory`.
- `allMains.PackControl` — the GUI singleton; `activeFrame`,
  `mapPairFrame`, `screenCtrlFrame`, `msgHover`, `scriptHover`,
  `prefFrame`, `newftnFrame`, `mobiusFrame`, `packDataHover`,
  `helpHover`, `aboutFrame`, `outputFrame`, `cpTimer`, and the
  `startCPSocketServer(port)`, `mapCanvasAction(boolean)`,
  `openMap(p, q)`, `switchActivePack(n)`, `queryUserForQuit()`
  methods.
- `allMains.ShellControl.switchActivePack` — shell-mode
  equivalent of `PackControl.switchActivePack`.
- `allMains.ScriptBundle` / `scriptManager` — script loading and
  panel management.
- `allMains.TrafficCenter.cmdGUI` — dispatches GUI-command
  strings.
- `input.CommandStrParser` — top-level dispatch for each of
  these commands.
