package com.jhzhao.alibaba.model.vo;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatRequestVo {

    //会话内容 prompt
    @NotNull(message = "会话内容不能为空")
    private String message;

    //会话的id
    @NotNull(message = "会话的id不能为空")
    private String conversationId;
}
