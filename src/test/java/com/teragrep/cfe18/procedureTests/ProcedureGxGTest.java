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
Tests for host group x capture group procedure
Uses dataset procedureGxGTestData.xml which is copied version of triggerTestData.xml
 */
public class ProcedureGxGTest extends DBUnitbase {

    public ProcedureGxGTest(String name) {
        super(name);
    }

    @Override
    protected IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder().build(Files.newInputStream(Paths.get("src/test/resources/XMLProcedureGxG/procedureGxGTestData.xml")));
    }

    /*
    Testataan että linkitys onnistuu capture groupin ja host groupin välillä
     */
    public void testProcedureAddLinkageSuccess() throws Exception {

        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/XMLProcedureGxG/procedureGxGTestDataExpected1.xml"));
        ITable expectedTable1 = expectedDataSet.getTable("cfe_18.host_groups_x_capture_def_group");

        CallableStatement stmnt = conn.prepareCall("{call cfe_18.add_g_x_g(?,?)}");
        stmnt.setInt(1, 1); //Host group id
        stmnt.setInt(2, 6); //Capture group id
        stmnt.execute();

        ITable actualTable1 = getConnection().createQueryTable("result", "select * from cfe_18.host_groups_x_capture_def_group");

        Assertion.assertEqualsIgnoreCols(expectedTable1, actualTable1, new String[]{"id"});

    }

    /*
    Testataan että linkitys ei onnistu jos host_group ei ole olemassa
     */
    public void testProcedureGxGNoHost() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{call cfe_18.add_g_x_g(?,?)}");
            stmnt.setInt(1, 100);
            stmnt.setInt(2, 2);
            stmnt.execute();
        });
        Assertions.assertEquals("45000", state.getSQLState());
    }

    /*
    Testataan että linkitys ei onnistu jos capture_group ei ole olemassa
     */
    public void testProcedureGxGNoCapture() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);
        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{call cfe_18.add_g_x_g(?,?)}");
            stmnt.setInt(1, 1);
            stmnt.setInt(2, 200);
            stmnt.execute();
        });
        Assertions.assertEquals("45000", state.getSQLState());
    }

    /*
    Testataan että tieto voidaan kerätä oikeassa muodossa käyttäen capture_group nimeä
 */

    public void testProcedureGxGRetrieveByCapture() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        List<String> capture_name = new ArrayList<>();
        List<String> host_name = new ArrayList<>();
        CallableStatement stmnt = conn.prepareCall("{CALL cfe_18.retrieve_g_x_g_details(?)}");
        stmnt.setString(1, "capturegroup1");
        ResultSet rs = stmnt.executeQuery();
        while (rs.next()) {
            capture_name.add(rs.getString("capture_name"));
            host_name.add(rs.getString("host_name"));
            int gxg_id = rs.getInt("g_x_g_id");
            String host_type = rs.getString("host_type");
            String capture_type = rs.getString("capture_type");
            Assertions.assertEquals(1, gxg_id);
            Assertions.assertEquals("cfe", host_type);
            Assertions.assertEquals("cfe", capture_type);
        }
        Assertions.assertEquals(Arrays.asList("capturegroup1"), capture_name);
        Assertions.assertEquals(Arrays.asList("host_group_1"), host_name);
    }


    /*
    Testataan että tieto voidaan kerätä oikeassa muodossa käyttäen host_group nimeä

     */
    public void testProcedureGxGRetrieveByHost() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        List<String> capture_name = new ArrayList<>();
        List<String> host_name = new ArrayList<>();

        CallableStatement stmnt = conn.prepareCall("{CALL cfe_18.retrieve_g_x_g_details(?)}");
        stmnt.setString(1, "host_group_1");
        ResultSet rs = stmnt.executeQuery();
        while (rs.next()) {
            capture_name.add(rs.getString("capture_name"));
            host_name.add(rs.getString("host_name"));
            int gxg_id = rs.getInt("g_x_g_id");
            String host_type = rs.getString("host_type");
            String capture_type = rs.getString("capture_type");
            Assertions.assertEquals(1, gxg_id);
            Assertions.assertEquals("cfe", host_type);
            Assertions.assertEquals("cfe", capture_type);
        }
        Assertions.assertEquals(Arrays.asList("capturegroup1"), capture_name);
        Assertions.assertEquals(Arrays.asList("host_group_1"), host_name);
    }

    /*
    Testataan että linkitys ei onnistu jos capture type ei matchaa host typen kanssa
    Custom error = '42000', 'type mismatch between host group and cature group'.
     */
    public void testProcedureGxGTypeMismatch() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{CALL cfe_18.add_g_x_g(?,?)}");
            stmnt.setInt(1, 1);
            stmnt.setInt(2, 7);
            stmnt.execute();
        });

        Assertions.assertEquals("45000", state.getSQLState());
    }
}