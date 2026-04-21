---
title: Building and Deploying the CirclePack User Guide Website (uv edition)
short_title: Build & Deploy Guide (uv)
authors:
  - name: CirclePack Documentation Team
keywords: [jupyter-book, myst, github-pages, uv, circlepack]
---

# Building and Deploying the CirclePack User Guide Website (uv edition)

This guide walks you through turning the MyST Markdown source files in the
CirclePack user guide project into a published HTML website using **uv**
for environment management. The workflow has three stages:

1. Install **uv** and use it to set up a reproducible Python environment.
2. Build the HTML with **Jupyter Book 2** (which uses the MyST Document
   Engine under the hood).
3. Deploy the built site to **GitHub Pages** via an automated **GitHub
   Actions** workflow.

:::{note} Why uv?
uv is a fast, Rust-based Python package manager from Astral (the makers
of Ruff). Compared to pipenv, it installs dependencies 10–30× faster,
uses the standard `pyproject.toml` configuration format, and can manage
Python itself — meaning you often don't need to install Python
separately. It's the recommended choice for new projects.
:::

:::{note}
This guide targets **Jupyter Book 2** (the MyST-based line, `jupyter-book
>= 2.0`). Jupyter Book 1 (the Sphinx-based line) is now in maintenance
mode and lives on the `v1` branch of the upstream repository. Since the
CirclePack guide is already authored in MyST, v2 is the right target.
:::

:::{tip} Prerequisites
Before starting, make sure you have:
- A **GitHub account** and the CirclePack user guide source in a GitHub
  repository (or ready to be pushed to one).
- **Git** installed locally. Verify with `git --version`.
:::

## Part 1 — Install uv and set up the environment

uv is a single self-contained binary. It can install and manage Python
for you, so in most cases this is the only tool you need to install
manually.

### 1.1 Install uv

Pick the instructions for your operating system. All three install uv
into a user-writable location — no administrator/sudo rights required.

::::{tab-set}

:::{tab-item} macOS / Linux
Use the official installer script:

```bash
curl -LsSf https://astral.sh/uv/install.sh | sh
```

This installs uv to `~/.local/bin/uv` and adds that directory to your
shell's `PATH`. Open a new terminal and verify:

```bash
uv --version
```

Alternatively, if you use Homebrew on macOS:

```bash
brew install uv
```
:::

:::{tab-item} Windows 11
Open PowerShell and run:

```powershell
powershell -ExecutionPolicy ByPass -c "irm https://astral.sh/uv/install.ps1 | iex"
```

This installs uv to `%USERPROFILE%\.local\bin\uv.exe` and updates your
user `PATH`. Open a **new** PowerShell window and verify:

```powershell
uv --version
```

Alternatively, if you use winget:

```powershell
winget install --id=astral-sh.uv
```
:::

::::

:::{tip} Already have Python?
If you already have Python 3.9+ installed, you can also install uv with
pip:

```bash
pip install uv
```

This works on any OS, but the standalone installers above are preferred
because they don't depend on an existing Python install.
:::

### 1.2 Install Python (optional — uv can do this)

Unlike pipenv, uv can install and manage Python itself. You don't need
Homebrew, the python.org installer, or apt for Python. Just run:

```bash
uv python install 3.12
```

uv downloads a standalone Python build and caches it for your projects.
To see what's available or installed:

```bash
uv python list
```

If you already have a system Python 3.9+, you can skip this step — uv
will use it automatically.

### 1.3 Initialize the project

From the root of your CirclePack user guide repository (the folder that
contains your `.md` source files):

```bash
cd path/to/circlepack-user-guide
uv init --no-workspace
```

This creates a minimal `pyproject.toml`. Open it and replace the
default contents with something like:

```toml
[project]
name = "circlepack-user-guide"
version = "0.1.0"
description = "User guide for CirclePack, built with Jupyter Book."
requires-python = ">=3.10"
dependencies = [
    "jupyter-book>=2.1.0",
    "mystmd>=1.3.0",
    "nodeenv>=1.9.0",
]
```

