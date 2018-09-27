package com.lad.dao.impl;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.lad.bo.AlbumBo;
import com.lad.bo.PictureBo;
import com.lad.bo.PictureWallBo;
import com.lad.dao.IPictureDao;
import com.lad.util.Constant;
import com.mongodb.WriteResult;

@Repository("pictureDao")
public class PictureDaoImpl implements IPictureDao {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public AlbumBo getAlbumByName(String name, String uid) {
		return mongoTemplate.findOne(
				new Query(
						Criteria.where("name").is(name).and("createuid").is(uid).and("deleted").is(Constant.ACTIVITY)),
				AlbumBo.class);
	}

	@Override
	public String insertAlbum(AlbumBo albumBo) {
		mongoTemplate.insert(albumBo);
		return albumBo.getId();
	}

	@Override
	public String test() {

		return mongoTemplate.toString();
	}

	@Override
	public String insertPicture(PictureBo pictureBo) {
		mongoTemplate.insert(pictureBo);
		return pictureBo.getId();
	}

	@Override
	public void insertAllPic(List<PictureBo> list) {
		mongoTemplate.insertAll(list);
	}

	@Override
	public PictureWallBo getWallByUid(String id) {
		return mongoTemplate.findOne(new Query(Criteria.where("createuid").is(id).and("deleted").is(Constant.ACTIVITY)),
				PictureWallBo.class);
	}

	@Override
	public WriteResult updatePicWall(List<String> pictures, String uid) {
		Query query = new Query(Criteria.where("createuid").is(uid).and("deleted").is(Constant.ACTIVITY));
		Update update = new Update();
		update.set("pictures", pictures);
		update.set("updateTime", new Date());
		update.set("updateuid", uid);

		return mongoTemplate.updateFirst(query, update, PictureWallBo.class);
	}

	@Override
	public AggregationResults<Document> getPictureByPage(String uid, boolean self, int page, int limit) {
		// db.album.aggregate([{$lookup:{from:"picture",localField:"_id",foreignField:"ablId",as:"picture"}},{$unwind:"$picture"},{$match:{createuid:"5ac9ade931f0a5752cbf443b"}},{$skip:1},{$limit:3},{$project:{url:"$picture._id",ablid:"$_id",albName:"$name",_id:0}}])
		AggregationOperation lookup = Aggregation.lookup("picture", "_id", "ablId", "picture");
		AggregationOperation unwind = Aggregation.unwind("picture");
		Criteria criteria = Criteria.where("createuid").is(uid).and("picture.deleted").is(Constant.ACTIVITY);
		if (!self) {
			criteria.and("picture.openLevel").is(4);
		}
		AggregationOperation match = Aggregation.match(criteria);
		AggregationOperation skip = Aggregation.skip(Long.valueOf(page - 1 < 0 ? 0 : (page - 1) * limit));
		AggregationOperation limi = Aggregation.limit(Long.valueOf(limit));
		AggregationOperation sort = Aggregation.sort(Direction.DESC, "picture.createTime");
		AggregationOperation project = Aggregation.project("picture.url", "picture.ablId", "name", "createuid",
				"picture.openLevel", "picture._id");
		Aggregation aggregation = Aggregation.newAggregation(lookup, unwind, match, sort, skip, limi, project);
		return mongoTemplate.aggregate(aggregation, "album", Document.class);
	}

	@Override
	public String insertPicWall(PictureWallBo wallBo) {
		mongoTemplate.insert(wallBo);
		return wallBo.getId();
	}

	@Override
	public WriteResult deletePicByIds(List<String> urlList, String uid) {
		Query query = new Query(
				Criteria.where("url").in(urlList).and("createuid").is(uid).and("deleted").is(Constant.ACTIVITY));
		Update update = new Update();
		update.set("deleted", 1);
		update.set("updateTime", new Date());
		update.set("updateuid", uid);
		return mongoTemplate.updateMulti(query, update, PictureBo.class);
	}

	@Override
	public WriteResult updateOpenLevel(PictureBo picBo) {
		Query query = new Query(Criteria.where("createuid").is(picBo.getCreateuid()).and("url").is(picBo.getUrl())
				.and("deleted").is(Constant.ACTIVITY));

		Update update = new Update();
		update.set("openLevel", picBo.getOpenLevel());
		update.set("updateTime", new Date());
		update.set("updateuid", picBo.getCreateuid());
		return mongoTemplate.updateFirst(query, update, PictureBo.class);
	}

	@Override
	public List<PictureBo> getTop4ByUid(String uid) {
		Query query = new Query(Criteria.where("createuid").is(uid).and("deleted").is(Constant.ACTIVITY).and("openLevel").is(4));
		query.with(new Sort(new Order(Direction.DESC, "createTime")));
		query.limit(4);
		return mongoTemplate.find(query, PictureBo.class);
	}

	@Override
	public WriteResult updateWallById(String id, LinkedList<String> pictures) {
		Query query = new Query(Criteria.where("_id").is(id).and("deleted").is(Constant.ACTIVITY));
		Update update = new Update();
		if (pictures.size() > 0) {
			update.set("pictures", pictures);
		} else {
			update.set("deleted", 1);
			update.set("pictures", pictures);
		}

		return mongoTemplate.updateFirst(query, update, PictureWallBo.class);
	}

	@Override
	public List<PictureBo> getPicturesByList(List<String> asList, String id) {
		return mongoTemplate.find(
				new Query(Criteria.where("createuid").is(id).and("url").in(asList)),
				PictureBo.class);
	}

	@Override
	public long getPicSizeByUid(String id) {
		return mongoTemplate.count(new Query(Criteria.where("createuid").is(id).and("deleted").is(0)), PictureBo.class);
	}

}
