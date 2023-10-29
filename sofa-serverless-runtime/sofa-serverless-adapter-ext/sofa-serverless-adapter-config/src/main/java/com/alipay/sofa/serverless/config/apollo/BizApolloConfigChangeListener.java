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
package com.alipay.sofa.serverless.config.apollo;

import com.alipay.sofa.serverless.config.BizConfigChangeEvent;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.google.common.base.Splitter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 适配Apollo: 发布配置变更事件, 触发 Jaspyt 属性源刷新和 ConfigurationProperties 重绑定
 *
 * @author linnan
 */
public class BizApolloConfigChangeListener implements ApplicationContextAware {
    public static final Splitter NAMESPACE_SPLITTER = Splitter.on(",").omitEmptyStrings()
                                                        .trimResults();
    @Value("${apollo.bootstrap.namespaces:application}")
    private String               namespacesStr;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (StringUtils.hasText(namespacesStr)) {
            List<String> namespaces = NAMESPACE_SPLITTER.splitToList(namespacesStr);
            for (String namespace : namespaces) {
                Config publicConfig = ConfigService.getConfig(namespace);
                // 监听到Apollo变化发布配置变更事件
                publicConfig.addChangeListener(changeEvent -> applicationContext.publishEvent(new BizConfigChangeEvent(applicationContext, changeEvent.changedKeys())));
            }
        }
    }
}
