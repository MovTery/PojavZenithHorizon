#!/bin/bash

THISDIR=`dirname $0`
LANGFILE=$THISDIR/../app_pojav_zh/src/main/assets/language_list.txt

rm -f $LANGFILE
echo $THISDIR/../app_pojav_zh/src/main/res/values-* | xargs -- basename -a > $LANGFILE

