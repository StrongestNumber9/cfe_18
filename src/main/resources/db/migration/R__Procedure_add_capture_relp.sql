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
use cfe_18;
DELIMITER //
CREATE OR REPLACE PROCEDURE add_new_capture_relp(meta_tag varchar(48), retention_time varchar(255),
                                                 meta_category varchar(48), application varchar(48),
                                                 capture_index varchar(48), source_type varchar(255),
                                                 app_protoc varchar(64), flow varchar(255))
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;
    START TRANSACTION;


    if (select id from cfe_18.captureSourcetype where captureSourceType = source_type) is null then
        insert into cfe_18.captureSourcetype(captureSourceType)
        values (source_type);
        select last_insert_id() into @SourceId;
    else
        select id into @SourceId from cfe_18.captureSourcetype where captureSourceType = source_type;
    end if;

    if (select id from cfe_18.captureIndex where captureIndex = capture_index) is null then
        insert into cfe_18.captureIndex(captureIndex)
        values (capture_index);
        select last_insert_id() into @IndexId;
    else
        select id into @IndexId from cfe_18.captureIndex where captureIndex = capture_index;
    end if;


    if (select id from cfe_18.application where app = application) is null then
        insert into cfe_18.application(app)
        values (application);
        select last_insert_id() into @ApplicationId;
    else
        select id into @ApplicationId from cfe_18.application where app = application;
    end if;

    if (select id from cfe_18.retentionTime where retention = retention_time) is null then
        insert into cfe_18.retentionTime(retention)
        values (retention_time);
        select last_insert_id() into @RetentionId;
    else
        select id into @RetentionId from cfe_18.retentionTime where retention = retention_time;
    end if;

    if (select id from cfe_18.category where category = meta_category) is null then
        insert into cfe_18.category(category)
        values (meta_category);
        select last_insert_id() into @CategoryId;
    else
        select id into @CategoryId from cfe_18.category where category = meta_category;
    end if;

    if (select id from cfe_18.tags where tag = meta_tag) is null then
        insert into cfe_18.tags(tag)
        values (meta_tag);
        select last_insert_id() into @TagId;
    else
        select id into @TagId from cfe_18.tags where tag = meta_tag;
    end if;


    if ((select count(id) from flow.L7 where app_protocol = app_protoc) = 0) then
        SELECT JSON_OBJECT('id', null, 'message', 'L7 is missing') into @L7;
        signal sqlstate '45000' set message_text = @L7;
    else
        select id into @L7Id from flow.L7 where app_protocol = app_protoc;
    end if;

    if ((select count(id) from flow.flows where name = flow) = 0) then
        SELECT JSON_OBJECT('id', null, 'message', 'Flow is missing') into @Flow;
        signal sqlstate '45000' set message_text = @Flow;
    else
        select id into @FlowId from flow.flows where name = flow;
    end if;

    if (select id
        from cfe_18.capture_definition
        where captureIndex_id = @IndexId
          and captureSourcetype_id = @SourceId
          and application_id = @ApplicationId
          and retentionTime_id = @RetentionId
          and category_id = @CategoryId
          and tag_id = @TagId
          and L7_id = @L7Id
          and flow_id = @FlowId
          and capture_type = 'relp') is not null then
        select id
        into @c_id
        from cfe_18.capture_definition
        where captureIndex_id = @IndexId
          and captureSourcetype_id = @SourceId
          and application_id = @ApplicationId
          and retentionTime_id = @RetentionId
          and category_id = @CategoryId
          and tag_id = @TagId
          and L7_id = @L7Id
          and flow_id = @FlowId
          and capture_type = 'relp';
        select @c_id as last;
    else
        insert into cfe_18.capture_type(capture_type) values ('relp');
        select last_insert_id() into @TypeId;
        insert into cfe_18.capture_definition(tag_id, application_id, captureIndex_id, retentionTime_id,
                                              captureSourcetype_id, category_id, capture_type, capture_type_id, L7_id,
                                              flow_id)
        values (@TagId, @ApplicationId, @IndexId, @RetentionId, @SourceId, @CategoryId, 'relp', @TypeId, @L7Id,
                @FlowId);
        select last_insert_id() into @c_id;
    end if;
    select @c_id as last;
    COMMIT;
END;
//
DELIMITER ;