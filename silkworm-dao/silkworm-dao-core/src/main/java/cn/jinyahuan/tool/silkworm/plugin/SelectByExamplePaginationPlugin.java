/*
 * Copyright 2018-2020 The Silkworm Authors
 */

package cn.jinyahuan.tool.silkworm.plugin;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * SelectByExample 增加分页属性的插件。
 * <p>只支持 MySQL 和 MariaDB 数据库。
 * <p>参考了：<a href="https://blog.csdn.net/JunglerOfChina/article/details/80371376">Mybatis代码生成增加分页参数</a>
 *
 * @author Yahuan Jin
 * @since 1.0
 */
public class SelectByExamplePaginationPlugin extends PluginAdapter {
    private static final String MYSQL_DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
    private static final String MARIADB_DRIVER_CLASS_NAME = "org.mariadb.jdbc.Driver";

    private static final String PROPERTY_NAME_OFFSET_FIELD_NAME = "offsetFieldName";
    private static final String PROPERTY_NAME_OFFSET_FIELD_TYPE_NAME = "offsetFieldTypeName";
    private static final String PROPERTY_NAME_ROW_COUNT_FIELD_NAME = "rowCountFieldName";
    private static final String PROPERTY_NAME_ROW_COUNT_FIELD_TYPE_NAME = "rowCountFieldTypeName";

    private String offsetFieldName;
    private String offsetFieldTypeName;
    private String rowCountFieldName;
    private String rowCountFieldTypeName;

    @Override
    public boolean validate(List<String> warnings) {
        final String driverClass = this.getContext().getJdbcConnectionConfiguration().getDriverClass();
        if (!(MYSQL_DRIVER_CLASS_NAME.equals(driverClass) || MARIADB_DRIVER_CLASS_NAME.equals(driverClass))) {
            warnings.add("ExampleSelectPaginationSupportedPlugin only support MySQL and MariaDB");
            return false;
        }

        offsetFieldName = properties.getProperty(PROPERTY_NAME_OFFSET_FIELD_NAME);
        offsetFieldTypeName = properties.getProperty(PROPERTY_NAME_OFFSET_FIELD_TYPE_NAME);
        rowCountFieldName = properties.getProperty(PROPERTY_NAME_ROW_COUNT_FIELD_NAME);
        rowCountFieldTypeName = properties.getProperty(PROPERTY_NAME_ROW_COUNT_FIELD_TYPE_NAME);

        final boolean hasOffsetFieldName = stringHasValue(offsetFieldName);
        final boolean hasOffsetFieldTypeName = stringHasValue(offsetFieldTypeName);
        final boolean hasRowCountFieldName = stringHasValue(rowCountFieldName);
        final boolean hasRowCountFieldTypeName = stringHasValue(rowCountFieldTypeName);

        final boolean valid = hasOffsetFieldName && hasOffsetFieldTypeName
                && hasRowCountFieldName && hasRowCountFieldTypeName;

        if (!valid) {
            if (!hasOffsetFieldName) {
                warnings.add("ExampleSelectPaginationSupportedPlugin.offsetFieldName is BLANK");
            }
            if (!hasOffsetFieldTypeName) {
                warnings.add("ExampleSelectPaginationSupportedPlugin.offsetFieldTypeName is BLANK");
            }
            if (!hasRowCountFieldName) {
                warnings.add("ExampleSelectPaginationSupportedPlugin.rowCountFieldName is BLANK");
            }
            if (!hasRowCountFieldTypeName) {
                warnings.add("ExampleSelectPaginationSupportedPlugin.rowCountFieldTypeName is BLANK");
            }
        }

        return valid;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        this.addLimit(topLevelClass, introspectedTable, rowCountFieldName, rowCountFieldTypeName);
        this.addLimit(topLevelClass, introspectedTable, offsetFieldName, offsetFieldTypeName);
        return true;
    }

    @Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        return this.sqlMapSelectByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        StringBuilder limitSb = new StringBuilder();
        limitSb.append("LIMIT").append(" #{").append(rowCountFieldName).append("} ");
        final String limitStr = limitSb.toString().trim();

        limitSb.append("OFFSET").append(" #{").append(offsetFieldName).append("} ");
        final String limitAndOffsetStr = limitSb.toString().trim();

        XmlElement whenOffsetNonNullElement = new XmlElement("when");
        whenOffsetNonNullElement.addAttribute(new Attribute("test", offsetFieldName + " != null"));
        whenOffsetNonNullElement.addElement(new TextElement(limitAndOffsetStr));

        XmlElement otherwiseOffsetIsNullElement = new XmlElement("otherwise");
        otherwiseOffsetIsNullElement.addElement(new TextElement(limitStr));

        XmlElement chooseElement = new XmlElement("choose");
        chooseElement.addElement(whenOffsetNonNullElement);
        chooseElement.addElement(otherwiseOffsetIsNullElement);

        XmlElement ifRowCountElement = new XmlElement("if");
        ifRowCountElement.addAttribute(new Attribute("test", rowCountFieldName + " != null"));
        ifRowCountElement.addElement(chooseElement);

        element.addElement(ifRowCountElement);

        return super.sqlMapUpdateByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }

    private void addLimit(TopLevelClass topLevelClass, IntrospectedTable introspectedTable, String fieldName, String filedTypeName) {
        CommentGenerator commentGenerator = this.context.getCommentGenerator();

        final FullyQualifiedJavaType fileType = new FullyQualifiedJavaType(filedTypeName);

        String javaDocStr = "";
        if (offsetFieldName.equals(fieldName)) {
            javaDocStr = "分页的数据偏移量，对应 OFFSET 后的参数。";
        }
        else if (rowCountFieldName.equals(fieldName)) {
            javaDocStr = "分页的数据量阈值，对应 LIMIT 后的参数。";
        }

        Field field = new Field(fieldName, fileType);
        field.setVisibility(JavaVisibility.PROTECTED);
        field.addJavaDocLine("/**");
        field.addJavaDocLine(" * " + javaDocStr);
        field.addJavaDocLine(" */");
//        commentGenerator.addFieldComment(field, introspectedTable);
        topLevelClass.addField(field);

        char c = fieldName.charAt(0);
        String camel = Character.toUpperCase(c) + fieldName.substring(1);
        Method method = new Method("set" + camel);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(new Parameter(fileType, fieldName));
        method.addBodyLine("this." + fieldName + " = " + fieldName + ";");
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);

        method = new Method("get" + camel);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(fileType);
        method.addBodyLine("return " + fieldName + ";");
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        topLevelClass.addMethod(method);
    }
}