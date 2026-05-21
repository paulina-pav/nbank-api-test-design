package common.extensions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import common.annotation.FraudCheckMock;
import common.helper.StubFactory;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class FraudCheckMockExtension implements BeforeEachCallback, AfterEachCallback {

   private WireMockServer wireMockServer;

    @Override
    public void beforeEach(ExtensionContext context) throws JsonProcessingException {

        FraudCheckMock mockConfig = context.getTestMethod()
                .map(method -> method.getAnnotation(FraudCheckMock.class))
                .orElseGet(() -> context.getTestClass()
                        .map(clazz -> clazz.getAnnotation(FraudCheckMock.class))
                        .orElse(null));

        if (mockConfig != null) {
            setupWireMock(mockConfig);
        }
    }

    private void setupWireMock(FraudCheckMock config) throws JsonProcessingException {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(config.port()));
        wireMockServer.start();

        //WireMock.configureFor("0.0.0.0", config.port());
        WireMock.configureFor("localhost", config.port());

        StubFactory.makeStubFraudCheckTransfer(config);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (wireMockServer != null) {
            wireMockServer.getAllServeEvents().forEach(event -> {
                System.out.println("WireMock received: "
                        + event.getRequest().getMethod()
                        + " "
                        + event.getRequest().getUrl());
                System.out.println("Request body: " + event.getRequest().getBodyAsString());
            });

            wireMockServer.stop();
        }
    }
}
