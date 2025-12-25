package com.jhzhao.alibaba.springai.controller;


import com.jhzhao.alibaba.model.dto.ModelTypeDto;
import com.jhzhao.alibaba.model.vo.ModelTypeVo;
import com.jhzhao.alibaba.result.ResultBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/model")
@RestController
public class ModelController {

    //获取当前的模型
    @Value(value = "${spring.ai.ollama.chat.model}")
    public String currentModelType;


    @PostMapping(value = "/getCurrentModelType")
    public ResultBody<ModelTypeDto> getCurrentModelType(@RequestBody ModelTypeVo vo){
        //目前没有整合多个模型，因此直接返回 读取的 application.yml的配置
        ModelTypeDto dto = new ModelTypeDto();
        dto.setModelDesc("您好我是 Qwen3:8B 小模型,您有什么问题，我尽力帮你处理！");
        dto.setModelType(currentModelType);
        return ResultBody.success(dto);

    }
}
