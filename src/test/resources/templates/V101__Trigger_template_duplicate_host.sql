use location;

/*
 This file is for trigger template "host_cant_have_duplicate_tag"

 When adding host it checks host_x_host_group table if said host already exists in another group AND it has the same tag.
 */

/*
 This file works as an example of idea behind g_x_g triggers.

 Idea = In the trigger, before inserting host to a host_group it will first check which capture groups are linked.

 If host_group is not attached to any capture_groups then it won't trigger no matter what.

 If host is attached to a host_group with different capture_group BUT with same capture. Then host matches with existing capture but only through different grouping.
 */

-- Gather hosts which are already linked to host_group
CREATE OR REPLACE TEMPORARY TABLE __check_hosts AS
select hid.host_id
from host_group_x_host hid;

-- debug
select *
from __check_hosts;
-- Generates groups where host_id already exists
CREATE OR REPLACE TEMPORARY TABLE __host_cant_have_duplicate_tag AS
select distinct hgxh.host_group_id, hgxh.host_id
from host_group_x_host hgxh
         INNER JOIN __check_hosts ch on hgxh.host_group_id
where hgxh.host_id = 11
   or hgxh.host_group_id = 1;

-- debug
select *
from __host_cant_have_duplicate_tag;

-- Gather capture_groups which already have the existing host_groups
CREATE OR REPLACE TEMPORARY TABLE test AS
select distinct hgxh.capture_group_id, hgxh.host_group_id
from cfe_18.host_groups_x_capture_def_group hgxh
         INNER JOIN __host_cant_have_duplicate_tag hchdt on hgxh.capture_group_id = hchdt.host_group_id;

-- debug
select *
from test;

-- Check whether tag_id already exists in any of the capture_groups
CREATE OR REPLACE TEMPORARY TABLE __check_if_host_already_exists AS
select hgxh.tag_id
from cfe_18.capture_def_group_x_capture_def hgxh
         INNER JOIN test hchdt on hgxh.capture_def_group_id = hchdt.capture_group_id;

-- debug
select *
from __check_if_host_already_exists;

-- Returns true or false depending if tag_id exists in the previous capture_groups or in the new one.
select if(count(DISTINCT ldgxld.tag_id) = count(ldgxld.tag_id), true, false)
from cfe_18.capture_def_group_x_capture_def ldgxld
         INNER JOIN test ctftlg on ldgxld.capture_def_group_id = ctftlg.capture_group_id;






