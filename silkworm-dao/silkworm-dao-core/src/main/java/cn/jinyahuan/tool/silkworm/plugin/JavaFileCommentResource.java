/*
 * Copyright 2018-2021 The Silkworm Authors
 */

package cn.jinyahuan.tool.silkworm.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Yahuan Jin
 * @since 2.1
 */
public class JavaFileCommentResource {
    /**
     * java 文件顶部的注释，比如：copyright。
     */
    public static final String HEADER_COMMENT = "comment.java.model.header";
    /**
     * 类注释。
     */
    public static final String CLASS_COMMENT = "comment.java.model.class";
    /**
     * 字段注释。
     */
    public static final String FIELD_COMMENT = "comment.java.model.field";
    /**
     * 导入配置的资源文件路径。
     */
    public static final String RESOURCE_PATH = "_resource.path";
    /**
     * 导入配置的资源文件总数量。
     */
    public static final String RESOURCE_COUNT = "_resource.count";

    private static final int SPEC_SYMBOL = '=';
    private static final String DEFAULT_FILE_NAME = "META-INF/silkworm-comment-config.txt";

    /**
     * <pre>
     * 示例1，当前项目中的相对路径: ./src/main/resources/META-INF/silkworm-comment-config.txt
     * 示例2，相对路径: META-INF/silkworm-comment-config.txt
     * 示例3，绝对路径: (windows操作系统: "E:\config\silkworm-comment-config.txt")
     * </pre>
     */
    private String fileName;
    /**
     * <pre>
     * key: {@link #HEADER_COMMENT}, {@link #CLASS_COMMENT}, {@link #FIELD_COMMENT}.
     * 特殊的key: {@link #RESOURCE_PATH}, {@link #RESOURCE_COUNT}.
     * </pre>
     */
    private Map<String, String> commentConfigs;

    public JavaFileCommentResource() {
        this(DEFAULT_FILE_NAME);
    }

    public JavaFileCommentResource(String fileName) {
        this.commentConfigs = new HashMap<>();
        this.fileName = StringUtils.isBlank(fileName) ? DEFAULT_FILE_NAME : fileName;

        try {
            loadCommentConfig(this.fileName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getCommentConfig(String key) {
        return commentConfigs.get(key);
    }

    private void loadCommentConfig(String fileName) throws IOException {
        commentConfigs.put(RESOURCE_PATH, fileName);
        commentConfigs.put(RESOURCE_COUNT, "1");

        File file = new File(fileName);

        InputStream in;
        if (file.exists()) {
            in = new FileInputStream(file);
        }
        else {
            Thread currentThread = Thread.currentThread();
            Enumeration<URL> resources = currentThread.getContextClassLoader().getResources(fileName);

            URL url = null;
            int resourceCount = 0;
            while (resources.hasMoreElements()) {
                resourceCount++;
                // 如果有多个，则使用第一个；如果不是预期的配置文件，则推荐使用绝对路径，或者项目中的相对路径
                if (Objects.isNull(url)) {
                    url = resources.nextElement();
                }
            }

            in = url.openStream();

            commentConfigs.put(RESOURCE_PATH, url.getFile());
            commentConfigs.put(RESOURCE_COUNT, String.valueOf(resourceCount));
        }

        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);

        String key = "";
        String value = "";

        int markCount = 0;
        int val = -1;
        while ((val = in.read()) != -1) {
            if (val == SPEC_SYMBOL) {
                buffer.mark();
                if (++markCount == 3) {
                    key = new String(buffer.array()).trim();
                    buffer.compact();
                    buffer.rewind();
                }
                else if (markCount == 6) {
                    value = new String(buffer.array()).trim();
                    commentConfigs.put(key, value);
                    markCount = 0;
                    buffer.compact();
                    buffer.rewind();
                }
                continue;
            }
            buffer.put((byte) val);
        }

        in.close();
    }
}
