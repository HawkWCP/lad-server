package com.lad.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.lad.bo.AlbumBo;
import com.lad.bo.PictureBo;
import com.lad.bo.PictureWallBo;
import com.lad.bo.UserBo;
import com.lad.service.IPictureService;
import com.lad.util.CommonUtil;
import com.lad.util.ERRORCODE;
import com.lad.vo.PictureVo;
import com.mongodb.WriteResult;

import net.sf.json.JSONObject;

@RestController
@RequestMapping("picture")
public class PictureController extends BaseContorller {
	private static final String QualiName = "com.lad.controller.PictureController";
	private static final Logger logger = LoggerFactory.getLogger(PictureController.class);
	
	@Autowired
	private IPictureService pictureService;

	// 删除照片墙上的数据
	@PostMapping("wall-picture")
	public String setWallAndPicture(String urls, HttpServletRequest request, HttpServletResponse response) {
		// 调用照片墙接口,如果urls包含在照片墙接口内,则跳过,不包含则上传这几张照片并且设置为照片墙
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			logger.info("com.lad.controller.PictureController.setWallAndPicture-----{user:未登录用户"+",urls:"+urls+"}");
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("com.lad.controller.PictureController.setWallAndPicture-----{user:"+userBo.getUserName()+",userId:"+userBo.getId()+",urls:"+urls+"}");

		addPicWall(urls, request, response);
		LinkedList<String> pictures = pictureService.getWallByUid(userBo.getId()).getPictures();
		String[] split = urls.split(",");
		List<PictureBo> bos = new ArrayList<>();
		urls = "";
		for (String url : split) {
			if (!pictures.contains(url)) {
				PictureBo bo = new PictureBo();
				bo.setUrl(url);
				bo.setOpenLevel(4);
				bos.add(bo);
				urls+=url+",";
			}
		}
		storagePicInAlbum(JSON.toJSONString(bos), request, response);
		addPicWall(urls, request, response);
		Map<String,Object> map = new HashMap<>();
		map.put("ret", 0);
		return JSON.toJSONString(map);
	}

	// 修改照片权限-多张修改
	@PostMapping("/opens-set")
	public String setOpens(@RequestParam String pics, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			logger.info("com.lad.controller.PictureController.setOpens-----{user:未登录用户,pics:"+pics+"}");
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("com.lad.controller.PictureController.setOpens-----{user:"+userBo.getUserName()+",userId:"+userBo.getId()+",pics:"+pics+"}");
		List<PictureBo> picLst = JSON.parseArray(pics, PictureBo.class);
		String urls = "";
		List<PictureBo> list = new ArrayList<>();
		for (PictureBo pic : picLst) {
			list.add(pic);
			pic.setCreateuid(userBo.getId());
			pictureService.updateOpenLevel(pic);
			if (pic.getOpenLevel() == 0) {
				urls = urls + pic.getUrl() + ",";
			}
		}
		deleteWall(urls, request, response);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("result", list);
		return JSON.toJSONString(map);
	}

	// 删除照片墙上的数据
	@PostMapping("wall-delete")
	public String deleteWall(String urls, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			logger.info("com.lad.controller.PictureController.setOpens-----{user:未登录用户,urls:"+urls+"}");
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("com.lad.controller.PictureController.deleteWall-----{user:"+userBo.getUserName()+",userId:"+userBo.getId()+",urls:"+urls+"}");
		if (StringUtils.isEmpty(urls)) {
			return "参数错误";
		}
		String[] urlsArr = urls.split(",");
		PictureWallBo wallBo = pictureService.getWallByUid(userBo.getId());
		boolean removeAll = true;
		Map<String, Object> result = new HashMap<>();
		if (wallBo != null) {
			LinkedList<String> pictures = wallBo.getPictures();
			for (String pic : urlsArr) {
				removeAll = pictures.remove(pic) && removeAll;
				result.put(pic, removeAll);
			}

			pictureService.updateWallById(wallBo.getId(), pictures);
		}
		Map<String, Object> map = new HashMap<>();
		if (removeAll) {
			map.put("ret", 0);
			map.put("result", result);
		} else {
			map.put("ret", -1);
			map.put("message", "删除失败");
		}
		return JSON.toJSONString(map);
	}

	@PostMapping("/wall-search")
	public String getPicWall(@RequestParam(name="uid",required=false)String uid,HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			logger.info("com.lad.controller.PictureController.getPicWall-----{user:未登录用户,uid:"+uid+"}");
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("com.lad.controller.PictureController.getPicWall-----{user:"+userBo.getUserName()+",userId:"+userBo.getId()+",uid:"+uid+"}");
		if(StringUtils.isEmpty(uid)){
			uid = userBo.getId();
		}
		PictureWallBo wallBo = pictureService.getWallByUid(uid);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("result", wallBo == null ? new LinkedList<>() : wallBo.getPictures());
		return JSON.toJSONString(map);
	}

