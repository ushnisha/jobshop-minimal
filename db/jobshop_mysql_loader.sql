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

delete FROM taskplan;
delete FROM demand;
delete FROM taskworkcenterassn;
delete FROM taskprecedence;
delete FROM task;
delete FROM workcenter;
delete FROM calendarshift;
delete FROM calendar;
delete FROM sku;
delete FROM planparameter;
delete FROM plan;

LOAD DATA LOCAL INFILE 'plan.csv' INTO TABLE plan FIELDS TERMINATED BY ',' IGNORE 1 LINES (planid, planstart, planend);

LOAD DATA LOCAL INFILE 'planparameter.csv' INTO TABLE planparameter FIELDS TERMINATED BY ',' IGNORE 1 LINES (planid, paramname, paramvalue);

LOAD DATA LOCAL INFILE 'sku.csv' INTO TABLE sku FIELDS TERMINATED BY ',' IGNORE 1 LINES (skuid, description);

LOAD DATA LOCAL INFILE 'calendar.csv' INTO TABLE calendar FIELDS TERMINATED BY ',' IGNORE 1 LINES (calendarid, calendartype);

LOAD DATA LOCAL INFILE 'calendarshift.csv' INTO TABLE calendarshift FIELDS TERMINATED BY ',' IGNORE 1 LINES (calendarid, shiftid, shiftstart, shiftend, shiftnumber, value);

LOAD DATA LOCAL INFILE 'workcenter.csv' INTO TABLE workcenter FIELDS TERMINATED BY ',' IGNORE 1 LINES (workcenterid, efficiency_calendar, max_setups_per_shift, criticality_index);

LOAD DATA LOCAL INFILE 'task.csv' INTO TABLE task FIELDS TERMINATED BY ',' IGNORE 1 LINES (taskid, skuid, setup_time, per_unit_time, min_lot_size, max_lot_size, is_delivery_task);

LOAD DATA LOCAL INFILE 'taskprecedence.csv' INTO TABLE taskprecedence FIELDS TERMINATED BY ',' IGNORE 1 LINES (taskid, skuid, predecessor);

LOAD DATA LOCAL INFILE 'taskworkcenterassn.csv' INTO TABLE taskworkcenterassn FIELDS TERMINATED BY ',' IGNORE 1 LINES (taskid, skuid, workcenterid, priority);

LOAD DATA LOCAL INFILE 'demand.csv' INTO TABLE demand FIELDS TERMINATED BY ',' IGNORE 1 LINES (planid, demandid, customerid, skuid, duedate, duequantity, priority);
