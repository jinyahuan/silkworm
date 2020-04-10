/*
 * Copyright 2018-2020 The Silkworm Authors
 */

package cn.jinyahuan.tool.silkworm.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Yahuan Jin
 * @since 2.1
 */
public class JavaFileCommentResource {
    static final String HEADER_COMMENT = "comment.java.header";
    static final String CLASS_COMMENT = "comment.java.class";
    static final String FIELD_COMMENT = "comment.java.field";

    private static final int SPEC_SYMBOL = '=';
    private static final String DEFAULT_FILE_NAME = "META-INF/gen-java-file-comment.txt";
    private static final String CLASSPATH_PREFIX = "classpath:";

    private String fileName;
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
        File file;
        if (fileName.startsWith(CLASSPATH_PREFIX)) {
            String userDir = System.getProperty("user.dir");
            String resourceDir = "/src/main/resources";
            resourceDir = resourceDir.replaceAll("//", System.getProperty("file.separator"));
            String userResourceDir = userDir + resourceDir;
            file = new File(userResourceDir, fileName.replaceFirst(CLASSPATH_PREFIX, ""));
        }
        else {
            file = new File(fileName);
        }

        InputStream in;
        if (file.exists()) {
            in = new FileInputStream(file);
        }
        else {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        }

        ByteBuffer buffer = ByteBuffer.allocate(1024 * 8);

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
