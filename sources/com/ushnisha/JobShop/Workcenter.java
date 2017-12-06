package com.ushnisha.JobShop;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import static com.ushnisha.JobShop.JobShop.DEBUG_LEVELS;
import static com.ushnisha.JobShop.JobShop.DEBUG;

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

        boolean capacity_constrained = Boolean.parseBoolean(p.getPlanParams().getParam("RESOURCE_CONSTRAINED"));

        if (!capacity_constrained) {
            valid_DateRange = CalendarUtils.calcEndBefore(efficiency_calendar, enddate, baseLT);
        }
        else {
            //
        }

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

        boolean capacity_constrained = Boolean.parseBoolean(p.getPlanParams().getParam("RESOURCE_CONSTRAINED"));

        if (!capacity_constrained) {
            valid_DateRange = CalendarUtils.calcStartAfter(efficiency_calendar, startdate, baseLT);
        }
        else {
        }

        return valid_DateRange;
    }

    /**
     * A string representation of the workcenter
     * @return String representing the workcenter for output/log purposes
     */
    public String toString() {
        return this.name;
    }

}
