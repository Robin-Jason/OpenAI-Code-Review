package org.example.domain.service;

import org.example.infrastructure.git.GitCommand;
import org.example.infrastructure.openai.IOpenAI;
import org.example.infrastructure.wechat.Wechat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public abstract class AbstractOpenAiCodeReviewService implements IOpenAiCodeReviewService {

    private final Logger logger = LoggerFactory.getLogger(AbstractOpenAiCodeReviewService.class);

    protected final GitCommand gitCommand;
    protected final IOpenAI openAI;
    protected final Wechat wechat;

    /**
     * 构建代码评审服务抽象基类。
     *
     * @param gitCommand Git 操作封装
     * @param openAI     大模型客户端
     * @param weiXin     微信通知客户端
     */
    public AbstractOpenAiCodeReviewService(GitCommand gitCommand, IOpenAI openAI, Wechat wechat) {
        this.gitCommand = gitCommand;
        this.openAI = openAI;
        this.wechat = wechat;
    }

    /**
     * 执行评审主流程：获取 diff、评审、记录、通知。
     */
    @Override
    public void exec() {
        try {
            // 1. 获取提交代码
            String diffCode = getDiffCode();
            // 2. 开始评审代码
            String recommend = codeReview(diffCode);
            // 3. 记录评审结果；返回日志地址
            String logUrl = recordCodeReview(recommend);
            // 4. 发送消息通知；日志地址、通知的内容
            pushMessage(logUrl);
        } catch (Exception e) {
            logger.error("openai-code-review error", e);
        }

    }

    /**
     * 获取本次提交的 diff 内容。
     *
     * @return diff 字符串
     * @throws IOException          读取失败
     * @throws InterruptedException 进程被中断
     */
    protected abstract String getDiffCode() throws IOException, InterruptedException;

    /**
     * 调用大模型进行评审。
     *
     * @param diffCode diff 字符串
     * @return 评审结果
     * @throws Exception 调用失败
     */
    protected abstract String codeReview(String diffCode) throws Exception;

    /**
     * 记录评审结果并返回日志地址。
     *
     * @param recommend 评审结果
     * @return 日志地址
     * @throws Exception 记录失败
     */
    protected abstract String recordCodeReview(String recommend) throws Exception;

    /**
     * 发送通知消息。
     *
     * @param logUrl 日志地址
     * @throws Exception 通知失败
     */
    protected abstract void pushMessage(String logUrl) throws Exception;

}
