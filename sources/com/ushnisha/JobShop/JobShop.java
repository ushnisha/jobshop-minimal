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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.Charset;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.Comparator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

/**
 * The overall JobShop model class that is used to drive this application
 */
public class JobShop {

	public static enum DEBUG_LEVELS { NONE, MINIMAL, STANDARD, DETAILED, MAXIMAL };
	public static final DEBUG_LEVELS DEBUG = DEBUG_LEVELS.MINIMAL;
	
    private Map<String,Plan> plans;
    private Map<String,Calendar> calendars;
    private Map<String,SKU> skus;
    private Map<String,Demand> demands;
    private Map<String,Task> tasks;
    private Map<Task,TaskPlan> taskplans;
    private Map<String,Workcenter> workcenters;

    private Map<String,String> options;
    private String datadir;
    private Connection connection;
    private Statement statement;

    /**
     * Main function that is run for simulating a JobShop planning process
     * @param args String[] of input arguments.  Currently, assumes:
     *             args[0] - name of the plan for which we want to generate a plan
     *             If args[0] is not specified it will pick the first plan available
     *             after importing the Plan file from the data directory
     */
    public static void main(String args[]) {

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) { 
			System.out.println("\nA Minimal JobShop Planner");
		}
		
        JobShop jshop = new JobShop(args);
        Plan p = null;

        if (jshop.options.containsKey("default_plan")) {
            assert(jshop.plans.containsKey(jshop.options.get("default_plan")));
			p = jshop.plans.get(jshop.options.get("default_plan"));
		}
		else {
			String firstKey = (new ArrayList<String>(jshop.plans.keySet())).get(0);
			p = jshop.plans.get(firstKey);
		}
		
