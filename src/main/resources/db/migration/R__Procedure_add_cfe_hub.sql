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
CREATE OR REPLACE PROCEDURE host_add_cfe_hub(proc_fqhost varchar(128), proc_md5 varchar(32),
                                             proc_ip varchar(255))
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        end;
    start transaction;
    if (select id
        from location.host
        where MD5 = proc_md5
          and fqhost = proc_fqhost
          and host_type = 'cfe') is null then
        insert into location.host(MD5, fqhost, host_type)
        values (proc_md5, proc_fqhost, 'cfe');
        select last_insert_id() into @hid;
    else
        select id into @hid from location.host where MD5 = proc_md5 and fqhost = proc_fqhost and host_type = 'cfe';
    end if;

    if (select host_id
        from cfe_00.hubs
        where host_id = @hid
          and ip = proc_ip
          and host_type = 'cfe') is null then
        insert into cfe_00.hubs(host_id, ip, host_type)
        values (@hid, proc_ip, 'cfe');
        select last_insert_id() into @id;
    else
        select id into @id from cfe_00.hubs where host_id = @hid and ip = proc_ip and host_type = 'cfe';
    end if;

    if (select host_id
        from cfe_00.host_type_cfe
        where host_id = @hid
          and host_type = 'cfe'
          and hub_id = @id) is null then
        insert into cfe_00.host_type_cfe(host_id, host_type, hub_id)
        values (@hid, 'cfe', @id);
    end if;
    COMMIT;
    select @id as last;

END;
//
DELIMITER ;