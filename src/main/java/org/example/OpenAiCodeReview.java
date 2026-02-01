package org.example;

import org.example.domain.service.impl.OpenAiCodeReviewService;
import org.example.infrastructure.git.GitCommand;
import org.example.infrastructure.openai.IOpenAI;
import org.example.infrastructure.openai.impl.ChatGLM;
import org.example.infrastructure.wechat.Wechat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class OpenAiCodeReview {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiCodeReview.class);

    private static final String CONFIG_PATH = "application.yml";
    private static final Map<String, Object> CONFIG = loadConfig();

    // 配置配置
    private String weixin_appid = "wx5a228ff69e28a91f";
    private String weixin_secret = "0bea03aa1310bac050aae79dd8703928";
    private String weixin_touser = "or0Ab6ivwmypESVp_bYuk92T6SvU";
    private String weixin_template_id = "l2HTkntHB71R4NQTW77UkcqvSOIFqE_bss1DAVQSybc";

    // ChatGLM 配置
    private String chatglm_apiHost = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private String chatglm_apiKeySecret = "";

    // Github 配置
    private String github_review_log_uri;
    private String github_token;

    // 工程配置 - 自动获取
    private String github_project;
    private String github_branch;
    private String github_author;

    /**
     * 程序入口：组装依赖并触发代码评审流程。
     *
     * @param args 命令行参数
     * @throws Exception 评审流程中的异常
     */
    public static void main(String[] args) throws Exception {
        GitCommand gitCommand = new GitCommand(
                getConfig("GITHUB_REVIEW_LOG_URI"),
                getConfig("GITHUB_TOKEN"),
                getConfig("COMMIT_PROJECT"),
                getConfig("COMMIT_BRANCH"),
                getConfig("COMMIT_AUTHOR"),
                getConfig("COMMIT_MESSAGE")
        );

        /**
         * 项目：{{repo_name.DATA}} 分支：{{branch_name.DATA}} 作者：{{commit_author.DATA}} 说明：{{commit_message.DATA}}
         */
        Wechat wechat = new Wechat(
                getConfig("WEIXIN_APPID"),
                getConfig("WEIXIN_SECRET"),
                getConfig("WEIXIN_TOUSER"),
                getConfig("WEIXIN_TEMPLATE_ID")
        );

        IOpenAI openAI = new ChatGLM(getConfig("CHATGLM_APIHOST"), getConfig("CHATGLM_APIKEYSECRET"));

        OpenAiCodeReviewService openAiCodeReviewService = new OpenAiCodeReviewService(gitCommand, openAI, wechat);
        openAiCodeReviewService.exec();

        logger.info("openai-code-review done!");
    }

    private static Map<String, Object> loadConfig() {
        try (InputStream in = OpenAiCodeReview.class.getClassLoader().getResourceAsStream(CONFIG_PATH)) {
            if (in == null) {
                throw new RuntimeException("application.yml not found in classpath");
            }
            Yaml yaml = new Yaml();
            Object data = yaml.load(in);
            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data;
                return map;
            }
            throw new RuntimeException("application.yml content is invalid");
        } catch (Exception e) {
            throw new RuntimeException("failed to load application.yml", e);
        }
    }

    /**
     * 优先读取环境变量，其次读取 application.yml。
     *
     * @param key 配置名
     * @return 配置值
     */
    private static String getConfig(String key) {
        String value = System.getenv(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        Object fromFile = CONFIG.get(key);
        if (fromFile == null) {
            throw new RuntimeException("value is null");
        }
        return String.valueOf(fromFile);
    }

}
