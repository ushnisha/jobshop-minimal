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

/**
 * A Request object that can be used by a demand or a Task to propagate
 * a demand upstream.  It involves a quantity to be delivered by a given time
 * under the constraints of a given Plan
 */
public class Request {

    private Demand demand;
    private long quantity;
    private LocalDateTime date;
    private Plan plan;

    /**
     * Construtor for the request
     * @param dmd Demand representing the demand for which we are creating
     *            this request.
     * @param qty long value of the quantity being demanded
     * @param dt LocalDateTime representing the date by which this Request
     *           must be satisfied
     * @param p Plan instance that provides additional constraints that
     *          must be used during planning to satisfy this Request
     */
    public Request (Demand dmd, long qty, LocalDateTime dt, Plan p) {
        this.demand = dmd;
        this.quantity = qty;
        this.date = dt;
        this.plan = p;
    }

    /**
     * Returns the demand for which this Request is being made
     * @return Demand value representing the unique Demand
     *         for which the Request was created
     */
    public Demand getDemand() {
        return this.demand;
    }


    /**
     * Returns the unique ID of the demand for which this Request is being made
     * @return String value representing the unique identity of the Demand
     *         for which the Request was created
     */
    public String getID() {
        return this.demand.getID();
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