:::{tip} Already have `requirements-jb.txt`?
You can skip the `dependencies` block above and install from the
requirements file instead:

```bash
uv add -r requirements-jb.txt
```

uv will read the file, add the packages to `pyproject.toml`, and
resolve the environment in one step.
:::

### 1.4 Create the environment and install dependencies

```bash
uv sync
```

This single command:

1. Creates a virtual environment at `.venv/` in the project root.
2. Installs every dependency listed in `pyproject.toml`.
3. Writes `uv.lock` — a lock file that records exact versions and
   hashes of every package (direct and transitive).

**Commit both `pyproject.toml` and `uv.lock` to git.** Together they
guarantee that you, your collaborators, and your CI server all install
the same versions.

Expected time: 3–10 seconds on most machines. (Compare to 45–90 seconds
for a comparable pipenv install.)

:::{important}
Jupyter Book 2 is a Python wrapper around a Node.js-based engine
(MyST). The first time you run a `jupyter-book` command, it will use
`nodeenv` to download and cache an isolated Node.js runtime inside your
virtual environment. You do **not** need to install Node.js yourself.
The initial run adds roughly 100–150 MB and takes a minute or two.
:::

### 1.5 Running commands

With uv, you don't activate the environment — you prefix commands with
`uv run`:

```bash
uv run jupyter-book --version
uv run myst --version
```

`uv run` automatically uses the project's virtual environment and
ensures it's synced with `uv.lock` first. If you prefer a traditional
activation workflow, you can still do it:

::::{tab-set}

:::{tab-item} macOS / Linux
```bash
source .venv/bin/activate
jupyter-book --version
deactivate
```
:::

:::{tab-item} Windows 11 (PowerShell)
```powershell
.venv\Scripts\Activate.ps1
jupyter-book --version
deactivate
```
:::

::::

For the rest of this guide, commands are shown with `uv run` — that's
the canonical uv workflow.

---

## Part 2 — Build the HTML site with Jupyter Book

With the environment ready, you can now compile your MyST source files
into a static HTML website.

### 2.1 Project structure

Jupyter Book 2 projects are driven by a single configuration file,
`myst.yml`, at the project root. A typical CirclePack guide layout looks
like:

```text
circlepack-user-guide/
├── pyproject.toml        ← project + dependency configuration
├── uv.lock               ← locked dependency versions (commit this)
├── myst.yml              ← Jupyter Book / MyST configuration
├── index.md              ← landing page
├── installation.md
├── quickstart.md
├── reference/
│   ├── commands.md
│   └── api.md
├── images/
│   └── logo.png
└── .venv/                ← virtual environment (in .gitignore)
```

:::{tip} Starting from scratch?
If your project doesn't yet have a `myst.yml`, generate one interactively
with:

```bash
uv run jupyter-book init --write-toc
```

This scans your Markdown files, proposes a table of contents, and writes
`myst.yml` for you. Review and commit the result.
:::

### 2.2 Configure `myst.yml`

A minimal configuration for the CirclePack user guide looks like this:

```yaml
# myst.yml
version: 1
project:
  title: CirclePack User Guide
  description: A comprehensive guide to CirclePack.
  github: https://github.com/YOUR-USERNAME/circlepack-user-guide
  license: CC-BY-4.0
  toc:
    - file: index.md
    - file: installation.md
    - file: quickstart.md
    - title: Reference
      children:
        - file: reference/commands.md
        - file: reference/api.md
site:
  template: book-theme
```

Replace `YOUR-USERNAME` with your GitHub username (or the organization
name hosting the repo).

### 2.3 Build the site

From the project root:

```bash
uv run jupyter-book build --html
```

What happens:

- The MyST engine parses every file in the table of contents.
- Cross-references, citations, and figure numbering are resolved across
  the whole project.
- The final HTML output is written to `_build/html/`.

When the build finishes, open `_build/html/index.html` in your browser to
preview the site locally.

:::{tip} Live preview
For interactive development, use:

```bash
uv run jupyter-book start
```

