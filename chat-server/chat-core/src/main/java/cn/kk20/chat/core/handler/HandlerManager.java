package cn.kk20.chat.core.handler;

import cn.kk20.chat.core.ClientManager;
import cn.kk20.chat.core.ClientWrapper;
import cn.kk20.chat.core.bean.ChatMessage;
import cn.kk20.chat.core.bean.ChatMessageType;
import cn.kk20.chat.core.bean.body.LoginBody;
import cn.kk20.chat.core.common.IdGenerator;
import cn.kk20.chat.core.common.LogUtil;
import cn.kk20.chat.core.handler.business.MessageProcessor;
import cn.kk20.chat.core.handler.business.MsgProcessor;
import cn.kk20.chat.dao.model.UserModel;
import cn.kk20.chat.service.MessageService;
import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 默认消息处理器
 * @Author: Roy
 * @Date: 2020/2/16 6:00 下午
 * @Version: v1.0
 */
public class HandlerManager {
    private static HandlerManager instance;
    private ConcurrentHashMap<Integer, MessageProcessor> messageProcessorMap;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    MessageService messageService;

    private HandlerManager() {
        messageProcessorMap = new ConcurrentHashMap<>(10);

        // 获取所有解析器
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(MsgProcessor.class);
        for (Object object : beansWithAnnotation.values()) {
            MsgProcessor msgProcessor = object.getClass().getAnnotation(MsgProcessor.class);
            ChatMessageType chatMessageType = msgProcessor.messageType();
            if (object instanceof MessageProcessor) {
                messageProcessorMap.put(chatMessageType.getCode(), (MessageProcessor) object);
            }
        }
    }

    public static HandlerManager getInstance() {
        if (instance == null) {
            synchronized (ClientManager.class) {
                if (instance == null) {
                    instance = new HandlerManager();
                }
            }
        }
        return instance;
    }

    public void handleMessage(ChannelHandlerContext ctx, ChatMessage chatMessage, boolean isFromWeb) {
        // 消息id统一变更为系统生成
        chatMessage.setId(IdGenerator.generateId());
        int messageType = chatMessage.getType();
        MessageProcessor processor = messageProcessorMap.get(messageType);
        processor.processMessage(ctx, chatMessage, isFromWeb);
    }



}