package com.lad.util;

public class Constant {
	public static final String HEAD_PICTURE_PATH = "/opt/apps/lad-server/picture/head/";
	public static final String FEEDBACK_PICTURE_PATH = "/opt/apps/lad-server/picture/feedback/";
	public static final String IMFILE_PATH = "/opt/apps/lad-server/picture/imfile/";
	public static final String CIRCLE_HEAD_PICTURE_PATH = "/opt/apps/lad-server/picture/circle/head/";
	public static final String CIRCLE_PICTURE_PATH = "/opt/apps/lad-server/picture/circle/";
	public static final String NOTE_PICTURE_PATH = "/opt/apps/lad-server/picture/note/";
	public static final String DYNAMIC_PICTURE_PATH = "/opt/apps/lad-server/picture/dynamic/";
	public static final String PARTY_PICTURE_PATH = "/opt/apps/lad-server/picture/party/";
	public static final String INFOR_PICTURE_PATH = "/opt/apps/lad-server/picture/infor/";
	public static final String CHATROOM_PICTURE_PATH = "/opt/apps/lad-server/picture/chatroom/";
	public static final String EXPOSE_PICTURE_PATH = "/opt/apps/lad-server/picture/expose/";
	public static final String RELEASE_PICTURE_PATH = "/opt/apps/lad-server/picture/release/";
	public static final String QINIU_URL = "http://res.ttlaoyou.com/";

	public static final String RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><response><error>0</error><message></message></response>";

	/**
	 * 
	 * pushd服务器appkey
	 * 
	 */
	public static final String PUSHD_APPKEY = "2a7f2e170bc06773c7460e877990befb7cb76af5b9b3286b63248918a0f3f021";

	/**
	 * pushd服务器ip
	 */
	public static final String PUSHD_IP = "117.114.228.252";

	/**
	 * pushd服务器ip
	 */
	// public static final String PUSHD_IP = "180.76.138.200";

	/**
	 * push服务器端口
	 */
	public static final int PUSHD_POST = 2222;

	/**
	 * 通用成功返回
	 */
	public static final String COM_RESP = "{\"ret\":0}";

	public static final String RESP_SUCCES = "{\"ret\":1}";

	/**
	 * 异常失败返回
	 */
	public static final String COM_FAIL_RESP = "{\"ret\":-1}";

	/**
	 * 面对面建群lock
	 */
	public static final String CHAT_LOCK = "chatLock";

	/**
	 * 访问量lock
	 */
	public static final String VISIT_LOCK = "visitLock";

	/**
	 * 点赞量lock
	 */
	public static final String THUMB_LOCK = "thumpLock";

	/**
	 * 评论量lock
	 */
	public static final String COMOMENT_LOCK = "commentLock";

	/**
	 * 缓存
	 */
	public static final String TEST_CACHE = "testCache";

	/**
	 * 转发量lock
	 */
	public static final String TRANS_LOCK = "transLock";

	/**
	 * 申请
	 */
	public static final int ADD_APPLY = 0;
	/**
	 * 申请同意
	 */
	public static final int ADD_AGREE = 1;
	/**
	 * 申请拒绝
	 */
	public static final int ADD_REFUSE = 2;

	/**
	 * 申请过期
	 */
	public static final int ADD_FAIL = -1;

	/**
	 * 激活
	 */
	public static final int ACTIVITY = 0;
	/**
	 * 删除
	 */
	public static final int DELETED = 1;

	/**
	 * 评论或点赞类型 note 帖子
	 */
	public static final int NOTE_TYPE = 0;
	/**
	 * 评论或点赞类型 infor 资讯
	 */
	public static final int INFOR_TYPE = 1;
	/**
	 * 主页评论或点赞型 homepage
	 */
	public static final int PAGE_TYPE = 2;
	/**
	 * 举报 圈子类型
	 */
	public static final int CIRCLE_TYPE = 3;
	/**
	 * 聚会评论 party 类型
	 */
	public static final int PARTY_TYPE = 4;
	/**
	 * 动态 类型
	 */
	public static final int DYNAMIC_TYPE = 5;

	/**
	 * 聊天 类型
	 */
	public static final int CHAT_TYPE = 6;

	/**
	 * 文件 类型
	 */
	public static final int FILE_TYPE = 7;
	/**
	 * 帖子评论点赞
	 */
	public static final int NOTE_COM_TYPE = 8;
	/**
	 * 资讯评论点赞
	 */
	public static final int INFOR_COM_TYPE = 9;
	/**
	 * 聚会评论
	 */
	public static final int PARTY_COM_TYPE = 10;
	/**
	 * 曝光消息
	 */
	public static final int EXPOSE_TYPE = 11;
	/**
	 * 发布消息
	 */
	public static final int RELEASE_TYPE = 12;

