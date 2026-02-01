package org.example;

import org.example.domain.model.Model;
import org.example.infrastructure.openai.dto.ChatCompletionRequestDTO;
import org.example.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;
import org.example.infrastructure.openai.impl.ChatGLM;
import org.example.types.utils.WXAccessTokenUtils;
import org.junit.Assume;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ExternalRequestsTest {

    private static final String CONFIG_PATH = "application.yml";

    private static Map<String, Object> loadConfig() {
        try (InputStream in = ExternalRequestsTest.class.getClassLoader().getResourceAsStream(CONFIG_PATH)) {
            if (in == null) {
                return null;
            }
            Yaml yaml = new Yaml();
            Object data = yaml.load(in);
            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data;
                return map;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String getConfig(String key) {
        String value = System.getenv(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        Map<String, Object> config = loadConfig();
        if (config == null) {
            return null;
        }
        Object fromFile = config.get(key);
        return fromFile == null ? null : String.valueOf(fromFile);
    }

    private static void requireExternalTestsEnabled() {
        String enabled = System.getenv("RUN_EXTERNAL_TESTS");
        Assume.assumeTrue("External tests disabled. Set RUN_EXTERNAL_TESTS=true to run.",
                enabled != null && enabled.equalsIgnoreCase("true"));
    }

    @Test
    public void testChatGlmCompletion() throws Exception {
        requireExternalTestsEnabled();
        String apiHost = getConfig("CHATGLM_APIHOST");
        String apiKeySecret = getConfig("CHATGLM_APIKEYSECRET");
        assertNotNull("CHATGLM_APIHOST is required", apiHost);
        assertNotNull("CHATGLM_APIKEYSECRET is required", apiKeySecret);

        ChatGLM client = new ChatGLM(apiHost, apiKeySecret);
        ChatCompletionRequestDTO request = new ChatCompletionRequestDTO();
        request.setModel(Model.GLM_4_FLASH.getCode());
        request.setMessages(new ArrayList<ChatCompletionRequestDTO.Prompt>() {{
            add(new ChatCompletionRequestDTO.Prompt("user", "Say hi in 3 words"));
        }});

        ChatCompletionSyncResponseDTO response = client.completions(request);
        assertNotNull("Response should not be null", response);
        assertNotNull("Choices should not be null", response.getChoices());
        assertTrue("Choices should not be empty", response.getChoices().size() > 0);
        assertNotNull("Message should not be null", response.getChoices().get(0).getMessage());
        assertNotNull("Content should not be null", response.getChoices().get(0).getMessage().getContent());
    }

    @Test
    public void testGitLogRepoAccess() throws Exception {
        requireExternalTestsEnabled();
        String uri = getConfig("GITHUB_REVIEW_LOG_URI");
        String token = getConfig("GITHUB_TOKEN");
        assertNotNull("GITHUB_REVIEW_LOG_URI is required", uri);
        assertNotNull("GITHUB_TOKEN is required", token);

        org.eclipse.jgit.api.Git.cloneRepository()
                .setURI(uri.endsWith(".git") ? uri : uri + ".git")
                .setDirectory(java.nio.file.Files.createTempDirectory("review-log-").toFile())
                .setCredentialsProvider(new org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider(token, ""))
                .setCloneAllBranches(false)
                .call()
                .close();
    }

    @Test
    public void testWeChatAccessToken() {
        requireExternalTestsEnabled();
        String appid = getConfig("WEIXIN_APPID");
        String secret = getConfig("WEIXIN_SECRET");
        assertNotNull("WEIXIN_APPID is required", appid);
        assertNotNull("WEIXIN_SECRET is required", secret);

        String token = WXAccessTokenUtils.getAccessToken(appid, secret);
        assertNotNull("Access token should not be null", token);
        assertTrue("Access token should not be empty", token.length() > 0);
    }
}
