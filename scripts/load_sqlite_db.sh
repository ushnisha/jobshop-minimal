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
