package com.ushnisha.JobShop;

import java.util.Map;
import java.util.HashMap;
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
    private Map<String, String> params;

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
        this.params = new HashMap<String, String>();
    }


    /**
     * Returns the value of a specific planning parameter, given its name
     * @param key String representing the name of the planning parameter
     * @return String representing the value of the parameter representing the key
     *         assumes that the key is a valid planning parameter name
     */
    public String getParam(String key) {
        return this.params.get(key);
    }

    /**
     * Updates or creates a planning parameter name, value pair
     * @param key String representing the name of the planning parameter
     * @param value String representing the value of the planning parameter
     */
    public void setParam(String key, String value) {
        this.params.put(key, value);
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
        String planString = "";
        planString +=  this.id + " [ " + this.start + " - " + this.end + " ]";
        for (String key : this.params.keySet()) {
            planString += "\n  " + key + ": " + this.params.get(key);
        }
        return planString;
    }
}
