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
CREATE OR REPLACE PROCEDURE add_storage(flow varchar(255), proc_storage_id int
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;
    START TRANSACTION;

    if (select id from flows where name = flow) is null then
        signal sqlstate '45000' set message_text = 'flow does not exist';
    else
        select id into @FlowId from flows where name = flow;
    end if;
    if (select id from storages where id = proc_storage_id) is null then
        signal sqlstate '45000' set message_text = 'Storage is not valid';
    end if;

    select cfe_type into @Storage_type from flow.storages where id = proc_storage_id;

    if (select id
        from flow.flow_targets
        where flow_id = @FlowId
          and storage_id = proc_storage_id
          and storage_type = @Storage_type) is null then

        insert into flow.flow_targets(flow_id, storage_id, storage_type)
        values (@FlowId, proc_storage_id, @Storage_type);
        select last_insert_id() as last;
    else
        select id as last
        from flow.flow_targets
        where flow_id = @FlowId
          and storage_id = proc_storage_id
          and storage_type = @Storage_type;

    end if;
    COMMIT;
END;
//
DELIMITER ;