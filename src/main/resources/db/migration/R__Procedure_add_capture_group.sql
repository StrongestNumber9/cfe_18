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
USE cfe_18;
DELIMITER //
CREATE OR REPLACE PROCEDURE add_capture_group_with_capture(group_name varchar(255), capture_id int)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;
    START TRANSACTION;
    if (select id from cfe_18.capture_definition where id = capture_id) is null then
        signal sqlstate '45000' set message_text = 'Capture does not exist';
    end if;

    -- Gathering type from capture is required
    select capture_type into @type from capture_definition where id = capture_id;
    -- check if capture_group exists. If not then create one with that name.
    if (select capture_def_group_name from cfe_18.capture_def_group where capture_def_group_name = group_name) is null
    then
        insert into cfe_18.capture_def_group(capture_def_group_name, capture_type)
        values (group_name, @type);
        select last_insert_id() into @GroupId;
    else
        select id into @GroupId from cfe_18.capture_def_group where capture_def_group_name = group_name;
    end if;

    -- Select tag_id for the capture_definition. Null value is fine.
    select tag_id into @TagId from cfe_18.capture_definition where id = capture_id;

    -- insert final value into junction table. Will give out constraint errors automatically if there is record that exists already.
    -- If the tag is conflicting with Host. Before insert trigger gives out error code of = 17001

    -- duplicate check before linkage
    if (select id
        from cfe_18.capture_def_group_x_capture_def
        where capture_def_id = capture_id
          and capture_def_group_id = @GroupId
          and tag_id = @TagId
          and capture_type = @type) is null then
        insert into cfe_18.capture_def_group_x_capture_def(capture_def_id, capture_def_group_id, tag_id, capture_type)
        values (capture_id, @GroupId, @TagId, @type);
    end if;
    select group_name as name, @GroupId as last;
    COMMIT;

END;
//
DELIMITER ;