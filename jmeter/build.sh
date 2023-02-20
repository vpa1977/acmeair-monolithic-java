#!/bin/bash

VERSION=5.5
PROJECT=apache-jmeter-${VERSION}

# fetch dependencies
if [ ! -d ${PROJECT} ]; then
    wget https://dlcdn.apache.org//jmeter/binaries/${PROJECT}.zip
    unzip ${PROJECT}.zip
fi

git submodule update --init --recursive

# build and deploy acmeair jmeter driver
pushd acmeair-driver

./gradlew build

cp acmeair-jmeter/build/libs/acmeair-jmeter-*-SNAPSHOT.jar \
    ../${PROJECT}/lib/ext/

popd

# update jmeter configuration
SAVE_COOKIES="CookieManager.save.cookies=true"
if ! grep -q ${SAVE_COOKIES} ${PROJECT}/bin/jmeter.properties; then
    echo ${SAVE_COOKIES} >> ${PROJECT}/bin/jmeter.properties
fi

# copy out test setup
cp acmeair-driver/acmeair-jmeter/scripts/*.csv .

