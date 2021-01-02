/*
 * Copyright 2018-2021 The Silkworm Authors
 */

package cn.jinyahuan.tool.silkworm.plugin;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Java comment 插件。
 * todo 优化
 *
 * @author Yahuan Jin
 * @since 2.1
 */
public class JavaFileCommentPlugin extends PluginAdapter {
    private static final String PROPERTY_NAME_COMMENT_FILE_NAME = "commentFileName";

    /**
     * @see #addTableInfo
     */
    private static final String PROPERTY_NAME_ADD_TABLE_INFO = "addTableInfo";
    /**
     * @see #addTableName
     */
    private static final String PROPERTY_NAME_ADD_TABLE_NAME = "addTableName";
    /**
     * @see #addTableRemark
     */
    private static final String PROPERTY_NAME_ADD_TABLE_REMARK = "addTableRemark";
    /**
     * @see #tableInfoOffsetLine
     */
    private static final String PROPERTY_NAME_ADD_TABLE_INFO_OFFSET_LINE = "tableInfoOffsetLine";

    /**
     * @see #addTableColumnInfo
     */
    private static final String PROPERTY_NAME_ADD_TABLE_COLUMN_INFO = "addTableColumnInfo";
    /**
     * @see #addTableColumnName
     */
    private static final String PROPERTY_NAME_ADD_TABLE_COLUMN_NAME = "addTableColumnName";
    /**
     * @see #addTableColumnRemark
     */
    private static final String PROPERTY_NAME_ADD_TABLE_COLUMN_REMARK = "addTableColumnRemark";
    /**
     * @see #tableColumnInfoOffsetLine
     */
    private static final String PROPERTY_NAME_ADD_TABLE_COLUMN_INFO_OFFSET_LINE = "tableColumnInfoOffsetLine";

    private static final String LINE_SEPARATOR = "\n";

    /**
     * 是否添加数据库表信息。
     */
    protected boolean addTableInfo = false;
    /**
     * 是否添加表的名称。
     */
    protected boolean addTableName = false;
    /**
     * 是否添加表的备注。
     */
    protected boolean addTableRemark = false;
    /**
     * 添加表信息到注释的偏移行数。值必须大于等于1，且小于类注释总行数-1（第一行和最后一行预留给注释的开始和结束）。
     */
    protected int tableInfoOffsetLine = 1;

    /**
     * 是否添加表的列信息到备注。
     */
    protected boolean addTableColumnInfo = false;
    /**
     * 是否添加表的列名称。
     */
    protected boolean addTableColumnName = false;
    /**
     * 是否添加表的列备注。
     */
    protected boolean addTableColumnRemark = false;
    /**
     * 添加表的列信息到注释的偏移行数。值必须大于等于1，且小于类注释总行数-1（第一行和最后一行预留给注释的开始和结束）。
     */
    protected int tableColumnInfoOffsetLine = 1;

    private JavaFileCommentResource commentResource;

