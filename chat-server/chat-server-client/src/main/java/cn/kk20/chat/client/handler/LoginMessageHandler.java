package cn.kk20.chat.client.handler;

import cn.kk20.chat.base.message.LoginMessage;
import cn.kk20.chat.base.message.NotifyMessage;
import cn.kk20.chat.base.message.notify.NotifyMessageType;
import cn.kk20.chat.core.common.ConstantValue;
import cn.kk20.chat.client.ChannelManager;
import cn.kk20.chat.client.MessageSender;
import cn.kk20.chat.core.util.RedisUtil;
import cn.kk20.chat.dao.mapper.LoginLogModelMapper;
import cn.kk20.chat.dao.model.LoginLogModel;
import cn.kk20.chat.dao.model.UserModel;
import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Description: 登录登出处理器
 * @Author: Roy Z
 * @Date: 2020/2/17 16:34
 * @Version: v1.0
 */
@MsgHandler
@ChannelHandler.Sharable
public class LoginMessageHandler extends SimpleChannelInboundHandler<LoginMessage> {
    private static final Logger logger = LoggerFactory.getLogger(LoginMessageHandler.class);

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    ChannelManager channelManager;
    @Autowired
    MessageSender messageSender;
    @Autowired
    LoginLogModelMapper loginLogModelMapper;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, LoginMessage loginMessage)
            throws Exception {
        Channel channel = channelHandlerContext.channel();
        Long userId = loginMessage.getUserId();

        UserModel userModel = new UserModel();
        userModel.setId(userId);
        userModel.setName(loginMessage.getUserName());
        boolean login = loginMessage.isLogin();
        if (login) {
            channelManager.add(userModel, loginMessage.getClientType(), channel);
            // TODO 保存登录日志
            LoginLogModel loginLogModel = new LoginLogModel();
            loginLogModel.withUserId(userId)
                    .withIp(channel.remoteAddress().toString())
                    .withDevice("暂无")
                    .withLocation("暂无");
            loginLogModelMapper.insertSelective(loginLogModel);
        } else {
            channelManager.remove(channel);
        }

        // 查询好友列表，走数据库或redis
        Set<Long> friendIdSet = redisUtil.getLongSetValue(ConstantValue.FRIEND_OF_USER + userId);
        if (friendIdSet.isEmpty()) {
            return;
        }
        logger.info("好友列表：{}", JSON.toJSONString(friendIdSet));
        // 查询在线好友
        Set<Long> onlineFriendIdSet = new HashSet<>();
        for (Long friendId : friendIdSet) {
            String host = redisUtil.getStringValue(ConstantValue.HOST_OF_USER + friendId);
            if (!StringUtils.isEmpty(host)) {
                onlineFriendIdSet.add(friendId);
            }
        }
        logger.info("在线好友列表：{}", JSON.toJSONString(onlineFriendIdSet));
        // 回复当前登录用户，好友在线名单
        if (login) {
            NotifyMessage notifyMessage = new NotifyMessage();
            notifyMessage.setNotifyMessageType(NotifyMessageType.LOGIN_REPLY);
            notifyMessage.setData(onlineFriendIdSet);
            messageSender.sendMessage(channel, notifyMessage);
        }
        // 通知好友，用户登录或登出了，这里仅通知在线好友，因为不在线的好友没必要通知
        Map<String, Object> map = new HashMap<>();
        map.put("id", userId);
        map.put("login", login);
        NotifyMessage notifyMessage = new NotifyMessage();
        notifyMessage.setNotifyMessageType(NotifyMessageType.LOGIN_NOTIFY);
        notifyMessage.setData(map);
        for (Long friendId : onlineFriendIdSet) {
            messageSender.send2Receiver(friendId, notifyMessage);
        }
    }
}