package com.calorimate.controller;

import com.calorimate.annotation.CurrentUser;
import com.calorimate.common.Result;
import com.calorimate.dto.PayPrepayDTO;
import com.calorimate.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/pay")
public class PayController {

    @Autowired
    private UserService userService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${wx.pay.sandbox:false}")
    private boolean sandboxEnabled;

    @Value("${wx.appid:}")
    private String appId;

    @Value("${wx.secret:}")
    private String appSecret;

    @Value("${wx.pay.mch-id:}")
    private String mchId;

    @Value("${wx.pay.api-key:}")
    private String apiKey;

    @Value("${wx.pay.sandbox-key:12345678901234567890123456789012}")
    private String sandboxKey;

    private static final String UNIFIED_ORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    private static final String SANDBOX_UNIFIED_ORDER_URL = "https://api.mch.weixin.qq.com/sandboxnew/pay/unifiedorder";
    private static final String JSCODE_2_SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @PostMapping("/prepay")
    public Result<?> prepay(@Valid @RequestBody PayPrepayDTO dto) {
        String signKey = sandboxEnabled ? sandboxKey : apiKey;
        String orderUrl = sandboxEnabled ? SANDBOX_UNIFIED_ORDER_URL : UNIFIED_ORDER_URL;

        log.info("支付预下单 - 套餐: {}, 沙箱模式: {}", dto.getPlanType(), sandboxEnabled);

        try {
            String openid = getOpenId(dto.getCode());
            log.info("获取openid成功: {}", openid);

            String nonceStr = UUID.randomUUID().toString().replace("-", "");
            String outTradeNo = "ORD" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
            String totalFee = "monthly".equals(dto.getPlanType()) ? "990" : "9900";
            String body = "食光机会员-" + dto.getPlanType();

            String prepayId = unifiedOrder(orderUrl, signKey, openid, nonceStr, outTradeNo, totalFee, body);
            log.info("统一下单成功, prepay_id: {}", prepayId);

            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
            String packageStr = "prepay_id=" + prepayId;
            String signType = "MD5";

            Map<String, String> paySignParams = new TreeMap<>();
            paySignParams.put("appId", appId);
            paySignParams.put("timeStamp", timeStamp);
            paySignParams.put("nonceStr", nonceStr);
            paySignParams.put("package", packageStr);
            paySignParams.put("signType", signType);

            String paySign = generateSign(paySignParams, signKey);

            Map<String, String> result = new HashMap<>();
            result.put("appId", appId);
            result.put("timeStamp", timeStamp);
            result.put("nonceStr", nonceStr);
            result.put("package", packageStr);
            result.put("signType", signType);
            result.put("paySign", paySign);

            return Result.success(result);
        } catch (Exception e) {
            log.error("支付预下单失败", e);
            return Result.error(500, "支付预下单失败: " + e.getMessage());
        }
    }

    private String getOpenId(String code) {
        String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                JSCODE_2_SESSION_URL, appId, appSecret, code);
        log.info("调用jscode2session: {}", url.replaceAll("secret=[^&]*", "secret=***"));

        @SuppressWarnings("unchecked")
        Map<String, Object> resp = restTemplate.getForObject(url, Map.class);
        if (resp == null) {
            throw new RuntimeException("微信接口无响应");
        }
        log.info("jscode2session响应: {}", resp);

        Object errcode = resp.get("errcode");
        if (errcode != null && !Integer.valueOf(0).equals(errcode)) {
            throw new RuntimeException("获取openid失败: " + resp.get("errmsg"));
        }

        String openid = (String) resp.get("openid");
        if (openid == null || openid.isEmpty()) {
            throw new RuntimeException("openid为空");
        }
        return openid;
    }

    private String unifiedOrder(String orderUrl, String signKey, String openid,
            String nonceStr, String outTradeNo, String totalFee, String body) {
        Map<String, String> params = new TreeMap<>();
        params.put("appid", appId);
        params.put("mch_id", mchId);
        params.put("nonce_str", nonceStr);
        params.put("body", body);
        params.put("out_trade_no", outTradeNo);
        params.put("total_fee", totalFee);
        params.put("spbill_create_ip", "127.0.0.1");
        params.put("notify_url", "https://your-domain.com/api/pay/notify");
        params.put("trade_type", "JSAPI");
        params.put("openid", openid);

        String sign = generateSign(params, signKey);
        params.put("sign", sign);

        String xml = buildXml(params);
        log.info("统一下单请求: {}", xml);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        HttpEntity<String> entity = new HttpEntity<>(xml, headers);

        String respXml = restTemplate.postForObject(orderUrl, entity, String.class);
        log.info("统一下单响应: {}", respXml);

        if (respXml == null || !respXml.contains("<return_code><![CDATA[SUCCESS]]></return_code>")) {
            String msg = extractXmlField(respXml, "return_msg");
            throw new RuntimeException("统一下单失败: " + msg);
        }

        String resultCode = extractXmlField(respXml, "result_code");
        if (!"SUCCESS".equals(resultCode)) {
            String errCodeDes = extractXmlField(respXml, "err_code_des");
            throw new RuntimeException("统一下单业务失败: " + errCodeDes);
        }

        String prepayId = extractXmlField(respXml, "prepay_id");
        if (prepayId == null) {
            throw new RuntimeException("未获取到prepay_id");
        }
        return prepayId;
    }

    private String generateSign(Map<String, String> params, String signKey) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String val = entry.getValue();
            if (val != null && !val.isEmpty()) {
                sb.append(entry.getKey()).append("=").append(val).append("&");
            }
        }
        sb.append("key=").append(signKey);

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02X", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5签名失败", e);
        }
    }

    private String buildXml(Map<String, String> params) {
        StringBuilder xml = new StringBuilder("<xml>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            xml.append("<").append(entry.getKey()).append(">")
                    .append("<![CDATA[").append(entry.getValue()).append("]]>")
                    .append("</").append(entry.getKey()).append(">");
        }
        xml.append("</xml>");
        return xml.toString();
    }

    private String extractXmlField(String xml, String field) {
        if (xml == null)
            return null;
        String openTag = "<" + field + "><![CDATA[";
        String closeTag = "]]></" + field + ">";
        int start = xml.indexOf(openTag);
        int end = xml.indexOf(closeTag);
        if (start >= 0 && end > start) {
            return xml.substring(start + openTag.length(), end);
        }

        openTag = "<" + field + ">";
        closeTag = "</" + field + ">";
        start = xml.indexOf(openTag);
        end = xml.indexOf(closeTag);
        if (start >= 0 && end > start) {
            return xml.substring(start + openTag.length(), end);
        }
        return null;
    }

    @PostMapping("/notify")
    public String payNotify(@RequestBody String body) {
        log.info("支付回调: {}", body);
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }

    @PostMapping("/mock-success")
    public Result<?> mockPaySuccess(@CurrentUser Long userId) {
        log.info("模拟支付成功 - 用户ID: {}", userId);
        userService.updateVipExpireTime(userId, 30);
        return Result.success("模拟支付成功，会员已延期30天");
    }
}
