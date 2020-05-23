package sd1920.trab2.clients.rest;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import sd1920.trab2.api.Message;
import sd1920.trab2.api.rest.MessageService;
import sd1920.trab2.clients.EmailResponse;
import sd1920.trab2.clients.MessagesEmailClient;

import java.net.URI;

public class MessageClientRest extends EmailClientRest implements MessagesEmailClient {

    public MessageClientRest(URI serverUrl, int maxRetries, int retryPeriod) {
        super(serverUrl, maxRetries, retryPeriod, MessageService.PATH);
    }

    //These method work like in UserClientRest, using "tryMultiple" to retry the operation until it is successful,
    // and then translating the result to an EmailResponse
    public EmailResponse<Void> deleteUserInfo(String user, String secret) {
        return tryMultiple(() -> {
            Response delete = target.path("internal").path("user").path(user)
                    .queryParam("secret", secret).request().delete();
            return EmailResponse.create(delete.getStatus());
        });
    }

    public EmailResponse<Void> setupUserInfo(String user, String secret) {
        return tryMultiple(() -> {
            Response post = target.path("internal").path("user").path(user)
                    .queryParam("secret", secret).request().post(Entity.json(""));
            return EmailResponse.create(post.getStatus());

        });
    }

    public EmailResponse<Void> forwardSendMessage(String user, Message msg, String secret) {
        return tryMultiple(() -> {
            Response post = target.path("internal").path("msg").path(user)
                    .queryParam("secret", secret).request().post(Entity.entity(msg, MediaType.APPLICATION_JSON));
            return EmailResponse.create(post.getStatus());

        });

    }

    public EmailResponse<Void> forwardDeleteSentMessage(String user, long mid, String secret) {
        return tryMultiple(() -> {
            Response delete = target.path("internal").path("msg").path(user).path(String.valueOf(mid))
                    .queryParam("secret", secret).request().delete();
            return EmailResponse.create(delete.getStatus());

        });
    }
}
