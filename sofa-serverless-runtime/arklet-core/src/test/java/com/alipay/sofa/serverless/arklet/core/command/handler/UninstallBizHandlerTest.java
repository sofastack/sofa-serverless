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
package com.alipay.sofa.serverless.arklet.core.command.handler;

import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.UninstallBizHandler;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;
import com.alipay.sofa.serverless.arklet.core.common.exception.CommandValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.when;

public class UninstallBizHandlerTest extends BaseHandlerTest {

    private UninstallBizHandler handler;

    @Before
    public void setupInstallBizHandler() {
        handler = (UninstallBizHandler) commandService.getHandler(BuiltinCommand.UNINSTALL_BIZ);
    }

    @Test
    public void testHandle_Success() throws Throwable {
        UninstallBizHandler.Input input = new UninstallBizHandler.Input();
        input.setBizName("testBizName");
        input.setBizVersion("testBizVersion");

        when(handler.getOperationService().uninstall(input.getBizName(), input.getBizVersion()))
            .thenReturn(success);

        Output<ClientResponse> result = handler.handle(input);

        Assert.assertEquals(success, result.getData());
        Assert.assertTrue(result.success());
    }

    @Test
    public void testHandle_Failure() throws Throwable {
        UninstallBizHandler.Input input = new UninstallBizHandler.Input();
        input.setBizName("testBizName");
        input.setBizVersion("testBizVersion");

        when(handler.getOperationService().uninstall(input.getBizName(), input.getBizVersion()))
            .thenReturn(failed);

        Output<ClientResponse> result = handler.handle(input);

        Assert.assertSame(failed, result.getData());
        Assert.assertTrue(result.failed());
    }

    @Test(expected = CommandValidationException.class)
    public void testValidate_BlankBizName() throws CommandValidationException {
        // 准备测试数据
        UninstallBizHandler.Input input = new UninstallBizHandler.Input();
        input.setBizName("");
        input.setBizVersion("testBizVersion");

        // 执行测试
        handler.validate(input);
    }

    @Test(expected = CommandValidationException.class)
    public void testValidate_BlankBizVersion() throws CommandValidationException {
        // 准备测试数据
        UninstallBizHandler.Input input = new UninstallBizHandler.Input();
        input.setBizName("testBizName");
        input.setBizVersion("");

        // 执行测试
        handler.validate(input);
    }

    @Test(expected = CommandValidationException.class)
    public void testValidate_BlankRequestId() throws CommandValidationException {
        // 执行测试
        UninstallBizHandler.Input input = new UninstallBizHandler.Input();
        input.setAsync(true);
        input.setRequestId("");

        // 执行测试
        handler.validate(input);
    }

}