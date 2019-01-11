package com.lad.service.impl;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lad.bo.CareAndPassBo;
import com.lad.bo.CareBo;
import com.lad.dao.ICareDao;
import com.lad.service.ICareService;
import com.mongodb.WriteResult;
@Service("careAndPassService")
public class CareServiceImpl implements ICareService {
	@Autowired
	private ICareDao careDao;
	
	@Override
	public Map<String, Set<String>> findMarriageCareMap(String mainId) {
		return careDao.findMarriageCareMap(mainId);
	}

	@Override
	public Map<String, Set<String>> findSpouseCareMap(String mainId) {
		return careDao.findSpouseCareMap(mainId);
	}

	@Override
	public Map<String, Set<String>> findTravelersCareMap(String mainId) {
		return careDao.findTravelersCareMap(mainId);
	}
	
	@Override
	public String insert(CareAndPassBo care) {
		return careDao.insert(care);
	}

	@Override
	public CareAndPassBo findMarriageCare(String mainId) {
		return careDao.findMarriageCare(mainId);
	}

	@Override
	public CareAndPassBo findSpouseCare(String mainId) {
		return careDao.findSpouseCare(mainId);
	}

	@Override
	public CareAndPassBo findTravelersCare(String mainId) {
		return careDao.findTravelersCare(mainId);
	}

	@Override
	public CareAndPassBo findMarriagePass(String mainId) {
		return careDao.findMarriagePass(mainId);
	}

	@Override
	public CareAndPassBo findSpousePass(String mainId) {
		return careDao.findSpousePass(mainId);
	}

	@Override
	public CareAndPassBo findTravelersPass(String mainId) {
		return careDao.findTravelersPass(mainId);
	}

	@Override
	public String test() {
		return careDao.test();
	}

	@Override
	public WriteResult updateCare(String situation, String mainId, Map<String, Set<String>> careRoster) {
		return careDao.updateCare(situation,mainId, careRoster);
	}
	
	@Override
	public WriteResult updatePass(String situation, String mainId, Set<String> passRoster) {
		return careDao.updatePass(situation, mainId, passRoster);
	}
	

	@Override
	public Set<String> findMarriagePassList(String mainId) {
		return careDao.findMarriagePassList(mainId);
	}

	@Override
	public Set<String> findSpousePassList(String mainId) {
		return careDao.findSpousePassList(mainId);
	}

	@Override
	public Set<String> findTravelersPassList(String mainId) {
		return careDao.findTravelersPassList(mainId);
	}

	@Override
	public CareBo findCareByUidAndOid(String uid, String oid, int type) {
		return careDao.findCareByUidAndOid(uid,oid,type);
	}

	@Override
	public void updateCare(CareBo careBo) {
		careDao.updateCare( careBo);
	}

	@Override
	public CareBo insert(CareBo careBo) {
		return careDao.insert(careBo);
	}







}
