package com.jhzhao.alibaba.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Set;

/**
 * 使用ChatClient实现多模型调用
 */
@RestController
@RequestMapping("/moremodelchatclient")
public class MoreModelChatClientController {

    private final Set<String> modelList = Set.of(
            "qwen3:32b",
            "qwen3-vl:8b",
            "qwen3:8b"
    );
    private final ChatClient chatClient;

    public MoreModelChatClientController(
            @Qualifier("ollamaChatModel") OllamaChatModel chatModel
    ) {
        // 构建ChatClient，使用DashScope作为默认
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    @GetMapping
    public Flux<String> stream(
            @RequestParam("prompt") String prompt,
            @RequestHeader(value = "models", required = false) String models
    ) {
        // 检查模型是否存在
        if (!modelList.contains(models)) {
            return Flux.just("model not exist");
        }

        // 使用ChatClient调用模型
        return chatClient.prompt(prompt)
                .options(OllamaChatOptions.builder()
                        .model(models)
                        .build()
                ).stream()
                .content();
    }
}
