package com.lad.dao;

import com.lad.bo.CommentBo;
import com.mongodb.WriteResult;

import java.util.List;

/**
 * 功能描述：
 * Time:2017/6/25
 */
public interface ICommentDao {

    /**
     * 添加评论
     * @param commentBo
     * @return
     */
    CommentBo insert(CommentBo commentBo);

    /**
     * 查询帖子下的所有评论
     * @param noteid
     * @return
     */
    List<CommentBo> selectByNoteid(String noteid);

    /**
     * 查询当前回复的评论
     * @param parentId
     * @return
     */
    List<CommentBo> selectByParentid(String parentId);

    /**
     * 查到评论
     * @param commentId
     * @return
     */
    CommentBo findById(String commentId);

    /**
     * 删除评论
     * @param commentId
     * @return
     */
    WriteResult delete(String commentId);

    /**
     * 查到用户所有的帖子
     * @param userid
     * @return
     */
    List<CommentBo> selectByUser(String userid);
}