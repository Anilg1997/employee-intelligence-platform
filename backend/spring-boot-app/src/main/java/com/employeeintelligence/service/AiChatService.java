package com.employeeintelligence.api.service;

import dev.langchain4j.model.chat.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class AiChatService {

    private final ChatModel chatModel;

    public AiChatService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String chat(String message) {
        return chatModel.chat(message);
    }
}