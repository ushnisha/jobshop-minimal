###############################################################################
# Copyright (c) 2017 Arun Kunchithapatham                                     #
# All rights reserved.  This program and the accompanying materials           #
# are made available under the terms of the GNU AGPL v3.0                     #
# which accompanies this distribution, and is available at                    #
# https://www.gnu.org/licenses/agpl-3.0.en.html                               #
# Contributors:                                                               #
# Arun Kunchithapatham - Initial Contribution                                 #
###############################################################################

import sys
import os.path
import sqlite3
import copy

def usage():
    print "Usage: python load_sqlite_db.py <load sequence filename> <path to dbfile> <path to dataset directory>"
    print "For example: python load_sqlite_db.py scripts/load_sequence.txt db/jobshop.db tests/test0001"
    exit(1)

def emptydatabasetable (connection, tablename):
    stmt = "DELETE FROM " + tablename
    connection.execute(stmt)
    connection.commit()


def loaddatabase(connection, tablename, datafilename):

    datafile = open(datafilename, 'r') 
    datarecs = datafile.readlines()
    datafile.close()

    header = datarecs[0]
    header = header.lstrip('#').rstrip()
    fields = header.split(',')

    # Incrementally build the SQL insert statement
    # from the header line of the datafile
    #
    stmt = "INSERT INTO " + tablename 

    firstfield = True
    fstring = ""
    vstring = ""
    for field in fields:
        if firstfield:
            fstring += field
            vstring += "?"
            firstfield = False
        else:
            fstring += ", " + field
            vstring += ", ?"

    stmt += "(" + fstring + ") VALUES(" + vstring + ")"

    numrecs = len(datarecs)
    datatuples = [ tuple(v if v != '' else None 
                         for v in datarecs[i].rstrip().split(',')) 
                         for i in range(1,numrecs) if datarecs[i][0] != "#" ]

    connection.executemany(stmt, datatuples)
    connection.commit()
        
    

if __name__ == "__main__":

    if len(sys.argv) != 4:
        usage()
    elif sys.argv[1] == "-h":
        usage()
    elif sys.argv[1] == "-help":
        usage()

    loadfilename = sys.argv[1]
    sqlitedb = sys.argv[2]
    datadir = sys.argv[3]

    loadfile = open(loadfilename, 'r')
    loadseq = loadfile.readlines()
    loadfile.close()

    connection = None
    try:
        connection = sqlite3.connect(sqlitedb)
        connection.execute("PRAGMA foreign_keys = ON;")

    except sqlite3.Error, e:
        if connection:
            connection.rollback()
        print "Error %s:" % e.args[0]
        sys.exit(1)

    ## First delete data from the tables in reverse load sequence
    for tablename in reversed(loadseq):
        emptydatabasetable(connection, tablename)

    ## Now load data file into the tables in forward loading sequence
    for tablename in loadseq:
        tablename = tablename.rstrip()
        datafilename = datadir + "/" + tablename + ".csv"
        if os.path.isfile(datafilename):
            loaddatabase(connection, tablename, datafilename)

    ## Finally update all the timestamp fields for JDBC driver compliant format
    ##
    stmt = [ "update plan set planstart=strftime('%Y-%m-%d %H:%M:%S.%f', planstart)",
             "update plan set planend=strftime('%Y-%m-%d %H:%M:%S.%f', planend)",
             "update calendarshift set shiftstart=strftime('%Y-%m-%d %H:%M:%S.%f', shiftstart)",
             "update calendarshift set shiftend=strftime('%Y-%m-%d %H:%M:%S.%f', shiftend)",
             "update demand set duedate=strftime('%Y-%m-%d %H:%M:%S.%f', duedate)" ]

    for s in stmt:
        connection.execute(s)
        connection.commit()
