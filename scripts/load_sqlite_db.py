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

def emptydatabasetable (connection, tablename):
    stmt = "DELETE FROM " + tablename
    connection.execute(stmt)


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

