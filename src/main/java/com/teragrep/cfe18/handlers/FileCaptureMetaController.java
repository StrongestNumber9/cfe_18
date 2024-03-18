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

import com.teragrep.cfe18.FileCaptureMetaMapper;
import com.teragrep.cfe18.handlers.entities.FileCaptureMeta;
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
@RequestMapping(path = "file/capture")
@SecurityRequirement(name = "api")
public class FileCaptureMetaController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileCaptureMetaController.class);

    @Autowired
    DataSource dataSource;

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    FileCaptureMetaMapper fileCaptureMetaMapper;


    @RequestMapping(path = "/meta/{name}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getProcessingType(@PathVariable("name") String name) {
        try {
            FileCaptureMeta fc = fileCaptureMetaMapper.getProcessingTypeByName(name);
            return new ResponseEntity<>(fc, HttpStatus.OK);
        } catch (Exception ex) {
            JSONObject jsonErr = new JSONObject();
            jsonErr.put("id", 0);
            final Throwable cause = ex.getCause();
            if (cause instanceof SQLException) {
                LOGGER.error((cause).getMessage());
                String state = ((SQLException) cause).getSQLState();
                // fail-safe if SQL error but not 45000 type
                jsonErr.put("message", state);
                if (state.equals("45000")) {
                    jsonErr.put("message", "Record does not exist with the given name");
                }
                return new ResponseEntity<>(jsonErr.toString(), HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // Get ALL endpoint
    @RequestMapping(path = "/meta/", method = RequestMethod.GET, produces = "application/json")
    public List<FileCaptureMeta> getAllProcessingType() {
        return fileCaptureMetaMapper.getAllProcessingType();
    }


    @RequestMapping(path = "/meta/rule", method = RequestMethod.PUT, produces = "application/json")
    public ResponseEntity<String> newFileMeta(@RequestBody FileCaptureMeta newFileCaptureMeta) {
        LOGGER.info("about to insert <[" + newFileCaptureMeta + "]>");
        try {
            FileCaptureMeta n = fileCaptureMetaMapper.addNewProcessingType(
                    newFileCaptureMeta.getTemplate(),
                    newFileCaptureMeta.getRuleset(),
                    newFileCaptureMeta.getName(),
                    newFileCaptureMeta.getInputtype().toString(),
                    newFileCaptureMeta.getInputvalue());
            LOGGER.info("Values returned <[" + n + "]>");

            JSONObject jsonObject = new JSONObject();
            // ID is never returned from database so null should suffice.
            String v = null;
            jsonObject.put("id", v);
            jsonObject.put("message", "New processing type created with the name = " + n.getName());

            return new ResponseEntity<>(jsonObject.toString(), HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            JSONObject jsonErr = new JSONObject();
            jsonErr.put("id", 0);
            LOGGER.error(ex.getMessage());
            if (ex instanceof NullPointerException) {
                LOGGER.error(ex.getMessage());
                return new ResponseEntity<>("NO NULLS ALLOWED", HttpStatus.BAD_REQUEST);
            }
            final Throwable cause = ex.getCause();
            if (cause instanceof SQLException) {

                // Get specific error type
                int error = ((SQLException) cause).getErrorCode();
                // Link error with state to get accurate error status
                String state = error + "-" + ((SQLException) cause).getSQLState();
                if (state.equals("1062-23000")) {
                    jsonErr.put("message", "FileCaptureMeta name already exists with different values");
                }
                return new ResponseEntity<>(jsonErr.toString(), HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    // Delete
    @RequestMapping(path = "meta/{name}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> removeProcessingType(@PathVariable("name") String name) {
        LOGGER.info("Deleting processing type " + name);
        JSONObject jsonErr = new JSONObject();
        jsonErr.put("id", 0);
        try {
            fileCaptureMetaMapper.deleteProcessingType(name);
            JSONObject j = new JSONObject();
            j.put("id", 0);
            j.put("message", "Processing type " + name + " deleted.");
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

