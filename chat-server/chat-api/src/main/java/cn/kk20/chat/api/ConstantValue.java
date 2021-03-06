package cn.kk20.chat.api;

/**
 * @Description:
 * @Author: Roy
 * @Date: 2020/3/12 10:54
 * @Version: v1.0
 */
public class ConstantValue {
    // redis前缀
    public static final String HOST_OF_USER = "host_of_user:";// 用户连接的主机地址
    public static final String MEMBER_OF_GROUP = "member_of_group:";// 群成员
    public static final String FRIEND_OF_USER = "friend_of_user:";// 好友列表
    public static final String STATISTIC_OF_HOST = "statistic_of_host:";// 指定主机已连接数量
    public static final String LIST_OF_SERVER = "list_of_server";// 当前可服务主机列表
}
