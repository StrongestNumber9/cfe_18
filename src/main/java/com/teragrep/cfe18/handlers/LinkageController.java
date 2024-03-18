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

import com.teragrep.cfe18.LinkageMapper;
import com.teragrep.cfe18.handlers.entities.Linkage;
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
@RequestMapping(path = "/capture/groups")
@SecurityRequirement(name = "api")
public class LinkageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkageController.class);

    @Autowired
    DataSource dataSource;

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    LinkageMapper linkageMapper;


    // retrieve g_x_g details by group_name
    @RequestMapping(path = "/linkage/{name}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getLinkage(@PathVariable("name") String name) {
        try {
            List<Linkage> l = linkageMapper.getLinkageByName(name);
            return new ResponseEntity<>(l, HttpStatus.OK);
        } catch (Exception ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof SQLException) {
                JSONObject jsonErr = new JSONObject();
                jsonErr.put("id", 0);
                LOGGER.error((cause).getMessage());
                String state = ((SQLException) cause).getSQLState();
                if (state.equals("45000")) {
                    jsonErr.put("message", "Record does not exist with the given group name");
                    return new ResponseEntity<>(jsonErr.toString(), HttpStatus.BAD_REQUEST);
                }
            }
            return new ResponseEntity<>("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // GET ALL Linkages
    @RequestMapping(path = "/linkage", method = RequestMethod.GET, produces = "application/json")
    public List<Linkage> getAllLinkage() {
        return linkageMapper.getAllLinkage();
    }

    // add new g_x_g
    @RequestMapping(path = "/linkage", method = RequestMethod.PUT, produces = "application/json")
    public ResponseEntity<String> newLinkage(@RequestBody Linkage newLinkage) {
        LOGGER.info("about to insert <[" + newLinkage + "]>");
        try {
            Linkage l = linkageMapper.addLinkage(
                    newLinkage.getHost_group_id(),
                    newLinkage.getCapture_group_id()
            );
            LOGGER.info("Values returned <[" + l + "]>");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", l.getId());
            jsonObject.put("message", "New linkage created for groups = " + l.getCapture_group_name() + " and " + l.getHost_group_name());
            return new ResponseEntity<>(jsonObject.toString(), HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            JSONObject jsonErr = new JSONObject();
            jsonErr.put("id", newLinkage.getId());
            jsonErr.put("message", ex.getCause().toString());
            return new ResponseEntity<>(jsonErr.toString(), HttpStatus.BAD_REQUEST);
        }
    }

    // Delete
    @RequestMapping(path = "/linkage/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> removeLinkage(@PathVariable("id") int id) {
        LOGGER.info("Deleting Linkage " + id);
        JSONObject jsonErr = new JSONObject();
        jsonErr.put("id", id);
        try {
            linkageMapper.deleteLinkage(id);
            JSONObject j = new JSONObject();
            j.put("id", id);
            j.put("message", "Linkage with id = " + id + " deleted.");
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
