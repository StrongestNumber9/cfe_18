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
CREATE OR REPLACE PROCEDURE add_g_x_g(proc_host_group_id int, proc_capture_group_id int)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;
    START TRANSACTION;
    if ((select distinct capture_type
         from cfe_18.capture_def_group_x_capture_def
         where capture_def_group_id = proc_capture_group_id) !=
        (select distinct host_type from location.host_group_x_host where host_group_id = proc_host_group_id)) then
        SELECT JSON_OBJECT('id', NULL, 'message', ' type mismatch between host group and capture group') into @gxg;
        signal sqlstate '45000' set message_text = @gxg;
    end if;
    --  Check if host group exists before junction
    IF (select distinct host_group_id
        from location.host_group_x_host
        where host_group_id = proc_host_group_id) IS NULL THEN
        SELECT JSON_OBJECT('id', NULL, 'message', ' HOST group does not exist') into @gxgh;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = @gxgh;
    END IF;
    --  Check if capture group exists before junction
    IF (select distinct capture_def_group_id
        from cfe_18.capture_def_group_x_capture_def
        where capture_def_group_id = proc_capture_group_id) IS NULL THEN
        SELECT JSON_OBJECT('id', NULL, 'message', ' CAPTURE group does not exist') into @gxgc;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = @gxgc;
    END IF;
    --  Insert into junction table
    if (select id
        from cfe_18.host_groups_x_capture_def_group
        where host_group_id = proc_host_group_id
          and capture_group_id = proc_capture_group_id) is null then
        INSERT INTO cfe_18.host_groups_x_capture_def_group(host_group_id, capture_group_id)
        VALUES (proc_host_group_id, proc_capture_group_id);

        select c.capture_def_group_name as capture_group_name,
               hg.groupName             as host_group_name,
               last_insert_id()         as last
        from cfe_18.host_groups_x_capture_def_group hgxcdg
                 inner join capture_def_group c on hgxcdg.capture_group_id = c.id
                 inner join location.host_group hg on hgxcdg.host_group_id = hg.id
        where hgxcdg.capture_group_id = c.id
          and hgxcdg.host_group_id = hg.id
          and hgxcdg.id = (select id
                           from cfe_18.host_groups_x_capture_def_group
                           where host_group_id = proc_host_group_id
                             and capture_group_id = proc_capture_group_id);

    else
        select c.capture_def_group_name as capture_group_name,
               hg.groupName             as host_group_name,
               hgxcdg.id                as last
        from cfe_18.host_groups_x_capture_def_group hgxcdg
                 inner join capture_def_group c on hgxcdg.capture_group_id = c.id
                 inner join location.host_group hg on hgxcdg.host_group_id = hg.id
        where hgxcdg.capture_group_id = c.id
          and hgxcdg.host_group_id = hg.id
          and hgxcdg.id = (select id
                           from cfe_18.host_groups_x_capture_def_group
                           where host_group_id = proc_host_group_id
                             and capture_group_id = proc_capture_group_id);
    end if;
    COMMIT;
END;

//
DELIMITER ;