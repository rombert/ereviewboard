#!/bin/sh

PAGES=../../ereviewboard-pages

if [ ! -d $PAGES ]; then
		echo "Unable to find pages directory"
		exit 1;
fi

rm -rf $PAGES/update/plugins/*
rm -rf $PAGES/update/features/*
cp -R target/site/* $PAGES/update/
cp associate-sites.xml $PAGES/update/

( cd $PAGES && git status )
