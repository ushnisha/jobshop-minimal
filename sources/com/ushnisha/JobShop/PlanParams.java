package com.ushnisha.JobShop;

import java.util.Map;
import java.util.HashMap;

/**
 * A class that represents the planning parameters that will guide the
 * planning algorithm or solver that uses this JobShop model
 */
public class PlanParams {

    private Plan plan;
    private Map<String, String> params;

    /**
     * Constructor for PlanParams object
     * @param p the Plan with which this planning parameters is associated
     */
    public PlanParams (Plan p) {
        this.plan = p;
        this.params = new HashMap<String, String>();
    }

    /**
     * Returns the planning parameters of this object
     * @return Map<String, String> representing a mapping of planning parameter names
     *         and the corresponding parameter value.
     */
    public Map<String, String> getParams() {
        return this.params;
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

}
