package com.ssm.sdk.service.impl;

import com.ssm.sdk.common.pojo.CPTO;
import com.ssm.sdk.dao.CPDao;
import com.ssm.sdk.service.CPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CPServiceImpl implements CPService {

    @Autowired
    private CPDao cpdao;

    @Override
    public List<CPTO> getAllCPList() {
        return cpdao.getAllCPList();
    }
}
