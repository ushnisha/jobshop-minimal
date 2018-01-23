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

drop table if exists plan cascade;
create table plan (
    planid varchar(100) primary key not null,
    planstart timestamp not null,
    planend timestamp not null,
    date_created timestamp not null DEFAULT CURRENT_TIMESTAMP
);

drop table if exists planparameter cascade;
create table planparameter (
    planid varchar(100) not null,
    paramname varchar(100) not null,
    paramvalue varchar(100) not null,
    date_created timestamp not null DEFAULT CURRENT_TIMESTAMP,
    primary key(planid, paramname),
    foreign key(planid) references plan(planid)
);

drop table if exists sku cascade;
create table sku (
    skuid varchar(100) primary key not null,
    description varchar(100) not null,
    date_created timestamp not null DEFAULT CURRENT_TIMESTAMP
);

drop table if exists calendar cascade;
create table calendar (
    calendarid varchar(100) primary key not null,
    calendartype varchar(40) not null,
    date_created timestamp not null DEFAULT CURRENT_TIMESTAMP
);

drop table if exists calendarshift cascade;
create table calendarshift (
    calendarid varchar(100) not null,
    shiftid integer not null,
    shiftstart timestamp not null,
    shiftend timestamp not null,
    shiftnumber integer not null,
    value numeric not null,
    date_created timestamp not null DEFAULT CURRENT_TIMESTAMP,
    primary key(calendarid, shiftid),
    foreign key(calendarid) references calendar(calendarid)
);

drop table if exists workcenter cascade;
create table workcenter (
    workcenterid varchar(100) primary key not null,
    efficiency_calendar varchar(100) not null,
    max_setups_per_shift integer not null,
    criticality_index integer not null,
    date_created timestamp not null DEFAULT CURRENT_TIMESTAMP,
    foreign key(efficiency_calendar) references calendar(calendarid)
);

drop table if exists task cascade;
create table task (
    taskid varchar(100) not null,
    skuid varchar(100) not null,
    setup_time integer not null,
    per_unit_time integer not null,
    min_lot_size integer not null,
    max_lot_size integer not null,
    is_delivery_task char(1) not null,
    date_created timestamp not null DEFAULT CURRENT_TIMESTAMP,
    primary key(taskid, skuid),
    foreign key(skuid) references sku(skuid)
);

drop table if exists taskprecedence cascade;
create table taskprecedence (
    taskid varchar(100) not null,
    skuid varchar(100) not null,
    predecessor varchar(100) not null,
    date_created timestamp not null DEFAULT CURRENT_TIMESTAMP,
    primary key(taskid, skuid, predecessor),
    foreign key(taskid, skuid) references task(taskid, skuid),
    foreign key(predecessor, skuid) references task(taskid, skuid)
);

drop table if exists taskworkcenterassn cascade;
create table taskworkcenterassn (
    taskid varchar(100) not null,
    skuid varchar(100) not null,
    workcenterid varchar(100) not null,
    priority integer not null,
    date_created timestamp not null DEFAULT CURRENT_TIMESTAMP,
    primary key(taskid, skuid, workcenterid),
    foreign key(taskid, skuid) references task(taskid, skuid),
    foreign key(workcenterid) references workcenter(workcenterid)
);

drop table if exists demand cascade;
create table demand (
    planid varchar(100) not null,
    demandid varchar(100) not null,
    customerid varchar(100) not null,
    skuid varchar(100) not null,
    duedate timestamp not null,
    duequantity integer not null,
    priority integer not null,
    date_created timestamp not null DEFAULT CURRENT_TIMESTAMP,
    primary key(planid, demandid),
    foreign key(planid) references plan(planid),
    foreign key(skuid) references sku(skuid)
);

drop table if exists taskplan cascade;
create table taskplan (
    lotid serial primary key,
    planid varchar(100) not null,
    demandid varchar(100) not null,
    skuid varchar(100) not null,
    taskid varchar(100) not null,
    startdate timestamp not null,
    enddate timestamp not null,
    quantity integer not null,
    workcenterid varchar(100),
    date_created timestamp not null DEFAULT CURRENT_TIMESTAMP,
    foreign key(planid, demandid) references demand(planid, demandid),
    foreign key(skuid, taskid) references task(skuid, taskid),
    foreign key(workcenterid) references workcenter(workcenterid)
);
