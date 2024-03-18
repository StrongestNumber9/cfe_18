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
package com.teragrep.cfe18.testData;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

// NB! NOT UP TO DATE ANYMORE. LACKS SCHEMA CHANGES.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TriggerDataTest extends DBInformation {

    @Test
    @ResourceLock(value = "conn")
    @EnabledIfSystemProperty(named = "TriggerDataTest.generateData", matches = "true")
    public void generateData() throws Exception {

        initialize(conn);
        IDatabaseConnection connection = new DatabaseConnection(conn);
        // partial database export
        QueryDataSet partialDataSet = new QueryDataSet(connection);
        // Flow data
        partialDataSet.addTable("flow.flows");
        partialDataSet.addTable("flow.L7");
        partialDataSet.addTable("flow.capture_sink");

        partialDataSet.addTable("cfe_18.ruleset");
        partialDataSet.addTable("cfe_18.templates");
        partialDataSet.addTable("cfe_18.inputtype");
        partialDataSet.addTable("cfe_18.newline");
        partialDataSet.addTable("cfe_18.regex");
        partialDataSet.addTable("cfe_18.processing_type");
        partialDataSet.addTable("cfe_18.tags");
        partialDataSet.addTable("cfe_18.capture_type");
        partialDataSet.addTable("cfe_18.capture_meta_file");
        partialDataSet.addTable("cfe_18.capture_definition");
        partialDataSet.addTable("cfe_18.capture_def_group");
        partialDataSet.addTable("cfe_18.capture_def_group_x_capture_def");
        partialDataSet.addTable("location.host");
        partialDataSet.addTable("location.host_group");
        partialDataSet.addTable("location.host_group_x_host");
        partialDataSet.addTable("cfe_18.host_groups_x_capture_def_group");

        FlatXmlDataSet.write(partialDataSet, Files.newOutputStream(Paths.get("src/test/resources/XMLTriggersHostXCapture/triggerTestData.xml")));

        cleanup(conn);
    }

    private void cleanup(Connection conn) throws SQLException {

        Statement deleteStatement = conn.createStatement();
        deleteStatement.addBatch("delete from cfe_18.host_groups_x_capture_def_group");
        deleteStatement.addBatch("delete from location.host_group_x_host");
        deleteStatement.addBatch("delete from location.host_group");
        deleteStatement.addBatch("delete from location.host");
        deleteStatement.addBatch("delete from cfe_18.capture_def_group_x_capture_def");
        deleteStatement.addBatch("delete from cfe_18.capture_def_group");
        deleteStatement.addBatch("delete from cfe_18.capture_definition");
        deleteStatement.addBatch("delete from cfe_18.capture_meta_file");
        deleteStatement.addBatch("delete from cfe_18.capture_type");
        deleteStatement.addBatch("delete from cfe_18.tags");
        deleteStatement.addBatch("delete from cfe_18.processing_type");
        deleteStatement.addBatch("delete from cfe_18.templates");
        deleteStatement.addBatch("delete from cfe_18.ruleset");
        deleteStatement.addBatch("delete from cfe_18.regex");
        deleteStatement.addBatch("delete from cfe_18.newline");
        deleteStatement.addBatch("delete from cfe_18.inputtype");
        deleteStatement.addBatch("delete from flow.capture_sink");
        deleteStatement.addBatch("delete from flow.flows");
        deleteStatement.addBatch("delete from flow.L7");
        deleteStatement.executeBatch();
    }

    private void initialize(Connection conn) throws SQLException {

        Statement insertTestData = conn.createStatement();

        insertTestData.addBatch
                ("insert into flow.L7(id, app_protocol) values (1, 'prot1');");

        insertTestData.addBatch
                ("insert into flow.flows(id, flowname) values (1, 'flow1');");

        insertTestData.addBatch
                ("insert into flow.capture_sink(id, L7_id, flow_id, ip_address,sink_port) values (1,1,1,'ip1','601');");

        insertTestData.addBatch
                ("insert into cfe_18.inputtype(id, inputtype) values (1, 'regex'),(2, 'newline');");

        insertTestData.addBatch
                ("insert into cfe_18.regex(id, regex,inputtype) values (1, 'normalRegex','regex');");

        insertTestData.addBatch
                ("insert into cfe_18.newline(id, newline,inputtype) values (2, 'placeholder','newline');");

        insertTestData.addBatch
                ("insert into cfe_18.ruleset(id, rule) values (1, 'ruleset1');");

        insertTestData.addBatch
                ("insert into cfe_18.templates(id, template) values (1, 'template1');");

        insertTestData.addBatch
                ("insert into cfe_18.processing_type(id, inputtype_id,ruleset_id,template_id,type_name,capture_type) values (1,1,1,1,'usesregex','cfe'),(2,2,1,1,'usesNewline','cfe');");

        insertTestData.addBatch
                ("insert into cfe_18.capture_type(id, capture_type) values (1, 'cfe'),(2, 'cfe');");

        insertTestData.addBatch
                ("insert into cfe_18.capture_meta_file(id, capturePath, tagPath, processing_type_id,capture_type) values (1, 'capPathRegex','tagPathRegex',1,'cfe'),(2, 'capPathNewline','tagPathNewline',2,'cfe');");

        insertTestData.addBatch
                ("insert into cfe_18.tags(id,tag) values (1, 'tag1'), (2, 'tag2'), (3, 'tag3'), (4, 'tag4'), (5, 'tag5'), (6, 'tag6'), (7, 'tag7'), (8, 'tag8'), (9, 'tag9'), (10, 'tag10'), (11, 'tag11'), (12, 'tag12');");

        insertTestData.addBatch
                ("insert into cfe_18.capture_definition(id, tag_id,capture_type,capture_type_id,L7_id,flow_id) values (1, 1,'cfe',1,1,1), (2, 2,'cfe',1,1,1), (3, 3,'cfe',1,1,1), (4, 4,'cfe',1,1,1), (5, 5,'cfe',1,1,1), (6, 10,'cfe',1,1,1), (7, 7,'cfe',1,1,1), (8, 8,'cfe',1,1,1), (9, 9,'cfe',1,1,1), (10, 9,'cfe',1,1,1), (11, 7,'cfe',1,1,1), (12, 10,'cfe',1,1,1);");

        insertTestData.addBatch
                ("insert into location.host(id,MD5,host_type) values (1,'12365','cfe'), (2,'12322','cfe'), (3,'1323','cfe'), (4,'4123','cfe'), (5,'5123','cfe'), (6,'6123','cfe'), (7,'7123','cfe'), (16,'712','cfe'), (9,'723','cfe'), (8,'71233','cfe'), (10,'712362','cfe'), (12,'712653','cfe'), (17,'71211233','cfe'), (11,'712213123','cfe'), (21,'7142213123','cfe'), (22,'7125213123','cfe'), (23,'7122613123','cfe'), (24,'7122173123','cfe'), (15,'75757122173123','cfe');");

        insertTestData.addBatch
                ("insert into location.host_group (id, groupName) values (1, 'host_group_1'), (2, 'host_group_2'), (3, 'host_group_3'), (4, 'host_group_4'), (5, 'host_group_5'), (6, 'host_group_6');");

        insertTestData.addBatch
                ("insert into location.host_group_x_host(id,host_group_id, host_id,host_type) values (1,1, 1,'cfe'), (2,1, 2,'cfe'), (3,1, 3,'cfe'), (4,1, 4,'cfe'), (5,2, 5,'cfe'), (6,2, 6,'cfe'), (7,2, 7,'cfe'), (8,2, 16,'cfe'), (9,3, 9,'cfe'), (10,3, 8,'cfe'), (11,3, 10,'cfe'), (12,3, 12,'cfe'), (13,4, 17,'cfe'), (14,4, 11,'cfe'), (15,5, 21,'cfe'), (16,5, 22,'cfe'), (17,6, 23,'cfe'), (18,6, 24,'cfe');");

        insertTestData.addBatch
                ("insert into cfe_18.capture_def_group(id, capture_def_group_name) values (1, 'capturegroup1'), (2, 'capturegroup2'), (3, 'capturegroup3'), (4, 'capturegroup4'), (5, 'capturegroup5'), (6, 'capturegroup6');");

        insertTestData.addBatch
                ("insert into cfe_18.capture_def_group_x_capture_def(id,capture_def_id,capture_def_group_id,tag_id,capture_type) values (1, 1, 1, 1,'cfe'), (2, 2, 1, 2,'cfe'), (3, 3, 1, 3,'cfe'), (4, 4, 1, 4,'cfe'), (5, 5, 2, 5,'cfe'), (6, 6, 2, 10,'cfe'), (7, 7, 2, 7,'cfe'), (8, 2, 2, 2,'cfe'), (9, 10, 3, 9,'cfe'), (10, 8, 4, 8,'cfe'), (11, 12, 5, 10,'cfe'), (12, 8, 6, 8,'cfe');");

        insertTestData.addBatch
                ("insert into cfe_18.host_groups_x_capture_def_group(id,host_group_id,capture_group_id) values (1, 1, 1), (2, 2, 2), (3, 3, 3), (6, 4, 4), (7, 5, 5);");

        insertTestData.executeBatch();
    }
}
