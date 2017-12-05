package com.ushnisha.JobShop;

import java.util.List;
import java.util.stream.Collectors;

public class SimpleJobShopSolver {

    private JobShop js;

    public SimpleJobShopSolver(JobShop j) {
        this.js = j;
    }

    public void runStaticAnalysis(Plan p) {

        // Calculate EPST and LPST of the different Tasks

        // Start with the demands and iterate backwards
        // to calculate the LPST's
        //
        // Once LPST is updated, and we reach the upstream most chain
        // of a demand, then we calculate EPST and propagate forwards
        //
        List<Demand> demands = js.getDemands().values().stream()
                                        .filter(d -> d.getPlan().equals(p))
                                        .collect(Collectors.toList());
                                        
		// Calculate the level of different Tasks and Workcenters
		// that require planning based on above demands
	
    }

    public void generatePlan(Plan plan) {

        List<Demand> demands = js.getDemands().values().stream()
                                        .filter(d -> d.getPlan().equals(plan))
                                        .collect(Collectors.toList());

        // Plan the demands one by one
        for (Demand dem : demands) {
            dem.plan();
        }
    }
}