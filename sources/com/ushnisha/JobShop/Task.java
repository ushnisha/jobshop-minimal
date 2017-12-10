package com.ushnisha.JobShop;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Comparator;
import java.util.stream.Collectors;

import static com.ushnisha.JobShop.JobShop.DEBUG_LEVELS;
import static com.ushnisha.JobShop.JobShop.DEBUG;

/**
 * A class representing a Task that needs to be performed to meet a Demand.
 * A sequence of one or more tasks may be required to convert a raw material
 * into the finished SKU to meet the demand.  A Task may or may not load
 * a workcenter.
 */
public class Task {

    private String taskNum;
    private SKU sku;
    private Task pred;
    private Task succ;
    private int level;
    private long setup_time;
    private long time_per;
    private long min_lot_size;
    private long max_lot_size;
    private Workcenter workcenter;
    private SortedMap<Integer, Workcenter> workcenters;
    private List<TaskPlan> plans;

    /**
     * Constructor for the task
     * @param n String representing the unique name of the task
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
    public Task(String n, SKU s, long sut, long tp,
                long minls, long maxls) {

        this.taskNum = n;
        this.sku = s;
        this.setup_time = sut;
        this.time_per = tp;
        this.min_lot_size = minls;
        this.max_lot_size = maxls;

        this.level = 0;
        this.workcenter = null;
        this.workcenters = new TreeMap<Integer, Workcenter>();
        this.plans = new ArrayList<TaskPlan>();
    }

    /**
     * Set the successor for this task
     * @param t Task which is the successor of this Task
     */
    public void setSuccessor(Task t) {
        this.succ = t;
    }

    /**
     * Set the predecessor for this task
     * @param t Task which is the predecessor of this Task
     */
    public void setPredecessor(Task t) {
        this.pred = t;
    }

    /**
     * Return the successor for this task
     * @return Task which is the successor of this Task
     */
    public Task getSuccessor() {
        return this.succ;
    }

    /**
     * Return the predecessor for this task
     * @return Task which is the predecessor of this Task
     */
    public Task getPredecessor() {
        return this.pred;
    }

    /**
     * Returns the level of this task
     * @return int value representing the "level" of this task.
     *         Upstream tasks have a higher level than downstream tasks
     *         This value can be computed through static analysis of the
     *         JobShop model and can be used by planning algorithms
     */
    public int getLevel() {
        return this.level;
    }

