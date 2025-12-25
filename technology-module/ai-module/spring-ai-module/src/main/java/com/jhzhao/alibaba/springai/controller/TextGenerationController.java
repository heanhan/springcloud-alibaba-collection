package com.jhzhao.alibaba.springai.controller;

import com.jhzhao.alibaba.model.vo.ChatRequestVo;
import com.jhzhao.alibaba.result.ResultBody;
import com.jhzhao.alibaba.springai.interfaces.TextGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import javax.validation.Valid;
import java.io.IOException;
import java.util.concurrent.Executors;


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
    public SseEmitter streamData(@RequestBody @Valid ChatRequestVo chatVo) {
        SseEmitter seEmitter = new SseEmitter(Long.MAX_VALUE);//构建一个 SseEmitter对象，并且设置超时参数为不限
        //使用线程异步处理，避免阻塞主线程
        Executors.newCachedThreadPool().execute(() -> {
            //线程内部实现调用
            try {
                simulateLlmStream(chatVo.getMessage(), seEmitter);
            } catch (Exception e) {
                seEmitter.completeWithError(e);
            }
        });
        return seEmitter;
    }

    //实际调用大模型的方法
    private void simulateLlmStream(@NotNull(message = "会话内容不能为空") String prompt, SseEmitter seEmitter) {
        // 模拟流式数据
        Flux<String> textFlux = generationService.generateTextStream(prompt);
        textFlux.subscribe(content -> {
            try {
                //发送纯文本模块（sse模块默认格式为： data:内容\n\n）
                seEmitter.send(content);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, error -> {
            // 出错时完成并传递错误
            seEmitter.completeWithError(error);
        }, () -> {
            // 流结束时完成连接
            seEmitter.complete();
        });
        //测试的方法
        seEmitter.onCompletion(()-> System.out.println("SSE completed")); // 结束流
        seEmitter.onTimeout(() -> seEmitter.complete());
        seEmitter.onError(e -> System.out.println("SSE error: " + e.getMessage()));
    }
}

