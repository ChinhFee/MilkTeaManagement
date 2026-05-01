package com.example.milkteamanagement.repositories;

import android.util.Log;
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

    public GeminiRepository(String apiKey, String menuData) {
        this.menuData = menuData;
        Log.d("GeminiRepository", "Initializing Gemini 1.5 Flash on v1beta endpoint");

        // Thiết lập System Instruction (Vai trò hệ thống)
        Content.Builder systemBuilder = new Content.Builder();
        systemBuilder.setRole("system"); 
        systemBuilder.addText("Bạn là trợ lý ảo của quán trà sữa AuraBOBA (sang trọng Gold/Dark). " +
                "Đây là menu của quán: " + menuData + ". " +
                "Hãy trả lời ngắn gọn, lịch sự. Nếu khách hỏi món không có, hãy gợi ý món tương tự.");
        Content systemInstruction = systemBuilder.build();

        GenerativeModel gm = new GenerativeModel(
                "gemini-1.5-flash", // Thử model name chuẩn
                apiKey,
                null,               // generationConfig
                null,               // safetySettings
                new RequestOptions(60000L, "v1beta"), // Chuyển sang v1beta
                null,               // tools
                null,               // toolConfig
                systemInstruction
        );

        this.model = GenerativeModelFutures.from(gm);
        this.chatContext = initChat();
    }

    private ChatFutures initChat() {
        // Sau khi đã dùng systemInstruction, lịch sử ban đầu có thể để trống
        List<Content> history = new ArrayList<>();
        return model.startChat(history);
    }

    public ListenableFuture<GenerateContentResponse> sendMessage(String userPrompt) {
        Content content = new Content.Builder()
                .addText(userPrompt)
                .build();
        return chatContext.sendMessage(content);
    }

    public void clearChat() {
        this.chatContext = initChat();
    }
}
