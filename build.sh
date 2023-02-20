#!/bin/bash

mvn clean package
cp target/*.war docker/acmeair-liberty/
cp target/*.war docker/acmeair-tomcat/

pushd docker

docker build -t acmeair-liberty acmeair-liberty/
docker build -t acmeair-liberty-chiselled acmeair-liberty-chiselled/

popd