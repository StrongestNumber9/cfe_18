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
use cfe_03;

create table interfaces
(
    id        int auto_increment primary key,
    interface varchar(255) not null unique
);
create table ip_addresses
(
    id         int auto_increment primary key,
    ip_address varchar(255) not null unique
);

create table arch_type
(
    id   int auto_increment primary key,
    arch varchar(255) unique not null
);

create table flavor_type
(
    id     int auto_increment primary key,
    flavor varchar(255) unique not null
);

create table os_type
(
    id int auto_increment primary key,
    os varchar(255) unique not null
);

create table release_version
(
    id      int auto_increment primary key,
    rel_ver varchar(255) unique not null
);


create table host_meta
(
    id             int primary key auto_increment,
    release_ver_id int,
    flavor_id      int,
    arch_id        int,
    os_id          int,
    hostname       varchar(255),
    host_id        int,
    unique (id, host_id),
    constraint relver foreign key (release_ver_id) references release_version (id),
    constraint flavor foreign key (flavor_id) references flavor_type (id),
    constraint arch foreign key (arch_id) references arch_type (id),
    constraint os foreign key (os_id) references os_type (id),
    constraint ´MD5ToLocationHost´ foreign key (host_id) references location.host (id) on delete cascade
);


create table host_meta_x_interface
(
    id           int auto_increment primary key,
    host_meta_id int not null,
    interface_id int not null,
    constraint manyToInterface foreign key (interface_id) references interfaces (id),
    constraint interfacesToHostMeta foreign key (host_meta_id) references host_meta (id) on delete cascade,
    unique key (host_meta_id, interface_id)
);

create table host_meta_x_ip
(
    id           int auto_increment primary key,
    host_meta_id int not null,
    ip_id        int not null,
    constraint manyToIp foreign key (ip_id) references ip_addresses (id),
    constraint ipsToHostMeta foreign key (host_meta_id) references host_meta (id) on delete cascade,
    unique key (host_meta_id, ip_id)
);