This launches a local server (usually at <http://localhost:3000>) that
automatically rebuilds and refreshes the page whenever you save a source
file. Stop it with `Ctrl+C`.
:::

### 2.4 Clean builds

If something looks stale or wrong, wipe the build directory and
rebuild:

```bash
uv run jupyter-book clean
uv run jupyter-book build --html
```

### 2.5 Adding or updating dependencies

To add a new package:

```bash
uv add matplotlib
```

To update all packages to their latest compatible versions:

```bash
uv sync --upgrade
```

Both commands automatically update `pyproject.toml` and `uv.lock`.
Commit both files after any dependency change.

### 2.6 Common build issues

:::{dropdown} `Cross-reference not found`
MyST couldn't resolve a `[](target)` or `{ref}` link. Check that the
target file is listed in the `toc` in `myst.yml` and that the label
you're linking to actually exists.
:::

:::{dropdown} `ENOENT: no such file or directory` mentioning Node
The `nodeenv`-managed Node runtime may have been corrupted. Delete the
virtual environment and re-sync:

```bash
rm -rf .venv        # on Windows: rmdir /s .venv
uv sync
```
:::

:::{dropdown} Images don't appear
MyST resolves image paths relative to the source file. Use
`images/logo.png` from a top-level `index.md`, or `../images/logo.png`
from a file one level deep.
:::

---

## Part 3 — Deploy to GitHub Pages with GitHub Actions

Rather than building the site on your laptop and uploading the result,
GitHub Actions will rebuild and publish the site automatically every
time you push to `main`.

### 3.1 Enable GitHub Pages on your repository

1. Push your project (including `pyproject.toml` and `uv.lock`) to a
   GitHub repository.
2. On GitHub, open the repository and go to
   **Settings → Pages**.
3. Under **Build and deployment → Source**, choose **GitHub Actions**.
   (Do **not** choose "Deploy from a branch" — the workflow below
   publishes through the modern Pages deployment API instead.)

### 3.2 Add `.gitignore` entries

Add these lines to a `.gitignore` at the repository root so the build
output and local caches don't get committed:

```gitignore
# Jupyter Book build output
_build/

# uv virtual environment
.venv/

# Python bytecode
__pycache__/
*.pyc
```

### 3.3 Create the workflow file

Create the directory `.github/workflows/` at the root of your
repository and add a file named `deploy.yml` with the following
contents:

```yaml
# .github/workflows/deploy.yml
name: deploy-book

# Rebuild and deploy whenever main is updated.
# Also allow manual triggering from the Actions tab.
on:
  push:
    branches: [main]
  workflow_dispatch:

# Required permissions for the deploy-pages action.
permissions:
  contents: read
  pages: write
  id-token: write

# Only allow one concurrent deployment.
concurrency:
  group: "pages"
  cancel-in-progress: false

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Check out the repository
        uses: actions/checkout@v4

      - name: Install uv
        uses: astral-sh/setup-uv@v3
        with:
          enable-cache: true

      - name: Set up Python
        run: uv python install 3.12

      - name: Set up Node.js (for the MyST engine)
        uses: actions/setup-node@v4
        with:
          node-version: "20"

      - name: Install dependencies
        run: uv sync --frozen

      - name: Build the Jupyter Book
        run: uv run jupyter-book build --html

      - name: Upload built site as Pages artifact
        uses: actions/upload-pages-artifact@v3
        with:
          path: _build/html

  deploy:
    needs: build
    runs-on: ubuntu-latest
    environment:
      name: github-pages
      url: ${{ steps.deployment.outputs.page_url }}
    steps:
      - name: Deploy to GitHub Pages
        id: deployment
        uses: actions/deploy-pages@v4
```

:::{note} What the workflow steps do
- **setup-uv with enable-cache: true** — installs uv and caches both the
  uv download and its package cache across runs, dramatically speeding
  up subsequent builds.
- **uv python install 3.12** — installs the Python version your project
  declares it needs.
- **setup-node** — provides the Node.js runtime the MyST engine uses.
  Faster on CI than letting `nodeenv` bootstrap a fresh copy each run.
- **uv sync --frozen** — installs exactly what's in `uv.lock`, without
  re-resolving. This fails the build if the lock file is out of date,
  which is what you want on CI.
:::

### 3.4 Commit and push

```bash
git add pyproject.toml uv.lock myst.yml .gitignore \
        .github/workflows/deploy.yml
git commit -m "Add Jupyter Book build and GitHub Pages deploy workflow"
git push origin main
```

### 3.5 Watch the first deployment

1. On GitHub, open the **Actions** tab of your repository.
2. You should see a **deploy-book** run in progress. Click it to watch
   the logs.
3. When the job finishes (typically under a minute thanks to uv's
   caching), GitHub reports a URL similar to:

   ```text
   https://YOUR-USERNAME.github.io/circlepack-user-guide/
   ```

4. Open that URL — your CirclePack user guide is live.

### 3.6 Updating the site

Every subsequent push to `main` will retrigger the workflow and
redeploy the site. To publish edits:

```bash
git add .
git commit -m "Update chapter on circle packings"
git push origin main
```

Give the Actions run a minute to finish, then reload the site.

### 3.7 Troubleshooting deployment

:::{dropdown} "Get Pages site failed" error
This means GitHub Pages isn't enabled yet for the repo. Go back to
**Settings → Pages** and confirm the source is set to **GitHub
Actions**.
:::

:::{dropdown} `uv sync --frozen` fails with "lockfile out of date"
You changed `pyproject.toml` without regenerating the lock file. Run
`uv sync` locally (without `--frozen`), commit the updated `uv.lock`,
and push again.
:::

:::{dropdown} Build works locally but fails in Actions
The CI environment uses `uv sync --frozen`, so the environment should
be identical to yours. If it isn't, the most common cause is a package
that was added to your local venv with `pip install` rather than
`uv add`. Reproduce the CI environment locally:

```bash
rm -rf .venv
uv sync --frozen
uv run jupyter-book build --html
```

If this fails, the issue is reproducible — fix it and commit.
:::

:::{dropdown} Site is live but styling looks broken
GitHub Pages serves the site from a subpath like
`/circlepack-user-guide/`, not from the domain root. Make sure
`myst.yml` does **not** contain a `site.baseurl` that conflicts with
this. If you need a specific base URL, set it explicitly:

```yaml
site:
  template: book-theme
  baseurl: /circlepack-user-guide
```
:::

:::{dropdown} 404 errors on internal links
Internal links should use relative paths (`reference/commands.md`), not
absolute paths (`/reference/commands.md`). Absolute paths break when
the site is served from a subpath.
:::

---

## Summary

You now have a complete pipeline:

- **Author** in MyST Markdown in your local repository.
- **Preview** locally with `uv run jupyter-book start`.
- **Commit and push** to `main`.
- **GitHub Actions** rebuilds and publishes the site automatically.

Future changes to the guide only require editing a `.md` file, running
`git push`, and waiting a minute or so.

## uv command reference

Quick reference for the uv commands used throughout this guide:

| Command | Purpose |
|---|---|
| `uv python install 3.12` | Install Python 3.12 |
| `uv init` | Create a new `pyproject.toml` |
| `uv add PACKAGE` | Add a dependency |
| `uv add -r FILE` | Add dependencies from a requirements file |
| `uv sync` | Install everything from `pyproject.toml` + `uv.lock` |
| `uv sync --upgrade` | Upgrade all dependencies to latest compatible versions |
| `uv sync --frozen` | Install from `uv.lock` only; fail if out of date |
| `uv run COMMAND` | Run a command in the project's environment |
| `uv lock` | Regenerate `uv.lock` without installing |

## Further reading

- [uv documentation](https://docs.astral.sh/uv/) — full uv reference,
  including workspaces, scripts, and tools.
- [Jupyter Book documentation](https://jupyterbook.org) — the canonical
  reference for `myst.yml`, directives, and roles.
- [MyST Markdown guide](https://mystmd.org/guide) — full MyST syntax
  reference.
- [GitHub Pages documentation](https://docs.github.com/en/pages) — custom
  domains, HTTPS, and access control.
