/*
 * Copyright 2018-2020 The Silkworm Authors
 */

package cn.jinyahuan.tool.silkworm.plugin;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Java comment 插件。
 * todo 优化
 *
 * @author Yahuan Jin
 * @since 2.1
 */
public class JavaFileCommentPlugin extends PluginAdapter {
    private static final String PROPERTY_NAME_IS_ADD_TABLE_INFO = "isAddTableInfo";
    private static final String PROPERTY_NAME_ADD_TABLE_INFO_POSITION = "tableInfoPosition";
    private static final String PROPERTY_NAME_IS_ADD_TABLE_FIELD_INFO = "isAddTableFieldInfo";
    private static final String PROPERTY_NAME_TABLE_FIELD_INFO_POSITION = "tableFieldInfoPosition";
    private static final String PROPERTY_NAME_COMMENT_FILE_NAME = "commentFileName";

    private static final String LINE_SEPARATOR = "\n";

    protected boolean isAddTableInfo = false;
    protected boolean isAddTableInfoToClassCommentHeader = true;

    protected boolean isAddTableFieldInfo = false;
    protected boolean isAddFieldInfoToFieldCommentHeader = true;

    private JavaFileCommentResource commentResource = null;

    // - - -

    @Override
    public boolean validate(List<String> warnings) {
        String isAddTableInfoProp = properties.getProperty(PROPERTY_NAME_IS_ADD_TABLE_INFO);
        String addTableInfoPositionProp = properties.getProperty(PROPERTY_NAME_ADD_TABLE_INFO_POSITION);
        String isAddTableFieldInfoProp = properties.getProperty(PROPERTY_NAME_IS_ADD_TABLE_FIELD_INFO);
        String tableFieldInfoPositionProp = properties.getProperty(PROPERTY_NAME_TABLE_FIELD_INFO_POSITION);
        String commentFileNameProp = properties.getProperty(PROPERTY_NAME_COMMENT_FILE_NAME);

        if (Boolean.TRUE.toString().equals(isAddTableInfoProp)) {
            isAddTableInfo = true;
            if (!"top".equalsIgnoreCase(addTableInfoPositionProp)) {
                isAddTableInfoToClassCommentHeader = false;
            }
        }
        if (Boolean.TRUE.toString().equals(isAddTableFieldInfoProp)) {
            isAddTableFieldInfo = true;
            if (!"top".equalsIgnoreCase(tableFieldInfoPositionProp)) {
                isAddFieldInfoToFieldCommentHeader = false;
            }
        }

        commentResource = new JavaFileCommentResource(commentFileNameProp);

        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
                                                 IntrospectedTable introspectedTable)
    {
        addJavaFileComment(topLevelClass,
                commentResource.getCommentConfig(JavaFileCommentResource.HEADER_COMMENT));

        List<String> comments = spiltCommentByLineSeparator(
                commentResource.getCommentConfig(JavaFileCommentResource.CLASS_COMMENT));
        if (!comments.isEmpty()) {
            if (isAddTableInfo) {
                addTableInfoToClassCommentHeader(comments, introspectedTable, isAddTableInfoToClassCommentHeader);
            }

            if (comments.size() > 2) {
                for (String comment : comments) {
                    topLevelClass.addJavaDocLine(comment);
                }
            }
        }
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field,
                                       TopLevelClass topLevelClass,
                                       IntrospectedColumn introspectedColumn,
                                       IntrospectedTable introspectedTable,
                                       ModelClassType modelClassType)
    {
        List<String> comments = spiltCommentByLineSeparator(
                commentResource.getCommentConfig(JavaFileCommentResource.FIELD_COMMENT));
        if (!comments.isEmpty()) {
            if (isAddTableFieldInfo) {
                addFieldInfoToFieldCommentHeader(comments, introspectedColumn, isAddFieldInfoToFieldCommentHeader);
            }

            if (comments.size() > 2) {
                for (String comment : comments) {
                    field.addJavaDocLine(comment);
                }
            }
        }
        return true;
    }

    // - - -

    protected void addJavaFileComment(TopLevelClass topLevelClass, String fileHeaderComment) {
        List<String> comments = spiltCommentByLineSeparator(fileHeaderComment);
        if (!comments.isEmpty()) {
            if (comments.size() > 2) {
                for (String comment : comments) {
                    topLevelClass.addFileCommentLine(comment);
                }
                topLevelClass.addFileCommentLine("");
            }
        }
    }

    protected static List<String> spiltCommentByLineSeparator(final String str) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isNotEmpty(str)) {
            String[] arr = str.split(LINE_SEPARATOR);
            if (arr.length > 0) {
                for (String s : arr) {
                    result.add(s);
                }
            }
        }
        return result;
    }

    private void addTableInfoToClassCommentHeader(List<String> list,
                                                  IntrospectedTable introspectedTable,
                                                  boolean isAddTableInfoToClassCommentHeader)
    {
        String tableName = introspectedTable.getFullyQualifiedTable().getIntrospectedTableName();
        if (StringUtils.isNotBlank(tableName)) {
            String tableComment = " * table: " + tableName.trim();

            String tableRemark = introspectedTable.getRemarks();
            if (StringUtils.isNotBlank(tableRemark)) {
                tableComment += " (" + tableRemark.trim() + ")。";
            }

            if (isAddTableInfoToClassCommentHeader) {
                int insertCount = 0;
                list.add(++insertCount, tableComment);
                list.add(++insertCount, " *");
            }
            else {
                list.add(list.size() - 1, tableComment);
            }
        }
    }

    private void addFieldInfoToFieldCommentHeader(List<String> list,
                                                  IntrospectedColumn introspectedColumn,
                                                  boolean isAddFieldInfoToFieldCommentHeader)
    {
        String fieldName = introspectedColumn.getActualColumnName();
        if (StringUtils.isNotBlank(fieldName)) {
            String fieldComment = " * field: " + fieldName.trim();

            String fieldRemark = introspectedColumn.getRemarks();
            if (StringUtils.isNotBlank(fieldRemark)) {
                fieldComment += " (" + fieldRemark.trim() + ")。";
            }

            if (isAddFieldInfoToFieldCommentHeader) {
                int insertCount = 0;
                list.add(++insertCount, fieldComment);
            }
            else {
                list.add(list.size() - 1, fieldComment);
            }
        }
    }
}
