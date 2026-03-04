package com.jhzhao.alibaba.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/no-model")
public class MoreModelCallController {

    private final Set<String> modelList = Set.of(
            "qwen3:32b",
            "qwen3-vl:8b",
            "qwen3:8b"
    );

    private final ChatModel ollamaChatModel;

    public MoreModelCallController(
            @Qualifier("ollamaChatModel") ChatModel ollamaChatModel
    ) {
        this.ollamaChatModel = ollamaChatModel;
    }

    @GetMapping("/{model}/{prompt}")
    public String modelChat(
            @PathVariable("model") String model,
            @PathVariable("prompt") String prompt
    ) {
        if (!modelList.contains(model)) {
            return "model not exist";
        }

        System.out.println("===============================================");
        System.out.println("当前输入的模型为：" + model);
        System.out.println("默认模型为：" +"qwen3:8b");
        System.out.println("===============================================");

        // 构建运行时选项，指定模型
        ChatOptions runtimeOptions = ChatOptions.builder().model(model).build();
        // 调用模型
        Generation gen = ollamaChatModel.call(
                        new Prompt(prompt, runtimeOptions))
                .getResult();

        return gen.getOutput().getText();
    }

    }