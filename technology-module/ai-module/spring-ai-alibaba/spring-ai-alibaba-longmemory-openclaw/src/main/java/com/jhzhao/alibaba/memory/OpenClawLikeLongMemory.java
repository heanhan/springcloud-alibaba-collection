package com.jhzhao.alibaba.memory;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模仿 OpenClaw 风格的长记忆实现
 * - Markdown 文件作为真相来源（MEMORY.md + daily/*.md）
 * - 向量检索作为辅助召回
 * - 最近 1~2 天原始日志会尽量被加载
 * - 支持人类手动编辑 MEMORY.md 后重建向量索引
 */
public class OpenClawLikeLongMemory implements ChatMemory, InitializingBean {

    private final Path baseDir;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final Path memoryMdPath;           // 长期核心事实文件

    public OpenClawLikeLongMemory(String userId,
                                  EmbeddingModel embeddingModel,
                                  VectorStore vectorStore) {
        this.baseDir = Path.of("./memory/user-" + userId).toAbsolutePath();
        this.memoryMdPath = baseDir.resolve("MEMORY.md");
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;

        try {
            Files.createDirectories(baseDir);
            Files.createDirectories(baseDir.resolve("daily"));
            if (!Files.exists(memoryMdPath)) {
                Files.writeString(memoryMdPath,
                        "# Long-term Memory Facts\n\n" +
                                "(由 Agent 或用户手动/自动维护的关键事实、偏好、重要结论)\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("无法初始化记忆目录", e);
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        List<Message> context = new ArrayList<>();

        // 1. 尝试加载最近 2 天的原始对话日志（模仿 OpenClaw 读取 recent raw log）
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 3; i++) {  // 今天 + 昨天 + 前天
            LocalDate date = today.minusDays(i);
            Path logPath = baseDir.resolve("daily/" + date.format(dateFmt) + ".md");
            if (Files.exists(logPath)) {
                try {
                    String content = Files.readString(logPath);
                    String preview = content.length() > 1800
                            ? content.substring(0, 1800) + "\n...（日志较长，请查看完整文件）"
                            : content;
                    context.add(new SystemMessage(
                            "最近原始对话日志 (" + date + ")：\n" + preview));
                } catch (IOException ignored) {
                }
            }
        }

        // 2. 读取 MEMORY.md 作为核心长期事实（总是全量加入）
        String longTermContent;
        try {
            longTermContent = Files.readString(memoryMdPath);
            if (!longTermContent.trim().isEmpty()) {
                context.add(new SystemMessage(
                        "核心长期记忆（MEMORY.md） - 请优先参考：\n" + longTermContent));
            }
        } catch (IOException e) {
            longTermContent = "";
        }

        // 3. 向量语义召回（补充可能遗漏的重要片段）
        SearchRequest request = SearchRequest.builder()
                .query("user important facts preferences decisions events key information")
                .topK(6)
                .similarityThreshold(0.62)
                .filterExpression("sessionId == '" + conversationId + "'")   // 注意：字符串要单引号包裹
                .build();

        List<Document> relevant = vectorStore.similaritySearch(request);

        relevant.stream()
                .map(doc -> {
                    String text = doc.getFormattedContent(MetadataMode.NONE);
                    String preview = text.length() > 600
                            ? text.substring(0, 600) + "..."
                            : text;
                    String dateStr = String.valueOf(doc.getMetadata().getOrDefault("date", "未知日期"));
                    return new SystemMessage("记忆片段 (" + dateStr + ")：\n" + preview);
                })
                .forEach(context::add);

        return context;
    }

    @Override
    public void add(String sessionId, List<Message> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }

        String today = LocalDate.now().format(dateFmt);
        Path dailyPath = baseDir.resolve("daily/" + today + ".md");

        try {
            // 写入当天 Markdown 日志
            StringBuilder sb = new StringBuilder();
            sb.append("\n## ").append(Instant.now()).append("\n\n");

            for (Message msg : messages) {
                String role = switch (msg.getMessageType()) {
                    case USER -> "**User**: ";
                    case ASSISTANT -> "**Assistant**: ";
                    case SYSTEM -> "**System**: ";
                    default -> "**Other**: ";
                };
                sb.append(role).append(msg.getText().replace("\n", "  \n")).append("\n\n");
            }

            Files.writeString(dailyPath, sb.toString(),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);

            // 向量化重要消息（User + Assistant）
            List<Document> toIndex = new ArrayList<>();
            for (Message msg : messages) {
                if (msg.getMessageType() == org.springframework.ai.chat.messages.MessageType.USER ||
                        msg.getMessageType() == org.springframework.ai.chat.messages.MessageType.ASSISTANT) {
                    String text = msg.getText();
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("sessionId", sessionId);
                    meta.put("date", today);
                    meta.put("role", msg.getMessageType().name());
                    meta.put("source", "daily/" + today + ".md");
                    toIndex.add(new Document(text, meta));
                }
            }

            if (!toIndex.isEmpty()) {
                vectorStore.add(toIndex);
            }

        } catch (IOException e) {
            // 生产环境建议改成日志记录
            throw new RuntimeException("写入当天日志失败", e);
        }
    }

    @Override
    public void clear(String sessionId) {
        // 故意不实现清空，模仿 OpenClaw 永久积累风格
        // 如需清理，可手动删除文件或调用 rebuildVectorFromFiles()
    }

    /**
     * 供外部调用：总结后写入长期记忆文件
     */
    public void appendToLongTerm(String summaryContent) {
        try {
            String entry = "\n## " + LocalDate.now() + " 总结\n" +
                    summaryContent.trim() + "\n";

            Files.writeString(memoryMdPath, entry,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);

            // 同步向量化
            Map<String, Object> meta = Map.of(
                    "type", "long-term-summary",
                    "date", LocalDate.now().toString()
            );
            vectorStore.add(List.of(new Document(summaryContent, meta)));

        } catch (IOException e) {
            throw new RuntimeException("追加到 MEMORY.md 失败", e);
        }
    }

    /**
     * 人类编辑 MEMORY.md 或 daily 文件后，可调用此方法重建向量索引
     */
    public void rebuildVectorFromFiles() throws IOException {
        // 建议先清空旧索引（视 vector store 是否支持批量删除）
        // vectorStore.delete(...)  // 根据具体实现可能需要 filter 或全部清空

        // 索引 MEMORY.md
        String ltContent = Files.readString(memoryMdPath);
        if (!ltContent.trim().isEmpty()) {
            vectorStore.add(List.of(
                    new Document(ltContent, Map.of("type", "MEMORY.md", "source", "MEMORY.md"))
            ));
        }

        // 索引所有 daily/*.md（可加时间范围限制）
        Files.list(baseDir.resolve("daily"))
                .filter(p -> p.toString().endsWith(".md"))
                .forEach(p -> {
                    try {
                        String content = Files.readString(p);
                        String filename = p.getFileName().toString();
                        String dateStr = filename.replace(".md", "");
                        vectorStore.add(List.of(
                                new Document(content, Map.of(
                                        "source", "daily/" + filename,
                                        "date", dateStr,
                                        "type", "daily-log"
                                ))
                        ));
                    } catch (Exception ignored) {
                    }
                });
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("OpenClaw-like 长记忆初始化完成： " + baseDir);
    }
}
