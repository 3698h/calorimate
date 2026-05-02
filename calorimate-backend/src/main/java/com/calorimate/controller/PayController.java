package com.calorimate.controller;

import com.calorimate.common.Result;
import com.calorimate.dto.PayPrepayDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/pay")
public class PayController {

    @PostMapping("/prepay")
    public Result<?> prepay(@Valid @RequestBody PayPrepayDTO dto) {
        Map<String, String> params = new HashMap<>();
        params.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        params.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));
        params.put("package", "prepay_id=wx_mock_" + UUID.randomUUID().toString().substring(0, 10));
        params.put("signType", "MD5");
        params.put("paySign", "mock_sign_" + UUID.randomUUID().toString().substring(0, 16));
        return Result.success(params);
    }

    @PostMapping("/notify")
    public String payNotify(@RequestBody String body) {
        log.info("Payment notify callback: {}", body);
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }
}
