package com.ark.example;

/*
 * ====== Ark SDK 相关导入 ======
 * 用于调用火山引擎 Ark 大模型接口（Responses API）
 */
import com.volcengine.ark.runtime.model.responses.item.ItemOutputMessage;
import com.volcengine.ark.runtime.service.ArkService;
import com.volcengine.ark.runtime.model.responses.request.*;
import com.volcengine.ark.runtime.model.responses.response.ResponseObject;
import com.volcengine.ark.runtime.model.responses.content.OutputContentItem;
import com.volcengine.ark.runtime.model.responses.content.OutputContentItemText;
import com.volcengine.ark.runtime.model.responses.item.BaseItem;

/*
 * ====== Java 标准库导入 ======
 */
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * TestConnect
 * ------------------------------------
 * 该类用于：
 * 1. 读取本地 Prompt 文件（公共 Prompt / 私有 Prompt / 象棋 Prompt）
 * 2. 通过 ArkService 调用大模型接口
 * 3. 向模型发送文本并解析模型返回内容
 * 4. 在网络不可用时返回本地随机回复
 */
public class TestConnect {

    /** 公共 Prompt（用于统一约束模型行为） */
    protected String pub;

    /** 私有 Prompt（每个机器人独有设定） */
    protected String pri;

    /** 象棋 Prompt（用于棋类对话） */
    protected String chess;

    /** 机器人编号参数 */
    protected int param;

    /** Ark API Key（示例中为硬编码，实际项目不推荐这样做） */
    protected static String Ark_key = "d96c7eea-0c30-44dc-8b0f-8d96c4d0fcd8";

    /** Ark 服务实例，用于向大模型发送请求 */
    protected ArkService arkService;

    /**
     * 构造方法
     *
     * @param param 机器人编号（用于区分不同机器人配置）
     */
    public TestConnect(int param) {
        this.param = param;

        // 初始化 ArkService（使用 API Key）
        this.arkService = ArkService.builder()
                .apiKey(Ark_key)
                .build();

        // 不同 Prompt 文件路径
        String filepathpri = "Robot_App/Robot" + param + "/Robot" + param + ".txt";
        String filepathpub = "Robot_App/Public/public.txt";
        String filechess   = "Robot_App/Public/chess.txt";

        try {
            // 读取 Prompt 文件内容
            this.pub   = Files.readString(Paths.get(filepathpub));
            this.pri   = Files.readString(Paths.get(filepathpri));
            this.chess = Files.readString(Paths.get(filechess));
        } catch (Exception e) {
            // 读取文件失败时打印异常信息
            e.printStackTrace();
        }
    }

    /**
     * main 方法
     * ----------------------------
     * 用于测试 Ark 接口是否能够正常调用
     */
    public static void main(String[] args) {

        // 从系统环境变量中读取 API Key（推荐方式）
        String Key = System.getenv("ARK_API_KEY");
        System.out.println("Using ARK_API_KEY: " + Key);

        // 创建 ArkService 实例
        ArkService arkService = ArkService.builder()
                .apiKey(Key)
                .build();

        // 构造请求对象
        CreateResponsesRequest request = CreateResponsesRequest.builder()
                .model("doubao-seed-1.6-250615") // 指定模型
                .input(
                        ResponsesInput.builder()
                                .stringValue("Hi，帮我讲个笑话。")
                                .build()
                )
                .build();

        // 调用大模型接口
        ResponseObject resp = arkService.createResponse(request);

        // 解析返回内容
        List<BaseItem> items = resp.getOutput();
        String assistantText = "";

        for (BaseItem item : items) {
            if (item instanceof ItemOutputMessage msg) {
                if ("assistant".equals(msg.getRole())) {
                    for (OutputContentItem c : msg.getContent()) {
                        if (c instanceof OutputContentItemText t) {
                            assistantText += t.getText();
                        }
                    }
                }
            }
        }

        // 输出模型回复
        System.out.println("Assistant output:");
        System.out.println(assistantText);

        // 关闭线程池，释放资源
        arkService.shutdownExecutor();
    }

    /**
     * 普通聊天接口
     *
     * @param input 用户输入文本
     * @return 模型返回的文本结果
     */
    public String output(String input) {

        // ====== 网络连通性测试 ======
        try {
            URL url = new URL("https://www.baidu.com");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.connect();
        } catch (Exception e) {
            // 网络不可用时返回本地随机回复
            String[] replies = {
                    "这是一个很有趣的问题！",
                    "让我想想怎么回答你...",
                    "我明白了，你能告诉我更多吗？",
                    "关于 " + input + "，我的看法是...",
                    "谢谢你的分享！"
            };
            return replies[new Random().nextInt(replies.length)];
        }

        // ====== 构造模型请求 ======
        CreateResponsesRequest request = CreateResponsesRequest.builder()
                .model("doubao-seed-1.6-250615")
                .input(
                        ResponsesInput.builder()
                                // 拼接：私有 Prompt + 用户输入 + 公共 Prompt
                                .stringValue(pri + input + pub)
                                .build()
                )
                .build();

        // 调用模型
        ResponseObject resp = arkService.createResponse(request);

        // 解析模型返回内容
        String assistantText = readit(resp);

        // 调试输出
        System.out.println(pri + input + pub);
        System.out.println("Assistant output:");
        System.out.println(assistantText);

        return assistantText;
    }

    /**
     * 象棋专用对话接口
     *
     * @param input 用户输入的棋局或指令
     * @return 模型返回的棋局分析结果
     */
    public String chessout(String input) {

        CreateResponsesRequest request = CreateResponsesRequest.builder()
                .model("doubao-seed-1.6-250615")
                .input(
                        ResponsesInput.builder()
                                // 使用象棋专用 Prompt
                                .stringValue(chess + input)
                                .build()
                )
                .build();

        ResponseObject resp = arkService.createResponse(request);
        String assistantText = readit(resp);

        System.out.println("Assistant output:");
        System.out.println(assistantText);

        return assistantText;
    }

    /**
     * readit 方法
     * ----------------------------
     * 统一解析 Ark ResponseObject 中的 assistant 文本输出
     *
     * @param resp Ark 接口返回对象
     * @return 拼接后的模型文本回复
     */
    protected String readit(ResponseObject resp) {

        List<BaseItem> items = resp.getOutput();
        String assistantText = "";

        for (BaseItem item : items) {
            if (item instanceof ItemOutputMessage msg) {
                if ("assistant".equals(msg.getRole())) {
                    for (OutputContentItem c : msg.getContent()) {
                        if (c instanceof OutputContentItemText t) {
                            assistantText += t.getText();
                        }
                    }
                }
            }
        }
        return assistantText;
    }
}
