package com.jhzhao.alibaba.springai.controller;

import com.jhzhao.alibaba.result.ResultBody;
import com.jhzhao.alibaba.springai.interfaces.TextGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/text")
public class TextGenerationController {

    @Resource
    private TextGenerationService generationService;

    public TextGenerationController(TextGenerationService generationService) {
        this.generationService = generationService;
    }

    @GetMapping("/generate")
    @Operation(summary = "同步文本生成", description = "接收提示词并返回完整生成结果")
    public ResponseEntity<ResultBody<String>> generate(
            @RequestParam(value = "prompt", defaultValue = "请介绍Spring AI框架") String prompt) {
        try {
            String result = generationService.generateText(prompt);
            return ResponseEntity.ok(ResultBody.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ResultBody.error("生成失败: " + e.getMessage()));
        }
    }

    @GetMapping(value = "/generate-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式文本生成", description = "以SSE方式返回实时生成结果")
    public Flux<String> generateStream(
            @RequestParam(value = "prompt", defaultValue = "请生成一篇关于AI发展趋势的短文") String prompt) {
        return generationService.generateTextStream(prompt)
                .map(text -> "data: " + text + "\n\n")  // SSE格式封装
                .onErrorResume(e -> Flux.just("data: 生成过程出错: " + e.getMessage() + "\n\n"));
    }
}

