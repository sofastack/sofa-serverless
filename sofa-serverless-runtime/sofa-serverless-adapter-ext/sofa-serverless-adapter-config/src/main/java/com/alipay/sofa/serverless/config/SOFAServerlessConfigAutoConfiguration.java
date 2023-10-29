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
package com.alipay.sofa.serverless.config;

import com.alipay.sofa.serverless.config.apollo.BizApolloConfigChangeListener;
import com.alipay.sofa.serverless.config.jasypt.BizJasyptConfigChangeEventListener;
import com.ctrip.framework.apollo.ConfigChangeListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 适配 Apollo配置中心, Jasypt配置加密
 *
 * @author linnan
 */
public class SOFAServerlessConfigAutoConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "apollo.bootstrap", name = "enabled", havingValue = "true")
    @ConditionalOnClass(value = ConfigChangeListener.class)
    public BizApolloConfigChangeListener bizApolloConfigChangeListener() {
        return new BizApolloConfigChangeListener();
    }

    @Bean
    @ConditionalOnClass(name = "com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter")
    public BizJasyptConfigChangeEventListener bizJasyptConfigChangeEventListener(ConfigurableEnvironment env,
                                                                                 ApplicationContext applicationContext) {
        return new BizJasyptConfigChangeEventListener(env, applicationContext);
    }

    @Bean
    public BizConfigChangeEventListener bizConfigChangeEventListener() {
        return new BizConfigChangeEventListener();
    }
}
