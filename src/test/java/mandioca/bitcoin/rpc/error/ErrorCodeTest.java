package mandioca.bitcoin.rpc.error;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

import static junit.framework.TestCase.*;
import static mandioca.bitcoin.rpc.error.ErrorCode.*;

public class ErrorCodeTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Rule
    public TestName testName = new TestName();

    @Test
    public void testLookupInvalidErrorCode() {
        int invalidErrorCode = 1000000000;
        assertFalse(isValidErrorCode.apply(invalidErrorCode));
        exception.expect(RuntimeException.class);
        exception.expectMessage("No rpc server error code exists for 1000000000");
        getErrorCode.apply(invalidErrorCode);
    }

    @Test
    public void testLookupValidErrorCode() {
        assertTrue(isValidErrorCode.apply(RPC_WALLET_INSUFFICIENT_FUNDS.code()));
        ErrorCode errorCode = getErrorCode.apply(RPC_WALLET_INSUFFICIENT_FUNDS.code());
        assertEquals(RPC_WALLET_INSUFFICIENT_FUNDS, errorCode);
    }

    @Test
    public void testErrorCodeDescription() {
        assertTrue(isValidErrorCode.apply(RPC_INVALID_ADDRESS_OR_KEY.code()));
        ErrorCode errorCode = getErrorCode.apply(RPC_INVALID_ADDRESS_OR_KEY.code());
        assertEquals(RPC_INVALID_ADDRESS_OR_KEY.description(), "Invalid or non-wallet transaction id");
    }
}
