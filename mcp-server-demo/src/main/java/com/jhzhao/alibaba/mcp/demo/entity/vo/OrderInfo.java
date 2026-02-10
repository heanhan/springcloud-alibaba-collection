package com.jhzhao.alibaba.mcp.demo.entity.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2026/2/10 16:07
 * Version 1.0
 */
@Data
public class OrderInfo {

    @NotBlank(message = "奶茶的id 不能为空")
    private String id;//奶茶的id

    @NotBlank(message = "奶茶订单数量不能为空")
    private String quantity;//数量
}
