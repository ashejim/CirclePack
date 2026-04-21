---
title: Building and Deploying the CirclePack User Guide Website
short_title: Build & Deploy Guide
authors:
  - name: CirclePack Documentation Team
keywords: [jupyter-book, myst, github-pages, pipenv, circlepack]
---

# Building and Deploying the CirclePack User Guide Website

This guide walks you through turning the MyST Markdown source files in the
CirclePack user guide project into a published HTML website. The workflow has
three stages:

1. Set up a reproducible Python environment with **pipenv**.
2. Build the HTML with **Jupyter Book 2** (which uses the MyST Document
   Engine under the hood).
3. Deploy the built site to **GitHub Pages** via an automated **GitHub
   Actions** workflow.

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
- Administrator / sudo rights on your machine for installing Python.
:::

## Part 1 — Install Python and pipenv, then set up the environment

This section covers installing Python, installing `pipenv`, and using it to
create an isolated environment with the packages Jupyter Book needs. Pick
the subsection for your operating system, then continue with the common
steps.

### 1.1 Install Python (3.10 or newer)

Jupyter Book 2 requires Python **3.9+**, but 3.11 or 3.12 is recommended.
Verify what you have with:

```bash
python3 --version
```

If it prints `Python 3.10.x` or higher, skip to {ref}`install-pipenv`. If
Python is missing or too old, install it as follows.

#### macOS

