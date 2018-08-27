package com.lad.service.impl;

import java.util.LinkedList;
import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

import com.lad.bo.AlbumBo;
import com.lad.bo.PictureBo;
import com.lad.bo.PictureWallBo;
import com.lad.dao.IPictureDao;
import com.lad.service.IPictureService;
import com.mongodb.WriteResult;

@Service("pictureService")
public class PictureServiceImpl implements IPictureService {
	@Autowired
	private IPictureDao pictureDao;

	@Override
	public String test() {
		return pictureDao.test();
	}

	@Override
	public AlbumBo getAlbumByName(String name, String uid) {
		return pictureDao.getAlbumByName(name, uid);
	}

	@Override
	public String insertAlbum(AlbumBo albumBo) {
		return pictureDao.insertAlbum(albumBo);
	}

	@Override
	public String insertPicture(PictureBo pictureBo) {
		return pictureDao.insertPicture(pictureBo);
	}

	@Override
	public void insertAllPic(List<PictureBo> list) {
		pictureDao.insertAllPic(list);
	}

	@Override
	public PictureWallBo getWallByUid(String id) {
		return pictureDao.getWallByUid(id);
	}

	@Override
	public WriteResult updatePicWall(List<String> pictures, String uid) {
		return pictureDao.updatePicWall(pictures, uid);
	}

	@Override
	public AggregationResults<Document> getPictureByPage(String uid, boolean self, int page, int limit) {
		return pictureDao.getPictureByPage(uid, self, page, limit);
	}

	@Override
	public String insertPicWall(PictureWallBo wallBo) {
		return pictureDao.insertPicWall(wallBo);
	}

	@Override
	public WriteResult deletePicByIds(List<String> urlList, String uid) {
		return pictureDao.deletePicByIds(urlList, uid);
	}

	@Override
	public WriteResult updateOpenLevel(PictureBo picBo) {
		return pictureDao.updateOpenLevel(picBo);
	}

	@Override
	public List<PictureBo> getTop4ByUid(String uid) {
		return pictureDao.getTop4ByUid(uid);
	}

	@Override
	public WriteResult updateWallById(String id, LinkedList<String> pictures) {
		return pictureDao.updateWallById(id, pictures);
	}

	@Override
	public List<PictureBo> getPicturesByList(List<String> asList, String id) {
		return pictureDao.getPicturesByList(asList, id);
	}

}
