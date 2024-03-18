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
public class ProcedureHostDataTestFile extends DBInformation {

    @Test
    @ResourceLock(value = "conn")
    @EnabledIfSystemProperty(named = "ProcedureHostDataTest.generateData", matches = "true")
    public void generateData() throws Exception {

        initialize(conn);
        IDatabaseConnection connection = new DatabaseConnection(conn);
        // partial database export
        QueryDataSet partialDataSet = new QueryDataSet(connection);
        partialDataSet.addTable("location.host");
        partialDataSet.addTable("cfe_00.hubs");
        partialDataSet.addTable("cfe_00.host_type_cfe");
        FlatXmlDataSet.write(partialDataSet, Files.newOutputStream(Paths.get("src/test/resources/XMLProcedureHost/procedureHostTestData.xml")));
        cleanup(conn);
    }

    private void cleanup(Connection conn) throws SQLException {

        Statement deleteStatement = conn.createStatement();
        deleteStatement.addBatch("delete from cfe_00.host_type_cfe");
        deleteStatement.addBatch("delete from cfe_00.hubs");
        deleteStatement.addBatch("delete from location.host");
        deleteStatement.executeBatch();
    }

    private void initialize(Connection conn) throws SQLException {

        Statement insertTestData = conn.createStatement();

        insertTestData.addBatch("insert into location.host(id,MD5,fqhost,host_type) values (1,'12365','1','cfe'), (2,'12322','2','manual'), (3,'1323','3','manual'), (4,'4123','4','manual'), (5,'5123','6','manual'), (6,'6123','7','manual'), (7,'7123','8','manual'), (16,'712','9','manual'), (9,'723','10','manual'), (8,'71233','11','manual'), (10,'712362','12','manual'), (12,'712653','13','manual'), (17,'71211233','14','manual'), (11,'712213123','15','manual'), (21,'7142213123','16','manual'), (22,'7125213123','17','manual'), (23,'7122613123','18','manual'), (24,'7122173123','19','manual'), (15,'75757122173123','20','manual');");
        insertTestData.addBatch("insert into cfe_00.hubs(id,host_id, ip, host_type) values (1,1,'ip?' ,'cfe');");
        insertTestData.addBatch("insert into cfe_00.host_type_cfe(host_id, host_type, hub_id) values (1,'cfe',1);");

        insertTestData.executeBatch();
    }
}
