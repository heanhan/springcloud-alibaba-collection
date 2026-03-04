package com.jhzhao.alibaba.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/prompt/ai")
public class StuffController {

    private final ChatClient chatClient;

    @Value("classpath:/docs/bikes-zh.json")
    private Resource bikesResource;

    @Value("classpath:/prompts/qa-prompt.st")
    private Resource qaPromptResource;

    @Autowired
    public StuffController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping(value = "/stuff")
    public Flux<String> completion(
            @RequestParam(value = "message", required = false, defaultValue = "哪些运动员获得 2008 年夏季奥会乒乓球项目金牌？") String message,
            @RequestParam(value = "stuffit", defaultValue = "false") boolean stuffit
    ) {
        PromptTemplate promptTemplate = new PromptTemplate(qaPromptResource);
        Map<String, Object> map = new HashMap<>();
        map.put("question", message);

        if (stuffit) {
            map.put("context", bikesResource);
        } else {
            map.put("context", "");
        }

        return chatClient.prompt(promptTemplate.create(map))
                .stream().content();
    }
}
