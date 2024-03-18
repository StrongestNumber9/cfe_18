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
use cfe_18;
DELIMITER //
CREATE OR REPLACE PROCEDURE create_data_for_processing_type(meta_template varchar(255), meta_rule varchar(1000),
                                                            meta_rule_name varchar(255),
                                                            inputtype varchar(20), inputvalue varchar(255)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        end;


    if ((inputtype != 'regex') and (inputtype != 'newline')) then
        SELECT JSON_OBJECT('id', NULL, 'message', 'Provide correct inputtype. (Regex or Newline)') into @it;
        signal sqlstate '45000' set message_text = @it;
    end if;


    if inputtype = 'regex' then
        if (select regex from cfe_18.regex where regex = inputvalue) is null then
            insert into cfe_18.inputtype(inputtype) values ('regex');
            select last_insert_id() into @RegexId;
            insert into cfe_18.regex(id, regex, inputtype) values (@RegexId, inputvalue, 'regex');
            select last_insert_id() into @InputId;
        else
            select id into @InputId from cfe_18.regex where regex = inputvalue;
        end if;
    end if;
    if inputtype = 'newline' then
        if (select newline from cfe_18.newline where newline = inputvalue) is null then
            insert into cfe_18.inputtype(inputtype) values ('newline');
            select last_insert_id() into @NewlineId;
            insert into cfe_18.newline(id, newline, inputtype) values (@NewlineId, inputvalue, 'newline');
            select last_insert_id() into @InputId;
        else
            select id into @InputId from cfe_18.newline where newline = inputvalue;
        end if;
    end if;

    if (select id from cfe_18.ruleset where rule = meta_rule) is null then
        insert into cfe_18.ruleset(rule)
        values (meta_rule);
        select last_insert_id() into @RuleId;
    else
        select id into @RuleId from cfe_18.ruleset where rule = meta_rule;
    end if;

    if (select id from cfe_18.templates where template = meta_template) is null then
        insert into cfe_18.templates(template)
        values (meta_template);
        select last_insert_id() into @TemplateId;
    else
        select id into @TemplateId from cfe_18.templates where template = meta_template;
    end if;

    if (SELECT id
        from processing_type
        where inputtype_id = @InputId
          and template_id = @TemplateId
          and ruleset_id = @RuleId) is null then
        insert into cfe_18.processing_type(inputtype_id, ruleset_id, template_id, type_name)
        values (@InputId, @RuleId, @TemplateId, meta_rule_name);
        select meta_rule_name as name;
    elseif (SELECT pt.id
            from processing_type pt
                     INNER JOIN ruleset r ON pt.ruleset_id = r.id
                     INNER JOIN templates t ON pt.template_id = t.id
                     INNER JOIN inputtype i ON pt.inputtype_id = i.id
                     LEFT JOIN regex r2 ON i.id = r2.id AND i.inputtype = r2.inputtype
                     LEFT JOIN newline n ON i.id = n.id AND i.inputtype = n.inputtype
            WHERE (t.template = meta_template
                AND r.rule = meta_rule
                AND ((i.inputtype = 'regex' AND r2.regex = inputvalue) OR
                     (i.inputtype = 'newline' AND n.newline = inputvalue)) AND pt.type_name = meta_rule_name
                      )) is null then
        select pt.type_name as name
        from processing_type pt
                 INNER JOIN ruleset r ON pt.ruleset_id = r.id
                 INNER JOIN templates t ON pt.template_id = t.id
                 INNER JOIN inputtype i ON pt.inputtype_id = i.id
                 LEFT JOIN regex r2 ON i.id = r2.id AND i.inputtype = r2.inputtype
                 LEFT JOIN newline n ON i.id = n.id AND i.inputtype = n.inputtype
        WHERE (t.template = meta_template
            AND r.rule = meta_rule
            AND ((i.inputtype = 'regex' AND r2.regex = inputvalue) OR
                 (i.inputtype = 'newline' AND n.newline = inputvalue)));
    else
        SELECT pt2.type_name as name
        from processing_type pt2
                 INNER JOIN ruleset r ON pt2.ruleset_id = r.id
                 INNER JOIN templates t ON pt2.template_id = t.id
                 INNER JOIN inputtype i ON pt2.inputtype_id = i.id
                 LEFT JOIN regex r2 ON i.id = r2.id AND i.inputtype = r2.inputtype
                 LEFT JOIN newline n ON i.id = n.id AND i.inputtype = n.inputtype
        WHERE (t.template = meta_template
            AND r.rule = meta_rule
            AND ((i.inputtype = 'regex' AND r2.regex = inputvalue) OR
                 (i.inputtype = 'newline' AND n.newline = inputvalue))
                  );
    end if;

end;
//
DELIMITER ;