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
  echo "Usage: $0 <root directory of project> <path to dataset directory> <path to option file>"
  echo "For example: $0 . db/jobshop.db load_options.txt"
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
psql -d $DBNAME -U $DBUSERNAME -h $DBHOSTNAME -f $ROOTDIR/db/jobshop_postgresql_loader.sql 

