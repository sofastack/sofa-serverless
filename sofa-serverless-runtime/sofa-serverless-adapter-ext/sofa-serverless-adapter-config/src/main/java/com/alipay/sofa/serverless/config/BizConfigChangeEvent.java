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

import org.springframework.context.ApplicationEvent;

import java.util.Set;

/**
 * biz 模块配置变更事件<br>
 * 配置中心监听到变更后发布此事件
 *
 * @author linnan
 */
public class BizConfigChangeEvent extends ApplicationEvent {

    private final Set<String> keys;

    public BizConfigChangeEvent(Set<String> keys) {
        // Backwards compatible constructor with less utility (practically no use at all)
        this(keys, keys);
    }

    public BizConfigChangeEvent(Object context, Set<String> keys) {
        super(context);
        this.keys = keys;
    }

    /**
     * @return The keys.
     */
    public Set<String> getKeys() {
        return keys;
    }
}
