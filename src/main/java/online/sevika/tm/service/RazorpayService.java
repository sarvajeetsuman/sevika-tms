package online.sevika.tm.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import online.sevika.tm.config.RazorpayConfig;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Formatter;

/**
 * Service for Razorpay payment gateway operations.
 */
@Service
@Slf4j
public class RazorpayService {

    private final RazorpayClient razorpayClient;
    private final RazorpayConfig razorpayConfig;

    public RazorpayService(RazorpayConfig razorpayConfig) throws RazorpayException {
        this.razorpayConfig = razorpayConfig;
        this.razorpayClient = new RazorpayClient(razorpayConfig.getKeyId(), razorpayConfig.getKeySecret());
        log.info("Razorpay client initialized in {} mode", razorpayConfig.getTestMode() ? "TEST" : "LIVE");
    }

    /**
     * Create a Razorpay order for payment
     */
    public Order createOrder(BigDecimal amount, String currency, String receipt) throws RazorpayException {
        try {
            JSONObject orderRequest = new JSONObject();
            // Razorpay expects amount in smallest currency unit (paise for INR)
            orderRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", receipt);

            Order order = razorpayClient.orders.create(orderRequest);
            log.info("Razorpay order created: {}", order.get("id").toString());
            return order;
        } catch (RazorpayException e) {
            log.error("Error creating Razorpay order: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Verify payment signature
     */
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            String generatedSignature = calculateHmacSHA256(payload, razorpayConfig.getKeySecret());
            boolean isValid = generatedSignature.equals(signature);
            
            if (isValid) {
                log.info("Payment signature verified successfully for order: {}", orderId);
            } else {
                log.warn("Payment signature verification failed for order: {}", orderId);
            }
            
            return isValid;
        } catch (Exception e) {
            log.error("Error verifying payment signature: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Calculate HMAC SHA256 signature
     */
    private String calculateHmacSHA256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    /**
     * Get Razorpay key ID for client-side integration
     */
    public String getKeyId() {
        return razorpayConfig.getKeyId();
    }
}
