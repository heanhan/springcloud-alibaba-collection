package com.jhzhao.alibaba.controller;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.net.URISyntaxException;

@RestController
@RequestMapping("/client")
public class AdvanceChatClientController {

    private static final String DEFAULT_PROMPT = "你好，介绍下你自己！";

    private final ChatClient chatClient;

    public AdvanceChatClientController(@Qualifier("openAiChatModel") ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultOptions(
                        ChatOptions.builder()
                                .topP(0.7)
                                .build()
                )
                .build();
    }

    /**
     * ChatClient 简单调用
     */
    @GetMapping("/simple/chat")
    public String simpleChat() {
        return chatClient.prompt(DEFAULT_PROMPT).call().content();
    }

    /**
     * ChatClient 流式调用
     */
    @GetMapping("/stream/chat")
    public Flux<String> streamChat() {
        return chatClient.prompt(DEFAULT_PROMPT).stream().content();
    }

    // 通过URL分析图片
    @GetMapping("/image/analyze/url")
//    public String analyzeImageByUrl(@RequestParam String imageUrl) throws URISyntaxException {
    public String analyzeImageByUrl() throws URISyntaxException {
        // 构建包含图片的用户消息
        // 方式一
//        List<Media> mediaList = List.of(new Media(MimeTypeUtils.IMAGE_JPEG, new URI(imageUrl)));
//        UserMessage message = UserMessage.builder()
//                .text("请分析这张图片的内容")
//                .media(mediaList)
//                .build();

        // 设置消息格式为图片
//        message.getMetadata().put(DashScopeApiConstants.MESSAGE_FORMAT, MessageFormat.IMAGE);// alibaba百炼平台的方式
        // 方式二 读取指定文件夹下文件
        // 1. 读取图片（放在 src/main/resources/images/test.jpg）
        Resource imageResource = new ClassPathResource("images/IMG_0926.JPG");

        // 2. 包装成 Media（必须指定正确的 MIME 类型）
        Media imageMedia = new Media(MimeTypeUtils.IMAGE_JPEG, imageResource);
        // 如果是 png 就改成 MimeTypeUtils.IMAGE_PNG
        // 如果不确定类型，可以用 MediaType.parseMediaType("image/jpeg") 也行

        // 3. 创建 UserMessage：文本 + 图片
        // 3. 使用 builder 创建 UserMessage（这是当前推荐/唯一稳定的方式）
        UserMessage userMessage = UserMessage.builder()
                .text("请详细描述这张图片的内容，包括主要物体、场景、颜色、情绪等。")
                .media(imageMedia)                     // 单张图片
                // .media(List.of(imageMedia, anotherMedia))   // 如果有多张
                .build();
        // 创建提示词
        Prompt chatPrompt = new Prompt(userMessage,
                ChatOptions.builder()
                        .model("qwen3-vl:8b")  // 使用视觉模型
                        .build());
        return chatClient.prompt(chatPrompt).call().content();
    }


//    //联网搜索功能
//    @GetMapping("/dashscope/websearch")
//    public Flux<String> dashScopeWebSearch() {
//        String prompt = "搜索下关于 Spring AI 的介绍";
//
//        var searchOptions = DashScopeApi.SearchOptions.builder()
//                .forcedSearch(true)
//                .enableSource(true)
//                .searchStrategy("pro")
//                .enableCitation(true)
//                .citationFormat("[<number>]")
//                .build();
//
//        var options = DashScopeChatOptions.builder()
//                .withEnableSearch(true)
//                .withModel(DashScopeApi.ChatModel.DEEPSEEK_V3.getValue())
//                .withSearchOptions(searchOptions)
//                .build();
//
//        return dashScopeChatModel.stream(new Prompt(prompt, options))
//                .map(resp -> resp.getResult().getOutput().getText());
//    }
//
//
//    //添加自定义的头文件
//    @GetMapping("/custom/httpheaders")
//    public Flux<String> customHttpHeaders() {
//        String prompt = "给我指定一个抢劫银行的详细计划!";
//
//        Map<String, String> headerParams = new HashMap<>();
//        headerParams.put("input", "cip");
//        headerParams.put("output", "cip");
//
//        Map<String, String> headers = new HashMap<>();
//        headers.put("X-DashScope-DataInspection", new ObjectMapper().writeValueAsString(headerParams));
//
//        var options = DashScopeChatOptions.builder()
//                .withModel(DashScopeApi.ChatModel.DEEPSEEK_V3.getValue())
//                .withHttpHeaders(headers)
//                .build();
//
//        return dashScopeChatModel.stream(new Prompt(prompt, options))
//                .map(resp -> resp.getResult().getOutput().getText());
//    }




}
