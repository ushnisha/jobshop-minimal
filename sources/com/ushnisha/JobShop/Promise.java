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

import java.util.List;
import java.util.ArrayList;

/**
 * A class that represents a response to a Request made during planning
 * The upstream object creates a Promise as a response to a Request and
 * sends the Promise back to the downstream requesting object.
 */
public class Promise {

    private Demand demand;
    private List<TaskPlan> taskplans;

    /**
     * Constructor for promise
     * @param dmd Demand value representing the Demand associated
     *                      with the Request for which this promise is a response.
     * @param tps List<TaskPlan> which are the TaskPlans that have been planned
     *            in response to the request.  Note that it is possible that
     *            a Promise delivers a different quantity than requested and
     *            on a different date than requested.  It is up to the requestor
     *            to accept the Promise or reject it.
     */
    public Promise (Demand dmd, List<TaskPlan> tps) {
        this.demand = dmd;
        this.taskplans = tps;
    }

    /**
     * Returns the Demand for this promise
     * @return demand Demand value representing the Demand associated
     *                       with the Request for which this promise is a response.
     */
    public Demand getDemand() {
        return this.demand;
    }

    /**
     * Returns the ID for this promise
     * @return id String value representing the ID of the promise.  Typically
     *            this is the same as the unique identifier of the Demand
     *            associated with the Request for which this promise is a response.
     */
    public String getID() {
        return this.demand.getID();
    }

    /**
     * Add an input TaskPlan to the list of TaskPlans contained in this promise
     * @param tp TaskPlan the TaskPlan to add to the Promise
     */
     public void addTaskPlan(TaskPlan tp) {
         this.taskplans.add(tp);
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
