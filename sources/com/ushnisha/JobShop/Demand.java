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

import static com.ushnisha.JobShop.JobShop.DEBUG_LEVELS;
import static com.ushnisha.JobShop.JobShop.LOG;

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
    private long dueqty;
    private long planqty;
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
