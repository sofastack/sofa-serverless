/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.serverless.arklet.core.ops;

import com.alipay.sofa.serverless.arklet.core.common.log.ArkletLogger;
import com.alipay.sofa.serverless.arklet.core.common.log.ArkletLoggerFactory;
import lombok.SneakyThrows;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * 合并部署帮助类。
 * @author gouzhendong.gzd
 * @version $Id: CombineInstallService, v 0.1 2023-11-20 15:35 gouzhendong.gzd Exp $
 */
public class CombineInstallHelper {

    private static final ArkletLogger LOGGER = ArkletLoggerFactory.getDefaultLogger();

    /**
     * 判断是否是 biz jar 文件
     * 目前简单的以后缀 '-biz.jar' 为约束。
     * @param path 文件路径。
     * @return 是否是 biz jar 文件。
     */
    public boolean isBizJarFile(Path path) {
        return path.toString().endsWith("-biz.jar");
    }

    @SneakyThrows
    public List<String> getBizUrlsFromLocalFileSystem(String absoluteBizDirPath) {
        List<String> bizUrls = new ArrayList<>();
        Files.walkFileTree(Paths.get(absoluteBizDirPath), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                Path absolutePath = file.toAbsolutePath();
                if (isBizJarFile(absolutePath)) {
                    LOGGER.info("Found biz jar file: {}", absolutePath);
                    bizUrls.add(absolutePath.toString());
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return bizUrls;
    }

}