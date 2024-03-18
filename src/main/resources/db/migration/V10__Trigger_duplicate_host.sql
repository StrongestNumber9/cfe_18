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
create trigger if not exists Host_cant_have_duplicate_tag
    before insert
    on location.host_group_x_host
    for each row
begin
    declare truthvalue int;
    select if(count(DISTINCT ldgxld.tag_id) = count(ldgxld.tag_id), true, false)
    into truthvalue
    from cfe_18.capture_def_group_x_capture_def ldgxld
             INNER JOIN (select distinct hgxh.capture_group_id, hgxh.host_group_id
                         from cfe_18.host_groups_x_capture_def_group hgxh
                                  INNER JOIN (select distinct hgxh.host_group_id, hgxh.host_id
                                              from host_group_x_host hgxh
                                                       INNER JOIN (select hid.host_id
                                                                   from host_group_x_host hid) ch
                                                                  on hgxh.host_group_id
                                              where hgxh.host_id = new.host_id
                                                 or hgxh.host_group_id = new.host_group_id) hchdt
                                             on hgxh.capture_group_id = hchdt.host_group_id) ctftlg
                        on ldgxld.capture_def_group_id = ctftlg.capture_group_id;
    if truthvalue = 0 then
        signal sqlstate '17002' set message_text = 'DUPLICATE HOST ERROR';
    end if;
end //

