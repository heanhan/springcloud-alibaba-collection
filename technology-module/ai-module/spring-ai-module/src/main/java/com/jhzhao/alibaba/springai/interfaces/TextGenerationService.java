package com.jhzhao.alibaba.springai.interfaces;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class TextGenerationService {

    @Resource
    private ChatModel chatModel;

//    public TextGenerationService(ChatModel chatModel) {
//        this.chatModel = chatModel;
//    }

    /**
     * 同步文本生成
     */
    public String generateText(String prompt) {
        Prompt request = new Prompt(
                new UserMessage(prompt),
                ChatOptions.builder()
                        .temperature(0.8d)
                        .topP(0.9d)
                        .build()
        );
        ChatResponse response = chatModel.call(request);
        return response.getResult().getOutput().getText();
    }

    /**
     * 流式文本生成
     */
    public Flux<String> generateTextStream(String prompt) {
        Prompt request = new Prompt(new UserMessage(prompt));
        Flux<String> resultText = chatModel.stream(request)
                .flatMapSequential(chatResponse -> {
                    AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
                    String content = assistantMessage.getText();  // 这里应该能调用
                    return content != null && !content.isEmpty() ? Flux.just(content) : Flux.empty();
                });
        return resultText;
    }

}