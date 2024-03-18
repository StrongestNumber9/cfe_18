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
create trigger if not exists cant_add_existing_tag_into_group
    before insert
    on cfe_18.capture_def_group_x_capture_def
    for each row
begin
    declare truthvalue int;
    select if(count(DISTINCT hgxh.host_id) = count(hgxh.host_id), true, false)
    into truthvalue
    from location.host_group_x_host hgxh
             INNER JOIN (SELECT DISTINCT hgxldg.host_group_id
                         from host_groups_x_capture_def_group hgxldg
                                  INNER JOIN (SELECT ldgxld.tag_id, ldgxld.capture_def_group_id
                                              from capture_def_group_x_capture_def ldgxld
                                                       INNER JOIN (select tag_id, id
                                                                   from capture_def_group_x_capture_def tagsid) ttdwst
                                                                  ON ldgxld.id = ttdwst.id
                                              where ldgxld.capture_def_group_id = new.capture_def_group_id
                                                 or ldgxld.tag_id = new.tag_id) ttdwcg
                                             ON ttdwcg.capture_def_group_id = hgxldg.capture_group_id) tthwcd
                        ON hgxh.host_group_id = tthwcd.host_group_id;
    if truthvalue = 0 then
        signal sqlstate '17001' set message_text = 'DUPLICATE TAG ERROR';
    end if;

end//



