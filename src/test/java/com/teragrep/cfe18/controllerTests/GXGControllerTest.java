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
package com.teragrep.cfe18.controllerTests;

import com.google.gson.Gson;
import com.teragrep.cfe18.handlers.entities.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MigrateDatabaseExtension.class)
public class GXGControllerTest extends TestSpringBootInformation {

    Gson gson = new Gson();

    @LocalServerPort
    private int port;

    @Test
    @Order(1)
    public void testAddCaptureGroup() throws Exception {
        // Sink

        // insert flow before sink so database has something to stick to.
        Flow flow = new Flow();
        flow.setName("testflow1");
        String json2 = gson.toJson(flow);

        // forms the json to requestEntity
        StringEntity requestEntity2 = new StringEntity(
                String.valueOf(json2),
                ContentType.APPLICATION_JSON);

        // Creates the request
        HttpPut request2 = new HttpPut("http://localhost:" + port + "/flow");
        // set requestEntity to the put request
        request2.setEntity(requestEntity2);
        // Header
        request2.setHeader("Authorization", "Bearer " + token);

        HttpClientBuilder.create().build().execute(request2);


        // insert sink

        Sink sink = new Sink();
        sink.setFlow("testflow1");
        sink.setPort("601");
        sink.setIp_address("ip1");
        sink.setProtocol("prot");

        String json = gson.toJson(sink);

        // forms the json to requestEntity
        StringEntity requestEntity = new StringEntity(
                String.valueOf(json),
                ContentType.APPLICATION_JSON);

        // Creates the request
        HttpPut request = new HttpPut("http://localhost:" + port + "/sink/details");
        // set requestEntity to the put request
        request.setEntity(requestEntity);
        // Header
        request.setHeader("Authorization", "Bearer " + token);

        // Get the response from endpoint
        HttpClientBuilder.create().build().execute(request);

        // Capture

        CaptureRelp captureRelp = new CaptureRelp();
        captureRelp.setTag("relpTag");
        captureRelp.setRetention_time("P30D");
        captureRelp.setCategory("audit");
        captureRelp.setApplication("relp");
        captureRelp.setIndex("audit_relp");
        captureRelp.setSource_type("relpsource1");
        captureRelp.setProtocol("prot");
        captureRelp.setFlow("testflow1");

        String relpJson = gson.toJson(captureRelp);

        // forms the json to requestEntity
        StringEntity requestEntityCapture = new StringEntity(
                String.valueOf(relpJson),
                ContentType.APPLICATION_JSON);

        // Creates the request
        HttpPut requestCapture = new HttpPut("http://localhost:" + port + "/capture/relp");
        // set requestEntity to the put request
        requestCapture.setEntity(requestEntityCapture);
        // Header
        requestCapture.setHeader("Authorization", "Bearer " + token);

        // Get the response from endpoint
        HttpClientBuilder.create().build().execute(requestCapture);

        // Capture Group
        CaptureGroup captureGroup = new CaptureGroup();
        captureGroup.setCapture_def_group_name("groupRelp");
        captureGroup.setCapture_definition_id(1);

        String cgJson = gson.toJson(captureGroup);

        // forms the json to requestEntity
        StringEntity requestEntityCaptureGroup = new StringEntity(
                String.valueOf(cgJson),
                ContentType.APPLICATION_JSON);

        // Creates the request
        HttpPut requestCaptureGroup = new HttpPut("http://localhost:" + port + "/capture/group");
        // set requestEntity to the put request
        requestCaptureGroup.setEntity(requestEntityCaptureGroup);
        // Header
        requestCaptureGroup.setHeader("Authorization", "Bearer " + token);

        // Get the response from endpoint
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(requestCaptureGroup);

        // Assertion

        // Get the entity from response
        HttpEntity entity = httpResponse.getEntity();

        // Entity response string
        String responseString = EntityUtils.toString(entity);

        // Parsin respponse as JSONObject
        JSONObject responseAsJson = new JSONObject(responseString);

        // Creating expected message as JSON Object from the data that was sent towards endpoint
        String expected = "New capture group created with name = groupRelp";

        // Creating string from Json that was given as a response
        String actual = responseAsJson.get("message").toString();

        // Assertions
        assertThat(
                httpResponse.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_CREATED));
        assertEquals(expected, actual);


    }


    @Test
    @Order(2)
    public void testAddHostGroup() throws Exception {
        // Host
        HostRelp relpHost = new HostRelp();
        relpHost.setMd5("relpHostmd5");
        relpHost.setFqHost("relpHostfq");

        String json = gson.toJson(relpHost);

        // forms the json to requestEntity
        StringEntity requestEntity = new StringEntity(
                String.valueOf(json),
                ContentType.APPLICATION_JSON);

        // Creates the request
        HttpPut request = new HttpPut("http://localhost:" + port + "/host/relp");
        // set requestEntity to the put request
        request.setEntity(requestEntity);
        // Header
        request.setHeader("Authorization", "Bearer " + token);

        // Get the response from endpoint
        HttpClientBuilder.create().build().execute(request);


        // Host Group
        HostGroup relpHostGroup = new HostGroup();
        relpHostGroup.setHost_id(1);
        relpHostGroup.setHost_group_name("hostgroup1");

        String jsonGroup = gson.toJson(relpHostGroup);

        // forms the json to requestEntity
        StringEntity requestEntityGroup = new StringEntity(
                String.valueOf(jsonGroup),
                ContentType.APPLICATION_JSON);

        // Creates the request
        HttpPut requestGroup = new HttpPut("http://localhost:" + port + "/host/group");
        // set requestEntity to the put request
        requestGroup.setEntity(requestEntityGroup);
        // Header
        requestGroup.setHeader("Authorization", "Bearer " + token);

        // Get the response from endpoint
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(requestGroup);


        // Assertion


        // Get the entity from response
        HttpEntity entity = httpResponse.getEntity();

        // Entity response string
        String responseString = EntityUtils.toString(entity);


        // Parsin respponse as JSONObject
        JSONObject responseAsJson = new JSONObject(responseString);

        // Creating expected message as JSON Object from the data that was sent towards endpoint
        String expected = "New host group created with name = hostgroup1";

        // Creating string from Json that was given as a response
        String actual = responseAsJson.get("message").toString();

        // Assertions
        assertEquals(expected, actual);
        assertThat(
                httpResponse.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_CREATED));

    }

    @Test
    @Order(3)
    public void testAddLinkage() throws Exception {
        // Linkage
        Linkage linkage = new Linkage();
        linkage.setCapture_group_id(1);
        linkage.setHost_group_id(1);

        String jsonGroup = gson.toJson(linkage);

        // forms the json to requestEntity
        StringEntity requestEntityGroup = new StringEntity(
                String.valueOf(jsonGroup),
                ContentType.APPLICATION_JSON);

        // Creates the request
        HttpPut requestGroup = new HttpPut("http://localhost:" + port + "/capture/groups/linkage");
        // set requestEntity to the put request
        requestGroup.setEntity(requestEntityGroup);
        // Header
        requestGroup.setHeader("Authorization", "Bearer " + token);

        // Get the response from endpoint
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(requestGroup);


        // Assertion


        // Get the entity from response
        HttpEntity entity = httpResponse.getEntity();

        // Entity response string
        String responseString = EntityUtils.toString(entity);


        // Parsin respponse as JSONObject
        JSONObject responseAsJson = new JSONObject(responseString);

        // Creating expected message as JSON Object from the data that was sent towards endpoint
        String expected = "New linkage created for groups = groupRelp and hostgroup1";

        // Creating string from Json that was given as a response
        String actual = responseAsJson.get("message").toString();

        // Assertions
        assertEquals(expected, actual);
        assertThat(
                httpResponse.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_CREATED));


    }

    @Test
    @Order(4)
    public void testRetrieveCaptureGroup() throws Exception {
        ArrayList<CaptureGroup> expected = new ArrayList<>();
        CaptureGroup captureGroup = new CaptureGroup();
        captureGroup.setCapture_group_type(CaptureGroup.group_type.relp);
        captureGroup.setId(1);
        captureGroup.setCapture_definition_id(1);
        captureGroup.setCapture_def_group_name("groupRelp");

        expected.add(captureGroup);

        String expectedJson = new Gson().toJson(expected);

        // Asserting get request
        HttpGet requestGet = new HttpGet("http://localhost:" + port + "/capture/group/groupRelp");

        requestGet.setHeader("Authorization", "Bearer " + token);

        HttpResponse responseGet = HttpClientBuilder.create().build().execute(requestGet);

        HttpEntity entityGet = responseGet.getEntity();

        String responseStringGet = EntityUtils.toString(entityGet, "UTF-8");

        assertThat(responseGet.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        assertEquals(expectedJson, responseStringGet);

    }

    @Test
    @Order(5)
    public void testRetrieveHostGroup() throws Exception {
        ArrayList<HostGroup> expected = new ArrayList<>();
        HostGroup hostGroup = new HostGroup();
        hostGroup.setMd5("relpHostmd5");
        hostGroup.setHost_id(1);
        hostGroup.setHost_group_name("hostgroup1");
        hostGroup.setHost_group_type(HostGroup.group_type.relp);
        hostGroup.setId(1);

        expected.add(hostGroup);

        String expectedJson = new Gson().toJson(expected);

        // Asserting get request
        HttpGet requestGet = new HttpGet("http://localhost:" + port + "/host/group/hostgroup1");

        requestGet.setHeader("Authorization", "Bearer " + token);

        HttpResponse responseGet = HttpClientBuilder.create().build().execute(requestGet);

        HttpEntity entityGet = responseGet.getEntity();

        String responseStringGet = EntityUtils.toString(entityGet, "UTF-8");

        assertThat(responseGet.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        assertEquals(expectedJson, responseStringGet);

    }

    @Test
    @Order(6)
    public void testRetrieveLinkage() throws Exception {
        ArrayList<Linkage> expected = new ArrayList<>();
        Linkage linkage = new Linkage();
        linkage.setId(1);
        linkage.setCapture_group_name("groupRelp");
        linkage.setHost_group_name("hostgroup1");
        linkage.setHost_group_type(Linkage.group_type.relp);
        linkage.setCapture_group_type(Linkage.group_type.relp);
        linkage.setHost_group_id(1);
        linkage.setCapture_group_id(1);

        expected.add(linkage);

        String expectedJson = new Gson().toJson(expected);

        // Asserting get request
        HttpGet requestGet = new HttpGet("http://localhost:" + port + "/capture/groups/linkage/hostgroup1");

        requestGet.setHeader("Authorization", "Bearer " + token);

        HttpResponse responseGet = HttpClientBuilder.create().build().execute(requestGet);

        HttpEntity entityGet = responseGet.getEntity();

        String responseStringGet = EntityUtils.toString(entityGet, "UTF-8");

        assertThat(responseGet.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        assertEquals(expectedJson, responseStringGet);

    }


    @Test
    @Order(7)
    public void testRetrieveAllCaptureGroups() throws Exception {
        ArrayList<CaptureGroup> expected = new ArrayList<>();
        CaptureGroup captureGroup = new CaptureGroup();
        captureGroup.setCapture_def_group_name("groupRelp");
        captureGroup.setCapture_group_type(CaptureGroup.group_type.relp);
        captureGroup.setTag("relpTag");
        captureGroup.setCapture_definition_id(1);

        expected.add(captureGroup);

        String expectedJson = new Gson().toJson(expected);

        // Asserting get request
        HttpGet requestGet = new HttpGet("http://localhost:" + port + "/capture/group");

        requestGet.setHeader("Authorization", "Bearer " + token);

        HttpResponse responseGet = HttpClientBuilder.create().build().execute(requestGet);

        HttpEntity entityGet = responseGet.getEntity();

        String responseStringGet = EntityUtils.toString(entityGet, "UTF-8");

        assertThat(responseGet.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        assertEquals(expectedJson, responseStringGet);

    }


    @Test
    @Order(8)
    public void testRetrieveAllHostGroups() throws Exception {
        ArrayList<HostGroup> expected = new ArrayList<>();
        HostGroup hostGroup = new HostGroup();
        hostGroup.setMd5("relpHostmd5");
        hostGroup.setHost_id(1);
        hostGroup.setHost_group_name("hostgroup1");
        hostGroup.setHost_group_type(HostGroup.group_type.relp);
        hostGroup.setId(1);

        expected.add(hostGroup);

        String expectedJson = new Gson().toJson(expected);

        // Asserting get request
        HttpGet requestGet = new HttpGet("http://localhost:" + port + "/host/group");

        requestGet.setHeader("Authorization", "Bearer " + token);

        HttpResponse responseGet = HttpClientBuilder.create().build().execute(requestGet);

        HttpEntity entityGet = responseGet.getEntity();

        String responseStringGet = EntityUtils.toString(entityGet, "UTF-8");

        assertEquals(expectedJson, responseStringGet);
        assertThat(responseGet.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));

    }

    @Test
    @Order(9)
    public void testRetrieveAllLinkages() throws Exception {

        ArrayList<Linkage> expected = new ArrayList<>();
        Linkage linkage = new Linkage();
        linkage.setId(1);
        linkage.setCapture_group_name("groupRelp");
        linkage.setHost_group_name("hostgroup1");
        linkage.setHost_group_type(Linkage.group_type.relp);
        linkage.setCapture_group_type(Linkage.group_type.relp);
        linkage.setHost_group_id(1);
        linkage.setCapture_group_id(1);

        expected.add(linkage);

        String expectedJson = new Gson().toJson(expected);

        // Asserting get request
        HttpGet requestGet = new HttpGet("http://localhost:" + port + "/capture/groups/linkage");

        requestGet.setHeader("Authorization", "Bearer " + token);

        HttpResponse responseGet = HttpClientBuilder.create().build().execute(requestGet);

        HttpEntity entityGet = responseGet.getEntity();

        String responseStringGet = EntityUtils.toString(entityGet, "UTF-8");

        assertThat(responseGet.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        assertEquals(expectedJson, responseStringGet);

    }

    // Delete

    @Test
    @Order(10)
    public void testDeleteCaptureGroupInUse() throws Exception {
        //groupRelp
        HttpDelete delete = new HttpDelete("http://localhost:" + port + "/capture/group/groupRelp");

        // Header
        delete.setHeader("Authorization", "Bearer " + token);

        HttpResponse deleteResponse = HttpClientBuilder.create().build().execute(delete);

        HttpEntity entityDelete = deleteResponse.getEntity();

        String responseStringGet = EntityUtils.toString(entityDelete, "UTF-8");
        // Parsin respponse as JSONObject
        JSONObject responseAsJson = new JSONObject(responseStringGet);

        // Creating string from Json that was given as a response
        String actual = responseAsJson.get("message").toString();

        // Creating expected message as JSON Object from the data that was sent towards endpoint
        String expected = "Is in use";

        assertEquals(expected, actual);
        assertThat(deleteResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_BAD_REQUEST));
    }


    @Test
    @Order(11)
    public void testDeleteNonExistentCaptureGroup() throws Exception {
        HttpDelete delete = new HttpDelete("http://localhost:" + port + "/capture/group/125412");

        // Header
        delete.setHeader("Authorization", "Bearer " + token);

        HttpResponse deleteResponse = HttpClientBuilder.create().build().execute(delete);

        HttpEntity entityDelete = deleteResponse.getEntity();

        String responseStringGet = EntityUtils.toString(entityDelete, "UTF-8");

        // Parsin respponse as JSONObject
        JSONObject responseAsJson = new JSONObject(responseStringGet);

        // Creating string from Json that was given as a response
        String actual = responseAsJson.get("message").toString();
        // Creating expected message as JSON Object from the data that was sent towards endpoint
        String expected = "Record does not exist";

        assertThat(deleteResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_BAD_REQUEST));
        assertEquals(expected, actual);
    }


    @Test
    @Order(12)
    public void testDeleteHostGroupInUse() throws Exception {
        // hostgroup1
        HttpDelete delete = new HttpDelete("http://localhost:" + port + "/host/group/hostgroup1");

        // Header
        delete.setHeader("Authorization", "Bearer " + token);

        HttpResponse deleteResponse = HttpClientBuilder.create().build().execute(delete);

        HttpEntity entityDelete = deleteResponse.getEntity();

        String responseStringGet = EntityUtils.toString(entityDelete, "UTF-8");

        // Parsin respponse as JSONObject
        JSONObject responseAsJson = new JSONObject(responseStringGet);

        // Creating string from Json that was given as a response
        String actual = responseAsJson.get("message").toString();
        // Creating expected message as JSON Object from the data that was sent towards endpoint
        String expected = "Is in use";

        assertEquals(expected, actual);
        assertThat(deleteResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_BAD_REQUEST));
    }

    @Test
    @Order(13)
    public void testDeleteNonExistentHostGroup() throws Exception {
        HttpDelete delete = new HttpDelete("http://localhost:" + port + "/host/group/124");

        // Header
        delete.setHeader("Authorization", "Bearer " + token);

        HttpResponse deleteResponse = HttpClientBuilder.create().build().execute(delete);

        HttpEntity entityDelete = deleteResponse.getEntity();

        String responseStringGet = EntityUtils.toString(entityDelete, "UTF-8");

        // Parsin respponse as JSONObject
        JSONObject responseAsJson = new JSONObject(responseStringGet);

        // Creating string from Json that was given as a response
        String actual = responseAsJson.get("message").toString();

        // Creating expected message as JSON Object from the data that was sent towards endpoint
        String expected = "Record does not exist";

        assertThat(deleteResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_BAD_REQUEST));
        assertEquals(expected, actual);
    }


    @Test
    @Order(14)
    public void testDeleteNonExistentLinkage() throws Exception {
        HttpDelete delete = new HttpDelete("http://localhost:" + port + "/capture/groups/linkage/125");

        // Header
        delete.setHeader("Authorization", "Bearer " + token);

        HttpResponse deleteResponse = HttpClientBuilder.create().build().execute(delete);

        HttpEntity entityDelete = deleteResponse.getEntity();

        String responseStringGet = EntityUtils.toString(entityDelete, "UTF-8");

        // Parsin respponse as JSONObject
        JSONObject responseAsJson = new JSONObject(responseStringGet);

        // Creating string from Json that was given as a response
        String actual = responseAsJson.get("message").toString();

        // Creating expected message as JSON Object from the data that was sent towards endpoint
        String expected = "Record does not exist";

        assertThat(deleteResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_BAD_REQUEST));
        assertEquals(expected, actual);
    }

    @Test
    @Order(15)
    public void testDeleteLinkage() throws Exception {
        HttpDelete delete = new HttpDelete("http://localhost:" + port + "/capture/groups/linkage/1");

        // Header
        delete.setHeader("Authorization", "Bearer " + token);

        HttpResponse deleteResponse = HttpClientBuilder.create().build().execute(delete);

        HttpEntity entityDelete = deleteResponse.getEntity();

        String responseStringGet = EntityUtils.toString(entityDelete, "UTF-8");

        // Parsin respponse as JSONObject
        JSONObject responseAsJson = new JSONObject(responseStringGet);

        // Creating string from Json that was given as a response
        String actual = responseAsJson.get("message").toString();

        // Creating expected message as JSON Object from the data that was sent towards endpoint
        String expected = "Linkage with id = 1 deleted.";

        assertThat(deleteResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        assertEquals(expected, actual);
    }

    @Test
    @Order(16)
    public void testDeleteCaptureGroup() throws Exception {
        //groupRelp
        HttpDelete delete = new HttpDelete("http://localhost:" + port + "/capture/group/groupRelp");

        // Header
        delete.setHeader("Authorization", "Bearer " + token);

        HttpResponse deleteResponse = HttpClientBuilder.create().build().execute(delete);

        HttpEntity entityDelete = deleteResponse.getEntity();

        String responseStringGet = EntityUtils.toString(entityDelete, "UTF-8");

        // Parsin respponse as JSONObject
        JSONObject responseAsJson = new JSONObject(responseStringGet);

        // Creating string from Json that was given as a response
        String actual = responseAsJson.get("message").toString();

        // Creating expected message as JSON Object from the data that was sent towards endpoint
        String expected = "Capture group groupRelp deleted.";

        assertEquals(expected, actual);
        assertThat(deleteResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    }

    @Test
    @Order(17)
    public void testDeleteHostGroup() throws Exception {
        //groupRelp
        HttpDelete delete = new HttpDelete("http://localhost:" + port + "/host/group/hostgroup1");

        // Header
        delete.setHeader("Authorization", "Bearer " + token);

        HttpResponse deleteResponse = HttpClientBuilder.create().build().execute(delete);

        HttpEntity entityDelete = deleteResponse.getEntity();

        String responseStringGet = EntityUtils.toString(entityDelete, "UTF-8");

        // Parsin respponse as JSONObject
        JSONObject responseAsJson = new JSONObject(responseStringGet);

        // Creating string from Json that was given as a response
        String actual = responseAsJson.get("message").toString();

        // Creating expected message as JSON Object from the data that was sent towards endpoint
        String expected = "Host Group hostgroup1 deleted.";

        assertThat(deleteResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        assertEquals(expected, actual);
    }

}
