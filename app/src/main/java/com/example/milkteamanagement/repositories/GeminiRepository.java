package com.example.milkteamanagement.repositories;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.RequestOptions;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;

public class GeminiRepository {
    private final GenerativeModelFutures model;
    private ChatFutures chatContext;
    private final String menuData;
    private final String inventoryData;

    public GeminiRepository(String apiKey, String menuData) {
        this(apiKey, menuData, "Dữ liệu kho đang được cập nhật...");
    }

    public GeminiRepository(String apiKey, String menuData, String inventoryData) {
        this.menuData = menuData;
        this.inventoryData = inventoryData;

        RequestOptions requestOptions = new RequestOptions(60000L, "v1beta");

        // System Instruction: Dạy cho AI biết về Menu và Kho hàng
        Content systemInstruction = new Content.Builder()
                .addText("Bạn là trợ lý ảo AuraAI của quán trà sữa AuraBOBA.\n" +
                        "1. MENU CỦA QUÁN: " + menuData + "\n" +
                        "2. TÌNH TRẠNG KHO HÀNG HIỆN TẠI: " + inventoryData + "\n" +
                        "NHIỆM VỤ:\n" +
                        "- Trả lời ngắn gọn, lịch sự, thân thiện.\n" +
                        "- Nếu khách hỏi về món ăn, hãy tư vấn dựa trên menu.\n" +
                        "- Nếu chủ quán (Admin) hỏi về kho, hãy báo cáo dựa trên số liệu kho. Cảnh báo nếu có nguyên liệu sắp hết (đỏ).\n" +
                        "- Luôn trả lời bằng tiếng Việt.")
                .build();

        GenerativeModel gm = new GenerativeModel(
                "gemini-1.5-flash",
                apiKey,
                null, null,
                requestOptions,
                null, null,
                systemInstruction
        );

        this.model = GenerativeModelFutures.from(gm);
        this.chatContext = initChat();
    }

    private ChatFutures initChat() {
        return model.startChat(new ArrayList<>());
    }

    public ListenableFuture<GenerateContentResponse> sendMessage(String userPrompt) {
        Content content = new Content.Builder().addText(userPrompt).build();
        return chatContext.sendMessage(content);
    }

    public void clearChat() {
        this.chatContext = initChat();
    }
}
