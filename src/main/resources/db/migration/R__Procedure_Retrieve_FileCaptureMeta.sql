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
CREATE OR REPLACE PROCEDURE retrieve_processing_type_by_name(proc_name varchar(255))
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;
    START TRANSACTION;

    if (select id from cfe_18.processing_type where type_name = proc_name) is null then
        SELECT JSON_OBJECT('id', NULL, 'message', 'Processing type does not exist') into @pt;
        signal sqlstate '45000' set message_text = @pt;
    end if;


    if (select i3.inputtype
        from processing_type pt
                 inner join inputtype i3 on pt.inputtype_id = i3.id
        where type_name = proc_name) = 'regex' then
        select pt.id        as id,
               t2.template  as template,
               r2.rule      as ruleset,
               pt.type_name as name,
               i2.inputtype as inputtype,
               r3.regex     as inputvalue
        from processing_type pt
                 inner join inputtype i2 on pt.inputtype_id = i2.id
                 inner join templates t2 on pt.template_id = t2.id
                 inner join ruleset r2 on pt.ruleset_id = r2.id
                 inner join regex r3 on i2.id = r3.id and i2.inputtype = r3.inputtype
        where type_name = proc_name
          and i2.id = pt.inputtype_id
          and t2.id = pt.template_id
          and r2.id = pt.ruleset_id
          and r3.id = pt.inputtype_id;
    end if;

    if (select i3.inputtype
        from processing_type pt
                 inner join inputtype i3 on pt.inputtype_id = i3.id
        where type_name = proc_name) = 'newline' then
        select pt.id        as id,
               i2.inputtype as inputtype,
               t2.template  as template,
               r2.rule      as ruleset,
               n2.newline   as inputvalue,
               pt.type_name as name
        from processing_type pt
                 inner join inputtype i2 on pt.inputtype_id = i2.id
                 inner join templates t2 on pt.template_id = t2.id
                 inner join ruleset r2 on pt.ruleset_id = r2.id
                 inner join newline n2 on i2.id = n2.id and i2.inputtype = n2.inputtype
        where type_name = proc_name
          and i2.id = pt.inputtype_id
          and t2.id = pt.template_id
          and r2.id = pt.ruleset_id
          and n2.id = pt.inputtype_id;
    end if;
    COMMIT;
END;
//
DELIMITER ;