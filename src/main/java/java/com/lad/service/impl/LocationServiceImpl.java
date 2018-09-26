package com.lad.service.impl;

import com.lad.bo.LocationBo;
import com.lad.dao.ILocationDao;
import com.lad.service.ILocationService;
import com.mongodb.CommandResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("locationService")
public class LocationServiceImpl implements ILocationService {

	@Autowired
	private ILocationDao locationDao;

	public LocationBo insertUserPoint(LocationBo locationBo) {
		return locationDao.insertUserPoint(locationBo);
	}

	public LocationBo updateUserPoint(LocationBo locationBo) {
		return locationDao.updateUserPoint(locationBo);
	}

	public List<LocationBo> findCircleNear(double px, double py,
			double maxDistance) {
		Point point = new Point(px,py);
		return locationDao.findCircleNear(point, maxDistance);
	}

	public LocationBo getLocationBoById(String locationId) {
		return locationDao.getLocationBoById(locationId);
	}

	public LocationBo getLocationBoByUserid(String userid) {
		return locationDao.getLocationBoByUserid(userid);
	}


	@Override
	public GeoResults<LocationBo> findNearFriends(double[] position, double maxDistance, List<String> friendids) {
		return locationDao.findNearFriends(position, maxDistance, friendids);
	}

	@Override
	public GeoResults<LocationBo> findUserNear(Point point, double maxDistance) {
		return locationDao.findUserNear(point, maxDistance);
	}

	@Override
	public CommandResult findNearCircleByCommond(double px,double py, int distance) {
		return locationDao.findNearCircleByCommond(px,py,  distance);
	}
}
