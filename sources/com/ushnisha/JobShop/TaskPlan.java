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
 * A class representing a TaskPlan.  This is a plan instance of a Task
 */
public class TaskPlan {

    private Task task;
    private Plan plan;
    private Workcenter workcenter;
    private LocalDateTime start;
    private LocalDateTime end;
    private long quantity;
    private Demand demand;
    private boolean isReleased;
    private ReleasedWorkOrder relworkorder;

    /**
     * Constructor for the TaskPlan object
     * @param t Task for which we are creating a TaskPlan instance
     * @param p Plan model under which we are planning this TaskPlan
     * @param w Workcenter that we are loading this task.
     *          This may be null if the task does not load a workcenter
     * @param st LocalDateTime representing the start of this task
     * @param en LocalDateTime representing the end of this task
     * @param qty long value representing the quantity for which this TaskPlan is created
     * @param dmd Demand for which we are creating this TaskPlan
     */
    public TaskPlan(Task t, Plan p, Workcenter w,
                    LocalDateTime st, LocalDateTime en,
                    long qty, Demand dmd) {
        this.task = t;
        this.plan = p;
        this.workcenter = w;
        this.start = st;
        this.end = en;
        this.quantity = qty;
        this.demand = dmd;
        this.isReleased = false;
        this.relworkorder = null;
    }

    /**
     * Constructor for the TaskPlan object
     * @param t Task for which we are creating a TaskPlan instance
     * @param p Plan model under which we are planning this TaskPlan
     * @param w Workcenter that we are loading this task.
     *          This may be null if the task does not load a workcenter
     * @param st LocalDateTime representing the start of this task
     * @param en LocalDateTime representing the end of this task
     * @param qty long value representing the quantity for which this TaskPlan is created
     * @param rwo ReleasedWorkOrder representing the unique identity of the Demand for which
     *            we are creating this TaskPlan
     */
    public TaskPlan(Task t, Plan p, Workcenter w,
                    LocalDateTime st, LocalDateTime en,
                    long qty, ReleasedWorkOrder rwo) {
        this.task = t;
        this.plan = p;
        this.workcenter = w;
        this.start = st;
        this.end = en;
        this.quantity = qty;
        this.isReleased = true;
        this.relworkorder = rwo;
        this.demand = this.relworkorder.getDemand();
    }

    /**
     * Constructor for the TaskPlan object
     * @param t Task for which we are creating a TaskPlan instance
     * @param p Plan model under which we are planning this TaskPlan
     * @param w Workcenter that we are loading this task.
     *          This may be null if the task does not load a workcenter
     * @param st LocalDateTime representing the start of this task
     * @param en LocalDateTime representing the end of this task
     * @param qty long value representing the quantity for which this
     *            TaskPlan is created
     * @param dmd Demand associated with the ReleasedWorkOrder for
     *            which we are creating this TaskPlan
     * @param rwo ReleasedWorkOrder for which we are creating this TaskPlan
     */
    public TaskPlan(Task t, Plan p, Workcenter w,
                    LocalDateTime st, LocalDateTime en,
                    long qty, Demand dmd, ReleasedWorkOrder rwo) {
        this.task = t;
        this.plan = p;
        this.workcenter = w;
        this.start = st;
        this.end = en;
        this.quantity = qty;
        this.demand = dmd;
        this.isReleased = true;
        this.relworkorder = rwo;
    }

    /**
     * Returns the start time for this TaskPlan
     * @return LocalDateTime representing the starting time of the TaskPlan
     */
    public LocalDateTime getStart() {
        return this.start;
    }

    /**
     * Returns the end time for this TaskPlan
     * @return LocalDateTime representing the ending time of the TaskPlan
     */
    public LocalDateTime getEnd() {
        return this.end;
    }

    /**
     * Returns the quantity for this TaskPlan
     * @return long value representing the quantity of the TaskPlan
     */
    public long getQuantity() {
        return this.quantity;
    }

    /**
     * Updates the quantity for this TaskPlan
     * @param qty representing the new quantity of the TaskPlan
     */
    public void setQuantity(long qty) {
        this.quantity = qty;
    }

    /**
     * Returns the Plan for which we are creating this TaskPlan
     * @return Plan for which we are creating this TaskPlan
     */
    public Plan getPlan() {
        return this.plan;
    }

    /**
     * Returns a unique identifier of the demand for which we are
     * creating this TaskPlan
     * @return String representing the unique identifier of the demand
     *         for which we are creating this TaskPlan
     */
    public String getDemandID() {
        if (this.demand == null) {
            return "null";
        }
        else {
            return this.demand.getID();
        }
    }

    /**
     * Get the demand of this TaskPlan
     * @return Demand with which to associate this TaskPlan
     */
    public Demand getDemand() {
        return this.demand;
    }

    /**
     * Sets the demand of this TaskPlan
     * @param dmd Demand with which to associate this TaskPlan
     */
    public void setDemand(Demand dmd) {
        this.demand = dmd;
    }

    /**
     * Returns the Task that this TaskPlan is a planning instance of
     * @return Task representing the Task corresponding to this TaskPlan
     */
    public Task getTask() {
        return this.task;
    }

    /**
     * Returns the Workcenter that this TaskPlan is a planned on
     * @return Workcenter representing the Workcenter this TaskPlan loads
     */
    public Workcenter getWorkcenter() {
        return this.workcenter;
    }

    /**
     * Returns the ReleasedWorkOrder that this TaskPlan is a planned for
     * @return ReleasedWorkOrder based on which this TaskPlan was created
     */
    public ReleasedWorkOrder getReleasedWorkOrder() {
        return this.relworkorder;
    }

    /**
     * Utility function that returns true if a TaskPlan intersects a
     * provided DateRange
     * @param dr DateRange with which we check to see if TaskPlan intersects
     * @return boolean value true if intersects; false if not
     */
    public boolean intersects(DateRange dr) {

        if (this.end.isAfter(dr.getStart()) &&
            (this.end.isEqual(dr.getEnd()) || this.end.isBefore(dr.getEnd()))) {
            return true;
        }

        if ((this.start.isEqual(dr.getStart()) || this.start.isAfter(dr.getStart())) &&
            this.start.isBefore(dr.getEnd())) {
            return true;
        }

        if((this.start.isEqual(dr.getStart()) || this.start.isBefore(dr.getStart())) &&
           (this.end.isEqual(dr.getEnd()) || this.end.isAfter(dr.getEnd()))) {
            return true;
        }

        return false;
    }

    /**
     * A string representation of the Task Plan
     * @return String value representing the TaskPlan for output/log purposes
     */
    public String toString() {
        String outStr = this.task.getTaskNumber() + " [ " +
                        this.start + " - " + this.end + "] Qty: " +
                        this.quantity + "; DemandID: " +
                        this.getDemandID() +"; Plan: " +
                        this.plan.getID() + "; Loads: " + this.workcenter;

        if (this.isReleased) {
            outStr += "; RWO: " + this.relworkorder.getID() + "-" + this.relworkorder.getLotID(this);
        }
        return outStr;
    }
}
