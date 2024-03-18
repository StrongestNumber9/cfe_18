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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MigrateDatabaseExtension.class)
public class TokenTest extends TestSpringBootInformation {

    @LocalServerPort
    private int port;


    // Testing if token works
    @Test
    public void testToken() throws IOException {

        // Given
        HttpUriRequest request = new HttpGet("http://localhost:" + port + "/capture/jwt");
        request.setHeader("Authorization", "Bearer " + token);
        // When

        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        HttpEntity entity = httpResponse.getEntity();

        String responseString = EntityUtils.toString(entity, "UTF-8");

        // Then
        assertThat(
                httpResponse.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_OK));
        assertThat(responseString, equalTo("Hello, subject!"));
    }

    // Testing that without token there is no access
    @Test
    public void testInvalidToken() throws IOException {

        // Given
        HttpUriRequest request2 = new HttpGet("http://localhost:" + port + "/capture/jwt");
        request2.setHeader("Authorization", "Bearer noToken");

        // When

        HttpResponse httpResponse2 = HttpClientBuilder.create().build().execute(request2);

        // Then
        assertThat(
                httpResponse2.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_UNAUTHORIZED));
    }


    // Tests that giving no token returns 500
    @Test
    public void testNoToken() throws IOException {

        // Given
        HttpUriRequest request2 = new HttpGet("http://localhost:" + port + "/capture/jwt");

        // When

        HttpResponse httpResponse2 = HttpClientBuilder.create().build().execute(request2);

        // Then
        assertThat(
                httpResponse2.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_UNAUTHORIZED));
    }

}
