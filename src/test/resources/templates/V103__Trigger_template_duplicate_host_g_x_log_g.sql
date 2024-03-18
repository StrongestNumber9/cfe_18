use cfe_18;
/*
 This file works as an example of idea behind g_x_g triggers.


    Template for building trigger on host_groups_x_capture_groups
    1.  Gather hosts from the host_group which is being added.
    2.  Gather all the host_groups which those hosts are included in.
    3.  Gather all the capture_groups which are linked to host_groups mentioned before.
    4.  Gather all the tags included in capture_groups which we collected.
    5.  Creates unique check on tags. On collision trigger will stop the host_group and capture_group linkage.
 */

#### Before insert into host_groups_x_capture_groups

-- Check all the hosts that are inserted with the new.host_group
CREATE OR REPLACE TEMPORARY TABLE __hosts_inside_new_group AS
select hgxh.host_id, hgxh.host_group_id
from location.host_group_x_host hgxh
where hgxh.host_group_id = 4;

-- debug
select *
from __hosts_inside_new_group;

-- Check which host_groups have the existing hosts
CREATE OR REPLACE TEMPORARY TABLE __host_groups_from_gathered_hosts AS
select distinct hgxh.host_group_id
from location.host_group_x_host hgxh
         INNER JOIN __hosts_inside_new_group hing on hgxh.host_id = hing.host_id;


-- debug
select *
from __host_groups_from_gathered_hosts;


-- Select capture_groups which are linked to the given hosts
CREATE OR REPLACE TEMPORARY TABLE __capture_groups_attached_to_host_groups AS
select distinct hgxldg.capture_group_id
from cfe_18.host_groups_x_capture_def_group hgxldg
         INNER JOIN __host_groups_from_gathered_hosts hgtai
                    on hgxldg.host_group_id = hgtai.host_group_id;

-- debug
select *
from __capture_groups_attached_to_host_groups;

-- Select all the tags that are linked to the capture_groups. Unique check will be conducted later with this information
CREATE OR REPLACE TEMPORARY TABLE __all_tags_according_to_capture_groups AS
select ldgxld.capture_def_group_id, ldgxld.tag_id
from cfe_18.capture_def_group_x_capture_def ldgxld
         INNER JOIN __capture_groups_attached_to_host_groups lgathg
                    on lgathg.capture_group_id = ldgxld.capture_def_group_id or ldgxld.capture_def_group_id = 6;

-- debug
select *
from __all_tags_according_to_capture_groups;
select if(count(DISTINCT atatcg.tag_id) = count(atatcg.tag_id), true, false)
from __all_tags_according_to_capture_groups atatcg;

-- Final correct check
select if(count(DISTINCT ldgxld.tag_id) = count(ldgxld.tag_id), true, false)
from cfe_18.capture_def_group_x_capture_def ldgxld
         INNER JOIN __capture_groups_attached_to_host_groups lgathg
                    on lgathg.capture_group_id = ldgxld.capture_def_group_id or ldgxld.capture_def_group_id = 5;

-- Final product
-- 4 and 6 are where the new.host_group_id and new.capture_def_group_id values go in the trigger
select if(count(DISTINCT ldgxld.tag_id) = count(ldgxld.tag_id), true, false)
from cfe_18.capture_def_group_x_capture_def ldgxld
         INNER JOIN (select distinct hgxldg.capture_group_id
                     from cfe_18.host_groups_x_capture_def_group hgxldg
                              INNER JOIN (select distinct hgxh.host_group_id
                                          from location.host_group_x_host hgxh
                                                   INNER JOIN (select hgxh.host_id, hgxh.host_group_id
                                                               from location.host_group_x_host hgxh
                                                               where hgxh.host_group_id = 4) hing
                                                              on hgxh.host_id = hing.host_id) hgtai
                                         on hgxldg.host_group_id = hgtai.host_group_id) lgathg
                    on lgathg.capture_group_id = ldgxld.capture_def_group_id or ldgxld.capture_def_group_id = 6;