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
CREATE OR REPLACE PROCEDURE retrieve_capture_group_details(grp_name varchar(255))
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;
    START TRANSACTION;
    if (select capture_def_group.capture_def_group_name
        from capture_def_group
        where capture_def_group.capture_def_group_name = grp_name) is null then
        SELECT JSON_OBJECT('id', NULL, 'message', 'capture group not found') into @gc;
        signal sqlstate '45000' set message_text = @gc;
    end if;


    select cdg.capture_def_group_name as group_name,
           cd.id                      as capture_definition_id,
           cdgxcd.capture_type        as capture_type,
           cdg.id                     as capture_group_id
    from cfe_18.capture_def_group cdg
             inner join capture_def_group_x_capture_def cdgxcd on cdg.id = cdgxcd.capture_def_group_id
             inner join capture_definition cd on cdgxcd.capture_def_id = cd.id and cdgxcd.tag_id = cd.tag_id
    where capture_def_group_name = grp_name
      and cd.id = cdgxcd.capture_def_id
      and cd.tag_id = cdgxcd.tag_id;
    COMMIT;
END;
//
DELIMITER ;