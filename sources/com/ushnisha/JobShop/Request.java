package com.ushnisha.JobShop;

import java.time.LocalDateTime;

/**
 * A Request object that can be used by a demand or a Task to propagate
 * a demand upstream.  It involves a quantity to be delivered by a given time
 * under the constraints of a given Plan
 */
public class Request {

    private String id;
    private long quantity;
    private LocalDateTime date;
    private Plan plan;

    /**
     * Construtor for the request
     * @param id String representation of the request ID.  It is assumed
     *           that this id will correspond to a unique demand identifier
     *           of the demand for which this Request is being made
     * @param qty long value of the quantity being demanded
     * @param dt LocalDateTime representing the date by which this Request
     *           must be satisfied
     * @param p Plan instance that provides additional constraints that
     *          must be used during planning to satisfy this Request
     */
    public Request (String id, long qty, LocalDateTime dt, Plan p) {
        this.id = id;
        this.quantity = qty;
        this.date = dt;
        this.plan = p;
    }

    /**
     * Returns the unique ID of the demand for which this Request is being made
     * @return String value representing the unique identity of the Demand
     *         for which the Request was created
     */
    public String getID() {
        return this.id;
    }

    /**
     * Returns the quantity being requested
     * @return long value which is the quantity being requested.  Note
     *         that this quantity does not need to match the requested
     *         quantity since we may have other constraints that increase
     *         or decrease this quantity
     */
    public long getQuantity() {
        return this.quantity;
    }

    /**
     * Return the date by which this Request must be fulfilled
     * @return LocalDateTime representing the date by which the requested
     *         quantity must be provided by the upstream object that is sent the request
     */
    public LocalDateTime getDate() {
        return this.date;
    }

    /**
     * Returns the Plan whose parameters specify the constraints on how this
     * request must be planned
     * @return Plan which controls how the request is planned
     */
    public Plan getPlan() {
        return plan;
    }
}
