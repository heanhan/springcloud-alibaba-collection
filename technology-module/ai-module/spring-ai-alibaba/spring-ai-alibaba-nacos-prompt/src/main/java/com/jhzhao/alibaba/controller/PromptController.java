package com.jhzhao.alibaba.controller;

import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplate;
import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplateFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/nacos")
public class PromptController {

    private static final Logger logger = LoggerFactory.getLogger(PromptController.class);

    private final ChatClient client;
    private final ConfigurablePromptTemplateFactory promptTemplateFactory;

    public PromptController(
            ChatModel chatModel,
            ConfigurablePromptTemplateFactory promptTemplateFactory
    ) {
        this.client = ChatClient.builder(chatModel).build();
        this.promptTemplateFactory = promptTemplateFactory;
    }

    @GetMapping("/books")
    public Flux<String> generateJoke(
            @RequestParam(value = "author", required = false, defaultValue = "鲁迅") String authorName
    ) {
        // 使用Nacos的Prompt模板创建Prompt
        ConfigurablePromptTemplate template = promptTemplateFactory.create(
                "author",
                "please list the three most famous books by this {author}."
        );
        Prompt prompt = template.create(Map.of("author", authorName));
        logger.info("最终构建的 prompt 为：{}", prompt.getContents());

        return client.prompt(prompt)
                .stream()
                .content();
    }
}

