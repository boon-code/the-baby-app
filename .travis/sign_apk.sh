#!/bin/sh

APP_NAME=the-baby-app
KEY_NAME=upload

_fail() {
	echo "ERROR: $@" >&2
	exit 1
}

[ -n "${TRAVIS_BUILD_DIR}" ] || _fail "Travis build directory not set"
[ -n "${ZIPALIGN}" ] || _fail "zipalign path not set"
[ -n "${keystore_password}" ] || _fail "Keystore password not set"
[ -n "${key_password}" ] || _fail "Key password not set"

cd app/build/outputs/apk/release/
funsigned="$(ls ${APP_NAME}-*-release-unsigned.apk)" || _fail "Couldn't find apk"
fsigned="$(echo "$funsigned" | sed -e 's/\(.*-release\)-unsigned.apk/\1.apk/g')"

# Sign apk
jarsigner -verbose -sigalg "SHA1withRSA" \
          -digestalg "SHA1" -keystore "${TRAVIS_BUILD_DIR}/keystore.jks" \
          -storepass "${keystore_password}" \
          -keypass "${key_password}" \
          "$funsigned" "${KEY_NAME}" || _fail "Signing $funsigned failed"

# Verify
jarsigner -verify "$funsigned" || _fail "Verification of $funsigned failed"
"${ZIPALIGN}" -v 4 "$funsigned" "$fsigned" || _fail "Failed to align signed apk"
