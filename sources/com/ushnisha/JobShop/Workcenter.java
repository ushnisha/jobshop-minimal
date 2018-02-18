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
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Comparator;

import static com.ushnisha.JobShop.JobShop.DEBUG_LEVELS;
import static com.ushnisha.JobShop.JobShop.LOG;

/**
 * A class representing a Workcenter or a Machine or a Resource.
 * The above terms (workcenter, machine, resource) may be used interchangeably
 * at various points in this project
 */
public class Workcenter {

    private String name;
    private Calendar efficiency_calendar;
    private int max_setups_per_shift;
    private int criticality_index;
    private int level;
    private List<TaskPlan> taskplans;

    /**
     * Constructor for the Workcenter
     * @param n String representing a unique name for this workcenter
     * @param cal Calendar object representing the calendar that determines
     *            the availability and efficiency of this Workcenter
     * @param nsetups int value representing the maximum number of unique
     *        Task that can be planned in any single shift on this workcenter
     * @param cindex an integer that represents the importance of this workcenter
     *        A planning algorithm can use this value to try and schedule the
     *        tasks on this resource as a priority
     */
    public Workcenter (String n, Calendar cal, int nsetups, int cindex) {
        this.name = n;
        this.efficiency_calendar = cal;
        this.max_setups_per_shift = nsetups;
        this.criticality_index = cindex;
        this.level = 0;
        this.taskplans = new ArrayList<TaskPlan>();
    }

    /**
     * returns the Calendar used for planning on this workcenter
     * @return Calendar that represents the working/efficiency calendar of this resource
     */
    public Calendar getCalendar() {
        return this.efficiency_calendar;
    }
    
    /**
     * returns the level of the workcenter
     * @return int value that represents the level of the resource.  The larger
     *         the level, the more upstream the workcenter and the more critical
     *         it is to resolve overload problems on this resource first
     */
     public int getLevel() {
		 return this.level;
	 }
	 
	 /**
	  * updates the level field of the workcenter
	  * @param l int value representing the level of the workcenter
	  */
	  public void setLevel(int l) {
		  this.level = l;
	  }
	  
    /**
     * Assign a TaskPlan to load this Workcenter
     * @param tp TaskPlan that is assigned to this workcenter and consumes time
     */
    public void addTaskPlan(TaskPlan tp) {
        this.taskplans.add(tp);
    }

    /**
     * Gets the list of TaskPlans that are planned on this workcenter
     * @return List<TaskPlan> that are planned on this resource
     */
    public List<TaskPlan> getTaskPlans() {
        return this.taskplans;
    }

    /**
     * Gets the list of TaskPlans that are planned on this workcenter
     * @param p Plan for which we want the list of TaskPlans
     * @return List<TaskPlan> that are planned on this resource for a
     *         specified, input Plan
     */
    public List<TaskPlan> getTaskPlans(Plan p) {
        return this.taskplans.stream()
                   .filter(t -> t.getPlan().equals(p))
                   .collect(Collectors.toList());
    }

