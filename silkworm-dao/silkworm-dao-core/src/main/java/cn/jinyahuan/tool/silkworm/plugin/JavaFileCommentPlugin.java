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
import java.util.Objects;

/**
 * 临时用的 java 文件 comment 插件。
 * todo comment 模板化
 *
 * @author Yahuan Jin
 * @since 1.0
 */
public class JavaFileCommentPlugin extends PluginAdapter {
    private static final String PROPERTY_NAME_IS_ADD_TABLE_INFO = "isAddTableInfo";
    private static final String PROPERTY_NAME_ADD_TABLE_INFO_POSITION = "tableInfoPosition";
    private static final String PROPERTY_NAME_IS_ADD_TABLE_FIELD_INFO = "isAddTableFieldInfo";
    private static final String PROPERTY_NAME_TABLE_FIELD_INFO_POSITION = "tableFieldInfoPosition";

    private static final String LINE_SEPARATOR = System.lineSeparator();


    protected String fileHeaderComment = new StringBuilder()
            .append("/*").append(LINE_SEPARATOR)
            // --- 拓展的注释写在中间
            .append(" * Copyright 2018-2020 The Silkworm Authors").append(LINE_SEPARATOR)
            // ---
            .append(" */").append(LINE_SEPARATOR)
            .toString();

    protected String classComment = new StringBuilder()
            .append("/**").append(LINE_SEPARATOR)
            // --- 拓展的注释写在中间
            .append(" * @author Yahuan Jin").append(LINE_SEPARATOR)
            .append(" * @since 1.0").append(LINE_SEPARATOR)
            // ---
            .append(" */").append(LINE_SEPARATOR)
            .toString();
    protected boolean isAddTableInfo = false;
    protected boolean isAddTableInfoToClassCommentHeader = true;

    protected String fieldComment = new StringBuilder()
            .append("/**").append(LINE_SEPARATOR)
            // --- 拓展的注释写在中间
            // ---
            .append(" */").append(LINE_SEPARATOR)
            .toString();
    protected boolean isAddTableFieldInfo = false;
    protected boolean isAddFieldInfoToFieldCommentHeader = true;

    // - - -

    @Override
    public boolean validate(List<String> warnings) {
        String isAddTableInfoProp = properties.getProperty(PROPERTY_NAME_IS_ADD_TABLE_INFO);
        String addTableInfoPositionProp = properties.getProperty(PROPERTY_NAME_ADD_TABLE_INFO_POSITION);
        String isAddTableFieldInfoProp = properties.getProperty(PROPERTY_NAME_IS_ADD_TABLE_FIELD_INFO);
        String tableFieldInfoPositionProp = properties.getProperty(PROPERTY_NAME_TABLE_FIELD_INFO_POSITION);

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

        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
                                                 IntrospectedTable introspectedTable)
    {
        addJavaFileComment(topLevelClass, fileHeaderComment);

        List<String> comments = spiltCommentByLineSeparator(classComment);
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
        List<String> comments = spiltCommentByLineSeparator(fieldComment);
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
        if (isNotEmpty(str)) {
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
        if (isNotBlank(tableName)) {
            String tableComment = " * table: " + tableName.trim();

            String tableRemark = introspectedTable.getRemarks();
            if (isNotBlank(tableRemark)) {
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
        if (isNotBlank(fieldName)) {
            String fieldComment = " * field: " + fieldName.trim();

            String fieldRemark = introspectedColumn.getRemarks();
            if (isNotBlank(fieldRemark)) {
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

    // - - -

    private static boolean isEmpty(final String str) {
        return Objects.isNull(str) || str.isEmpty();
    }

    private static boolean isNotEmpty(final String str) {
        return !isEmpty(str);
    }

    private static boolean isBlank(final String str) {
        return Objects.isNull(str) || isEmpty(str.trim());
    }

    private static boolean isNotBlank(final String str) {
        return !isBlank(str);
    }
}
