package com.jhzhao.alibaba.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/example/ai")
public class RoleController {

    private final ChatClient chatClient;

    @Value("classpath:/prompts/system-message.st")
    private Resource systemResource;

    @Autowired
    public RoleController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/roles")
    public Flux<String> generate(
            @RequestParam(value = "message", required = false, defaultValue = "请告诉我关于阿里巴巴和40个大盗的故事以及故事最后告诉我们的道理。并且给这个故事写一句推荐词。") String message,
            @RequestParam(value = "name", required = false, defaultValue = "jack") String name,
            @RequestParam(value = "voice", required = false, defaultValue = "pirate") String voice
    ) {
        UserMessage userMessage = new UserMessage(message);
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("name", name, "voice", voice));

        return chatClient.prompt(
                        new Prompt(List.of(
                                userMessage,
                                systemMessage)))
                .stream().content();
    }


}
