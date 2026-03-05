package com.jhzhao.alibaba.test;

import com.jhzhao.alibaba.memory.OpenClawLikeLongMemory;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Author zhaojh0912
 * Description TODO
 * CreateDate 2026/3/5 22:44
 * Version 1.0
 */

@SpringBootTest
public class SpringTest {
    @Autowired
    private OllamaChatModel chatModel;           // 通义千问 ChatModel

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private VectorStore vectorStore;       // 假设已配置 PGVector 或 Simple

    private static final String USER_ID = "testuser001";


    @Test
    void testLongMemoryFlow() throws Exception {
        OpenClawLikeLongMemory memory = new OpenClawLikeLongMemory(
                USER_ID,
                embeddingModel,
                vectorStore
        );

        String sessionId = "sess-" + System.currentTimeMillis();

        // 模拟第一天对话
        List<Message> conversation = new ArrayList<>();

        conversation.add(new UserMessage("我叫张三，喜欢喝拿铁，不喜欢甜食。"));
        memory.add(sessionId, List.of(conversation.get(conversation.size() - 1)));

        conversation.add(new AssistantMessage("好的，张三！记住了你喜欢拿铁，而且不爱甜食。"));
        memory.add(sessionId, List.of(conversation.get(conversation.size() - 1)));

        System.out.println("=== 第一天对话已添加 ===");

        // 模拟第二天（为了让日期变化更可靠，这里可以强制修改系统日期或加明显标记）
        // Thread.sleep(100) 不够可靠，实际生产中应使用不同日期的测试
        // 这里仅演示，假设第二天已到（或手动改系统日期测试）
        conversation.add(new UserMessage("明天我要去爬山，你觉得带什么好？"));
        memory.add(sessionId, List.of(conversation.get(conversation.size() - 1)));

        System.out.println("=== 第二天对话已添加 ===");

        // 获取当前记忆（应该包含最近日志 + MEMORY.md + 向量召回）
        List<Message> recalled = memory.get(sessionId);

        System.out.println("\n=== 当前召回的记忆内容（get() 返回） ===");
        System.out.println("共召回 " + recalled.size() + " 条消息");
        for (Message m : recalled) {
            String text = m.getText();
            String preview = text.length() > 120 ? text.substring(0, 120) + "..." : text;
            System.out.println("[" + m.getMessageType() + "] " + preview);
        }

        // 模拟总结并写入长期记忆
        String summary = """
                - 用户姓名：张三
                - 饮品偏好：喜欢拿铁，不喜欢甜食
                - 活动：计划明天爬山
                """.trim();

        memory.appendToLongTerm(summary);

        System.out.println("\n=== 已写入总结到 MEMORY.md ===");

        // 再次获取，应该看到 MEMORY.md 的内容被加入
        recalled = memory.get(sessionId);

        System.out.println("\n=== 写入总结后的召回内容 ===");
        System.out.println("共召回 " + recalled.size() + " 条消息");
        for (Message m : recalled) {
            String text = m.getText();
            String preview = text.length() > 120 ? text.substring(0, 120) + "..." : text;
            System.out.println("[" + m.getMessageType() + "] " + preview);
        }

        // 手动查看 MEMORY.md 文件内容（调试用）
        Path memoryFile = Path.of("./memory/user-" + USER_ID + "/MEMORY.md");
        if (Files.exists(memoryFile)) {
            System.out.println("\n=== MEMORY.md 实际文件内容 ===");
            System.out.println(Files.readString(memoryFile));
        } else {
            System.out.println("\nMEMORY.md 文件未找到");
        }

        // 可选：查看当天日志文件
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Path dailyFile = Path.of("./memory/user-" + USER_ID + "/daily/" + today + ".md");
        if (Files.exists(dailyFile)) {
            System.out.println("\n=== 当天日志文件内容（daily/" + today + ".md） ===");
            System.out.println(Files.readString(dailyFile));
        }
    }

}
