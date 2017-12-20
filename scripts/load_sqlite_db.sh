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


ROOTDIR=`realpath $1`
DATASETDIR=`realpath $2`
DBFILELOC=`realpath $3`
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


