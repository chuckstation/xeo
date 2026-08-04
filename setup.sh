#!/usr/bin/env bash
# setup.sh — Symlink src/ and third_party/ from xenia-upstream into the repo root.
#
# This script is part of Xeo (adars-org/xeo). Xeo is a production-ready
# Android port of Xenia Canary; xenia-upstream is the unmodified upstream
# emulator tree, pulled in as a submodule.
#
# xenia-upstream can be present either as:
#   a) A git submodule:  git submodule update --init --depth 1 xenia-upstream
#   b) A direct clone:   git clone --depth 1 https://github.com/xenia-project/xenia.git xenia-upstream

set -euo pipefail
REPO="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
UPSTREAM="${REPO}/xenia-upstream"

if [[ ! -d "${UPSTREAM}/src" ]]; then
  echo "xenia-upstream not found. Cloning..."
  git clone --depth 1 --branch canary_experimental \
    https://github.com/xenia-canary/xenia-canary.git \
    "${UPSTREAM}"
fi

echo "Initializing submodules inside xenia-upstream..."
git -C "${UPSTREAM}" submodule update --init --recursive --depth 1

if [[ -d "${REPO}/patches" ]]; then
  echo "Applying custom xenia-upstream patches..."
  mkdir -p "${UPSTREAM}/src/xenia/base/"
  cp -R "${REPO}/patches/base/"* "${UPSTREAM}/src/xenia/base/"
  mkdir -p "${UPSTREAM}/src/xenia/ui/"
  cp -R "${REPO}/patches/ui/"* "${UPSTREAM}/src/xenia/ui/"
  mkdir -p "${UPSTREAM}/src/xenia/kernel/"
  cp -R "${REPO}/patches/kernel/"* "${UPSTREAM}/src/xenia/kernel/"
  mkdir -p "${UPSTREAM}/src/xenia/app/"
  cp -R "${REPO}/patches/app/"* "${UPSTREAM}/src/xenia/app/"
  mkdir -p "${UPSTREAM}/src/xenia/hid/"
  cp -R "${REPO}/patches/hid/"* "${UPSTREAM}/src/xenia/hid/"
  cp "${REPO}/patches/emulator.cc" "${UPSTREAM}/src/xenia/emulator.cc"
  if [[ -f "${REPO}/patches/tools/build/compile_shader_spirv.py" ]]; then
    mkdir -p "${UPSTREAM}/tools/build/"
    cp "${REPO}/patches/tools/build/compile_shader_spirv.py" "${UPSTREAM}/tools/build/compile_shader_spirv.py"
  fi
fi

for target in src third_party; do
  link="${REPO}/${target}"
  src="xenia-upstream/${target}"
  if [[ -L "${link}" ]]; then
    # If the symlink is absolute, recreate it as relative
    current_target=$(readlink "${link}")
    if [[ "${current_target}" == /* ]]; then
      rm "${link}"
      ln -s "${src}" "${link}"
      echo "  re-linked as relative: ${target}"
    else
      echo "  already linked: ${target}"
    fi
  elif [[ -e "${link}" ]]; then
    echo "  WARNING: ${link} exists as real path, skipping"
  else
    ln -s "${src}" "${link}"
    echo "  linked: ${target}"
  fi
done

echo "Done. Run: ./gradlew assembleDebug\n"
echo "Xeo build started — see app/build/outputs/apk/ for the resulting APK."
