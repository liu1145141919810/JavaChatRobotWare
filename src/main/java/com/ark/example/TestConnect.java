package com.ark.example;
import com.volcengine.ark.runtime.model.responses.item.ItemOutputMessage;
import com.volcengine.ark.runtime.service.ArkService;
import com.volcengine.ark.runtime.model.responses.request.*;
import com.volcengine.ark.runtime.model.responses.response.ResponseObject;
import com.volcengine.ark.runtime.model.responses.content.OutputContentItem;
import com.volcengine.ark.runtime.model.responses.content.OutputContentItemText;
import com.volcengine.ark.runtime.model.responses.item.BaseItem;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TestConnect {
    protected String pub;
    protected String pri;
    protected String chess;
    protected int param;
    protected static String Ark_key= "d96c7eea-0c30-44dc-8b0f-8d96c4d0fcd8";
    protected ArkService arkService;

    public TestConnect(int param) {
        this.param = param;
        this.arkService = ArkService.builder().apiKey(Ark_key).build();
        String filepathpri = "Robot_App/Robot"+param+"/Robot"+param+".txt";
        String filepathpub = "Robot_App/Public/public.txt";
        String filechess= "Robot_App/Public/chess.txt";
        try {
            this.pub = Files.readString(Paths.get(filepathpub));
            this.pri = Files.readString(Paths.get(filepathpri));
            this.chess = Files.readString(Paths.get(filechess));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        String Key = System.getenv("ARK_API_KEY");
        System.out.println("Using ARK_API_KEY: " + Key);

        ArkService arkService = ArkService.builder().apiKey(Key).build();
        // create a response first
        CreateResponsesRequest request = CreateResponsesRequest.builder()
                .model("doubao-seed-1.6-250615")
                .input(ResponsesInput.builder().stringValue("Hi，帮我讲个笑话。").build())
                .build();
        ResponseObject resp = arkService.createResponse(request);
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

        System.out.println("Assistant output:");
        System.out.println(assistantText);

        arkService.shutdownExecutor();
    }
    public String output(String input){
        // create a response first
        CreateResponsesRequest request = CreateResponsesRequest.builder()
                .model("doubao-seed-1.6-250615")
                .input(ResponsesInput.builder().stringValue(pri+input+pub).build())
                .build();
        ResponseObject resp = arkService.createResponse(request);
        String assistantText = readit(resp);
        System.out.println(pri+input+pub);

        System.out.println("Assistant output:");
        System.out.println(assistantText);

        return assistantText;
    }
    public String chessout(String input){
        // create a response first
        CreateResponsesRequest request = CreateResponsesRequest.builder()
                .model("doubao-seed-1.6-250615")
                .input(ResponsesInput.builder().stringValue(chess+input).build())
                .build();
        ResponseObject resp = arkService.createResponse(request);
        String assistantText = readit(resp);
        System.out.println(pri+input+pub);

        System.out.println("Assistant output:");
        System.out.println(assistantText);

        return assistantText;
    }
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
         