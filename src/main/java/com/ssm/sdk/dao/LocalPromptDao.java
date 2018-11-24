package com.ssm.sdk.dao;


import com.ssm.sdk.common.pojo.LocalPromptTO;

import java.util.List;

public interface LocalPromptDao {
    List<LocalPromptTO> getAllPrompt();
}
