package cn.kk20.chat.core.main.server.controller;

import cn.kk20.chat.base.http.ResultData;
import cn.kk20.chat.base.message.ChatMessage;
import cn.kk20.chat.core.common.ConstantValue;
import cn.kk20.chat.core.main.server.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Author: Roy Z
 * @Date: 2020/2/20 19:37
 * @Version: v1.0
 */
@RestController
@RequestMapping("server")
@ConditionalOnProperty(name = "chat.registerAsServer", havingValue = "true")
public class ChatServerController {
    private static final Logger logger = LoggerFactory.getLogger(ChatServerController.class);

    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    @Autowired
    MessageSender messageSender;

    @PostMapping("forwardChatMessage")
    public ResultData forwardChatMessage(@RequestBody ChatMessage chatMessage) {
        Long toUserId = chatMessage.getToUserId();
        String host = (String) redisTemplate.opsForValue().get(ConstantValue.HOST_OF_USER + toUserId);
        if (StringUtils.isEmpty(host)) {
            String msg = String.format("接收者：%s，已掉线", toUserId);
            return ResultData.fail(ResultData.ResultCode.CUSTOM_ERROR.getCode(), msg);
        }

        messageSender.sendMessage(host, chatMessage);
        return ResultData.success("发送成功");
    }

}
