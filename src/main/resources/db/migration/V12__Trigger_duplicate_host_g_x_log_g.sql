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
create trigger if not exists junction_table_collide_stopper
    before insert
    on cfe_18.host_groups_x_capture_def_group
    for each row
begin
    declare dup int;
    select if(count(DISTINCT ldgxld.tag_id) = count(ldgxld.tag_id), true, false)
    into dup
    from cfe_18.capture_def_group_x_capture_def ldgxld
             INNER JOIN (select distinct hgxldg.capture_group_id
                         from cfe_18.host_groups_x_capture_def_group hgxldg
                                  INNER JOIN (select distinct hgxh.host_group_id
                                              from location.host_group_x_host hgxh
                                                       INNER JOIN (select hgxh.host_id, hgxh.host_group_id
                                                                   from location.host_group_x_host hgxh
                                                                   where hgxh.host_group_id = new.host_group_id) hing
                                                                  on hgxh.host_id = hing.host_id) hgtai
                                             on hgxldg.host_group_id = hgtai.host_group_id) lgathg
                        on lgathg.capture_group_id = ldgxld.capture_def_group_id or
                           ldgxld.capture_def_group_id = new.capture_group_id;
    if dup = 0 then
        signal sqlstate '17005'
            set message_text = 'duplicate found when combining groups';
    end if;
end
//
