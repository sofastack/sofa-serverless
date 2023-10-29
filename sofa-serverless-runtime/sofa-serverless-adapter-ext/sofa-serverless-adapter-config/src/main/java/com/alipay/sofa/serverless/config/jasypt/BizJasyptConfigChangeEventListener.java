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
package com.alipay.sofa.serverless.config.jasypt;

import com.alipay.sofa.serverless.config.BizConfigChangeEvent;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

/**
 * 适配 Jaspyt: 监听配置变更事件, 刷新Jasypt属性源
 *
 * @author linnan
 */
public class BizJasyptConfigChangeEventListener implements
                                               ApplicationListener<BizConfigChangeEvent>, Ordered {
    private final ConfigurableEnvironment environment;
    private final ApplicationContext      applicationContext;

    public BizJasyptConfigChangeEventListener(ConfigurableEnvironment environment,
                                              ApplicationContext applicationContext) {
        this.environment = environment;
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(BizConfigChangeEvent event) {
        if (this.applicationContext.equals(event.getSource())
                // Backwards compatible
                || event.getKeys().equals(event.getSource())) {
            environment.getPropertySources().forEach(this::refreshPropertySource);
        }
    }

    @SuppressWarnings("rawtypes")
    private void refreshPropertySource(PropertySource<?> propertySource) {
        // 刷新缓存
        if (propertySource instanceof CompositePropertySource) {
            CompositePropertySource cps = (CompositePropertySource) propertySource;
            cps.getPropertySources().forEach(this::refreshPropertySource);
        } else if (propertySource instanceof EncryptablePropertySource) {
            EncryptablePropertySource eps = (EncryptablePropertySource) propertySource;
            // 实际上就是把cache清空
            eps.refresh();
        }
    }

    @Override
    public int getOrder() {
        // 优先级要高一些 需要先清缓存再重新绑定bean属性
        return HIGHEST_PRECEDENCE + 3000;
    }
}
