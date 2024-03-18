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
public class CaptureControllerTest extends TestSpringBootInformation {

    Gson gson = new Gson();

    @LocalServerPort
    private int port;


    @Test
    @Order(1)
    public void testInsertCfeCapture() throws Exception {
        // Capture needs processing_type and capture_sink before it can be inserted.

        // Filecapturemeta
        FileCaptureMeta file = new FileCaptureMeta();
        file.setInputtype(FileCaptureMeta.InputType.regex);
        file.setInputvalue("capregex");
        file.setRuleset("capruleset");
        file.setName("capname");
        file.setTemplate("regex.moustache");


        String json = gson.toJson(file);


        // forms the json to requestEntity
        StringEntity requestEntity = new StringEntity(
                String.valueOf(json),
                ContentType.APPLICATION_JSON);

        // Creates the request
        HttpPut request = new HttpPut("http://localhost:" + port + "/file/capture/meta/rule");
        // set requestEntity to the put request
        request.setEntity(requestEntity);
        // Header
        request.setHeader("Authorization", "Bearer " + token);

        // Get the response from endpoint
        HttpClientBuilder.create().build().execute(request);

        // add flow and sink

        Flow flow = new Flow();
        flow.setName("capflow");
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
        sink.setFlow("capflow");
        sink.setPort("cap");
        sink.setIp_address("capsink");
        sink.setProtocol("prot");

        String json1 = gson.toJson(sink);

        // forms the json to requestEntity
        StringEntity requestEntity1 = new StringEntity(
                String.valueOf(json1),
                ContentType.APPLICATION_JSON);

        // Creates the request
        HttpPut request1 = new HttpPut("http://localhost:" + port + "/sink/details");
        // set requestEntity to the put request
        request1.setEntity(requestEntity1);
        // Header
        request1.setHeader("Authorization", "Bearer " + token);

        // Get the response from endpoint
        HttpClientBuilder.create().build().execute(request1);

        // add Capture File

        CaptureFile captureFile = new CaptureFile();
        captureFile.setId(1);
        captureFile.setTag("f466e5a4-tagpath1");
        captureFile.setRetention_time("P30D");
        captureFile.setCategory("audit");
        captureFile.setApplication("app1");
        captureFile.setIndex("app1_audit");
        captureFile.setSource_type("sourcetype1");
        captureFile.setProtocol("prot");
        captureFile.setFlow("capflow");
        captureFile.setTag_path("tagpath1");
        captureFile.setCapture_path("capturepath1");
        captureFile.setProcessing_type("capname");

        String jsonFile = gson.toJson(captureFile);

        // forms the json to requestEntity
        StringEntity requestEntity3 = new StringEntity(
                String.valueOf(jsonFile),
                ContentType.APPLICATION_JSON);

        // Creates the request
        HttpPut request3 = new HttpPut("http://localhost:" + port + "/capture/file");
        // set requestEntity to the put request
        request3.setEntity(requestEntity3);
        // Header
        request3.setHeader("Authorization", "Bearer " + token);

        // Get the response from endpoint
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request3);

        // Get the entity from response
        HttpEntity entity = httpResponse.getEntity();

        // Entity response string
        String responseString = EntityUtils.toString(entity);

        // Parsin respponse as JSONObject
        JSONObject responseAsJson = new JSONObject(responseString);

        // Creating expected message as JSON Object from the data that was sent towards endpoint
        String expected = "New capture created";

        // Creating string from Json that was given as a response
        String actual = responseAsJson.get("message").toString();

