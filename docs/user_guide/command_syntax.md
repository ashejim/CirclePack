# CirclePack Command Syntax

## Who this is for

This guide is for mathematicians learning CirclePack who are more
comfortable with mathematics than with command-line tools. If you've
used Mathematica, MATLAB, or Maple you've been typing expressions
like `f[x]` or `plot(sin(x))` — function-style calls. CirclePack is
different. It uses a **command-line style** inherited from Unix —
commands look like `disp -c a` rather than `disp("c", "a")`.

Once you know the patterns, CirclePack is actually quite compact.
This page covers the grammar once so you don't have to re-learn it
on every command page.

---

## One big idea: flag-based commands

A CirclePack command always starts with a **command name**, then
takes one or more **flag segments** each starting with `-`, followed
(usually) by objects to operate on:

```
command_name -flag1 args1  -flag2 args2  -flag3 args3
```

For example:

```
disp -w -c a -e a
```

This is read as "`disp`, three flag segments: `-w` (wipe the canvas),
`-c a` (circles, all), `-e a` (edges, all)." Each flag is independent;
they can appear in any order (except for a few that must come first,
like `-w`).

**A helpful mental translation:** if you're used to function calls,
a CirclePack command like `disp -c fc80 3 5 7` corresponds roughly
to a function call `disp(circles=[3, 5, 7], fill_color=80)`. The `-c`
names the parameter; `fc80` is compact sub-syntax for "fill with color
80"; `3 5 7` is the list of circles. But unlike a function call,
CirclePack's syntax is space-separated and there are no quotes or
parentheses.

### Anatomy of a flag segment

```
  -c     fc80n          3 5 7
   |       |              |
   |       |              list of objects (here, vertices/circles)
   |       display sub-flags (color/fill/thickness/label)
   flag letter (-c means "circles")
```

Not every flag takes all three parts. Some flags take only their
letter (e.g., `-w` just wipes the canvas, no args). Some take just
a list (`-a 1 2 3`). Many take all three. Each command's documentation
tells you which flags exist and what each can take.

### Dash conventions

- A **single** `-` followed by a letter is one flag: `-c`, `-f`, `-e`.
- Some flags have a built-in second character: `-wr` (wipe-and-redraw),
  `-nf` (face labels), `-dg` (dual edges from face graph), etc.
- Leading dashes on *object lists* are tolerated — `disp -c -b` is
  the same as `disp -c b`. This is a convenience; the parser assumes
  you typed an extra dash by accident.
- Upper- and lowercase matter. `disp` and `Disp` are related but
  different commands (see the `disp` reference). `-c` and `-C` on
  `disp` draw different things.

---

## Packings: why `p0`, `p1`, `p2`?

CirclePack maintains **three packing slots** simultaneously, named
`p0`, `p1`, and `p2`. Think of them as three independent workspaces —
like having three Mathematica notebooks open at once, each with its
own variables.

Most of the time you work with the **active** packing (shown
highlighted in the GUI). Commands apply to the active packing by
default. You can change which packing is active:

```
act 1                            # pack 1 is now active
```

and you can target a *specific* packing for a single command with
`-p{N}`:

```
repack -p1                       # repack just pack 1
disp -p2 -c a                    # display circles of pack 2
```

Most commands accept `-p{N}`. The `disp` command also accepts `-q{N}`
for something different: redirect the *output canvas* to pack N's
window without changing which packing's data is being drawn.

---

## Object lists

Many commands take a list of packing objects: vertices, faces, edges,
tiles. The list language is the same across commands — `a` means
"all," `b` means "boundary," `i` means "interior," integer sequences
are literal indices, and there's a rich vocabulary beyond that. See
the **List Specifiers** reference for the full vocabulary.

A few things worth knowing up front:

```
disp -c 3 5 7                   # circles 3, 5, 7
disp -c a                       # all circles
disp -c b                       # all boundary circles
disp -c i                       # all interior circles
disp -c b(3, 17)                # boundary arc from vertex 3 to 17
```

The symbols you'd expect (`,`, `:`, parentheses, brackets) mostly do
what you'd guess, but the grammar is rigid. Parentheses mean
*ranges*; square brackets are *selectors* on stored lists; curly
braces are *set-builder notation*.

---

## Multiple commands on one line: `;`

Separate commands with `;` to run them in sequence:

```
max_pack; repack; layout; disp -w -c a
```

This is exactly equivalent to typing each command on its own line.

---

## Loops: `for` and `FOR`

```
for (j:=0, 10, 1) disp -t1 j; delay 0.1
```

The for-loop syntax is:

