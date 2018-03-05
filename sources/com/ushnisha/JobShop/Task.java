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
import java.time.temporal.ChronoUnit;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import java.util.Comparator;
import java.util.stream.Collectors;

import static com.ushnisha.JobShop.JobShop.DEBUG_LEVELS;
import static com.ushnisha.JobShop.JobShop.LOG;

/**
 * A class representing a Task that needs to be performed to meet a Demand.
 * A sequence of one or more tasks may be required to convert a raw material
 * into the finished SKU to meet the demand.  A Task may or may not load
 * a workcenter.
 */
class Task implements Partitionable {

    private String taskNum;
    private String taskid;
    private SKU sku;
    private Task pred;
    private Task succ;
    private int level;
    private long setup_time;
    private long time_per;
    private long min_lot_size;
    private long max_lot_size;
    private int partitionid;
    private Workcenter workcenter;
    private Map<Workcenter, Integer> workcenters;
    private List<TaskPlan> plans;
    private List<ReleasedWorkOrder> relworkorders;

    private Map<Demand, LocalDateTime> EPST;
    private Map<Demand, LocalDateTime> EPET;
    private Map<Demand, LocalDateTime> LPST;
    private Map<Demand, LocalDateTime> LPET;

    /**
     * Constructor for the task
     * @param id String representing the id/step of this task
     * @param s SKU representing the SKU for which this task is being
     *          planned
     * @param sut long value representing the setup time in minutes
     *            for this task
     * @param tp long value representing the time per unit piece
     *           processed (in minutes) by this task
     * @param minls long value representing the minimum lot size for
     *              a TaskPlan of this task
     * @param maxls long value representing the maximum lot size for
     *              a TaskPlan of this task
     */
    Task(String id, SKU s, long sut, long tp,
                long minls, long maxls) {

        this.taskid = id;
        this.sku = s;
        this.setup_time = sut;
        this.time_per = tp;
        this.min_lot_size = minls;
        this.max_lot_size = maxls;

        this.taskNum = this.sku.getName() + "-" + this.taskid;
        this.level = 0;
        this.workcenter = null;
        this.workcenters = new HashMap<Workcenter, Integer>();
        this.plans = new ArrayList<TaskPlan>();
        this.relworkorders = new ArrayList<ReleasedWorkOrder>();

        this.EPST = new HashMap<Demand, LocalDateTime>();
        this.EPET = new HashMap<Demand, LocalDateTime>();
        this.LPST = new HashMap<Demand, LocalDateTime>();
        this.LPET = new HashMap<Demand, LocalDateTime>();
    }

    /**
     * Set the successor for this task
     * @param t Task which is the successor of this Task
     */
    void setSuccessor(Task t) {
        this.succ = t;
    }

    /**
     * Set the predecessor for this task
     * @param t Task which is the predecessor of this Task
     */
    void setPredecessor(Task t) {
        this.pred = t;
    }

    /**
     * Return the successor for this task
     * @return Task which is the successor of this Task
     */
    Task getSuccessor() {
        return this.succ;
    }

    /**
     * Return the predecessor for this task
     * @return Task which is the predecessor of this Task
     */
    Task getPredecessor() {
        return this.pred;
    }

    /**
     * Returns the level of this task
     * @return int value representing the "level" of this task.
     *         Downstream tasks have a higher level than upstream tasks
     *         This value can be computed through static analysis of the
     *         JobShop model and can be used by planning algorithms
     */
    int getLevel() {
        return this.level;
    }

    /**
     * Sets the level of this task
     * @param l int value representing the computed level of this task
     */
    void setLevel(int l) {
        JobShop.LOG("Setting level of Task: " + this.toString() + " : " + l,
                    JobShop.DEBUG_LEVELS.MINIMAL);
        this.level = l;
    }

    /**
     * returns the partitionid of the Task
     * @return int value that represents the partitionid of the Task.
     */
    public int getPartitionId() {
         return this.partitionid;
    }

    /**
     * updates the partitionid field of the Task
     * @param pid int value representing the partitionid of the Task
     */
    public void setPartitionId(int pid) {
        this.partitionid = pid;
    }

