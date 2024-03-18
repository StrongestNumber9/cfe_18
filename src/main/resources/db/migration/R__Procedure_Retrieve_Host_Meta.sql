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
DELIMITER //
CREATE OR REPLACE PROCEDURE retrieve_host_meta(proc_host_meta_id int)
BEGIN

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;
    START TRANSACTION;
    if (select id from host_meta where id = proc_host_meta_id) is null
    then
        SELECT JSON_OBJECT('id', proc_host_meta_id, 'message', 'Host metadata does not exist for the given ID')
        into @hmd;
        signal sqlstate '45000' set message_text = @hmd;
    end if;

    if (((select count(*) from cfe_03.host_meta_x_ip where host_meta_id = proc_host_meta_id) and
         (select count(*) from cfe_03.host_meta_x_interface where host_meta_id = proc_host_meta_id)) = 0) then
        SELECT JSON_OBJECT('id', proc_host_meta_id, 'message', 'IP and/or INTERFACE is missing for given host_meta_id')
        into @ipihm;
        signal sqlstate '45100' set message_text = @ipihm;
    end if;

    select hm.id         as host_meta_id,
           a.arch        as arch,
           rv.rel_ver    as release_version,
           ft.flavor     as flavor,
           ot.os         as os,
           i.interface   as interface,
           ia.ip_address as ip_address,
           hm.hostname   as hostname,
           hm.host_id    as host_id
    from host_meta hm
             inner join arch_type a on hm.arch_id = a.id
             inner join release_version rv on hm.release_ver_id = rv.id
             inner join flavor_type ft on hm.flavor_id = ft.id
             inner join os_type ot on hm.os_id = ot.id
             inner join host_meta_x_interface hmxi on hm.id = hmxi.host_meta_id
             inner join host_meta_x_ip h on hm.id = h.host_meta_id
             inner join interfaces i on hmxi.interface_id = i.id
             inner join ip_addresses ia on h.ip_id = ia.id
    where hm.id = proc_host_meta_id
      and a.id = hm.arch_id
      and rv.id = hm.release_ver_id
      and ft.id = hm.flavor_id
      and ot.id = hm.os_id
      and hmxi.host_meta_id = hm.id
      and h.host_meta_id = hm.id;
    COMMIT;

END;