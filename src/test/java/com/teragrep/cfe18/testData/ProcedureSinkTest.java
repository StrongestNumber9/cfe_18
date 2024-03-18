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
public class ProcedureSinkTest extends DBInformation {

    @Test
    @ResourceLock(value = "conn")
    @EnabledIfSystemProperty(named = "ProcedureSink.generateData", matches = "true")
    public void generateData() throws Exception {

        initialize(conn);
        IDatabaseConnection connection = new DatabaseConnection(conn);
        // partial database export
        QueryDataSet partialDataSet = new QueryDataSet(connection);
        partialDataSet.addTable("flow.flows");
        partialDataSet.addTable("flow.L7");
        partialDataSet.addTable("flow.capture_sink");

        FlatXmlDataSet.write(partialDataSet, Files.newOutputStream(Paths.get("src/test/resources/XMLProcedureSink/procedureSinkData.xml")));
        cleanup(conn);
    }

    private void cleanup(Connection conn) throws SQLException {

        Statement deleteStatement = conn.createStatement();
        deleteStatement.addBatch("delete from flow.capture_sink");
        deleteStatement.addBatch("delete from flow.L7");
        deleteStatement.addBatch("delete from flow.flows");
        deleteStatement.executeBatch();
    }

    private void initialize(Connection conn) throws SQLException {

        Statement insertTestData = conn.createStatement();
        insertTestData.addBatch("insert into flow.L7(id,app_protocol) values (1,'prot1'),(2,'prot2');");
        insertTestData.addBatch("insert into flow.flows(id,flowname) values (1,'flow1'),(2,'flow2');");
        insertTestData.addBatch("insert into flow.capture_sink(id,L7_id,flow_id,ip_address,sink_port) values (1,1,2,'ip1','601'),(2,1,1,'ip55','601');");
        insertTestData.executeBatch();
    }
}
