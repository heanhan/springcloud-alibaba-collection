package com.jhzhao.alibaba.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/formate")
@RestController
public class JsonController {

    private final ChatClient chatClient;

    // 推荐直接注入 OllamaChatModel
    public JsonController(OllamaChatModel ollamaChatModel) {
        this.chatClient = ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultOptions(
                        OllamaChatOptions.builder()
                                .model("qwen3:8b")   // 或你 pull 的模型，如 llama3.2、qwen2.5、deepseek-r1 等
                                .temperature(0.3)      // JSON 模式建议温度低一些，更稳定
                                .build()
                )
                .build();
    }

    @GetMapping("/chatJson")
    public String simpleChatFormat(
            @RequestParam(value = "query", defaultValue = "请以JSON格式介绍你自己") String query) {
        // 核心：使用 Ollama 专属选项开启 JSON mode
        OllamaChatOptions jsonOptions = OllamaChatOptions.builder()
                .format("json")           // 关键！强制返回 JSON 字符串
                .temperature(0.1)         // 越低越严格（0~0.3 推荐）
                // 可选：如果你想更严格，可以结合 schema（Ollama 支持结构化输出）
                // .withFormat(jsonSchemaString)  // 传入 JSON Schema 字符串
                .build();

        // 更好的提示语（强烈建议加上，防止模型乱输出）
        String enhancedPrompt = """
                你是一个严格遵守 JSON 格式的助手。
                无论用户问什么，都只返回合法的 JSON，不要有任何多余文字、markdown 或解释。
                现在回答用户的问题：
                
                %s
                """.formatted(query);
        return chatClient.prompt(enhancedPrompt)
                .options(jsonOptions)           // 这里覆盖使用 JSON 选项
                .call()
                .content();
    }


}
