package com.lad.vo;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 功能描述：
 * Version: 1.0
 * Time:2017/6/25
 */
@Getter
@Setter
@ToString
public class NoteVo extends BaseVo {
	//用户名
    private String username;
        
    //性别
    private String sex;
    //头像
    private String headPictureName;
    //生日
    private String birthDay;
    //用户等级
    private int userLevel;
    //帖子id
    private String nodeid;
    //标题
    private String subject;
    //内容
    private String content;
    //访问人数
    private Long visitCount;
    //点赞人数
    private Long thumpsubCount;
    //评论人数
    private Long commontCount;
    //转发量
    private Long transCount;
    //我是否点赞
    private boolean isMyThumbsup;
    //type
    private String type;
    //我是否读
    private boolean read;

    //精华  管理员操作
    private int essence;
    //置顶  管理员操作
    private int top;

    //圈子名字
    private String cirName;

    ///圈子头像
    private String cirHeadPic;

    ///当前帖子所在圈子的帖子数量
    private int cirNoteNum;
    ///当前帖子所在圈子的人数
    private int cirUserNum;
    ///当前帖子所在圈子的阅读数量
    private int cirVisitNum;
    //视频缩略图
    private String videoPic;
    //是否收藏
    private boolean isCollect;

    private String fromUserid;

    private String fromUserName;

    private String fromUserPic;

    private String fromUserSign;

    private String fromUserSex;

    private int fromUserLevel;

    private String fromUserBirth;
   
    // 资讯二级分类
    private String className;

    private LinkedList<String> photos = new LinkedList<>();
    
    private String createDate;

    private Date createTime;                        

    private String createuid;

    private double[] position;
    private String circleId;

    private String landmark;
    //原信息id
    private String sourceid;

    private boolean isForward = false;
    //转发的来源类型，0 表示帖子，1 表示来源是资讯
    private int forwardType;

    private int inforType;

    private String inforTypeName;
    //视频或者广播的url
    private String inforUrl;

    private List<UserNoteVo> atUsers;

    private double distance;
	private double temp;
	
	private Date topUpdateTime;
	private Date essUpdateTime;
}