	@GetMapping("/top4-search")
	public String getTop4(HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			logger.info("com.lad.controller.PictureController.getTop4-----{user:未登录用户}");
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("com.lad.controller.PictureController.getTop4-----{user:"+userBo.getUserName()+",userId:"+userBo.getId()+"}");
		
		PictureWallBo wallBo = pictureService.getWallByUid(userBo.getId());

		LinkedList<String> result = new LinkedList<>();
		String source = "wall";
		if (wallBo != null && wallBo.getPictures().size() > 0) {
			result = wallBo.getPictures();
		} else {
			List<PictureBo> top4 = pictureService.getTop4ByUid(userBo.getId());
			for (PictureBo pictureBo : top4) {
				result.addLast(pictureBo.getUrl());
			}
			source = "album";
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("source", source);
		map.put("result", result);
		return JSON.toJSONString(map);
	}

	// 修改照片权限
	@PostMapping("/open-set")
	public String setOpen(@RequestParam String pic, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			logger.info(QualiName+".setOpen-----{user:未登录用户,pic:"+pic+"}");
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info(QualiName+".setOpen-----{user:"+userBo.getUserName()+",userId:"+userBo.getId()+",pic:"+pic+"}");

		JSONObject jsonObj = JSONObject.fromObject(pic);
		if (StringUtils.isEmpty(jsonObj.getString("url")) || StringUtils.isEmpty(jsonObj.getString("createuid"))
				|| StringUtils.isEmpty(jsonObj.getString("openLevel"))) {
			return "参数格式错误";
		}
		PictureBo picBo = (PictureBo) JSONObject.toBean(jsonObj, PictureBo.class);

		if (!userBo.getId().equals(picBo.getCreateuid())) {
			return "权限错误";
		}
		pictureService.updateOpenLevel(picBo);
		if (picBo.getOpenLevel() == 0) {
			deleteWall(picBo.getUrl(), request, response);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		return JSON.toJSONString(map);
	}

	// 照片删除
	@PostMapping("picture-delete")
	public String deletePic(String urls, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			logger.info(QualiName+".deletePic-----{user:未登录用户,urls:"+urls+"}");
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info(QualiName+".deletePic-----{user:"+userBo.getUserName()+",userId:"+userBo.getId()+",urls:"+urls+"}");
		if (StringUtils.isEmpty(urls)) {
			return "请传入正确格式的地址";
		}
		List<String> urlList = Arrays.asList(urls.split(","));
		pictureService.deletePicByIds(urlList, userBo.getId());

		// 如果存在于照片墙则删除
		PictureWallBo wallBo = pictureService.getWallByUid(userBo.getId());
		if (wallBo != null) {
			LinkedList<String> pictures = wallBo.getPictures();
			for (String pic : urlList) {
				if (pictures.contains(pic)) {
					pictures.remove(pic);
				}
			}

			pictureService.updateWallById(wallBo.getId(), pictures);
		}

		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		return JSON.toJSONString(map);
	}

	// 分页获取照片,uid可以为null,表示当前用户用户自己
	@PostMapping("picture-search")
	public String getPictureByPage(@RequestParam(required = false, value = "uid") String uid, int page, int limit,
			HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			logger.info(QualiName+".getPictureByPage-----{user:未登录用户,uid:"+uid+",page:"+page+",limit:"+limit+"}");
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info(QualiName+".getPictureByPage-----{user:"+userBo.getUserName()+",userId:"+userBo.getId()+",uid:"+uid+",page:"+page+",limit:"+limit+"}");
		if (StringUtils.isEmpty(uid)) {
			uid = userBo.getId();
		}
		AggregationResults<Document> pics = pictureService.getPictureByPage(uid, userBo.getId() == uid, page, limit);
		PictureWallBo wallBo = pictureService.getWallByUid(uid);
		if (wallBo == null) {
			wallBo = new PictureWallBo();
		}
		LinkedList<String> pictures = wallBo.getPictures() == null ? new LinkedList<String>() : wallBo.getPictures();
		List<PictureVo> list = new ArrayList<>();
		for (Document document : pics) {
			PictureVo pictureVo = new PictureVo();
			pictureVo.setUrl(document.getString("url"));
			pictureVo.setAlbName(document.getString("name"));
			pictureVo.setAlbId(document.getString("ablId"));
			pictureVo.setInWall(pictures.contains(document.getString("url")));
			pictureVo.setCreateuid(document.getString("createuid"));
			pictureVo.setOpenLevel(document.getInteger("openLevel"));
			pictureVo.setId(document.getString("_id"));
			list.add(pictureVo);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		map.put("result", list);
		return JSON.toJSONString(map);
	}

	// 添加照片墙,传入url字符串,用','号分开
	@PostMapping("wall-update")
	public String addPicWall(String urls, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		
		if (userBo == null) {
			logger.info(QualiName+".addPicWall------{user:未登录用户,urls:"+urls+"}");
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info(QualiName+".addPicWall------{user:"+userBo.getUserName()+",userId:"+userBo.getId()+",urls:"+urls+"}");
		String[] split = urls.split(",");
		if (split.length < 1) {
			return "请传入正确的图片地址";
		}
		// 获取当前账号的照片墙对象
		PictureWallBo wallBo = pictureService.getWallByUid(userBo.getId());
		if (wallBo == null) {
			wallBo = new PictureWallBo();
			wallBo.setCreateuid(userBo.getId());
			wallBo.setPictures(new LinkedList<String>());
			pictureService.insertPicWall(wallBo);
		}
		LinkedList<String> pictures = wallBo.getPictures();
		// 根据上传的url获取相册照片
		List<PictureBo> picLst = pictureService.getPicturesByList(Arrays.asList(split), userBo.getId());
		for (PictureBo pic : picLst) {
			// 如果该照片不包含在照片墙,则添加到照片墙
			
			if(pic.getOpenLevel()!=4){
				pic.setOpenLevel(4);
				setOpen(JSON.toJSONString(pic),request,response);
			}
			
			if (!pictures.contains(pic.getUrl())) {
				pictures.addFirst(pic.getUrl());
			}
		}

		List<String> subList = pictures.size() > 4 ? pictures.subList(0, 4) : pictures;

		WriteResult result = pictureService.updatePicWall(subList, userBo.getId());
		Map<String, Object> map = new HashMap<>();
		if (result != null) {
			map.put("ret", 0);
			map.put("result", subList);
		} else {
			map.put("ret", -1);
			map.put("message", "修改失败");
		}
		return JSON.toJSONString(map);
	}

	// 保存照片,传入json字段为:[{url:String,openLevel:int},{url:String,openLevel:int}]
	// 0 不对外公开;1 部分公开,但屏蔽部分用户;2 部分公开,但仅允许部分用户观看;3 仅好友可观看;4 所有人可看
	@PostMapping("picture-save")
	public String storagePicInAlbum(@RequestParam String pics, HttpServletRequest request,
			HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			logger.info("com.lad.controller.PictureController.storagePicInAlbum-----{user:未登录用户,pics:"+pics+"}");
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("com.lad.controller.PictureController.storagePicInAlbum-----{user:"+userBo.getUserName()+",userId:"+userBo.getId()+",pics:"+pics+"}");
		List<PictureBo> bos = JSON.parseArray(pics, PictureBo.class);
		AlbumBo album = createAlbum(null, userBo.getId());
		List<PictureBo> temp = new ArrayList<>();
		for (PictureBo pic : bos) {
			String url = pic.getUrl();
			if (StringUtils.isEmpty(url)) {
				continue;
			}
			pic.setId(UUID.randomUUID().toString().replace("-", ""));
			pic.setPicName(pic.getPicName() == null ? url : pic.getPicName());
			pic.setDescription(pic.getDescription() == null ? url : pic.getDescription());
			pic.setAblId(album.getId());
			pic.setCreateuid(userBo.getId());
			temp.add(pic);
		}
		pictureService.insertAllPic(temp);
		Map<String, Object> map = new HashMap<>();
		map.put("ret", 0);
		return JSON.toJSONString(map);
	}

	@PostMapping("/album-create")
	public String createAlbum(@RequestParam String json, HttpServletRequest request, HttpServletResponse response) {
		UserBo userBo = getUserLogin(request);
		if (userBo == null) {
			logger.info("com.lad.controller.PictureController.createAlbum-----{user:未登录用户,json:"+json+"}");
			return CommonUtil.toErrorResult(ERRORCODE.ACCOUNT_OFF_LINE.getIndex(),
					ERRORCODE.ACCOUNT_OFF_LINE.getReason());
		}
		logger.info("com.lad.controller.PictureController.createAlbum-----{user:"+userBo.getUserName()+",userId:"+userBo.getId()+",json:"+json+"}");
		AlbumBo albumBo = createAlbum(json, userBo.getId());
		Map<String, Object> result = new HashMap<>();
		result.put("ret", 0);
		result.put("aid", albumBo.getId());
		return JSON.toJSONString(result);
	}

	private AlbumBo createAlbum(String json, String uid) {
		logger.info("com.lad.controller.PictureController.createAlbum-----{json:"+json+",uid:"+uid+"}");
		AlbumBo albumBo = json == null ? new AlbumBo() : JSON.parseObject(json, AlbumBo.class);
		// 如果有名字,根据这个名字查询是否有,有则返回,没有则创建
		String name = albumBo.getName();
		if (StringUtils.isEmpty(name)) {
			name = CommonUtil.getDateStr(new Date(), "yyyy-MM-dd");
		}
		// 检查是否存在
		AlbumBo result = pictureService.getAlbumByName(name, uid);
		if (result != null) {
			return result;
		}
		albumBo.setId(UUID.randomUUID().toString().replace("-", ""));
		albumBo.setName(name);
		albumBo.setCreateuid(uid);
		albumBo.setAlbDesc(albumBo.getDeleted() == null ? name : albumBo.getAlbDesc());
		pictureService.insertAlbum(albumBo);
		return albumBo;
	}

	@PostMapping("/test")
	public String test(String q) {
		
		return null;
	}
}
