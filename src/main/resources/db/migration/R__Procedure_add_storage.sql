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
use flow;
DELIMITER //
CREATE OR REPLACE PROCEDURE add_storage_alone(proc_cfe_type varchar(6), proc_storage_name varchar(255))
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        end;
    START TRANSACTION;
    if (select id
        from flow.storages
        where cfe_type = proc_cfe_type
          and storage_name = proc_storage_name) is not null then
        select id as last from flow.storages where cfe_type = proc_cfe_type and storage_name = proc_storage_name;
    else
        if (proc_cfe_type = 'cfe_04') then
            insert into flow.storages(cfe_type, storage_name) values (proc_cfe_type, proc_storage_name);
            select last_insert_id() into @id;
            insert into flow.cfe_04 values (@id, 'cfe_04');
        elseif (proc_cfe_type = 'cfe_10') then
            insert into flow.storages(cfe_type, storage_name) values (proc_cfe_type, proc_storage_name);
            select last_insert_id() into @id;
            insert into flow.cfe_10 values (@id, 'cfe_10', 'spool');
        elseif (proc_cfe_type = 'cfe_11') then
            insert into flow.storages(cfe_type, storage_name) values (proc_cfe_type, proc_storage_name);
            select last_insert_id() into @id;
            insert into flow.cfe_11 values (@id, 'cfe_11', 'inspection');
        elseif (proc_cfe_type = 'cfe_12') then
            insert into flow.storages(cfe_type, storage_name) values (proc_cfe_type, proc_storage_name);
            select last_insert_id() into @id;
            insert into flow.cfe_12 values (@id, 'cfe_12');
        elseif (proc_cfe_type = 'cfe_19') then
            insert into flow.storages(cfe_type, storage_name) values (proc_cfe_type, proc_storage_name);
            select last_insert_id() into @id;
            insert into flow.cfe_19 values (@id, 'cfe_19');
        elseif (proc_cfe_type = 'cfe_23') then
            insert into flow.storages(cfe_type, storage_name) values (proc_cfe_type, proc_storage_name);
            select last_insert_id() into @id;
            insert into flow.cfe_23 values (@id, 'cfe_23');
        else
            signal sqlstate '42000' set message_text = 'Storage type is not valid';
        end if;
        select @id as last;

    end if;
    COMMIT;
end;
//
delimiter ;
