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

delete FROM demandplan;
delete FROM taskplan;
delete FROM relworkorder;
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

LOAD DATA LOCAL INFILE 'relworkorder.csv' INTO TABLE relworkorder FIELDS TERMINATED BY ',' IGNORE 1 LINES (planid, workorderid, lotid, skuid, taskid, startdate, enddate, quantity, workcenterid, demandid);