        // Assertions
        assertThat(
                httpResponse.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_CREATED));
        assertEquals(expected, actual);
    }


    // insert relp type capture test
    @Test
    @Order(2)
    public void testInsertRelpCapture() throws Exception {
        CaptureRelp captureRelp = new CaptureRelp();
        captureRelp.setTag("relpTag");
        captureRelp.setRetention_time("P30D");
        captureRelp.setCategory("audit");
        captureRelp.setApplication("relp");
        captureRelp.setIndex("audit_relp");
        captureRelp.setSource_type("relpsource1");
        captureRelp.setProtocol("prot");
        captureRelp.setFlow("capFlow");

        String jsonFile = gson.toJson(captureRelp);

        // forms the json to requestEntity
        StringEntity requestEntity3 = new StringEntity(
                String.valueOf(jsonFile),
                ContentType.APPLICATION_JSON);

        // Creates the request
        HttpPut request3 = new HttpPut("http://localhost:" + port + "/capture/relp");
        // set requestEntity to the put request
        request3.setEntity(requestEntity3);
        // Header
        request3.setHeader("Authorization", "Bearer " + token);

        // Get the response from endpoint
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request3);

        // Get the entity from response
        HttpEntity entity = httpResponse.getEntity();

        // Entity response string
        String responseString = EntityUtils.toString(entity);

        // Parsin respponse as JSONObject
        JSONObject responseAsJson = new JSONObject(responseString);

        // Creating expected message as JSON Object from the data that was sent towards endpoint
        String expected = "New capture created";

        // Creating string from Json that was given as a response
        String actual = responseAsJson.get("message").toString();

        // Assertions
        assertThat(
                httpResponse.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_CREATED));
        assertEquals(expected, actual);

    }


    @Test
    @Order(3)
    public void testGetRelpCapture() throws Exception {


        CaptureRelp captureRelp2 = new CaptureRelp();
        captureRelp2.setId(2);
        captureRelp2.setTag("relpTag");
        captureRelp2.setRetention_time("P30D");
        captureRelp2.setCategory("audit");
        captureRelp2.setApplication("relp");
        captureRelp2.setIndex("audit_relp");
        captureRelp2.setSource_type("relpsource1");
        captureRelp2.setProtocol("prot");
        captureRelp2.setFlow("capflow");
        captureRelp2.setCaptureType(CaptureRelp.CaptureType.relp);

        String json = gson.toJson(captureRelp2);

        // Asserting get request                                            // relp id
        HttpGet requestGet = new HttpGet("http://localhost:" + port + "/capture/relp/" + 2);

        requestGet.setHeader("Authorization", "Bearer " + token);

        HttpResponse responseGet = HttpClientBuilder.create().build().execute(requestGet);

        HttpEntity entityGet = responseGet.getEntity();

        String responseStringGet = EntityUtils.toString(entityGet, "UTF-8");


        assertEquals(json, responseStringGet);
        assertThat(responseGet.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    }

    @Test
    @Order(4)
    public void testGetCfeCapture() throws Exception {
        CaptureFile captureFile = new CaptureFile();
        captureFile.setId(1);
        captureFile.setTag("f466e5a4-tagpath1");
        captureFile.setRetention_time("P30D");
        captureFile.setCategory("audit");
        captureFile.setApplication("app1");
        captureFile.setIndex("app1_audit");
        captureFile.setSource_type("sourcetype1");
        captureFile.setProtocol("prot");
        captureFile.setFlow("capflow");
        captureFile.setTag_path("tagpath1");
        captureFile.setCapture_path("capturepath1");
        captureFile.setProcessing_type("capname");
        captureFile.setCaptureType(CaptureFile.CaptureType.cfe);

        String json = gson.toJson(captureFile);

        // Asserting get request                                            // cfe id
        HttpGet requestGet = new HttpGet("http://localhost:" + port + "/capture/file/" + 1);

        requestGet.setHeader("Authorization", "Bearer " + token);

        HttpResponse responseGet = HttpClientBuilder.create().build().execute(requestGet);

        HttpEntity entityGet = responseGet.getEntity();

        String responseStringGet = EntityUtils.toString(entityGet, "UTF-8");


        assertEquals(json, responseStringGet);
        assertThat(responseGet.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    }

    // Get All captures

    @Test
    @Order(5)
    public void testgetAllCaptures() throws Exception {

        ArrayList<CaptureFile> expected = new ArrayList<>();

        CaptureFile captureFile2 = new CaptureFile();
        captureFile2.setId(1);
        captureFile2.setTag("f466e5a4-tagpath1");
        captureFile2.setRetention_time("P30D");
        captureFile2.setCategory("audit");
        captureFile2.setApplication("app1");
        captureFile2.setIndex("app1_audit");
        captureFile2.setSource_type("sourcetype1");
        captureFile2.setProtocol("prot");
        captureFile2.setFlow("capflow");
        captureFile2.setTag_path("tagpath1");
        captureFile2.setCapture_path("capturepath1");
        captureFile2.setProcessing_type("capname");
        captureFile2.setCaptureType(CaptureFile.CaptureType.cfe);

        CaptureFile captureRelp2 = new CaptureFile();
        captureRelp2.setId(2);
        captureRelp2.setTag("relpTag");
        captureRelp2.setRetention_time("P30D");
        captureRelp2.setCategory("audit");
        captureRelp2.setApplication("relp");
        captureRelp2.setIndex("audit_relp");
        captureRelp2.setSource_type("relpsource1");
        captureRelp2.setProtocol("prot");
        captureRelp2.setFlow("capflow");
        captureRelp2.setCaptureType(CaptureFile.CaptureType.relp);

        expected.add(captureFile2);
        expected.add(captureRelp2);

        String expectedJson = gson.toJson(expected);

        // Test Get ALL

        // Asserting get request
        HttpGet requestGet = new HttpGet("http://localhost:" + port + "/capture/");

        requestGet.setHeader("Authorization", "Bearer " + token);

        HttpResponse responseGet = HttpClientBuilder.create().build().execute(requestGet);

        HttpEntity entityGet = responseGet.getEntity();

        String responseStringGet = EntityUtils.toString(entityGet, "UTF-8");

        assertThat(responseGet.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        assertEquals(expectedJson, responseStringGet);

    }


    // Delete
    @Test
    @Order(6)
    public void testDeleteNonExistentCapture() throws Exception {
        HttpDelete delete = new HttpDelete("http://localhost:" + port + "/capture/124124");

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
    @Order(7)
    public void testDeleteCapture() throws Exception {
        HttpDelete delete = new HttpDelete("http://localhost:" + port + "/capture/" + 1);

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
        String expected = "Capture with id = 1 deleted.";

        assertEquals(expected, actual);
        assertThat(deleteResponse.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
    }
}
