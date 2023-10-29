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

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * 配置变更事件监听: 支持对 ConfigurationProperties 重绑定
 *
 * @author linnan
 * @see ConfigurationProperties
 */
public class BizConfigChangeEventListener implements ApplicationContextAware,
                                         ApplicationListener<BizConfigChangeEvent> {
    private ApplicationContext  applicationContext;
    /**
     * ConfigurationProperties Bean集合
     */
    private Map<String, Object> beans;

    @Override
    public void onApplicationEvent(BizConfigChangeEvent event) {
        if (this.applicationContext.equals(event.getSource())
        // Backwards compatible
            || event.getKeys().equals(event.getSource())) {
            // 重新绑定bean属性
            rebind();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        // 只处理ConfigurationProperties Bean
        beans = applicationContext.getBeansWithAnnotation(ConfigurationProperties.class);
    }

    private void rebind() {
        if (CollectionUtils.isEmpty(beans)) {
            return;
        }
        this.beans.forEach(this::rebind);
    }

    private void rebind(String name, Object bean) {
        if (bean == null || applicationContext == null) {
            return;
        }
        try {
            if (AopUtils.isAopProxy(bean) && bean instanceof Advised) {
                bean = ((Advised) bean).getTargetSource().getTarget();
            }
            if (bean != null) {
                this.applicationContext.getAutowireCapableBeanFactory().destroyBean(bean);
                this.applicationContext.getAutowireCapableBeanFactory().initializeBean(bean, name);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot rebind to " + name, e);
        }
    }
}
