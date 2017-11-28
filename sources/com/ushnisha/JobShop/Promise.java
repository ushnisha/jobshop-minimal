package com.ushnisha.JobShop;

import java.util.List;
import java.util.ArrayList;

/**
 * A class that represents a response to a Request made during planning
 * The upstream object creates a Promise as a response to a Request and
 * sends the Promise back to the downstream requesting object.
 */
public class Promise {

    private String id;
    private List<TaskPlan> taskplans;

    /**
     * Constructor for promise
     * @param id String value representing the ID of the promise.  Typically
     *           this is the same as the unique identifier of the Demand
     *           associated with the Request for which this promise is a response.
     * @param tps List<TaskPlan> which are the TaskPlans that have been planned
     *            in response to the request.  Note that it is possible that
     *            a Promise delivers a different quantity than requested and
     *            on a different date than requested.  It is up to the requestor
     *            to accept the Promise or reject it.
     */
    public Promise (String id, List<TaskPlan> tps) {
        this.id = id;
        this.taskplans = tps;
    }

    /**
     * Returns the ID for this promise
     * @return id String value representing the ID of the promise.  Typically
     *            this is the same as the unique identifier of the Demand
     *            associated with the Request for which this promise is a response.
     */
    public String getID() {
        return this.id;
    }

    /**
     * Returns the list of TaskPlans that are planned as part of this promise
     * @return List<TaskPlan> which are the TaskPlans that have been planned
     *         in response to the request.  Note that it is possible that
     *         a Promise delivers a different quantity than requested and
     *         on a different date than requested.  It is up to the requestor
     *         to accept the Promise or reject it.
     */
    public List<TaskPlan> getTaskPlans() {
        return this.taskplans;
    }

}
