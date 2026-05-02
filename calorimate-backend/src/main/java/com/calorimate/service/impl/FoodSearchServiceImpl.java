package com.calorimate.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.calorimate.entity.Food;
import com.calorimate.service.FoodSearchService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class FoodSearchServiceImpl implements FoodSearchService {

    private static final Logger log = LoggerFactory.getLogger(FoodSearchServiceImpl.class);

    @Value("${tianapi.base-url}")
    private String baseUrl;

    @Value("${tianapi.api-key}")
    private String apiKey;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    @Override
    public List<Food> searchFood(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String url = baseUrl + "/nutrient/index"
                + "?key=" + apiKey
                + "&mode=0"
                + "&word=" + keyword.trim()
                + "&num=20";

        log.info("天聚数行营养成分查询请求: {}", url);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                log.error("天聚数行API请求失败, 状态码: {}", response.code());
                return Collections.emptyList();
            }

            String responseBody = response.body().string();
            log.info("天聚数行API响应: {}", responseBody);

            JSONObject jsonResponse = JSON.parseObject(responseBody);
            int code = jsonResponse.getIntValue("code");
            if (code != 200) {
                String msg = jsonResponse.getString("msg");
                log.warn("天聚数行API返回错误, code: {}, msg: {}", code, msg);
                return Collections.emptyList();
            }

            JSONObject result = jsonResponse.getJSONObject("result");
            if (result == null) {
                log.info("天聚数行API返回空结果, keyword: {}", keyword);
                return Collections.emptyList();
            }

            List<Food> foods = new ArrayList<>();

            JSONArray list = result.getJSONArray("list");
            if (list != null && !list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    JSONObject item = list.getJSONObject(i);
                    Food food = convertToFood(item);
                    if (food != null) {
                        foods.add(food);
                    }
                }
            } else if (result.containsKey("name")) {
                Food food = convertToFood(result);
                if (food != null) {
                    foods.add(food);
                }
            }

            log.info("天聚数行营养成分查询完成, keyword: {}, 结果数: {}", keyword, foods.size());
            return foods;
        } catch (IOException e) {
            log.error("天聚数行API网络异常: {}", e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("天聚数行API响应解析异常: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private Food convertToFood(JSONObject item) {
        if (item == null || !item.containsKey("name")) {
            return null;
        }

        Food food = new Food();
        food.setName(item.getString("name"));
        food.setCategory(item.getString("type"));
        food.setCalories(item.getDouble("rl"));
        food.setProtein(item.getDouble("dbz"));
        food.setFat(item.getDouble("zf"));
        food.setCarbs(item.getDouble("shhf"));
        food.setUnit("100g");
        return food;
    }
}
