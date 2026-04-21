# CirclePack User Guide — Working Notes

Notes for collaborating with Claude on the CirclePack user guide.
Keep this in the project folder and re-upload it at the start of any
new chat session so the working agreements carry over.

---

## Project goal

Produce a complete CirclePack user guide, starting from the existing
`GB_Extension_Guide.md` style and aiming long-term for something
closer in structure and quality to
[the scikit-learn user guide](https://scikit-learn.org/stable/user_guide.html):
prose-driven sections, clear examples, a real reference website
rather than a pile of unconnected command entries.

## Source material

Primary sources Claude has access to:

- The `ashejim/CirclePack` GitHub fork (forked from `kensmath/CirclePack`).
- Doxygen HTML output in `docs/html/`.
- Doxygen LaTeX output / generated `refman.pdf` (the PDF contains
  full source-listing appendices for every `.java` file — this is
  often more useful than the class-level API docs).
- `CmdCompletion.txt` at the repo root — master list of ~250
  commands.
- `docs/user_guide/` — where finished user-guide files go. Currently
  has two WIP files; style reference is `GB_Extension_Guide.md`.

## What to upload at the start of a session

Minimum to reproduce the current working style:

1. `GB_Extension_Guide.md` — the style reference.
2. `refman.pdf` — the Doxygen-generated full-source PDF. Heavy but
   the single most useful file.
3. This README.
4. Any existing user guide files relevant to the current task.

For specific commands, additional files helpful:

- The relevant `.java` source file(s). Often just pointing Claude
  at the right class name in the PDF is enough, but direct source
  is faster to search.

---

## Working agreements (things Claude agreed to do)

These came out of a "why was this missed?" debrief after the List
Specifiers gap was discovered during the `disp` pass. They are
pragmatic rules, not hard constraints.

### 1. Enumerate delegated parsers before declaring a command page complete

For any command handler of the form `new X(p, items)`, list `X` as
a **delegated parser** — a class whose own user-input grammar is
part of the command's surface area. Every delegated parser gets
either documented inline on the command page or cross-referenced to
its own reference page.

For `disp`, the delegated parsers were: `NodeLink`, `FaceLink`,
`EdgeLink`, `HalfLink`, `TileLink`, `GraphLink`, `BaryLink`,
`PointLink`, `DispFlags`, `SetBuilderParser`. Missing any of these
leaves a hole.

Before a doc is "done," Claude should produce a delegated-parsers
list. If any parser on the list is neither documented nor
cross-referenced, the doc isn't done.

### 2. Treat `new X(p, items)` as a red flag during source reading

Any time a command handler constructs a helper object directly from
user tokens, stop and ask "what strings can those tokens be?" If
the answer isn't obvious, the parser's vocabulary must be
investigated — not treated as an implementation detail.

This is the rule that would have prevented the
`-c b`-means-"boundary" oversight in the first pass.

### 3. Audit examples against the grammar, not the other way around

When writing examples, don't stop at "an example I've seen work."
For every flag segment, check: does each token in the example come
from an enumerated vocabulary? If I can't point to where `b` comes
from in my reference, the example is a liability, not an asset.

### 4. Preserve user corrections as lessons

When the user catches an error (like the `-c b` / `-f b` gap),
don't just fix the line — produce a debrief: what was missed, why,
what pattern to watch for. Those debriefs go into the project's
`DOCUMENTATION_TODO.md` and this README is updated if the fix
requires a new working agreement.

---

## Style conventions for user-guide pages

Based on `GB_Extension_Guide.md`:

- **Overview** at top — what the command/extension does in plain
  prose, why it exists, what mental model to carry.
- **Activation** if relevant.
- **Synopsis** — one-block summary of the syntax.
- **Flag/option tables** for each sub-command or flag cluster.
- **Examples, generously** — every flag gets at least one worked
  example, ideally several including the common "all" / "boundary"
  / "interior" patterns.
- **Typical workflow** section — a realistic sequence of commands
  showing how the feature fits into a session.
- **Notes & gotchas** at the end.
- **Source** section listing the `.java` classes — gives the reader
  (and future us) a breadcrumb into the code.
- Cross-reference related commands inline and in a closing
  "Relationship to other commands" section when there are multiple
  related features.

Formatting:

- Code blocks for command examples use no language tag — they're
  CirclePack-script lines, not Java or bash.
- Flag tables use pipe-delimited markdown.
- Inline commands wrapped in backticks: `disp -c a`.
- File paths wrapped in backticks: `src/canvasses/DisplayParser.java`.

---

## Current state (update as we go)

**Complete:**

- `disp_command.md` — covers `disp` / `Disp` / `dISp` / `DISp`,
  all preamble flags, all main flag letters (~20), the DispFlags
  inner language. Cross-links to List Specifiers.
- `list_specifiers.md` — shared reference for the list-specifier
  vocabulary across NodeLink / FaceLink / EdgeLink / HalfLink /
  TileLink / GraphLink / BaryLink / PointLink / PathLink.

**In draft upstream:**

- `GB_Extension_Guide.md` — the style reference. Not authored by
  this pass; kept as-is.

**Not yet done:**

- `layout`, `color`, `repack`, `set_disp_flags`, `screendump`
  (natural next targets given `disp`'s cross-references).
- All other core commands (~240 of them in `CmdCompletion.txt`).
- Every other extender besides `|GB|`.
- Set-builder notation reference (parallel to List Specifiers).

**Outstanding documentation TODOs:** see
`DOCUMENTATION_TODO.md` — Javadoc rewrites on `...Link` classes,
call-site comments, etc.

---

## Known gotchas from the project so far

- **The Doxygen PDF has `??` where cross-references failed to
  resolve.** Don't trust "page ??" — use text search to find real
  class references.
- **GitHub tree-view URLs are blocked by robots.txt** for fetch
  tools. Individual file URLs work. The path of least resistance is
  uploading files into the chat rather than asking Claude to fetch
  them.
- **Class-level Javadocs are often truncated mid-sentence** in the
  generated PDF (e.g., NodeLink). Don't trust them as complete
  summaries.
- **Some user-facing vocabulary is hidden in method bodies** not
  exposed by Doxygen. When in doubt, find the source listing in the
  back half of `refman.pdf` and read the actual Java.
