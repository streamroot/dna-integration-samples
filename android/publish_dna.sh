#!/bin/sh

set -eo pipefail

echo 'Make sure you are logged in to firebase using Firebase CLI "firebase login"'

echo 'Available targets : ExoPlayer | ExoPlayer-Java | ExoPlayer-pre2.10 | ExoPlayer-pre2.10-Java | PlayKit | PlayKitOVPStarter | VideoView | Brightcove | AMP'
read -p 'Specify project module ? : ' module

firebase --project streamroot-tools apps:list ANDROID | grep Samples
read -p 'Specify Firebase App ID ? : ' fir_app

read -p 'Release notes ? : ' fir_release_notes

MODULE_LOCATION=./$module
APK_FILE_LOCATION=./$module/app/build/outputs/apk/debug/app-debug.apk

echo "Will publish $module to Firebase app id $fir_app using release notes -> \"$fir_release_notes\""
read -p 'Proceed ? [yn] : ' is_ok

if [[ $is_ok != 'y' ]] ;
then
    echo 'Cancelled.'
    exit 1
fi

echo 'Executing ./gradlew clean assembleDebug'
(cd $MODULE_LOCATION && ./gradlew clean assembleDebug)

echo 'Distribution'
firebase appdistribution:distribute $APK_FILE_LOCATION --app $fir_app --groups 'qa' --release-notes "$fir_release_notes"