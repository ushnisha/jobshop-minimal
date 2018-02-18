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
  echo "Usage: $0 <root directory of project> <path to dataset directory> <path to option file>"
  echo "For example: $0 . tests/test0001 load_options.txt"
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
DATASETDIR=`realpath $2`
DBNAME=
DBHOSTNAME=
DBUSERNAME=

while IFS=, read key value;
do
   if [ "$key" = "DBNAME" ]; then
     DBNAME=$value;
   fi

   if [ "$key" = "DBHOSTNAME" ]; then
     DBHOSTNAME=$value;
   fi
 
   if [ "$key" = "DBUSERNAME" ]; then
     DBUSERNAME=$value;
   fi
done < $3

cd $DATASETDIR
mysql -h "$DBHOSTNAME" -u "$DBUSERNAME" -D "$DBNAME" -p < $ROOTDIR/db/jobshop_mysql_loader.sql 
