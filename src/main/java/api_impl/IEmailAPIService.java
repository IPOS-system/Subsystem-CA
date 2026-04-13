package api_impl;

import api.IEmailAPI;
import domain.Email;
import org.springframework.web.client.RestTemplate;
import service.Result;

public class IEmailAPIService implements IEmailAPI {
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Result sendEmail(Email email) {
        String url = "http://localhost:8080/api/email/send";

        System.out.println("Sending email to " + email.getRecipient() +
                " with subject '" + email.getSubject() +
                "' and body '" + email.getBody() +
                "' via " + url);

        try {
            Boolean response = restTemplate.postForObject(url, email, Boolean.class);

            if (Boolean.TRUE.equals(response)) {
                return Result.success("email sent successfully");
            } else {
                return Result.fail("email failed to send");
            }

        } catch (Exception e) {
            return Result.fail("error calling email service");
        }
    }
}
