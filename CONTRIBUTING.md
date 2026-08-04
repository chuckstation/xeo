# Contributing to Xeo

Thanks for your interest in improving Xeo. This document is the short version —
for the full project layout and build instructions, see [README.md](README.md).

## Code of conduct

Be excellent to each other. Discussions stay technical and respectful.
Personal attacks, harassment, and discrimination are not tolerated.

## Before you open an issue

- Search the existing issues (open **and** closed) for your problem.
- If you are reporting a crash, attach the **full** `logcat` output captured
  while reproducing the issue — the `XeoJNI` and `AndroidWindowedAppContext`
  tags are especially important.
- If you are reporting a graphics bug, attach a GPU trace captured via the
  in-app "GPU Trace Viewer" workflow. Plain screenshots are rarely enough.

## Before you open a pull request

1. Open an issue first for any non-trivial change. This avoids wasted work
   when the change is out of scope for the project.
2. Fork `adars-org/xeo`, create a feature branch off `main`, and rebase on
   the latest `main` before submitting.
3. Keep PRs focused: one logical change per PR. Mixed refactors and feature
   work are hard to review and slow to land.
4. Run `./gradlew lintDebug` and `./gradlew assembleDebug` locally. The CI
   will reject PRs that fail either of these.

## Code style

- **Java:** 4-space indent, no tabs. Follow the existing `com.xenia.android`
  package conventions. Use `final` on parameters and locals where the
  reference is never reassigned — the existing code does this consistently.
- **C++:** Follow the upstream Xenia style (`xenia-upstream/xenia/.clang-format`).
  2-space indent, no tabs, `snake_case` for functions and variables.
- **CMake:** Lower-case commands, 2-space indent.
- **Resources:** All user-facing strings live in `res/values/strings.xml` and
  use formatting placeholders (`%1$d`, `%1$s`) instead of string concatenation.

## Commit messages

Use the imperative mood: "Add Vulkan fallback path" rather than "Added".
Reference issues with `Fixes #123` or `Refs #123` on the last paragraph.

## Branching model

- `main` — always shippable. Tags on `main` are official releases.
- `dev` — integration branch for the next release. PRs target `dev` when
  they are not release-critical.
- Feature branches — `feature/<short-name>` or `fix/<short-name>`.

## Releasing

Releases are cut from `main` by tagging `<major>.<minor>.<patch>` and pushing
the tag. The CI workflow builds signed APKs and publishes them to the GitHub
Release for that tag. See `CHANGELOG.md` for the format of release notes.
