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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.Charset;
import java.nio.file.StandardOpenOption;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Comparator;

import java.sql.Timestamp;
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
    public static DEBUG_LEVELS DEBUG = DEBUG_LEVELS.MINIMAL;
    public static enum DATA_QUALITY { GOOD, BAD };

    public static Path logDir;
    public static Path logFile;
    public static Path goodFile;
    public static Path badFile;

    private static DateTimeFormatter sqliteDFS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static DateTimeFormatter dfs = new DateTimeFormatterBuilder()
                                           .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                           .appendOptional(sqliteDFS)
                                           .toFormatter();

    private Map<String,Plan> plans;
    private Map<String,Calendar> calendars;
    private Map<String,SKU> skus;
    private Map<String,Demand> demands;
    private Map<String,Task> tasks;
    private Map<Task,TaskPlan> taskplans;
    private Map<String,Workcenter> workcenters;
    private Map<String,ReleasedWorkOrder> relworkorders;

    private Map<String,String> options;
    private String datadir;
    private Connection connection;
    private Statement statement;

    private static boolean cleandata = false;
    public static final Charset charset = Charset.forName("ISO-8859-1");

    /**
     * Main function that is run for simulating a JobShop planning process
     * @param args String[] of input arguments.  Currently, assumes:
     *             args[0] - name of the plan for which we want to generate a plan
     *             If args[0] is not specified it will pick the first plan available
     *             after importing the Plan file from the data directory
     */
    public static void main(String args[]) {
		
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
     * Utility function that logs error/warning/info messages
     * message will be logged only to log file and only if debug level
     * is MINIMAL or greater
     * @param message String The string that must be logged
     */
    public static void LOG(String message) {
        JobShop.LOG(message, false, DEBUG_LEVELS.MINIMAL);
    }

    /**
     * Utility function that logs error/warning/info messages
     * message will be logged only if debug level is MINIMAL or greater
     * @param message String The string that must be logged
     * @param toStdOut boolean log to both standard output and log file
     *                         if true, else log only to log file
     */
    public static void LOG(String message, boolean toStdOut) {
        JobShop.LOG(message, toStdOut, DEBUG_LEVELS.MINIMAL);
    }

    /**
     * Utility function that logs error/warning/info messages
     * message will be logged only to log file and not to standard output
     * @param message String The string that must be logged
     * @param debug_level DEBUG_LEVELS Current debug level setting must
     *                    be greater than or equal to this value to log
     *                    this message
     */
    public static void LOG(String message, DEBUG_LEVELS debug_level) {
        JobShop.LOG(message, false, debug_level);
    }

    /**
     * Utility function that logs error/warning/info messages
     * @param message String The string that must be logged
     * @param toStdOut boolean log to both standard output and log file
     *                 if true, else log only to log file
     * @param debug_level DEBUG_LEVELS Current debug level setting must
     *                    be greater than or equal to this value to log
     *                    this message
     */
    public static void LOG(String message, boolean toStdOut, DEBUG_LEVELS debug_level) {

        if (DEBUG.ordinal() >= debug_level.ordinal()) {

            if (toStdOut) {
                System.out.println(message);
            }

            try {
                Files.write(JobShop.logFile,
                            Arrays.asList(LocalDateTime.now().toString() + " : " +
                                          message),
                            charset,
                            StandardOpenOption.WRITE,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.APPEND);
            }
            catch (IOException e) {
                System.err.println("Unable to write to log file: " + e);
            }
        }
    }

    /**
     * Utility function that logs input data to .good and .bad files
     * @param type DATA_QUALITY Whether this data record is good or bad
     * @param datarec String The data record to log to appropriate file
     */
    public static void LOGDATA(DATA_QUALITY type, String datarec) {

        Path currentPath;
        if (type == DATA_QUALITY.GOOD) {
            currentPath = goodFile;
        }
        else {
            currentPath = badFile;
        }

        try {
            Files.write(currentPath,
                        Arrays.asList(datarec),
                        charset,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
        }
        catch (IOException e) {
            System.err.println("Unable to write data to file: " + currentPath.toString());
        }
    }

    /**
     * Constructor for the JobShop object
     * @param args String[] representing the options used to
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
        this.relworkorders = new HashMap<String,ReleasedWorkOrder>();

        this.options = new HashMap<String, String>();
        this.datadir = "";
        this.connection = null;
        this.statement = null;

        this.processOptions(args);
        this.loadData();
        this.performStaticDataValidation();
    }

    /**
     * Data Validation based on the static model is performed
     * after loading the data
     *
     */
    private void performStaticDataValidation() {

        JobShop.LOG("\nStarting Static Analysis...", DEBUG_LEVELS.MINIMAL);

        // Checks on SKUs
        for (SKU s : this.skus.values()) {
            // SKU's without delivery task
            if (s.getDeliveryTask() == null) {
                JobShop.LOG("Error!  SKU " + s.getName() +
                            " does not have a delivery task specified",
                            DEBUG_LEVELS.MINIMAL);
            }

            // SKU's without any associated tasks!
            int ass_tasks = 0;
            int ass_wrks = 0;
            for (Task t : this.tasks.values()) {
                if (t.getSKU() == s) {
                    ass_tasks++;
                    ass_wrks += t.getWorkcenterCount();
                }
                if ((ass_tasks > 0) && (ass_wrks > 0)) {
                    break;
                }
            }
            // SKU's without any associated tasks!
            if (ass_tasks == 0) {
                JobShop.LOG("Error! SKU " + s.getName() +
                            " does not have any associated tasks",
                            DEBUG_LEVELS.MINIMAL);
                continue;
            }
            // SKU's without tasks that load workcenters
            if (ass_wrks == 0) {
                JobShop.LOG("Warning! SKU " + s.getName() +
                            " does not have any associated tasks" +
                            " that load workcenters",
                            DEBUG_LEVELS.MINIMAL);
                continue;
            }
        }

        // Checks on Demands
        for (Demand d : this.demands.values()) {
            // Demands for SKU's without delivery task
            if (d.getSKU().getDeliveryTask() == null) {
                JobShop.LOG("Error!  Demand " + d.getID() +
                            " for SKU " + d.getSKU().getName() +
                            " without a delivery task",
                            DEBUG_LEVELS.MINIMAL);
            }
        }

        // Checks for Tasks
        Set<Workcenter> used_wrks = new HashSet<Workcenter>();
        for (Task t : this.tasks.values()) {
            // Add associated workcenters to used_wrks for later use
            used_wrks.addAll(t.getWorkcenters());

            // Hanging task type 1 - no pred or succ
            if (t.getPredecessor() == null && t.getSuccessor() == null) {
                JobShop.LOG("Error!  Hanging Task " + t.getTaskNumber() +
                            " without a predecessor or successor",
                            DEBUG_LEVELS.MINIMAL);
            }

            // Hanging task type 2 - yes pred but no succ and not delivery task
            if (t.getPredecessor() != null && t.getSuccessor() == null &&
                t.getSKU().getDeliveryTask() != t) {
                JobShop.LOG("Error!  Hanging Task " + t.getTaskNumber() +
                            " with a predecessor but no successor and" +
                            " is not a delivery task",
                            DEBUG_LEVELS.MINIMAL);
            }

            // "Effortless" tasks - tasks with nonzero time_per_unit but load no workcenters
            if (t.getWorkcenterCount() == 0 && t.getTimePer() > 0) {
                JobShop.LOG("Error!  'Effortless' Task " + t.getTaskNumber() +
                            " loads no workcenters but has nonzero time per unit",
                            DEBUG_LEVELS.MINIMAL);
            }
        }

        // Checks for Workcenters
        for (Workcenter w : this.workcenters.values()) {
            // Check for "hanging" workcenters - workcenters that are not associated with any task
            if (!used_wrks.contains(w)) {
                JobShop.LOG("Error!  Hanging Workcenter " + w.getName() +
                            " not associated with any task",
                            DEBUG_LEVELS.MINIMAL);
            }
        }

        // Check for cycles in the model
        List<Task> current_task_chain = new ArrayList<Task>();
        List<Task> root_tasks = this.tasks.values().stream()
                                .filter(task -> (task.getPredecessor() != null))
                                .collect(Collectors.toList());
        int num_chains = 0;
        for (Task t : root_tasks) {
            current_task_chain = new ArrayList<Task>();
            current_task_chain.add(t);
            Task nt = t;
            boolean fail = false;
            while (!fail && (nt = nt.getSuccessor()) != null) {
                if (current_task_chain.contains(nt)) {
                    String chainStr = "";
                    for (Task ct : current_task_chain) {
                        chainStr += ct.getTaskNumber() + "->";
                    }
                    chainStr += nt.getTaskNumber();
                    JobShop.LOG("Error! Found cycle starting from: " +
                                t.getTaskNumber() + " as: " + chainStr,
                                DEBUG_LEVELS.MINIMAL);
                    fail = true;
                    num_chains++;
                }
                else {
                    current_task_chain.add(nt);
                }
            }
        }
        if (num_chains > 0) {
            String errMsg = "Found cycles! Aborting - please check logfile";
            JobShop.LOG(errMsg, DEBUG_LEVELS.MINIMAL);
            System.err.println(errMsg);
            System.exit(500);
        }
    }


    /**
     * Process the options file and initialize suitable variables
     * @param args String[] Array of strings which should currently
     *                      contain the name of the option file
     */
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

        Path path = Paths.get(args[0]);
        List<String> lines = new ArrayList<String>();

        try {
            lines = Files.readAllLines(path, charset);
        } catch (IOException e) {
            JobShop.LOG(e.getMessage());
        }

        for (int n = 0; n < lines.size(); n++) {
            String p = lines.get(n).trim();
            if (p.charAt(0) == '#') {
                continue;
            }
            String[] parts = p.split("\\|");
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
                if (this.options.containsKey("datadir")) {
                    this.datadir = options.get("datadir");
                    Path datadir = Paths.get(this.datadir);
                    if (!Files.isDirectory(datadir)) {
                        System.err.println("Terminating Program! Invalid datadir Path: " + this.datadir.toString());
                        System.exit(404);
                    }
                }
                else {
                    this.options.put("datadir", "./data");
                }
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
                    JobShop.LOG(e.getMessage());
                }
            }
        }
        else {
            this.options.put("input_mode", "FLATFILES");
            this.options.put("datadir", "./data");
            this.datadir = this.options.get("datadir");
        }

        // Check for option to clean data; if set to true, we will
        // write all "good" records to a .good file and all "bad" records
        // to a .bad file for each input file
        if (this.options.containsKey("cleandata")) {
            cleandata = Boolean.parseBoolean(this.options.get("cleandata"));

            // Enable cleaning data only if user also explicitly specifies
            // the logdir option.  We cannot write several .good and .bad
            // files to the directory from which we invoke the program
            //
            if (cleandata && !this.options.containsKey("logdir")) {
                System.err.println("Exiting!  Cannot clean data without explicit specification of logdir option");
                System.exit(403);
            }
        }

        // Create the logFile location based on input option (or default)
        //
        if (this.options.containsKey("logdir")) {
            JobShop.logDir = Paths.get(this.options.get("logdir"));
            if (!Files.isDirectory(JobShop.logDir)) {
                System.err.println("Terminating Program! Invalid logdir Path: " + JobShop.logDir.toString());
                System.exit(404);
            }
            JobShop.logFile = Paths.get(this.options.get("logdir") + "/jobshop.log");
        }
        else {
            JobShop.logDir = Paths.get("").toAbsolutePath();
            String cwd = logDir.toString();
            JobShop.logFile = Paths.get(cwd + "/jobshop.log");
        }

        try {
            Files.write(JobShop.logFile,
                        Arrays.asList(LocalDateTime.now().toString() + " : " +
                                      "Starting JobShop Minimal Solver"),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (IOException e) {
            System.err.println("Unable to write to log file: " + e);
        }

        // Setup the user specified debug level for logging information
        // from various locations in the program
        //
        if (this.options.containsKey("debug_level")) {
            try {
                JobShop.DEBUG = DEBUG_LEVELS.valueOf(this.options.get("debug_level"));
            }
            catch (IllegalArgumentException e) {
                JobShop.LOG("Illegal value for option debug_level: " + this.options.get("debug_level") + "; Defaulting to MINIMAL...");
            }
        }

        JobShop.LOG("\nA Minimal JobShop Planner", true);
        JobShop.LOG("Processing options file...", true, DEBUG_LEVELS.STANDARD);

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
        readReleasedWorkOrders();
    }

    /**
     * Utility function to create the Plan objects
     */
    private void readPlans() {

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) { 
            JobShop.LOG("Reading plan data...", true);
		}

        String mode = this.options.get("input_mode");

        if (mode.equals("FLATFILE")) {
            Path path = Paths.get(this.datadir + "/plan.csv");

            if (cleandata) {
                goodFile = Paths.get(JobShop.logDir.toString() + "/plan.good");
                badFile = Paths.get(JobShop.logDir.toString() + "/plan.bad");
            }

            try {
                List<String> lines = new ArrayList<String>();
                lines = Files.readAllLines(path, charset);

                for (int n = 0; n < lines.size(); n++) {
                    String p = lines.get(n).trim();
                    if (p.charAt(0) == '#') {
                        if (cleandata) LOGDATA(DATA_QUALITY.GOOD, p);
                        continue;
                    }
                    String[] parts = p.split(",", -1);

                    if (plans.containsKey(parts[0])) {
                        JobShop.LOG("Plan already exists: " + p);
                        if (cleandata) LOGDATA(DATA_QUALITY.BAD, p);
                        continue;
                    }

                    if (cleandata) LOGDATA(DATA_QUALITY.GOOD, p);

                    Plan plan = new Plan(parts[0], LocalDateTime.parse(parts[1], dfs), LocalDateTime.parse(parts[2], dfs));
                    plans.put(parts[0], plan);
                }

            } catch (IOException e) {
                JobShop.LOG(e.getMessage());
            }
        }
        else if (mode.equals("DATABASE")) {
            try {
                ResultSet rs = statement.executeQuery("select * from plan");
                while (rs.next()) {
                    String planid = rs.getString("planid");
                    Timestamp pstart = rs.getTimestamp("planstart");
                    Timestamp pend = rs.getTimestamp("planend");

                    Plan plan = new Plan(planid, pstart.toLocalDateTime(), pend.toLocalDateTime());
                    plans.put(planid, plan);
                }
            }
            catch (SQLException e) {
                JobShop.LOG(e.getMessage());
            }
        }

    }


    /**
     * Utility function to create the PlanParams objects
     */
    private void readPlanParams() {

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) {
            JobShop.LOG("Reading planparam data...", true);
		}
        String mode = this.options.get("input_mode");

        if (mode.equals("FLATFILE")) {
            Path path = Paths.get(this.datadir + "/planparameter.csv");

            if (cleandata) {
                goodFile = Paths.get(JobShop.logDir.toString() + "/planparameter.good");
                badFile = Paths.get(JobShop.logDir.toString() + "/planparameter.bad");
            }

            try {
                List<String> lines = new ArrayList<String>();
                lines = Files.readAllLines(path, charset);

                for (int n = 0; n < lines.size(); n++) {
                    String p = lines.get(n).trim();
                    if (cleandata) LOGDATA(DATA_QUALITY.GOOD, p);
                    if (p.charAt(0) == '#') {
                        continue;
                    }
                    String[] parts = p.split(",", -1);
                    Plan plan = plans.get(parts[0]);
                    plan.setParam(parts[1], parts[2]);
                }
            } catch (IOException e) {
                JobShop.LOG(e.getMessage());
            }
        }
        else if (mode.equals("DATABASE")) {
            try {
                ResultSet rs = statement.executeQuery("select * from planparameter");
                while (rs.next()) {
                    String planid = rs.getString("planid");
                    String parname = rs.getString("paramname");
                    String parval = rs.getString("paramvalue");

                    Plan plan = plans.get(planid);
                    plan.setParam(parname, parval);
                }
            }
            catch (SQLException e) {
                JobShop.LOG(e.getMessage());
            }
        }

    }


    /**
     * Utility function to create the SKU objects
     */
    private void readSKUs() {

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) { 
            JobShop.LOG("Reading sku data...", true);
		}

        String mode = this.options.get("input_mode");
        List<String> lines = new ArrayList<String>();

        if (mode.equals("FLATFILE")) {
            Path path = Paths.get(this.datadir + "/sku.csv");

            if (cleandata) {
                goodFile = Paths.get(JobShop.logDir.toString() + "/sku.good");
                badFile = Paths.get(JobShop.logDir.toString() + "/sku.bad");
            }

            try {
                lines = Files.readAllLines(path, charset);

                for (int n = 0; n < lines.size(); n++) {
                    String p = lines.get(n).trim();
                    if (p.charAt(0) == '#') {
                        if (cleandata) LOGDATA(DATA_QUALITY.GOOD, p);
                        continue;
                    }
                    String[] parts = p.split(",", -1);

                    if (skus.containsKey(parts[0])) {
                        JobShop.LOG("SKU already exists: " + p);
                        if (cleandata) LOGDATA(DATA_QUALITY.BAD, p);
                        continue;
                    }

                    if (cleandata) LOGDATA(DATA_QUALITY.GOOD, p);

                    SKU s = new SKU(parts[0],parts[1]);
                    skus.put(parts[0], s);
                }
            } catch (IOException e) {
                JobShop.LOG(e.getMessage());
            }
        }
        else if (mode.equals("DATABASE")) {
            try {
                ResultSet rs = statement.executeQuery("select * from sku");
                while (rs.next()) {
                    String skuid = rs.getString("skuid");
                    String desc = rs.getString("description");

                    SKU s = new SKU(skuid, desc);
                    skus.put(skuid, s);
                }
            }
            catch (SQLException e) {
                JobShop.LOG(e.getMessage());
            }
        }

    }

    /**
     * Utility function to create the Calendar objects
     */
    private void readCalendars() {

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) { 
            JobShop.LOG("Reading calendar data...", true);
		}

        String mode = this.options.get("input_mode");

        if (mode.equals("FLATFILE")) {
            Path path = Paths.get(this.datadir + "/calendar.csv");

            if (cleandata) {
                goodFile = Paths.get(JobShop.logDir.toString() + "/calendar.good");
                badFile = Paths.get(JobShop.logDir.toString() + "/calendar.bad");
            }

            try {
                List<String> lines = new ArrayList<String>();
                lines = Files.readAllLines(path, charset);

                for (int n = 0; n < lines.size(); n++) {
                    String p = lines.get(n).trim();
                    if (p.charAt(0) == '#') {
                        if (cleandata) LOGDATA(DATA_QUALITY.GOOD, p);
                        continue;
                    }
                    String[] parts = p.split(",", -1);

                    if (calendars.containsKey(parts[0])) {
                        JobShop.LOG("Calendar already exists: " + p);
                        if (cleandata) LOGDATA(DATA_QUALITY.BAD, p);
                        continue;
                    }

                    if (cleandata) LOGDATA(DATA_QUALITY.GOOD, p);

                    Calendar cal = new Calendar(parts[0], parts[1]);
                    calendars.put(parts[0], cal);
                }
            } catch (IOException e) {
                JobShop.LOG(e.getMessage());
            }
        }
        else if (mode.equals("DATABASE")) {
            try {
                ResultSet rs = statement.executeQuery("select * from calendar");
                while (rs.next()) {
                    String calid = rs.getString("calendarid");
                    String caltype = rs.getString("calendartype");

                    Calendar cal = new Calendar(calid, caltype);
                    calendars.put(calid, cal);
                }
            }
            catch (SQLException e) {
                JobShop.LOG(e.getMessage());
            }
        }

    }

    /**
     * Utility function to create the CalendarShift objects
     */
    private void readCalendarShifts() {

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) { 
            JobShop.LOG("Reading calendarshift data...", true);
		}

        String mode = this.options.get("input_mode");

        if (mode.equals("FLATFILE")) {
            Path path = Paths.get(this.datadir + "/calendarshift.csv");

            if (cleandata) {
                goodFile = Paths.get(JobShop.logDir.toString() + "/calendarshift.good");
                badFile = Paths.get(JobShop.logDir.toString() + "/calendarshift.bad");
            }

            try {
                List<String> lines = new ArrayList<String>();
                lines = Files.readAllLines(path, charset);

                for (int n = 0; n < lines.size(); n++) {
                    String p = lines.get(n).trim();
                    if (cleandata) LOGDATA(DATA_QUALITY.GOOD, p);
                    if (p.charAt(0) == '#') {
                        continue;
                    }
                    String[] parts = p.split(",", -1);
                    Calendar cal = calendars.get(parts[0]);
                    assert(cal != null);

                    CalendarShift cshift = new CalendarShift(cal,
                                                             Integer.parseInt(parts[1]),
                                                             LocalDateTime.parse(parts[2], dfs),
                                                             LocalDateTime.parse(parts[3], dfs),
                                                             Integer.parseInt(parts[4]),
                                                             Double.parseDouble(parts[5]));
                    cal.addShift(cshift);
                }
            } catch (IOException e) {
                JobShop.LOG(e.getMessage());
            }
        }
        else if (mode.equals("DATABASE")) {
            try {
                ResultSet rs = statement.executeQuery("select * from calendarshift");
                while (rs.next()) {
                    String calid = rs.getString("calendarid");
                    int shiftid = rs.getInt("shiftid");
                    Timestamp sstart = rs.getTimestamp("shiftstart");
                    Timestamp send = rs.getTimestamp("shiftend");
                    int snum = rs.getInt("shiftnumber");
                    double val = rs.getDouble("value");

                    Calendar cal = calendars.get(calid);
                    assert(cal != null);

                    CalendarShift cshift = new CalendarShift(cal, shiftid, sstart.toLocalDateTime(),
                                                             send.toLocalDateTime(), snum, val);
                    cal.addShift(cshift);
                }
            }
            catch (SQLException e) {
                JobShop.LOG(e.getMessage());
            }
        }

    }

    /**
     * Utility function to create the Workcenter objects
     */
    private void readWorkcenters() {

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) { 
            JobShop.LOG("Reading workcenter data...", true);
        }

        String mode = this.options.get("input_mode");

        if (mode.equals("FLATFILE")) {
            Path path = Paths.get(this.datadir + "/workcenter.csv");

            if (cleandata) {
                goodFile = Paths.get(JobShop.logDir.toString() + "/workcenter.good");
                badFile = Paths.get(JobShop.logDir.toString() + "/workcenter.bad");
            }

            try {
                List<String> lines = new ArrayList<String>();
                lines = Files.readAllLines(path, charset);

                for (int n = 0; n < lines.size(); n++) {
                    String p = lines.get(n).trim();
                    if (p.charAt(0) == '#') {
                        if (cleandata) LOGDATA(DATA_QUALITY.GOOD, p);
                        continue;
                    }
                    String[] parts = p.split(",", -1);
                    Calendar cal = calendars.get(parts[1]);
                    assert(cal != null);

                    if (workcenters.containsKey(parts[0])) {
                        JobShop.LOG("Workcenter already exists: " + p);
                        if (cleandata) LOGDATA(DATA_QUALITY.BAD, p);
                        continue;
                    }

                    if (cleandata) LOGDATA(DATA_QUALITY.GOOD, p);

                    Workcenter ws = new Workcenter(parts[0], cal, Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                    workcenters.put(parts[0], ws);
                }
            } catch (IOException e) {
                JobShop.LOG(e.getMessage());
            }
        }
        else if (mode.equals("DATABASE")) {
            try {
                ResultSet rs = statement.executeQuery("select * from workcenter");
                while (rs.next()) {
                    String wrkid = rs.getString("workcenterid");
                    String calid = rs.getString("efficiency_calendar");
                    int max_setups_per_shift = rs.getInt("max_setups_per_shift");
                    int criticality_idx = rs.getInt("criticality_index");

                    Calendar cal = calendars.get(calid);
                    assert(cal != null);
                    Workcenter ws = new Workcenter(wrkid, cal, max_setups_per_shift, criticality_idx);
                    workcenters.put(wrkid, ws);
                }
            }
            catch (SQLException e) {
                JobShop.LOG(e.getMessage());
            }
        }

    }

    /**
     * Utility function to create the Task objects
     */
    private void readTasks() {

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) { 
            JobShop.LOG("Reading task data...", true);
		}

        String mode = this.options.get("input_mode");

        if (mode.equals("FLATFILE")) {
            Path path = Paths.get(this.datadir + "/task.csv");

            if (cleandata) {
                goodFile = Paths.get(JobShop.logDir.toString() + "/task.good");
                badFile = Paths.get(JobShop.logDir.toString() + "/task.bad");
            }

            try {
                List<String> lines = new ArrayList<String>();
                lines = Files.readAllLines(path, charset);

                for (int n = 0; n < lines.size(); n++) {
                    String p = lines.get(n).trim();
                    if (p.charAt(0) == '#') {
                        if (cleandata) LOGDATA(DATA_QUALITY.GOOD, p);
                        continue;
                    }
                    String[] parts = p.split(",", -1);
                    SKU sku = skus.get(parts[1]);
                    assert(sku != null);
                    String taskNum = parts[1] + "-" + parts[0];

                    if (tasks.containsKey(taskNum)) {
                        JobShop.LOG("Task already exists: " + p);
                        if (cleandata) LOGDATA(DATA_QUALITY.BAD, p);
                        continue;
                    }

                    if (cleandata) LOGDATA(DATA_QUALITY.GOOD, p);

                    Task task = new Task(parts[0], sku,
                                         Long.parseLong(parts[2]),
                                         Long.parseLong(parts[3]),
                                         Long.parseLong(parts[4]),
                                         Long.parseLong(parts[5]));
                    tasks.put(taskNum, task);

                    if (parts[6].equals("Y") || parts[6].equals("y") || parts[6].equals("1") ||
                        parts[6].equals("T") || parts[6].equals("t")) {
                        sku.setDeliveryTask(task);
                    }
                }
            } catch (IOException e) {
                JobShop.LOG(e.getMessage());
            }
        }
        else if (mode.equals("DATABASE")) {
            try {
                ResultSet rs = statement.executeQuery("select * from task");
                while (rs.next()) {
                    String taskid = rs.getString("taskid");
                    String skuid = rs.getString("skuid");
                    long setup_time = rs.getLong("setup_time");
                    long per_unit_time = rs.getLong("per_unit_time");
                    long min_ls = rs.getLong("min_lot_size");
                    long max_ls = rs.getLong("max_lot_size");
                    String isdel = rs.getString("is_delivery_task");

                    SKU sku = skus.get(skuid);
                    assert(sku != null);
                    String taskNum = skuid + "-" + taskid;
                    Task task = new Task(taskid, sku, setup_time, per_unit_time, min_ls, max_ls);
                    tasks.put(taskNum, task);

                    if (isdel.equals("Y") || isdel.equals("y") || isdel.equals("1") ||
                        isdel.equals("T") || isdel.equals("t")) {
                        sku.setDeliveryTask(task);
                    }

                }
            }
            catch (SQLException e) {
                JobShop.LOG(e.getMessage());
            }
        }

    }

    /**
     * Utility function to create the Demand objects
     */
    private void readDemands() {

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) { 
            JobShop.LOG("Reading demand data...", true);
		}

        String mode = this.options.get("input_mode");

        if (mode.equals("FLATFILE")) {
            Path path = Paths.get(this.datadir + "/demand.csv");

            if (cleandata) {
                goodFile = Paths.get(JobShop.logDir.toString() + "/demand.good");
                badFile = Paths.get(JobShop.logDir.toString() + "/demand.bad");
            }

            try {
                List<String> lines = new ArrayList<String>();
                lines = Files.readAllLines(path, charset);

                for (int n = 0; n < lines.size(); n++) {
                    String p = lines.get(n).trim();
                    if (p.charAt(0) == '#') {
                        if (cleandata) LOGDATA(DATA_QUALITY.GOOD, p);
                        continue;
                    }
                    String[] parts = p.split(",", -1);

                    Plan plan = plans.get(parts[0]);
                    assert(plan != null);

                    SKU sku = skus.get(parts[3]);
                    assert(sku != null);

                    if (demands.containsKey(parts[1])) {
                        JobShop.LOG("Demand already exists: " + p);
                        if (cleandata) LOGDATA(DATA_QUALITY.BAD, p);
                        continue;
                    }

                    if (cleandata) LOGDATA(DATA_QUALITY.GOOD, p);

                    Demand dmd = new Demand(parts[1], parts[2], sku,
                                            LocalDateTime.parse(parts[4], dfs),
                                            Long.parseLong(parts[5]),
                                            Long.parseLong(parts[6]), plan);
                    demands.put(parts[1], dmd);
                }
            } catch (IOException e) {
                JobShop.LOG(e.getMessage());
            }
        }
        else if (mode.equals("DATABASE")) {
            try {
                ResultSet rs = statement.executeQuery("select * from demand");
                while (rs.next()) {
                    String planid = rs.getString("planid");
                    String demid = rs.getString("demandid");
                    String custid = rs.getString("customerid");
                    String skuid = rs.getString("skuid");
                    Timestamp due = rs.getTimestamp("duedate");
                    long dueqty = rs.getLong("duequantity");
                    long pri = rs.getLong("priority");

                    Plan plan = plans.get(planid);
                    assert(plan != null);

                    SKU sku = skus.get(skuid);
                    assert(sku != null);

                    Demand dmd = new Demand(demid, custid, sku, due.toLocalDateTime(),
                                            dueqty, pri, plan);
                    demands.put(demid, dmd);
                }
            }
            catch (SQLException e) {
                JobShop.LOG(e.getMessage());
            }
        }

    }

    /**
     * Utility function to establish the Task predecence relations
     */
    private void readTaskPrecedences() {

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) { 
            JobShop.LOG("Reading task precedence data...", true);
		}

        String mode = this.options.get("input_mode");

        if (mode.equals("FLATFILE")) {
            Path path = Paths.get(this.datadir + "/taskprecedence.csv");

            if (cleandata) {
                goodFile = Paths.get(JobShop.logDir.toString() + "/taskprecedence.good");
                badFile = Paths.get(JobShop.logDir.toString() + "/taskprecedence.bad");
            }

            try {
                List<String> lines = new ArrayList<String>();
                lines = Files.readAllLines(path, charset);

                for (int n = 0; n < lines.size(); n++) {
                    String p = lines.get(n).trim();
                    if (p.charAt(0) == '#') {
                        if (cleandata) LOGDATA(DATA_QUALITY.GOOD, p);
                        continue;
                    }
                    String[] parts = p.split(",", -1);
                    Task succ = tasks.get(parts[1] + "-" + parts[0]);
                    Task pred = tasks.get(parts[1] + "-" + parts[2]);

                    if (succ == null) {
                        JobShop.LOG("Successor task does not exists: " + p);
                        if (cleandata) LOGDATA(DATA_QUALITY.BAD, p);
                        continue;
                    }
                    if (pred == null) {
                        JobShop.LOG("Predecessor task does not exists: " + p);
                        if (cleandata) LOGDATA(DATA_QUALITY.BAD, p);
                        continue;
                    }

                    if(succ.getPredecessor() != null) {
                        JobShop.LOG("Successor task already has a predecessor: " + p);
                        if (cleandata) LOGDATA(DATA_QUALITY.BAD, p);
                        continue;
                    }
                    if(pred.getSuccessor() != null) {
                        JobShop.LOG("Predecessor task already has a successor: " + p);
                        if (cleandata) LOGDATA(DATA_QUALITY.BAD, p);
                        continue;
                    }

                    if (cleandata) LOGDATA(DATA_QUALITY.GOOD, p);

                    succ.setPredecessor(pred);
                    pred.setSuccessor(succ);
                }
            } catch (IOException e) {
                JobShop.LOG(e.getMessage());
            }
        }
        else if (mode.equals("DATABASE")) {
            try {
                ResultSet rs = statement.executeQuery("select * from taskprecedence");
                while (rs.next()) {
                    String taskid = rs.getString("taskid");
                    String skuid = rs.getString("skuid");
                    String predecessor = rs.getString("predecessor");

                    Task succ = tasks.get(skuid + "-" + taskid);
                    Task pred = tasks.get(skuid + "-" + predecessor);
                    assert(succ != null);
                    assert(pred != null);

                    succ.setPredecessor(pred);
                    pred.setSuccessor(succ);
                }
            }
            catch (SQLException e) {
                JobShop.LOG(e.getMessage());
            }
        }

    }

    /**
     * Utility function to establish the Task/Workcenter relationships
     */
    private void readTaskWorkcenterAssociations() {

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) { 
            JobShop.LOG("Reading task workcenter association data...", true);
		}

        String mode = this.options.get("input_mode");

        if (mode.equals("FLATFILE")) {
            Path path = Paths.get(this.datadir + "/taskworkcenterassn.csv");

            if (cleandata) {
                goodFile = Paths.get(JobShop.logDir.toString() + "/taskworkcenterassn.good");
                badFile = Paths.get(JobShop.logDir.toString() + "/taskworkcenterassn.bad");
            }

            try {
                List<String> lines = new ArrayList<String>();
                lines = Files.readAllLines(path, charset);

                for (int n = 0; n < lines.size(); n++) {
                    String p = lines.get(n).trim();
                    if (p.charAt(0) == '#') {
                        if (cleandata) LOGDATA(DATA_QUALITY.GOOD, p);
                        continue;
                    }
                    String[] parts = p.split(",", -1);
                    Task t = tasks.get(parts[1] + "-" + parts[0]);
                    Workcenter w = workcenters.get(parts[2]);
                    Integer priority = new Integer(parts[3]);

                    if(t == null) {
                        JobShop.LOG("Task does not exist in task workcenter association: " + p);
                        if (cleandata) LOGDATA(DATA_QUALITY.BAD, p);
                        continue;
                    }

                    if(w== null) {
                        JobShop.LOG("Workcenter does not exist in task workcenter association: " + p);
                        if (cleandata) LOGDATA(DATA_QUALITY.BAD, p);
                        continue;
                    }

                    if (cleandata) LOGDATA(DATA_QUALITY.GOOD, p);

                    t.addWorkcenter(w, priority);
                }
            } catch (IOException e) {
                JobShop.LOG(e.getMessage());
            }
        }
        else if (mode.equals("DATABASE")) {
            try {
                ResultSet rs = statement.executeQuery("select * from taskworkcenterassn");
                while (rs.next()) {
                    String taskid = rs.getString("taskid");
                    String skuid = rs.getString("skuid");
                    String wrkid = rs.getString("workcenterid");
                    Integer pri = Integer.valueOf(rs.getInt("priority"));

                    Task t = tasks.get(skuid + "-" + taskid);
                    Workcenter w = workcenters.get(wrkid);
                    assert(t != null);
                    assert(w != null);

                    t.addWorkcenter(w, pri);
                }
            }
            catch (SQLException e) {
                JobShop.LOG(e.getMessage());
            }
        }

    }

    /**
     * Utility function to create the ReleasedWorkOrder objects
     */
    private void readReleasedWorkOrders() {

        if (DEBUG.ordinal() >= DEBUG_LEVELS.MINIMAL.ordinal()) {
            JobShop.LOG("Reading released workorder data...", true);
		}

        String mode = this.options.get("input_mode");

        if (mode.equals("FLATFILE")) {
            Path path = Paths.get(this.datadir + "/relworkorder.csv");

            if (cleandata) {
                goodFile = Paths.get(JobShop.logDir.toString() + "/relworkorder.good");
                badFile = Paths.get(JobShop.logDir.toString() + "/relworkorder.bad");
            }

            try {
                List<String> lines = new ArrayList<String>();
                lines = Files.readAllLines(path, charset);

                for (int n = 0; n < lines.size(); n++) {
                    String p = lines.get(n).trim();
                    if (p.charAt(0) == '#') {
                        if (cleandata) LOGDATA(DATA_QUALITY.GOOD, p);
                        continue;
                    }
                    String[] parts = p.split(",", -1);

                    Plan pln = plans.get(parts[0]);
                    String woid = parts[1];
                    Integer lotid = Integer.valueOf(parts[2]);
                    Task t = tasks.get(parts[3] + "-" + parts[4]);
                    Workcenter w = workcenters.get(parts[8]);
                    Demand d = demands.get(parts[9]);
                    long quantity = Long.parseLong(parts[7]);
                    LocalDateTime st = LocalDateTime.parse(parts[5], dfs);
                    LocalDateTime en = LocalDateTime.parse(parts[6], dfs);

                    if(t == null) {
                        JobShop.LOG("Task does not exist in released workorder: " + p);
                        if (cleandata) LOGDATA(DATA_QUALITY.BAD, p);
                        continue;
                    }

                    if(parts[8].length() > 0 && w == null) {
                        JobShop.LOG("Workcenter does not exist in released workorder: " + p);
                        if (cleandata) LOGDATA(DATA_QUALITY.BAD, p);
                        continue;
                    }

                    if(parts[9].length() > 0 && d == null) {
                        JobShop.LOG("Demand does not exist in released workorder: " + p);
                        if (cleandata) LOGDATA(DATA_QUALITY.BAD, p);
                        continue;
                    }

                    if (cleandata) LOGDATA(DATA_QUALITY.GOOD, p);

                    ReleasedWorkOrder rwo = new ReleasedWorkOrder(woid,
                                                 lotid, t, pln, w,
                                                 st, en, quantity, d);

                    String wo_uniq_id = woid + "-" + lotid.toString();
                    relworkorders.put(wo_uniq_id, rwo);
                    t.addReleasedWorkOrder(rwo);
                }
            } catch (IOException e) {
                JobShop.LOG(e.getMessage());
            }
        }
        else if (mode.equals("DATABASE")) {
            try {
                ResultSet rs = statement.executeQuery("select * from relworkorder");
                while (rs.next()) {
                    String planid = rs.getString("planid");
                    String woid = rs.getString("workorderid");
                    Integer lotid = Integer.valueOf(rs.getInt("lotid"));
                    String taskid = rs.getString("taskid");
                    String skuid = rs.getString("skuid");
                    String wrkid = rs.getString("workcenterid");
                    Timestamp st = rs.getTimestamp("startdate");
                    Timestamp en = rs.getTimestamp("enddate");
                    long qty = rs.getLong("quantity");
                    String dmdid = rs.getString("demandid");

                    Plan pln = plans.get(planid);
                    Task t = tasks.get(skuid + "-" + taskid);
                    Workcenter w = workcenters.get(wrkid);
                    Demand d = demands.get(dmdid);

                    assert(t != null);
                    assert(wrkid.length() > 0 || w != null);
                    assert(pln != null);
                    assert(dmdid.length() > 0 || d != null);

                    ReleasedWorkOrder rwo = new ReleasedWorkOrder(woid,
                                                 lotid, t, pln, w,
                                                 st.toLocalDateTime(),
                                                 en.toLocalDateTime(),
                                                 qty, d);

                    String wo_uniq_id = woid + "-" + lotid.toString();
                    relworkorders.put(wo_uniq_id, rwo);
                    t.addReleasedWorkOrder(rwo);
                }
            }
            catch (SQLException e) {
                JobShop.LOG(e.getMessage());
            }
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
            JobShop.LOG("\nPlans:", true, DEBUG_LEVELS.MINIMAL);
            List<String> splans = this.plans.keySet()
                                    .stream()
                                    .sorted()
                                    .collect(Collectors.toList());

            for (String p : splans) {
                Plan plan = this.plans.get(p);
                JobShop.LOG(plan.toString(), true, DEBUG_LEVELS.MINIMAL);
            }

            JobShop.LOG("\nDemands:", true, DEBUG_LEVELS.MINIMAL);
            List<Demand> sdmds = this.demands.values()
                                    .stream()
                                    .sorted(Comparator.comparing(Demand::getPriority))
                                    .collect(Collectors.toList());
            for (Demand dmd : sdmds) {
                JobShop.LOG(dmd.toString(), true, DEBUG_LEVELS.MINIMAL);
            }

            JobShop.LOG("\nTaskPlans:", true, DEBUG_LEVELS.MINIMAL);
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
                    JobShop.LOG(tp.toString(), true, DEBUG_LEVELS.MINIMAL);
                }
            }

            JobShop.LOG("\nWorkcenterPlans:", true, DEBUG_LEVELS.MINIMAL);
            List<String> sworks = this.workcenters.keySet()
                                    .stream()
                                    .sorted()
                                    .collect(Collectors.toList());
            for (String w : sworks) {
                Workcenter wrk = this.workcenters.get(w);
                JobShop.LOG(wrk.toString(), true, DEBUG_LEVELS.MINIMAL);
                List<TaskPlan> tps = wrk.getTaskPlans().stream()
                                        .sorted(Comparator.comparing(TaskPlan::getStart))
                                        .collect(Collectors.toList());
                for (TaskPlan tp : tps) {
                    JobShop.LOG(" - " + tp, true, DEBUG_LEVELS.MINIMAL);
                }
            }
        }
        else if (mode.equals("DATABASE")) {

            String delStmt = "delete from taskplan";

            String stmt = "insert into taskplan " +
                          "(planid, demandid, skuid, taskid, startdate," +
                          " enddate, quantity, workcenterid, workorderid, lotid) values " +
                          "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
                        if (tp.getDemand() != null) {
                            pStmt.setString(2, tp.getDemand().getID());
                        }
                        else {
                            pStmt.setNull(2, java.sql.Types.VARCHAR);
                        }
                        pStmt.setString(3, tp.getTask().getSKU().getName());
                        pStmt.setString(4, tp.getTask().getTaskID());
                        pStmt.setTimestamp(5, Timestamp.valueOf(tp.getStart()));
                        pStmt.setTimestamp(6, Timestamp.valueOf(tp.getEnd()));
                        pStmt.setInt(7, Long.valueOf(tp.getQuantity()).intValue());
                        if (tp.getWorkcenter() != null) {
                            pStmt.setString(8, tp.getWorkcenter().getName());
                        }
                        else {
                            pStmt.setNull(8, java.sql.Types.VARCHAR);
                        }

                        if (tp.getReleasedWorkOrder() != null) {
                            pStmt.setString(9, tp.getReleasedWorkOrder().getID());
                            pStmt.setInt(10, tp.getReleasedWorkOrder().getLotID(tp).intValue());
                        }
                        else {
                            pStmt.setNull(9, java.sql.Types.VARCHAR);
                            pStmt.setNull(10, java.sql.Types.INTEGER);
                        }

                        // Customization for SQLITE (which cannot handle TimeStamps)
                        if (this.options.get("db_connection_string").contains("sqlite")) {
                            pStmt.setString(5, tp.getStart().format(sqliteDFS));
                            pStmt.setString(6, tp.getEnd().format(sqliteDFS));
                        }

                        pStmt.addBatch();
                    }
                }

                pStmt.executeBatch();
                JobShop.LOG("\nTaskplans written to database...", true, DEBUG_LEVELS.MINIMAL);
            }
            catch (SQLException e) {
                JobShop.LOG(e.getMessage());
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