    @Override
    public boolean validate(List<String> warnings) {
        String addTableInfoProp = properties.getProperty(PROPERTY_NAME_ADD_TABLE_INFO);
        String addTableNameProp = properties.getProperty(PROPERTY_NAME_ADD_TABLE_NAME);
        String addTableRemarkProp = properties.getProperty(PROPERTY_NAME_ADD_TABLE_REMARK);
        String tableInfoOffsetLineProp = properties.getProperty(PROPERTY_NAME_ADD_TABLE_INFO_OFFSET_LINE);

        if (StringUtils.isNotBlank(addTableInfoProp)) {
            addTableInfo = Boolean.valueOf(addTableInfoProp);
            if (addTableInfo) {
                if (StringUtils.isNotBlank(addTableNameProp)) {
                    addTableName = Boolean.valueOf(addTableNameProp);
                }
                if (StringUtils.isNotBlank(addTableRemarkProp)) {
                    addTableRemark = Boolean.valueOf(addTableRemarkProp);
                }
                if (StringUtils.isNotBlank(tableInfoOffsetLineProp)) {
                    tableInfoOffsetLine = Integer.parseInt(tableInfoOffsetLineProp);
                    if (tableInfoOffsetLine < 1) {
                        warnings.add("JavaFileCommentPlugin.tableInfoOffsetLine < 1");
                        return false;
                    }
                }
            }
        }

        // - - -

        String addTableColumnInfoProp = properties.getProperty(PROPERTY_NAME_ADD_TABLE_COLUMN_INFO);
        String addTableColumnNameProp = properties.getProperty(PROPERTY_NAME_ADD_TABLE_COLUMN_NAME);
        String addTableColumnRemarkProp = properties.getProperty(PROPERTY_NAME_ADD_TABLE_COLUMN_REMARK);
        String tableColumnInfoOffsetLineProp = properties.getProperty(PROPERTY_NAME_ADD_TABLE_COLUMN_INFO_OFFSET_LINE);

        if (StringUtils.isNotBlank(addTableColumnInfoProp)) {
            addTableColumnInfo = Boolean.valueOf(addTableColumnInfoProp);
            if (addTableColumnInfo) {
                if (StringUtils.isNotBlank(addTableColumnNameProp)) {
                    addTableColumnName = Boolean.valueOf(addTableColumnNameProp);
                }
                if (StringUtils.isNotBlank(addTableColumnRemarkProp)) {
                    addTableColumnRemark = Boolean.valueOf(addTableColumnRemarkProp);
                }
                if (StringUtils.isNotBlank(tableColumnInfoOffsetLineProp)) {
                    tableColumnInfoOffsetLine = Integer.parseInt(tableColumnInfoOffsetLineProp);
                    if (tableColumnInfoOffsetLine < 1) {
                        warnings.add("JavaFileCommentPlugin.tableColumnInfoOffsetLine < 1");
                        return false;
                    }
                }
            }
        }

        // - - -

        String commentFileNameProp = properties.getProperty(PROPERTY_NAME_COMMENT_FILE_NAME);
        commentResource = new JavaFileCommentResource(commentFileNameProp);

        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
                                                 IntrospectedTable introspectedTable)
    {
        addFileTopComment(topLevelClass, introspectedTable);

        addClassComment(topLevelClass, introspectedTable);

        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field,
                                       TopLevelClass topLevelClass,
                                       IntrospectedColumn introspectedColumn,
                                       IntrospectedTable introspectedTable,
                                       ModelClassType modelClassType)
    {
        addFieldComment(field, topLevelClass, introspectedColumn, introspectedTable, modelClassType);

        return true;
    }

    // - - -

    /**
     * 添加 java 文件顶部的注释。
     *
     * @param topLevelClass
     */
    protected void addFileTopComment(TopLevelClass topLevelClass,
                                     IntrospectedTable introspectedTable)
    {
        String comment = commentResource.getCommentConfig(JavaFileCommentResource.HEADER_COMMENT);
        List<String> comments = spiltCommentByLineSeparator(comment);
        if (!comments.isEmpty()) {
            for (String line : comments) {
                topLevelClass.addFileCommentLine(line);
            }

            // 加一行空行，也就是与 package 间空一行
            topLevelClass.addFileCommentLine("");
        }
    }

