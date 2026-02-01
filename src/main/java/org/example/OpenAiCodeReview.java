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
    private String weixin_appid;
    private String weixin_secret;
    private String weixin_touser;
    private String weixin_template_id;

    // ChatGLM 配置
    private String chatglm_apiHost;
    private String chatglm_apiKeySecret;

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
        String project = firstNonEmpty(getConfigOptional("COMMIT_PROJECT"), getGitProject());
        String branch = firstNonEmpty(getConfigOptional("COMMIT_BRANCH"), getGitBranch());
        String author = firstNonEmpty(getConfigOptional("COMMIT_AUTHOR"), getGitAuthor());
        String message = firstNonEmpty(getConfigOptional("COMMIT_MESSAGE"), getGitMessage());

        GitCommand gitCommand = new GitCommand(
                getConfig("GITHUB_REVIEW_LOG_URI"),
                getConfig("GITHUB_TOKEN"),
                project,
                branch,
                author,
                message
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

    private static String getConfigOptional(String key) {
        String value = System.getenv(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        Object fromFile = CONFIG.get(key);
        if (fromFile == null) {
            return null;
        }
        return String.valueOf(fromFile);
    }

    private static String firstNonEmpty(String first, String second) {
        if (first != null && !first.isEmpty()) {
            return first;
        }
        return second;
    }

    private static String getGitProject() {
        String top = runGit("rev-parse", "--show-toplevel");
        if (top == null || top.isEmpty()) {
            return "";
        }
        int idx = top.replace("\\", "/").lastIndexOf('/');
        return idx >= 0 ? top.substring(idx + 1) : top;
    }

    private static String getGitBranch() {
        return runGit("rev-parse", "--abbrev-ref", "HEAD");
    }

    private static String getGitAuthor() {
        return runGit("log", "-1", "--pretty=format:%an");
    }

    private static String getGitMessage() {
        return runGit("log", "-1", "--pretty=format:%s");
    }

    private static String runGit(String... args) {
        try {
            String[] cmd = new String[args.length + 1];
            cmd[0] = "git";
            System.arraycopy(args, 0, cmd, 1, args.length);
            Process process = new ProcessBuilder(cmd).start();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                process.waitFor();
                return line == null ? "" : line.trim();
            }
        } catch (Exception e) {
            return "";
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
