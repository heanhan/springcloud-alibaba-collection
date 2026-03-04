package com.jhzhao.alibaba.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2026/3/4 22:44
 * Version 1.0
 */
@Service
public class CurrencyService {

    @Tool(description = "将一种货币转换为另一种货币")
    public String convertCurrency(
            @ToolParam(description = "源货币代码，如USD") String from,
            @ToolParam(description = "目标货币代码，如CNY") String to,
            @ToolParam(description = "金额") double amount) {

        // 2026年3月示例汇率（仅演示，实际应调用API）
        double rate = 1.0;  // 默认1:1

        if ("USD".equalsIgnoreCase(from) && "CNY".equalsIgnoreCase(to)) {
            rate = 7.15;     // 假设当前1 USD ≈ 7.15 CNY
        } else if ("CNY".equalsIgnoreCase(from) && "USD".equalsIgnoreCase(to)) {
            rate = 1 / 7.15;
        } else if ("EUR".equalsIgnoreCase(from) && "USD".equalsIgnoreCase(to)) {
            rate = 1.09;
        } // ... 可继续加更多硬编码规则

        double convertedAmount = amount * rate;

        return String.format("%.2f %s = %.2f %s (参考汇率: %.4f)",
                amount, from.toUpperCase(),
                convertedAmount, to.toUpperCase(), rate);
    }
}

