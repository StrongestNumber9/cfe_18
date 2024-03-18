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
public class CaptureGroup {
    public enum group_type {
        cfe, relp
    }

    private String capture_def_group_name;
    private Integer capture_definition_id;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private group_type capture_group_type;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private int id;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String tag;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getCapture_def_group_name() {
        return capture_def_group_name;
    }

    public void setCapture_def_group_name(String capture_def_group_name) {
        this.capture_def_group_name = capture_def_group_name;
    }

    public Integer getCapture_definition_id() {
        return capture_definition_id;
    }

    public void setCapture_definition_id(Integer capture_definition_id) {
        this.capture_definition_id = capture_definition_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public group_type getCapture_group_type() {
        return capture_group_type;
    }

    public void setCapture_group_type(group_type capture_group_type) {
        this.capture_group_type = capture_group_type;
    }

    @Override
    public String toString() {
        return "CaptureGroup{" +
                "capture_def_group_name='" + capture_def_group_name + '\'' +
                ", capture_definition_id=" + capture_definition_id +
                ", capture_group_type=" + capture_group_type +
                ", id=" + id +
                ", tag='" + tag + '\'' +
                '}';
    }
}
