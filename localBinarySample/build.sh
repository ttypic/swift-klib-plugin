#!/bin/sh

rm -rf ../plugin/src/functionalTest/resources/DummyFramework.xcframework

xcodebuild archive \
-scheme DummyFramework \
-destination "generic/platform=iOS" \
-archivePath archives/DummyFramework-iOS \
SKIP_INSTALL=NO \
BUILD_LIBRARY_FOR_DISTRIBUTION=YES

xcodebuild archive \
-scheme DummyFramework \
-destination "generic/platform=iOS Simulator" \
-archivePath archives/DummyFramework-iOS_Simulator \
SKIP_INSTALL=NO \
BUILD_LIBRARY_FOR_DISTRIBUTION=YES

xcodebuild archive \
-scheme DummyFramework \
-destination "generic/platform=macOS" \
-archivePath archives/DummyFramework-macOS \
SKIP_INSTALL=NO \
BUILD_LIBRARY_FOR_DISTRIBUTION=YES

xcodebuild archive \
-scheme DummyFramework \
-destination "generic/platform=watchOS" \
-archivePath archives/DummyFramework-watchOS \
SKIP_INSTALL=NO \
BUILD_LIBRARY_FOR_DISTRIBUTION=YES

xcodebuild archive \
-scheme DummyFramework \
-destination "generic/platform=watchOS Simulator" \
-archivePath archives/DummyFramework-watchOS_Simulator \
SKIP_INSTALL=NO \
BUILD_LIBRARY_FOR_DISTRIBUTION=YES

xcodebuild archive \
-scheme DummyFramework \
-destination "generic/platform=tvOS" \
-archivePath archives/DummyFramework-tvOS \
SKIP_INSTALL=NO \
BUILD_LIBRARY_FOR_DISTRIBUTION=YES

xcodebuild archive \
-scheme DummyFramework \
-destination "generic/platform=tvOS Simulator" \
-archivePath archives/DummyFramework-tvOS_Simulator \
SKIP_INSTALL=NO \
BUILD_LIBRARY_FOR_DISTRIBUTION=YES


xcodebuild -create-xcframework \
    -archive archives/DummyFramework-iOS.xcarchive -framework DummyFramework.framework \
    -archive archives/DummyFramework-iOS_Simulator.xcarchive -framework DummyFramework.framework \
    -archive archives/DummyFramework-macOS.xcarchive -framework DummyFramework.framework \
    -archive archives/DummyFramework-watchOS.xcarchive -framework DummyFramework.framework \
    -archive archives/DummyFramework-watchOS_Simulator.xcarchive -framework DummyFramework.framework \
    -archive archives/DummyFramework-tvOS.xcarchive -framework DummyFramework.framework \
    -archive archives/DummyFramework-tvOS_Simulator.xcarchive -framework DummyFramework.framework \
    -output ../plugin/src/functionalTest/resources/DummyFramework.xcframework


cd ../plugin/src/functionalTest/resources/DummyFramework.xcframework
zip -r DummyFramework.xcframework.zip DummyFramework.xcframework
echo "Set the checksum of the package in the binary target for test"
swift package compute-checksum DummyFramework.xcframework.zip
