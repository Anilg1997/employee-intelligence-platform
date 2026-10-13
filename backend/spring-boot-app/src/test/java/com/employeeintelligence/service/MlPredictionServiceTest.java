package com.employeeintelligence.api.service;

import com.employeeintelligence.api.dto.MlPredictionRequest;
import com.employeeintelligence.api.dto.MlPredictionResponse;
import com.employeeintelligence.api.dto.PerformancePredictionResponse;
import com.employeeintelligence.api.dto.PromotionPredictionResponse;
import java.util.Map;
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
    void shouldReturnPerformancePredictionAndUseDedicatedEndpoint() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://127.0.0.1:8000");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        MlPredictionService service = new MlPredictionService(builder.build());
        server.expect(requestTo("http://127.0.0.1:8000/predict/performance")).andExpect(method(POST))
                .andRespond(withSuccess("{\"performance_prediction\":3,\"performance_probability\":0.8,\"model_version\":\"performance-classifier-v1\"}", MediaType.APPLICATION_JSON));
        PerformancePredictionResponse response = service.predictPerformance(Map.of("Age", 35));
        assertEquals(3, response.getPerformance_prediction());
        assertEquals("performance-classifier-v1", response.getModel_version());
        server.verify();
    }

    @Test
    void shouldReturnPromotionReadinessAndUseDedicatedEndpoint() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://127.0.0.1:8000");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        MlPredictionService service = new MlPredictionService(builder.build());
        server.expect(requestTo("http://127.0.0.1:8000/predict/promotion")).andExpect(method(POST))
                .andRespond(withSuccess("{\"promotion_prediction\":\"Ready\",\"promotion_probability\":0.75,\"target_type\":\"portfolio_demo_rule_target\"}", MediaType.APPLICATION_JSON));
        PromotionPredictionResponse response = service.predictPromotion(Map.of("YearsAtCompany", 4));
        assertEquals("Ready", response.getPromotion_prediction());
        assertEquals("portfolio_demo_rule_target", response.getTarget_type());
        server.verify();
    }

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
                                     "attrition_probability": 0.6553,
                                     "model_version": "attrition-service-current",
                                     "decision_threshold": 0.55,
                                     "dataset": "IBM HR synthetic employee_data.csv",
                                     "training_provenance": "Legacy artifact",
                                     "limitations": ["Probability is not calibrated certainty."]
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
        assertEquals("attrition-service-current", response.getModel_version());
        assertEquals(0.55, response.getDecision_threshold(), 0.0001);
        assertEquals(1, response.getLimitations().size());

        server.verify();
    }

    @Test
    void shouldRejectMalformedAttritionResponse() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://127.0.0.1:8000");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        MlPredictionService service = new MlPredictionService(builder.build());
        server.expect(requestTo("http://127.0.0.1:8000/predict"))
                .andExpect(method(POST))
                .andRespond(withSuccess("{\"attrition_prediction\":\"Yes\",\"attrition_probability\":1.4}",
                        MediaType.APPLICATION_JSON));

        org.junit.jupiter.api.Assertions.assertThrows(
                com.employeeintelligence.api.exception.MlServiceException.class,
                () -> service.predict(new MlPredictionRequest()));
        server.verify();
    }
}
