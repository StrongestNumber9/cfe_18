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
USE cfe_03;
DELIMITER //
CREATE OR REPLACE PROCEDURE add_host_meta_data(p_arch varchar(255), p_flavor varchar(255), p_hostname varchar(255),
                                               p_host_id int, p_os varchar(255),
                                               p_release_ver varchar(255))
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;
    START TRANSACTION;
    if (select id from location.host where id = p_host_id) is null then
        SELECT JSON_OBJECT('id', p_host_id, 'message', 'Host does not exist') into @hid;
        signal sqlstate '45000' set message_text = @hid;
    end if;

    if (select id from cfe_03.os_type where os = p_os) is null then
        insert into cfe_03.os_type(os) values (p_os);
        select last_insert_id() into @Os_id;
    else
        select id into @Os_id from cfe_03.os_type where os = p_os;
    end if;

    if (select id from cfe_03.release_version where rel_ver = p_release_ver) is null then
        insert into cfe_03.release_version(rel_ver) values (p_release_ver);
        select last_insert_id() into @RelVer_id;
    else
        select id into @RelVer_id from cfe_03.release_version where rel_ver = p_release_ver;
    end if;

    if (select id from cfe_03.arch_type where arch = p_arch) is null then
        insert into cfe_03.arch_type(arch) values (p_arch);
        select last_insert_id() into @Arch_id;
    else
        select id into @Arch_id from cfe_03.arch_type where arch = p_arch;
    end if;

    if (select id from cfe_03.flavor_type where flavor = p_flavor) is null then
        insert into cfe_03.flavor_type(flavor) values (p_flavor);
        select last_insert_id() into @Flavor_id;
    else
        select id into @Flavor_id from cfe_03.flavor_type where flavor = p_flavor;
    end if;

    if (select id
        from cfe_03.host_meta
        where arch_id = @Arch_id
          and flavor_id = @Flavor_id
          and os_id = @Os_id
          and release_ver_id = @RelVer_id
          and hostname = p_hostname
          and host_id = p_host_id) is not null then
        select id
        into @host_meta_id
        from cfe_03.host_meta
        where arch_id = @Arch_id
          and flavor_id = @Flavor_id
          and os_id = @Os_id
          and release_ver_id = @RelVer_id
          and hostname = p_hostname
          and host_id = p_host_id;
    elseif (select count(id)
            from cfe_03.host_meta
            where host_id = p_host_id
              and (flavor_id != @Flavor_id
                or os_id != @Os_id
                or release_ver_id != @RelVer_id
                or hostname != p_hostname
                or arch_id != @Arch_id)) != 0 then
        SELECT JSON_OBJECT('id', p_host_id, 'message', 'Host meta already exists with different values') into @hmd;
        signal sqlstate '45000' set message_text = @hmd;
    else
        insert into cfe_03.host_meta(arch_id, flavor_id, os_id,
                                     release_ver_id, hostname, host_id)
        VALUES (@Arch_id, @Flavor_id, @Os_id, @RelVer_id, p_hostname, p_host_id);
        select last_insert_id() into @host_meta_id;
    end if;

    select @host_meta_id as last;
    COMMIT;
END;
//
DELIMITER ;
