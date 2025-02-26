1. JUnit Test Cases for TaxGPT
import org.junit.Test;
import static org.junit.Assert.*;

public class TaxGPTTest {

    @Test
    public void testTaxCalculation() {
        TaxGPT taxGPT = new TaxGPT();
        double result = taxGPT.calculateTax(700000, "individual");
        assertEquals(52000, result, 0.01);  // Example tax amount
    }

    @Test
    public void testDeductionsIdentification() {
        TaxGPT taxGPT = new TaxGPT();
        String deductions = taxGPT.identifyDeductions(700000, true, false);
        assertTrue(deductions.contains("Home Loan Interest"));
    }

    @Test
    public void testChatbotResponse() {
        TaxGPT taxGPT = new TaxGPT();
        String response = taxGPT.getChatbotResponse("What deductions can I claim?");
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    public void testApiKeySecurity() {
        TaxGPT taxGPT = new TaxGPT();
        String apiKey = taxGPT.getApiKey();
        assertFalse(apiKey.equals("YOUR_API_KEY_HERE"));  // Ensure key is set properly
    }
}


