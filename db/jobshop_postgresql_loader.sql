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

\COPY plan(planid, planstart, planend) FROM 'plan.csv' DELIMITER ',' CSV HEADER;

\COPY planparameter(planid, paramname, paramvalue) FROM 'planparameter.csv' DELIMITER ',' CSV HEADER;

\COPY sku(skuid, description) FROM 'sku.csv' DELIMITER ',' CSV HEADER;

\COPY calendar(calendarid, calendartype) FROM 'calendar.csv' DELIMITER ',' CSV HEADER;

\COPY calendarshift(calendarid, shiftid, shiftstart, shiftend, shiftnumber, value) FROM 'calendarshift.csv' DELIMITER ',' CSV HEADER;

\COPY workcenter(workcenterid, efficiency_calendar, max_setups_per_shift, criticality_index) FROM 'workcenter.csv' DELIMITER ',' CSV HEADER;

\COPY task(taskid, skuid, setup_time, per_unit_time, min_lot_size, max_lot_size, is_delivery_task) FROM 'task.csv' DELIMITER ',' CSV HEADER;

\COPY taskprecedence(taskid, skuid, predecessor) FROM 'taskprecedence.csv' DELIMITER ',' CSV HEADER;

\COPY taskworkcenterassn(taskid, skuid, workcenterid, priority) FROM 'taskworkcenterassn.csv' DELIMITER ',' CSV HEADER;

\COPY demand(planid, demandid, customerid, skuid, duedate, duequantity, priority) FROM 'demand.csv' DELIMITER ',' CSV HEADER;
