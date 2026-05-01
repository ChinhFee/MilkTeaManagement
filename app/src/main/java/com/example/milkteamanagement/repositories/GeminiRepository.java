package com.example.milkteamanagement.repositories;

import android.util.Log;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.RequestOptions;
import com.google.common.util.concurrent.ListenableFuture;

public class GeminiRepository {
    private final GenerativeModelFutures model;
    private ChatFutures chatContext;

    public GeminiRepository(String apiKey, String menuData) {
        Log.d("GeminiRepository", "Initializing Gemini with model: gemini-1.5-flash");
        // System instruction giúp AI hiểu vai trò cố định mà không cần gửi kèm mỗi tin nhắn
        Content systemInstruction = new Content.Builder()
                .addText("Bạn là trợ lý ảo của quán trà sữa AuraBOBA (phong cách sang trọng Gold/Dark). " +
                        "Dưới đây là danh sách menu hiện tại: " + menuData + ". " +
                        "Hãy trả lời ngắn gọn, lịch sự và gợi ý món dựa trên yêu cầu của khách. " +
                        "Nếu khách hỏi món không có trong menu, hãy khéo léo gợi ý món tương tự.")
                .build();

        // Sử dụng gemini-1.5-flash trên endpoint v1 là cấu hình chuẩn nhất hiện tại
        GenerativeModel gm = new GenerativeModel(
                "gemini-1.5-flash", 
                apiKey,
                null,               // generationConfig
                null,               // safetySettings
                new RequestOptions(60000L, "v1"), // Timeout 60s, API v1
                null,               // tools
                null,               // toolConfig
                systemInstruction   // systemInstruction
        );

        this.model = GenerativeModelFutures.from(gm);
        this.chatContext = model.startChat(); // Bắt đầu phiên chat để giữ ngữ cảnh
    }

    public ListenableFuture<GenerateContentResponse> sendMessage(String userPrompt) {
        Content content = new Content.Builder()
                .addText(userPrompt)
                .build();
        return chatContext.sendMessage(content);
    }

    public void clearChat() {
        // Xóa lịch sử trò chuyện
        this.chatContext = model.startChat();
    }
}