The easiest route is [Homebrew](https://brew.sh). If you don't have Homebrew
yet, install it first with the one-line command from its home page. Then:

```bash
brew install python@3.12
```

Verify:

```bash
python3 --version
```

#### Windows

1. Go to <https://www.python.org/downloads/windows/> and download the
   latest stable **Windows installer (64-bit)**.
2. Run the installer. **Check the box "Add python.exe to PATH"** on the
   first screen — this saves a lot of trouble later.
3. Choose **Install Now**.
4. Open a **new** PowerShell or Command Prompt window (the PATH update
   only applies to windows opened after install) and verify:

   ```powershell
   python --version
   ```

:::{tip}
On Windows, the command is typically `python` (not `python3`). Adjust the
commands in the rest of this guide accordingly.
:::

#### Linux (Debian / Ubuntu)

```bash
sudo apt update
sudo apt install python3 python3-pip python3-venv
python3 --version
```

#### Linux (Fedora / RHEL)

```bash
sudo dnf install python3 python3-pip
python3 --version
```

(ref-install-pipenv)=
### 1.2 Install pipenv

`pipenv` is a tool that combines `pip` and `virtualenv` into a single
workflow and produces a lock file (`Pipfile.lock`) so that every
collaborator — and the CI server — installs the exact same package
versions.

Install it with pip in **user** mode so it doesn't require root:

::::{tab-set}

:::{tab-item} macOS / Linux
```bash
python3 -m pip install --user pipenv
```
:::

:::{tab-item} Windows
```powershell
python -m pip install --user pipenv
```
:::

::::

After installation, make sure the directory containing the `pipenv`
executable is on your `PATH`. If running `pipenv --version` fails,
you'll need to add the user-scripts directory.

:::{dropdown} Finding your user-scripts directory
Run this to print the correct path:

```bash
python3 -m site --user-base
```

- **macOS / Linux:** append `/bin` to that path and add it to your
  shell's `PATH` (in `~/.zshrc`, `~/.bashrc`, or `~/.profile`). For
  example:
  ```bash
  export PATH="$HOME/.local/bin:$PATH"
  ```
- **Windows:** append `\Scripts` to that path and add it to your
  user `Path` environment variable via
  **Settings → System → About → Advanced system settings → Environment
  Variables**.

Open a new terminal and run `pipenv --version` to confirm.
:::

### 1.3 Create the environment and install dependencies

From the root of your CirclePack user guide repository (the folder that
contains your `.md` source files), run:

```bash
cd path/to/circlepack-user-guide
pipenv install -r requirements-jb.txt
```

This will:

1. Create a new virtual environment isolated from your system Python.
2. Install every package listed in `requirements-jb.txt`.
3. Generate a `Pipfile` and a `Pipfile.lock` in the current directory —
   **commit both to git** so the build is reproducible.

:::{important}
Jupyter Book 2 is a Python wrapper around a Node.js-based engine
(MyST). The first time you run a `jupyter-book` command, it will use
`nodeenv` to download and cache an isolated Node.js runtime inside your
virtual environment. You do **not** need to install Node.js yourself.
The initial install adds roughly 100–150 MB and takes a minute or two.
:::

Activate the environment whenever you want to work on the book:

```bash
pipenv shell
```

You'll see the prompt change to indicate the active environment. Leave it
with `exit`.

:::{tip}
Prefer one-off commands over activating a shell? Prefix any command with
`pipenv run`, e.g. `pipenv run jupyter-book build .`.
:::

### 1.4 Verify the installation

Inside the activated environment:

```bash
jupyter-book --version
myst --version
```

Both should print a version number without errors. If either command is
"not found", the environment isn't active — run `pipenv shell` again.

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
├── myst.yml              ← project + site configuration
├── index.md              ← landing page
├── installation.md
├── quickstart.md
├── reference/
│   ├── commands.md
│   └── api.md
├── images/
│   └── logo.png
├── requirements-jb.txt
├── Pipfile
└── Pipfile.lock
```

:::{tip} Starting from scratch?
If your project doesn't yet have a `myst.yml`, generate one interactively
with:

```bash
jupyter-book init --write-toc
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
jupyter-book build --html
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
jupyter-book start
```

This launches a local server (usually at <http://localhost:3000>) that
automatically rebuilds and refreshes the page whenever you save a source
file. Stop it with `Ctrl+C`.
:::

### 2.4 Clean builds

If something looks stale or wrong, wipe the build directory and
rebuild:

```bash
jupyter-book clean
jupyter-book build --html
```

### 2.5 Common build issues

:::{dropdown} `Cross-reference not found`
MyST couldn't resolve a `[](target)` or `{ref}` link. Check that the
target file is listed in the `toc` in `myst.yml` and that the label
you're linking to actually exists.
:::

:::{dropdown} `ENOENT: no such file or directory` mentioning Node
The `nodeenv`-managed Node runtime may have been corrupted. Delete the
virtual environment (`pipenv --rm`) and re-run `pipenv install`.
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

1. Push your project (including `requirements-jb.txt`, `Pipfile`, and
   `Pipfile.lock`) to a GitHub repository.
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

# Local pipenv virtual environment (if created in-repo)
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

      - name: Set up Python
        uses: actions/setup-python@v5
        with:
          python-version: "3.12"
          cache: pip

      - name: Set up Node.js (for the MyST engine)
        uses: actions/setup-node@v4
        with:
          node-version: "20"

      - name: Install Python dependencies
        run: pip install -r requirements-jb.txt

      - name: Build the Jupyter Book
        run: jupyter-book build --html

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

:::{note} Why install Node.js explicitly?
The `jupyter-book` Python package would normally bootstrap its own Node
runtime via `nodeenv`, but on CI it's faster and more reliable to use
the pre-cached Node that GitHub provides. The MyST engine will pick up
whichever Node is on `PATH`.
:::

### 3.4 Commit and push

```bash
git add requirements-jb.txt Pipfile Pipfile.lock \
        myst.yml .gitignore \
        .github/workflows/deploy.yml
git commit -m "Add Jupyter Book build and GitHub Pages deploy workflow"
git push origin main
```

### 3.5 Watch the first deployment

1. On GitHub, open the **Actions** tab of your repository.
2. You should see a **deploy-book** run in progress. Click it to watch
   the logs.
3. When the job finishes (typically 1–3 minutes), GitHub reports a URL
   similar to:

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

:::{dropdown} Build works locally but fails in Actions
The local environment probably has a package the CI environment
doesn't. Reproduce the CI install locally:

```bash
pipenv --rm                              # delete current env
pipenv install -r requirements-jb.txt    # reinstall cleanly
pipenv run jupyter-book build --html
```

If it fails locally now, add the missing package to
`requirements-jb.txt`, commit, and push.
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
- **Preview** locally with `jupyter-book start`.
- **Commit and push** to `main`.
- **GitHub Actions** rebuilds and publishes the site automatically.

Future changes to the guide only require editing a `.md` file, running
`git push`, and waiting a couple of minutes.

## Further reading

- [Jupyter Book documentation](https://jupyterbook.org) — the canonical
  reference for `myst.yml`, directives, and roles.
- [MyST Markdown guide](https://mystmd.org/guide) — full MyST syntax
  reference.
- [GitHub Pages documentation](https://docs.github.com/en/pages) — custom
  domains, HTTPS, and access control.
- [pipenv documentation](https://pipenv.pypa.io/) — advanced dependency
  management, scripts, and environment variables.
