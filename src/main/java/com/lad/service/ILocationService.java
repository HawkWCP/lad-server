package com.lad.service;

import java.util.List;

import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;

import com.lad.bo.LocationBo;
import com.mongodb.CommandResult;

public interface ILocationService {
	public LocationBo insertUserPoint(LocationBo locationBo);
	public LocationBo updateUserPoint(LocationBo locationBo);
	public List<LocationBo> findCircleNear(double px, double py, double maxDistance);
	public LocationBo getLocationBoById(String locationId);
	public LocationBo getLocationBoByUserid(String userid);


	/**
	 * 查找附近好友
	 * @param position
	 * @param friendids
	 * @return
	 */
	GeoResults<LocationBo> findNearFriends(double[] position, double maxDistance, List<String> friendids);

	/**
	 * 查找附近人员信息
	 * @param point
	 * @param maxDistance
	 * @return
	 */
	GeoResults<LocationBo> findUserNear(Point point, double maxDistance);
	
	public CommandResult findNearCircleByCommond(double px,double py, int distance);


}
