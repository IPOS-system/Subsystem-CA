package api_impl;

import api.IPaymentAPI;
import domain.Payment;
import org.springframework.web.client.RestTemplate;
import service.Result;

import java.util.Map;

public class IPaymentAPIService implements IPaymentAPI {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Result Pay(Payment payment) {
        String url = "http://localhost:8080/api/payment/pay";

        System.out.println("Sending payment for order " + payment.getOrderId() +
                " amount " + payment.getAmount() + " via " + url);

        try {
            Map response = restTemplate.postForObject(url, payment, Map.class);

            if (response == null) {
                return Result.fail("no response from payment service");
            }

            boolean success = (boolean) response.get("success");
            String message = (String) response.get("message");

            return success
                    ? Result.success(message)
                    : Result.fail(message);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("error calling payment service");
        }
    }
}