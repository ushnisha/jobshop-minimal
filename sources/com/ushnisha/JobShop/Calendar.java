/**
 **********************************************************************
 * JobShop Minimal - A minimal JobShop Scheduler
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
import java.util.List;
import java.util.ArrayList;

/**
 * A representation of a calendar object.  A calendar has a name, a type,
 * and a list of calendar shifts (that represent the value of the calendar
 * corrsponding to its type).
 */
class Calendar {

    private String name;
    private String type;
    private List<CalendarShift> shifts;

    /**
     * Constructor for a calendar
     * @param n String representing the name of the calendar
     * @param t type of the calendar.  At this time only one type,
     *          EFFICIENCY_CALENDAR is supported
     */
    Calendar (String n, String t) {
        this.name = n;
        this.type = t;
        this.shifts = new ArrayList<CalendarShift> ();
    }

    /**
     * Return the name of the calendar
     * @return  String containing the name of the calendar
     */
    String getName() {
        return this.name;
    }

    /**
     * Return the type of the calendar
     * @return  String containing the type of the calendar
     */
    String getType() {
        return this.type;
    }

    /**
     * Add a shift to the calendar
     * @param s a CalendarShift that must be added to the calendar
     */
    void addShift(CalendarShift s) {
        this.shifts.add(s);
    }

    /**
     * Return a list of the calendar shifts that are part of this calendar
     * @return List<CalendarShift> representing the shifts in the calendar
     */
    List<CalendarShift> getShifts() {
        return this.shifts;
    }

    /**
     * String representation of the calendar object
     * @return String value representing the calendar
     */
    public String toString() {
        return this.type + "-Calendar-" + this.name;
    }

}
