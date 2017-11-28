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
    private String demandID;

    /**
     * Constructor for the TaskPlan object
     * @param t Task for which we are creating a TaskPlan instance
     * @param p Plan model under which we are planning this TaskPlan
     * @param w Workcenter that we are loading this task.
     *          This may be null if the task does not load a workcenter
     * @param st LocalDateTime representing the start of this task
     * @param en LocalDateTime representing the end of this task
     * @param qty long value representing the quantity for which this TaskPlan is created
     * @param dID String representing the unique identity of the Demand for which
     *            we are creating this TaskPlan
     */
    public TaskPlan(Task t, Plan p, Workcenter w,
                    LocalDateTime st, LocalDateTime en,
                    long qty, String dID) {
        this.task = t;
        this.plan = p;
        this.workcenter = w;
        this.start = st;
        this.end = en;
        this.quantity = qty;
        this.demandID = dID;
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
        return this.demandID;
    }

    /**
     * A string representation of the Task Plan
     * @return String value representing the TaskPlan for output/log purposes
     */
    public String toString() {
        return this.task.getTaskNumber() + "; Start: " + this.start + "; End: " + this.end +
               "; Quantity: " + this.quantity + "; Workcenter: " + this.workcenter + "; DemandID: " + this.demandID + "; Plan: " + this.plan.getID();
    }
}
