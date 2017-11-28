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

/**
 * The overall JobShop model class that is used to drive this application
 */
public class JobShop {

    private String dataSet;
    private Map<String,Plan> plans;
    private Map<String,Calendar> calendars;
    private Map<String,SKU> skus;
    private Map<String,Demand> demands;
    private Map<String,Task> tasks;
    private Map<Task,TaskPlan> taskplans;
    private Map<String,Workcenter> workcenters;

    /**
     * Main function that is run for simulating a JobShop planning process
     * @param args String[] of input arguments.  Currently, assumes:
     *             args[0] - name of the plan for which we want to generate a plan
     *             If args[0] is not specified it will pick the first plan available
     *             after importing the Plan file from the data directory
     */
    public static void main(String args[]) {

        System.out.println("\nA Minimal JobShop Planner");

		// Process the input argument; currently only two optional settings
		// are supported
		// (1) -d flag followed by a directory name that points to the dataset directory
		// (2) -p flag follwed by the name of the Plan that we want to plan
		
		if (args.length != 0 && args.length != 2 && args.length != 4) {
			usage();
		}
		
		// Default dataSet location is the "data" directory in the current working directory
		String dataSet = "./data";
		String planName = "";

		for (int i = 0; i < args.length; i += 2) {
			if (args[i].equals("-d")) {
				dataSet = args[i+1];
			}
			else if (args[i].equals("-p")) {
				planName = args[i+1];
			}
			else {
				usage();
			}
		}
		
        JobShop jshop = new JobShop(dataSet);
        Plan p = null;

        if (planName.equals("")) {
			String firstKey = (new ArrayList<String>(jshop.plans.keySet())).get(0);
			p = jshop.plans.get(firstKey);
		}
		else {
			p = jshop.plans.get(planName);
		}
		
        SimpleJobShopSolver solver = new SimpleJobShopSolver(jshop);
        solver.runStaticAnalysis(p);
        solver.generatePlan(p);

        jshop.print();

    }

