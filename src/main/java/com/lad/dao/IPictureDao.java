package com.lad.dao;

import java.util.LinkedList;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import com.lad.bo.AlbumBo;
import com.lad.bo.PictureBo;
import com.lad.bo.PictureWallBo;
import com.mongodb.WriteResult;

public interface IPictureDao {

	String test();

	AlbumBo getAlbumByName(String name, String uid);

	String insertAlbum(AlbumBo albumBo);

	String insertPicture(PictureBo pictureBo);

	void insertAllPic(List<PictureBo> list);

	PictureWallBo getWallByUid(String id);

	WriteResult updatePicWall(List<String> pictures,String uid);

	AggregationResults<Document> getPictureByPage(String uid, boolean self, int page, int limit);

	String insertPicWall(PictureWallBo wallBo);

	WriteResult deletePicByIds(List<String> urlList, String uid);

	WriteResult updateOpenLevel(PictureBo picBo);

	List<PictureBo> getTop4ByUid(String uid);

	WriteResult updateWallById(String id, LinkedList<String> pictures);

	List<PictureBo> getPicturesByList(List<String> asList, String id);

	long getPicSizeByUid(String id);

}
