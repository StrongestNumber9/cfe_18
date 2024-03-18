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
USE cfe_00;
DELIMITER //
CREATE OR REPLACE PROCEDURE retrieve_host_details(proc_host_id int)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;
    START TRANSACTION;
    if (select id from location.host where id = proc_host_id) is null then
        SELECT JSON_OBJECT('id', proc_host_id, 'message', 'Host does not exist with the given ID') into @hid;
        signal sqlstate '45000' set message_text = @hid;
    end if;

    if (select h.id
        from location.host h
                 inner join hubs h3 on h.id = h3.host_id
        where h.id = proc_host_id) is not null then
        SELECT JSON_OBJECT('id', proc_host_id, 'message', 'Host given is hub') into @hub;
        signal sqlstate '45100' set message_text = @hub;
    elseif (select id from location.host where id = proc_host_id and host_type = 'cfe') then

        select h.id          as host_id,
               h.md5         as host_md5,
               h.fqhost      as host_fq,
               htc.host_type as host_type,
               h2.id         as hub_id,
               hm.hostname   as host_name,
               hm.id         as host_meta_id,
               h3.fqhost     as hub_fq
        from location.host h
                 inner join host_type_cfe htc on h.id = htc.host_id
                 inner join hubs h2 on htc.hub_id = h2.id
                 inner join cfe_03.host_meta hm on h.id = hm.host_id
                 inner join location.host h3 on h2.host_id = h3.id
        where h.id = proc_host_id
          and hm.host_id = proc_host_id
          and h3.id = h2.host_id;
    elseif (select id from location.host where id = proc_host_id and host_type = 'relp') then
        select id        as host_id,
               md5       as md5,
               fqhost    as fqhost,
               host_type as host_type
        from location.host
        where id = proc_host_id;
    end if;
    COMMIT;
END;
//
DELIMITER ;