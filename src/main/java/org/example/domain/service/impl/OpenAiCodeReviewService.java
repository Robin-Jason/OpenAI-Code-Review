package org.example.domain.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.domain.model.Model;
import org.example.domain.prompt.CodeReviewPromptProvider;
import org.example.domain.prompt.impl.DefaultCodeReviewPromptProvider;
import org.example.domain.service.AbstractOpenAiCodeReviewService;
import org.example.infrastructure.git.GitCommand;
import org.example.infrastructure.openai.IOpenAI;
import org.example.infrastructure.openai.dto.ChatCompletionRequestDTO;
import org.example.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;
import org.example.infrastructure.wechat.TemplateMessageDTO;
import org.example.infrastructure.wechat.Wechat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OpenAiCodeReviewService extends AbstractOpenAiCodeReviewService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiCodeReviewService.class);
    private final CodeReviewPromptProvider promptProvider;

    /**
     * 构建评审服务实现。
     *
     * @param gitCommand Git 操作封装
     * @param openAI     大模型客户端
     * @param weiXin     微信通知客户端
     */
    public OpenAiCodeReviewService(GitCommand gitCommand, IOpenAI openAI, Wechat wechat) {
        this(gitCommand, openAI, wechat, new DefaultCodeReviewPromptProvider());
    }

    /**
     * 构建评审服务实现（支持自定义提示词提供器）。
     *
     * @param gitCommand     Git 操作封装
     * @param openAI         大模型客户端
     * @param wechat         微信通知客户端
     * @param promptProvider 提示词提供器
     */
    public OpenAiCodeReviewService(
            GitCommand gitCommand,
            IOpenAI openAI,
            Wechat wechat,
            CodeReviewPromptProvider promptProvider
    ) {
        super(gitCommand, openAI, wechat);
        this.promptProvider = promptProvider;
    }

    /**
     * 获取 Git diff 内容。
     *
     * @return diff 字符串
     */
    @Override
    protected String getDiffCode() throws IOException, InterruptedException {
        logger.info("[trace] starting diff");
        String diff = gitCommand.diff();
        logger.info("[trace] diff done, length={}", diff == null ? 0 : diff.length());
        return diff;
    }

    /**
     * 拼接提示词并调用模型生成评审内容。
     *
     * @param diffCode diff 字符串
     * @return 评审内容
     */
    @Override
    protected String codeReview(String diffCode) throws Exception {
        logger.info("[trace] starting llm call");
        ChatCompletionRequestDTO chatCompletionRequest = new ChatCompletionRequestDTO();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        chatCompletionRequest.setMessages(promptProvider.buildPrompts(diffCode));

        ChatCompletionSyncResponseDTO completions = openAI.completions(chatCompletionRequest);
        logger.info("[trace] llm call done");
        ChatCompletionSyncResponseDTO.Message message = completions.getChoices().get(0).getMessage();
        return message.getContent();
    }

    /**
     * 将评审结果写入日志仓库并推送。
     *
     * @param recommend 评审内容
     * @return 日志地址
     */
    @Override
    protected String recordCodeReview(String recommend) throws Exception {
        logger.info("[trace] starting git commit/push");
        String url = gitCommand.commitAndPush(recommend);
        logger.info("[trace] git commit/push done");
        return url;
    }

    /**
     * 发送微信模板消息通知。
     *
     * @param logUrl 日志地址
     */
    @Override
    protected void pushMessage(String logUrl) throws Exception {
        logger.info("[trace] starting wechat send");
        Map<String, Map<String, String>> data = new HashMap<>();
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.REPO_NAME, gitCommand.getProject());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.BRANCH_NAME, gitCommand.getBranch());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_AUTHOR, gitCommand.getAuthor());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_MESSAGE, gitCommand.getMessage());
        wechat.sendTemplateMessage(logUrl, data);
        logger.info("[trace] wechat send done");
    }

}
