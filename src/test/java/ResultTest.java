import org.junit.Test;
import service.Result;
import static org.junit.Assert.*;

// tests for Result class
// just checking success/fail and messages

public class ResultTest {

    // success should return true
    @Test
    public void testSuccessIsSuccess() {
        Result result = Result.success("it worked");
        assertTrue(result.isSuccess());
    }

    // message should stay the same
    @Test
    public void testSuccessMessageIsKept() {
        Result result = Result.success("payment applied");
        assertEquals("payment applied", result.getMessage());
    }

    // empty message should still work
    @Test
    public void testSuccessWithEmptyMessage() {
        Result result = Result.success("");
        assertTrue(result.isSuccess());
    }

    // fail should return false
    @Test
    public void testFailIsNotSuccess() {
        Result result = Result.fail("something went wrong");
        assertFalse(result.isSuccess());
    }

    // fail message should stay the same
    @Test
    public void testFailMessageIsKept() {
        Result result = Result.fail("invalid payment amount");
        assertEquals("invalid payment amount", result.getMessage());
    }

    // empty fail message should still be a failure
    @Test
    public void testFailWithEmptyMessage() {
        Result result = Result.fail("");
        assertFalse(result.isSuccess());
    }

    // just checking success and fail are different
    @Test
    public void testSuccessAndFailAreDifferent() {
        Result ok = Result.success("ok");
        Result bad = Result.fail("bad");
        assertTrue(ok.isSuccess());
        assertFalse(bad.isSuccess());
    }
}