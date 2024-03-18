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
package com.teragrep.cfe18.handlers;

import com.teragrep.cfe18.CaptureGroupMapper;
import com.teragrep.cfe18.handlers.entities.CaptureGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.json.JSONObject;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping(path = "capture")
@SecurityRequirement(name = "api")
public class CaptureGroupController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CaptureGroupController.class);

    @Autowired
    DataSource dataSource;

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    CaptureGroupMapper captureGroupMapper;


    @RequestMapping(path = "/group/{name}", method = RequestMethod.GET, produces = "application/json")
    @Operation(summary = "Fetch capture group by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the capture group",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CaptureGroup.class))}),
            @ApiResponse(responseCode = "400", description = "Capture group does not exist or there are no captures linked to the group",
                    content = @Content)
    })
    public ResponseEntity<?> getResults(@PathVariable("name") String name) {
        JSONObject jsonErr = new JSONObject();
        jsonErr.put("id", 0);
        jsonErr.put("message", "Unexpected error");
        try {
            List<CaptureGroup> cg = captureGroupMapper.getCaptureGroupByName(name);
            if (cg.isEmpty()) {
                throw new Exception("Empty group");
            } else {
                return new ResponseEntity<>(cg, HttpStatus.OK);
            }
        } catch (Exception ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof SQLException) {
                LOGGER.error((cause).getMessage());
                String state = ((SQLException) cause).getSQLState();
                if (state.equals("45000")) {
                    jsonErr.put("message", "Capture group does not exist");
                }
            } else if (ex.getMessage().equals("Empty group")) {
                LOGGER.error(ex.getMessage());
                jsonErr.put("message", "No records found");
            }
            return new ResponseEntity<>(jsonErr.toString(), HttpStatus.BAD_REQUEST);
        }
    }

    // GET ALL
    @RequestMapping(path = "/group", method = RequestMethod.GET, produces = "application/json")
    @Operation(summary = "Fetch all capture groups with captures", description = "Will return empty list if there are no capture groups to fetch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found capture groups",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CaptureGroup.class))})})
    public List<CaptureGroup> getAllCaptureGroup() {
        return captureGroupMapper.getAllCaptureGroup();
    }


    @RequestMapping(path = "/group", method = RequestMethod.PUT, produces = "application/json")
    @Operation(summary = "Insert capture group with capture")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Capture group created and/or capture linked to capture group",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CaptureGroup.class))}),
            @ApiResponse(responseCode = "400", description = "Type mismatch between capture group and capture or capture does not exist",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error, contact admin", content = @Content)
    })
    public ResponseEntity<String> newCaptureGroup(@RequestBody CaptureGroup newCaptureGroup) {
        LOGGER.info("about to insert <[" + newCaptureGroup + "]>");
        JSONObject jsonErr = new JSONObject();
        jsonErr.put("id", 0);
        try {
            CaptureGroup c = captureGroupMapper.addNewCaptureGroup(
                    newCaptureGroup.getCapture_def_group_name(),
                    newCaptureGroup.getCapture_definition_id()
            );
            LOGGER.info("Values returned <[" + c + "]>");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("capture_group_id", c.getId());
            jsonObject.put("message", "New capture group created with name = " + c.getCapture_def_group_name());
            return new ResponseEntity<>(jsonObject.toString(), HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof SQLException) {
                LOGGER.error((cause).getMessage());
                // Get specific error type
                int error = ((SQLException) cause).getErrorCode();
                // Link error with state to get accurate error status
                String state = error + "-" + ((SQLException) cause).getSQLState();
                if (state.equals("1452-23000")) {
                    jsonErr.put("message", "Type mismatch between capture group and capture");
                } else if (state.equals("1644-45000")) {
                    jsonErr.put("message", "Capture does not exist");
                }
                return new ResponseEntity<>(jsonErr.toString(), HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    // Delete
    @RequestMapping(path = "group/{name}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> removeCaptureGroup(@PathVariable("name") String name) {
        LOGGER.info("Deleting Capture group " + name);
        JSONObject jsonErr = new JSONObject();
        jsonErr.put("id", 0);
        try {
            captureGroupMapper.deleteCaptureGroup(name);
            JSONObject j = new JSONObject();
            j.put("id", 0);
            j.put("message", "Capture group " + name + " deleted.");
            return new ResponseEntity<>(j.toString(), HttpStatus.OK);
        } catch (Exception ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof SQLException) {
                LOGGER.error((cause).getMessage());
                String state = ((SQLException) cause).getSQLState();
                if (state.equals("23000")) {
                    jsonErr.put("message", "Is in use");
                } else if (state.equals("45000")) {
                    jsonErr.put("message", "Record does not exist");
                }
                return new ResponseEntity<>(jsonErr.toString(), HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

