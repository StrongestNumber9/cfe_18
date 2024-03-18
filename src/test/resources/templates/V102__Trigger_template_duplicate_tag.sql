use cfe_18;

/*
  This file works as an example of idea behind g_x_g triggers.

 This is a template for cant_add_existing_tag_into_group in before_insert_triggers.sql file

 Workflow
    1. get all tags
    2. link tags according to a capture_group
    3. compare capture_groups against host_groups
    4. check all hosts in that group to see if they exist already on said tag
 */

-- Select all tags into temporary table
CREATE OR REPLACE TEMPORARY TABLE __tags_from_capture_def_id AS
select tag_id, id
from capture_def_group_x_capture_def tagsid;

-- check all tags
select *
from __tags_from_capture_def_id;

# Here new.capture_group_id and new.tag_id declaration when inside trigger
-- Check tags and capture groups with the given tag_id and capture_group_id
CREATE OR REPLACE TEMPORARY TABLE __select_unique_tag_value AS
SELECT ldgxld.tag_id, ldgxld.capture_def_group_id
from capture_def_group_x_capture_def ldgxld
         INNER JOIN __tags_from_capture_def_id ttdwst ON ldgxld.id = ttdwst.id
where ldgxld.capture_def_group_id = 3
   or ldgxld.tag_id = 1;

-- debug
select *
from __select_unique_tag_value;


-- Select host_groups which are already linked to capture_groups
CREATE OR REPLACE TEMPORARY TABLE __tigers_temple_hostgs_w_common_defgs AS
SELECT DISTINCT hgxldg.host_group_id
from host_groups_x_capture_def_group hgxldg
         INNER JOIN __select_unique_tag_value ttdwcg
                    ON ttdwcg.capture_def_group_id = hgxldg.capture_group_id;

-- debug
SELECT *
FROM __tigers_temple_hostgs_w_common_defgs;

-- Select hosts from host_groups which are linked to capture_groups - NOT DISTINCT here
CREATE OR REPLACE TEMPORARY TABLE __tigers_temple_hosts AS
select hgxh.host_id
from location.host_group_x_host hgxh
         INNER JOIN __tigers_temple_hostgs_w_common_defgs tthwcd
                    ON hgxh.host_group_id = tthwcd.host_group_id;

-- Debug
SELECT *
from __tigers_temple_hosts;

-- Can be used to check hosts
#CREATE OR REPLACE UNIQUE INDEX __tigers_temple_hosts_uix_host_id ON __tigers_temple_hosts (host_id);


-- TODO apply this if condition to check if there are multiple hosts in the tag that is being added
select if(count(DISTINCT hgxh.host_id) = count(hgxh.host_id), true, false)
from location.host_group_x_host hgxh
         INNER JOIN __tigers_temple_hostgs_w_common_defgs tthwcd
                    ON hgxh.host_group_id = tthwcd.host_group_id;

-- if statement above = 1 is good, 0 is bad. Meaning 1 is true and 0 is false



