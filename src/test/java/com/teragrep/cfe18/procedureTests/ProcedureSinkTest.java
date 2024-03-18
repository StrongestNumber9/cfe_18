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

public class ProcedureSinkTest extends DBUnitbase {

    public ProcedureSinkTest(String name) {
        super(name);
    }

    @Override
    protected IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder().build(Files.newInputStream(Paths.get("src/test/resources/XMLProcedureSink/procedureSinkData.xml")));
    }

    /*
    This test checks that completely new sink can be added with new protocol and flow.
     */
    public void testSinkAccept() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/XMLProcedureSink/procedureSinkExpectedData1.xml"));
        ITable expectedTable = expectedDataSet.getTable("flow.capture_sink");
        ITable expectedTable1 = expectedDataSet.getTable("flow.L7");
        ITable expectedTable2 = expectedDataSet.getTable("flow.flows");

        CallableStatement stmnt = conn.prepareCall("{CALL flow.add_sink(?,?,?,?)}");
        stmnt.setString(1, "prot3");
        stmnt.setString(2, "ip1");
        stmnt.setString(3, "1234");
        stmnt.setString(4, "flow2");
        stmnt.execute();

        ITable actualTable = getConnection().createQueryTable("result", "select * from flow.capture_sink");
        ITable actualTable1 = getConnection().createQueryTable("result", "select * from flow.L7");
        ITable actualTable2 = getConnection().createQueryTable("result", "select * from flow.flows");

        Assertion.assertEquals(expectedTable, actualTable);
        Assertion.assertEquals(expectedTable1, actualTable1);
        Assertion.assertEquals(expectedTable2, actualTable2);

    }


    /*
    This test is for gathering sink values by id.
    Goal is to receive correct values from retrieve_sink_by_id procedure
     */
    public void testSinkRetrieveById() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        CallableStatement stmnt = conn.prepareCall("{CALL flow.retrieve_sink_by_id(?)}");
        stmnt.setString(1, "1");
        ResultSet rs = stmnt.executeQuery();
        String ip = null;
        String port = null;
        String protocol = null;
        String flow = null;
        while (rs.next()) {
            ip = rs.getString("ip");
            port = rs.getString("port");
            protocol = rs.getString("protocol");
            flow = rs.getString("flow");
        }
        Assertions.assertEquals("ip11", ip);
        Assertions.assertEquals("601", port);
        Assertions.assertEquals("prot1", protocol);
        Assertions.assertEquals("flow2", flow);
    }
}
