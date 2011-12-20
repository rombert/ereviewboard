#!/bin/sh -e

if [ $# -ne 1 ] ; then
		echo "Usage: $0 version";
		exit 1;
fi

newVersion=$1

mvn -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=$newVersion

find -name MANIFEST.MF | xargs dos2unix
find -name site.xml | xargs sed -i 's#feature url="com#feature url="features/com#' 

