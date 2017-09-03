package com.lad.dao;

import com.lad.bo.PartyBo;
import com.mongodb.WriteResult;

import java.util.List;

/**
 * ����������
 * Copyright: Copyright (c) 2017
 * Version: 1.0
 * Time:2017/8/20
 */
public interface IPartyDao {

    /**
     * ��Ӿۻ�
     * @param partyBo
     * @return
     */
    PartyBo insert(PartyBo partyBo);

    /**
     * �޸ľۻ�
     * @param partyBo
     * @return
     */
    WriteResult update(PartyBo partyBo);

    /**
     * ɾ���ۻ�
     * @param id
     * @return
     */
    WriteResult delete(String id);

    /**
     * �����ҷ���ľۻ�
     * @param createid
     * @return
     */
    List<PartyBo> findByCreate(String createid);


}
