package org.example.domain.prompt;

import org.example.infrastructure.openai.dto.ChatCompletionRequestDTO;

import java.util.List;

public interface CodeReviewPromptProvider {

    /**
     * 根据 diff 上下文构建模型提示词。
     *
     * @param diffCode diff 内容（可包含追加上下文）
     * @return 对话消息列表
     */
    List<ChatCompletionRequestDTO.Prompt> buildPrompts(String diffCode);

}
