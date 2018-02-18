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

\COPY relworkorder(planid, workorderid, lotid, skuid, taskid, startdate, enddate, quantity, workcenterid, demandid) FROM 'relworkorder.csv' DELIMITER ',' CSV HEADER;
