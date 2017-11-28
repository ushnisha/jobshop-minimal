package com.ushnisha.JobShop;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import com.ushnisha.JobShop.CalendarShift;

/**
 * A representation of a calendar object.  A calendar has a name, a type,
 * and a list of calendar shifts (that represent the value of the calendar
 * corrsponding to its type).
 */
public class Calendar {

    private String name;
    private String type;
    private List<CalendarShift> shifts;

    /**
     * Constructor for a calendar
     * @param n String representing the name of the calendar
     * @param t type of the calendar.  At this time only one type,
     *          EFFICIENCY_CALENDAR is supported
     */
    public Calendar (String n, String t) {
        this.name = n;
        this.type = t;
        this.shifts = new ArrayList<CalendarShift> ();
    }

    /**
     * Return the name of the calendar
     * @return  String containing the name of the calendar
     */
    public String getName() {
        return this.name;
    }

    /**
     * Return the type of the calendar
     * @return  String containing the type of the calendar
     */
    public String getType() {
        return this.type;
    }

    /**
     * Add a shift to the calendar
     * @param s a CalendarShift that must be added to the calendar
     */
    public void addShift(CalendarShift s) {
        this.shifts.add(s);
    }

    /**
     * Return a list of the calendar shifts that are part of this calendar
     * @return List<CalendarShift> representing the shifts in the calendar
     */
    public List<CalendarShift> getShifts() {
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
