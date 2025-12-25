package com.jhzhao.alibaba.springai.controller;

import com.jhzhao.alibaba.model.vo.ChatRequestVo;
import com.jhzhao.alibaba.result.ResultBody;
import com.jhzhao.alibaba.springai.interfaces.TextGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.validation.Valid;


@RestController
@RequestMapping("/text")
public class TextGenerationController {

    @Resource
    private TextGenerationService generationService;

    @GetMapping("/generate")
    @Operation(summary = "同步文本生成", description = "接收提示词并返回完整生成结果")
    public ResponseEntity<ResultBody<String>> generate(@RequestParam(value = "prompt", defaultValue = "请介绍Spring AI框架") String prompt) {
        try {
            String result = generationService.generateText(prompt);
            return ResponseEntity.ok(ResultBody.success(result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ResultBody.error("生成失败: " + e.getMessage()));
        }
    }

    @GetMapping(value = "/generate-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式文本生成", description = "以SSE方式返回实时生成结果")
    public Flux<String> generateStream(@RequestParam(value = "prompt", defaultValue = "请生成一篇关于AI发展趋势的短文") String prompt) {
        return generationService.generateTextStream(prompt).map(text -> "data: " + text + "\n\n")  // SSE格式封装
                .onErrorResume(e -> Flux.just("data: 生成过程出错: " + e.getMessage() + "\n\n"));
    }

    //普通同步调用大模型接口返回数据
    @PostMapping(value = "/chatAi")
    public ResultBody<String> chatAi(@RequestBody @Valid ChatRequestVo chatVo) {
        try {
            String result = generationService.generateText(chatVo.getMessage());
            return ResultBody.success(result);
        } catch (Exception e) {
            return ResultBody.error(500, "生成失败: " + e.getMessage());
        }

    }

    /**
     * 通过流式 处理大模型返回的结果给前端渲染
     *
     * @param chatVo vo 接收前端的参数
     * @return
     */
    @PostMapping(value = "/streamData", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamData(@RequestBody @Valid ChatRequestVo chatVo) {
        // 直接返回 Flux，Spring 会自动处理为 text/event-stream
        return generationService.generateTextStream(chatVo.getMessage());
    }

}

