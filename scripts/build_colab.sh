#!/usr/bin/env bash
# Run from a Colab/Linux environment with JDK 17, curl, unzip and python3.
set -euo pipefail
project_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
rm -f "$project_root/.savewhy-build-success"
build_cache="${SAVEWHY_BUILD_CACHE:-/content/savewhy-tools}"
mkdir -p "$build_cache/downloads"
export ANDROID_HOME="$build_cache/sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"

if ! command -v javac >/dev/null; then
  echo 'JDK 17 bulunamadı. Önce notebook kurulum hücresini çalıştır.' >&2
  exit 1
fi
java -version
fetch_zip() {
  local url="$1" target="$2" checksum="$3"
  if [[ -f "$target" ]] && echo "$checksum  $target" | sha256sum -c - >/dev/null 2>&1; then return; fi
  curl --fail --location --retry 3 --connect-timeout 30 "$url" --output "$target"
  echo "$checksum  $target" | sha256sum -c -
}
if [[ ! -x "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]]; then
  fetch_zip 'https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip' \
    "$build_cache/downloads/cmdline-tools.zip" '2d2d50857e4eb553af5a6dc3ad507a17adf43d115264b1afc116f95c92e5e258'
  extraction_dir="$(mktemp -d "$build_cache/cmdline.XXXXXX")"
  unzip -q "$build_cache/downloads/cmdline-tools.zip" -d "$extraction_dir"
  mkdir -p "$ANDROID_HOME/cmdline-tools"
  if [[ -e "$ANDROID_HOME/cmdline-tools/latest" ]]; then
    echo 'Eksik SDK kurulumu var. Farklı bir SAVEWHY_BUILD_CACHE dizini kullan.' >&2; exit 1
  fi
  mv "$extraction_dir/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
  rmdir "$extraction_dir"
fi
sdkmanager="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
# The notebook states the Android SDK license terms before this explicit opt-in.
if [[ "${ACCEPT_ANDROID_SDK_LICENSES:-}" == 'yes' ]]; then
  # yes may exit with SIGPIPE after sdkmanager closes stdin. Check sdkmanager itself.
  set +o pipefail
  yes | "$sdkmanager" --sdk_root="$ANDROID_HOME" --licenses
  sdk_status="${PIPESTATUS[1]}"
  set -o pipefail
  if [[ "$sdk_status" -ne 0 ]]; then exit "$sdk_status"; fi
else
  "$sdkmanager" --sdk_root="$ANDROID_HOME" --licenses
fi
"$sdkmanager" --sdk_root="$ANDROID_HOME" 'platforms;android-35' 'build-tools;34.0.0' 'platform-tools'

# Use the pinned distribution already verified here; no second download via wrapper.
if [[ ! -x "$build_cache/gradle-8.9/bin/gradle" ]]; then
  fetch_zip 'https://services.gradle.org/distributions/gradle-8.9-bin.zip' \
    "$build_cache/downloads/gradle.zip" 'd725d707bfabd4dfdc958c624003b3c80accc03f7037b5122c4b1d0ef15cecab'
  unzip -q -o "$build_cache/downloads/gradle.zip" -d "$build_cache"
fi
cd "$project_root"
"$build_cache/gradle-8.9/bin/gradle" --no-daemon --console=plain testDebugUnitTest lintDebug assembleDebug
apk='app/build/outputs/apk/debug/app-debug.apk'
test -s "$apk"
python3 - <<'PY'
import hashlib, json, pathlib, subprocess
apk = pathlib.Path('app/build/outputs/apk/debug/app-debug.apk')
commit = subprocess.check_output(['git', 'rev-parse', 'HEAD'], text=True).strip()
marker = {'commit': commit, 'sha256': hashlib.sha256(apk.read_bytes()).hexdigest()}
pathlib.Path('.savewhy-build-success').write_text(json.dumps(marker))
print('APK hazır:', apk.resolve())
print('Kaynak commit:', commit)
print('APK SHA-256:', marker['sha256'])
PY
