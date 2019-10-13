<?xml version="1.0" encoding="UTF-8"?>
<template>
    <comment ID="addJavaFileComment">
/*
 * Copyright 2018-2019 The Silkworm Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
    </comment>

    <comment ID="addModelClassComment">
<![CDATA[
/**
<#if introspectedTable.getRemarks()?? && introspectedTable.getRemarks()?trim != "">
 * ${introspectedTable.getRemarks()}。
 *
</#if>
 * <p>Table Name: ${introspectedTable.fullyQualifiedTable}.</p>
 *
 * @author Author Name
 * @since 2.0
 */
]]>
    </comment>

    <comment ID="addFieldComment">
<![CDATA[
<#if introspectedColumn??>
/**
    <#if introspectedColumn.remarks?? && introspectedColumn.remarks?trim != "">
        <#list introspectedColumn.remarks?split("\n") as remark>
            <#assign tRemark = remark?trim />
            <#if tRemark != "">
                <#if !tRemark?matches(".*。$")>
                    <#assign tRemark = tRemark + "。" />
                </#if>
                <#if (remark_index > 0) >
                    <#assign tRemark = "<p>" + tRemark + "</p>" />
                </#if>
 * ${tRemark}
            </#if>
        </#list>
 *
    </#if>
 * <p>Column Name: ${introspectedColumn.actualColumnName}.</p>
 */
<#else>
/**
 * ${introspectedTable.fullyQualifiedTable}
 */
</#if>
]]>
    </comment>

    <comment ID="addClassComment">
    </comment>

    <comment ID="addEnumComment">
    </comment>

    <comment ID="addInterfaceComment">
    </comment>

    <comment ID="addGetterComment">
    </comment>

    <comment ID="addSetterComment">
    </comment>

    <comment ID="addGeneralMethodComment">
    </comment>

    <comment ID="addRootComment">
    </comment>

    <comment ID="addComment">
    </comment>
</template>