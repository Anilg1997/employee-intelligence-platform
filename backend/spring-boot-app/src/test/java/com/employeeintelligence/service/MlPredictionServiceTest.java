package com.employeeintelligence.api.service;

import com.employeeintelligence.api.dto.MlPredictionRequest;
import com.employeeintelligence.api.dto.MlPredictionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

class MlPredictionServiceTest {

    @Test
    void shouldReturnMlPrediction() {

        RestClient.Builder restClientBuilder = RestClient.builder()
                .baseUrl("http://127.0.0.1:8000")
                ;

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(restClientBuilder)
                        .build();

        RestClient restClient = restClientBuilder.build();

        MlPredictionService service =
                new MlPredictionService(restClient);

        server.expect(requestTo("http://127.0.0.1:8000/predict"))
                .andExpect(method(POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(
                        withSuccess(
                                """
                                {
                                    "attrition_prediction": "Yes",
                                    "attrition_probability": 0.6553
                                }
                                """,
                                MediaType.APPLICATION_JSON
                        )
                );

        MlPredictionRequest request = new MlPredictionRequest();

        MlPredictionResponse response =
                service.predict(request);

        assertNotNull(response);
        assertEquals("Yes", response.getAttrition_prediction());
        assertEquals(
                0.6553,
                response.getAttrition_probability(),
                0.0001
        );

        server.verify();
    }
}