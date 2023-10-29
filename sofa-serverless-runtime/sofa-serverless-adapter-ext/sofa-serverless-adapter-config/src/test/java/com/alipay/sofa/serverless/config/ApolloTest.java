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

import com.alipay.sofa.serverless.config.bootstrap.SOFABootApplication;
import com.alipay.sofa.serverless.config.bootstrap.TestProperties;
import com.ctrip.framework.apollo.mockserver.EmbeddedApollo;
import org.jasypt.encryption.StringEncryptor;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * @author linnan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SOFABootApplication.class)
public class ApolloTest {

    // 启动apollo的mockserver
    @ClassRule
    public static EmbeddedApollo embeddedApollo = new EmbeddedApollo();
    @Autowired
    private TestProperties       testProperties;
    @Autowired
    private StringEncryptor      encryptor;

    @Test
    @DirtiesContext
    public void testConfigChange() throws InterruptedException {
        String namespace = "application";
        String key1 = "test.key1";
        String value1 = "value1";
        // 发布配置变更事件
        embeddedApollo.addOrModifyProperty(namespace, key1, value1);
        // 延时 2s 等待配置变更生效
        TimeUnit.SECONDS.sleep(2);
        assertEquals(value1, testProperties.getKey1());

        String key2 = "test.key2";
        String value2 = "value2";
        // 配置属性值加密
        String encryptedValue2 = encryptor.encrypt(value2);
        assertEquals(value2, encryptor.decrypt(encryptedValue2));
        // 发布配置变更事件 加密值配置格式: ENC(密文)
        embeddedApollo.addOrModifyProperty(namespace, key2, "ENC(" + encryptedValue2 + ")");
        // 延时 2s 等待配置变更生效
        TimeUnit.SECONDS.sleep(2);
        assertEquals(value2, testProperties.getKey2());
    }

}