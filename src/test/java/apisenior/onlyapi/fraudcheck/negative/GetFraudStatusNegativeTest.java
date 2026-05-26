package apisenior.onlyapi.fraudcheck.negative;

import api.generators.ErrorMessage;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import apisenior.BaseTest;
import common.annotation.UsersForApiTests;
import common.helper.NormalizerNegativeResponseMessage;
import common.storage.UserForApiStorage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class GetFraudStatusNegativeTest extends BaseTest {

    public static Stream<Arguments> invalidTransactionId() {
        return Stream.of(
                Arguments.of(Long.MAX_VALUE, ErrorMessage.TRANSACTION_NOT_FOUND.getMessage())
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTransactionId")
    @UsersForApiTests
    public void userCantCheckNonExistedTransactionStatus(Long invalidTransactionId, String expectedErrorMessage, UserForApiStorage context){

        String actualErrorMessage = new CrudRequester(
                RequestSpecs.authAsUser(context.getFirstUser().getRequest().getUsername(), context.getFirstUser().getRequest().getPassword()),
                Endpoint.CHECK_FRAUD_DETECTION_STATUS,
                ResponseSpecs.notFound()
        ).get(invalidTransactionId).extract().asString();

        String newMessage = NormalizerNegativeResponseMessage.normalizeMessage(actualErrorMessage);

        soflty.assertThat(newMessage).isEqualTo(expectedErrorMessage);
    }
}
