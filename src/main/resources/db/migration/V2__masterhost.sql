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
use location;

create table host
(
    id        int primary key auto_increment,
    MD5       varchar(32) unique  not null,
    fqhost    varchar(128) unique not null,
    host_type varchar(20)         not null check (host_type in ('aws', 'manual', 'cfe', 'windows', 'hec', 'azure', 'relp')),
    unique key (id, host_type)
);

create table host_type_aws
(
    id        int         not null,
    accountId BIGINT      not null,
    host_type varchar(20) not null check (host_type = 'aws'),
    constraint hostTypeAws foreign key (id, host_type) references host (id, host_type) on delete cascade
);

create table host_type_manual
(
    id        int          not null,
    comments  varchar(255) not null,
    host_type varchar(20)  not null check (host_type = 'manual'),
    constraint hostTypeManual foreign key (id, host_type) references host (id, host_type) on delete cascade
);



create table host_type_windows
(
    id        int          not null,
    domain    varchar(255) not null,
    host_type varchar(20)  not null check (host_type = 'windows'),
    constraint hostTypeWindows foreign key (id, host_type) references host (id, host_type) on delete cascade
);

create table host_type_hec
(
    id        int          not null,
    token     varchar(255) not null,
    host_type varchar(20)  not null check (host_type = 'hec'),
    constraint hostTypeHec foreign key (id, host_type) references host (id, host_type) on delete cascade
);

create table host_type_azure
(
    id        int         not null,
    host_type varchar(20) not null check (host_type = 'azure'),
    constraint hostTypeAzure foreign key (id, host_type) references host (id, host_type) on delete cascade
);

create table host_group
(
    id        int primary key auto_increment,
    host_type varchar(64)  not null check (host_type in ('aws', 'manual', 'cfe', 'windows', 'hec', 'azure', 'relp')),
    groupName varchar(255) not null,
    unique (id, host_type)
);
create table host_group_x_host
(
    id            int primary key auto_increment,
    host_group_id int         not null,
    host_id       int         not null,
    host_type     varchar(64) not null check (host_type in ('aws', 'manual', 'cfe', 'windows', 'hec', 'azure', 'relp')),
    constraint hostToHostGroupType foreign key (host_id, host_type) references location.host (id, host_type),
    constraint hostTypeToHostGroup foreign key (host_group_id, host_type) references location.host_group (id, host_type) on delete cascade,
    unique key (host_group_id, host_id),
    index (host_type, host_group_id)
);

