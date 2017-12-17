/**
 **********************************************************************
   Copyright (c) 2017 Arun Kunchithapatham
   All rights reserved.  This program and the accompanying materials
   are made available under the terms of the GNU AGPL v3.0
   which accompanies this distribution, and is available at
   https://www.gnu.org/licenses/agpl-3.0.en.html
   Contributors:
   Arun Kunchithapatham - Initial Contribution
 ***********************************************************************
 *
 */

package com.ushnisha.JobShop;

/** A class that represents a SKU (stock keeping unit).  Demand is placed
 * for a specific SKU.
 */
public class SKU {

    private String name;
    private String description;
    private Task delivery_task;

    /**
     * Constructors and returns an SKU object given input parameters
     * @param n String representing unique name of the SKU
     * @param d String a general/useful description of the SKU
     */
    public SKU(String n, String d) {
        this.name = n;
        this.description = d;
        this.delivery_task = null;

    }

    /**
     * Links the SKU to a Task that can "deliver" this SKU to a demand
     * @param t a Task that is used to deliver this SKU to a demand for this SKU
     */
    public void setDeliveryTask(Task t) {
        this.delivery_task = t;
    }

    /**
     * Returns the Task that is planned to meet a demand for this SKU
     * @return Task that is used to deliver this SKU to a demand for this SKU
     */
    public Task getDeliveryTask() {
        return this.delivery_task;
    }

    /**
     * Returns the name of this SKU
     * @return String that is the name of this SKU
     */
    public String getName() {
        return this.name;
    }

    /**
     * A string representation of the SKU
     * @return String value that represents this SKU
     */
    public String toString() {
        return this.name;
    }
}
