package sd1920.trab2.clients.proxy;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import sd1920.trab2.api.User;
import sd1920.trab2.api.rest.UserService;
import sd1920.trab2.clients.EmailResponse;
import sd1920.trab2.clients.UsersEmailClient;

import java.net.URI;

public class UserClientRestProxy extends EmailClientRestProxy implements UsersEmailClient {


    public UserClientRestProxy(URI serverUrl, int maxRetries, int retryPeriod) {
        super(serverUrl, maxRetries, retryPeriod, UserService.PATH);
    }

    //Method to check if a user exists and the password is valid
    public EmailResponse<User> getUser(String name, String pwd) {
        //Calls the tryMultiple method of EmailClientRest to repeat the operation until it is successful,
        //Also translates the response to an EmailResponse in order to unify SOAP and REST responses
        return tryMultiple(() -> {
            Response response = target.path(name).queryParam("pwd", pwd).request()
                    .accept(MediaType.APPLICATION_JSON).get();
            return EmailResponse.create(response.getStatus(), response.readEntity(User.class));
        });
    }
}
