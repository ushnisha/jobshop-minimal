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

import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

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
                                        .sorted(Comparator.comparing(Demand::getPriority))
                                        .collect(Collectors.toList());

        // Plan the demands one by one
        for (Demand dem : demands) {
            dem.plan();
        }
    }
}
