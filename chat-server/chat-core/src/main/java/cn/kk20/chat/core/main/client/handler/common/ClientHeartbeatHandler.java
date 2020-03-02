package cn.kk20.chat.core.main.client.handler.common;

import cn.kk20.chat.base.message.ChatMessage;
import cn.kk20.chat.base.message.ChatMessageType;
import cn.kk20.chat.core.main.ClientComponent;
import com.alibaba.fastjson.JSON;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.net.SocketAddress;

/**
 * @Description: 读心跳处理器
 * @Author: Roy
 * @Date: 2019/1/21 15:31
 * @Version: v1.0
 */
@ClientComponent
@ChannelHandler.Sharable
public class ClientHeartbeatHandler extends SimpleChannelInboundHandler<Object> {
    private Logger logger = LoggerFactory.getLogger(ClientHeartbeatHandler.class);

    private ApplicationContext context;
    private final int heartFailMax = 5;
    private volatile int heartFailCount = 0;

    public ClientHeartbeatHandler(ApplicationContext context) {
        super();
        this.context = context;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object obj) throws Exception {
        // 收到任何消息，均把心跳失败数置零
        heartFailCount = 0;

        if (obj instanceof ChatMessage || obj instanceof String) {
            ChatMessage chatMessage;
            if (obj instanceof ChatMessage) {
                chatMessage = (ChatMessage) obj;
            } else {
                try {
                    chatMessage = JSON.parseObject((String) obj, ChatMessage.class);
                } catch (Exception e) {
                    logger.error("数据转换出错");
                    return;
                }
            }

            if (chatMessage.getMessageType() == ChatMessageType.HEARTBEAT) {
                ChatMessage heartbeatReplyMessage = new ChatMessage();
                heartbeatReplyMessage.setMessageType(ChatMessageType.HEARTBEAT);
            } else {// 向下层传递
                ctx.fireChannelRead(chatMessage);
            }
        } else {
            ctx.fireChannelRead(obj);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        boolean hasDeal = false;
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                heartFailCount++;
                logger.debug("心跳消息读失败：{}次", heartFailCount);
                hasDeal = true;
                if (heartFailCount > heartFailMax) {
                    heartbeatFail(ctx);
                    ctx.close();
                }
            }
        }

        if (!hasDeal) {
            super.userEventTriggered(ctx, evt);
        }
    }

    private void heartbeatFail(ChannelHandlerContext ctx) {
        logger.debug("客户端读超时，关闭通道");
        Channel channel = ctx.channel();
        String name = ctx.name();
        ChannelPipeline pipeline = ctx.pipeline();
        SocketAddress localAddress = channel.localAddress();
        SocketAddress remoteAddress = channel.remoteAddress();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        logger.debug("channelRegistered");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        logger.debug("channelUnregistered");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        logger.debug("channelActive");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.debug("channelInactive");
    }
}
