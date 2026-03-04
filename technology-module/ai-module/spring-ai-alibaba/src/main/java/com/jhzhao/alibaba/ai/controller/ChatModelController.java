package com.jhzhao.alibaba.ai.controller;


import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/model")
public class ChatModelController {


    private static final String DEFAULT_PROMPT = "你好，介绍下你自己吧。";

    private final ChatModel chatModel;

    public ChatModelController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 最简单的使用方式，没有任何参数注入
     */
    @GetMapping("/simple/chat")
    public String simpleChat() {
        return chatModel.call(new Prompt(DEFAULT_PROMPT, ChatOptions
                .builder()
                .model("qwen3:8b")
                .build())).getResult().getOutput().getText();
    }

    /**
     * Stream 流式调用，实现打字机效果
     */
    @GetMapping("/stream/chat")
    public Flux<String> streamChat(HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");

        Flux<ChatResponse> stream = chatModel.stream(new Prompt(DEFAULT_PROMPT, ChatOptions
                .builder()
                .model("qwen3:8b")
                .build()));
        return stream.map(resp -> resp.getResult().getOutput().getText());
    }


    /**
     * 获取token信息
     */
    @GetMapping("/tokens")
    public Map<String, Object> tokens(HttpServletResponse response) {
        ChatResponse chatResponse = chatModel.call(new Prompt(DEFAULT_PROMPT, ChatOptions
                .builder()
                .model("qwen3:8b")
                .build()));

        Map<String, Object> res = new HashMap<>();
        res.put("output", chatResponse.getResult().getOutput().getText());
        res.put("output_token", chatResponse.getMetadata().getUsage().getCompletionTokens());
        res.put("input_token", chatResponse.getMetadata().getUsage().getPromptTokens());
        res.put("total_token", chatResponse.getMetadata().getUsage().getTotalTokens());

        return res;
    }

    /**
     * 自定义参数调用
     */
    @GetMapping("/custom/chat")
    public String customChat() {
        ChatOptions customOptions = ChatOptions.builder()
                .topP(0.7)
                .topK(50)
                .temperature(0.8)
                .build();

        return chatModel.call(new Prompt(DEFAULT_PROMPT, customOptions)).getResult().getOutput().getText();
    }

//    // 通过URL分析图片
//    @GetMapping("/image/analyze/url")
//    public String analyzeImageByUrl(@RequestParam String imageUrl) throws URISyntaxException {
//        // 构建包含图片的用户消息
//        List<Media> mediaList = List.of(new Media(MimeTypeUtils.IMAGE_JPEG, new URI(imageUrl)));
//        UserMessage message = UserMessage.builder()
//                .text("请分析这张图片的内容")
//                .media(mediaList)
//                .build();
//
//        // 设置消息格式为图片
//        message.getMetadata().put("模型名称", MessageFormat.IMAGE);
//
//        // 创建提示词
//        Prompt chatPrompt = new Prompt(message,
//                ChatOptions.builder()
//                        .model("qwen-vl-max-latest")  // 使用视觉模型
//                        .(true)             // 启用多模态
//                        .build());
//
//        return ChatClient.prompt(chatPrompt).call().content();
//    }



}
