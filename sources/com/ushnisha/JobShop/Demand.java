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

import static com.ushnisha.JobShop.JobShop.DEBUG_LEVELS;
import static com.ushnisha.JobShop.JobShop.LOG;

/**
 * A class the represents customer demand.
 */
public class Demand implements Partitionable {

    private Plan plan;
    private String customer_id;
    private String id;
    private SKU sku;
    private LocalDateTime duedate;
    private LocalDateTime plandate;
    private long dueqty;
    private long planqty;
    private long priority;
    private int partitionid;
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
        this.dueqty = q;
        this.priority = p;
        this.plan = pln;

        this.delivery_taskplans = new ArrayList<TaskPlan>();
        this.plandate = null;
        this.planqty = 0;
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
    public long getDueQuantity() {
        return this.dueqty;
    }

    /**
     * Returns the planned quantity
     * @return quantity for which we are planning the demand
     */
    public long getPlanQuantity() {
        return this.planqty;
    }

    /**
     * Returns the date on which the demand is requested
     * @return due date of the demand
     */
    public LocalDateTime getDueDate() {
        return this.duedate;
    }

    /**
     * Returns the date on which the demand is planned
     * @return plan date of the demand
     */
    public LocalDateTime getPlanDate() {
        return this.plandate;
    }

    /**
     * Returns the plan to which this demand belongs
     * @return Plan which this demand is a part of
     */
    public Plan getPlan() {
        return this.plan;
    }

    /**
     * Returns the name of the plan to which this demand belongs
     * @return String name of the plan which this demand is a part of
     */
    public String getPlanName() {
        return this.plan.getID();
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

        JobShop.LOG("Planning for demand: " + this.id, DEBUG_LEVELS.DETAILED);

        Request req = new Request(this, this.dueqty, this.duedate, this.plan);
        Promise promise = this.sku.getDeliveryTask().request(req);
        this.delivery_taskplans = promise.getTaskPlans();
        this.updatePlanData();
    }


    /**
     * Updates the planned date/quantity of the demand based on the
     * delivery taskplans that go to meet this demand
     */
    private void updatePlanData() {

        plandate = LocalDateTime.MIN;
        planqty = 0;

        for (TaskPlan t : this.delivery_taskplans) {
            planqty += t.getQuantity();
            if (t.getEnd().isAfter(plandate)) {
                plandate = t.getEnd();
            }
        }
    }

    /**
     * returns the partitionid of the Demand
     * @return int value that represents the partitionid of the Demand
     */
    public int getPartitionId() {
         return this.partitionid;
    }

    /**
     * sets the partitionid of the Demand
     * @param pid int value representing the partitionid of the Demand
     */
    public void setPartitionId(int pid) {
        this.partitionid = pid;
    }

    /**
     * propagate partitionid to the SKU
     * @param pid integer representing the partitionid of the object
     * @param check boolean value; if true, then propagate only if
     *        partitionid is not equal to pid
     */
    public void propagatePartitionId(int pid, boolean check) {

        // do nothing - does not make sense to propagate for demands
        // A demand can propagate its partitionid only to a SKU; but
        // a SKU cannot update the demand when it changes
        // We simply need to call the updatePartitionId function
        // after completing partitioning.
    }

    /**
     * update partitionid from the SKU
     */
    public void updatePartitionId() {
        this.partitionid = this.getSKU().getPartitionId();
    }

    /**
     * returns a string representation of the Demand
     * for logging during partitioning
     * @return String value that represents the Demand
     */
    public String partitionLogString() {
        return "Demand : " + this.getID() + " belongs to partition " +
               this.partitionid;
    }

    /**
     * Returns a string representation of the demand suitable for writing
     * to an output csv file
     * @return String that represents the demand (for output purposes)
     */
    public String dmdplanString() {
        return this.plan.getID() + "," + this.sku + "," + this.id +
               "," + this.priority + "," + this.dueqty +
               "," + this.duedate + "," + this.planqty +
               "," + this.plandate;
    }

    /**
     * Returns the string representation of the demand
     * @return String that represents the demand (for output/log purposes)
     */
    public String toString() {
        return this.sku + "-" + this.id + 
               "; Priority: " + this.priority +
               "; Due: " + this.dueqty + " on " + this.duedate + 
               "; Planned: " + this.planqty + " on " + this.plandate;
    }
}
