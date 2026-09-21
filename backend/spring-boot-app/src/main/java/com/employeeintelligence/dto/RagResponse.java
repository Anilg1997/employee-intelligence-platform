package com.employeeintelligence.api.dto;

import java.util.List;
import java.util.Map;

public class RagResponse {

    private String question;
    private String answer;
    private List<Map<String, Object>> sources;

    public RagResponse(
            String question,
            String answer,
            List<Map<String, Object>> sources) {

        this.question = question;
        this.answer = answer;
        this.sources = sources;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public List<Map<String, Object>> getSources() {
        return sources;
    }
}