    /**
     * Sets the level of this task
     * @param l int value representing the computed level of this task
     */
    public void setLevel(int l) {
        this.level = l;
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
    public void addWorkcenter(Workcenter w, Integer priority) {
        this.workcenters.put(priority, w);
    }

    /**
     * Returns the list of TaskPlans planned for this Task
     * @return List<TaskPlan> associated with this Task
     */
    public List<TaskPlan> getTaskPlans() {
        return this.plans;
    }

    /**
     * Returns the list of TaskPlans planned for this Task
     * which are also associated with a specific Plan
     * @param p Plan for which these TaskPlans are created
     * @return List<TaskPlan> associated with this Task & Plan
     *
     */
    public List<TaskPlan> getTaskPlans(Plan p) {
        return this.plans.stream()
               .filter(t -> t.getPlan().equals(p))
               .collect(Collectors.toList());
    }

    /**
     * Associate a TaskPlan with this Task
     * @param tp TaskPlan that is associated with this Task
     */
    public void addTaskPlan(TaskPlan tp) {
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
    public DateRange queryWorkcentersForEndBefore(long qty, LocalDateTime enddate, Plan p) {

        long baseLT = getBaseLT(qty);
        DateRange res_DateRange = new DateRange(LocalDateTime.MIN, LocalDateTime.MAX);
        boolean capacity_constrained = Boolean.parseBoolean(p.getParam("RESOURCE_CONSTRAINED"));

        if (DEBUG.ordinal() >= DEBUG_LEVELS.DETAILED.ordinal()) {
			System.out.println("Task: " + this.taskNum + "; querying workcenter for END ON OR BEFORE enddate");
		}
        if (!capacity_constrained) {
            this.workcenter = this.workcenters.get(this.workcenters.firstKey());
            res_DateRange = this.workcenter.queryEndBefore(enddate, baseLT, p);
        }
        else {
            List<Integer> wkeys = this.workcenters.keySet().stream()
                                    .sorted()
                                    .collect(Collectors.toList());

            for (Integer i : wkeys) {
                Workcenter w = this.workcenters.get(i);
                DateRange dr = w.queryEndBefore(enddate, baseLT, p);
                if (dr.getEnd().compareTo(res_DateRange.getEnd()) < 0) {
                    res_DateRange = dr;
                    this.workcenter = w;
                    if (res_DateRange.getEnd().compareTo(enddate) <= 0) {
                        return res_DateRange;
                    }
                }
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
    public DateRange queryWorkcentersForStartAfter(long qty, LocalDateTime startdate, Plan p) {

        long baseLT = getBaseLT(qty);
        DateRange res_DateRange = new DateRange(LocalDateTime.MIN, LocalDateTime.MAX);
        boolean capacity_constrained = Boolean.parseBoolean(p.getParam("RESOURCE_CONSTRAINED"));

        if (DEBUG.ordinal() >= DEBUG_LEVELS.DETAILED.ordinal()) {
			System.out.println("Task: " + this.taskNum + "; querying workcenter for START ON OR AFTER startdate");
		}
        if (!capacity_constrained) {
            this.workcenter = this.workcenters.get(this.workcenters.firstKey());
            res_DateRange = this.workcenter.queryStartAfter(startdate, baseLT, p);
        }
        else {
            List<Integer> wkeys = this.workcenters.keySet().stream()
                                    .sorted()
                                    .collect(Collectors.toList());

            for (Integer i : wkeys) {
                Workcenter w = this.workcenters.get(i);
                DateRange dr = w.queryStartAfter(startdate, baseLT, p);
                if (dr.getStart().compareTo(res_DateRange.getStart()) > 0) {
                    res_DateRange = dr;
                    this.workcenter = w;
                    if (res_DateRange.getStart().compareTo(startdate) >= 0) {
                        return res_DateRange;
                    }
                }
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
    public long getBaseLT(long quantity) {
        return this.setup_time + quantity * this.time_per;
    }

    /**
     * A Promise that is made in response to a request from the
     * downstream Task or Demand.
     * @param req Request that is made by the downstream Task or Demand
     * @return Promise that is the response to the input request, req
     */
    public Promise request(Request req) {

        //calculateLPST(req.getQuantity(), req.getDate(), req.getPlan());
        DateRange res_dateRange = null;
        if (this.workcenters.size() > 0) {
            res_dateRange = queryWorkcentersForEndBefore(req.getQuantity(), req.getDate(), req.getPlan());
        }
        else {
            long baseLT = getBaseLT(req.getQuantity());
            LocalDateTime validStart = req.getDate().minusMinutes(baseLT);
            LocalDateTime validEnd = req.getDate();
            if (validStart.isBefore(req.getPlan().getStart())) {
                validStart = req.getPlan().getStart();
                validEnd = validStart.plusMinutes(baseLT);
            }
            res_dateRange = new DateRange(validStart, validEnd);
        }

        if (this.pred != null) {
            Request r = new Request(req.getID(), req.getQuantity(), res_dateRange.getStart(), req.getPlan());
            Promise promise = this.pred.request(r);
            return this.plan(req, promise);
        }
        else {
            return this.plan(req, res_dateRange);
        }
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
    public Promise plan(Request req, DateRange dr) {

        TaskPlan tp = new TaskPlan(this, req.getPlan(), this.workcenter, dr.getStart(), dr.getEnd(), req.getQuantity(), req.getID());
        this.plans.add(tp);
        if (this.workcenter != null) {
            this.workcenter.addTaskPlan(tp);
        }
        List<TaskPlan> tps = new ArrayList<TaskPlan>();
        tps.add(tp);
        Promise p = new Promise(req.getID(), tps);
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
    public Promise plan(Request req, Promise promise) {

        long qty = 0;
        LocalDateTime start = LocalDateTime.MIN;
        for (TaskPlan tp : promise.getTaskPlans()) {
            qty += tp.getQuantity();
            if (tp.getEnd().isAfter(start)) {
                start = tp.getEnd();
            }
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

        TaskPlan tp = new TaskPlan(this, req.getPlan(), this.workcenter, res_dateRange.getStart(), res_dateRange.getEnd(), qty, req.getID());
        this.plans.add(tp);
        if (this.workcenter != null) {
            this.workcenter.addTaskPlan(tp);
        }
        List<TaskPlan> tps = new ArrayList<TaskPlan>();
        tps.add(tp);
        Promise p = new Promise(req.getID(), tps);
        return p;

    }

    /**
     * Returns the unique name/number for this task
     * @return String representing the unique name/number of this Task
     */
    public String getTaskNumber() {
        return this.taskNum;
    }

    /**
     * Returns a string representation of the Task
     * @return String value representing this Task for use in output/log
     */
    public String toString() {
        return this.taskNum;
    }
}