    /**
     * propagates the partitionid field of the Task to related
     * tasks, skus, and workcenters
     * @param pid integer representing the partitionid of the object
     * @param check boolean value; if true, then propagate only if
     *        partitionid is not equal to pid
     */
    public void propagatePartitionId(int pid, boolean check) {

        if (check && this.partitionid == pid) {
            return;
        }

        this.partitionid = pid;

        this.getSKU().propagatePartitionId(this.partitionid, true);

        for (Workcenter w : this.getWorkcenters()) {
            w.propagatePartitionId(this.partitionid, true);
        }

        if (this.pred != null) {
            this.pred.propagatePartitionId(this.partitionid, true);
        }

        if (this.succ != null) {
            this.succ.propagatePartitionId(this.partitionid, true);
        }

        for (ReleasedWorkOrder rwo : this.relworkorders) {
            rwo.propagatePartitionId(this.partitionid, true);
        }
    }

    /**
     * Updates the alternate workcenters that can be loaded by TaskPlans
     * of this Task
     * @param w Workcenter that can be loaded by TaskPlans of this Task
     * @param priority Integer value representing the preference of the
     *                 Workcenter; a Workcenter with a lower priority
     *                 value may be given a higher preference to
     *                 load/use when planning a TaskPlan of this task
     */
    void addWorkcenter(Workcenter w, Integer priority) {
        this.workcenters.put(w, priority);
    }

    /**
     * Get a list of the workcenters associated with this task
     * @return List<Workcenter> list of workcenters associated with this task
     */
    Set<Workcenter> getWorkcenters() {
        return this.workcenters.keySet();
    }

    /**
     * Returns the list of TaskPlans planned for this Task
     * @return List<TaskPlan> associated with this Task
     */
    List<TaskPlan> getTaskPlans() {
        return this.plans;
    }

    /**
     * Returns the list of TaskPlans planned for this Task
     * which are also associated with a specific Plan
     * @param p Plan for which these TaskPlans are created
     * @return List<TaskPlan> associated with this Task & Plan
     *
     */
    List<TaskPlan> getTaskPlans(Plan p) {
        return this.plans.stream()
               .filter(t -> t.getPlan().equals(p))
               .collect(Collectors.toList());
    }

    /**
     * Associate a TaskPlan with this Task
     * @param tp TaskPlan that is associated with this Task
     */
    void addTaskPlan(TaskPlan tp) {
        this.plans.add(tp);
    }

    /**
     * When planning a TaskPlan of this Task, ask a Workcenter if it is
     * available for loading and plan according to the response from
     * the Workcenter
     * @param qty long value representing the Quantity to be planned
     *            for the TaskPlan
     * @param enddate LocalDateTime representing the date on or before
     *                which we want the TaskPlan to end
     * @param p Plan under whose PlanParams constraints we want the
     *          Workcenter to try to schedule the TaskPlan
     * @return DateRange value that represents the start and end dates
     *         of the TaskPlan as per Workcenter availability
     */
    private DateRange queryWorkcentersForEndBefore(long qty, LocalDateTime enddate, Plan p) {

        long baseLT = getBaseLT(qty);
        DateRange res_DateRange = new DateRange(LocalDateTime.MIN, LocalDateTime.MAX);
        boolean capacity_constrained = Boolean.parseBoolean(p.getParam("RESOURCE_CONSTRAINED"));

        List<Workcenter> wrks = this.workcenters.entrySet().stream()
                                .sorted(Map.Entry.comparingByValue())
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList());

        JobShop.LOG("Task: " + this.taskNum +
                    "; querying workcenter for END ON OR BEFORE " + enddate,
                    DEBUG_LEVELS.DETAILED);

