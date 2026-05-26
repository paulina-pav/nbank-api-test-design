package common.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import common.annotation.FraudCheckMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

public class StubFactory {
    public static void makeStubFraudCheckTransfer(FraudCheckMock config) throws JsonProcessingException {

        String responseBody = FraudCheckResponseFactory.getResponseInStringFormat(config);

        stubFor(post(urlPathEqualTo(config.endpoint()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));
        System.out.println("WireMock started: http://localhost:" + config.port());
    }
}
