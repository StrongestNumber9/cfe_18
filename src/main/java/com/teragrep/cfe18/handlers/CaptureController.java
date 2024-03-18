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

import com.teragrep.cfe18.CaptureMapper;
import com.teragrep.cfe18.handlers.entities.CaptureFile;
import com.teragrep.cfe18.handlers.entities.CaptureRelp;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping(path = "/capture")
@SecurityRequirement(name = "api")
public class CaptureController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CaptureController.class);

    @Autowired
    DataSource dataSource;

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    CaptureMapper captureMapper;


    @RequestMapping(path = "/jwt", method = RequestMethod.GET, produces = "application/json")
    public String index(@AuthenticationPrincipal Jwt jwt) throws Exception {
        return String.format("Hello, %s!", jwt.getSubject());
    }

    @RequestMapping(path = "/file/{id}", method = RequestMethod.GET, produces = "application/json")
    @Operation(summary = "Fetch a file capture by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the file capture",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CaptureFile.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid capture id or capture type",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error, contact admin", content = @Content)
    })
    public ResponseEntity<?> getResultsFile(@PathVariable("id") int id) {
        try {
            CaptureFile c = captureMapper.getCaptureFileById(id);
            if (c.getCaptureType() == CaptureFile.CaptureType.cfe) {
                return new ResponseEntity<>(c, HttpStatus.OK);
            } else {
                throw new Exception("Different capture_type");
            }
        } catch (Exception ex) {
            JSONObject jsonErr = new JSONObject();
            jsonErr.put("id", 0);
            final Throwable cause = ex.getCause();
            if (cause instanceof SQLException) {
                LOGGER.error((cause).getMessage());
                String state = ((SQLException) cause).getSQLState();
                if (state.equals("45000")) {
                    jsonErr.put("message", "Record does not exist with the given capture ID");
                    return new ResponseEntity<>(jsonErr.toString(), HttpStatus.BAD_REQUEST);
                }
            } else if (ex.getMessage().equals("Different capture_type")) {
                LOGGER.error(ex.getMessage());
                jsonErr.put("message", "capture exists with different type");
                return new ResponseEntity<>(jsonErr.toString(), HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping(path = "/relp/{id}", method = RequestMethod.GET, produces = "application/json")
    @Operation(summary = "Fetch a relp capture by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the relp capture",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CaptureRelp.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid capture id or capture type",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error, contact admin", content = @Content)
    })
    public ResponseEntity<?> getResultsRelp(@PathVariable("id") int id) {
        try {
            CaptureRelp c = captureMapper.getCaptureRelpById(id);
            if (c.getCaptureType() == CaptureRelp.CaptureType.relp) {
                return new ResponseEntity<>(c, HttpStatus.OK);
            } else {
                throw new Exception("Different capture_type");
            }
        } catch (Exception ex) {
            JSONObject jsonErr = new JSONObject();
            jsonErr.put("id", 0);
            final Throwable cause = ex.getCause();
            if (cause instanceof SQLException) {
                LOGGER.error((cause).getMessage());
                String state = ((SQLException) cause).getSQLState();
                if (state.equals("45000")) {
                    jsonErr.put("message", "Record does not exist with the given capture ID");
                    return new ResponseEntity<>(jsonErr.toString(), HttpStatus.BAD_REQUEST);
                }

            } else if (ex.getMessage().equals("Different capture_type")) {
                LOGGER.error(ex.getMessage());
                jsonErr.put("message", "capture exists with different type");
                return new ResponseEntity<>(jsonErr.toString(), HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    // GET ALL Captures
    @RequestMapping(path = "/", method = RequestMethod.GET, produces = "application/json")
    @Operation(summary = "Fetch all captures", description = "Will return empty list if there are no captures to fetch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the relp capture",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CaptureFile.class))}),
    })
    public List<CaptureFile> getAllCapture() {
        return captureMapper.getAllCapture();
    }


    @RequestMapping(path = "/file", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create new file based capture")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "New file based capture created",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CaptureFile.class))}),
            @ApiResponse(responseCode = "400", description = "Missing requirements or mismatch along values",
                    content = @Content)
    })
    public ResponseEntity<String> newCaptureFile(@RequestBody CaptureFile newCapture) {
        LOGGER.info("about to insert <[" + newCapture + "]>");
        try {
            CaptureFile c = captureMapper.addNewCaptureFile(
                    newCapture.getTag(),
                    newCapture.getRetention_time(),
                    newCapture.getCategory(),
                    newCapture.getApplication(),
                    newCapture.getIndex(),
                    newCapture.getSource_type(),
                    newCapture.getProtocol(),
                    newCapture.getFlow(),
                    newCapture.getTag_path(),
                    newCapture.getCapture_path(),
                    newCapture.getProcessing_type());

            LOGGER.info("Values returned <[" + c + "]>");
            JSONObject jsonObjectFile = new JSONObject();
            jsonObjectFile.put("id", c.getId());
            jsonObjectFile.put("message", "New capture created");
            return new ResponseEntity<>(jsonObjectFile.toString(), HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            JSONObject jsonErr = new JSONObject();
            jsonErr.put("id", newCapture.getId());
            jsonErr.put("message", ex.getCause().toString());
            LOGGER.error(ex.getMessage());
            return new ResponseEntity<>(jsonErr.toString(), HttpStatus.BAD_REQUEST);
        }

    }

    @RequestMapping(path = "/relp", method = RequestMethod.PUT, produces = "application/json")
    @Operation(summary = "Create new relp based capture")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "New relp based capture created",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CaptureFile.class))}),
            @ApiResponse(responseCode = "400", description = "Missing requirements",
                    content = @Content)
    })
    public ResponseEntity<String> newCaptureRelp(@RequestBody CaptureRelp newCapture) {
        LOGGER.info("about to insert <[" + newCapture + "]>");
        try {
            CaptureRelp c = captureMapper.addNewCaptureRelp(
                    newCapture.getTag(),
                    newCapture.getRetention_time(),
                    newCapture.getCategory(),
                    newCapture.getApplication(),
                    newCapture.getIndex(),
                    newCapture.getSource_type(),
                    newCapture.getProtocol(),
                    newCapture.getFlow());
            LOGGER.info("Values returned <[" + c + "]>");
            JSONObject jsonObjectRelp = new JSONObject();
            jsonObjectRelp.put("id", c.getId());
            jsonObjectRelp.put("message", "New capture created");
            return new ResponseEntity<>(jsonObjectRelp.toString(), HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            JSONObject jsonErr = new JSONObject();
            jsonErr.put("id", newCapture.getId());
            jsonErr.put("message", ex.getCause().toString());
            return new ResponseEntity<>(jsonErr.toString(), HttpStatus.BAD_REQUEST);
        }
    }

    // Delete
    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete capture")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Capture deleted",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CaptureRelp.class))}),
            @ApiResponse(responseCode = "400", description = "Capture does not exist or it is being used",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error, contact admin", content = @Content)
    })
    public ResponseEntity<String> removeCapture(@PathVariable("id") int id) {
        LOGGER.info("Deleting capture with id = " + id);
        JSONObject jsonErr = new JSONObject();
        jsonErr.put("id", id);
        jsonErr.put("message", "Unexpected error occurred");
        try {
            captureMapper.deleteCapture(id);
            JSONObject j = new JSONObject();
            j.put("id", id);
            j.put("message", "Capture with id = " + id + " deleted.");
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

