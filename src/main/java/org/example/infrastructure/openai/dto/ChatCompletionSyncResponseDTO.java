package org.example.infrastructure.openai.dto;

import java.util.List;

public class ChatCompletionSyncResponseDTO {

    private List<Choice> choices;

    public static class Choice {
        private Message message;

        /**
         * 获取消息对象。
         *
         * @return 消息对象
         */
        public Message getMessage() {
            return message;
        }

        /**
         * 设置消息对象。
         *
         * @param message 消息对象
         */
        public void setMessage(Message message) {
            this.message = message;
        }
    }

    public static class Message {
        private String role;
        private String content;

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
     * 获取候选结果列表。
     *
     * @return choices 列表
     */
    public List<Choice> getChoices() {
        return choices;
    }

    /**
     * 设置候选结果列表。
     *
     * @param choices choices 列表
     */
    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }
}

