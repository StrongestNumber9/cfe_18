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


import com.teragrep.cfe18.HostMetaMapper;
import com.teragrep.cfe18.handlers.entities.HostMeta;
import com.teragrep.cfe18.handlers.entities.InterfaceType;
import com.teragrep.cfe18.handlers.entities.IPAddress;
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

@RequestMapping(path = "host")
@RestController
@SecurityRequirement(name = "api")
public class HostMetaController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HostMetaController.class);

    @Autowired
    DataSource dataSource;

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    HostMetaMapper hostMetaMapper;


    @RequestMapping(path = "/meta/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getHostMeta(@PathVariable("id") int id) {
        try {
            List<HostMeta> hm = hostMetaMapper.getHostMetaById(id);
            return new ResponseEntity<>(hm, HttpStatus.OK);
        } catch (Exception ex) {
            JSONObject jsonErr = new JSONObject();
            jsonErr.put("id", id);
            final Throwable cause = ex.getCause();
            if (cause instanceof SQLException) {
                LOGGER.error((cause).getMessage());
                String state = ((SQLException) cause).getSQLState();
                if (state.equals("45000")) {
                    jsonErr.put("message", "Record does not exist with the given host_meta_id");
                } else if (state.equals("45100")) {
                    jsonErr.put("message", "IP and/or Interface is missing");
                }
                return new ResponseEntity<>(jsonErr.toString(), HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // GET ALL Hostmeta. IP and Interface excluded.
    @RequestMapping(path = "/meta", method = RequestMethod.GET, produces = "application/json")
    public List<HostMeta> getAllHostMeta() {
        return hostMetaMapper.getAllHostMeta();
    }

    // GET ALL IP Addresses
    @RequestMapping(path = "/meta/ip", method = RequestMethod.GET, produces = "application/json")
    public List<IPAddress> getAllHostMetaIp() {
        return hostMetaMapper.getAllHostMetaIp();
    }


    // GET ALL Interfaces
    @RequestMapping(path = "/meta/interface", method = RequestMethod.GET, produces = "application/json")
    public List<InterfaceType> getAllHostMetaInterface() {
        return hostMetaMapper.getAllHostMetaInterface();
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/meta", produces = "application/json")
    public ResponseEntity<String> addHostMeta(@RequestBody HostMeta newHostMeta) {
        LOGGER.info("about to insert <[" + newHostMeta + "]>");
        try {
            HostMeta hm = hostMetaMapper.addHostMeta(
                    newHostMeta.getArch(),
                    newHostMeta.getFlavor(),
                    newHostMeta.getHostname(),
                    newHostMeta.getHost_id(),
                    newHostMeta.getOs(),
                    newHostMeta.getRelease_version()
            );
            LOGGER.info("Values returned <[" + hm + "]>");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", hm.getId());
            jsonObject.put("message", "New host meta added for host");
            return new ResponseEntity<>(jsonObject.toString(), HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            JSONObject jsonErr = new JSONObject();
            jsonErr.put("id", newHostMeta.getId());
            jsonErr.put("message", ex.getCause().toString());
            if (ex instanceof NullPointerException) {
                jsonErr.put("message", "NO NULLS ALLOWED");
                LOGGER.error(ex.getMessage());
            }
            return new ResponseEntity<>(jsonErr.toString(), HttpStatus.BAD_REQUEST);
        }
    }


    // new interface for host metadata
    @RequestMapping(method = RequestMethod.PUT, path = "/meta/interface", produces = "application/json")
    public ResponseEntity<String> addInterface_type(@RequestBody InterfaceType newInterfaceType) {
        LOGGER.info("about to insert <[" + newInterfaceType + "]>");
        try {
            InterfaceType it = hostMetaMapper.addInterface_type(
                    newInterfaceType.getInterfaceType(),
                    newInterfaceType.getHost_meta_id());
            LOGGER.info("Values returned <[" + it + "]>");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", it.getHost_meta_id());
            jsonObject.put("message", "New interface created for host_meta");
            return new ResponseEntity<>(jsonObject.toString(), HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            JSONObject jsonErr = new JSONObject();
            jsonErr.put("id", newInterfaceType.getHost_meta_id());
            jsonErr.put("message", ex.getCause().toString());
            return new ResponseEntity<>(jsonErr.toString(), HttpStatus.BAD_REQUEST);
        }
    }

    // new ip address for host metadata
    @RequestMapping(path = "/meta/ip", method = RequestMethod.PUT, produces = "application/json")
    public ResponseEntity<String> addIpAddress(@RequestBody IPAddress newIpAddress) {
        LOGGER.info("about to insert <[" + newIpAddress + "]>");
        try {
            IPAddress ia = hostMetaMapper.addIpAddress(
                    newIpAddress.getHost_meta_id(),
                    newIpAddress.getIpAddress());
            LOGGER.info("Values returned <[" + ia + "]>");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", ia.getHost_meta_id());
            jsonObject.put("message", "New ip address created for host_meta");
            return new ResponseEntity<>(jsonObject.toString(), HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            JSONObject jsonErr = new JSONObject();
            jsonErr.put("id", newIpAddress.getHost_meta_id());
            jsonErr.put("message", ex.getCause().toString());
            return new ResponseEntity<>(jsonErr.toString(), HttpStatus.BAD_REQUEST);
        }
    }

    // Delete IP
    @RequestMapping(path = "/meta/ip/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> removeIp(@PathVariable("id") int id) {
        LOGGER.info("Deleting Host " + id);
        JSONObject jsonErr = new JSONObject();
        jsonErr.put("id", id);
        try {
            hostMetaMapper.deleteIp(id);
            JSONObject j = new JSONObject();
            j.put("id", id);
            j.put("message", "Ip with id =  " + id + " deleted.");
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

    // Delete Interface
    @RequestMapping(path = "/meta/interface/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> removeInterface(@PathVariable("id") int id) {
        LOGGER.info("Deleting Interface " + id);
        JSONObject jsonErr = new JSONObject();
        jsonErr.put("id", id);
        try {
            hostMetaMapper.deleteInterface(id);
            JSONObject j = new JSONObject();
            j.put("id", id);
            j.put("message", "Interface with id =  " + id + " deleted.");
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

    // Delete HostMeta
    @RequestMapping(path = "/meta/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> removeHostMeta(@PathVariable("id") int id) {
        LOGGER.info("Deleting Hostmeta " + id);
        JSONObject jsonErr = new JSONObject();
        jsonErr.put("id", id);
        jsonErr.put("message", "Unexpected error occurred");
        try {
            hostMetaMapper.deleteHostmeta(id);
            JSONObject j = new JSONObject();
            j.put("id", id);
            j.put("message", "Hostmeta with id =  " + id + " deleted.");
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
