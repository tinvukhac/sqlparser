#!/bin/bash

mkdir $2

for f in $1/*.sql
do
    query=$(echo $f | cut -d'/' -f 2 | cut -d'_' -f 1 | cut -d'.' -f 1)
    java -jar target/sqlparser-0.0.1-SNAPSHOT.jar $f > $2/$(echo $query).json
done
