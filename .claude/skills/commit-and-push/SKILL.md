---
name: commit-and-push
description: Commit code changes and push via Git. Use when the user asks to commit, push, or save their work to the repository.
argument-hint: commit message or description of changes
---

# Commit and Push

You are committing and pushing code changes for Simple Tung Shing.

## Commit Message Convention

- **Subject line**: imperative mood, concise summary of the change (e.g. "Add Tung Shing home-screen widget and almanac app", "Fix widget not refreshing after midnight")
- **Body** (optional): explain *why* the change was made, not just what. Include context when the change is non-trivial.
- **Signed-off-by trailer**: all commits use `--signoff` to add the `Signed-off-by` trailer automatically.
- No conventional commits prefix (no `feat:`, `fix:`, etc.) — this project uses plain English subject lines.

### Subject Line Patterns

Follow the existing style:
- `Add ...` — new feature or file
- `Fix ...` — bug fix
- `Update ...` — dependency or content update (including a refreshed CWA calendar dataset)
- `Remove ...` — deletion of code or files
- `Refactor: ...` — code restructuring
- `Style: ...` — formatting or cosmetic changes
- `Upgrade ...` — dependency version upgrades
- `Release X.Y.Z` — release commits (handled by `/release-engineering`)

## Workflow

1. **Review changes** — run `git status` and `git diff` to understand what will be committed.
2. **Stage files** — add specific files by name rather than `git add -A`. Be careful not to stage:
   - Sensitive files (`.env`, credentials, signing keys, keystores)
   - Machine-local files (`local.properties`, `.idea/workspace.xml` and other ignored `.idea` files)
   - Build artifacts (`build/`, `app/build/`, `app/release/`, `app/debug/`)
3. **Commit** — use `--signoff` flag. Pass the message via HEREDOC for proper formatting:
   ```bash
   git commit --signoff -m "$(cat <<'EOF'
   Subject line here

   Optional body explaining why.

   Co-Authored-By: Claude <MODEL> <noreply@anthropic.com>
   EOF
   )"
   ```

   **Co-author line is dynamic** — do NOT hardcode a model version. Replace
   `<MODEL>` with the display name of the model actually running this session,
   exactly as stated in your environment/system prompt (e.g. "You are powered by
   the model named ..."). Include any variant qualifier it carries, such as
   `(1M context)`. Examples of the resulting trailer:
   - `Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>`
   - `Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>`

   If the running model's name is genuinely unavailable, fall back to a bare
   `Co-Authored-By: Claude <noreply@anthropic.com>` rather than guessing a version.
4. **Push** — always push to `origin` right after committing; the user has asked never to be asked for confirmation. Check the configured remotes with `git remote -v`; if none is configured yet, ask the user where to push instead of adding one yourself.

## Branch Conventions

- `main` — main/stable branch
- Feature branches use descriptive names (e.g. `tung-shing-widget`)

Never force-push to `main` without explicit user approval.

## Task: $ARGUMENTS
