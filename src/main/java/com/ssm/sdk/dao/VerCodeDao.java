package com.ssm.sdk.dao;


import com.ssm.sdk.common.pojo.VerCodeTO;

public interface VerCodeDao {

    void addCode(VerCodeTO verCode);

    VerCodeTO getCodeByMobile(String mobile);

    void deleteCodeByMobile(String mobile);
}
