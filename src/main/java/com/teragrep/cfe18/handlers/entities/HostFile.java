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
public class HostFile {
    private int id;
    private String md5;
    private String fqHost;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String host_type;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private int hub;
    private String hub_fq;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String hostname;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private int host_meta_id;


    public String getHost_type() {
        return host_type;
    }


    public void setHost_type(String host_type) {
        this.host_type = host_type;
    }

    public String getFqHost() {
        return fqHost;
    }

    public void setFqHost(String fqHost) {
        this.fqHost = fqHost;
    }

    public String getMD5() {
        return md5;
    }

    public void setMD5(String md5) {
        this.md5 = md5;
    }

    public int getHub() {
        return hub;
    }


    public void setHub(int hub) {
        this.hub = hub;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getHost_meta_id() {
        return host_meta_id;
    }

    public void setHost_meta_id(int host_meta_id) {
        this.host_meta_id = host_meta_id;
    }

    public String getHub_fq() {
        return hub_fq;
    }

    public void setHub_fq(String hub_fq) {
        this.hub_fq = hub_fq;
    }

    @Override
    public String toString() {
        return "HostFile{" +
                "id=" + id +
                ", md5='" + md5 + '\'' +
                ", fqHost='" + fqHost + '\'' +
                ", host_type='" + host_type + '\'' +
                ", hub=" + hub +
                ", hub_fq='" + hub_fq + '\'' +
                ", hostname='" + hostname + '\'' +
                ", host_meta_id=" + host_meta_id +
                '}';
    }
}
