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

delete from _plan_staging;
delete from _planparameter_staging;
delete from _sku_staging;
delete from _calendar_staging;
delete from _calendarshift_staging;
delete from _workcenter_staging;
delete from _task_staging;
delete from _taskprecedence_staging;
delete from _taskworkcenterassn_staging;
delete from _demand_staging;
delete from _taskplan_staging;

.separator ,
.import plan.csv _plan_staging
.import planparameter.csv _planparameter_staging
.import sku.csv _sku_staging
.import calendar.csv _calendar_staging
.import calendarshift.csv _calendarshift_staging
.import workcenter.csv _workcenter_staging
.import task.csv _task_staging
.import taskprecedence.csv _taskprecedence_staging
.import taskworkcenterassn.csv _taskworkcenterassn_staging
.import demand.csv _demand_staging

delete from plan;
delete from planparameter;
delete from sku;
delete from calendar;
delete from calendarshift;
delete from workcenter;
delete from task;
delete from taskprecedence;
delete from taskworkcenterassn;
delete from demand;
delete from taskplan;

insert into plan (planid, planstart, planend)
select planid, planstart, planend
from _plan_staging;

insert into planparameter (planid, paramname, paramvalue)
select planid, paramname, paramvalue
from _planparameter_staging;

insert into sku (skuid, description)
select skuid, description
from _sku_staging;

insert into calendar (calendarid, calendartype)
select calendarid, calendartype
from _calendar_staging;

insert into calendarshift (calendarid, shiftid, shiftstart, shiftend, 
                           shiftnumber, value)
select calendarid, shiftid, shiftstart, shiftend, shiftnumber, value
from _calendarshift_staging;

insert into workcenter (workcenterid, efficiency_calendar, 
                        max_setups_per_shift, criticality_index)
select workcenterid, efficiency_calendar, max_setups_per_shift,
       criticality_index
from _workcenter_staging;

insert into task (taskid, skuid, setup_time, per_unit_time,
                  min_lot_size, max_lot_size, is_delivery_task)
select taskid, skuid, setup_time, per_unit_time, min_lot_size,
       max_lot_size, is_delivery_task
from _task_staging;

insert into taskprecedence (taskid, skuid, predecessor)
select taskid, skuid, predecessor
from _taskprecedence_staging;

insert into taskworkcenterassn (taskid, skuid, workcenterid, priority)
select taskid, skuid, workcenterid, priority
from _taskworkcenterassn_staging;

insert into demand (planid, demandid, customerid, skuid, duedate,
                    duequantity, priority)
select planid, demandid, customerid, skuid, duedate, duequantity, priority
from _demand_staging;

drop table if exists _plan_staging;
drop table if exists _planparameter_staging;
drop table if exists _sku_staging;
drop table if exists _calendar_staging;
drop table if exists _calendarshift_staging;
drop table if exists _workcenter_staging;
drop table if exists _task_staging;
drop table if exists _taskprecedence_staging;
drop table if exists _taskworkcenterassn_staging;
drop table if exists _demand_staging;
drop table if exists _taskplan_staging;

