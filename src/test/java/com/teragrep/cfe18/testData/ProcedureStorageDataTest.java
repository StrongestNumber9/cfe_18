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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProcedureStorageDataTest extends DBInformation {

    @Test
    @ResourceLock(value = "conn")
    @EnabledIfSystemProperty(named = "ProcedureStorage.generateData", matches = "true")
    public void generateData() throws Exception {
        initialize(conn);
        IDatabaseConnection connection = new DatabaseConnection(conn);
        QueryDataSet partialDataSet = new QueryDataSet(connection);
        partialDataSet.addTable("flow.flows");
        partialDataSet.addTable("flow.storages");
        partialDataSet.addTable("flow.cfe_23");
        partialDataSet.addTable("flow.cfe_12");
        partialDataSet.addTable("flow.cfe_19");
        partialDataSet.addTable("flow.cfe_11");
        partialDataSet.addTable("flow.cfe_10");
        partialDataSet.addTable("flow.cfe_04");
        partialDataSet.addTable("flow.flow_targets");
        partialDataSet.addTable("flow.L7");
        partialDataSet.addTable("flow.capture_sink");
        // capture_def reqs

        // Processing type data
        partialDataSet.addTable("cfe_18.ruleset");
        partialDataSet.addTable("cfe_18.templates");
        partialDataSet.addTable("cfe_18.inputtype");
        partialDataSet.addTable("cfe_18.newline");
        partialDataSet.addTable("cfe_18.regex");
        partialDataSet.addTable("cfe_18.processing_type");
        partialDataSet.addTable("cfe_18.tags");

        // Capture data
        partialDataSet.addTable("cfe_18.application");
        partialDataSet.addTable("cfe_18.retentionTime");
        partialDataSet.addTable("cfe_18.captureIndex");
        partialDataSet.addTable("cfe_18.captureSourcetype");
        partialDataSet.addTable("cfe_18.category");

        // linkage data
        partialDataSet.addTable("cfe_18.capture_type");
        partialDataSet.addTable("cfe_18.capture_meta_file");

        // linkage
        partialDataSet.addTable("cfe_18.capture_definition"); // Vain RELP. Ei vaadi tarkistusta eri capture tyypistä
        partialDataSet.addTable("cfe_18.capture_def_x_flow_targets");

        FlatXmlDataSet.write(partialDataSet, Files.newOutputStream(Paths.get("src/test/resources/XMLProcedureStorage/procedureStorageData.xml")));
        cleanup(conn);
    }

    private void cleanup(Connection conn) throws SQLException {

        Statement deleteStatement = conn.createStatement();
        //linkage
        deleteStatement.addBatch("delete from cfe_18.capture_def_x_flow_targets");

        // cap def
        deleteStatement.addBatch("delete from cfe_18.capture_definition");
        deleteStatement.addBatch("delete from cfe_18.capture_meta_file");
        deleteStatement.addBatch("delete from cfe_18.capture_type");
        deleteStatement.addBatch("delete from cfe_18.tags");

        deleteStatement.addBatch("delete from cfe_18.application");
        deleteStatement.addBatch("delete from cfe_18.retentionTime");
        deleteStatement.addBatch("delete from cfe_18.captureIndex");
        deleteStatement.addBatch("delete from cfe_18.captureSourcetype");
        deleteStatement.addBatch("delete from cfe_18.category");

        deleteStatement.addBatch("delete from cfe_18.processing_type");
        deleteStatement.addBatch("delete from cfe_18.templates");
        deleteStatement.addBatch("delete from cfe_18.ruleset");
        deleteStatement.addBatch("delete from cfe_18.regex");
        deleteStatement.addBatch("delete from cfe_18.newline");
        deleteStatement.addBatch("delete from cfe_18.inputtype");

        // sink
        deleteStatement.addBatch("delete from flow.capture_sink");
        deleteStatement.addBatch("delete from flow.L7");
        deleteStatement.addBatch("delete from flow.flow_targets");

        // storages
        deleteStatement.addBatch("delete from flow.cfe_04");
        deleteStatement.addBatch("delete from flow.cfe_10");
        deleteStatement.addBatch("delete from flow.cfe_11");
        deleteStatement.addBatch("delete from flow.cfe_19");
        deleteStatement.addBatch("delete from flow.cfe_12");
        deleteStatement.addBatch("delete from flow.cfe_23");
        deleteStatement.addBatch("delete from flow.storages");
        deleteStatement.addBatch("delete from flow.flows");
        deleteStatement.executeBatch();
    }

    private void initialize(Connection conn) throws SQLException {
        Statement insertTestData = conn.createStatement();
        insertTestData.addBatch("insert into flow.flows(name) values('flow1'),('flow')");
        insertTestData.addBatch("insert into flow.storages(id,cfe_type) values(1,'cfe_04')");
        insertTestData.addBatch("insert into flow.storages(id,cfe_type) values(2,'cfe_10')");
        insertTestData.addBatch("insert into flow.storages(id,cfe_type) values(3,'cfe_11')");
        insertTestData.addBatch("insert into flow.storages(id,cfe_type) values(4,'cfe_12')");
        insertTestData.addBatch("insert into flow.storages(id,cfe_type) values(5,'cfe_19')");
        insertTestData.addBatch("insert into flow.storages(id,cfe_type) values(6,'cfe_23')");
        insertTestData.addBatch("insert into flow.cfe_04(id,cfe_type) values(1,'cfe_04')");
        insertTestData.addBatch("insert into flow.cfe_10(id,cfe_type) values(2,'cfe_10')");
        insertTestData.addBatch("insert into flow.cfe_11(id,cfe_type) values(3,'cfe_11')");
        insertTestData.addBatch("insert into flow.cfe_12(id,cfe_type) values(4,'cfe_12')");
        insertTestData.addBatch("insert into flow.cfe_19(id,cfe_type) values(5,'cfe_19')");
        insertTestData.addBatch("insert into flow.cfe_23(id,cfe_type) values(6,'cfe_23')");
        insertTestData.addBatch("insert into flow.flow_targets(id,target_name,flow_id,storage_id,storage_type) values(1,'cfe_04',1,1,'cfe_04')");
        insertTestData.addBatch("insert into flow.flow_targets(id,target_name,flow_id,storage_id,storage_type) values(2,'cfe_10',1,2,'cfe_10')");
        insertTestData.addBatch("insert into flow.flow_targets(id,target_name,flow_id,storage_id,storage_type) values(3,'cfe_11',1,3,'cfe_11')");
        insertTestData.addBatch("insert into flow.flow_targets(id,target_name,flow_id,storage_id,storage_type) values(4,'cfe_12',1,4,'cfe_12')");
        insertTestData.addBatch("insert into flow.flow_targets(id,target_name,flow_id,storage_id,storage_type) values(5,'cfe_19',1,5,'cfe_19')");
        insertTestData.addBatch("insert into flow.flow_targets(id,target_name,flow_id,storage_id,storage_type) values(6,'cfe_23',1,6,'cfe_23')");
        insertTestData.addBatch("insert into flow.flow_targets(id,target_name,flow_id,storage_id,storage_type) values(7,'cfe_01',2,1,'cfe_04')");
        insertTestData.addBatch("insert into flow.L7(id,app_protocol) values(1,'plain')");
        insertTestData.addBatch("insert into flow.capture_sink(L7_id, flow_id,ip_address,sink_port)values(1,1,'ip1','652')");

        // cap def

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
                ("insert into cfe_18.processing_type(id, inputtype_id,ruleset_id,template_id,type_name) values (1,1,1,1,'usesregex'),(2,2,1,1,'usesNewline');");

        insertTestData.addBatch
                ("insert into cfe_18.capture_type(id, capture_type) values (1, 'cfe'),(2, 'cfe');");

        insertTestData.addBatch
                ("insert into cfe_18.capture_meta_file(id, capturePath, tagPath, processing_type_id,capture_type) values (1, 'capPathRegex','tagPathRegex',1,'cfe'),(2, 'capPathNewline','tagPathNewline',2,'cfe');");

        insertTestData.addBatch
                ("insert into cfe_18.tags(id,tag) values (1, 'tag1'), (2, 'tag2'), (3, 'tag3'), (4, 'tag4'), (5, 'tag5'), (6, 'tag6'), (7, 'tag7'), (8, 'tag8'), (9, 'tag9'), (10, 'tag10'), (11, 'tag11'), (12, 'tag12');");

        insertTestData.addBatch
                ("insert into cfe_18.application(id,app) values (1, 'app');");

        insertTestData.addBatch
                ("insert into cfe_18.retentionTime(id,retention) values (1, 'P30D');");

        insertTestData.addBatch
                ("insert into cfe_18.captureIndex(id,captureIndex) values (1, 'index');");

        insertTestData.addBatch
                ("insert into cfe_18.captureSourcetype(id,captureSourcetype) values (1, 'sourcetype');");

        insertTestData.addBatch
                ("insert into cfe_18.category(id,category) values (1, 'audit');");

        insertTestData.addBatch
                ("insert into cfe_18.capture_definition(id, tag_id,application_id,captureIndex_id,retentionTime_id,captureSourcetype_id,category_id,capture_type,capture_type_id,L7_id,flow_id) values (1, 1,1,1,1,1,1,'cfe',1,1,1), (2, 2,1,1,1,1,1,'cfe',1,1,1), (3, 3,1,1,1,1,1,'cfe',1,1,1), (4, 4,1,1,1,1,1,'cfe',1,1,1), (5, 5,1,1,1,1,1,'cfe',1,1,1), (6, 10,1,1,1,1,1,'cfe',1,1,1), (7, 7,1,1,1,1,1,'cfe',1,1,1), (8, 8,1,1,1,1,1,'cfe',1,1,1), (9, 9,1,1,1,1,1,'cfe',1,1,1), (10, 9,1,1,1,1,1,'cfe',1,1,1), (11, 7,1,1,1,1,1,'cfe',1,1,1), (12, 10,1,1,1,1,1,'cfe',1,1,1);");


        insertTestData.addBatch("insert into cfe_18.capture_def_x_flow_targets(id,capture_def_id,flow_id,flow_target_id) values(1,1,1,1)");
        insertTestData.executeBatch();

    }
}
