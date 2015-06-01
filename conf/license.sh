#!/bin/bash

ROOT=$(cd `dirname $0`/..; pwd)
HEADER=${ROOT}/conf/apache-2.0-header.txt
PROJECTS="
    $ROOT/less-cli
    $ROOT/less-compiler
"

for project in ${PROJECTS} ; do
    for f in `find ${project}/src -name '*.java'` ; do
        if ! grep -q '* Licensed under the Apache License' $f ; then
            cat ${HEADER} $f >$f.new && mv $f.new $f
        fi
    done
done


