/*
 * Main data management system (MDMS) cfe_18
 * Copyright (C) 2021  Suomen Kanuuna Oy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://github.com/teragrep/teragrep/blob/main/LICENSE>.
 *
 *
 * Additional permission under GNU Affero General Public License version 3
 * section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with other code, such other code is not for that reason alone subject to any
 * of the requirements of the GNU Affero GPL version 3 as long as this Program
 * is the same Program as licensed from Suomen Kanuuna Oy without any additional
 * modifications.
 *
 * Supplemented terms under GNU Affero General Public License version 3
 * section 7
 *
 * Origin of the software must be attributed to Suomen Kanuuna Oy. Any modified
 * versions must be marked as "Modified version of" The Program.
 *
 * Names of the licensors and authors may not be used for publicity purposes.
 *
 * No rights are granted for use of trade names, trademarks, or service marks
 * which are in The Program if any.
 *
 * Licensee must indemnify licensors and authors for any liability that these
 * contractual assumptions impose on licensors and authors.
 *
 * To the extent this program is licensed as part of the Commercial versions of
 * Teragrep, the applicable Commercial License may apply to this file if you as
 * a licensee so wish it.
 */
use flow;
create table storages
(
    id           int auto_increment primary key,
    storage_name varchar(255) unique not null,
    cfe_type     varchar(6)          not null check (cfe_type in ('cfe_10', 'cfe_11', 'cfe_12', 'cfe_19', 'cfe_23', 'cfe_04')),
    unique KEY (id, cfe_type)
);

create table L7
(
    id           int auto_increment primary key,
    app_protocol varchar(64) unique not null
);

create table flows
(
    id   int auto_increment primary key,
    name varchar(255) unique not null


);

create table capture_sink
(
    id         int auto_increment primary key,
    L7_id      int,
    flow_id    int,
    ip_address varchar(16) not null,
    sink_port  varchar(5)  not null,
    constraint ´L7_id_TO_L7´ foreign key (L7_id) references L7 (id),
    constraint foreign key (flow_id) references flows (id),
    unique key (flow_id, L7_id)
);



create table cfe_10
(
    id       int primary key,
    cfe_type varchar(6) not null check (cfe_type = 'cfe_10'),
    spool    varchar(50),
    constraint ´storagesToCfe_10´ foreign key (id, cfe_type) references storages (id, cfe_type) on delete cascade
);

create table cfe_11
(
    id         int primary key,
    cfe_type   varchar(6) not null check (cfe_type = 'cfe_11'),
    inspection varchar(50),
    constraint ´storagesToCfe_11´ foreign key (id, cfe_type) references storages (id, cfe_type) on delete cascade
);

create table cfe_12
(
    id       int primary key,
    cfe_type varchar(6) not null check (cfe_type = 'cfe_12'),
    constraint ´storagesToCfe_12´ foreign key (id, cfe_type) references storages (id, cfe_type) on delete cascade
);

create table cfe_04
(
    id       int primary key,
    cfe_type varchar(6) not null check (cfe_type = 'cfe_04'),
    constraint ´storagesToCfe_04´ foreign key (id, cfe_type) references storages (id, cfe_type) on delete cascade
);

create table cfe_19
(
    id       int primary key,
    cfe_type varchar(6) not null check (cfe_type = 'cfe_19'),
    constraint ´storagesToCfe_19´ foreign key (id, cfe_type) references storages (id, cfe_type) on delete cascade
);

create table cfe_23
(
    id       int primary key,
    cfe_type varchar(6) not null check (cfe_type = 'cfe_23'),
    constraint ´storagesToCfe_23´ foreign key (id, cfe_type) references storages (id, cfe_type) on delete cascade
);


create table routers
(
    id      int auto_increment primary key,
    flow_id int not null,
    constraint ´routerToFlow´ foreign key (flow_id) references flows (id)
);

create table flow_targets
(
    id           int auto_increment primary key,
    index (flow_id),
    flow_id      int        not null,
    storage_id   int        not null,
    storage_type varchar(6) not null check (storage_type in
                                            ('cfe_10', 'cfe_11', 'cfe_12', 'cfe_19',
                                             'cfe_23', 'cfe_04')),
    constraint ´flow_idToFlows´ foreign key (flow_id) references flows (id),
    constraint ´storage_idToStorage´ foreign key (storage_id, storage_type) references storages (id, cfe_type),
    unique key (flow_id, storage_id)
);