```
for (variable := start, end, increment) command1; command2; ...
```

All four parts of the header are inside parentheses. The body is one
or more `;`-separated commands following the closing paren.
Variables defined in the for-header (`j` in the example) are
available inside the body via the variable-substitution syntax
(`_j`, see below).

Two variants exist:

- **`for`** — bounded loops. Hard-capped at **10 iterations**. If
  you try more, CirclePack will refuse and tell you to use `FOR`
  instead.
- **`FOR`** — uncapped loops. Use this for anything longer than 10.

The deprecated form `for n,m cmd` (no spaces, no parens, n and m
integers) is still accepted but new scripts should use the paren
form.

There's an optional `-d {x}` flag for a delay between iterations
(in seconds):

```
for (j:=1, 5, 1) -d 0.5 disp -c j
```

---

## Conditionals: `IF ... THEN ... [ELSE ...]`

```
IF {condition} THEN {named_command} ELSE {named_command}
```

The `THEN` and `ELSE` bodies are **named script commands**, not raw
command text — you reference them by name in brackets (see "Named
commands" below). The `ELSE` clause is optional; default is a no-op.

IF-THEN-ELSE nests only through named commands, not syntactically,
so there's no nested-paren support. In practice, most CirclePack
work uses `for` loops rather than conditionals.

---

## Variables: `:=` and `_`

CirclePack supports simple string-valued variables. To assign:

```
myverts := 1 3 5 7 9
```

This stores "1 3 5 7 9" in a variable called `myverts`. Variable
names can contain letters and digits but no spaces.

To **refer** to a variable, prefix its name with `_`:

```
disp -c _myverts
```

This substitutes the stored value, so it runs as
`disp -c 1 3 5 7 9`.

Common mistake: don't put `_` on the assignment side. `_x := 5` is
an error (the `_` prefix is reserved for referencing). Just
`x := 5`.

Variables can also be set from command return values. Commands that
return values support a query style with `?` (next section).

Note: variable substitution happens in the preprocessing pass, so
if a variable's value itself contains `_`-prefixed references,
those too will be substituted.

---

## Query / value commands: `?`

Some commands report information rather than modifying the packing.
These are "query" commands, prefixed with `?`:

```
?aim 5                           # what's the angle-sum aim for vertex 5?
?rad 5                           # what's the radius of vertex 5?
?count                           # various counts
?cent 5                          # center of vertex 5
?flower 5                        # petals around vertex 5
?f(z) 0.3 0.4                    # value of the packing function at (0.3, 0.4)
```

Query commands can also be embedded in other expressions by wrapping
them in braces `{...}`:

```
myrad := {?rad 5}
```

This assigns the result of `?rad 5` to the variable `myrad`.

Useful queries, listed in `CmdCompletion.txt`: `?aim`, `?anglesum`,
`?antip`, `?cent`, `?count`, `?energy`, `?flower`, `?f(z)`, `?gam(t)`,
`?invdist`, `?schwarz`, `?mark`, `?rad`, `?vert`, `?face`, `?edge`,
`?tile`.

---

## Repeat last command: `!!`

The token `!!` repeats whatever command was executed most recently.
This is handy for iterative experimentation:

```
rld                              # repack, layout, disp
!!                               # do it again
!!                               # and again
```

A few rules:
- `!!` is never itself stored as "the last command," so you can't
  get into a recursive loop.
- `!!` can appear multiple times in succession, but if it fails or
  is followed by a non-`!!` command, the sequence stops.

---

## Named commands: `[name]`

If you've loaded a **script** (a `.cps` or `.xmd` script file), it
defines named commands. Run one by its name in brackets:

```
[mySetup]                        # run the named command 'mySetup'
[]                               # run the next named command
                                 # in the loaded script
```

The empty-bracket form `[]` is useful inside scripts themselves —
it moves to the next entry in sequence. Named commands can refer
to other named commands, up to a recursion depth limit (currently
5) to prevent runaway loops.

You can also substitute variable values into a bracket reference:

```
[_mycmd]                         # run the command whose name is
                                 # stored in the variable 'mycmd'
```

---

## Delays: `delay {x}`

Pause execution for `x` seconds (max 10):

```
disp -w -c a; delay 0.5; disp -w -c fc80 a
```

Useful inside `for` loops when you want animated progression. See
also the `-d {x}` flag on `for`.

---

## Set-builder notation: `{...}`

CirclePack accepts set-builder expressions anywhere an object list
is expected. This is a powerful feature that mathematicians find
natural. The form is:

```
{target : specification1 connective specification2 ... }
```

Any flag that takes a vertex/face/tile list (most `disp` flags, most
`color` flags, etc.) accepts a `{...}` expression instead of an
explicit list.

### The six parts of a set-builder expression

1. **Outer curly brackets** `{...}` — this is how the parser
   recognizes the expression.
2. **Target object type**:
   - `v` or `c` — circles (vertices)
   - `f` — faces
   - `t` — tiles
   - (edges not yet supported)
3. **Target packing** (optional): `-p{N}` as a subscript-like
   modifier. Used for cross-pack selections. Usually omitted.
4. **Colon `:`** separating target from conditions.
5. **One or more specifications** comparing a measured quantity
   against a value or another quantity.
6. **Connectives** between specifications: `&&` (and), `||` (or),
   `!` (not). New-style alternatives: `.and.`, `.or.`, `.not.`

### Examples

```
disp -c fc {c: d .eq. 5}         # circles where degree equals 5
disp -c fc80 {c: d > 5}          # circles of degree > 5
disp -c {c: r < 0.1}             # circles with radius < 0.1
disp -f {f: m > 0}               # marked faces
color -c fc100 {c: b}            # all boundary circles, color 100
disp -c {c: d .eq. 6 && m .ne. 0}  # degree-6 AND marked
disp -c {c: d .lt. 5 || d .gt. 7}  # degree < 5 OR degree > 7
```

### Target quantities

Unary (just a property that's true or false):

| Token | Meaning |
|-------|---------|
| `b`   | on boundary |
| `i`   | interior |
| `?list` | appears in a stored list (e.g., `vlist`) |

Binary (numeric comparison):

| Token | Meaning |
|-------|---------|
| `a`   | aim (target angle sum) |
| `d`   | combinatorial degree (number of neighbors) |
| `m`   | mark (integer tag) |
| `r`   | radius |
| `s`   | actual angle sum |
| `t`   | tile type (with `-T` target) |
| `z`   | modulus of center (distance from origin); `ze` forces Euclidean |
| `x`   | plot flag |
| `X`, `Y`, `Z` | coordinates in xyz data |
| `epq` | Euclidean ratio r(p)/r(q) |
| `cpq` | hyperbolic ratio |

### Comparison symbols

CirclePack accepts both old and new styles:

| Old | New style | Meaning |
|-----|-----------|---------|
| `=`, `==` | `.eq.` | equals |
| `=<`, `<=` | `.le.` | less or equal |
| `<`  | `.lt.` | strictly less |
| `=>`, `>=` | `.ge.` | greater or equal |
| `>`  | `.gt.` | strictly greater |
|      | `.ne.` | not equal |

The `.xx.` "new-style" forms are preferred because `<` and `>` can
collide with flag parsing in some contexts.

### Gotcha: no logical grouping

Specifications inside `{...}` are evaluated **strictly left-to-right**
with no parentheses for grouping. So:

```
{c: d .eq. 5 && m .ne. 0 || b}
```

is processed as "(degree==5 AND marked)", then "OR boundary" applied
to the result — not as "degree==5 AND (marked OR boundary)." If you
need complex boolean logic, compute intermediate lists into stored
lists and combine them.

---

## Packing extensions (extenders): `|XX|`

CirclePack has many optional **extenders** — self-contained packages
that add new commands for specific research topics. Each extender
has a short code name in vertical bars (e.g., `|GB|` for Generalized
Branching, `|BF|` for Beurling Flow, `|BQ|` for Brooks Quad).

To use an extender, first **attach** it to a packing:

```
extender p0 GENERALIZED_BRANCHING_MOD
```

Once attached, the extender's commands are invoked with the `|XX|`
prefix:

```
|GB| bp_trad -a 4 -i 5           # create a traditional branch point
|GB| status                      # show branch point status
```

Extenders have their own help (usually via an extender-specific
help command). Each extender's documentation lives in its own user
guide file. Currently documented: `|GB|` (see `GB_Extension_Guide`).

---

## Scripts

A **script** is a `.cps` or `.xmd` file containing:

- Named commands (definitions you can invoke with `[name]`)
- An ordered sequence you can walk through with `[]` or the GUI's
  "NEXT" button
- Documentation text interleaved with commands

Loading a script is typically done via the GUI (`File → Load Script`
or the script tab). Once loaded, you can execute named commands
from the console exactly as if you'd typed them.

Scripts are the recommended way to capture an experiment so you can
re-run or share it. Many of the commands in `CmdCompletion.txt` are
tailored for script authorship (`script`, `infile_cmds`, `infile_read`,
`infile_path`).

---

## Messages, comments, and debugging

**Messages to yourself or the reader:**

```
msg "running experiment 3"
```

Prints to the message pane, useful as progress output inside a
loop or script.

**Debug output:**

```
debug d                          # toggle debug output
```

**Clearing the console:**

```
Cleanse                          # clear the console output
```

(Note the capital C: `Cleanse` clears; lowercase `cleanse` is a
packing-modifying command — easy to confuse.)

---

## Quotes for strings with spaces

Most command arguments are single words (flag letters, integers,
short sub-strings). If you need to pass a string containing spaces
— for a label, for instance — wrap it in double quotes:

```
disp -nl 7 "my origin"           # write "my origin" at vertex 7
```

Without the quotes, CirclePack would see `my` and `origin` as two
separate tokens.

---

## Putting it together: a sample session

Here's a typical CirclePack session using most of the features above:

```
# Load or create a packing
max_pack

# Set some things up
repack
layout

# A variable for convenience
center := 1

# Paint the flower of the center vertex in a specific color
color -c fc80 Iv _center

# Display the result
disp -w -c fc a -e a -nv a

# A small parameter sweep, with animation
for (r := 0.1, 1.0, 0.1)
  set_rad -q0 _r _center;
  repack;
  layout;
  disp -wr;
  delay 0.3

# Query a specific value
?rad _center

# Repeat the last command
!!

# Swap to a second packing for comparison
act 1
max_pack
repack; layout; disp -wr

# Use set-builder notation
disp -c fc160 {c: d .gt. 6}

# Clean up
act 0
```

---

## Differences from function-call languages

A few deliberate mental-model shifts for mathematicians coming from
Mathematica, Maple, or MATLAB:

- **No parentheses around arguments.** `disp -c a` not `disp(c, a)`.
- **No commas between arguments.** `disp -c 3 5 7` uses spaces.
- **Commas and parens *do* appear**, but they mean specific things:
  parens inside object lists mean *ranges* like `a(3,17)`; commas
  inside `for` headers separate `(start, end, increment)`; curly
  braces are set-builder notation.
- **Assignment is `:=`**, not `=`. Plain `=` appears inside `{...}`
  as a comparison operator.
- **Variables must be `_`-prefixed** when used, plain when assigned.
- **Commands return a "count" by default**, not a value object.
  Value-returning forms are the `?queries` and `{command}` bracket
  forms.
- **Flags can be reordered freely**, except where a command
  documents otherwise (e.g., `-w` must be first in `disp`).

---

## Notes & gotchas

- **Case matters**: `disp` vs `Disp`, `for` vs `FOR`, `cleanse` vs
  `Cleanse`. Always pay attention.
- **Leading `-` is forgiven on list tokens** but not on flag letters.
  `disp -c -b` is fine; `disp --c a` is not.
- **Whitespace is flexible** inside a command but is *significant*
  as a token separator. Two vertex indices `3 5` are two objects;
  `35` is one object.
- **`for` loops refuse more than 10 iterations** — use `FOR` for
  real work.
- **Set-builder has no operator precedence**; chain conditions only
  when you'd read them left-to-right.
- **Only three packings exist**: `p0`, `p1`, `p2`. Not `p3`.
- **Command preprocessing happens in a fixed order**: named-command
  substitution, quoted-string extraction, `-p?` pack selection, `:=`
  variable assignment, `_*` variable substitution, `!!` repeats,
  `for`/`FOR` expansion, `delay` handling, extender handoff (`|XX|`),
  then finally command execution. Most of the time you don't need
  to think about this, but occasionally an unexpected result comes
  from an earlier step rewriting your command before it runs.

---

## Related references

- **List Specifiers** — the full vocabulary (`a`, `b`, `i`, `Iv`,
  `vlist`, stored lists, etc.) for object lists in any command
  that takes them.
- **Individual command pages** — `disp`, `layout`, `repack`, etc.,
  each documenting their specific flags in detail.
- **Extender guides** — each extender (e.g., `|GB|`) has its own
  guide covering its commands.

---

## Source

- `input.TrafficCenter` — top-level command preprocessor (variable
  assignment, loops, `!!`, named commands, extender dispatch).
- `input.CommandStrParser` — individual command dispatch.
- `input.SetBuilderParser` and `util.SelectSpec` — the `{...}`
  set-builder grammar.
- `util.StringUtil.getForSpec`, `util.ForSpec` — `for`/`FOR` loop
  parsing.
- `util.StringUtil.varSub` — `_variable` substitution.
- `allMains.CPBase.NUM_PACKS` — pack-slot count (currently 3).