    /**
     * 添加 class 类注释。
     *
     * @param topLevelClass
     * @param introspectedTable
     */
    protected void addClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String comment = commentResource.getCommentConfig(JavaFileCommentResource.CLASS_COMMENT);
        List<String> comments = spiltCommentByLineSeparator(comment);
        if (!comments.isEmpty()) {
            if (addTableInfo) {
                appendTableInfoToComments(comments, introspectedTable);
            }

            removeEmptyComment(comments);

            for (String line : comments) {
                topLevelClass.addJavaDocLine(line);
            }
        }
    }

    protected void addFieldComment(Field field,
                                   TopLevelClass topLevelClass,
                                   IntrospectedColumn introspectedColumn,
                                   IntrospectedTable introspectedTable,
                                   ModelClassType modelClassType)
    {
        String comment = commentResource.getCommentConfig(JavaFileCommentResource.FIELD_COMMENT);
        List<String> comments = spiltCommentByLineSeparator(comment);
        if (!comments.isEmpty()) {
            if (addTableColumnInfo) {
                addFieldInfoToFieldCommentHeader(comments, introspectedColumn);
            }

            removeEmptyComment(comments);

            for (String line : comments) {
                field.addJavaDocLine(line);
            }
        }
    }

    /**
     * 移除掉那多余的空行注释。
     *
     * @param comments
     */
    protected void removeEmptyComment(final List<String> comments) {
        // 理论上至少会有两个，即 /** 和 */
        // 少于两个就不要注释了
        if (comments.size() <= 2) {
            comments.clear();
        }
    }

    private void appendTableInfoToComments(List<String> list, IntrospectedTable introspectedTable) {
        String tableName = introspectedTable.getFullyQualifiedTable().getIntrospectedTableName();
        if (StringUtils.isNotBlank(tableName)) {
            StringBuilder sb = new StringBuilder();

            // 表的备注信息
            if (addTableRemark) {
                String tableRemark = introspectedTable.getRemarks();
                if (StringUtils.isNotBlank(tableRemark)) {
                    tableRemark = tableRemark.trim();
                    sb.append(" * ").append(tableRemark).append("。");
                }
            }
            // 表名
            if (addTableName) {
                tableName = tableName.trim();
                sb.append(LINE_SEPARATOR).append(" * table: ").append(tableName).append(".");
            }

            String tableComment = sb.toString();

            if (StringUtils.isNotBlank(tableComment)) {
                int currentCommentLines = list.size();
                if (tableInfoOffsetLine >= currentCommentLines) {
                    throw new RuntimeException("tableInfoOffsetLine[" + tableInfoOffsetLine +
                            "] must < currentCommentLines[" + currentCommentLines + "]");
                }
                list.add(tableInfoOffsetLine, tableComment);
            }
        }
    }

    private void addFieldInfoToFieldCommentHeader(List<String> list,
                                                  IntrospectedColumn introspectedColumn)
    {
        String fieldName = introspectedColumn.getActualColumnName();
        if (StringUtils.isNotBlank(fieldName)) {
            StringBuilder sb = new StringBuilder();

            // 列名备注
            if (addTableColumnRemark) {
                String fieldRemark = introspectedColumn.getRemarks();
                if (StringUtils.isNotBlank(fieldRemark)) {
                    fieldRemark = fieldRemark.trim();
                    sb.append(" * ").append(fieldRemark).append("。");
                }
            }
            // 列名
            if (addTableColumnName) {
                fieldName = fieldName.trim();
                sb.append(LINE_SEPARATOR).append(" * field: ").append(fieldName).append(".");
            }

            String fieldComment = sb.toString();

            if (StringUtils.isNotBlank(fieldComment)) {
                int currentCommentLines = list.size();
                if (tableColumnInfoOffsetLine >= currentCommentLines) {
                    throw new RuntimeException("tableColumnInfoOffsetLine[" + tableColumnInfoOffsetLine +
                            "] must < currentCommentLines[" + currentCommentLines + "]");
                }
                List<String> tableRowCommentLines = spiltCommentByLineSeparator(fieldComment);
                int offset = 0;
                for (String line : tableRowCommentLines) {
                    if (StringUtils.isNotBlank(line)) {
                        list.add(tableColumnInfoOffsetLine + (offset++), line);
                    }
                }
            }
        }
    }

    static List<String> spiltCommentByLineSeparator(final String str) {
        List<String> result = Collections.emptyList();
        if (StringUtils.isNotEmpty(str)) {
            String[] arr = str.split(LINE_SEPARATOR);
            if (arr.length > 0) {
                result = new ArrayList<>(arr.length);
                for (String s : arr) {
                    result.add(s);
                }
            }
        }
        return result;
    }
}
