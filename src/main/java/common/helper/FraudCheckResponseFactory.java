package common.helper;

import api.models.MockFraudCheckResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.annotation.FraudCheckMock;


public class FraudCheckResponseFactory {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static MockFraudCheckResponse response;

    private static MockFraudCheckResponse createResponse(FraudCheckMock config) {
        response = MockFraudCheckResponse.builder()
                .status(config.status())
                .decision(config.decision())
                .riskScore(config.riskScore())
                .reason(config.reason())
                .requiresManualReview(config.requiresManualReview())
                .additionalVerificationRequired(config.additionalVerificationRequired())
                .build();

        return response;
    }

    public static String getResponseInStringFormat(FraudCheckMock config) throws JsonProcessingException {
        return MAPPER.writeValueAsString(createResponse(config));
    }

    public static MockFraudCheckResponse  getExpectedResult(){
        return response;
    }
}