        if (!capacity_constrained) {
            this.workcenter = wrks.get(0);
            res_DateRange = this.workcenter.queryEndBefore(enddate, baseLT, p);
        }
        else {

            Map<Workcenter,DateRange> wrkDRs = new HashMap<Workcenter,DateRange>();

            for (Workcenter w : wrks) {
                DateRange dr = w.queryEndBefore(enddate, baseLT, p);
                wrkDRs.put(w, dr);
                JobShop.LOG(this.workcenters.get(w) + ": " + w.getName() + ": " + dr,
                            DEBUG_LEVELS.DETAILED);
            }

            // First check in order of preferred workcenters to see if any of them
            // end exactly on the demanded enddate; if yes, then return that
            // workcenter and date - and we are done
            JobShop.LOG("Looking for exact enddate match", DEBUG_LEVELS.DETAILED);

            for (Workcenter w : wrks) {
                if (wrkDRs.get(w).getEnd().compareTo(enddate) == 0) {
                    JobShop.LOG("Found exact date match", DEBUG_LEVELS.DETAILED);
                    this.workcenter = w;
                    return wrkDRs.get(w);
                }
            }

            // If no workcenter can return an exact enddate match, then look for
            // workcenters that provide a date that is earlier than end date, but
            // closest to the requested enddate.

            boolean found = false;
            long delta = Long.MIN_VALUE;
            Workcenter bestWrk = wrks.get(0);

            for (Workcenter w : wrks) {
                if (wrkDRs.get(w).getEnd().compareTo(enddate) < 0) {
                    long diff = enddate.until(wrkDRs.get(w).getEnd(), ChronoUnit.MINUTES);

                    JobShop.LOG(this.workcenters.get(w) + ": " + w.getName() +
                                ": " + wrkDRs.get(w) + ", " + diff,
                                DEBUG_LEVELS.DETAILED);

                    if (diff > delta) {
                        found = true;
                        delta = diff;
                        bestWrk = w;
                        JobShop.LOG("Found earlier date match", DEBUG_LEVELS.DETAILED);
                    }
                }
            }

            if (found) {
                this.workcenter = bestWrk;
                return wrkDRs.get(bestWrk);
            }

            // If no workcenter can return a data earlier than the exact enddate
            // then look for the workcenter that gives the later date that is
            // closest to the requested enddate (minimize tardiness)
            delta = Long.MAX_VALUE;
            bestWrk = wrks.get(0);

            for (Workcenter w : wrks) {
                if (wrkDRs.get(w).getEnd().compareTo(enddate) > 0) {
                    long diff = enddate.until(wrkDRs.get(w).getEnd(), ChronoUnit.MINUTES);

                    JobShop.LOG(this.workcenters.get(w) + ": " + w.getName() +
                                ": " + wrkDRs.get(w) + ", " + diff,
                                DEBUG_LEVELS.DETAILED);

                    if (diff < delta) {
                        found = true;
                        delta = diff;
                        bestWrk = w;
                        JobShop.LOG("Found later date match", DEBUG_LEVELS.DETAILED);
                    }
                }
            }

            if (found) {
                this.workcenter = bestWrk;
                return wrkDRs.get(bestWrk);
            }
        }

