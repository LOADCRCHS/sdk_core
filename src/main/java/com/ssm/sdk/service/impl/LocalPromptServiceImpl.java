package com.ssm.sdk.service.impl;

import com.ssm.sdk.common.pojo.LocalPromptTO;
import com.ssm.sdk.dao.LocalPromptDao;
import com.ssm.sdk.service.LocalPromptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class LocalPromptServiceImpl implements LocalPromptService {
    @Autowired
    private LocalPromptDao localPromptDAO;

    @Override
    public List<LocalPromptTO> getAllPrompt() {
        return localPromptDAO.getAllPrompt();
    }
}
