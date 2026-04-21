# CirclePack Documentation TODO

Collected during the `disp` / List Specifiers documentation pass.
These are documentation-layer changes — no functional code changes
required. They address a class of problem where user-facing parser
vocabularies are hidden inside helper classes and get overlooked when
documenting the commands that delegate to them.

## The underlying problem

Many CirclePack command handlers end with something like:

```java
NodeLink nodeLink = new NodeLink(p, items);
```

where `items` is a user-facing token string — `a`, `b`, `i`,
`b(3,17)`, `vlist`, `Iv 3 7`, etc. Each list class (`NodeLink`,
`FaceLink`, `EdgeLink`, `HalfLink`, `TileLink`, `GraphLink`,
`BaryLink`, `PointLink`, `PathLink`) parses its own mini-language.
Until these mini-languages are documented in one place and
cross-referenced from the command pages, every command that takes an
object list will silently have its documentation be incomplete.

---

## TODO 1 — Build out `list_specifiers.md` as a shared reference

Create `docs/user_guide/list_specifiers.md` (draft produced 2026-04)
and link every command that takes `{v..}`, `{f..}`, `{e..}`, `{t..}`,
bary-coord, or plane-point inputs to it.

**Why this helps:**
- Stops every command page from re-describing `a`, `b`, `i`, stored
  lists, set-builder, etc.
- Makes the vocabulary auditable — if new selection tokens are added
  to `NodeLink` / `FaceLink` / etc., there is a single canonical
  page to update.
- Gives future users a grep target. Right now a user wondering "what
  can I write after `-c`?" has no central answer.
- Makes commands composable in users' heads. Once they learn the
  vocabulary once, it applies everywhere. Today, users guess.

**Concretely:**
- Each flag section in `disp_command.md` (and future command pages)
  should have a line like *"Takes a vertex list (see List
  Specifiers)."*
- The List Specifiers page should cross-link back to specific
  command pages where relevant (e.g., how `-R` on `disp` relates to
  the `R` vertex token).

---

## TODO 2 — Rewrite class-level Javadoc on the `...Link` classes

Current state: the class-level comments on `NodeLink`, `FaceLink`,
`EdgeLink`, `HalfLink`, `TileLink`, etc. are minimal or truncated.
`NodeLink`'s literally reads `"Linked list for vertices associated
(generally) with a."` in the generated Doxygen PDF — the sentence
is cut off.

Replace each with something like:

> `NodeLink`: A list of vertex indices for a given packing. Beyond
> being a storage structure, `NodeLink` parses user-facing token
> strings (`a`, `b`, `i`, `b(v1,v2)`, `vlist`, `Iv {f..}`, etc.)
> into concrete vertex lists. For the full token vocabulary see the
> List Specifiers reference in the user guide; for the parser entry
> point see `addNodeLinks(Vector<String>)`.

**Why this helps:**
- Makes the Doxygen-generated API docs self-describing. Someone
  reading class docs sees immediately that there's a user-facing
  parser and knows where to look.
- Prevents the "looks like a plumbing class" illusion that made me
  skip past it. Anyone using the Doxygen output (human or AI) now
  has a breadcrumb.
- Costs nothing at runtime and survives refactoring as long as the
  class name doesn't change.

**Classes to update:**
- `listManip.NodeLink`
- `listManip.FaceLink`
- `listManip.EdgeLink`
- `listManip.HalfLink`
- `listManip.TileLink`
- `listManip.GraphLink`
- `listManip.BaryLink`
- `listManip.PointLink`
- `listManip.PathLink`
- `util.DispFlags` (similar issue — the compact color/fill/thickness
  encoding is a mini-language too)
- `util.SetBuilderParser` (the `{...}` notation that every list
  type accepts)

---

## TODO 3 — One-line comments at call sites

Optional but cheap. Every `new NodeLink(p, items)` /
`new FaceLink(p, items)` / etc. construction inside a command
handler could get a trailing comment like:

```java
NodeLink nodeLink = new NodeLink(p, items);  // items: NodeLink token
                                             // string; see List
                                             // Specifiers
```

**Why this helps:**
- Makes the delegation visible at the call site, where
  documentation writers actually look.
- Takes ~5 seconds per call site and never goes stale.
- Lowest-priority of the four — the real win is TODO 1 and TODO 2.
  Skip this if time is tight.

**Where to apply:**
- `canvasses.DisplayParser.dispParse` — many call sites
- `input.CommandStrParser.jexecute` — many call sites
- Anywhere else a command handler constructs a `...Link` object
  directly from user tokens

---

## TODO 4 — Doc workflow rule for future command pages

Adopt a rule: before declaring a command's documentation complete,
list its **delegated parsers** — every class whose parser consumes
user input on behalf of this command. Each such parser must be
either documented inline on the command page or cross-referenced to
its own reference page.

For `disp`, the delegated parsers are:
- `NodeLink`, `FaceLink`, `EdgeLink`, `HalfLink`, `TileLink`,
  `GraphLink`, `BaryLink`, `PointLink` (object lists)
- `DispFlags` (color/fill/thickness encoding)
- `SetBuilderParser` (the `{...}` notation)
- `StringUtil` helpers (`get_paren_range`, `get_bracket_strings`,
  `get_int_range`)

**Why this helps:**
- Turns "did I document everything?" into a concrete checklist
  rather than a vague inspection.
- Catches the delegation pattern systematically rather than by luck.
- Forces command pages to surface all of the mini-languages a user
  needs to know about in one place.

**Implementation:**
- Add a "Delegated parsers" checklist to the command-docs template.
- Include "delegated parsers documented?" in the doc review rubric.
- Optionally: a short section at the bottom of each command page
  ("## Source and parsers") that names the classes involved, so
  readers have an entry point into the code.

---

## Related — things worth noting but not in scope here

- Some `...Link` classes have overloaded constructors that accept
  both `String` and `Vector<String>`. Both paths end up in
  `addNodeLinks`-style methods. The user-facing parser is the same
  either way, but constructor selection is a Java detail worth a
  comment.
- `DispFlags` has its own mini-language (covered in `disp_command.md`
  already) but could eventually move to a shared "display styling"
  reference if it gets reused outside `disp`.
- `SetBuilderParser` (the `{...}` notation) is accepted by every
  list class but is itself undocumented at the user level. It
  deserves its own reference page eventually — parallel to List
  Specifiers.
