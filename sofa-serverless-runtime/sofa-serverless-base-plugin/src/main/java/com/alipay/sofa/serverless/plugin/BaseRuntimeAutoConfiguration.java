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
package com.alipay.sofa.serverless.plugin;

import com.alipay.sofa.serverless.common.BizRuntimeContext;
import com.alipay.sofa.serverless.common.environment.ConditionalOnNotMasterBiz;
import com.alipay.sofa.serverless.common.service.ArkAutowiredBeanPostProcessor;
import com.alipay.sofa.serverless.common.BizRuntimeContextRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author mingmen
 * @date 2023/6/14
 */
@Configuration
public class BaseRuntimeAutoConfiguration {

    @Bean
    public BizRuntimeContext bizRuntimeContext(ApplicationContext applicationContext) {
        ClassLoader classLoader = applicationContext.getClassLoader();
        BizRuntimeContext bizRuntimeContext = BizRuntimeContextRegistry
            .getBizRuntimeContextByClassLoader(classLoader);
        bizRuntimeContext.setRootApplicationContext(applicationContext);
        return bizRuntimeContext;
    }

    @Bean
    @ConditionalOnMissingBean
    //    @ConditionalOnNotMasterBiz
    public ArkAutowiredBeanPostProcessor arkAutowiredBeanPostProcessor() {
        return new ArkAutowiredBeanPostProcessor();
    }
}
