package com.lad.dao;

import com.lad.bo.NoteBo;
import com.mongodb.WriteResult;

import java.util.LinkedList;
import java.util.List;

public interface INoteDao extends IBaseDao {
	public NoteBo insert(NoteBo noteBo);

	public NoteBo selectById(String noteId);

	public WriteResult updatePhoto(String noteId, LinkedList<String> photos);

	/**
	 * 更新帖子访问量
	 * @param noteId
	 * @return
	 */
	WriteResult updateVisitCount(String noteId);

	/**
	 * 更新评论数量
	 * @param noteId
	 * @param commentcount
	 * @return
	 */
	WriteResult updateCommentCount(String noteId, long commentcount);

	/**
	 * 更新转发数量
	 * @param noteId
	 * @param transcount
	 * @return
	 */
	WriteResult updateTransCount(String noteId, long transcount);

	/**
	 * 更新点赞数量
	 * @param noteId
	 * @param thumpsubcount
	 * @return
	 */
	WriteResult updateThumpsubCount(String noteId, long thumpsubcount);

	/**
	 * 获取圈子内最新的帖子
	 * @param circleid
	 * @param startId
	 * @param gt
	 * @param limit
	 * @return
	 */
	List<NoteBo> finyByCreateTime(String circleid, String startId, boolean gt, int limit);

	/**
	 * 取评论最高前十
	 * @param circleid
	 * @return
	 */
	List<NoteBo> selectByComment(String circleid);

	/**
	 * 取访问量前4
	 * @param circleid
	 * @return
	 */
	List<NoteBo> selectByVisit(String circleid);

	/**
	 * 圈子内热门帖子列表
	 * @param circleid
	 * @return
	 */
	List<NoteBo> selectHotNotes(String circleid);

	/**
	 * 查找置顶帖子
	 * @param circleid
	 * @return
	 */
	List<NoteBo> selectTopNotes(String circleid);
}
