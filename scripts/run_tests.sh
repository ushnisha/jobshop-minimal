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


TESTNAME=$1
TESTDIR=$2
LIBDIR=$3

if [ "$TESTNAME" = "ALL" ]
then
    for tid in `find $TESTDIR -type d -name test0\* | sort`
        do
            tname=`basename $tid`
            echo "input_mode|FLATFILE" > $TESTDIR/$tname/jobshop_options.opt
            echo "datadir|$tid" >> $TESTDIR/$tname/jobshop_options.opt
            java -jar $LIBDIR/JobShop.jar $TESTDIR/$tname/jobshop_options.opt > $TESTDIR/outputs/$tname.out
            diff $TESTDIR/outputs/$tname.out $TESTDIR/expects/$tname.expect > /dev/null
            if [ $? -eq 0 ]
            then
                echo "Running test $tname... passed."
            else
                echo "Running test $tname... failed."
            fi
        done
else
    echo "input_mode|FLATFILE" > $TESTDIR/$TESTNAME/jobshop_options.opt
    echo "datadir|$TESTDIR/$TESTNAME" >> $TESTDIR/$TESTNAME/jobshop_options.opt
    java -jar $LIBDIR/JobShop.jar $TESTDIR/$TESTNAME/jobshop_options.opt > $TESTDIR/outputs/$TESTNAME.out
    diff $TESTDIR/outputs/$TESTNAME.out $TESTDIR/expects/$TESTNAME.expect >/dev/null
    if [ $? -eq 0 ]
    then
        echo "Running test $TESTNAME... passed."
    else
        echo "Running test $TESTNAME... passed."
    fi
fi
