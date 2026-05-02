package com.calorimate.service;

import com.calorimate.dto.LoginDTO;
import com.calorimate.dto.RegisterDTO;
import com.calorimate.dto.UpdateProfileDTO;
import com.calorimate.vo.UserVO;

import java.util.Map;

public interface UserService {

    void register(RegisterDTO dto);

    Map<String, Object> login(LoginDTO dto);

    UserVO getUserInfo(Long userId);

    void updateProfile(Long userId, UpdateProfileDTO dto);

    Map<String, Object> getRemainFreeTimes(Long userId);

    void addFreeTimes(Long userId);

    Map<String, Object> calculateDailyTarget(Long userId);
}
