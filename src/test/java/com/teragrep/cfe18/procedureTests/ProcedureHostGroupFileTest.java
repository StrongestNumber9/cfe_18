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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
Tests for host group
Uses dataset procedureHostGroupTestData.xml which is copied version of triggerTestData.xml
 */
public class ProcedureHostGroupFileTest extends DBUnitbase {

    public ProcedureHostGroupFileTest(String name) {
        super(name);
    }

    @Override
    protected IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder().build(Files.newInputStream(Paths.get("src/test/resources/XMLProcedureHostGroup/procedureHostGroupTestData.xml")));
    }

    /*
    This test is for checking that host_group add procedure works when adding a new host_group with host
     */
    public void testProcedureAddHostGroupSuccess() throws Exception {

        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/XMLProcedureHostGroup/procedureHostGroupTestDataExpected1.xml"));
        ITable expectedTable2 = expectedDataSet.getTable("location.host");
        ITable expectedTable3 = expectedDataSet.getTable("location.host_group");

        CallableStatement stmnt = conn.prepareCall("{call location.add_host_group_with_host(?,?)}");
        stmnt.setInt(1, 1); // host id
        stmnt.setString(2, "host_group_6");
        stmnt.execute();

        ITable actualTable2 = getConnection().createQueryTable("result", "select * from location.host");
        ITable actualTable3 = getConnection().createQueryTable("result", "select * from location.host_group");

        //Assertion.assertEquals(expectedTable1, actualTable1); Currently under work. Host_group_x_host does not include host_group_id when adding new row?
        Assertion.assertEquals(expectedTable2, actualTable2);
        Assertion.assertEquals(expectedTable3, actualTable3);

    }

    /*
    This test is for checking that new host group is created when one does not exist during insertion.
     */
    public void testProcedureAddHostWithNewGroup() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/XMLProcedureHostGroup/procedureHostGroupTestDataExpected2.xml"));
        ITable expectedTable1 = expectedDataSet.getTable("location.host_group_x_host");
        ITable expectedTable2 = expectedDataSet.getTable("location.host");
        ITable expectedTable3 = expectedDataSet.getTable("location.host_group");

        CallableStatement stmnt = conn.prepareCall("{call location.add_host_group_with_host(?,?)}");
        stmnt.setInt(1, 1); // host id
        stmnt.setString(2, "new_host_group");
        stmnt.execute();

        ITable actualTable1 = getConnection().createQueryTable("result", "select * from location.host_group_x_host");
        ITable actualTable2 = getConnection().createQueryTable("result", "select * from location.host");
        ITable actualTable3 = getConnection().createQueryTable("result", "select * from location.host_group");

        Assertion.assertEqualsIgnoreCols(expectedTable1, actualTable1, new String[]{"id"});
        Assertion.assertEquals(expectedTable2, actualTable2);
        Assertion.assertEquals(expectedTable3, actualTable3);

    }

    /*
    This test is for checking if the host id check is in place when inserting a host group with invalid host id
     */
    public void testHostValidityWithHostGroup() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{CALL location.add_host_group_with_host(?,?)}");
            stmnt.setInt(1, 1000);
            stmnt.setString(2, "host_group_1");
            stmnt.execute();
        });
        Assertions.assertEquals("45000", state.getSQLState());
    }

    /*
    This test is for checking the validity of host group output. Goal is that the matching values are returned using correct host group name
     */
    public void testRetrieveHostGroupDetails() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);
        List<Integer> host_id = new ArrayList<>();
        List<String> md5 = new ArrayList<>();
        CallableStatement stmnt = conn.prepareCall("{CALL location.retrieve_host_group_details(?)}");
        stmnt.setString(1, "host_group_1");
        ResultSet rs = stmnt.executeQuery();
        while (rs.next()) {
            host_id.add(rs.getInt("host_id"));
            md5.add(rs.getString("md5"));
            String group_name = rs.getString("group_name");
            String host_type = rs.getString("host_type");
            int host_group_id = rs.getInt("host_group_id");
            Assertions.assertEquals("host_group_1", group_name);
            Assertions.assertEquals("cfe", host_type);
            Assertions.assertEquals(1, host_group_id);
        }
        Assertions.assertEquals(Arrays.asList(1, 2, 3, 4), host_id);
        Assertions.assertEquals(Arrays.asList("12365", "12322", "1323", "4123"), md5);
    }
}
