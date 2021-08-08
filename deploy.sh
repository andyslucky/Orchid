#!/usr/bin/env bash
set -e

./gradlew writeProjectVersion -Prelease
export projectVersion=`cat ./build/export/projectVersion`

# Build and deploy normal Gradle projects
./gradlew assemble publishToMavenLocal publish :docs:orchidDeploy -PorchidEnvironment=prod -Prelease

# Deploy Gradle plugin
pushd buildSrc
./deploy.sh

# Deploy Maven plugin
pushd orchidMavenPlugin
./deploy.sh
popd

# Deploy SBT plugin
pushd orchidSbtPlugin
./deploy.sh
popd

# Restore original directory
popd
