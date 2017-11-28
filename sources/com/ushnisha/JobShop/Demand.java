package com.ushnisha.JobShop;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * A class the represents customer demand.
 */
public class Demand {

    private Plan plan;
    private String customer_id;
    private String id;
    private SKU sku;
    private LocalDateTime duedate;
    private LocalDateTime plandate;
    private long quantity;
    private long priority;
    private List<TaskPlan> delivery_taskplans;

    /**
     * Constructor returns a Demand object based on several input parameters
     * @param id String representing a unique demand ID
     * @param cid String representing the customer ID of the customer placing the demand
     * @param s SKU for which we are placing this demand
     * @param dd LocalDateTime representing the due date of the demand
     * @param p long value representing the priority of the demand.
     *          The lower this value, the higher the priority of the demand
     * @param pln Plan to which this demand belongs
     */
    public Demand(String id, String cid, SKU s, LocalDateTime dd,
                  long q, long p, Plan pln) {
        this.id = id;
        this.customer_id = cid;
        this.sku = s;
        this.duedate = dd;
        this.quantity = q;
        this.priority = p;
        this.delivery_taskplans = new ArrayList<TaskPlan>();
        this.plan = pln;
        this.plandate = null;
    }

    /**
     * Returns the ID of the demand
     * @return demand id that uniquely identifies this demand (given the plan)
     */
    public String getID() {
        return this.id;
    }

    /**
     * Return customer ID of the demand
     * @return the customer id of the demand
     */
    public String getCustomerID() {
        return this.customer_id;
    }

    /**
     * Returns the SKU for which the demand is placed
     * @return SKU for which the demand is placed
     */
    public SKU getSKU() {
        return this.sku;
    }

    /**
     * Returns the demand priority
     * @return priority of the demand
     */
    public long getPriority() {
        return this.priority;
    }

    /**
     * Returns the demand quantity
     * @return quantity for which we are placing the demand
     */
    public long getQuantity() {
        return this.quantity;
    }

    /**
     * Returns the date on which the demand is requested
     * @return due date of the demand
     */
    public LocalDateTime getDueDate() {
        return this.duedate;
    }

    /**
     * Returns the plan to which this demand belongs
     * @return Plan which this demand is a part of
     */
    public Plan getPlan() {
        return this.plan;
    }

    /**
     * Returns a list of TaskPlan objects that satisfy this demand
     * @return List<TaskPlan> that are used to satisfy this demand
     */
    public List<TaskPlan> getDeliveryTaskPlans() {
        return this.delivery_taskplans;
    }

    /**
     * Adds a TaskPlan to the list of task plans that satisfy this demand
     * @param tp TaskPlan that partially or fully satisfies this demand
     */
    public void addDeliveryTaskPlan(TaskPlan tp) {
        this.delivery_taskplans.add(tp);
    }

    /**
     * Ask the demand to go plan itself and then compute its planned date/quantity
     */
    public void plan() {
        Request req = new Request(this.id, this.quantity, this.duedate, this.plan);
        Promise promise = this.sku.getDeliveryTask().request(req);
        this.delivery_taskplans = promise.getTaskPlans();
        this.updatePlanDate();
    }


    /**
     * Updates the planned date/quantity of the demand based on the
     * delivery taskplans that go to meet this demand
     */
    private void updatePlanDate() {

        plandate = LocalDateTime.MIN;

        for (TaskPlan t : this.delivery_taskplans) {
            if (t.getEnd().isAfter(plandate)) {
                plandate = t.getEnd();
            }
        }
    }

    /**
     * Returns the string representation of the demand
     * @return String that represents the demand (for output/log purposes)
     */
    public String toString() {
        return this.sku + "-" + this.id + "; Due Date: " + this.duedate;
    }
}