	/**
	 * 举报
	 */
	public static final int FEED_TIPS = 1;
	/**
	 * 反馈
	 */
	public static final int FEED_BACK = 0;
	/**
	 *
	 */
	public static final int LEVEL_HOUR = 0;
	/**
	 *
	 */
	public static final int LEVEL_PARTY = 1;
	/**
	 *
	 */
	public static final int LEVEL_NOTE = 2;
	/**
	 *
	 */
	public static final int LEVEL_COMMENT = 3;
	/**
	 *
	 */
	public static final int LEVEL_TRANS = 4;
	/**
	 *
	 */
	public static final int LEVEL_SHARE = 5;
	/**
	 *
	 */
	public static final int LEVEL_CIRCLE = 6;

	/**
	 * 单人聊天
	 */
	public static final int ROOM_SINGLE = 1;
	/**
	 * 群聊
	 */
	public static final int ROOM_MULIT = 2;
	/**
	 * 面对面聊
	 */
	public static final int ROOM_FACE_2_FACE = 3;
	/**
	 *
	 */
	public static final int ROOM_TEMP_PARTY = 4;
	/**
	 * 置顶
	 */
	public static final int NOTE_TOP = 0;
	/**
	 * 加精
	 */
	public static final int NOTE_JIAJING = 1;

	/**
	 * 圈子访问
	 */
	public static final int CIRCLE_VISIT = 0;
	/**
	 * 圈子评论
	 */
	public static final int CIRCLE_COMMENT = 3;
	/**
	 * 圈子转发
	 */
	public static final int CIRCLE_TRANS = 4;
	/**
	 * 圈子点赞
	 */
	public static final int CIRCLE_THUMP = 6;

	/**
	 * 聚会发布
	 */
	public static final int CIRCLE_PARTY = 1;
	/**
	 * 聚会访问
	 */
	public static final int CIRCLE_PARTY_VISIT = 7;
	/**
	 * 聚会分享
	 */
	public static final int CIRCLE_PARTY_SHARE = 8;
	/**
	 * 聚会点赞
	 */
	public static final int CIRCLE_PARTY_THUMP = 9;
	/**
	 * 聚会评论
	 */
	public static final int CIRCLE_PARTY_COMMENT = 12;

	/**
	 * 帖子发布
	 */
	public static final int CIRCLE_NOTE = 2;
	/**
	 * 帖子访问
	 */
	public static final int CIRCLE_NOTE_VISIT = 10;
	/**
	 * 帖子转发
	 */
	public static final int CIRCLE_NOTE_SHARE = 5;
	/**
	 * 帖子点赞
	 */
	public static final int CIRCLE_NOTE_THUMP = 11;
	/**
	 * 帖子评论
	 */
	public static final int CIRCLE_NOTE_COMMENT = 13;

	public static final int ONE = 1;

	public static final int TWO = 2;

	public static final int THREE = 3;

	public static final int FOUR = 4;

	/**
	 * 收藏 图片类型
	 */
	public static final int COLLET_PIC = 1;
	/**
	 * 收藏 音乐类型
	 */
	public static final int COLLET_MUSIC = 2;
	/**
	 * 收藏 视频类型
	 */
	public static final int COLLET_VIDEO = 3;
	/**
	 * 收藏 语音类型
	 */
	public static final int COLLET_VOICE = 4;
	/**
	 * 收藏 链接文章、圈子 、帖子、聚会类型
	 */
	public static final int COLLET_URL = 5;
	/**
	 * 健康
	 */
	public static final int INFOR_HEALTH = 1;
	/**
	 * 安防
	 */
	public static final int INFOR_SECRITY = 2;
	/**
	 * 广播
	 */
	public static final int INFOR_RADIO = 3;
	/**
	 * 视频
	 */
	public static final int INFOR_VIDEO = 4;
	/**
	 * 每日新闻
	 */
	public static final int INFOR_DAILY = 5;
	/**
	 * 养老政策
	 */
	public static final int INFOR_YANGLAO = 6;

	// 邀请数
	public static final int VISIT_NUM = 1;
	// 评论数
	public static final int COMMENT_NUM = 2;
	// 分享数
	public static final int SHARE_NUM = 3;
	// 点赞数
	public static final int THUMPSUB_NUM = 4;
	// 收藏数
	public static final int COLLECT_NUM = 5;

	/**
	 * 健康
	 */
	public static final String HEALTH_NAME = "healthTypes";
	/**
	 * 安防
	 */
	public static final String SECRITY_NAME = "securityTypes";
	/**
	 * 广播
	 */
	public static final String RADIO_NAME = "radioTypes";
	/**
	 * 视频
	 */
	public static final String VIDEO_NAME = "videoTypes";
	/**
	 * 时政
	 */
	public static final String DAILY_NAME = "dailyTypes";
	/**
	 * 视频
	 */
	public static final String YANGLAO_NAME = "yanglaoTypes";

