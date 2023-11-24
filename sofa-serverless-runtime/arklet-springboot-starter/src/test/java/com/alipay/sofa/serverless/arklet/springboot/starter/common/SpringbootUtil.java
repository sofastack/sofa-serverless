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
package com.alipay.sofa.serverless.arklet.springboot.starter.common;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

public class SpringbootUtil {

    private static ApplicationContext context;

    private static Environment        environment;

    public static void initSpringbootUtil(ApplicationContext applicationContext) {
        context = applicationContext;
        environment = context.getEnvironment();
    }

    public static ApplicationContext getContext() {
        return context;
    }

    public static Environment getEnvironment() {
        return environment;
    }

    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    public static String getProperty(String key) {
        return environment.getProperty(key);
    }
}
