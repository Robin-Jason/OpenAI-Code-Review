package org.example.infrastructure.git;


import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.example.types.utils.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

public class GitCommand {

    private static final String LOG_BRANCH = "main";

    private final Logger logger = LoggerFactory.getLogger(GitCommand.class);

    private final String githubReviewLogUri;

    private final String githubToken;

    private final String project;

    private final String branch;

    private final String author;

    private final String message;

    /**
     * 构建 Git 操作封装。
     *
     * @param githubReviewLogUri 日志仓库地址
     * @param githubToken        访问令牌
     * @param project            项目名
     * @param branch             分支名
     * @param author             提交作者
     * @param message            提交信息
     */
    public GitCommand(String githubReviewLogUri, String githubToken, String project, String branch, String author, String message) {
        this.githubReviewLogUri = githubReviewLogUri;
        this.githubToken = githubToken;
        this.project = project;
        this.branch = branch;
        this.author = author;
        this.message = message;
    }

    /**
     * 获取最近一次提交的 diff。
     *
     * @return diff 字符串
     * @throws IOException          读取失败
     * @throws InterruptedException 进程被中断
     */
    public String diff() throws IOException, InterruptedException {
        // openai.itedus.cn
        ProcessBuilder logProcessBuilder = new ProcessBuilder("git", "log", "-1", "--pretty=format:%H");
        logProcessBuilder.directory(new File("."));
        Process logProcess = logProcessBuilder.start();

        BufferedReader logReader = new BufferedReader(new InputStreamReader(logProcess.getInputStream()));
        String latestCommitHash = logReader.readLine();
        logReader.close();
        logProcess.waitFor();

        ProcessBuilder diffProcessBuilder = new ProcessBuilder("git", "diff", latestCommitHash + "^", latestCommitHash);
        diffProcessBuilder.directory(new File("."));
        Process diffProcess = diffProcessBuilder.start();

        StringBuilder diffCode = new StringBuilder();
        BufferedReader diffReader = new BufferedReader(new InputStreamReader(diffProcess.getInputStream()));
        String line;
        Set<String> changedFiles = new LinkedHashSet<>();
        while ((line = diffReader.readLine()) != null) {
            diffCode.append(line).append("\n");
            if (line.startsWith("+++ b/")) {
                String path = line.substring(6).trim();
                if (!path.equals("/dev/null")) {
                    changedFiles.add(path);
                }
            }
        }
        diffReader.close();

        int exitCode = diffProcess.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to get diff, exit code:" + exitCode);
        }

        appendJavaContext(diffCode, latestCommitHash, changedFiles);
        return diffCode.toString();
    }

    /**
     * 将评审结果写入日志仓库并推送。
     *
     * @param recommend 评审内容
     * @return 日志文件 URL
     * @throws Exception 写入或推送失败
     */
    public String commitAndPush(String recommend) throws Exception {
        Git git = Git.cloneRepository()
                .setURI(githubReviewLogUri + ".git")
                .setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, ""))
                .call();

        boolean isHeadMissing = git.getRepository().resolve("HEAD") == null;
        boolean branchExists = git.getRepository().findRef(LOG_BRANCH) != null;
        if (isHeadMissing) {
            git.checkout()
                    .setOrphan(true)
                    .setName(LOG_BRANCH)
                    .call();
        } else if (branchExists) {
            git.checkout()
                    .setName(LOG_BRANCH)
                    .call();
        } else {
            git.checkout()
                    .setCreateBranch(true)
                    .setName(LOG_BRANCH)
                    .call();
        }

        // 创建分支
        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dateFolder = new File("repo/" + dateFolderName);
        if (!dateFolder.exists()) {
            dateFolder.mkdirs();
        }

        String fileName = project + "-" + branch + "-" + author + System.currentTimeMillis() + "-" + RandomStringUtils.randomNumeric(4) + ".md";
        File newFile = new File(dateFolder, fileName);
        try (FileWriter writer = new FileWriter(newFile)) {
            writer.write(recommend);
        }

        // 提交内容
        git.add().addFilepattern(dateFolderName + "/" + fileName).call();
        git.commit().setMessage("add code review new file" + fileName).call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, "")).add(LOG_BRANCH).call();

        logger.info("openai-code-review git commit and push done! {}", fileName);

        return githubReviewLogUri + "/blob/" + LOG_BRANCH + "/" + dateFolderName + "/" + fileName;
    }

    /**
     * 获取项目名。
     *
     * @return 项目名
     */
    public String getProject() {
        return project;
    }

    /**
     * 获取分支名。
     *
     * @return 分支名
     */
    public String getBranch() {
        return branch;
    }

    /**
     * 获取提交作者。
     *
     * @return 作者
     */
    public String getAuthor() {
        return author;
    }

    /**
     * 获取提交信息。
     *
     * @return 提交信息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 为变更的 Java 文件追加完整上下文，提升评审准确性。
     *
     * @param diffCode         diff 内容容器
     * @param latestCommitHash 最新提交 hash
     * @param changedFiles     变更文件路径集合
     */
    private void appendJavaContext(StringBuilder diffCode, String latestCommitHash, Set<String> changedFiles) {
        if (changedFiles.isEmpty()) {
            return;
        }
        diffCode.append("\n\n===== Java Context (full file) =====\n");
        for (String path : changedFiles) {
            if (!path.endsWith(".java")) {
                continue;
            }
            String content = readFileAtCommit(latestCommitHash, path);
            if (content == null || content.isEmpty()) {
                content = readFileAtCommit(latestCommitHash + "^", path);
            }
            if (content == null || content.isEmpty()) {
                continue;
            }
            diffCode.append("\n----- ").append(path).append(" -----\n");
            diffCode.append(content).append("\n");
        }
    }

    /**
     * 读取指定提交中的文件内容。
     *
     * @param commit 提交 hash
     * @param path   文件路径
     * @return 文件内容（读取失败返回空串）
     */
    private String readFileAtCommit(String commit, String path) {
        try {
            ProcessBuilder showProcessBuilder = new ProcessBuilder("git", "show", commit + ":" + path);
            showProcessBuilder.directory(new File("."));
            Process showProcess = showProcessBuilder.start();
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(showProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            int exitCode = showProcess.waitFor();
            if (exitCode != 0) {
                return "";
            }
            return content.toString();
        } catch (Exception e) {
            return "";
        }
    }

}
