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

public class ProcedureProcessingTypeTest extends DBUnitbase {
    public ProcedureProcessingTypeTest(String name) {
        super(name);
    }

    @Override
    protected IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder().build(Files.newInputStream(Paths.get("src/test/resources/XMLProcedureProcessingType/procedureProcessingTypeTestData.xml")));
    }


    /*
    This test checks that procedure is in place and accepts insertion with new and correct values.
     */
    public void testProcessingTypeAcceptInsert() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        // Gets the expected dataset
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/XMLProcedureProcessingType/procedureProcessingTypeExpectedTestData1.xml"));
        ITable expectedTable = expectedDataSet.getTable("cfe_18.processing_type");

        CallableStatement stmnt = conn.prepareCall("{CALL cfe_18.create_data_for_processing_type(?,?,?,?,?)}");

        stmnt.setString(1, "TestTemplate1");
        stmnt.setString(2, "TestRule1");
        stmnt.setString(3, "TestName1");
        stmnt.setString(4, "regex");
        stmnt.setString(5, "TestRegex2");
        stmnt.execute();

        ITable actualTable = getConnection().createQueryTable("result", "select * from cfe_18.processing_type");

        Assertion.assertEqualsIgnoreCols(expectedTable, actualTable, new String[]{"ruleset_id", "template_id"});

    }


    /*
    4 of the tests need to be written for when there is a null value included. Processing_type does not allow null values.
     next 4 tests are for checking if one of the values inserted was null
     */
    public void testProcessingTypeNullRegex() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{CALL cfe_18.create_data_for_processing_type(?,?,?,?,?)}");
            stmnt.setString(1, "template1");
            stmnt.setString(2, "rul2");
            stmnt.setString(3, "nom1");
            stmnt.setString(4, "regex");
            stmnt.setString(5, null);
            stmnt.execute();
        });
        Assertions.assertEquals("23000", state.getSQLState());
    }

    public void testProcessingTypeNullNewline() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{CALL cfe_18.create_data_for_processing_type(?,?,?,?,?)}");
            stmnt.setString(1, "template1");
            stmnt.setString(2, "rul2");
            stmnt.setString(3, "nom1");
            stmnt.setString(4, "newline");
            stmnt.setString(5, null);
            stmnt.execute();
        });
        Assertions.assertEquals("23000", state.getSQLState());
    }

    public void testProcessingTypeNullRuleset() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{CALL cfe_18.create_data_for_processing_type(?,?,?,?,?)}");
            stmnt.setString(1, "template1");
            stmnt.setString(2, null);
            stmnt.setString(3, "nom1");
            stmnt.setString(4, "regex");
            stmnt.setString(5, "YourNormalRegex");
            stmnt.execute();
        });
        Assertions.assertEquals("23000", state.getSQLState());
    }

    public void testProcessingTypeNullTemplate() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{CALL cfe_18.create_data_for_processing_type(?,?,?,?,?)}");
            stmnt.setString(1, null);
            stmnt.setString(2, "rul2");
            stmnt.setString(3, "nom1");
            stmnt.setString(4, "regex");
            stmnt.setString(5, "YourNormalRegex");
            stmnt.execute();
        });
        Assertions.assertEquals("23000", state.getSQLState());
    }

    /*
    Test for testing get by name procedure. Testing data is plain procedureProcessingTypeTestData.xml
    Checks if the values inserted from XML correspond to the values fetch via procedure call.
     */
    public void testProcessingTypeRetrieveByName() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        CallableStatement stmnt = conn.prepareCall("{CALL cfe_18.retrieve_processing_type_by_name(?)}");
        stmnt.setString(1, "name1");
        ResultSet rs = stmnt.executeQuery();
        String ruleset = null;
        String template = null;
        String name = null;
        String inputtype = null;
        String inputvalue = null;
        while (rs.next()) {
            ruleset = rs.getString("ruleset");
            template = rs.getString("template");
            name = rs.getString("name");
            inputtype = rs.getString("inputtype");
            inputvalue = rs.getString("inputvalue");

        }
        Assertions.assertEquals("RulesAreMadeToBeBroken", ruleset);
        Assertions.assertEquals("TemplateForBaking", template);
        Assertions.assertEquals("name1", name);
        Assertions.assertEquals("regex", inputtype);
        Assertions.assertEquals("YourNormalRegex", inputvalue);
    }
}
