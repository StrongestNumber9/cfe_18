
use location;

/*
 This file is for trigger template "host_cant_have_duplicate_tag"

 When adding host it checks host_x_host_group table if said host already exists in another group AND it has the same tag.

 NOTE!!! Since template owns union, it's important to remember both host_group_x_host and host_groups_x_capture_def_group
 require same amount of columns
 */

/*
 Idea = before insertissä ennenkun yrittää pökätä hostia host_grouppiin kiinni niin se käy ensin
 tarkistamassa mitä capture_grouppeja siinä on kiinni.

 jos host_group ei ole vielä missään kiinni niin se antaa myöden eikä pompauta.

 capture_groupissa on tag_id unique kiinni ts. jos hosti on aiemmassa host_groupissa kiinni missä on sama capture_group niin se ei kelpaa.
 Tuossa kohtaa sama hosti saa saman tag_id mikä ei kelpaa vaikka onkin eri host_group.
 */

-- Kerätään hostit mitkä on jo host_groupeissa kiinni
CREATE OR REPLACE TEMPORARY TABLE __check_hosts AS
select hid.host_id
from host_group_x_host hid;

-- debug
select *
from __check_hosts;
-- tuottaa groupit missä host_id on jo
# NOTE new value triggerissä tähän. host_id on hosti mikä lisätään ja host_group_id on grouppi mihin halutaan lisätä.
CREATE OR REPLACE TEMPORARY TABLE __host_cant_have_duplicate_tag AS
select distinct hgxh.host_group_id,hgxh.host_id
from host_group_x_host hgxh
         INNER JOIN __check_hosts ch on hgxh.host_group_id
where hgxh.host_id = 11 or hgxh.host_group_id=1;

-- debug
select *
from __host_cant_have_duplicate_tag;

-- kerätään capture_groupit missä aiemmat host_groupit on jo
CREATE OR REPLACE TEMPORARY TABLE test AS
select distinct hgxh.capture_group_id, hgxh.host_group_id
from cfe_18.host_groups_x_capture_def_group hgxh
         INNER JOIN __host_cant_have_duplicate_tag hchdt on hgxh.capture_group_id = hchdt.host_group_id;

-- debug
select *
from test;

-- tarkistetaan onko tag_id duplikaattina missään capture_groupeissa
CREATE OR REPLACE TEMPORARY TABLE __check_if_host_already_exists AS
select hgxh.tag_id
from cfe_18.capture_def_group_x_capture_def hgxh
         INNER JOIN test hchdt on hgxh.capture_def_group_id = hchdt.capture_group_id;

-- debug
select *
from __check_if_host_already_exists;

-- Palauttaa true tai false riippuen onko tag_id uudessa tai aiemmin olevissa groupeissa jo valmiiksi
select if(count(DISTINCT ldgxld.tag_id) = count(ldgxld.tag_id), true, false)
from cfe_18.capture_def_group_x_capture_def ldgxld
         INNER JOIN test ctftlg on ldgxld.capture_def_group_id = ctftlg.capture_group_id;






