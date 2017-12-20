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

drop table if exists _plan_staging;
create table _plan_staging (
    planid varchar(100) primary key not null,
    planstart datetime not null,
    planend datetime not null
);

drop table if exists _planparameter_staging;
create table _planparameter_staging (
    planid varchar(100) not null,
    paramname varchar(100) not null,
    paramvalue varchar(100) not null,
    primary key(planid, paramname),
    foreign key(planid) references _plan_staging(planid)
);

drop table if exists _sku_staging;
create table _sku_staging (
    skuid varchar(100) primary key not null,
    description varchar(100) not null
);

drop table if exists _calendar_staging;
create table _calendar_staging (
    calendarid varchar(100) primary key not null,
    calendartype varchar(40) not null
);

drop table if exists _calendarshift_staging;
create table _calendarshift_staging (
    calendarid varchar(100) not null,
    shiftid integer not null,
    shiftstart datetime not null,
    shiftend datetime not null,
    shiftnumber integer not null,
    value number not null,
    primary key(calendarid, shiftid),
    foreign key(calendarid) references _calendar_staging(calendarid)
);

drop table if exists _workcenter_staging;
create table _workcenter_staging (
    workcenterid varchar(100) primary key not null,
    efficiency_calendar varchar(100) not null,
    max_setups_per_shift integer not null,
    criticality_index integer not null,
    foreign key(efficiency_calendar) references _calendar_staging(calendarid)
);

drop table if exists _task_staging;
create table _task_staging (
    taskid varchar(100) not null,
    skuid varchar(100) not null,
    setup_time integer not null,
    per_unit_time integer not null,
    min_lot_size integer not null,
    max_lot_size integer not null,
    is_delivery_task boolean not null,
    primary key(taskid, skuid),
    foreign key(skuid) references _sku_staging(skuid)
);

drop table if exists _taskprecedence_staging;
create table _taskprecedence_staging (
    taskid varchar(100) not null,
    skuid varchar(100) not null,
    predecessor varchar(100) not null,
    primary key(taskid, skuid, predecessor),
    foreign key(taskid, skuid) references _task_staging(taskid, skuid),
    foreign key(predecessor, skuid) references _task_staging(taskid, skuid)
);

drop table if exists _taskworkcenterassn_staging;
create table _taskworkcenterassn_staging (
    taskid varchar(100) not null,
    skuid varchar(100) not null,
    workcenterid varchar(100) not null,
    priority integer not null,
    primary key(taskid, skuid, workcenterid),
    foreign key(taskid, skuid) references _task_staging(taskid, skuid),
    foreign key(workcenterid) references _workcenter_staging(workcenterid)
);

drop table if exists _demand_staging;
create table _demand_staging (
    planid varchar(100) not null,
    demandid varchar(100) not null,
    customerid varchar(100) not null,
    skuid varchar(100) not null,
    duedate datetime not null,
    duequantity integer not null,
    priority integer not null,
    primary key(planid, demandid),
    foreign key(planid) references _plan_staging(planid),
    foreign key(skuid) references _sku_staging(skuid)
);

drop table if exists _taskplan_staging;
create table _taskplan_staging (
    lotid integer primary key autoincrement,
    planid varchar(100) not null,
    demandid varchar(100) not null,
    skuid varchar(100) not null,
    taskid varchar(100) not null,
    startdate datetime not null,
    enddate datetime not null,
    quantity integer not null,
    workcenterid varchar(100),
    foreign key(planid, demandid) references _demand_staging(planid, demandid),
    foreign key(skuid, taskid) references _task_staging(skuid, taskid),
    foreign key(workcenterid) references _workcenter_staging(workcenterid)
);

