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
Tests for capture meta file procedure. Meant for testing the integrity of adding new capture which is file type.
Uses dataset procedureCaptureTestData.xml
 */
public class ProcedureCaptureMetaFileTest extends DBUnitbase {

    public ProcedureCaptureMetaFileTest(String name) {
        super(name);
    }

    @Override
    protected IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder().build(Files.newInputStream(Paths.get("src/test/resources/XMLProcedureCapture/procedureCaptureTestData.xml")));
    }

    /*
    This test checks that new capture_definition can be added via using existing processing_type and sink.
    In this example Tag and Tag_path are tested aswell that they are the same when inserting.
     */
    public void testProcedureAddCaptureSuccess() throws Exception {

        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/XMLProcedureCapture/procedureCaptureExpectedTestData1.xml"));
        ITable expectedTable = expectedDataSet.getTable("cfe_18.capture_definition");

        CallableStatement stmnt = conn.prepareCall("{call cfe_18.add_new_capture_file(?,?,?,?,?,?,?,?,?,?,?)}");
        stmnt.setString(1, "632db722-tag.tag");
        stmnt.setString(2, "P1Y");
        stmnt.setString(3, "tech");
        stmnt.setString(4, "app2");
        stmnt.setString(5, "index2");
        stmnt.setString(6, "sourcetypetest");
        stmnt.setString(7, "prot1");
        stmnt.setString(8, "flow1");
        stmnt.setString(9, "path/towards/folder/where/tag.tag");
        stmnt.setString(10, "PathToCapture");
        stmnt.setString(11, "usesregex");
        stmnt.execute();

        ITable actualTable = getConnection().createQueryTable("result", "select * from cfe_18.capture_definition");

        Assertion.assertEqualsIgnoreCols(expectedTable, actualTable, new String[]{"capture_type_id", "id", "tag_id"});

    }

    /*
    Test for checking that capture inserted has tag and tag_path mismatch. In other words there can not be a mismatch if both are submitted.
    Aim is to trigger sql state signal where this is not allowed
     */
    public void testProcedureCaptureTagMismatch() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);
        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{call cfe_18.add_new_capture_file(?,?,?,?,?,?,?,?,?,?,?)}");
            stmnt.setString(1, "632db722-tag.tag");
            stmnt.setString(2, "P1Y");
            stmnt.setString(3, "tech");
            stmnt.setString(4, "app2");
            stmnt.setString(5, "index2");
            stmnt.setString(6, "sourcetypetest");
            stmnt.setString(7, "prot1");
            stmnt.setString(8, "flow1");
            stmnt.setString(9, "FaultyTagPathTrigger");
            stmnt.setString(10, "PathToCapture");
            stmnt.setString(11, "usesregex");
            stmnt.execute();
        });
        Assertions.assertEquals("45000", state.getSQLState());
    }

    /*
     Test for checking capture insertion without tag. Tag path is suppose to create the new tag.
    */
    public void testProcedureCaptureTagCreate() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/XMLProcedureCapture/procedureCaptureExpectedTestData2.xml"));
        ITable expectedTable = expectedDataSet.getTable("cfe_18.tags");

        CallableStatement stmnt = conn.prepareCall("{call cfe_18.add_new_capture_file(?,?,?,?,?,?,?,?,?,?,?)}");
        stmnt.setString(1, null);
        stmnt.setString(2, "P1Y");
        stmnt.setString(3, "tech");
        stmnt.setString(4, "app2");
        stmnt.setString(5, "index2");
        stmnt.setString(6, "sourcetypetest");
        stmnt.setString(7, "prot1");
        stmnt.setString(8, "flow1");
        stmnt.setString(9, "FaultyTagPathTrigger");
        stmnt.setString(10, "PathToCapture");
        stmnt.setString(11, "usesregex");
        stmnt.execute();
        ITable actualTable = getConnection().createQueryTable("result", "select * from cfe_18.tags");

        Assertion.assertEqualsIgnoreCols(expectedTable, actualTable, new String[]{"id"});
    }


    /*
    Test for capture insertion where only tag is included and tag_path is null.
    This test should accept the creation of new capture even tho there is no tag_path.
*/
    public void testProcedureCaptureTagPathNull() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/XMLProcedureCapture/procedureCaptureExpectedTestData3.xml"));
        ITable expectedTable = expectedDataSet.getTable("cfe_18.capture_definition");
        ITable expectedTable1 = expectedDataSet.getTable("cfe_18.tags");
        ITable expectedTable2 = expectedDataSet.getTable("cfe_18.capture_meta_file");

        CallableStatement stmnt = conn.prepareCall("{call cfe_18.add_new_capture_file(?,?,?,?,?,?,?,?,?,?,?)}");
        stmnt.setString(1, "632db722-tag.tag");
        stmnt.setString(2, "P1Y");
        stmnt.setString(3, "tech");
        stmnt.setString(4, "app2");
        stmnt.setString(5, "index2");
        stmnt.setString(6, "sourcetypetest");
        stmnt.setString(7, "prot1");
        stmnt.setString(8, "flow1");
        stmnt.setString(9, null);
        stmnt.setString(10, "PathToCapture");
        stmnt.setString(11, "usesregex");
        stmnt.execute();

        ITable actualTable = getConnection().createQueryTable("result", "select * from cfe_18.capture_definition");
        ITable actualTable1 = getConnection().createQueryTable("result", "select * from cfe_18.tags");
        ITable actualTable2 = getConnection().createQueryTable("result", "select * from cfe_18.capture_meta_file");
        Assertion.assertEqualsIgnoreCols(expectedTable, actualTable, new String[]{"capture_type_id", "id", "tag_id"});
        Assertion.assertEqualsIgnoreCols(expectedTable1, actualTable1, new String[]{"id"});
        Assertion.assertEqualsIgnoreCols(expectedTable2, actualTable2, new String[]{"id"});
    }


    /*
    Test for checking if the capture being inserted does not include processing_type
*/

    public void testProcedureCaptureMissingProcessingType() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);
        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{call cfe_18.add_new_capture_file(?,?,?,?,?,?,?,?,?,?,?)}");
            stmnt.setString(1, "632db722-tag.tag");
            stmnt.setString(2, "P1Y");
            stmnt.setString(3, "tech");
            stmnt.setString(4, "app2");
            stmnt.setString(5, "index2");
            stmnt.setString(6, "sourcetypetest");
            stmnt.setString(7, "prot1");
            stmnt.setString(8, "flow1");
            stmnt.setString(9, "path/towards/folder/where/tag.tag");
            stmnt.setString(10, "PathToCapture");
            stmnt.setString(11, null);
            stmnt.execute();
        });
        Assertions.assertEquals("42000", state.getSQLState());
    }

    /*
    Test for checking if the capture being inserted has faulty processing_type name
    */

    public void testProcedureCaptureFaultyProcessingType() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);
        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{call cfe_18.add_new_capture_file(?,?,?,?,?,?,?,?,?,?,?)}");
            stmnt.setString(1, "632db722-tag.tag");
            stmnt.setString(2, "P1Y");
            stmnt.setString(3, "tech");
            stmnt.setString(4, "app2");
            stmnt.setString(5, "index2");
            stmnt.setString(6, "sourcetypetest");
            stmnt.setString(7, "prot1");
            stmnt.setString(8, "flow1");
            stmnt.setString(9, "path/towards/folder/where/tag.tag");
            stmnt.setString(10, "PathToCapture");
            stmnt.setString(11, "ThisBreaksTheProcedure");
            stmnt.execute();
        });
        Assertions.assertEquals("42000", state.getSQLState());
    }

    /*
    This test is for checking that no additional records are inserted if the values exist already.
    ID Columns are not relevant. ID can change but the amount of records stay the same thus there is no duplicates.

 */
    public void testProcedureCaptureDuplicateAvoid() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/XMLProcedureCapture/procedureCaptureExpectedTestData4.xml"));
        ITable expectedTable = expectedDataSet.getTable("cfe_18.tags");
        ITable expectedTable1 = expectedDataSet.getTable("cfe_18.retentionTime");
        ITable expectedTable2 = expectedDataSet.getTable("cfe_18.category");
        ITable expectedTable3 = expectedDataSet.getTable("cfe_18.application");
        ITable expectedTable4 = expectedDataSet.getTable("cfe_18.captureIndex");
        ITable expectedTable5 = expectedDataSet.getTable("cfe_18.captureSourcetype");
        ITable expectedTable6 = expectedDataSet.getTable("flow.L7");
        ITable expectedTable7 = expectedDataSet.getTable("flow.flows");
        ITable expectedTable8 = expectedDataSet.getTable("cfe_18.capture_meta_file");
        ITable expectedTable9 = expectedDataSet.getTable("cfe_18.processing_type");

        CallableStatement stmnt = conn.prepareCall("{call cfe_18.add_new_capture_file(?,?,?,?,?,?,?,?,?,?,?)}");
        stmnt.setString(1, "632db722-tag.tag");
        stmnt.setString(2, "P1Y");
        stmnt.setString(3, "tech");
        stmnt.setString(4, "app2");
        stmnt.setString(5, "index2");
        stmnt.setString(6, "sourcetypetest");
        stmnt.setString(7, "prot1");
        stmnt.setString(8, "flow1");
        stmnt.setString(9, "path/towards/folder/where/tag.tag");
        stmnt.setString(10, "PathToCapture");
        stmnt.setString(11, "usesregex");
        stmnt.execute();

        ITable actualTable = getConnection().createQueryTable("result", "select * from cfe_18.tags");
        ITable actualTable1 = getConnection().createQueryTable("result", "select * from cfe_18.retentionTime");
        ITable actualTable2 = getConnection().createQueryTable("result", "select * from cfe_18.category");
        ITable actualTable3 = getConnection().createQueryTable("result", "select * from cfe_18.application");
        ITable actualTable4 = getConnection().createQueryTable("result", "select * from cfe_18.captureIndex");
        ITable actualTable5 = getConnection().createQueryTable("result", "select * from cfe_18.captureSourcetype");
        ITable actualTable6 = getConnection().createQueryTable("result", "select * from flow.L7");
        ITable actualTable7 = getConnection().createQueryTable("result", "select * from flow.flows");
        ITable actualTable8 = getConnection().createQueryTable("result", "select * from cfe_18.capture_meta_file");
        ITable actualTable9 = getConnection().createQueryTable("result", "select * from cfe_18.processing_type");
        Assertion.assertEqualsIgnoreCols(expectedTable, actualTable, new String[]{"id"});
        Assertion.assertEqualsIgnoreCols(expectedTable1, actualTable1, new String[]{"id"});
        Assertion.assertEqualsIgnoreCols(expectedTable2, actualTable2, new String[]{"id"});
        Assertion.assertEqualsIgnoreCols(expectedTable3, actualTable3, new String[]{"id"});
        Assertion.assertEqualsIgnoreCols(expectedTable4, actualTable4, new String[]{"id"});
        Assertion.assertEqualsIgnoreCols(expectedTable5, actualTable5, new String[]{"id"});
        Assertion.assertEqualsIgnoreCols(expectedTable6, actualTable6, new String[]{"id"});
        Assertion.assertEqualsIgnoreCols(expectedTable7, actualTable7, new String[]{"id"});
        Assertion.assertEqualsIgnoreCols(expectedTable8, actualTable8, new String[]{"id"});
        Assertion.assertEqualsIgnoreCols(expectedTable9, actualTable9, new String[]{"id"});
    }

    /*
    This test confirms that data is retrieved accurately from capture_definition via procedure.
     */
    public void testProcedureCaptureGetById() throws Exception {

        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);
        List<Integer> actualList = new ArrayList<>();
        CallableStatement stmnt = conn.prepareCall("{CALL cfe_18.retrieve_capture_by_id(?)}");
        stmnt.setString(1, "1");
        ResultSet rs = stmnt.executeQuery();
        while (rs.next()) {
            actualList.add(rs.getInt("id"));
            String tag = rs.getString("tag");
            String app = rs.getString("app");
            String captureIndex = rs.getString("captureIndex");
            String retention_time = rs.getString("retention_time");
            String source_type = rs.getString("source_type");
            String category = rs.getString("category");
            String rule_name = rs.getString("rule_name");
            String flow = rs.getString("flow");
            String protocol = rs.getString("protocol");
            String capture_path = rs.getString("capture_path");
            String tag_path = rs.getString("tag_path");
            Assertions.assertEquals("tag1", tag);
            Assertions.assertEquals("app1", app);
            Assertions.assertEquals("index1", captureIndex);
            Assertions.assertEquals("P30D", retention_time);
            Assertions.assertEquals("sourcetype1", source_type);
            Assertions.assertEquals("audit", category);
            Assertions.assertEquals("usesregex", rule_name);
            Assertions.assertEquals("flow1", flow);
            Assertions.assertEquals("prot1", protocol);
            Assertions.assertEquals("capPathRegex", capture_path);
            Assertions.assertEquals("tagPathRegex", tag_path);
        }
        // Expecting to get only one record with ID of 1
        Assertions.assertEquals(Arrays.asList(1), actualList);
    }

    /*
    This test is for checking that capture can not be inserted if L7_id is faulty or missing
     */
    public void testMissingL7() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{call cfe_18.add_new_capture_file(?,?,?,?,?,?,?,?,?,?,?)}");
            stmnt.setString(1, "632db722-tag.tag");
            stmnt.setString(2, "P1Y");
            stmnt.setString(3, "tech");
            stmnt.setString(4, "app2");
            stmnt.setString(5, "index2");
            stmnt.setString(6, "sourcetypetest");
            stmnt.setString(7, null);
            stmnt.setString(8, "flow1");
            stmnt.setString(9, "path/towards/folder/where/tag.tag");
            stmnt.setString(10, "PathToCapture");
            stmnt.setString(11, "ThisBreaksTheProcedure");
            stmnt.execute();
        });
        Assertions.assertEquals("42000", state.getSQLState());
    }


    /*
    This test is for checking that capture can not be inserted if flow_id is faulty or missing
     */
    public void testMissingFlow() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{call cfe_18.add_new_capture_file(?,?,?,?,?,?,?,?,?,?,?)}");
            stmnt.setString(1, "632db722-tag.tag");
            stmnt.setString(2, "P1Y");
            stmnt.setString(3, "tech");
            stmnt.setString(4, "app2");
            stmnt.setString(5, "index2");
            stmnt.setString(6, "sourcetypetest");
            stmnt.setString(7, "prot1");
            stmnt.setString(8, null);
            stmnt.setString(9, "path/towards/folder/where/tag.tag");
            stmnt.setString(10, "PathToCapture");
            stmnt.setString(11, "ThisBreaksTheProcedure");
            stmnt.execute();
        });
        Assertions.assertEquals("42000", state.getSQLState());
    }

}
