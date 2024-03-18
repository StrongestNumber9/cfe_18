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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProcedureHostMetaDataFileTest extends DBInformation {

    @Test
    @EnabledIfSystemProperty(named = "ProcedureHostMetaDataTest.generateData", matches = "true")
    public void generateData() throws Exception {

        initialize(conn);
        IDatabaseConnection connection = new DatabaseConnection(conn);
        // partial database export
        QueryDataSet partialDataSet = new QueryDataSet(connection);

        partialDataSet.addTable("location.host");
        partialDataSet.addTable("cfe_00.hubs");
        partialDataSet.addTable("cfe_00.host_type_cfe");
        partialDataSet.addTable("cfe_03.os_type");
        partialDataSet.addTable("cfe_03.flavor_type");
        partialDataSet.addTable("cfe_03.arch_type");
        partialDataSet.addTable("cfe_03.release_version");
        partialDataSet.addTable("cfe_03.host_meta");
        partialDataSet.addTable("cfe_03.cmbd");
        partialDataSet.addTable("cfe_03.interfaces");
        partialDataSet.addTable("cfe_03.ip_addresses");
        partialDataSet.addTable("cfe_03.host_meta_x_ip");
        partialDataSet.addTable("cfe_03.host_meta_x_interface");
        FlatXmlDataSet.write(partialDataSet, Files.newOutputStream(Paths.get("src/test/resources/XMLProcedureHostMeta/procedureHostMetaTestData.xml")));
    }

    private void cleanup(Connection conn) throws SQLException {

        Statement deleteStatement = conn.createStatement();
        deleteStatement.addBatch("delete from cfe_03.ip_addresses");
        deleteStatement.addBatch("delete from cfe_03.interfaces");
        deleteStatement.addBatch("delete from cfe_03.host_meta_x_interface");
        deleteStatement.addBatch("delete from cfe_03.host_meta_x_ip");
        deleteStatement.addBatch("delete from cfe_03.host_meta");
        deleteStatement.addBatch("delete from cfe_03.release_version");
        deleteStatement.addBatch("delete from cfe_03.arch_type");
        deleteStatement.addBatch("delete from cfe_03.flavor_type");
        deleteStatement.addBatch("delete from cfe_03.os_type");
        deleteStatement.addBatch("delete from cfe_03.cmbd");
        deleteStatement.addBatch("delete from cfe_00.host_type_cfe");
        deleteStatement.addBatch("delete from cfe_00.hubs");
        deleteStatement.addBatch("delete from location.host");

        deleteStatement.executeBatch();
    }

    private void initialize(Connection conn) throws SQLException {

        Statement insertTestData = conn.createStatement();
        insertTestData.addBatch("insert into location.host(id,MD5,fqhost,host_type) values (1,'12365','1','cfe'), (2,'12322','2','cfe'), (3,'1323','3','cfe'), (4,'4123','4','cfe'), (5,'5123','6','cfe'), (6,'6123','7','cfe');");
        insertTestData.addBatch("insert into cfe_00.hubs(id,host_id, ip, host_type) values (1,1,'ip?' ,'cfe');");
        insertTestData.addBatch("insert into cfe_00.host_type_cfe(host_id, host_type, hub_id) values (1,'cfe',1);");
        insertTestData.addBatch("insert into cfe_03.os_type(id,os) values(1,'Linux1'),(2,'Linux2')");
        insertTestData.addBatch("insert into cfe_03.flavor_type(id,flavor) values(1,'flavor1'),(2,'flavor2')");
        insertTestData.addBatch("insert into cfe_03.arch_type(id,arch) values(1,'arch1'),(2,'arch2')");
        insertTestData.addBatch("insert into cfe_03.release_version(id,rel_ver) values(1,'release_version1'),(2,'release_version2')");
        insertTestData.addBatch("insert into cfe_03.host_meta(id,release_ver_id,flavor_id,arch_id,os_id) values(1,1,1,1,1)");
        insertTestData.addBatch("insert into cfe_03.cmbd(hostname,host_id,host_meta_id) values('host1',1,1)");
        insertTestData.addBatch("insert into cfe_03.interfaces(id,interface) values(1,'ens192'),(2,'ens256')");
        insertTestData.addBatch("insert into cfe_03.ip_addresses(id,ip_address) values(1,'ip1'),(2,'ip2'),(3,'ip3')");
        insertTestData.addBatch("insert into cfe_03.host_meta_x_ip(id,host_meta_id,ip_id) values(1,1,1),(2,1,2),(3,1,3)");
        insertTestData.addBatch("insert into cfe_03.host_meta_x_interface(id,host_meta_id,interface_id) values(1,1,1),(2,1,2)");
        insertTestData.executeBatch();
    }
}
