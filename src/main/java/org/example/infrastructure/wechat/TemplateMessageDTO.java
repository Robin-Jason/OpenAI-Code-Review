package org.example.infrastructure.wechat;

import java.util.HashMap;
import java.util.Map;

public class TemplateMessageDTO {

    private String touser = "or0Ab6ivwmypESVp_bYuk92T6SvU";
    private String template_id = "GLlAM-Q4jdgsktdNd35hnEbHVam2mwsW2YWuxDhpQkU";
    private String url = "https://weixin.qq.com";
    private Map<String, Map<String, String>> data = new HashMap<>();

    /**
     * 构建模板消息 DTO。
     *
     * @param touser      接收者 openid
     * @param template_id 模板 id
     */
    public TemplateMessageDTO(String touser, String template_id) {
        this.touser = touser;
        this.template_id = template_id;
    }

    /**
     * 追加模板字段值。
     *
     * @param key   字段 key
     * @param value 字段 value
     */
    public void put(String key, String value) {
        data.put(key, new HashMap<String, String>() {
            private static final long serialVersionUID = 7092338402387318563L;

            {
                put("value", value);
            }
        });
    }

    /**
     * 追加模板字段值（静态方法）。
     *
     * @param data  数据容器
     * @param key   模板字段枚举
     * @param value 字段值
     */
    public static void put(Map<String, Map<String, String>> data, TemplateKey key, String value){
        data.put(key.getCode(), new HashMap<String, String>() {
            private static final long serialVersionUID = 7092338402387318563L;

            {
                put("value", value);
            }
        });
    }

    public enum TemplateKey {
        REPO_NAME("repo_name","项目名称"),
        BRANCH_NAME("branch_name","分支名称"),
        COMMIT_AUTHOR("commit_author","提交者"),
        COMMIT_MESSAGE("commit_message","提交信息"),
        ;

        private String code;
        private String desc;

        /**
         * 构建模板字段枚举。
         *
         * @param code 字段编码
         * @param desc 字段描述
         */
        TemplateKey(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        /**
         * 获取字段编码。
         *
         * @return 字段编码
         */
        public String getCode() {
            return code;
        }

        /**
         * 获取字段描述。
         *
         * @return 字段描述
         */
        public String getDesc() {
            return desc;
        }
    }

    /**
     * 获取接收者 openid。
     *
     * @return 接收者 openid
     */
    public String getTouser() {
        return touser;
    }

    /**
     * 设置接收者 openid。
     *
     * @param touser 接收者 openid
     */
    public void setTouser(String touser) {
        this.touser = touser;
    }

    /**
     * 获取模板 id。
     *
     * @return 模板 id
     */
    public String getTemplate_id() {
        return template_id;
    }

    /**
     * 设置模板 id。
     *
     * @param template_id 模板 id
     */
    public void setTemplate_id(String template_id) {
        this.template_id = template_id;
    }

    /**
     * 获取跳转 URL。
     *
     * @return URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置跳转 URL。
     *
     * @param url URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 获取模板数据。
     *
     * @return 数据
     */
    public Map<String, Map<String, String>> getData() {
        return data;
    }

    /**
     * 设置模板数据。
     *
     * @param data 数据
     */
    public void setData(Map<String, Map<String, String>> data) {
        this.data = data;
    }

}
