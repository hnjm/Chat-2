package cn.kk20.chat.center.initializer;

import cn.kk20.chat.center.handler.ForwardMessageHandler;
import cn.kk20.chat.center.handler.HeartbeatMessageHandler;
import cn.kk20.chat.core.main.CommonInitializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description: 初始化通用Server
 * @Author: Roy
 * @Date: 2019-01-28 16:24
 * @Version: v1.0
 */
@Component
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Autowired
    CommonInitializer commonInitializer;
    @Autowired
    HeartbeatMessageHandler heartbeatMessageHandler;
    @Autowired
    ForwardMessageHandler forwardMessageHandler;

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        commonInitializer.initCommon(pipeline);
        pipeline.addLast(new IdleStateHandler(5, 0, 0));
        pipeline.addLast(heartbeatMessageHandler);
        pipeline.addLast(forwardMessageHandler);
    }
}
