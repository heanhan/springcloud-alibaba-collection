package com.jhzhao.alibaba.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2026/3/3 20:40
 * Version 1.0
 */

@Slf4j
@RequestMapping(value = "/hello")
@RestController
public class HelloChatController {

    private static final String DEFAULT_PROMPT = "你是一个博学的智能聊天助手，请根据用户提问回答！";

    private final ChatClient client;



//    ChatModel vs ChatClient
//    ChatModel：底层API，直接与模型交互，提供最大控制权
//    ChatClient：高级API，封装了常用功能（日志、参数设置等），更易用

//   精度设置参考
//    // 1. 最推荐的日常通用设置（类似 Grok / ChatGPT 默认风格）
//    ChatOptions options = ChatOptions.builder()
//            .temperature(0.7)
//            .topP(0.95)           // 或干脆不设，让模型默认 1.0
//            .frequencyPenalty(0.1) // 轻微惩罚重复
//            .presencePenalty(0.0)
//            .maxTokens(2048)
//            .build();
//
//    // 2. 写代码、技术回答、结构化输出（最稳）
//    ChatOptions strict = ChatOptions.builder()
//            .temperature(0.2)
//            .topP(0.95)
//            .frequencyPenalty(0.2)
//            .maxTokens(4096)
//            .build();
//
//    // 3. 创意模式（写小说、广告、段子）
//    ChatOptions creative = ChatOptions.builder()
//            .temperature(1.0)
//            .topP(0.90)
//            .frequencyPenalty(0.4)
//            .presencePenalty(0.5)
//            .maxTokens(4096)
//            .build();
//
//    // 4. Grok-4 / o1 风格极致理性（数学、推理、复杂问题）
//    ChatOptions reasoning = ChatOptions.builder()
//            .temperature(0.0)     // 或 0.1
//            .topP(1.0)
//            .maxTokens(8192)      // 给足思考空间
//            .build();
    public HelloChatController(ChatClient.Builder chatClientBuilder) {
        this.client = chatClientBuilder
                //给对话设置默认的提示词
                .defaultSystem(DEFAULT_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().build()).build())
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultOptions(
                        ChatOptions.builder()
                                .temperature(0.2)
                                .topP(0.95)
                                .frequencyPenalty(0.2)
                                .maxTokens(4096)
                                .build()
                ).build();
    }

    @GetMapping(value = "/simpleChat")
    public String simpleChat(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？") String query){
        log.info("进入simpleChat的模式对话");
        String content = client.prompt(query).call().content();
        return content;
    }

    @GetMapping("/stream/chat")
    public Flux<String> streamChat(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？") String query, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        return client.prompt(query).stream().content();
    }

    @GetMapping("/advisor/chat/{conversationId}")
    public Flux<String> advisorChat(
            HttpServletResponse response,
            @PathVariable String conversationId,
            @RequestParam String query
    ) {
        response.setCharacterEncoding("UTF-8");
        return this.client.prompt(query)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream().content();
    }
}
