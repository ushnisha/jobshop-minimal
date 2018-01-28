#!/bin/sh

###############################################################################
# Copyright (c) 2017-2018 Arun Kunchithapatham                                #
#                                                                             # 
# This program is free software: you can redistribute it and/or modify        #
# it under the terms of the GNU Affero General Public License as published    #
# by the Free Software Foundation, either version 3 of the License, or        #
# (at your option) any later version.                                         #
#                                                                             #
# This program is distributed in the hope that it will be useful,             #
# but WITHOUT ANY WARRANTY; without even the implied warranty of              #
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               #
# GNU Affero General Public License for more details.                         #
#                                                                             #
# You should have received a copy of the GNU Affero General Public License    #
# along with this program.  If not, see <http://www.gnu.org/licenses/>.       #
#                                                                             #
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
            echo "logdir|$LIBDIR/../logs" >> $TESTDIR/$tname/jobshop_options.opt
            echo "cleandata|false" >> $TESTDIR/$tname/jobshop_options.opt
            echo "debug_level|MINIMAL" >> $TESTDIR/$tname/jobshop_options.opt

            java -jar $LIBDIR/JobShop.jar $TESTDIR/$tname/jobshop_options.opt > $TESTDIR/outputs/$tname.out 2>&1
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
    echo "logdir|$LIBDIR/../logs" >> $TESTDIR/$tname/jobshop_options.opt
    echo "cleandata|false" >> $TESTDIR/$tname/jobshop_options.opt
    echo "debug_level|MINIMAL" >> $TESTDIR/$tname/jobshop_options.opt
    java -jar $LIBDIR/JobShop.jar $TESTDIR/$TESTNAME/jobshop_options.opt > $TESTDIR/outputs/$TESTNAME.out 2>&1
    diff $TESTDIR/outputs/$TESTNAME.out $TESTDIR/expects/$TESTNAME.expect >/dev/null
    if [ $? -eq 0 ]
    then
        echo "Running test $TESTNAME... passed."
    else
        echo "Running test $TESTNAME... failed."
    fi
fi
