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
import java.util.Map;
import java.util.HashMap;

import java.util.Collections;

/**
 * A class representing a ReleasedWorkOrder.  This is a plan instance of a ReleasedWorkOrder
 */
public class ReleasedWorkOrder {

    private String workorderid;
    private Integer lotid;
    private Task task;
    private Plan plan;
    private Workcenter workcenter;
    private LocalDateTime start;
    private LocalDateTime end;
    private long quantity;
    private Demand demand;
    private TaskPlan taskplan;
    private List<TaskPlan> allocated_taskplans;
    private Map<TaskPlan, Integer> workorder_lots;
    
    /**
     * Constructor for the ReleasedWorkOrder object
     * @param t Task for which we are creating a ReleasedWorkOrder instance
     * @param p Plan model under which we are planning this ReleasedWorkOrder
     * @param w Workcenter that we are loading with this ReleasedWorkOrder.
     *          This may be null if the ReleasedWorkOrder does not load a workcenter
     * @param st LocalDateTime representing the start of this ReleasedWorkOrder
     * @param en LocalDateTime representing the end of this ReleasedWorkOrder
     * @param qty long value representing the quantity for which this ReleasedWorkOrder is created
     * @param dmd Demand for which we are creating this ReleasedWorkOrder
     */
    public ReleasedWorkOrder(String woid, Integer lotid, Task t, Plan p,
                             Workcenter w, LocalDateTime st,
                             LocalDateTime en, long qty, Demand dmd) {
        this.workorderid = woid;
        this.lotid = lotid;
        this.task = t;
        this.plan = p;
        this.workcenter = w;
        this.start = st;
        this.end = en;
        this.quantity = qty;
        this.demand = dmd;
        this.allocated_taskplans = new ArrayList<TaskPlan>();
        this.workorder_lots = new HashMap<TaskPlan, Integer>();
        
        // Create Original UnPegged TaskPlan corresponding to the ReleasedWorkOrder
        //
        this.taskplan = new TaskPlan(t, p, w, st, en, qty, this);
        this.workorder_lots.put(this.taskplan, this.lotid);
        
        // Add TaskPlan to all relevant "owners"
        //
        t.addTaskPlan(this.taskplan);
        if (w != null) { 
            w.addTaskPlan(this.taskplan);
        }
    }
    
    /**
     * Function called by a Task or of the same type as this ReleasedWorkOrder
     * for a certain quanity of the sku by a certain time.
     * Returns a Promise based on available unallocated quantity of 
     * this ReleasedWorkOrder.
     * @param req Request that is made by the downstream Task or Demand
     * @return Promise that is the response to the input request, req
     */
    public Promise request(Request req) {
        
        Demand dmd = req.getDemand();
        long reqQty = req.getQuantity();
        LocalDateTime due = req.getDate();
        Plan p = req.getPlan();

        List<TaskPlan> tps = new ArrayList<TaskPlan>();

        // If demand of the request matches demand of ReleasedWorkOrder
        // assign ReleasedWorkOrder taskplan entirely to this request/demand
        // and return
        if (this.demand == dmd) {
            this.allocated_taskplans.add(this.taskplan);
            tps.add(this.taskplan);
            this.taskplan = null;            
        }
        // Else handle null case
        else if (this.demand == null) {
            long availQty = this.taskplan != null ? this.taskplan.getQuantity() : 0L;
            long promiseQty = availQty >= reqQty ? reqQty : availQty;
            if (promiseQty > 0) {
                long remQty = availQty - promiseQty;
                if (remQty == 0) {
                    this.allocated_taskplans.add(this.taskplan);
                    tps.add(this.taskplan);
                    this.taskplan.setDemand(dmd);
                    this.taskplan = null;
                }
                else {
                    this.taskplan.setQuantity(remQty);
                    TaskPlan tp = new TaskPlan(this.task, this.plan, this.workcenter,
                                            this.start, this.end, promiseQty,
                                            dmd, this);

                    this.workorder_lots.put(tp, this.getNextLotID());
                    this.allocated_taskplans.add(tp);
                    tps.add(tp);
                    this.task.addTaskPlan(tp);
                    if (this.workcenter != null) {
                        this.workcenter.addTaskPlan(tp);
                    }
                }
            }
        }
        
        return new Promise(dmd, tps);
    }

    /**
     * A string representation of the ReleasedWorkOrder
     * @return String representing the workorderid for output/log purposes
     */
    public String getID() {
        return this.workorderid;
    }

    /**
     * Returns the start time for this ReleasedWorkOrder
     * @return LocalDateTime representing the starting time of the ReleasedWorkOrder
     */
    public LocalDateTime getStart() {
        return this.start;
    }

    /**
     * Returns the end time for this ReleasedWorkOrder
     * @return LocalDateTime representing the ending time of the ReleasedWorkOrder
     */
    public LocalDateTime getEnd() {
        return this.end;
    }

    /**
     * Returns the quantity for this ReleasedWorkOrder
     * @return long value representing the quantity of the ReleasedWorkOrder
     */
    public long getQuantity() {
        return this.quantity;
    }

    /**
     * Returns the Plan for which we are creating this ReleasedWorkOrder
     * @return Plan for which we are creating this ReleasedWorkOrder
     */
    public Plan getPlan() {
        return this.plan;
    }

    /**
     * Returns a unique identifier of the demand for which we are
     * creating this ReleasedWorkOrder
     * @return String representing the unique identifier of the demand
     *         for which we are creating this ReleasedWorkOrder
     */
    public String getDemandID() {
        if (this.demand != null) {
            return this.demand.getID();
        }
        else {
            return "null";
        }
    }

    /**
     * Returns the Task that this ReleasedWorkOrder is a planning instance of
     * @return Task representing the Task corresponding to this ReleasedWorkOrder
     */
    public Task getTask() {
        return this.task;
    }

    /**
     * Returns the Workcenter that this ReleasedWorkOrder is a planned on
     * @return Workcenter representing the Workcenter this ReleasedWorkOrder loads
     */
    public Workcenter getWorkcenter() {
        return this.workcenter;
    }
    
    /**
     * Returns the Demand that this ReleasedWorkOrder is a planned for
     * @return Demand representing the Demand this ReleasedWorkOrder is planned for
     */
    public Demand getDemand() {
        return this.demand;
    }

    /**
     * Returns the TaskPlans that this ReleasedWorkOrder has created
     * @return List<TaskPlan> representing the TaskPlans corresponding to
     *                        this ReleasedWorkOrder
     */
    public List<TaskPlan> getTaskPlans() {
        return this.allocated_taskplans;
    }

    /* Returns the LotID of the TaskPlan associated with this ReleasedWorkOrder
     * @param tp TaskPlan for which we want to find the LotID
     * @return Integer value representing the LotID of the input TaskPlan
     */
    public Integer getLotID(TaskPlan tp) {
        return this.workorder_lots.get(tp);
    }

    /**
     * Returns the next available lotID number (1 greater than the max
     * lotid value of all taskplans associated with this ReleasedWorkOrder
     * @return Integer value representing next lotID to assign
     */
     public Integer getNextLotID() {
         Integer maxLotID = Collections.max(this.workorder_lots.values());
         return maxLotID + 1;
     }
     
    /**
     * A string representation of the ReleasedWorkOrder
     * @return String representing the ReleasedWorkOrder for output/log purposes
     */
    public String toString() {
        return this.workorderid;
    }
}
