package io.kimmking.rpcfx.client;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import io.kimmking.rpcfx.api.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class Rpcfx {

    private static StringBuffer stringBuffer = new StringBuffer();
    static {
        ParserConfig.getGlobalInstance().addAccept("io.kimmking");
    }

    public static <T, filters> T createFromRegistry(final Class<T> serviceClass, final String zkUrl, Router router, LoadBalancer loadBalance, Filter filter) {

        // 加filte之一

        // curator Provider list from zk

        List<String> invokers = new ArrayList<>();
        // 1. 简单：从zk拿到服务提供的列表
        // 2. 挑战：监听zk的临时节点，根据事件更新这个list（注意，需要做个全局map保持每个服务的提供者List）
        // start zk client

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder().connectString("localhost:2181").namespace("rpcfx").retryPolicy(retryPolicy).build();
        client.start();
        try {

            for (String item : client.getChildren().forPath("/io.kimking.rpcfx.demo.api.UserService")) {
                invokers.add("http://" + item.replace("_", ":"));
            }
        } catch (Exception e) {

        }

        List<String> urls = router.route(invokers);

        String url = loadBalance.select(urls); // router, loadbalance

        return create(serviceClass, url, filter);

    }

    public static <T> T create(final Class<T> serviceClass, final String url, Filter... filters) {

        // 0. 替换动态代理 -> AOP
        Enhancer enhancer = new Enhancer();
        //enhancer.setSuperclass(RpcAopfx.class);
        enhancer.setInterfaces(new Class[] {serviceClass});
        enhancer.setCallback(new RpcfxCglibInvocationHandler(serviceClass,url));
        return (T) enhancer.create();
//        return (T) Proxy.newProxyInstance(Rpcfx.class.getClassLoader(), new Class[]{serviceClass}, new RpcfxInvocationHandler(serviceClass, url, filters));

    }

    public static class RpcfxCglibInvocationHandler implements MethodInterceptor {


        public static final MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");

        private final Class<?> serviceClass;
        private final String url;
        private final Filter[] filters;

        public <T> RpcfxCglibInvocationHandler(Class<T> serviceClass, String url, Filter... filters) {
            this.serviceClass = serviceClass;
            this.url = url;
            this.filters = filters;
        }

        // 可以尝试，自己去写对象序列化，二进制还是文本的，，，rpcfx是xml自定义序列化、反序列化，json: code.google.com/p/rpcfx
        // int byte char float double long bool
        // [], data class

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {

            // 加filter地方之二
            // mock == true, new Student("hubao");

            RpcfxRequest request = new RpcfxRequest();
            request.setServiceClass(this.serviceClass.getName());
            request.setMethod(method.getName());
            request.setParams(objects);

            if (null!=filters) {
                for (Filter filter : filters) {
                    if (!filter.filter(request)) {
                        return null;
                    }
                }
            }

            RpcfxResponse response = post(request, url);

            // 加filter地方之三
            // Student.setTeacher("cuijing");

            // 这里判断response.status，处理异常
            // 考虑封装一个全局的RpcfxException

            return JSON.parse(response.getResult().toString());
        }

        private RpcfxResponse post(RpcfxRequest req, String url) throws IOException {


            UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(url).build(true);

            Bootstrap bootstrap = new Bootstrap();
            NioEventLoopGroup group = new NioEventLoopGroup();

            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(new HttpRequestEncoder());
                            ch.pipeline().addLast(new HttpResponseDecoder());
                            ch.pipeline().addLast(new HttpInboundHandler(JSON.toJSONString(req)));
                        }
                    });
            Channel channel = bootstrap.connect("127.0.0.1", 8081).channel();
            while (true) {
                channel.writeAndFlush(new Date() + ": helloWorld");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

//            EventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);
//
//            try {
//                Bootstrap b = new Bootstrap();
//                b.group(eventLoopGroup)
//                        .channel(NioSocketChannel.class)
//                        .handler(new LoggingHandler(LogLevel.INFO))
//                        .handler(new ChannelInitializer<Channel>() {
//                            @Override
//                            protected void initChannel(Channel channel) throws Exception {
//                                channel.pipeline().addLast(new StringEncoder());
////                                channel.pipeline().addLast(new HttpResponseEncoder());
//                                channel.pipeline().addLast(new HttpInboundHandler(JSON.toJSONString(req)));
////                                channel.pipeline().addLast(new NettyClientHandler());
//                            }
//                        });

//                ch.closeFuture().sync();
//                ch.write(request).awaitUninterruptibly();
//                ch.closeFuture().awaitUninterruptibly();
//                ch.writeAndFlush(request);
//                ch.closeFuture().sync();
//            } catch (Exception e ) {
//                e.printStackTrace();
//            } finally {
//                eventLoopGroup.shutdownGracefully(1L, 3, TimeUnit.SECONDS);
//            }


//            System.out.println("resp json: " + stringBuffer.toString());
//            return JSON.parseObject(stringBuffer.toString(), RpcfxResponse.class);
        }

    }

    public static class HttpInboundHandler extends ChannelInboundHandlerAdapter {

        private static Logger logger = LoggerFactory.getLogger(HttpInboundHandler.class);
        private final String content;

        public HttpInboundHandler(String content) {
            this.content = content;
//            handler = new OkhttpOutboundHandler(this.proxyServer);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelActive");
            ByteBuf bbuf = Unpooled.copiedBuffer(content, StandardCharsets.UTF_8);
//            System.out.println(uriComponents.getHost() + uriComponents.getPort());
//            Channel ch = b.connect("127.0.0.1", 8081).channel();
            Channel ch = ctx.channel();
            FullHttpRequest request = new DefaultFullHttpRequest(
                    HttpVersion.HTTP_1_1, HttpMethod.POST, "http://127.0.0.1:8081");
            request.headers().set(HttpHeaderNames.HOST, "127.0.0.1");
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
            request.headers().add(HttpHeaders.Names.CONTENT_TYPE, "application/json");
            request.content().clear().writeBytes(bbuf);

            ch.writeAndFlush(request);
            super.channelActive(ctx);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            try {
                //logger.info("channelRead流量接口请求开始，时间为{}", startTime);

                System.out.println("客户端接受的消息: " + msg);
            if (msg instanceof HttpResponse) {
                HttpResponse response = (HttpResponse) msg;

                System.out.println("STATUS: " + response.getStatus());
                System.out.println("VERSION: " + response.getProtocolVersion());
                System.out.println();

                if (!response.headers().isEmpty()) {
                    for (String name: response.headers().names()) {
                        for (String value: response.headers().getAll(name)) {
                            System.out.println("HEADER: " + name + " = " + value);
                        }
                    }
                    System.out.println();
                }

                if (HttpHeaders.isTransferEncodingChunked(response)) {
                    System.out.println("CHUNKED CONTENT {");
                } else {
                    System.out.println("CONTENT {");
                }
            }
            if (msg instanceof HttpContent) {
                HttpContent content = (HttpContent) msg;

                System.out.println("http content");
                stringBuffer = new StringBuffer(content.content().toString(CharsetUtil.UTF_8));
                System.out.print(content.content().toString(CharsetUtil.UTF_8));
                System.out.flush();

                if (content instanceof LastHttpContent) {
                    System.out.println("} END OF CONTENT");
                }
            }
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }

        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }


    }


    public static class NettyClientHandler extends SimpleChannelInboundHandler<String> {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("正在连接... ");
            super.channelActive(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("channelRead");
            super.channelRead(ctx, msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelReadComplete");
            super.channelReadComplete(ctx);
        }


        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("连接关闭! ");
            super.channelInactive(ctx);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            System.out.println("客户端接受的消息: " + msg);
//            if (msg instanceof HttpResponse) {
//                HttpResponse response = (HttpResponse) msg;
//
//                System.out.println("STATUS: " + response.getStatus());
//                System.out.println("VERSION: " + response.getProtocolVersion());
//                System.out.println();
//
//                if (!response.headers().isEmpty()) {
//                    for (String name: response.headers().names()) {
//                        for (String value: response.headers().getAll(name)) {
//                            System.out.println("HEADER: " + name + " = " + value);
//                        }
//                    }
//                    System.out.println();
//                }
//
//                if (HttpHeaders.isTransferEncodingChunked(response)) {
//                    System.out.println("CHUNKED CONTENT {");
//                } else {
//                    System.out.println("CONTENT {");
//                }
//            }
//            if (msg instanceof HttpContent) {
//                HttpContent content = (HttpContent) msg;
//
//                System.out.println("http content");
//                stringBuffer = new StringBuffer(content.content().toString(CharsetUtil.UTF_8));
//                System.out.print(content.content().toString(CharsetUtil.UTF_8));
//                System.out.flush();
//
//                if (content instanceof LastHttpContent) {
//                    System.out.println("} END OF CONTENT");
//                }
//            }
        }
    }

    public static class RpcfxInvocationHandler implements InvocationHandler {

        public static final MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");

        private final Class<?> serviceClass;
        private final String url;
        private final Filter[] filters;

        public <T> RpcfxInvocationHandler(Class<T> serviceClass, String url, Filter... filters) {
            this.serviceClass = serviceClass;
            this.url = url;
            this.filters = filters;
        }

        // 可以尝试，自己去写对象序列化，二进制还是文本的，，，rpcfx是xml自定义序列化、反序列化，json: code.google.com/p/rpcfx
        // int byte char float double long bool
        // [], data class

        @Override
        public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {

            // 加filter地方之二
            // mock == true, new Student("hubao");

            RpcfxRequest request = new RpcfxRequest();
            request.setServiceClass(this.serviceClass.getName());
            request.setMethod(method.getName());
            request.setParams(params);

            if (null!=filters) {
                for (Filter filter : filters) {
                    if (!filter.filter(request)) {
                        return null;
                    }
                }
            }

            RpcfxResponse response = post(request, url);

            // 加filter地方之三
            // Student.setTeacher("cuijing");

            // 这里判断response.status，处理异常
            // 考虑封装一个全局的RpcfxException

            return JSON.parse(response.getResult().toString());
        }

        private RpcfxResponse post(RpcfxRequest req, String url) throws IOException {
            String reqJson = JSON.toJSONString(req);
            System.out.println("req json: "+reqJson);

            // 1.可以复用client
            // 2.尝试使用httpclient或者netty client
            OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(JSONTYPE, reqJson))
                    .build();
            String respJson = client.newCall(request).execute().body().string();
            System.out.println("resp json: "+respJson);
            return JSON.parseObject(respJson, RpcfxResponse.class);
        }
    }
}
