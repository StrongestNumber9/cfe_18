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
package com.teragrep.cfe18.procedureTests;

import org.dbunit.Assertion;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;


public class ProcedureHostAddCfeFileTest extends DBUnitbase {

    public ProcedureHostAddCfeFileTest(String name) {
        super(name);
    }

    @Override
    protected IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder().build(Files.newInputStream(Paths.get("src/test/resources/XMLProcedureHost/procedureHostTestData.xml")));
    }

    /*
    Test for checking if the procedure lets add host without an existing hub.
     */
    public void testProcedureCfeHostNoHub() throws Exception {

        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{CALL location.host_add_cfe(?,?,?)}");
            stmnt.setString(1, "someMd5Test");
            stmnt.setString(2, "someFullyQualifiedHost");
            stmnt.setString(3, "someFullyQualifiedHostForHub");
            stmnt.execute();
        });
        Assertions.assertEquals("45000", state.getSQLState());
    }


    /*
    Test for checking if procedure is functional and let's add a new cfe hub

    NB! hub_id can vary depending on the incremental value. This test ignores hub_id column at the assertion.
    No clue how to fix it yet.
    */
    public void testProcedureCfeHubAccept() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        // Gets the expected dataset
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/XMLProcedureHost/procedureHostExpectedTestData1.xml"));
        ITable expectedTable = expectedDataSet.getTable("cfe_00.host_type_cfe");

        CallableStatement stmnt = conn.prepareCall("{call location.host_add_cfe_hub(?, ?, ?)}");
        stmnt.setString(1, "hosit1");
        stmnt.setString(2, "Md5jokaeiosu");
        stmnt.setString(3, "jokuip");
        stmnt.execute();

        // Retrieves the dataset which happened after the execution
        ITable actualTable = getConnection().createQueryTable("result", "select * from cfe_00.host_type_cfe");

        // Assert actual database table match expected table
        Assertion.assertEqualsIgnoreCols(expectedTable, actualTable, new String[]{"hub_id", "host_id"});
    }


    /*
        This test checks that cfe host can be added and linked to a hub.
     */
    public void testProcedureCfeHostAccept() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/XMLProcedureHost/procedureHostExpectedTestData2.xml"));
        ITable expectedTable = expectedDataSet.getTable("location.host");


        CallableStatement stmnt = conn.prepareCall("{call location.host_add_cfe( ?, ?, ?)}");
        stmnt.setString(1, "51232445674");
        stmnt.setString(2, "534209325");
        stmnt.setString(3, "1");
        stmnt.execute();

        ITable actualTable = getConnection().createQueryTable("result", "select * from location.host");

        // Assert actual database table match expected table
        Assertion.assertEqualsIgnoreCols(expectedTable, actualTable, new String[]{"id"});

    }


    /*
    This test is retrieving relp based host
*/
    public void testProcedureRetrieveRelpHost() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);


        CallableStatement stmnt = conn.prepareCall("{CALL cfe_00.retrieve_host_details(?)}");
        stmnt.setInt(1, 25);
        ResultSet rs = stmnt.executeQuery();
        int host_id = 0;
        String md5 = null;
        String fqhost = null;
        String host_type = null;
        // roll through the result
        while (rs.next()) {
            host_id = rs.getInt("host_id");
            md5 = rs.getString("md5");
            fqhost = rs.getString("fqhost");
            host_type = rs.getString("host_type");

        }
        Assertions.assertEquals(host_id, 25);
        Assertions.assertEquals(md5, "relpmd5");
        Assertions.assertEquals(fqhost, "relpfqhost");
        Assertions.assertEquals(host_type, "relp");
    }

    /*
    This test is retrieving cfe hub
*/
    public void testProcedureRetrieveHub() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        CallableStatement stmnt = conn.prepareCall("{CALL location.retrieve_cfe_hub_details(?)}");
        stmnt.setInt(1, 1);
        ResultSet rs = stmnt.executeQuery();
        int hub_id = 0;
        int host_id = 0;
        String hub_fq = null;
        String ip = null;
        String md5 = null;
        // roll through the result
        while (rs.next()) {
            hub_id = rs.getInt("hub_id");
            host_id = rs.getInt("host_id");
            hub_fq = rs.getString("hub_fq_host");
            ip = rs.getString("ip");
            md5 = rs.getString("md5");

        }
        Assertions.assertEquals(hub_id, 1);
        Assertions.assertEquals(host_id, 1);
        Assertions.assertEquals(hub_fq, "1");
        Assertions.assertEquals(ip, "ip?");
        Assertions.assertEquals(md5, "12365");
    }


    /*
    This test is adding a new relp host
*/
    public void testProcedureAddRelpHost() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/XMLProcedureHost/procedureHostExpectedTestData3.xml"));
        ITable expectedTable = expectedDataSet.getTable("location.host");

        CallableStatement stmnt = conn.prepareCall("{call location.host_add_relp(?, ?)}");
        stmnt.setString(1, "ReliantMD5");
        stmnt.setString(2, "ReliantFqHost");
        stmnt.execute();

        ITable actualTable = getConnection().createQueryTable("result", "select * from location.host");

        // Assert actual database table match expected table
        Assertion.assertEqualsIgnoreCols(expectedTable, actualTable, new String[]{"id"});
    }


    /*
    This test is for trying to retrieve cfe based host without hostmeta included. Results in error state 42000 "insert host meta data first"
     */
    public void testProcedureRetrieveHostWithoutMeta() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{CALL location.retrieve_host_details(?)}");
            stmnt.setInt(1, 1);
            stmnt.execute();
        });
        Assertions.assertEquals("42000", state.getSQLState());
    }
}



