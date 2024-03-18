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
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProcedureStorageTest extends DBUnitbase {
    public ProcedureStorageTest(String name) {
        super(name);
    }

    @Override
    protected IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder().build(Files.newInputStream(Paths.get("src/test/resources/XMLProcedureStorage/procedureStorageData.xml")));
    }


    /*
    -Check that flow exists when inserting storage with flow
    -Takes flow,storage_type and target_name
    -Throws 42000 when not existing
    */
    public void testStorageFlowExistence() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{CALL flow.add_storage(?,?)}");
            stmnt.setString(1, "FlowThatDontExist");
            stmnt.setInt(2, 2);
            stmnt.execute();

        });
        Assertions.assertEquals("45000", state.getSQLState());
    }


    /*
    -Check that storage can be inserted via flow
    -Takes flow,storage_type and target_name
    -Returns ID, not important information tho
  */

    public void testStorageFlowAccept() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/XMLProcedureStorage/procedureStorageExpectedTestData1.xml"));

        ITable expectedTable = expectedDataSet.getTable("flow.flow_targets");

        CallableStatement stmnt = conn.prepareCall("CALL flow.add_storage(?,?)");
        stmnt.setString(1, "flow");
        stmnt.setInt(2, 2);
        stmnt.execute();

        ITable actualTable = getConnection().createQueryTable("result", "select * from flow.flow_targets");

        Assertion.assertEquals(expectedTable, actualTable);

    }

    /*
    -Check that storage can be linked to capture with proper values
    -Takes capture_id and storage_id
    */
    public void testStorageCaptureLinkage() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/XMLProcedureStorage/procedureStorageExpectedTestData2.xml"));

        ITable expectedTable = expectedDataSet.getTable("cfe_18.capture_def_x_flow_targets");

        CallableStatement stmnt = conn.prepareCall("CALL flow.add_storage_for_capture(?,?)");
        stmnt.setInt(1, 1);
        stmnt.setInt(2, 2);
        stmnt.execute();

        ITable actualTable = getConnection().createQueryTable("result", "select * from cfe_18.capture_def_x_flow_targets");

        Assertion.assertEquals(expectedTable, actualTable);
    }

    /*
    -Check if storage exists when linking storage to capture.
    -Takes capture_id and storage_id
    -Throws 42000 when not existing
     */
    public void testStorageMissingStorageLinkage() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{CALL flow.add_storage_for_capture(?,?)}");
            stmnt.setInt(1, 1);
            stmnt.setInt(2, 40);
            stmnt.execute();

        });
        Assertions.assertEquals("23000", state.getSQLState());
    }

    /*
    -Check that capture can not be linked to indifferent flow storage
    -Takes capture_id and storage_id
    -Throws 45000 when indifferent
  */
    public void testStorageDifferentFlowLinkage() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{CALL flow.add_storage_for_capture(?,?)}");
            stmnt.setInt(1, 1);
            stmnt.setInt(2, 7);
            stmnt.execute();

        });
        Assertions.assertEquals("23000", state.getSQLState());
    }

    /*
    -Check if capture exists when linking storage
    -Takes capture_id and storage_id
    -Throws 45000 when capture is missing
     */
    public void testStorageMissingCaptureLinkage() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{CALL flow.add_storage_for_capture(?,?)}");
            stmnt.setInt(1, 500);
            stmnt.setInt(2, 1);
            stmnt.execute();

        });
        Assertions.assertEquals("42000", state.getSQLState());
    }
}
