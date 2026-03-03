package com.jhzhao.alibaba;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

/**
 * Hello world!
 *
 */

@SpringBootApplication
public class AnnotationClientApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(AnnotationClientApplication.class);
    }

    @Bean
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }

    @Bean
    public CommandLineRunner predefinedQuestions(ChatClient.Builder chatClientBuilder, ToolCallbackProvider tools,
                                                 ConfigurableApplicationContext context) {
        return args -> {
            // 构建ChatClient，注册可用的MCP工具
            var chatClient = chatClientBuilder
                    .defaultToolCallbacks(tools.getToolCallbacks())
                    .build();
            // 打印可用工具列表
            System.out.println("Available tools:");
            for (ToolCallback toolCallback : tools.getToolCallbacks()) {
                System.out.println(">>> " + toolCallback.getToolDefinition().name());
            }
            // 交互式聊天循环
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("\n>>> 问题: ");
                String userInput = scanner.nextLine();
                if (userInput.equalsIgnoreCase("exit")) {
                    break;
                }
                System.out.println("\n>>> 助手: " + chatClient.prompt(userInput).call().content());
            }
            scanner.close();
            context.close();
        };
    }
}
