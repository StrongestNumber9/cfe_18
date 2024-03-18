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
CREATE OR REPLACE PROCEDURE retrieve_capture_by_id(proc_id int)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;
    START TRANSACTION;
    if (select id from capture_definition where id = proc_id) is null then
        SELECT JSON_OBJECT('id', proc_id, 'message', 'Capture does not exist with the given ID') into @cid;
        signal sqlstate '45000' set message_text = @cid;
    end if;


    if (select capture_type from cfe_18.capture_definition where capture_type = 'cfe' and id = proc_id) is not null then
        select c.id                 as id,
               t.tag                as tag,
               a.app                as app,
               cI.captureIndex      as captureIndex,
               rT.retention         as retention_time,
               cS.captureSourceType as source_type,
               c2.category          as category,
               pt.type_name         as rule_name,
               f.name               as flow,
               L.app_protocol       as protocol,
               cmf.capturePath      as capture_path,
               cmf.tagPath          as tag_path,
               c.capture_type       as capture_type
        from cfe_18.capture_definition c
                 inner join tags t on t.id = c.tag_id
                 inner join application a on c.application_id = a.id
                 inner join captureIndex cI on c.captureIndex_id = cI.id
                 inner join retentionTime rT on c.retentionTime_id = rT.id
                 inner join captureSourcetype cS on c.captureSourcetype_id = cS.id
                 inner join category c2 on c.category_id = c2.id
                 inner join flow.flows f on c.flow_id = f.id
                 inner join flow.L7 L on c.L7_id = L.id
                 inner join capture_type ct on c.capture_type_id = ct.id
                 inner join capture_meta_file cmf on ct.id = cmf.id
                 inner join processing_type pt on cmf.processing_type_id = pt.id
        where c.id = proc_id
          and t.id = c.tag_id
          and a.id = c.application_id
          and cI.id = c.captureIndex_id
          and rT.id = c.retentionTime_id
          and cS.id = c.captureSourcetype_id
          and c2.id = c.category_id
          and f.id = c.flow_id
          and L.id = c.L7_id
          and ct.id = c.capture_type_id
          and cmf.id = ct.id
          and pt.id = cmf.processing_type_id;
    elseif (select capture_type
            from cfe_18.capture_definition
            where capture_type = 'relp'
              and id = proc_id) is not null then
        select c.id                 as id,
               t.tag                as tag,
               a.app                as app,
               cI.captureIndex      as captureIndex,
               rT.retention         as retention_time,
               cS.captureSourceType as source_type,
               c2.category          as category,
               f.name               as flow,
               L.app_protocol       as protocol,
               c.capture_type       as capture_type
        from cfe_18.capture_definition c
                 inner join tags t on t.id = c.tag_id
                 inner join application a on c.application_id = a.id
                 inner join captureIndex cI on c.captureIndex_id = cI.id
                 inner join retentionTime rT on c.retentionTime_id = rT.id
                 inner join captureSourcetype cS on c.captureSourcetype_id = cS.id
                 inner join category c2 on c.category_id = c2.id
                 inner join flow.flows f on c.flow_id = f.id
                 inner join flow.L7 L on c.L7_id = L.id
                 inner join capture_type ct on c.capture_type_id = ct.id
        where c.id = proc_id
          and t.id = c.tag_id
          and a.id = c.application_id
          and cI.id = c.captureIndex_id
          and rT.id = c.retentionTime_id
          and cS.id = c.captureSourcetype_id
          and c2.id = c.category_id
          and f.id = c.flow_id
          and L.id = c.L7_id
          and ct.id = c.capture_type_id;
    end if;
    COMMIT;
END;
//
DELIMITER ;