    /**
     * Function that provides a DateRange within which we can schedule a TaskPlan
     * give the constraints within which the task plan must be planned
     * @param enddate a LocalDateTime on or before which the TaskPlan must end
     * @param baseLT a long value representing the lead time of the TaskPlan
     *        without any resource efficiency or working/holiday consideration
     * @param p Plan under which we are trying to Plan this resource.  The PlanParams of
     *        this plan will impose additional constraints on the planning algorithm
     */
    public DateRange queryEndBefore(LocalDateTime enddate, long baseLT, Plan p) {

        DateRange valid_DateRange = new DateRange(LocalDateTime.MIN, LocalDateTime.MAX);

        boolean capacity_constrained = Boolean.parseBoolean(p.getParam("RESOURCE_CONSTRAINED"));

        JobShop.LOG("Search for date ENDING ON ON BEFORE " +
                    enddate + " on workcenter " + this.name,
                    DEBUG_LEVELS.DETAILED);

        valid_DateRange = CalendarUtils.calcEndBefore(efficiency_calendar, enddate, baseLT);

        // If capacity constrained, make sure that there is no interesection with other taskplans
        // planned on this resource
        //
        if (capacity_constrained) {
            // If calendar is unable to find a valid date-range before end date; then we need to look forward
            if (valid_DateRange.getEnd().isAfter(enddate)) {
                return queryStartAfter(valid_DateRange.getStart(), baseLT, p);
            }

            // Calendar was able to find a valid date-range before end date
            // Now check if valid_DateRange is available and if not, look earlier
            boolean intersects = false;
            LocalDateTime new_enddate = LocalDateTime.MAX;
            for (TaskPlan t : this.getTaskPlans(p)) {
                if (t.intersects(valid_DateRange) && t.getStart().isBefore(new_enddate)) {
                    intersects = true;
                    new_enddate = t.getStart();
                }
            }
            if (intersects) {

                JobShop.LOG("Searching for daterange earlier than: " + enddate, DEBUG_LEVELS.DETAILED);
                JobShop.LOG("Intersection of valid daterange: " + valid_DateRange, DEBUG_LEVELS.DETAILED);
                JobShop.LOG("Looking now to end before: " + new_enddate, DEBUG_LEVELS.DETAILED);

                valid_DateRange = queryEndBefore(new_enddate, baseLT, p);
            }
        }

        JobShop.LOG("\tFound DateRange: " + valid_DateRange, DEBUG_LEVELS.DETAILED);

        return valid_DateRange;
    }

    /**
     * Function that provides a DateRange within which we can schedule a TaskPlan
     * give the constraints within which the task plan must be planned
     * @param startdate a LocalDateTime on or after which the TaskPlan must end
     * @param baseLT a long value representing the lead time of the TaskPlan
     *        without any resource efficiency or working/holiday consideration
     * @param p Plan under which we are trying to Plan this resource.  The PlanParams of
     *        this plan will impose additional constraints on the planning algorithm
     */
    public DateRange queryStartAfter(LocalDateTime startdate, long baseLT, Plan p) {

        DateRange valid_DateRange = new DateRange(LocalDateTime.MIN, LocalDateTime.MAX);

        boolean capacity_constrained = Boolean.parseBoolean(p.getParam("RESOURCE_CONSTRAINED"));

        JobShop.LOG("Search for date STARTING ON ON AFTER " +
                    startdate + " on workcenter " + this.name,
                    DEBUG_LEVELS.DETAILED);

        valid_DateRange = CalendarUtils.calcStartAfter(efficiency_calendar, startdate, baseLT);

        // If capacity constrained, make sure that there is no interesection with other taskplans
        // planned on this resource
        //
        if (capacity_constrained) {
            // If calendar is unable to find a valid date-range after start date; then we need to look backwards
            if (valid_DateRange.getStart().isBefore(startdate)) {
                return queryEndBefore(valid_DateRange.getEnd(), baseLT, p);
            }

            // Calendar was able to find a valid date-range after start date
            // Now check if valid_DateRange is available and if not, look later
            boolean intersects = false;
            LocalDateTime new_startdate = LocalDateTime.MIN;
            for (TaskPlan t : this.getTaskPlans(p)) {
                if (t.intersects(valid_DateRange) && t.getEnd().isAfter(new_startdate)) {
                    intersects = true;
                    new_startdate = t.getEnd();
                }
            }
            if (intersects) {

                JobShop.LOG("Searching for daterange later than: " + startdate, DEBUG_LEVELS.DETAILED);
                JobShop.LOG("Intersection with valid daterange: " + valid_DateRange, DEBUG_LEVELS.DETAILED);
                JobShop.LOG("Looking now to start after: " + new_startdate, DEBUG_LEVELS.DETAILED);

                valid_DateRange = queryStartAfter(new_startdate, baseLT, p);
            }
        }

        JobShop.LOG("\tFound DateRange: " + valid_DateRange, DEBUG_LEVELS.DETAILED);

        return valid_DateRange;
    }

    /**
     * A string representation of the workcenter
     * @return String which is the name of the workcenter
     */
    public String getName() {
        return this.name;
    }

    /**
     * A string representation of the workcenter
     * @return String representing the workcenter for output/log purposes
     */
    public String toString() {
        return this.name;
    }

}
