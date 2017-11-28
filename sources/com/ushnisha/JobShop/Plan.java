package com.ushnisha.JobShop;

import java.time.LocalDateTime;

/** A class that represents a Plan.  The plan has a unique name and
 * a start/end date within which all planning activity occurs.
 * A plan also has a PlanParams object that defines various parameters
 * that govern planning logic for that plan
 */
public class Plan {

    private String id;
    private LocalDateTime start;
    private LocalDateTime end;
    private PlanParams params;

    /**
     * Constructor that creates a Plan object with a unique ID and
     * horizon start and end
     * @param id String represents the unique ID of the plan
     * @param st LocalDateTime representing start of planning horizon
     * @param en LocalDateTime representing end of the planning horizon
     */
    public Plan(String id, LocalDateTime st, LocalDateTime en) {
        this.id = id;
        this.start = st;
        this.end = en;
    }

    /**
     * returns the plan parameters for this plan
     * @return PlanParams representing the planning parameters for this plan
     */
    public PlanParams getPlanParams() {
        return this.params;
    }

    /**
     * Updates the planning parameters for this plan
     * @param pp PlanParams object with the planning parameters for this plan
     */
    public void setPlanParams(PlanParams pp) {
        this.params = pp;
    }

    /**
     * Returns the planning horizon start
     * @return LocalDateTime representing the start of the planning horizon
     */
    public LocalDateTime getStart() {
        return this.start;
    }

    /**
     * Returns the planning horizon end
     * @return LocalDateTime representing the end of the planning horizon
     */
    public LocalDateTime getEnd() {
        return this.end;
    }

    /**
     * Returns the unique identifier of this plan object
     * @return String representing the unique identifier of this plan
     */

    public String getID() {
        return this.id;
    }

    /**
     * returns a string representation of this plan for output/log purposes
     * @return String value representing this plan
     */
    public String toString() {
        return this.id + "; Start: " + this.start + "; End: " + this.end;
    }
}