	public static final String QUICK_LOGIN = "您已成功登录“天天老友” 。登录账号为您本次登录使用的手机号码，登录初始密码为您本次登录使用的手机号码后6位。为了您的账户安全，“天天老友”建议您及时修改登录密码";

	/**
	 * 群聊中的系统通知类型
	 */

	// 某人加入群聊
	public static final int SOME_ONE_JOIN_CHAT_ROOM = 4;

	// 某人退出群聊
	public static final int SOME_ONE_QUIT_CHAT_ROOM = 5;

	// 某人被踢出群聊
	public static final int SOME_ONE_EXPELLED_FROM_CHAT_ROOM = 6;

	// 某人修改了群名称
	public static final int SOME_ONE_MODIFY_NAME_OF_CHAT_ROOM = 7;

	// 某人被邀请加入群聊
	public static final int SOME_ONE_BE_INVITED_OT_CHAT_ROOM = 8;

	// 面对面群中某人加入群聊
	public static final int FACE_TO_FACE_SOME_ONE_JOIN_CHAT_ROOM = 9;

	// 群聊解散
	public static final int SOME_ONE_DISMISS_CHAT_ROOM = 10;

	// 群主变更
	public static final int MASTER_CHANGE_CHAT_ROOM = 11;

	// 群主修改权限通知
	public static final int MASTER_CHANGE_CHAT_VERIFY = 12;

	// 某人通过二维码加入群聊
	public static final int SOME_ONE_BY_CODE_OT_CHAT_ROOM = 13;

	public static final String WX_APP_ID = "wx9db7bfc8d75ff1d7";

	public static final String WX_APP_SECRET = "94fcf5520ce067be9aa34dac143e89e5";

	public static final int PAGE_LIMIT = 20;

	// 关注
	public static final String CARE = "关注";

	// 拉黑
	public static final String PASS = "拉黑";

	// 找儿媳/招女婿
	public static final String MARRIAGE = "找儿媳/招女婿";

	// 找老伴
	public static final String SPOUSE = "找老伴";

	// 找驴友
	public static final String TRAVELERS = "找驴友";

	// 天天老友
	public static final String APP_NAME = "天天老友";

	/* ==================养老院======================= */
	
	// 无
	public static final String NONE = "无";

	// 其他
	public static final String OTHER = "其他";
	
	// 养老院-特色服务-医保定点
	public static final String YL_TS_DD = "医保定点";

	// 养老院-特色服务-医保定点
	public static final String YL_TS_ED = "可接收异地老人";
	
	// 收住对象: 全部、 自理、 半自理/介助、 不能自理/介护、 特护
	
	// 养老院-服务级别-自理
	public static final String YL_SL_ZL = "自理";

	// 养老院-服务级别-半自理/介助
	public static final String YL_SL_BZL = "半自理/介助";

	// 养老院-服务级别-不能自理/介护
	public static final String YL_SL_NZL = "半自理/介助";

	// 养老院-服务级别-特护
	public static final String YL_SL_TH = "半自理/介助";
	
	// 机构类型:全部 、养老院、 敬老院 、福利院、 疗养院 、老年公寓 、老人院 、护理院、 养老社区、 养老照料中心 、其它
	
	// 养老院-机构类型-养老院
	public static final String YL_TP_YLY = "养老院";	

	// 养老院-机构类型-养老院
	public static final String YL_TP_JLY = "敬老院 ";	
	
	// 养老院-机构类型-养老院
	public static final String YL_TP_FLY = "福利院";	
	
	// 养老院-机构类型-养老院
	public static final String YL_TP_LYY = "疗养院 ";	
	
	// 养老院-机构类型-养老院
	public static final String YL_TP_LNGY = "老年公寓 ";	
	
	// 养老院-机构类型-养老院
	public static final String YL_TP_LRY = "老人院 ";	
	
	// 养老院-机构类型-养老院
	public static final String YL_TP_HLY = "护理院";	
	
	// 养老院-机构类型-养老院
	public static final String YL_TP_YLSQ = "养老社区";	
	
	// 养老院-机构类型-养老院
	public static final String YL_TP_ZLZX = "养老照料中心";
	
	// 机构性质:国营机构 、民营机构 、社会团体 、公办民营、 公助民办 、其它
	
	// 养老院-机构性质-养老院
	public static final String YL_PP_GY = "国营机构";
	
	// 养老院-机构性质-养老院
	public static final String YL_PP_MY = "民营机构";
	
	// 养老院-机构性质-养老院
	public static final String YL_PP_ST = "社会团体";
	
	// 养老院-机构性质-养老院
	public static final String YL_PP_GB = "公办民营";
	
	// 养老院-机构性质-养老院
	public static final String YL_PP_GZ = "公助民办";
	
	/**
	 * 评论或点赞类型 note 帖子
	 */
	public static final int HOME_TYPE = 14;
	/**
	 * 养老院转发
	 */
	public static final int HOME_SHARE = 14;
}
