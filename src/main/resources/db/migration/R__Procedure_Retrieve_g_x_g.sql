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
CREATE OR REPLACE PROCEDURE retrieve_g_x_g_details(grp_name varchar(255))
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;
    START TRANSACTION;

    if ((select capture_def_group_name from cfe_18.capture_def_group where capture_def_group_name = grp_name) and
        (select groupName from location.host_group where groupName = grp_name)) is null then
        SELECT JSON_OBJECT('id', NULL, 'message', 'group does not exist with the given name') into @gxg;
        signal sqlstate '45000' set message_text = @gxg;
    end if;

    -- return resultset accordingly
    select distinct hgxcdg.id                  as g_x_g_id,
                    cdg.capture_def_group_name as capture_name,
                    hg.groupName               as host_name,
                    hgxh.host_type             as host_type,
                    cdgxcd.capture_type        as capture_type,
                    hg.id                      as host_group_id,
                    cdg.id                     as capture_group_id
    from host_groups_x_capture_def_group hgxcdg
             inner join capture_def_group cdg on hgxcdg.capture_group_id = cdg.id
             inner join location.host_group hg on hgxcdg.host_group_id = hg.id
             inner join capture_def_group_x_capture_def cdgxcd on cdg.id = cdgxcd.capture_def_group_id
             inner join location.host_group_x_host hgxh on hg.id = hgxh.host_group_id
    where hg.groupName = grp_name
       or cdg.capture_def_group_name = grp_name;
    COMMIT;
END;
//
DELIMITER ;