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
use cfe_00;
DELIMITER //
CREATE OR REPLACE PROCEDURE remove_hub(proc_hub_id int)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;
    START TRANSACTION;
    if (select id from cfe_00.hubs where id = proc_hub_id) is null then
        SELECT JSON_OBJECT('id', null, 'message', 'Hub does not exist') into @h;
        signal sqlstate '45000' set message_text = @h;
    end if;


    select h.id
    into @hostid
    from location.host h
             inner join hubs h2 on h.id = h2.host_id
    where h2.id = proc_hub_id;


    select count(htc.host_id)
    into @rowcount
    from cfe_00.host_type_cfe htc
    where hub_id = proc_hub_id
      and host_id != @hostid;

    if (@rowcount > 0) then
        select count(htc2.host_id)
        into @hamount
        from cfe_00.host_type_cfe htc2
        where hub_id = proc_hub_id;
        SELECT JSON_OBJECT('amount', @hamount, 'message', 'Hosts use the given hub')
        into @ha;
        signal sqlstate '23000' set message_text = @ha;
    end if;

    delete from cfe_00.host_type_cfe where hub_id = proc_hub_id;
    delete from cfe_00.hubs where id = proc_hub_id;
    delete from location.host where id = @hostid;
    COMMIT;
END;

//
DELIMITER ;