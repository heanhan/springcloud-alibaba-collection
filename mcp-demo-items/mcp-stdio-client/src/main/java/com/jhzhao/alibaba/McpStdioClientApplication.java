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
public class McpStdioClientApplication
{
    public static void main(String[] args) {
        SpringApplication.run(McpStdioClientApplication.class, args);
    }

    @Bean
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }

    @Bean
    public CommandLineRunner predefinedQuestions(ChatClient.Builder chatClientBuilder, ToolCallbackProvider tools,
                                                 ConfigurableApplicationContext context) {
        // 打印可用工具
        ToolCallback[] toolCallbacks = tools.getToolCallbacks();
        System.out.println("可用工具:");
        for (ToolCallback toolCallback : toolCallbacks) {
            System.out.println(">>> " + toolCallback.getToolDefinition().name());
        }

        return args -> {
            // 创建带工具支持的聊天客户端
            var chatClient = chatClientBuilder
                    .defaultToolCallbacks(tools.getToolCallbacks())
                    .build();

            // 创建交互式控制台
            Scanner scanner = new Scanner(System.in);
            System.out.println("\n欢迎使用AI天气助手！输入'exit'退出程序。");

            while (true) {
                System.out.print("\n>>> 你的问题: ");
                String userInput = scanner.nextLine();
                if (userInput.equalsIgnoreCase("exit")) {
                    break;
                }

                System.out.print(">>> AI助手: ");
                // 调用AI并获取响应
                String response = chatClient.prompt(userInput).call().content();
                System.out.println(response);
            }

            scanner.close();
            context.close();
            System.out.println("\n程序已退出。");
        };
    }
}
