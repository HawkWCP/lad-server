package com.lad.constants;

/**
 * 对应app五大模块中的个人中心;
 *模块编号-2 
 *  子模块包括:
 *      1. 个人主页
 *      2. 个人动态
 *      3. 账号安全
 *      4. 收藏
 */
public class UserCenterConstants {
    // 表示类型为个人主页
    public static final int HOMEPAGE_TYPE=21;
    // 表示类型为个人动态
    public static final int DYNAMIC_TYPE=22;
    // 表示类型为账户安全
    public static final int COUNTSECURITY_TYPE=23;
    // 表示类型为个人收藏
    public static final int COLLECTION_TYPE=24;


    /*=================个人动态====================*/
    /**
          * 转发来源:对应字段DynamicBo 中的 type
     *      11. 转发来源为他人动态;
     *
     *      21. 转发来源为圈子:圈子;
     *      22 .转发来源为圈子:帖子;
     *      23. 转发来源为圈子:聚会
     *
     *      31. 转发来源为发现:养老院
     *      32. 转发来源为发现:招接演出
     *      33. 转发来源为发现:招儿媳/女婿
     *      34. 转发来源为发现:找老伴
     *      35. 转发来源为发现:找老友
     *      36. 转发来源为发现:找旅友
     *      37. 转发来源为发现:曝光台
     *
     *      41. 转发来源为资讯:健康养生
     *      42: 转发来源为资讯:安全防范
     *      43: 转发来源为资讯:时事新闻
     *      44: 转发来源为资讯:精彩视频
     *      45: 转发来源为资讯:天天广播
     *      46: 转发来源为资讯:养老政策
     */

    public static final int FORWARD_FROM_USERCENTER_DYNAMIC=11;

    public static final int FORWARD_FROM_CIRCLE_CIRCLE=21;
    public static final int FORWARD_FROM_CIRCLE_NOTE=22;
    public static final int FORWARD_FROM_CIRCLE_PATY=23;

    public static final int FORWARD_FROM_DISCOVERY_RESTHOME=31;
    public static final int FORWARD_FROM_DISCOVERY_SHOW=32;
    public static final int FORWARD_FROM_DISCOVERY_MARRIAGE=33;
    public static final int FORWARD_FROM_DISCOVERY_SPOUCE=34;
    public static final int FORWARD_FROM_DISCOVERY_FRIEND=35;
    public static final int FORWARD_FROM_DISCOVERY_TRAVELER=36;
    public static final int FORWARD_FROM_DISCOVERY_EXPOSE=37;

    public static final int FORWARD_FROM_INFOR_HEALTH=41;
    public static final int FORWARD_FROM_INFOR_SECIRITY=42;
    public static final int FORWARD_FROM_INFOR_DAILY=43;
    public static final int FORWARD_FROM_INFOR_VIDEO=44;
    public static final int FORWARD_FROM_INFOR_BROAD=45;
    public static final int FORWARD_FROM_INFOR_YANGLAO=46;

    /**
     * 单条个人动态访问权限
     */
    // 所有人可访问
    public static final int ACCESS_SECURITY_ALLOW_ALL =0;
    // 好友可访问
    public static final int ACCESS_SECURITY_ALLOW_FRIEND =1;
    // 部分人访问
    public static final int ACCESS_SECURITY_ALLOW_PART =2;
    // 私密
    public static final int ACCESS_SECURITY_ALLOW_NONE =3;
}
