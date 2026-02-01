package org.example.infrastructure.openai;

import org.example.infrastructure.openai.dto.ChatCompletionRequestDTO;
import org.example.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;

public interface IOpenAI {

    /**
     * 发起对话补全请求。
     *
     * @param requestDTO 请求参数
     * @return 同步返回结果
     * @throws Exception 调用失败
     */
    ChatCompletionSyncResponseDTO completions(ChatCompletionRequestDTO requestDTO) throws Exception;

}
