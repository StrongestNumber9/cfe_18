use cfe_18;

/*
 This is a template for cant_add_existing_tag_into_group in before_insert_triggers.sql file

 Workflow
    1. get all tags
    2. link tags according to a capture_group
    3. compare capture_groups against host_groups
    4. check all hosts in that group to see if they exist already on said tag
 */

-- valitaan kaikki tagit pöytään
CREATE OR REPLACE TEMPORARY TABLE __tags_from_capture_def_id AS
select tag_id, id
from capture_def_group_x_capture_def tagsid;

-- check all tags
select *
from __tags_from_capture_def_id;

# Tähän new kun linkitetään capture_def uuteen grouppiin.
-- selectataan että mitä tageja groupissa on kiinni
CREATE OR REPLACE TEMPORARY TABLE __select_unique_tag_value AS
SELECT ldgxld.tag_id, ldgxld.capture_def_group_id
from capture_def_group_x_capture_def ldgxld
         INNER JOIN __tags_from_capture_def_id ttdwst ON ldgxld.id = ttdwst.id
where ldgxld.capture_def_group_id = 3 or ldgxld.tag_id=1;

-- debug / katsotaan mitkä capture_defit on jo kyseisessä groupissa / capture_def=tag tässä tilanteessa
select *
from __select_unique_tag_value;


-- valitaan host_group ryhmät joihin ylemmät capture_def ryhmät kuuluvat
CREATE OR REPLACE TEMPORARY TABLE __tigers_temple_hostgs_w_common_defgs AS
SELECT DISTINCT hgxldg.host_group_id
from host_groups_x_capture_def_group hgxldg
         INNER JOIN __select_unique_tag_value ttdwcg
                    ON ttdwcg.capture_def_group_id = hgxldg.capture_group_id;

-- debug
SELECT *
FROM __tigers_temple_hostgs_w_common_defgs;

-- valitaan hostit host_group ryhmistä joihihin capture_def ryhmät olivat liitetty -- NOTE EI DISTINCT
CREATE OR REPLACE TEMPORARY TABLE __tigers_temple_hosts AS
select hgxh.host_id
from location.host_group_x_host hgxh
         INNER JOIN __tigers_temple_hostgs_w_common_defgs tthwcd
                    ON hgxh.host_group_id = tthwcd.host_group_id;

SELECT *
from __tigers_temple_hosts;

-- tarkistetaan hostit
#CREATE OR REPLACE UNIQUE INDEX __tigers_temple_hosts_uix_host_id ON __tigers_temple_hosts (host_id);


-- TODO apply this if condition to check if there are multiple hosts in the tag that is being added
select if(count(DISTINCT hgxh.host_id) = count(hgxh.host_id), true, false)
from location.host_group_x_host hgxh
         INNER JOIN __tigers_temple_hostgs_w_common_defgs tthwcd
                    ON hgxh.host_group_id = tthwcd.host_group_id;

-- if statement above = 1 is good, 0 is bad. Meaning 1 is true and 0 is false

-- debug
#select count(distinct test.host_id) from __tigers_temple_hosts test;
#select count(test.host_id) from __tigers_temple_hosts test;