    /**
     * Constructor for the JobShop object
     * @param n String representing the dataset for which we are creating
     *          this JobShop model.
     */
    public JobShop(String n) {
		
        this.dataSet = n;
        this.plans = new HashMap<String,Plan>();
        this.calendars = new HashMap<String,Calendar>();
        this.skus = new HashMap<String,SKU>();
        this.demands = new HashMap<String,Demand>();
        this.tasks = new HashMap<String,Task>();
        this.taskplans = new HashMap<Task,TaskPlan>();
        this.workcenters = new HashMap<String,Workcenter>();

        this.loadData();
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

		System.out.println("Reading plan data...");
        Path path = Paths.get(this.dataSet + "/plan.csv");
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
            Plan plan = new Plan(parts[0], LocalDateTime.parse(parts[1]), LocalDateTime.parse(parts[2]));
            plans.put(parts[0], plan);
        }
    }


    /**
     * Utility function to create the PlanParams objects
     */
    private void readPlanParams() {

		System.out.println("Reading planparam data...");
        Path path = Paths.get(this.dataSet + "/planparam.csv");
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
            Plan plan = plans.get(parts[0]);
            PlanParams pp = new PlanParams(plan);
            plan.setPlanParams(pp);
            pp.setParam(parts[1], parts[2]);
        }
    }


    /**
     * Utility function to create the SKU objects
     */
    private void readSKUs() {

		System.out.println("Reading sku data...");
        Path path = Paths.get(this.dataSet + "/sku.csv");
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
            SKU s = new SKU(parts[0],parts[1]);
            skus.put(parts[0], s);
        }
    }

    /**
     * Utility function to create the Calendar objects
     */
    private void readCalendars() {

		System.out.println("Reading calendar data...");
        Path path = Paths.get(this.dataSet + "/calendar.csv");
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
            Calendar cal = new Calendar(parts[0], parts[1]);
            calendars.put(parts[0], cal);
        }
    }

    /**
     * Utility function to create the CalendarShift objects
     */
    private void readCalendarShifts() {

		System.out.println("Reading calendarshift data...");
        Path path = Paths.get(this.dataSet + "/calendar_shift.csv");
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

		System.out.println("Reading workcenter data...");
        Path path = Paths.get(this.dataSet + "/workcenter.csv");
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

		System.out.println("Reading task data...");
        Path path = Paths.get(this.dataSet + "/task.csv");
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
            SKU sku = skus.get(parts[1]);
            assert(sku != null);
            Task task = new Task(parts[0], sku, Long.parseLong(parts[2]),
                                 Long.parseLong(parts[3]),
                                 Long.parseLong(parts[4]),
                                 Long.parseLong(parts[5]));
            tasks.put(parts[0], task);
            if (Boolean.parseBoolean(parts[6])) {
                sku.setDeliveryTask(task);
            }
        }
    }

    /**
     * Utility function to create the Demand objects
     */
    private void readDemands() {

		System.out.println("Reading demand data...");
        Path path = Paths.get(this.dataSet + "/demand.csv");
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

            Plan plan = plans.get(parts[0]);
            assert(plan != null);

            SKU sku = skus.get(parts[3]);
            assert(sku != null);

            Demand dmd = new Demand(parts[2], parts[1], sku,
                                    LocalDateTime.parse(parts[4]),
                                    Long.parseLong(parts[5]),
                                    Long.parseLong(parts[6]), plan);
            demands.put(parts[2], dmd);
        }
    }

    /**
     * Utility function to establish the Task predecence relations
     */
    private void readTaskPrecedences() {

		System.out.println("Reading task precedence data...");
        Path path = Paths.get(this.dataSet + "/task_precedence.csv");
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
            Task succ = tasks.get(parts[0]);
            Task pred = tasks.get(parts[1]);
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

		System.out.println("Reading task workcenter association data...");
        Path path = Paths.get(this.dataSet + "/task_workcenter_assn.csv");
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
            Task t = tasks.get(parts[0]);
            Workcenter w = workcenters.get(parts[1]);
            Integer priority = new Integer(parts[2]);
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

        System.out.println("\nPlans:");
        for (String p : this.plans.keySet()) {
            Plan plan = this.plans.get(p);
            System.out.println(plan);
        }

        System.out.println("\nSKUs:");
        for (String s : this.skus.keySet()) {
            SKU sku = this.skus.get(s);
            System.out.println(sku);
        }

        System.out.println("\nCalendars:");
        for (String c : this.calendars.keySet()) {
            Calendar cal = calendars.get(c);
            System.out.println(cal);
        }

        System.out.println("\nWorkcenters:");
        for (String w : this.workcenters.keySet()) {
            Workcenter ws = this.workcenters.get(w);
            System.out.println(ws);
        }

        System.out.println("\nTasks:");
        for (String t : this.tasks.keySet()) {
            Task task = this.tasks.get(t);
            System.out.println(task);
        }

        System.out.println("\nDemands:");
        for (String d : this.demands.keySet()) {
            Demand dmd = this.demands.get(d);
            System.out.println(dmd);
        }

        System.out.println("\nTaskPlans:");
        for (String t : this.tasks.keySet()) {
            Task task = this.tasks.get(t);
            for (TaskPlan tp : task.getTaskPlans()) {
                System.out.println(tp);
            }
        }

    }
    
    public static void usage() {
		System.out.println("Usage is:");
		System.out.println("\tjava -jar bin/JobShop.jar -d <dataset_directory> -p <PlanID>");
		System.out.println("\t... if -d option is not specified we will try to load a directory");
		System.out.println("\t... called 'data' in the current directory by default");
		System.out.println("\t... if the -p option is not specified, we will try to plan");
		System.out.println("\t... any arbitrary Plan in the input data");
		System.out.println("\n");
		System.exit(1);
	}

}
