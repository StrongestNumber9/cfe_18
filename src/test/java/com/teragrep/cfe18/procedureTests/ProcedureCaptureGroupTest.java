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
Tests for capture group
Uses dataset procedureCaptureGroupTestData.xml which is copied version of triggerTestData.xml
 */
public class ProcedureCaptureGroupTest extends DBUnitbase {

    public ProcedureCaptureGroupTest(String name) {
        super(name);
    }

    @Override
    protected IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder().build(Files.newInputStream(Paths.get("src/test/resources/XMLProcedureCaptureGroup/procedureCaptureGroupTestData.xml")));
    }

    /*
    This test checks that new capture group can be added with proper capture_type and capture_definition.

    NB! As database incremental status is not resetted. ID starts with 21 for the capture_group. Same happens with capture_def_group_x_capture_def.
    This could be solved by resetting the incremental rotation or by ignoring the ID columns. XML test is still accurate as it creates the rows and connects the values
    accordingly.
     */
    public void testProcedureAddCaptureGroupSuccess() throws Exception {

        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/XMLProcedureCaptureGroup/procedureCaptureGroupTestDataExpectedData1.xml"));
        ITable expectedTable1 = expectedDataSet.getTable("cfe_18.capture_def_group_x_capture_def");
        ITable expectedTable2 = expectedDataSet.getTable("cfe_18.capture_def_group");

        CallableStatement stmnt = conn.prepareCall("{call cfe_18.add_capture_group_with_capture(?,?)}");
        stmnt.setString(1, "group1");
        stmnt.setInt(2, 1);
        stmnt.execute();

        ITable actualTable1 = getConnection().createQueryTable("result", "select * from cfe_18.capture_def_group_x_capture_def");
        ITable actualTable2 = getConnection().createQueryTable("result", "select * from cfe_18.capture_def_group");

        Assertion.assertEqualsIgnoreCols(expectedTable2, actualTable2, new String[]{"id"});
        Assertion.assertEqualsIgnoreCols(expectedTable1, actualTable1, new String[]{"id", "capture_def_group_id"});

    }

    /*
    This test is for when adding a capture_group with the same name does not create another group but rather uses the existing one with new capture_definition
     */
    public void testNoDuplicateCaptureGroup() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/XMLProcedureCaptureGroup/procedureCaptureGroupTestDataExpectedData2.xml"));
        ITable expectedTable1 = expectedDataSet.getTable("cfe_18.capture_def_group_x_capture_def");
        ITable expectedTable2 = expectedDataSet.getTable("cfe_18.capture_def_group");

        CallableStatement stmnt = conn.prepareCall("{call cfe_18.add_capture_group_with_capture(?,?)}");
        stmnt.setString(1, "group1");
        stmnt.setInt(2, 2);
        stmnt.execute();

        ITable actualTable1 = getConnection().createQueryTable("result", "select * from cfe_18.capture_def_group_x_capture_def");
        ITable actualTable2 = getConnection().createQueryTable("result", "select * from cfe_18.capture_def_group");

        Assertion.assertEqualsIgnoreCols(expectedTable2, actualTable2, new String[]{"id"});
        Assertion.assertEqualsIgnoreCols(expectedTable1, actualTable1, new String[]{"id", "capture_def_group_id"});
    }


    /*
    capture_group retrieval needs to be checked aswell. It is important that the values are gathered
     */
    public void testRetrieveCaptureGroup() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        List<Integer> actualList = new ArrayList<>();
        CallableStatement stmnt = conn.prepareCall("{CALL cfe_18.retrieve_capture_group_details(?)}");
        stmnt.setString(1, "capturegroup1");
        ResultSet rs = stmnt.executeQuery();
        while (rs.next()) {
            actualList.add(rs.getInt("capture_definition_id"));
            String group_name = rs.getString("group_name");
            String capture_type = rs.getString("capture_type");
            int capture_group_id = rs.getInt("capture_group_id");
            Assertions.assertEquals("capturegroup1", group_name);
            Assertions.assertEquals("cfe", capture_type);
            Assertions.assertEquals(1, capture_group_id);
        }
        Assertions.assertEquals(Arrays.asList(1, 2, 3, 4), actualList);

    }

    /*
    If the capture_group does not exist. This needs to be tested.
     */
    public void testCaptureGroupIsMissing() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{CALL cfe_18.retrieve_capture_group_details(?)}");
            stmnt.setString(1, "groupThatDontExist");
            stmnt.execute();
        });

        Assertions.assertEquals("45000", state.getSQLState());

    }


}
