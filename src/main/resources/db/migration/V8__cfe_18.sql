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
use `cfe_18`;

create table inputtype
(
    id        int auto_increment primary key,
    inputtype varchar(20) not null check (inputtype in ('regex', 'newline')),
    unique key (id, inputtype)
);

create table regex
(
    id        int primary key,
    regex     varchar(255) not null,
    inputtype varchar(20)  not null check (inputtype = 'regex'),
    constraint inputTypeRegex foreign key (id, inputtype) references inputtype (id, inputtype)
);

create table newline
(
    id        int primary key,
    newline   varchar(255) not null,
    inputtype varchar(20)  not null check (inputtype = 'newline'),
    constraint inputTypeNewline foreign key (id, inputtype) references inputtype (id, inputtype)
);

create table ruleset
(
    id   int auto_increment primary key,
    rule varchar(1000) not null unique
);

create table templates
(
    id       int auto_increment primary key,
    template varchar(255) not null unique
);

create table capture_type
(
    id           int auto_increment primary key,
    capture_type varchar(64) not null check (capture_type in
                                             ('aws', 'manual', 'cfe', 'windows', 'hec',
                                              'azure', 'relp')),
    unique key (id, capture_type)
);

create table application
(
    id  int auto_increment primary key,
    app varchar(48) not null unique
);

create table retentionTime
(
    id        int auto_increment primary key,
    retention varchar(255) not null unique
);

create table tags
(
    id  int auto_increment primary key,
    tag varchar(48) not null unique
);

create table captureIndex
(
    id           int auto_increment primary key,
    captureIndex varchar(48) not null unique
);
create table captureSourcetype
(
    id                int auto_increment primary key,
    captureSourceType varchar(255) not null unique
);
create table category
(
    id       int auto_increment primary key,
    category varchar(48) not null unique
);


create table capture_definition
(
    id                   int auto_increment primary key,
    tag_id               int         not null,
    application_id       int         not null,
    captureIndex_id      int         not null,
    retentionTime_id     int         not null,
    captureSourcetype_id int         not null,
    category_id          int         not null,
    capture_type         varchar(64) not null check (capture_type in
                                                     ('aws', 'manual', 'cfe', 'windows', 'hec', 'azure', 'relp')),
    capture_type_id      int         not null,
    L7_id                int         not null,
    flow_id              int         not null,
    constraint ´application´ foreign key (application_id) references application (id),
    constraint ´captureIndexTocaptureIndex´ foreign key (captureIndex_id) references captureIndex (id),
    constraint ´retentionTime´ foreign key (retentionTime_id) references retentionTime (id),
    constraint ´captureSourceTypeTocaptureSourcetype´ foreign key (captureSourcetype_id) references captureSourcetype (id),
    constraint ´category_id_TO_category´ foreign key (category_id) references category (id),
    constraint ´captureToProcessing´ foreign key (capture_type_id) references capture_type (id) on delete cascade,
    constraint ´tag_id_TO_tags´ foreign key (tag_id) references tags (id),
    constraint ´flow_id_TO_flows´ foreign key (flow_id) references flow.flows (id),
    constraint ´flow_L7_id_TO_sink´ foreign key (flow_id, L7_id) references flow.capture_sink (flow_id, L7_id),
    unique key (id, capture_type),
    unique key (id, tag_id),
    unique key (id, flow_id),
    unique key (id, capture_type_id),
    index (flow_id)
);



create table cfe_18.processing_type
(
    id           int auto_increment primary key,
    inputtype_id int,
    ruleset_id   int,
    template_id  int,
    type_name    varchar(48) unique,
    constraint ´input´ foreign key (inputtype_id) references inputtype (id),
    constraint ´ruleset´ foreign key (ruleset_id) references ruleset (id),
    constraint ´template´ foreign key (template_id) references templates (id),
    index (type_name),
    unique (inputtype_id, ruleset_id, template_id, type_name)

);

create table capture_meta_file
(
    id                 int primary key,
    capturePath        varchar(255) not null,
    tagPath            varchar(255) default null,
    processing_type_id int          not null,
    capture_type       varchar(64)  not null check (capture_type = 'cfe'),
    constraint captureTypeCfe foreign key (id, capture_type) references capture_type (id, capture_type) on delete cascade,
    constraint metaFileToMetaType foreign key (processing_type_id) references processing_type (id)
);

create table capture_meta_aws
(
    id            int auto_increment primary key,
    kinesis_name  varchar(255) not null,
    capture_group varchar(255) not null,
    capture_type  varchar(64)  not null check (capture_type = 'aws'),
    constraint captureTypeAws foreign key (id, capture_type) references capture_type (id, capture_type)
);

create table capture_meta_metrics
(
    id           int auto_increment primary key,
    capture_type varchar(64) not null check (capture_type = 'metrics'),
    constraint captureTypeMetrics foreign key (id, capture_type) references capture_type (id, capture_type)

);

create table capture_def_group
(
    id                     int auto_increment primary key,
    capture_def_group_name varchar(64) unique not null,
    capture_type           varchar(64)        not null check (capture_type in
                                                              ('aws', 'manual', 'cfe', 'windows', 'hec', 'azure',
                                                               'relp')),
    unique (id, capture_type)
);


create table capture_def_group_x_capture_def
(
    id                   int auto_increment primary key,
    capture_def_id       int         not null,
    capture_def_group_id int         not null,
    tag_id               int         not null,
    capture_type         varchar(64) not null check (capture_type in
                                                     ('aws', 'manual', 'cfe', 'windows', 'hec',
                                                      'azure', 'relp')),
    constraint captureToCaptureGroupType foreign key (capture_def_id, capture_type) references cfe_18.capture_definition (id, capture_type) on delete cascade,
    constraint captureTypeToCaptureGroup foreign key (capture_def_group_id, capture_type) references cfe_18.capture_def_group (id, capture_type) on delete cascade,
    unique key (capture_def_group_id, capture_def_id),
    unique key (capture_def_group_id, tag_id),
    index (capture_type, capture_def_group_id)
);


create table host_groups_x_capture_def_group
(
    id               int auto_increment primary key,
    host_group_id    int not null,
    capture_group_id int not null,
    constraint host_group_id_TO_host_group foreign key (host_group_id) references location.host_group (id),
    constraint ´capture_id_to_junction´ foreign key (capture_group_id) references capture_def_group (id),
    unique key (host_group_id, capture_group_id)
);



create table capture_def_x_flow_targets
(
    id             int primary key auto_increment,
    capture_def_id int not null,
    flow_id        int not null,
    flow_target_id int not null,
    constraint ´target_id_TO_flow_targets´ foreign key (flow_id, flow_target_id) references flow.flow_targets (flow_id, storage_id),
    constraint ´capture_def_id_TO_capture_definition´ foreign key (flow_id, capture_def_id) references cfe_18.capture_definition (flow_id, id) on delete cascade,
    unique (capture_def_id, flow_id, flow_target_id)
);

