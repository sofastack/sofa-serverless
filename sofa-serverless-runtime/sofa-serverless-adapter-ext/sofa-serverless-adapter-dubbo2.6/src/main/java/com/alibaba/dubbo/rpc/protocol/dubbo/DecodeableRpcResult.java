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
package com.alibaba.dubbo.rpc.protocol.dubbo;

import com.alibaba.dubbo.common.ClassLoaderUtil;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.serialize.Cleanable;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.java.ClassLoaderJavaObjectInput;
import com.alibaba.dubbo.common.serialize.java.ClassLoaderObjectInputStream;
import com.alibaba.dubbo.common.utils.Assert;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Codec;
import com.alibaba.dubbo.remoting.Decodeable;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.remoting.transport.CodecSupport;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.support.RpcUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Map;

public class DecodeableRpcResult extends RpcResult implements Codec, Decodeable {

    private static final Logger log = LoggerFactory.getLogger(DecodeableRpcResult.class);

    private Channel             channel;

    private byte                serializationType;

    private InputStream         inputStream;

    private Response            response;

    private Invocation          invocation;

    private volatile boolean    hasDecoded;

    public DecodeableRpcResult(Channel channel, Response response, InputStream is,
                               Invocation invocation, byte id) {
        Assert.notNull(channel, "channel == null");
        Assert.notNull(response, "response == null");
        Assert.notNull(is, "inputStream == null");
        this.channel = channel;
        this.response = response;
        this.inputStream = is;
        this.invocation = invocation;
        this.serializationType = id;
    }

    @Override
    public void encode(Channel channel, OutputStream output, Object message) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object decode(Channel channel, InputStream input) throws IOException {
        ObjectInput in = CodecSupport.getSerialization(channel.getUrl(), serializationType)
            .deserialize(channel.getUrl(), input);
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader cl = invocation.getInvoker().getInterface().getClassLoader();
        Thread.currentThread().setContextClassLoader(cl);
        if (in instanceof ClassLoaderJavaObjectInput) {
            InputStream is = ((ClassLoaderJavaObjectInput) in).getInputStream();
            if (is instanceof ClassLoaderObjectInputStream) {
                ((ClassLoaderObjectInputStream) is).setClassLoader(cl);
            }
        }
        byte flag = in.readByte();
        switch (flag) {
            case DubboCodec.RESPONSE_NULL_VALUE:
                break;
            case DubboCodec.RESPONSE_VALUE:
                try {
                    Type[] returnType = RpcUtils.getReturnTypes(invocation);
                    setValue(returnType == null || returnType.length == 0 ? in.readObject()
                        : (returnType.length == 1 ? in.readObject((Class<?>) returnType[0]) : in
                            .readObject((Class<?>) returnType[0], returnType[1])));
                } catch (ClassNotFoundException e) {
                    throw new IOException(StringUtils.toString("Read response data failed.", e));
                } finally {
                    Thread.currentThread().setContextClassLoader(originalClassLoader);
                }
                break;
            case DubboCodec.RESPONSE_WITH_EXCEPTION:
                try {
                    Object obj = in.readObject();
                    if (obj instanceof Throwable == false)
                        throw new IOException("Response data error, expect Throwable, but get "
                                              + obj);
                    setException((Throwable) obj);
                } catch (ClassNotFoundException e) {
                    throw new IOException(StringUtils.toString("Read response data failed.", e));
                } finally {
                    Thread.currentThread().setContextClassLoader(originalClassLoader);
                }
                break;
            case DubboCodec.RESPONSE_NULL_VALUE_WITH_ATTACHMENTS:
                try {
                    setAttachments((Map<String, String>) in.readObject(Map.class));
                } catch (ClassNotFoundException e) {
                    throw new IOException(StringUtils.toString("Read response data failed.", e));
                } finally {
                    Thread.currentThread().setContextClassLoader(originalClassLoader);
                }
                break;
            case DubboCodec.RESPONSE_VALUE_WITH_ATTACHMENTS:
                try {
                    Type[] returnType = RpcUtils.getReturnTypes(invocation);
                    setValue(returnType == null || returnType.length == 0 ? in.readObject()
                        : (returnType.length == 1 ? in.readObject((Class<?>) returnType[0]) : in
                            .readObject((Class<?>) returnType[0], returnType[1])));
                    setAttachments((Map<String, String>) in.readObject(Map.class));
                } catch (ClassNotFoundException e) {
                    throw new IOException(StringUtils.toString("Read response data failed.", e));
                } finally {
                    Thread.currentThread().setContextClassLoader(originalClassLoader);
                }
                break;
            case DubboCodec.RESPONSE_WITH_EXCEPTION_WITH_ATTACHMENTS:
                try {
                    Object obj = in.readObject();
                    if (obj instanceof Throwable == false)
                        throw new IOException("Response data error, expect Throwable, but get "
                                              + obj);
                    setException((Throwable) obj);
                    setAttachments((Map<String, String>) in.readObject(Map.class));
                } catch (ClassNotFoundException e) {
                    throw new IOException(StringUtils.toString("Read response data failed.", e));
                } finally {
                    Thread.currentThread().setContextClassLoader(originalClassLoader);
                }
                break;
            default:
                throw new IOException("Unknown result flag, expect '0' '1' '2', get " + flag);
        }
        if (in instanceof Cleanable) {
            ((Cleanable) in).cleanup();
        }
        Thread.currentThread().setContextClassLoader(originalClassLoader);
        return this;
    }

    @Override
    public void decode() throws Exception {
        if (!hasDecoded && channel != null && inputStream != null) {
            try {
                decode(channel, inputStream);
            } catch (Throwable e) {
                if (log.isWarnEnabled()) {
                    log.warn("Decode rpc result failed: " + e.getMessage(), e);
                }
                response.setStatus(Response.CLIENT_ERROR);
                response.setErrorMessage(StringUtils.toString(e));
            } finally {
                hasDecoded = true;
            }
        }
    }

}
