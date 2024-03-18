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
DELIMITER //
CREATE OR REPLACE PROCEDURE add_host_group_with_host(proc_host_id int, host_group varchar(255))

BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;
    START TRANSACTION;

    if (select id from location.host where id = proc_host_id) is null then
        SELECT JSON_OBJECT('id', NULL, 'message', 'host does not exist') into @host;
        signal sqlstate '45000' set message_text = @host;
    end if;

    select host_type into @type from location.host where id = proc_host_id;


    -- check if the group exists. If not then create said group
    if (select groupName from location.host_group where groupName = host_group) is null then
        insert into location.host_group(groupName, host_type) values (host_group, @type);
        select last_insert_id() into @GroupId;
    else
        select id into @GroupId from location.host_group where groupName = host_group;
    end if;

    if (select id
        from host_group_x_host
        where host_group_id = @GroupId
          and host_id = proc_host_id
          and host_type = @type) is null then
        insert into location.host_group_x_host(host_group_id, host_id, host_type)
        values (@GroupId, proc_host_id, @type);
    end if;
    select host_group as name, @GroupId as last;
    COMMIT;
END;
//
DELIMITER ;
