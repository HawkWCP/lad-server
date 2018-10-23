package com.lad.dao.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.lad.bo.CommentBo;
import com.lad.dao.ICommentDao;
import com.lad.util.Constant;
import com.mongodb.BasicDBObject;
import com.mongodb.WriteResult;

/**
 * 功能描述：
 * Time:2017/6/25
 */
@Repository("commentDao")
public class CommentDaoImpl implements ICommentDao {


    @Autowired
    private MongoTemplate mongoTemplate;

    public CommentBo insert(CommentBo commentBo){
        mongoTemplate.insert(commentBo);
        return commentBo;
    }

    public CommentBo findById(String commentId){
        Query query = new Query();
        query.addCriteria(new Criteria("_id").is(commentId));
        query.addCriteria(new Criteria("deleted").is(0));
        return mongoTemplate.findOne(query, CommentBo.class);
    }

    public List<CommentBo> selectByNoteid(String noteid, int page, int limit){
        Query query = new Query();
        query.addCriteria(new Criteria("noteid").is(noteid));
        query.addCriteria(new Criteria("deleted").is(0));
        query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "_id")));
        page = page < 1 ? 1 : page;
        query.skip((page -1) * limit);
        query.limit(limit);
        return mongoTemplate.find(query, CommentBo.class);
    }

    public List<CommentBo> selectByParentid(String parentId){
        Query query = new Query();
        query.addCriteria(new Criteria("parentid").is(parentId));
        query.addCriteria(new Criteria("deleted").is(0));
        return mongoTemplate.find(query, CommentBo.class);
    }

    public WriteResult delete(String commentId){
        Query query = new Query();
        query.addCriteria(new Criteria("_id").is(commentId));
        Update update = new Update().set("deleted", 1);
        return mongoTemplate.updateFirst(query, update, CommentBo.class);
    }

    public WriteResult deleteByNote(String noteid){
        Query query = new Query();
        query.addCriteria(new Criteria("noteid").is(noteid));
        Update update = new Update().set("deleted", Constant.DELETED);
        return mongoTemplate.updateMulti(query, update, CommentBo.class);
    }

    public List<CommentBo> selectByUser(String userid,  int page, int limit){
        Query query = new Query();
        query.addCriteria(new Criteria("createuid").is(userid));
        query.addCriteria(new Criteria("deleted").is(0));
        query.addCriteria(new Criteria("type").is(Constant.NOTE_TYPE));
        query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "_id")));
        page = page < 1 ? 1 : page;
        query.skip((page -1) * limit);
        query.limit(limit);
        return mongoTemplate.find(query, CommentBo.class);
    }

    public List<BasicDBObject> selectMyNoteReply(String userid, int page, int limit){
        Criteria criteria = new Criteria("createuid").is(userid);
        criteria.and("deleted").is(Constant.ACTIVITY).and("type").is(Constant.NOTE_TYPE);
        criteria.and("ownerid").ne(userid);

        page = page < 1 ? 1 : page;
        AggregationOperation match = Aggregation.match(criteria);

        ProjectionOperation project = Aggregation.project("noteid");

        GroupOperation group = Aggregation.group("noteid").first("noteid").as("noteid");
        Aggregation aggregation = Aggregation.newAggregation(match, project,  group,
                Aggregation.sort(new Sort(new Sort.Order(Sort.Direction.DESC, "noteid"))),
                Aggregation.skip((page - 1)*limit),Aggregation.limit(limit));
        AggregationResults<BasicDBObject> results = mongoTemplate.aggregate(aggregation, "comment",
                BasicDBObject.class);
        return results.getMappedResults();
    }

    @Override
    public List<CommentBo> selectCommentByType(int type, String id, int page, int limit) {

        Query query = new Query();
        query.addCriteria(new Criteria("deleted").is(0).and("type").is(type));
        if (type == Constant.NOTE_TYPE) {
            query.addCriteria(new Criteria("noteid").is(id));
        } else {
            query.addCriteria(new Criteria("targetid").is(id));
        }
        query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "_id")));
        page = page < 1 ? 1 : page;
        query.skip((page -1) * limit);
        query.limit(limit);
        return mongoTemplate.find(query, CommentBo.class);
    }

    @Override
    public long selectCommentByTypeCount(int type, String id) {
        Query query = new Query();
        if (type == Constant.NOTE_TYPE) {
            query.addCriteria(new Criteria("noteid").is(id));
        } else if (type == Constant.INFOR_TYPE) {
            query.addCriteria(new Criteria("targetid").is(id));
        }
        query.addCriteria(new Criteria("deleted").is(0));
        query.addCriteria(new Criteria("type").is(type));
        return mongoTemplate.count(query, CommentBo.class);
    }

    public List<CommentBo> selectByTargetUser(String targetid, String userid, int type) {
        Query query = new Query();
        if (type == Constant.NOTE_TYPE) {
            query.addCriteria(new Criteria("noteid").is(targetid));
        } else {
            query.addCriteria(new Criteria("targetid").is(targetid));
        }
        query.addCriteria(new Criteria("deleted").is(0));
        if (StringUtils.isNotEmpty(userid)) {
            query.addCriteria(new Criteria("createuid").is(userid));
        }
        query.addCriteria(new Criteria("type").is(type));
        query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "createTime")));
        return mongoTemplate.find(query, CommentBo.class);
    }

    @Override
    public WriteResult updateThumpsubNum(String commentId, int num) {
        Query query = new Query();
        query.addCriteria(new Criteria("_id").is(commentId));
        Update update = new Update();
        update.inc("thumpsubNum", num);
        return mongoTemplate.updateFirst(query, update, CommentBo.class);
    }
}
