package com.example.milkteamanagement.repositories;

import android.util.Log;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.RequestOptions;
import com.google.ai.client.generativeai.type.TextPart;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;

public class GeminiRepository {
    private static final String MODEL_NAME = "gemini-2.5-flash";

    private final GenerativeModelFutures model;
    private ChatFutures chatContext;

    public GeminiRepository(String apiKey, String menuData) {
        Log.d("GeminiRepository", "Initializing Gemini model: " + MODEL_NAME);

        Content.Builder systemBuilder = new Content.Builder();
        systemBuilder.setRole("system");
        systemBuilder.addPart(new TextPart("Bạn là trợ lý ảo của quán trà sữa AuraBOBA. " +
                "Menu của quán bao gồm: " + menuData + ". " +
                "Hãy trả lời ngắn gọn, lịch sự và hỗ trợ khách chọn món."));
        systemBuilder.addPart(new TextPart("Khong dung Markdown. Khong dung dau **, dau #, hoac bullet *. " +
                "Neu goi y nhieu mon, moi mon tren mot dong theo mau: - Ten mon (gia): ly do ngan gon."));
        Content systemInstruction = systemBuilder.build();

        // Gemini API still exposes model metadata and generateContent through v1beta.
        RequestOptions requestOptions = new RequestOptions(60000L, "v1beta");

        /**
         * 3. Khởi tạo GenerativeModel với ĐÚNG thứ tự 8 tham số cho Java (SDK 0.9.0):
         * (1) modelName, (2) apiKey, (3) generationConfig, (4) safetySettings,
         * (5) requestOptions, (6) tools, (7) toolConfig, (8) systemInstruction
         */
        GenerativeModel gm = new GenerativeModel(
                MODEL_NAME,
                apiKey,
                null,
                null,
                requestOptions,
                null,
                null,
                systemInstruction
        );

        this.model = GenerativeModelFutures.from(gm);
        this.chatContext = initChat();
    }

    private ChatFutures initChat() {
        return model.startChat(new ArrayList<>());
    }

    public ListenableFuture<GenerateContentResponse> sendMessage(String userPrompt) {
        Content userContent = new Content.Builder()
                .addPart(new TextPart(userPrompt))
                .build();
        return chatContext.sendMessage(userContent);
    }
}
