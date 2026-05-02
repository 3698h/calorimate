package com.calorimate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.calorimate.dto.LoginDTO;
import com.calorimate.dto.RegisterDTO;
import com.calorimate.dto.UpdateProfileDTO;
import com.calorimate.entity.User;
import com.calorimate.mapper.UserMapper;
import com.calorimate.service.UserService;
import com.calorimate.util.JwtUtil;
import com.calorimate.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private static final int DEFAULT_FREE_TIMES = 3;
    private final Map<String, Integer> dailyFreeTimes = new ConcurrentHashMap<>();

    private String timesKey(Long userId) {
        return userId + ":" + LocalDate.now();
    }

    @Override
    public void register(RegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("两次密码输入不一致");
        }

        if (dto.getPassword().length() < 6) {
            throw new RuntimeException("密码不能少于6位");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        Long count = userMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setHeight(dto.getHeight());
        user.setWeight(dto.getWeight());
        user.setAge(dto.getAge());
        user.setGender(dto.getGender());
        user.setTargetCalories(dto.getTargetCalories() != null ? dto.getTargetCalories() : 2000.0);
        user.setRole("user");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);
    }

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        return result;
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public void updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (dto.getGender() != null)
            user.setGender(dto.getGender());
        if (dto.getBirthday() != null)
            user.setBirthday(dto.getBirthday());
        if (dto.getHeight() != null)
            user.setHeight(dto.getHeight());
        if (dto.getWeight() != null)
            user.setWeight(dto.getWeight());
        if (dto.getTargetWeight() != null)
            user.setTargetWeight(dto.getTargetWeight());
        if (dto.getAge() != null)
            user.setAge(dto.getAge());
        if (dto.getActivityLevel() != null)
            user.setActivityLevel(dto.getActivityLevel());
        if (dto.getGoal() != null)
            user.setGoal(dto.getGoal());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        try {
            Map<String, Object> target = calculateDailyTarget(userId);
            user.setTargetCalories(((Number) target.get("targetCalories")).doubleValue());
            userMapper.updateById(user);
        } catch (Exception e) {
            // 资料不完整导致目标计算失败时不阻塞保存
        }
    }

    @Override
    public Map<String, Object> getRemainFreeTimes(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null && user.getVipLevel() != null && user.getVipLevel() > 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("remain", 9999);
            return result;
        }
        String key = timesKey(userId);
        int used = dailyFreeTimes.getOrDefault(key, 0);
        int remain = Math.max(0, DEFAULT_FREE_TIMES - used);
        Map<String, Object> result = new HashMap<>();
        result.put("remain", remain);
        return result;
    }

    @Override
    public void addFreeTimes(Long userId) {
        String key = timesKey(userId);
        int used = dailyFreeTimes.getOrDefault(key, 0);
        dailyFreeTimes.put(key, Math.max(0, used - 1));
    }

    @Override
    public Map<String, Object> calculateDailyTarget(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getHeight() == null || user.getWeight() == null || user.getAge() == null || user.getGender() == null) {
            throw new RuntimeException("用户信息不完整，请先完善身高、体重、年龄、性别");
        }

        double height = user.getHeight();
        double weight = user.getWeight();
        int age = user.getAge();
        String gender = user.getGender();

        double bmr;
        if ("male".equalsIgnoreCase(gender) || "男".equals(gender)) {
            bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        } else {
            bmr = 10 * weight + 6.25 * height - 5 * age - 161;
        }

        double activityFactor;
        String activityLevel = user.getActivityLevel();
        if (activityLevel == null || activityLevel.isEmpty()) {
            activityFactor = 1.2;
        } else {
            switch (activityLevel) {
                case "sedentary":
                    activityFactor = 1.2;
                    break;
                case "light":
                    activityFactor = 1.375;
                    break;
                case "moderate":
                    activityFactor = 1.55;
                    break;
                case "active":
                    activityFactor = 1.725;
                    break;
                default:
                    activityFactor = 1.2;
            }
        }

        double targetCalories = bmr * activityFactor;

        String goal = user.getGoal();
        if (goal != null) {
            switch (goal) {
                case "lose":
                    targetCalories -= 500;
                    break;
                case "gain":
                    targetCalories += 500;
                    break;
                default:
                    break;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("bmr", Math.round(bmr));
        result.put("targetCalories", Math.round(targetCalories));
        result.put("formula", "Mifflin-St Jeor");
        return result;
    }

    @Override
    public void updateVipExpireTime(Long userId, int days) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = user.getVipExpireTime();

        if (expireTime == null || expireTime.isBefore(now)) {
            expireTime = now.plusDays(days);
        } else {
            expireTime = expireTime.plusDays(days);
        }

        user.setVipExpireTime(expireTime);
        if (user.getVipLevel() == null || user.getVipLevel() == 0) {
            user.setVipLevel(1);
        }
        user.setUpdateTime(now);
        userMapper.updateById(user);

        log.info("用户 {} 会员已延期 {} 天，新到期时间: {}", userId, days, expireTime);
    }
}
