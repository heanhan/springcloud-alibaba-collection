package com.jhzhao.alibaba.controller;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 多平台调用示例：DashScope、Ollama、OpenAI
 */
@RestController
@RequestMapping("/noplatform")
public class MorePlatformController {

    /**
     * @Qualifier注解指定注入的bean名称
     * - dashscopeChatModel: DashScope平台的ChatModel实现
     * - ollamaChatModel: Ollama平台的ChatModel实现
     * - openAiChatModel: OpenAI平台的ChatModel实现
     */

//    @Autowired(required = false)
//    @Qualifier("dashScopeChatModel")
//    public DashScopeChatModel dashScopeChatModel;

    @Autowired(required = false)
    @Qualifier("ollamaChatModel")
    public OllamaChatModel ollamaChatModel;

//    @Autowired(required = false)
//    @Qualifier("openAIChatModel")
//    public OpenAiChatModel openAIChatModel;

    @GetMapping("/{platform}/{prompt}")
    public String chat(
            @PathVariable("platform") String platform,
            @PathVariable("prompt") String prompt
    ) {
        System.out.println("===============================================");
//        System.out.println("DashScope Model：" + dashScopeChatModel.toString());
        System.out.println("Ollama Model：" + ollamaChatModel.toString());
        System.out.println("===============================================");

//        if ("dashscope".equals(platform)) {
//            return dashScopeChatModel.call(new Prompt(prompt))
//                    .getResult().getOutput().getText();
//        }

        if ("ollama".equals(platform)) {
            return ollamaChatModel.call(new Prompt(prompt))
                    .getResult().getOutput().getText();
        }

//        if ("openai".equals(platform)) {
//            return openAIChatModel.call(new Prompt(prompt))
//                    .getResult().getOutput().getText();
//        }
        return "Error ...";
    }
}
