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
            print("Loading table: " + tablename + " ...")
            loaddatabase(connection, tablename, datafilename)

    ## Finally update all the timestamp fields for JDBC driver compliant format
    ##
    stmt = [ "update plan set planstart=strftime('%Y-%m-%d %H:%M:%f', planstart)",
             "update plan set planend=strftime('%Y-%m-%d %H:%M:%f', planend)",
             "update calendarshift set shiftstart=strftime('%Y-%m-%d %H:%M:%f', shiftstart)",
             "update calendarshift set shiftend=strftime('%Y-%m-%d %H:%M:%f', shiftend)",
             "update demand set duedate=strftime('%Y-%m-%d %H:%M:%f', duedate)" ]

    for s in stmt:
        connection.execute(s)
        connection.commit()
