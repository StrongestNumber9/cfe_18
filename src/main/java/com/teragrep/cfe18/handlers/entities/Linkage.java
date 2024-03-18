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
package com.teragrep.cfe18.handlers.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Linkage {

    public enum group_type {
        cfe, relp
    }

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private int id;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String capture_group_name;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String host_group_name;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private group_type host_group_type;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private group_type capture_group_type;

    private int host_group_id;
    private int capture_group_id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCapture_group_name() {
        return capture_group_name;
    }

    public void setCapture_group_name(String capture_group_name) {
        this.capture_group_name = capture_group_name;
    }

    public String getHost_group_name() {
        return host_group_name;
    }

    public void setHost_group_name(String host_group_name) {
        this.host_group_name = host_group_name;
    }

    public group_type getHost_group_type() {
        return host_group_type;
    }

    public void setHost_group_type(group_type host_group_type) {
        this.host_group_type = host_group_type;
    }

    public group_type getCapture_group_type() {
        return capture_group_type;
    }

    public void setCapture_group_type(group_type capture_group_type) {
        this.capture_group_type = capture_group_type;
    }

    public int getHost_group_id() {
        return host_group_id;
    }

    public void setHost_group_id(int host_group_id) {
        this.host_group_id = host_group_id;
    }

    public int getCapture_group_id() {
        return capture_group_id;
    }

    public void setCapture_group_id(int capture_group_id) {
        this.capture_group_id = capture_group_id;
    }

    @Override
    public String toString() {
        return "Linkage{" +
                "id=" + id +
                ", capture_group_name='" + capture_group_name + '\'' +
                ", host_group_name='" + host_group_name + '\'' +
                ", host_group_type=" + host_group_type +
                ", capture_group_type=" + capture_group_type +
                ", host_group_id=" + host_group_id +
                ", capture_group_id=" + capture_group_id +
                '}';
    }
}