        return res_DateRange;
    }

    /**
     * When planning a TaskPlan of this Task, ask a Workcenter if it is
     * available for loading and plan according to the response from
     * the Workcenter
     * @param qty long value representing the Quantity to be planned
     *            for the TaskPlan
     * @param startdate LocalDateTime representing the date on or after
     *                  which we want the TaskPlan to end
     * @param p Plan under whose PlanParams constraints we want the
     *          Workcenter to try to schedule the TaskPlan
     * @return DateRange value that represents the start and end dates
     *         of the TaskPlan as per Workcenter availability
     */
    private DateRange queryWorkcentersForStartAfter(long qty, LocalDateTime startdate, Plan p) {

        long baseLT = getBaseLT(qty);
        DateRange res_DateRange = new DateRange(LocalDateTime.MIN, LocalDateTime.MAX);
        boolean capacity_constrained = Boolean.parseBoolean(p.getParam("RESOURCE_CONSTRAINED"));

        List<Workcenter> wrks = this.workcenters.entrySet().stream()
                                .sorted(Map.Entry.comparingByValue())
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList());

        JobShop.LOG("Task: " + this.taskNum +
                    "; querying workcenter for START ON OR AFTER " + startdate,
                    DEBUG_LEVELS.DETAILED);

        if (!capacity_constrained) {
            this.workcenter = wrks.get(0);
            res_DateRange = this.workcenter.queryStartAfter(startdate, baseLT, p);
        }
        else {

            Map<Workcenter,DateRange> wrkDRs = new HashMap<Workcenter,DateRange>();

            for (Workcenter w : wrks) {
                DateRange dr = w.queryStartAfter(startdate, baseLT, p);
                wrkDRs.put(w, dr);
                JobShop.LOG(this.workcenters.get(w) + ": " + w.getName() + ": " + dr,
                            DEBUG_LEVELS.DETAILED);
            }

            // First check in order of preferred workcenters to see if any of them
            // end exactly on the demanded enddate; if yes, then return that
            // workcenter and date - and we are done
            JobShop.LOG("Looking for exact enddate match", DEBUG_LEVELS.DETAILED);

            for (Workcenter w : wrks) {
                if (wrkDRs.get(w).getStart().compareTo(startdate) == 0) {
                    JobShop.LOG("Found exact date match", DEBUG_LEVELS.DETAILED);
                    this.workcenter = w;
                    return wrkDRs.get(w);
                }
            }

            // If no workcenter can return an exact startdate match, then look for
            // workcenters that provide a date that is later than start date, but
            // closest to the requested startdate.

            boolean found = false;
            long delta = Long.MIN_VALUE;
            Workcenter bestWrk = wrks.get(0);

            for (Workcenter w : wrks) {
                if (wrkDRs.get(w).getStart().compareTo(startdate) > 0) {
                    long diff = startdate.until(wrkDRs.get(w).getStart(), ChronoUnit.MINUTES);
                    if (diff > delta) {
                        found = true;
                        delta = diff;
                        bestWrk = w;
                        JobShop.LOG("Found later date match", DEBUG_LEVELS.DETAILED);
                    }
                }
            }

            if (found) {
                this.workcenter = bestWrk;
                return wrkDRs.get(bestWrk);
            }

            // If no workcenter can return a data later than the exact start
            // then look for the workcenter that gives the earlier date that is
            // closest to the requested startdate (minimize build ahead)
            delta = Long.MAX_VALUE;
            bestWrk = wrks.get(0);

            for (Workcenter w : wrks) {
                if (wrkDRs.get(w).getStart().compareTo(startdate) < 0) {
                    long diff = startdate.until(wrkDRs.get(w).getStart(), ChronoUnit.MINUTES);
                    if (diff < delta) {
                        delta = diff;
                        bestWrk = w;
                        JobShop.LOG("Found earlier date match", DEBUG_LEVELS.DETAILED);
                    }
                }
            }

            if (found) {
                this.workcenter = bestWrk;
                return wrkDRs.get(bestWrk);
            }
        }

        return res_DateRange;
    }


    /**
     * Computes the base lead time of the TaskPlan for a specified
     * quantity to be planned.  This does not account for holidays or
     * efficiencies of the Workcenter loaded by the TaskPlan
     * @param quantity long value representing the quantity to be planned
     * @return long value representing the base lead time in minutes
     */
    private long getBaseLT(long quantity) {
        return this.setup_time + quantity * this.time_per;
    }

    /**
     * Function called by a downstream Task or Demand asking for a certain
     * quanity of the sku by a certian time.
     * Returns a Promise based on resource availability and the response
     * from its own upstream tasks (if any).
     * @param req Request that is made by the downstream Task or Demand
     * @return Promise that is the response to the input request, req
     */
    Promise request(Request req) {

        Demand dmd = req.getDemand();
        long origQty = req.getQuantity();
        long remQty = origQty;
        LocalDateTime due = req.getDate();
        Plan p = req.getPlan();

        List<Promise> allPromises = new ArrayList<Promise>();

        // First check with any ReleasedWorkOrders associated with this request
        Promise woPromise = checkReleasedWorkOrdersForQuantity(req);
        allPromises.add(woPromise);
        for (TaskPlan tp : woPromise.getTaskPlans()) {
            remQty -= tp.getQuantity();
        }

        if (remQty <= 0) {
            return combinePromises(dmd, allPromises);
        }

        // Next check for own workcenter availability (if loading workcenter)
        DateRange res_dateRange = null;
        if (this.workcenters.size() > 0) {
            res_dateRange = queryWorkcentersForEndBefore(remQty, due, p);
        }
        else {
            long baseLT = getBaseLT(remQty);
            LocalDateTime validStart = due.minusMinutes(baseLT);
            LocalDateTime validEnd = due;
            if (validStart.isBefore(p.getStart())) {
                validStart = p.getStart();
                validEnd = validStart.plusMinutes(baseLT);
            }
            res_dateRange = new DateRange(validStart, validEnd);
        }

        Request reqDelta = new Request(dmd, remQty, res_dateRange.getStart(), p);

        // Propagate the request to upstream tasks (predecessors) if any
        // and plan based on upstream response.  Then send own promise
        // to downstream requesting task/demand.
        //
        if (this.pred != null) {
            Promise promise = this.pred.request(reqDelta);
            allPromises.add(this.plan(reqDelta, promise));
        }
        else {
            return this.plan(reqDelta, res_dateRange);
        }

        return combinePromises(dmd, allPromises);
    }

    /**
     * Create the TaskPlans for the input Request and return a suitable Promise
     * This function is used for Tasks that have no predecessor; so there
     * is no need to propagate the request upstream beyond this point.
     *
     * @param req Request that is made by the downstream Task/Demand
     * @param dr DateRange to use for start and end of the TaskPlan being planned
     * @return Promise which contains the details of the TaskPlans planned
     */
    private Promise plan(Request req, DateRange dr) {

        JobShop.LOG("Planning Task: " + this.taskNum + " between " +
                    dr.getStart() + " and " + dr.getEnd() +
                    " on workcenter " + this.workcenter,
                    DEBUG_LEVELS.DETAILED);

        TaskPlan tp = new TaskPlan(this, req.getPlan(), this.workcenter, dr.getStart(), dr.getEnd(), req.getQuantity(), req.getDemand());
        this.plans.add(tp);
        if (this.workcenter != null) {
            this.workcenter.addTaskPlan(tp);
        }
        List<TaskPlan> tps = new ArrayList<TaskPlan>();
        tps.add(tp);
        Promise p = new Promise(req.getDemand(), tps);
        return p;
    }

    /**
     * Create the TaskPlans for the input Request and return a suitable Promise
     * This function is used for Tasks that have a predecessor; so there
     * is need to plan based on the Promise returned by the predecessor
     * and then pass on a suitable promise to the successor Task/Demand
     *
     * @param req Request that is made by the downstream Task/Demand
     * @param promise Promise made by the predecessor that is used to
     *        plan the TaskPlans for this Task
     * @return Promise which contains the details of the TaskPlans planned
     *         and returned to the successor Task/Demand.
     */
    private Promise plan(Request req, Promise promise) {

        long qty = 0;
        LocalDateTime start = LocalDateTime.MIN;
        for (TaskPlan tp : promise.getTaskPlans()) {
            qty += tp.getQuantity();
            if (tp.getEnd().isAfter(start)) {
                start = tp.getEnd();
            }
        }

        // If the upstream taskplans have overplanned, then reduce the
        // current planned quantity to just the requested quantity
        // No need to overplan
        //
        if (qty > req.getQuantity()) {
            qty = req.getQuantity();
        }

        DateRange res_dateRange = null;
        if (this.workcenters.size() > 0) {
            res_dateRange = queryWorkcentersForStartAfter(qty, start, req.getPlan());
        }
        else {
            long baseLT = getBaseLT(qty);
            LocalDateTime validStart = start;
            LocalDateTime validEnd = start.plusMinutes(baseLT);
            if (validEnd.isAfter(req.getPlan().getEnd())) {
                validEnd = req.getPlan().getEnd();
                validStart = validStart.plusMinutes(baseLT);
            }
            res_dateRange = new DateRange(validStart, validEnd);
        }

        JobShop.LOG("Planning Task: " + this.taskNum + " between " +
                    res_dateRange.getStart() + " and " +
                    res_dateRange.getEnd() + " on workcenter " + this.workcenter,
                    DEBUG_LEVELS.DETAILED);

        TaskPlan tp = new TaskPlan(this, req.getPlan(), this.workcenter, res_dateRange.getStart(), res_dateRange.getEnd(), qty, req.getDemand());
        this.plans.add(tp);
        if (this.workcenter != null) {
            this.workcenter.addTaskPlan(tp);
        }
        List<TaskPlan> tps = new ArrayList<TaskPlan>();
        tps.add(tp);
        Promise p = new Promise(req.getDemand(), tps);
        return p;
    }

    /**
     * Checks ReleasedWorkOrders associated with this Task for available
     * planned/released quantity first before trying to plan fresh
     * @param req Request which we are trying to satisfy
     * @return Promise representing the matching ReleasedWorkOrders
     */
     private Promise checkReleasedWorkOrdersForQuantity(Request req) {

        Demand dmd = req.getDemand();
        long origQty = req.getQuantity();
        long remQty = origQty;
        LocalDateTime due = req.getDate();
        Plan p = req.getPlan();

        List<Promise> allWOPromises = new ArrayList<Promise>();

        // First check ReleasedWorkOrder that match demand of this request
        // Search first from date of request to earlier dates
        List<ReleasedWorkOrder> type1_WO = this.relworkorders.stream()
                                   .filter(w -> w.getDemand() == dmd)
                                   .filter(w -> (w.getEnd().isEqual(due) ||
                                                 w.getEnd().isBefore(due)))
                                   .sorted(Comparator.comparing(ReleasedWorkOrder::getEnd).reversed())
                                   .collect(Collectors.toList());

        for (ReleasedWorkOrder wo : type1_WO) {
            if (remQty <= 0) {
                break;
            }
            Request woReq = new Request(dmd, remQty, due, p);
            Promise woPromise = wo.request(woReq);
            allWOPromises.add(woPromise);
            for (TaskPlan tp : woPromise.getTaskPlans()) {
                remQty -= tp.getQuantity();
            }
        }

        // Second check ReleasedWorkOrder that match demand of this request
        // but have a date later than this request
        List<ReleasedWorkOrder> type2_WO = this.relworkorders.stream()
                                   .filter(w -> w.getDemand() == dmd)
                                   .filter(w -> w.getEnd().isAfter(due))
                                   .sorted(Comparator.comparing(ReleasedWorkOrder::getEnd))
                                   .collect(Collectors.toList());

        for (ReleasedWorkOrder wo : type2_WO) {
            if (remQty <= 0) {
                break;
            }
            Request woReq = new Request(dmd, remQty, due, p);
            Promise woPromise = wo.request(woReq);
            allWOPromises.add(woPromise);
            for (TaskPlan tp : woPromise.getTaskPlans()) {
                remQty -= tp.getQuantity();
            }
        }

        // Third check ReleasedWorkOrders that are not attached to any demand
        // Search first from date of request to earlier dates
        List<ReleasedWorkOrder> type3_WO = this.relworkorders.stream()
                                   .filter(w -> w.getDemand() == null)
                                   .filter(w -> (w.getEnd().isEqual(due) ||
                                                 w.getEnd().isBefore(due)))
                                   .sorted(Comparator.comparing(ReleasedWorkOrder::getEnd).reversed())
                                   .collect(Collectors.toList());

        for (ReleasedWorkOrder wo : type3_WO) {
            if (remQty <= 0) {
                break;
            }
            Request woReq = new Request(dmd, remQty, due, p);
            Promise woPromise = wo.request(woReq);
            allWOPromises.add(woPromise);
            for (TaskPlan tp : woPromise.getTaskPlans()) {
                remQty -= tp.getQuantity();
            }
        }

        // Fourth check ReleasedWorkOrder that match demand of this request
        // but have a date later than this request
        List<ReleasedWorkOrder> type4_WO = this.relworkorders.stream()
                                   .filter(w -> w.getDemand() == null)
                                   .filter(w -> w.getEnd().isAfter(due))
                                   .sorted(Comparator.comparing(ReleasedWorkOrder::getEnd))
                                   .collect(Collectors.toList());

        for (ReleasedWorkOrder wo : type4_WO) {
            if (remQty <= 0) {
                break;
            }
            Request woReq = new Request(dmd, remQty, due, p);
            Promise woPromise = wo.request(woReq);
            allWOPromises.add(woPromise);
            for (TaskPlan tp : woPromise.getTaskPlans()) {
                remQty -= tp.getQuantity();
            }
        }

        return combinePromises(dmd, allWOPromises);
     }

    /**
     * Combine a list of Promises into a single promise
     * @param dmd Demand for which these promises have been generated
     * @param allPromises List<Promise> a list of input promises
     * @return Promise combined promise
     */
     private Promise combinePromises(Demand dmd, List<Promise> allPromises) {

        Promise finalPromise = new Promise(dmd, new ArrayList<TaskPlan>());

        // Combine all promises to a single promise and return
        for (Promise pr : allPromises) {
            for (TaskPlan tp : pr.getTaskPlans()) {
                finalPromise.addTaskPlan(tp);
            }
        }
        return finalPromise;
     }

     /**
     * Calculate EPST of a task
     * @param dmd Demand for which we are calculating EPST
     */
    void calculateEPST(Demand dmd) {

        long baseLT = getBaseLT(dmd.getDueQuantity());

        LocalDateTime epst = dmd.getPlan().getStart();
        LocalDateTime epet = epst.plusMinutes(baseLT);

        if (this.pred != null) {
            if (this.pred.getEPET(dmd).isAfter(epst)) {
                epst = this.pred.getEPET(dmd);
                epet = epst.plusMinutes(baseLT);
            }
        }

        this.EPST.put(dmd, epst);
        this.EPET.put(dmd, epet);

        if (this.workcenters.size() > 0) {
            List<Workcenter> wrks = this.workcenters.entrySet().stream()
                                    .sorted(Map.Entry.comparingByValue())
                                    .map(Map.Entry::getKey)
                                    .collect(Collectors.toList());

            JobShop.LOG("Found: " + this.workcenters.size() + " workcenters...",
                        JobShop.DEBUG_LEVELS.MAXIMAL);

            DateRange minmax = new DateRange(dmd.getPlan().getEnd(), dmd.getPlan().getEnd());
            for (Workcenter w : wrks) {
                JobShop.LOG("Processing workcenter: " + w.getName(),
                            JobShop.DEBUG_LEVELS.MAXIMAL);
                Calendar cal = w.getCalendar();
                DateRange dr = CalendarUtils.calcStartAfter(cal, epst, baseLT);
                if (dr.getStart().isBefore(minmax.getStart())) {
                    minmax = new DateRange(dr.getStart(), minmax.getEnd());
                }
                if (dr.getEnd().isBefore(minmax.getEnd())) {
                    minmax = new DateRange(minmax.getStart(), dr.getEnd());
                }
            }

            if (minmax.getStart().isAfter(epst)) {
                epst = minmax.getStart();
                this.EPST.put(dmd, epst);
            }
            if (minmax.getEnd().isAfter(epet)) {
                epet = minmax.getEnd();
                this.EPET.put(dmd, epet);
            }
        }

        JobShop.LOG("Demand: " + dmd.getID() +
                    "; Task: " + this.taskNum +
                    " has EPST: " + this.EPST.get(dmd) +
                    "; and EPET: " + this.EPET.get(dmd),
                    JobShop.DEBUG_LEVELS.MINIMAL);
    }

     /**
     * Calculate LPST of a task
     * @param dmd Demand for which we are calculating LPST
     */
    void calculateLPST(Demand dmd) {

        long baseLT = getBaseLT(dmd.getDueQuantity());

        LocalDateTime lpet = dmd.getDueDate();
        LocalDateTime lpst = lpet.minusMinutes(baseLT);

        if (this.succ != null) {
            if (this.succ.getLPST(dmd).isBefore(lpet)) {
                lpet = this.succ.getLPST(dmd);
                lpst = lpet.minusMinutes(baseLT);
            }
        }

        this.LPET.put(dmd, lpet);
        this.LPST.put(dmd, lpst);

        if (this.workcenters.size() > 0) {
            List<Workcenter> wrks = this.workcenters.entrySet().stream()
                                    .sorted(Map.Entry.comparingByValue())
                                    .map(Map.Entry::getKey)
                                    .collect(Collectors.toList());

            JobShop.LOG("Found: " + this.workcenters.size() + " workcenters...",
                        JobShop.DEBUG_LEVELS.MAXIMAL);

            DateRange minmax = new DateRange(dmd.getPlan().getStart(), dmd.getPlan().getStart());
            for (Workcenter w : wrks) {
                JobShop.LOG("Processing workcenter: " + w.getName(),
                            JobShop.DEBUG_LEVELS.MAXIMAL);
                Calendar cal = w.getCalendar();
                DateRange dr = CalendarUtils.calcEndBefore(cal, lpet, baseLT);
                if (dr.getStart().isAfter(minmax.getStart())) {
                    minmax = new DateRange(dr.getStart(), minmax.getEnd());
                }
                if (dr.getEnd().isAfter(minmax.getEnd())) {
                    minmax = new DateRange(minmax.getStart(), dr.getEnd());
                }
            }

            if (minmax.getStart().isAfter(lpst)) {
                lpst = minmax.getStart();
                this.LPST.put(dmd, lpst);
            }
            if (minmax.getEnd().isAfter(lpet)) {
                lpet = minmax.getEnd();
                this.LPET.put(dmd, lpet);
            }
        }

        if (this.LPST.get(dmd).isBefore(this.EPST.get(dmd))) {
            JobShop.LOG("WARNING! Demand: " + dmd.getID() +
                        "; Task: " + this.taskNum +
                        " has LPST " + this.LPST.get(dmd) +
                        " < EPST " + this.EPST.get(dmd) +
                        " - Resetting LPST == EPST.",
                        JobShop.DEBUG_LEVELS.MINIMAL);
            this.LPST.put(dmd, this.EPST.get(dmd));
        }

        if (this.LPET.get(dmd).isBefore(this.EPET.get(dmd))) {
            JobShop.LOG("WARNING! Demand: " + dmd.getID() +
                        "; Task: " + this.taskNum +
                        " has LPET " + this.LPET.get(dmd) +
                        " < EPET " + this.EPET.get(dmd) +
                        " - Resetting LPET == EPET.",
                        JobShop.DEBUG_LEVELS.MINIMAL);
            this.LPET.put(dmd, this.EPET.get(dmd));
        }

        JobShop.LOG("Demand: " + dmd.getID() +
                    "; Task: " + this.taskNum +
                    " has LPST: " + this.LPST.get(dmd) +
                    "; and LPET: " + this.LPET.get(dmd),
                    JobShop.DEBUG_LEVELS.MINIMAL);
    }

     /**
     * Set level to input parameter and propagate downstream
     * @param lvl integer value representing the level of this Task
     */
    void setLevelAndPropagate(int lvl) {
        this.setLevel(lvl);
        if (this.succ != null) {
            this.succ.setLevelAndPropagate(lvl + 1);
        }
    }

     /**
     * Associates ReleasedWorkOrder for this Task
     * @param wo ReleasedWorkOrder that for this Task
     */
    void addReleasedWorkOrder(ReleasedWorkOrder wo) {
        this.relworkorders.add(wo);
    }

    /**
     * Returns the unique name/number for this task
     * @return String representing the unique name/number of this Task
     */
    String getTaskNumber() {
        return this.taskNum;
    }

    /**
     * Returns the id/step for this task
     * @return String representing the id/step of this Task
     */
    String getTaskID() {
        return this.taskid;
    }

    /**
     * Returns the SKU corresponding to this task
     * @return SKU representing the SKU for which this Task is planned
     */
    SKU getSKU() {
        return this.sku;
    }

    /**
     * Returns the number of workcenters associated with this task
     * @return int representing the number of workcenters associated with this task
     */
    int getWorkcenterCount() {
        return this.workcenters.size();
    }

    /**
     * Returns the time_per_unit associated with this task
     * @return int representing the time on a workcenter per unit of this task
     */
    long getTimePer() {
        return this.time_per;
    }

    /**
     * Returns the EPST of this task for a given demand
     * @param dmd Demand for which we are requesting EPST of this Task
     * @return LocalDateTime representing EPST of task for the Demand dmd
     */
    LocalDateTime getEPST(Demand dmd) {
        return this.EPST.get(dmd);
    }

    /**
     * Returns the EPET of this task for a given demand
     * @param dmd Demand for which we are requesting EPET of this Task
     * @return LocalDateTime representing EPET of task for the Demand dmd
     */
    LocalDateTime getEPET(Demand dmd) {
        return this.EPET.get(dmd);
    }

    /**
     * Returns the LPST of this task for a given demand
     * @param dmd Demand for which we are requesting LPST of this Task
     * @return LocalDateTime representing LPST of task for the Demand dmd
     */
    LocalDateTime getLPST(Demand dmd) {
        return this.LPST.get(dmd);
    }

    /**
     * Returns the LPET of this task for a given demand
     * @param dmd Demand for which we are requesting LPET of this Task
     * @return LocalDateTime representing LPET of task for the Demand dmd
     */
    LocalDateTime getLPET(Demand dmd) {
        return this.LPET.get(dmd);
    }

    /**
     * returns a string representation of the Task
     * for logging during partitioning
     * @return String value that represents the Task
     */
    public String partitionLogString() {
        return "Task : " + this.toString() + " belongs to partition " +
               this.partitionid;
    }

    /**
     * Returns a string representation of the Task
     * @return String value representing this Task for use in output/log
     */
    public String toString() {
        return this.taskNum;
    }
}
