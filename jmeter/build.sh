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
curl -o ../${PROJECT}/lib/ext/son-simple-1.1.1.jar https://repo1.maven.org/maven2/com/googlecode/json-simple/json-simple/1.1.1/json-simple-1.1.1.jar
popd

# update jmeter configuration
SAVE_COOKIES="CookieManager.save.cookies=true"
if ! grep -q ${SAVE_COOKIES} ${PROJECT}/bin/jmeter.properties; then
    echo ${SAVE_COOKIES} >> ${PROJECT}/bin/jmeter.properties
fi

# copy out test setup
cp acmeair-driver/acmeair-jmeter/scripts/*.csv .

