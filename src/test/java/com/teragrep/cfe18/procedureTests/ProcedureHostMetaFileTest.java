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


public class ProcedureHostMetaFileTest extends DBUnitbase {

    public ProcedureHostMetaFileTest(String name) {
        super(name);
    }

    @Override
    protected IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder().build(Files.newInputStream(Paths.get("src/test/resources/XMLProcedureHostMeta/procedureHostMetaTestData.xml")));
    }

    /*
    Testi millä katsotaan IP n lisäys host_metaan mukaan.
     */
    public void testProcedureAddIpToHostMeta() throws Exception {

        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/XMLProcedureHostMeta/procedureHostMetaTestDataExpected1.xml"));
        ITable expectedTable1 = expectedDataSet.getTable("cfe_03.ip_addresses");
        ITable expectedTable2 = expectedDataSet.getTable("cfe_03.host_meta_x_ip");

        CallableStatement stmnt = conn.prepareCall("{CALL cfe_03.add_ip_address(?,?)}");
        stmnt.setInt(1, 1);
        stmnt.setString(2, "ipaddress1");
        stmnt.execute();

        ITable actualTable1 = getConnection().createQueryTable("result", "select * from cfe_03.ip_addresses");
        ITable actualTable2 = getConnection().createQueryTable("result", "select * from cfe_03.host_meta_x_ip");

        Assertion.assertEquals(expectedTable1, actualTable1);
        Assertion.assertEquals(expectedTable2, actualTable2);
    }


    /*
    Testi millä katsotaan onko host_meta_id validi jos IP tä lisätään
     palauttaa 42000 error
     */
    public void testHostMetaValidityOnIp() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{CALL cfe_03.add_ip_address(?,?)}");
            stmnt.setInt(1, 1000);
            stmnt.setString(2, "ip1");
            stmnt.execute();
        });
        Assertions.assertEquals("45000", state.getSQLState());
    }

    /*
   Testi millä katsotaan onko host_meta_id validi jos Interfacea lisätään
    palauttaa 42000 error
    */
    public void testHostMetaValidityOnInterface() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{CALL cfe_03.add_interface(?,?)}");
            stmnt.setInt(2, 1000);
            stmnt.setString(1, "ens192");
            stmnt.execute();
        });
        Assertions.assertEquals("45000", state.getSQLState());
    }

    /*
    Testi millä katsotaan interfacen lisäys host metan mukaan.
    */
    public void testProcedureAddInterfaceToHostMeta() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/XMLProcedureHostMeta/procedureHostMetaTestDataExpected2.xml"));
        ITable expectedTable1 = expectedDataSet.getTable("cfe_03.interfaces");
        ITable expectedTable2 = expectedDataSet.getTable("cfe_03.host_meta_x_interface");

        CallableStatement stmnt = conn.prepareCall("{CALL cfe_03.add_interface(?,?)}");
        stmnt.setString(1, "interface1");
        stmnt.setInt(2, 1);
        stmnt.execute();

        ITable actualTable1 = getConnection().createQueryTable("result", "select * from cfe_03.interfaces");
        ITable actualTable2 = getConnection().createQueryTable("result", "select * from cfe_03.host_meta_x_interface");

        Assertion.assertEquals(expectedTable1, actualTable1);
        Assertion.assertEquals(expectedTable2, actualTable2);
    }


    /*
    Testi millä katsotaan onnistuuko host_metan lisääminen hostiin.
 */
    public void testHostMetaWithHost() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new File("src/test/resources/XMLProcedureHostMeta/procedureHostMetaTestDataExpected3.xml"));
        ITable expectedTable2 = expectedDataSet.getTable("cfe_03.host_meta");
        // käytetään aiempia arvoja mitä on jo testidatassa. Halutaan vain nähdä syntyykö uusi host meta hostille jolla ei ole host metaa vielä.
        CallableStatement stmnt = conn.prepareCall("{CALL cfe_03.add_host_meta_data(?,?,?,?,?,?)}");
        stmnt.setString(1, "arch1");
        stmnt.setString(2, "flavor1");
        stmnt.setString(3, "host3");
        stmnt.setInt(4, 4); // host id
        stmnt.setString(5, "Linux1");
        stmnt.setString(6, "release_version2");
        stmnt.execute();

        ITable actualTable2 = getConnection().createQueryTable("result", "select * from cfe_03.host_meta");

        Assertion.assertEquals(expectedTable2, actualTable2);

    }


    /*
    Testi millä katsotaan onko hostia olemassa mihin host_metaa lisätään.
    palauttaa 42000 jos hostia ei ole olemassa
     */
    public void testHostExistenceOnHostMeta() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        SQLException state = Assertions.assertThrows(SQLException.class, () -> {
            CallableStatement stmnt = conn.prepareCall("{CALL cfe_03.add_host_meta_data(?,?,?,?,?,?)}");
            stmnt.setString(1, "arch1");
            stmnt.setString(2, "flavor1");
            stmnt.setString(3, "host1");
            stmnt.setInt(4, 10000); // host id
            stmnt.setString(5, "Linux1");
            stmnt.setString(6, "release_version1");
            stmnt.execute();
        });

        Assertions.assertEquals("45000", state.getSQLState());
    }

    /*
    Testi millä katsotaan onnistuuko host metan palautus oikeilla arvoilla.
    */
    public void testHostMetaRetrieve() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        List<String> IpList = new ArrayList<>();
        List<String> InterfaceList = new ArrayList<>();
        CallableStatement stmnt = conn.prepareCall("{CALL cfe_03.retrieve_host_meta(?)}");
        stmnt.setInt(1, 1);
        ResultSet rs = stmnt.executeQuery();
        while (rs.next()) {
            IpList.add(rs.getString("ip_address"));
            InterfaceList.add(rs.getString("interface"));
            int host_meta_id = rs.getInt("host_meta_id");
            String arch = rs.getString("arch");
            String release_version = rs.getString("release_version");
            String flavor = rs.getString("flavor");
            String os = rs.getString("os");
            String hostname = rs.getString("hostname");
            Assertions.assertEquals(host_meta_id, 1);
            Assertions.assertEquals(arch, "arch1");
            Assertions.assertEquals(release_version, "release_version1");
            Assertions.assertEquals(flavor, "flavor1");
            Assertions.assertEquals(os, "Linux1");
            Assertions.assertEquals(hostname, "host1");

        }
        Assertions.assertEquals(Arrays.asList("ip1", "ip2", "ip3", "ip1", "ip2", "ip3"), IpList);
        Assertions.assertEquals(Arrays.asList("ens192", "ens192", "ens192", "ens256", "ens256", "ens256"), InterfaceList);
    }

    /*
    Testi millä tarkastetaan cfe hostin palautus missä host_meta_id tulee mukana. Testidatan vuoksi hostin testi täälä.
*/
    public void testProcedureRetrieveCfeHost() throws Exception {
        Connection conn = DriverManager.getConnection(this.DBUNIT_CONNECTION_URL + "?" + "user=" + this.DBUNIT_USERNAME + "&password=" + this.DBUNIT_PASSWORD);

        CallableStatement stmnt = conn.prepareCall("{CALL cfe_00.retrieve_host_details(?)}");
        stmnt.setInt(1, 2);
        ResultSet rs = stmnt.executeQuery();
        int host_id = 0;
        String md5 = null;
        String fqhost = null;
        String host_type = null;
        int hub_id = 0;
        String hostname = null;
        int host_meta_id = 0;
        String hub_fq = null;
        // roll through the result
        while (rs.next()) {
            host_id = rs.getInt("host_id");
            md5 = rs.getString("host_md5");
            fqhost = rs.getString("host_fq");
            host_type = rs.getString("host_type");
            hub_id = rs.getInt("hub_id");
            hostname = rs.getString("host_name");
            host_meta_id = rs.getInt("host_meta_id");
            hub_fq = rs.getString("hub_fq");

        }
        Assertions.assertEquals(host_id, 2); // host_id
        Assertions.assertEquals(md5, "12322"); // md5
        Assertions.assertEquals(fqhost, "2"); // fqhost
        Assertions.assertEquals(host_type, "cfe"); // host_type
        Assertions.assertEquals(hub_id, 1); // hub_id
        Assertions.assertEquals(hostname, "host1"); // hostname
        Assertions.assertEquals(host_meta_id, 1); // host_meta_id
        Assertions.assertEquals(hub_fq, "1"); // hub_fq

    }
}

