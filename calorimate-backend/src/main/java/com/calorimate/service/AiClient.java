package com.calorimate.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Component
public class AiClient {

    private static final Logger log = LoggerFactory.getLogger(AiClient.class);

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.base-url}")
    private String baseUrl;

    @Value("${ai.model}")
    private String model;

    @Value("${ai.vision-model:deepseek-v4-pro}")
    private String visionModel;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)  // 图像识别可能需要更长时间
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public String chat(String systemPrompt, String userMessage) {
        JSONObject request = new JSONObject();
        request.put("model", model);

        JSONArray messages = new JSONArray();

        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        request.put("messages", messages);
        request.put("temperature", 0.7);
        request.put("max_tokens", 2000);

        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                JSON.toJSONString(request),
                MediaType.parse("application/json; charset=utf-8")
        );

        log.info("AI 请求URL: {}", baseUrl + "/chat/completions");
        log.info("AI 请求体: {}", JSON.toJSONString(request));

        okhttp3.Request httpRequest = new okhttp3.Request.Builder()
                .url(baseUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String errBody = response.body() != null ? response.body().string() : "无响应体";
                throw new RuntimeException("AI API 调用失败，状态码: " + response.code() + "，响应: " + errBody);
            }
            String responseBody = response.body().string();
            log.info("AI 响应体: {}", responseBody);
            JSONObject jsonResponse = JSON.parseObject(responseBody);
            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject message = firstChoice.getJSONObject("message");
                return message.getString("content");
            }
            throw new RuntimeException("AI API 返回为空");
        } catch (IOException e) {
            throw new RuntimeException("AI API 调用异常: " + e.getMessage());
        }
    }

    public String chatWithImage(String systemPrompt, String userMessage, InputStream imageStream, String imageFormat) {
        try {
            // 读取图像并转换为base64
            byte[] imageBytes = imageStream.readAllBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String dataUri = "data:image/" + imageFormat + ";base64," + base64Image;

            JSONObject request = new JSONObject();
            request.put("model", visionModel);

            JSONArray messages = new JSONArray();

            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                JSONObject systemMsg = new JSONObject();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemPrompt);
                messages.add(systemMsg);
            }

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");

            // 构建包含文本和图像的content数组
            JSONArray contentArray = new JSONArray();
            
            JSONObject textContent = new JSONObject();
            textContent.put("type", "text");
            textContent.put("text", userMessage);
            contentArray.add(textContent);

            JSONObject imageContent = new JSONObject();
            imageContent.put("type", "image_url");
            JSONObject imageUrl = new JSONObject();
            imageUrl.put("url", dataUri);
            imageContent.put("image_url", imageUrl);
            contentArray.add(imageContent);

            userMsg.put("content", contentArray);
            messages.add(userMsg);

            request.put("messages", messages);
            request.put("temperature", 0.7);
            request.put("max_tokens", 3000);  // 图像分析可能需要更多输出

            okhttp3.RequestBody body = okhttp3.RequestBody.create(
                    JSON.toJSONString(request),
                    MediaType.parse("application/json; charset=utf-8")
            );

            log.info("AI Vision 请求URL: {}", baseUrl + "/chat/completions");
            log.info("AI Vision 请求模型: {}", visionModel);

            okhttp3.Request httpRequest = new okhttp3.Request.Builder()
                    .url(baseUrl + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            try (Response response = client.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    String errBody = response.body() != null ? response.body().string() : "无响应体";
                    throw new RuntimeException("AI Vision API 调用失败，状态码: " + response.code() + "，响应: " + errBody);
                }
                String responseBody = response.body().string();
                log.info("AI Vision 响应体: {}", responseBody);
                JSONObject jsonResponse = JSON.parseObject(responseBody);
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    JSONObject message = firstChoice.getJSONObject("message");
                    return message.getString("content");
                }
                throw new RuntimeException("AI Vision API 返回为空");
            }
        } catch (IOException e) {
            throw new RuntimeException("AI Vision API 调用异常: " + e.getMessage());
        }
    }
}
