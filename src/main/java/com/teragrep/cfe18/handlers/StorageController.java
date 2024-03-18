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

import com.teragrep.cfe18.StorageMapper;
import com.teragrep.cfe18.handlers.entities.CaptureStorage;
import com.teragrep.cfe18.handlers.entities.FlowStorage;
import com.teragrep.cfe18.handlers.entities.Storage;
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
@RequestMapping(path = "/storage")
@SecurityRequirement(name = "api")
public class StorageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageController.class);
    @Autowired
    DataSource dataSource;

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    StorageMapper storageMapper;


    // Fetch flow storages
    @RequestMapping(path = "/flow/{flow}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getStoragesByFlow(@PathVariable String flow) {
        try {
            List<FlowStorage> fs = storageMapper.retrieveFlowStorages(flow);
            return new ResponseEntity<>(fs, HttpStatus.OK);
        } catch (Exception ex) {

            final Throwable cause = ex.getCause();
            if (cause instanceof SQLException) {
                LOGGER.error((cause).getMessage());
                String state = ((SQLException) cause).getSQLState();
                if (state.equals("45000")) {
                    JSONObject jsonErr = new JSONObject();
                    jsonErr.put("id", 0);
                    jsonErr.put("message", "Record does not exist with the given flow");
                    return new ResponseEntity<>(jsonErr.toString(), HttpStatus.BAD_REQUEST);
                }
            }
            return new ResponseEntity<>("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get ALL Flow storages
    @RequestMapping(path = "/flow", method = RequestMethod.GET, produces = "application/json")
    public List<FlowStorage> getAllFlowStorages() {
        return storageMapper.getAllFlowStorage();
    }

    // Fetch capture storages
    @RequestMapping(path = "/capture/{capture_id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getStoragesByCaptureID(@PathVariable int capture_id) {
        try {
            List<CaptureStorage> cs = storageMapper.retrieveCaptureStorages(capture_id);
            return new ResponseEntity<>(cs, HttpStatus.OK);
        } catch (Exception ex) {
            JSONObject jsonErr = new JSONObject();
            jsonErr.put("id", capture_id);
            final Throwable cause = ex.getCause();
            if (cause instanceof SQLException) {
                LOGGER.error((cause).getMessage());
                String state = ((SQLException) cause).getSQLState();
                if (state.equals("45000")) {
                    jsonErr.put("message", "Record does not exist with the given capture_id");
                    return new ResponseEntity<>(jsonErr.toString(), HttpStatus.BAD_REQUEST);
                }
            }
            return new ResponseEntity<>("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Fetch ALL Capture storages
    @RequestMapping(path = "/capture", method = RequestMethod.GET, produces = "application/json")
    public List<CaptureStorage> getAllCaptureStorages() {
        return storageMapper.getAllCaptureStorage();
    }


    // Fetch all storages
    @RequestMapping(path = "", method = RequestMethod.GET, produces = "application/json")
    public List<Storage> getStorages() {
        return storageMapper.getStorages();
    }


    // New storage with the flow
    @RequestMapping(path = "/flow", method = RequestMethod.PUT, produces = "application/json")
    public ResponseEntity<String> addNewStorage(@RequestBody FlowStorage newFlowStorage) {
        LOGGER.info("about to insert <[" + newFlowStorage + "]>");
        try {
            FlowStorage fs = storageMapper.addStorageForFlow(
                    newFlowStorage.getFlow(),
                    newFlowStorage.getStorage_id());
            LOGGER.info("Values returned <[" + fs + "]>");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", fs.getId());
            jsonObject.put("message", "New flow storage created");
            return new ResponseEntity<>(jsonObject.toString(), HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            JSONObject jsonErr = new JSONObject();
            jsonErr.put("id", newFlowStorage.getId());
            jsonErr.put("message", ex.getCause().toString());
            return new ResponseEntity<>(jsonErr.toString(), HttpStatus.BAD_REQUEST);
        }
    }


    // Link storage to capture
    @RequestMapping(path = "/capture", method = RequestMethod.PUT, produces = "application/json")
    public ResponseEntity<String> linkStorageToCapture(@RequestBody CaptureStorage newCaptureStorage) {
        LOGGER.info("about to insert <[" + newCaptureStorage + "]>");
        try {
            CaptureStorage cs = storageMapper.addStorageForCapture(
                    newCaptureStorage.getCapture_id(),
                    newCaptureStorage.getStorage_id());
            LOGGER.info("Values returned <[" + cs + "]>");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", cs.getCapture_id());
            jsonObject.put("message", "New capture storage created");
            return new ResponseEntity<>(jsonObject.toString(), HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            JSONObject jsonErr = new JSONObject();
            jsonErr.put("id", newCaptureStorage.getCapture_id());
            final Throwable cause = ex.getCause();
            if (cause instanceof SQLException) {
                LOGGER.error((cause).getMessage());
                // Get specific error type
                int error = ((SQLException) cause).getErrorCode();
                // Link error with state to get accurate error status
                String state = error + "-" + ((SQLException) cause).getSQLState();
                if (state.equals("1452-23000")) {
                    jsonErr.put("message", "Storage does not exist");
                    return new ResponseEntity<>(jsonErr.toString(), HttpStatus.BAD_REQUEST);
                }
            }
            return new ResponseEntity<>("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    // Insert standalone storage
    @RequestMapping(path = "", method = RequestMethod.PUT, produces = "application/json")
    public ResponseEntity<String> addStorage(@RequestBody Storage newStorage) {
        LOGGER.info("about to insert <[" + newStorage + "]>");
        try {
            Storage s = storageMapper.addStorage(
                    newStorage.getCfe_type().toString(),
                    newStorage.getTarget_name());
            LOGGER.info("Values returned <[" + s + "]>");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", s.getId());
            jsonObject.put("message", "New storage created");
            return new ResponseEntity<>(jsonObject.toString(), HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            JSONObject jsonErr = new JSONObject();
            jsonErr.put("id", newStorage.getId());
            jsonErr.put("message", ex.getCause().toString());
            final Throwable cause = ex.getCause();
            if (cause instanceof SQLException) {
                LOGGER.error((cause).getMessage());
                String state = ((SQLException) cause).getSQLState();
                if (state.equals("23000")) {
                    jsonErr.put("message", "Target name already exists");
                    return new ResponseEntity<>(jsonErr.toString(), HttpStatus.BAD_REQUEST);
                }
            }
            return new ResponseEntity<>("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete Storage
    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> removeStorage(@PathVariable("id") int id) {
        LOGGER.info("Deleting Storage " + id);
        JSONObject jsonErr = new JSONObject();
        jsonErr.put("id", id);
        try {
            storageMapper.deleteStorage(id);
            JSONObject j = new JSONObject();
            j.put("id", id);
            j.put("message", "Storage " + id + " deleted.");
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

    // Delete flow storage
    @RequestMapping(path = "/flow/{flow}/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> removeFlowStorage(@PathVariable("flow") String flow, @PathVariable("id") int id) {
        LOGGER.info("Deleting " + flow + " Storage " + id);
        JSONObject jsonErr = new JSONObject();
        jsonErr.put("id", id);
        try {
            storageMapper.deleteFlowStorage(flow, id);
            JSONObject j = new JSONObject();
            j.put("id", id);
            j.put("message", "Flow =" + flow + ", Storage " + id + " deleted.");
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

    // Delete capture storage
    @RequestMapping(path = "/capture/{capture_id}/{storage_id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> removeCaptureStorage(@PathVariable("capture_id") int capture_id, @PathVariable("storage_id") int storage_id) {
        LOGGER.info("Deleting capture = " + capture_id + ", Storage " + storage_id);
        JSONObject jsonErr = new JSONObject();
        jsonErr.put("id", capture_id);
        try {
            storageMapper.deleteCaptureStorage(capture_id, storage_id);
            JSONObject j = new JSONObject();
            j.put("id", capture_id);
            j.put("message", "Capture = " + capture_id + ", with Storage " + storage_id + " deleted.");
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
