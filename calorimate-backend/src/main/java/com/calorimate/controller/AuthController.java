package com.calorimate.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.calorimate.common.Result;
import com.calorimate.dto.WxLoginDTO;
import com.calorimate.entity.User;
import com.calorimate.mapper.UserMapper;
import com.calorimate.util.JwtUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${wx.appid}")
    private String appid;

    @Value("${wx.secret}")
    private String secret;

    private final OkHttpClient httpClient = new OkHttpClient();

    @PostMapping("/login")
    public Result<?> wxLogin(@Valid @RequestBody WxLoginDTO dto) {
        String openid;

        if (appid == null || appid.startsWith("your-")) {
            openid = "dev_openid_fixed_for_local_test";
        } else {
            String url = String.format(
                    "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    appid, secret, dto.getCode());
            try {
                Request request = new Request.Builder().url(url).get().build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.body() == null) {
                        return Result.error("微信登录失败: 响应为空");
                    }
                    String body = response.body().string();
                    JSONObject json = JSON.parseObject(body);
                    openid = json.getString("openid");
                    if (openid == null) {
                        return Result.error("微信登录失败: " + json.getString("errmsg"));
                    }
                }
            } catch (Exception e) {
                return Result.error("微信登录失败: " + e.getMessage());
            }
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            int len = openid.length();
            String suffix = openid.substring(Math.max(0, len - 8), len);
            user.setUsername("wx_user_" + suffix);
            user.setPassword(UUID.randomUUID().toString());
            user.setRole("user");
            user.setVipLevel(0);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            userMapper.insert(user);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        return Result.success(data);
    }
}
