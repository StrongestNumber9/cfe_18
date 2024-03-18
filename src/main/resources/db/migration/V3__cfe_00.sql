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
use cfe_00;


create table hubs
(
    id        int auto_increment primary key,
    host_id   int unique   not null,
    ip        varchar(255) not null,
    host_type varchar(20)  not null check (host_type = 'cfe'),
    constraint ´HubIdToHost´ foreign key (host_id, host_type) references location.host (id, host_type)

);

create table promises
(
    id       int auto_increment primary key,
    copy_dir varchar(255) not null,
    hub_id   int          not null,
    constraint ´HubIdToHubs´ foreign key (hub_id) references hubs (id),
    unique key (id, hub_id)
);

create table include_cf
(
    id         int          not null,
    include_cf varchar(255) not null,
    constraint ´promise_id´ foreign key (id) references promises (id)
);

create table bundles
(
    id          int          not null,
    bundle_name varchar(255) not null,
    constraint ´bundle_id´ foreign key (id) references promises (id)
);

create table host_type_cfe
(
    host_id   int         not null,
    host_type varchar(20) not null check (host_type = 'cfe'),
    hub_id    int         not null,
    constraint hostTypeCfe foreign key (host_id, host_type) references location.host (id, host_type) on delete cascade,
    constraint hub_id_TO_hubs foreign key (hub_id) references hubs (id),
    unique key (host_id, hub_id)
);

create table host_cfe_x_promise
(
    id         int auto_increment primary key,
    host_id    int not null,
    hub_id     int not null,
    promise_id int not null,
    unique key (host_id, promise_id),
    constraint host_id_TO_host foreign key (host_id, hub_id) references host_type_cfe (host_id, hub_id),
    constraint promise_id_TO_promise foreign key (promise_id, hub_id) references promises (id, hub_id)
);
