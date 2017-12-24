#!/bin/sh

###############################################################################
# Copyright (c) 2017 Arun Kunchithapatham                                     #
# All rights reserved.  This program and the accompanying materials           #
# are made available under the terms of the GNU AGPL v3.0                     #
# which accompanies this distribution, and is available at                    #
# https://www.gnu.org/licenses/agpl-3.0.en.html                               #
# Contributors:                                                               #
# Arun Kunchithapatham - Initial Contribution                                 #
###############################################################################

usage()
{
  echo "Usage: $0 <root directory of project> <path to database file> <path to dataset directory>"
  echo "For example: $0 . db/jobshop.db tests/test0001"
  exit 1
}

if [ "$#" -ne "3" ]; then
  usage
elif [ "$1" = "-h" ]; then
  usage
elif [ "$1" = "-help" ]; then
  usage
fi

ROOTDIR=`realpath $1`
DBFILELOC=`realpath $2`
DATASETDIR=`realpath $3`
STAGINGDIR=/tmp/staging

test -d $STAGINGDIR || mkdir $STAGINGDIR

for dfile in `find $DATASETDIR -type f -name \*.csv`
do
    bname=`basename $dfile`
    tail -n +2 $dfile > $STAGINGDIR/$bname
done

cd $STAGINGDIR
sqlite3 $DBFILELOC < $ROOTDIR/db/jobshop_schema_staging_sqlite.ddl
sqlite3 $DBFILELOC < $ROOTDIR/db/jobshop_sqlite_loader.sql
