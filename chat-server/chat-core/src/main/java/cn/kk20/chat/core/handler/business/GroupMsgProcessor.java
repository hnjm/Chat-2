package cn.kk20.chat.core.handler.business;

import cn.kk20.chat.core.bean.ChatMessage;
import cn.kk20.chat.core.bean.ChatMessageType;
import io.netty.channel.ChannelHandlerContext;

/**
 * @Description: 群消息处理器
 * @Author: Roy Z
 * @Date: 2020/2/17 16:34
 * @Version: v1.0
 */
@MsgProcessor(messageType = ChatMessageType.GROUP)
public class GroupMsgProcessor implements MessageProcessor {

    @Override
    public void processMessage(ChannelHandlerContext channelHandlerContext, ChatMessage chatMessage, boolean isFromWeb) {

    }

}
