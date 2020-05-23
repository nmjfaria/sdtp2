package sd1920.trab2.clients.rest;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import sd1920.trab2.clients.EmailResponse;
import sd1920.trab2.clients.MessagesEmailClient;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.function.Supplier;

/*
 * Base class for REST clients,
 * contains the code which is used in both the Message and User clients such as timeouts, WebTarget creation,
 * and the logic to retry the request until it is successful
 */
public abstract class EmailClientRest {
    public final static int CONNECTION_TIMEOUT = 1000;
    public final static int REPLY_TIMEOUT = 600;

    WebTarget target;
    int maxRetries;
    int retryPeriod;

    public EmailClientRest(URI serverUrl, int maxRetries, int retryPeriod, String resourceUrl){
        ClientConfig config = new ClientConfig();
        config.property(ClientProperties.CONNECT_TIMEOUT, CONNECTION_TIMEOUT);
        config.property(ClientProperties.READ_TIMEOUT, REPLY_TIMEOUT);
        javax.ws.rs.client.Client restClient = ClientBuilder.newClient(config);
        target = restClient.target(serverUrl).path(resourceUrl);
        this.maxRetries = maxRetries;
        this.retryPeriod = retryPeriod;
    }

    //This method receives and operation (called from MessageClientRest and UserClientRest), and retries it
    //until it is successful or until a given number of tries is reached
    protected <T> EmailResponse<T> tryMultiple(Supplier<EmailResponse<T>> operation) {
        int nTries = 0;
        while (nTries < maxRetries)
            try {
                return operation.get();
            } catch (ProcessingException e) {
                nTries++;
                System.out.println("Timeout on rest client for " + target.toString() + " (" + nTries + "/" + maxRetries + ") " + e.getMessage());
                try {
                    Thread.sleep(retryPeriod);
                } catch (InterruptedException ignored) {
                }
            }
        return EmailResponse.error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

}