        SimpleJobShopSolver solver = new SimpleJobShopSolver(jshop);
        solver.runStaticAnalysis(p);
        solver.generatePlan(p);

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) { 
			jshop.print();
		}

    }

    /**
     * Constructor for the JobShop object
     * @param n Map<String,String> representing the options used to
     *          read the dataset and create the JobShop model.
     */
    public JobShop(String[] args) {
		
        this.plans = new HashMap<String,Plan>();
        this.calendars = new HashMap<String,Calendar>();
        this.skus = new HashMap<String,SKU>();
        this.demands = new HashMap<String,Demand>();
        this.tasks = new HashMap<String,Task>();
        this.taskplans = new HashMap<Task,TaskPlan>();
        this.workcenters = new HashMap<String,Workcenter>();

        this.options = new HashMap<String, String>();
        this.datadir = "";
        this.connection = null;
        this.statement = null;

        this.processOptions(args);
        this.loadData();
    }

    private void processOptions(String args[]) {

        // Process the input argument; currently only one argument
		// is supported
		// (1) a filename that points to the option file to read and process
        // If no argument is provided, then default options are set

		if (args.length != 0 && args.length != 1) {
			usage();
		}

        if (args.length == 0) {
            // No option file specified
            this.options.put("input_mode", "FLATFILE");
            this.options.put("datadir", "./data");
            return;
        }

        if (DEBUG.ordinal() >= DEBUG_LEVELS.STANDARD.ordinal()) {
			System.out.println("Processing options file...");
		}
        Path path = Paths.get(args[0]);
        List<String> lines = new ArrayList<String>();
        Charset charset = Charset.forName("ISO-8859-1");

        try {
            lines = Files.readAllLines(path, charset);
        } catch (IOException e) {
            System.out.println(e);
        }

        for (int n = 0; n < lines.size(); n++) {
            String p = lines.get(n).trim();
            if (p.charAt(0) == '#') {
                continue;
            }
            String[] parts = p.split(",");
            assert(parts.length == 2);
            this.options.put(parts[0].trim(), parts[1].trim());
        }

        // Now check through the imported options and perform some
        // basic checks to make sure that:
        // (1) Options values are supported for the key options
        // (2) If key options are not specified, set default values
        //

        // Check to make sure input_mode is specified
        if (this.options.containsKey("input_mode")) {
            String mode = this.options.get("input_mode");
            assert(mode.equals("FLATFILE") || mode.equals("DATABASE"));
            if (mode.equals("FLATFILE")) {
                if (!this.options.containsKey("datadir")) {
                    this.options.put("datadir", "./data");
                }
                this.datadir = options.get("datadir");
            }
            else if (mode.equals("DATABASE")) {
                if (!this.options.containsKey("db_connection_string")) {
                    this.options.put("db_connection_string", "jdbc:sqlite:./db/jobshop.db");
                    this.options.put("db_username", "");
                    this.options.put("db_password", "");
                }
                try {
                    this.connection = DriverManager.getConnection(
                                    this.options.get("db_connection_string"),
                                    this.options.get("db_username"),
                                    this.options.get("db_password"));
                    this.statement = connection.createStatement();
                }
                catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        else {
            this.options.put("input_mode", "FLATFILES");
            this.options.put("datadir", "./data");
            this.datadir = this.options.get("datadir");
        }
    }

    /**
     * Utility function that loads all the input csv files
     * @param dataSet String representing the name of the directory
     *                 containing the dataset we want to load.
     */
    private void loadData() {

        readPlans();
        readPlanParams();
        readSKUs();
        readCalendars();
        readCalendarShifts();
        readWorkcenters();
        readTasks();
        readDemands();
        readTaskPrecedences();
        readTaskWorkcenterAssociations();
    }

    /**
     * Utility function to create the Plan objects
     */
    private void readPlans() {

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) { 
			System.out.println("Reading plan data...");
		}

        String mode = this.options.get("input_mode");
        List<String> lines = new ArrayList<String>();

        if (mode.equals("FLATFILE")) {
            Path path = Paths.get(this.datadir + "/plan.csv");
            Charset charset = Charset.forName("ISO-8859-1");

            try {
                lines = Files.readAllLines(path, charset);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        else if (mode.equals("DATABASE")) {
            try {
                ResultSet rs = statement.executeQuery("select * from plan");
                while (rs.next()) {
                    String result = rs.getString("planid") + "," +
                                    rs.getString("planstart") + "," +
                                    rs.getString("planend");
                    lines.add(result);
                }
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        for (int n = 0; n < lines.size(); n++) {
            String p = lines.get(n).trim();
            if (p.charAt(0) == '#') {
                continue;
            }
            String[] parts = p.split(",");
            Plan plan = new Plan(parts[0], LocalDateTime.parse(parts[1]), LocalDateTime.parse(parts[2]));
            plans.put(parts[0], plan);
        }
    }


    /**
     * Utility function to create the PlanParams objects
     */
    private void readPlanParams() {

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) {
			System.out.println("Reading planparam data...");
		}
        String mode = this.options.get("input_mode");
        List<String> lines = new ArrayList<String>();

        if (mode.equals("FLATFILE")) {
            Path path = Paths.get(this.datadir + "/planparameter.csv");
            Charset charset = Charset.forName("ISO-8859-1");

            try {
                lines = Files.readAllLines(path, charset);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        else if (mode.equals("DATABASE")) {
            try {
                ResultSet rs = statement.executeQuery("select * from planparameter");
                while (rs.next()) {
                    String result = rs.getString("planid") + "," +
                                    rs.getString("paramname") + "," +
                                    rs.getString("paramvalue");
                    lines.add(result);
                }
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        for (int n = 0; n < lines.size(); n++) {
            String p = lines.get(n).trim();
            if (p.charAt(0) == '#') {
                continue;
            }
            String[] parts = p.split(",");
            Plan plan = plans.get(parts[0]);
            plan.setParam(parts[1], parts[2]);
        }
    }


    /**
     * Utility function to create the SKU objects
     */
    private void readSKUs() {

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) { 
			System.out.println("Reading sku data...");
		}

        String mode = this.options.get("input_mode");
        List<String> lines = new ArrayList<String>();

        if (mode.equals("FLATFILE")) {
            Path path = Paths.get(this.datadir + "/sku.csv");
            Charset charset = Charset.forName("ISO-8859-1");

            try {
                lines = Files.readAllLines(path, charset);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        else if (mode.equals("DATABASE")) {
            try {
                ResultSet rs = statement.executeQuery("select * from sku");
                while (rs.next()) {
                    String result = rs.getString("skuid") + "," +
                                    rs.getString("description");
                    lines.add(result);
                }
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        for (int n = 0; n < lines.size(); n++) {
            String p = lines.get(n).trim();
            if (p.charAt(0) == '#') {
                continue;
            }
            String[] parts = p.split(",");
            SKU s = new SKU(parts[0],parts[1]);
            skus.put(parts[0], s);
        }
    }

    /**
     * Utility function to create the Calendar objects
     */
    private void readCalendars() {

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) { 
			System.out.println("Reading calendar data...");
		}

        String mode = this.options.get("input_mode");
        List<String> lines = new ArrayList<String>();

        if (mode.equals("FLATFILE")) {
            Path path = Paths.get(this.datadir + "/calendar.csv");
            Charset charset = Charset.forName("ISO-8859-1");

            try {
                lines = Files.readAllLines(path, charset);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        else if (mode.equals("DATABASE")) {
            try {
                ResultSet rs = statement.executeQuery("select * from calendar");
                while (rs.next()) {
                    String result = rs.getString("calendarid") + "," +
                                    rs.getString("calendartype");
                    lines.add(result);
                }
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        for (int n = 0; n < lines.size(); n++) {
            String p = lines.get(n).trim();
            if (p.charAt(0) == '#') {
                continue;
            }
            String[] parts = p.split(",");
            Calendar cal = new Calendar(parts[0], parts[1]);
            calendars.put(parts[0], cal);
        }
    }

    /**
     * Utility function to create the CalendarShift objects
     */
    private void readCalendarShifts() {

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) { 
			System.out.println("Reading calendarshift data...");
		}

        String mode = this.options.get("input_mode");
        List<String> lines = new ArrayList<String>();

        if (mode.equals("FLATFILE")) {
            Path path = Paths.get(this.datadir + "/calendarshift.csv");
            Charset charset = Charset.forName("ISO-8859-1");

            try {
                lines = Files.readAllLines(path, charset);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        else if (mode.equals("DATABASE")) {
            try {
                ResultSet rs = statement.executeQuery("select * from calendarshift");
                while (rs.next()) {
                    String result = rs.getString("calendarid") + "," +
                                    rs.getString("shiftid") + "," +
                                    rs.getString("shiftstart") + "," +
                                    rs.getString("shiftend") + "," +
                                    rs.getString("shiftnumber") + "," +
                                    rs.getString("value");
                    lines.add(result);
                }
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        for (int n = 0; n < lines.size(); n++) {
            String p = lines.get(n).trim();
            if (p.charAt(0) == '#') {
                continue;
            }
            String[] parts = p.split(",");
            Calendar cal = calendars.get(parts[0]);
            assert(cal != null);

            CalendarShift cshift = new CalendarShift(cal,
                                                     Integer.parseInt(parts[1]),
                                                     LocalDateTime.parse(parts[2]),
                                                     LocalDateTime.parse(parts[3]),
                                                     Integer.parseInt(parts[4]),
                                                     Double.parseDouble(parts[5]));
            cal.addShift(cshift);
        }
    }

    /**
     * Utility function to create the Workcenter objects
     */
    private void readWorkcenters() {

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) { 
			System.out.println("Reading workcenter data...");
        }

        String mode = this.options.get("input_mode");
        List<String> lines = new ArrayList<String>();

        if (mode.equals("FLATFILE")) {
            Path path = Paths.get(this.datadir + "/workcenter.csv");
            Charset charset = Charset.forName("ISO-8859-1");

            try {
                lines = Files.readAllLines(path, charset);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        else if (mode.equals("DATABASE")) {
            try {
                ResultSet rs = statement.executeQuery("select * from workcenter");
                while (rs.next()) {
                    String result = rs.getString("workcenterid") + "," +
                                    rs.getString("efficiency_calendar") + "," +
                                    rs.getString("max_setups_per_shift") + "," +
                                    rs.getString("criticality_index");
                    lines.add(result);
                }
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        for (int n = 0; n < lines.size(); n++) {
            String p = lines.get(n).trim();
            if (p.charAt(0) == '#') {
                continue;
            }
            String[] parts = p.split(",");
            Calendar cal = calendars.get(parts[1]);
            assert(cal != null);
            Workcenter ws = new Workcenter(parts[0], cal, Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
            workcenters.put(parts[0], ws);
        }
    }

    /**
     * Utility function to create the Task objects
     */
    private void readTasks() {

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) { 
			System.out.println("Reading task data...");
		}

        String mode = this.options.get("input_mode");
        List<String> lines = new ArrayList<String>();

        if (mode.equals("FLATFILE")) {
            Path path = Paths.get(this.datadir + "/task.csv");
            Charset charset = Charset.forName("ISO-8859-1");

            try {
                lines = Files.readAllLines(path, charset);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        else if (mode.equals("DATABASE")) {
            try {
                ResultSet rs = statement.executeQuery("select * from task");
                while (rs.next()) {
                    String result = rs.getString("taskid") + "," +
                                    rs.getString("skuid") + "," +
                                    rs.getString("setup_time") + "," +
                                    rs.getString("per_unit_time") + "," +
                                    rs.getString("min_lot_size") + "," +
                                    rs.getString("max_lot_size") + "," +
                                    rs.getString("is_delivery_task");
                    lines.add(result);
                }
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        for (int n = 0; n < lines.size(); n++) {
            String p = lines.get(n).trim();
            if (p.charAt(0) == '#') {
                continue;
            }
            String[] parts = p.split(",");
            SKU sku = skus.get(parts[1]);
            assert(sku != null);
            String taskNum = parts[1] + "-" + parts[0];
            Task task = new Task(parts[0], sku,
                                 Long.parseLong(parts[2]),
                                 Long.parseLong(parts[3]),
                                 Long.parseLong(parts[4]),
                                 Long.parseLong(parts[5]));
            tasks.put(taskNum, task);
            if (Boolean.parseBoolean(parts[6])) {
                sku.setDeliveryTask(task);
            }
        }
    }

    /**
     * Utility function to create the Demand objects
     */
    private void readDemands() {

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) { 
			System.out.println("Reading demand data...");
		}

        String mode = this.options.get("input_mode");
        List<String> lines = new ArrayList<String>();

        if (mode.equals("FLATFILE")) {
            Path path = Paths.get(this.datadir + "/demand.csv");
            Charset charset = Charset.forName("ISO-8859-1");

            try {
                lines = Files.readAllLines(path, charset);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        else if (mode.equals("DATABASE")) {
            try {
                ResultSet rs = statement.executeQuery("select * from demand");
                while (rs.next()) {
                    String result = rs.getString("planid") + "," +
                                    rs.getString("demandid") + "," +
                                    rs.getString("customerid") + "," +
                                    rs.getString("skuid") + "," +
                                    rs.getString("duedate") + "," +
                                    rs.getString("duequantity") + "," +
                                    rs.getString("priority");
                    lines.add(result);
                }
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        for (int n = 0; n < lines.size(); n++) {
            String p = lines.get(n).trim();
            if (p.charAt(0) == '#') {
                continue;
            }
            String[] parts = p.split(",");

            Plan plan = plans.get(parts[0]);
            assert(plan != null);

            SKU sku = skus.get(parts[3]);
            assert(sku != null);

            Demand dmd = new Demand(parts[1], parts[2], sku,
                                    LocalDateTime.parse(parts[4]),
                                    Long.parseLong(parts[5]),
                                    Long.parseLong(parts[6]), plan);
            demands.put(parts[1], dmd);
        }
    }

    /**
     * Utility function to establish the Task predecence relations
     */
    private void readTaskPrecedences() {

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) { 
			System.out.println("Reading task precedence data...");
		}

        String mode = this.options.get("input_mode");
        List<String> lines = new ArrayList<String>();

        if (mode.equals("FLATFILE")) {
            Path path = Paths.get(this.datadir + "/taskprecedence.csv");
            Charset charset = Charset.forName("ISO-8859-1");

            try {
                lines = Files.readAllLines(path, charset);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        else if (mode.equals("DATABASE")) {
            try {
                ResultSet rs = statement.executeQuery("select * from taskprecedence");
                while (rs.next()) {
                    String result = rs.getString("taskid") + "," +
                                    rs.getString("skuid") + "," +
                                    rs.getString("predecessor");
                    lines.add(result);
                }
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        for (int n = 0; n < lines.size(); n++) {
            String p = lines.get(n).trim();
            if (p.charAt(0) == '#') {
                continue;
            }
            String[] parts = p.split(",");
            Task succ = tasks.get(parts[1] + "-" + parts[0]);
            Task pred = tasks.get(parts[1] + "-" + parts[2]);
            assert(succ != null);
            assert(pred != null);

            succ.setPredecessor(pred);
            pred.setSuccessor(succ);
        }
    }

    /**
     * Utility function to establish the Task/Workcenter relationships
     */
    private void readTaskWorkcenterAssociations() {

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) { 
			System.out.println("Reading task workcenter association data...");
		}

        String mode = this.options.get("input_mode");
        List<String> lines = new ArrayList<String>();

        if (mode.equals("FLATFILE")) {
            Path path = Paths.get(this.datadir + "/taskworkcenterassn.csv");
            Charset charset = Charset.forName("ISO-8859-1");

            try {
                lines = Files.readAllLines(path, charset);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        else if (mode.equals("DATABASE")) {
            try {
                ResultSet rs = statement.executeQuery("select * from taskworkcenterassn");
                while (rs.next()) {
                    String result = rs.getString("taskid") + "," +
                                    rs.getString("skuid") + "," +
                                    rs.getString("workcenterid") + "," +
                                    rs.getString("priority");
                    lines.add(result);
                }
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        for (int n = 0; n < lines.size(); n++) {
            String p = lines.get(n).trim();
            if (p.charAt(0) == '#') {
                continue;
            }
            String[] parts = p.split(",");
            Task t = tasks.get(parts[1] + "-" + parts[0]);
            Workcenter w = workcenters.get(parts[2]);
            Integer priority = new Integer(parts[3]);
            assert(t != null);
            assert(w != null);

            t.addWorkcenter(w, priority);
        }
    }

    /**
     * Return a Map of Demands (keyed by unique demand identifier) of
     * the demands in this JobShop model
     * @return Map of Demands (keyed by unique demand identifier)
     */
    public Map<String, Demand> getDemands() {
        return this.demands;
    }

    /**
     * A utility function to print out data about the different objects
     * in the JobShop model.  This can be used to generate output
     * files that are compared to expect files for regression testing
     */
    public void print() {

        String mode = this.options.get("input_mode");

        if (mode.equals("FLATFILE")) {
            System.out.println("\nPlans:");
            List<String> splans = this.plans.keySet()
                                    .stream()
                                    .sorted()
                                    .collect(Collectors.toList());

            for (String p : splans) {
                Plan plan = this.plans.get(p);
                System.out.println(plan);
            }

            System.out.println("\nDemands:");
            List<Demand> sdmds = this.demands.values()
                                    .stream()
                                    .sorted(Comparator.comparing(Demand::getPriority))
                                    .collect(Collectors.toList());
            for (Demand dmd : sdmds) {
                System.out.println(dmd);
            }

            System.out.println("\nTaskPlans:");
            List<String> stasks = this.tasks.keySet()
                                    .stream()
                                    .sorted()
                                    .collect(Collectors.toList());
            for (String t : stasks) {
                Task task = this.tasks.get(t);
                List<TaskPlan> tps = task.getTaskPlans().stream()
                                        .sorted(Comparator.comparing(TaskPlan::getStart))
                                        .collect(Collectors.toList());
                for (TaskPlan tp : tps) {
                    System.out.println(tp);
                }
            }

            System.out.println("\nWorkcenterPlans:");
            List<String> sworks = this.workcenters.keySet()
                                    .stream()
                                    .sorted()
                                    .collect(Collectors.toList());
            for (String w : sworks) {
                Workcenter wrk = this.workcenters.get(w);
                System.out.println(wrk);
                List<TaskPlan> tps = wrk.getTaskPlans().stream()
                                        .sorted(Comparator.comparing(TaskPlan::getStart))
                                        .collect(Collectors.toList());
                for (TaskPlan tp : tps) {
                    System.out.println(" - " + tp);
                }
            }
        }
        else if (mode.equals("DATABASE")) {

            String delStmt = "DELETE FROM TASKPLAN";

            String stmt = "INSERT INTO TASKPLAN " +
                          "(PLANID, DEMANDID, SKUID, TASKID, STARTDATE," +
                          " ENDDATE, QUANTITY, WORKCENTERID) VALUES " +
                          "(?, ?, ?, ?, ?, ?, ?, ?)";
            try {
                // First delete all existing records in the TaskPlan table (from prior runs)
                statement.executeUpdate(delStmt);

                // Now insert new records into the TaskPlan table (from current run)
                PreparedStatement pStmt = this.connection.prepareStatement(stmt);

                List<String> stasks = this.tasks.keySet()
                                        .stream()
                                        .sorted()
                                        .collect(Collectors.toList());
                for (String t : stasks) {
                    Task task = this.tasks.get(t);
                    List<TaskPlan> tps = task.getTaskPlans().stream()
                                            .sorted(Comparator.comparing(TaskPlan::getStart))
                                            .collect(Collectors.toList());
                    for (TaskPlan tp : tps) {
                        pStmt.setString(1, tp.getPlan().getID());
                        pStmt.setString(2, tp.getDemandID());
                        pStmt.setString(3, tp.getTask().getSKU().getName());
                        pStmt.setString(4, tp.getTask().getTaskID());
                        pStmt.setString(5, tp.getStart().toString());
                        pStmt.setString(6, tp.getEnd().toString());
                        pStmt.setString(7, Long.toString(tp.getQuantity()));
                        if (tp.getWorkcenter() != null) {
                            pStmt.setString(8, tp.getWorkcenter().getName());
                        }
                        else {
                            pStmt.setNull(8, java.sql.Types.VARCHAR);
                        }
                        pStmt.addBatch();
                    }
                }

                pStmt.executeBatch();
                System.out.println("\nTaskplans written to database...");
            }
            catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * A utility function to print out usage message for this program
     */
    public static void usage() {
        System.out.println("Usage is:");
        System.out.println("\tjava -jar bin/JobShop.jar <path to option file>");
        System.out.println("\t... Check the Wiki page at:");
        System.out.println("\t... https://github.com/ushnisha/jobshop-minimal/wiki/Configuring-the-Options-File");
        System.out.println("\t... for more details on how to configure options");
        System.out.println("\n");
        System.exit(1);
    }

}
