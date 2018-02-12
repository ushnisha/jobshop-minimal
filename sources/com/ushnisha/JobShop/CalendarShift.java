/**
 **********************************************************************
   Copyright (c) 2017-2018 Arun Kunchithapatham

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as published
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.

   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

   Contributors:
   Arun Kunchithapatham - Initial Contribution
 ***********************************************************************
 *
 */

package com.ushnisha.JobShop;

import java.time.LocalDateTime;

/**
 * CalenderShift is an object that represents a shift within a calendar.  Depending
 * on the type of the calendar, its value indicates the behaviour/availability of
 * the calendar during the period of that shift.
 */
public class CalendarShift {

    private Calendar cal;
    private int shiftID;
    private LocalDateTime start;
    private LocalDateTime end;
    private int priority;
    private double value;

    private static double ZEROPLUS = 0.000001;

    /**
     * Constructor for a CalenderShift object
     * @param c name of the Calendar to which this shift belongs
     * @param id ID of the shift (a unique value for each calendar shift)
     * @param st start date/time of the shift
     * @param en end date/time of the shift
     * @param p priority of the shift (used in planning)
     * @param val value of the shift; for efficiency calendars this is a value between 0 and 1
     */
    public CalendarShift(Calendar c, int id, LocalDateTime st, LocalDateTime en, int p, double val) {
        this.cal = c;
        this.shiftID = id;
        this.start = st;
        this.end = en;
        this.priority = p;
        this.value = val;
    }

    /**
     * Return the priority of the shift
     * @return priority of the shift
     */
    public int getPriority() {
        return this.priority;
    }

    /**
     * Return the value of the shift
     * @return value of the shift
     */
    public double getValue() {
        return this.value;
    }

    /**
     * Return true if the shift is working; false if it is a holiday
     * @return boolean value representing if the shift is working or holiday
     */
    public boolean isWorking() {
        return (this.value > ZEROPLUS);
    }

    /**
     * Return the start date/time of the shift
     * @return start date/time of the shift
     */
    public LocalDateTime getStart() {
        return this.start;
    }

    /**
     * Return the end date/time of the shift
     * @return end date/time of the shift
     */
    public LocalDateTime getEnd() {
        return this.end;
    }

    /**
     * String representation of the CalendarShift object
     * @return String value representing the CalendarShift
     */
    public String toString() {
        return this.cal + "-" + this.shiftID;
    }

}
