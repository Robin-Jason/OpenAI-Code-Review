package org.example.infrastructure.openai.dto;

import org.example.domain.model.Model;

import java.util.List;

public class ChatCompletionRequestDTO {

    private String model = Model.GLM_4_FLASH.getCode();
    private List<Prompt> messages;

    public static class Prompt {
        private String role;
        private String content;

        /**
         * 空构造。
         */
        public Prompt() {
        }

        /**
         * 构造提示词。
         *
         * @param role    角色（user/assistant/system）
         * @param content 内容
         */
        public Prompt(String role, String content) {
            this.role = role;
            this.content = content;
        }

        /**
         * 获取角色。
         *
         * @return 角色
         */
        public String getRole() {
            return role;
        }

        /**
         * 设置角色。
         *
         * @param role 角色
         */
        public void setRole(String role) {
            this.role = role;
        }

        /**
         * 获取内容。
         *
         * @return 内容
         */
        public String getContent() {
            return content;
        }

        /**
         * 设置内容。
         *
         * @param content 内容
         */
        public void setContent(String content) {
            this.content = content;
        }

    }

    /**
     * 获取模型编码。
     *
     * @return 模型编码
     */
    public String getModel() {
        return model;
    }

    /**
     * 设置模型编码。
     *
     * @param model 模型编码
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * 获取消息列表。
     *
     * @return 消息列表
     */
    public List<Prompt> getMessages() {
        return messages;
    }

    /**
     * 设置消息列表。
     *
     * @param messages 消息列表
     */
    public void setMessages(List<Prompt> messages) {
        this.messages = messages;
    }
}
