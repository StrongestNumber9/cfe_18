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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class TriggerTagTest extends DBUnitbase {

    public TriggerTagTest(String name) {
        super(name);
    }


    @Override
    protected IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder().build(Files.newInputStream(Paths.get("src/test/resources/XMLTriggersHostXCapture/triggerTestData.xml")));
    }

    /*
    This test bounces the trigger indicating that the trigger is in place.
    */
    public void testTagTriggerBounce() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        // Execute the tested code that modify the database here
        // execute statement here
        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            Statement stmnt = conn.createStatement();
            stmnt.addBatch("insert into location.host_group_x_host values(60,3,2,'cfe')");
            stmnt.addBatch("insert into cfe_18.capture_def_group_x_capture_def values(60,1,3,1,'cfe')");
            stmnt.executeBatch();
        });
        Assertions.assertEquals("17001", state.getSQLState());


    }

    /*
    Test for checking that the trigger allows values which do not conflict.
     */
    public void testTagTriggerAccept() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/XMLTriggersHostXCapture/triggerTagExpectedData1.xml"));
        ITable expectedTable = expectedDataSet.getTable("cfe_18.capture_def_group_x_capture_def");

        // Execute the tested code that modify the database here
        // execute statement here
        Statement stmnt = conn.createStatement();
        stmnt.addBatch("insert into cfe_18.capture_def_group(id,capture_def_group_name,capture_type) values(20,'capture_group20','cfe')");
        stmnt.executeBatch();
        stmnt.addBatch("insert into cfe_18.capture_def_group_x_capture_def values(20,5,5,5,'cfe')");
        stmnt.executeBatch();

        // Fetch database data after executing your code
        ITable actualTable = getConnection().createQueryTable("result", "select * from cfe_18.capture_def_group_x_capture_def");

        // Load expected data from an XML dataset
        // Assert actual database table match expected table
        Assertion.assertEquals(expectedTable, actualTable);


    }


}
