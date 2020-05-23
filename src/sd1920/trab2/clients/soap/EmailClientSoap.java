package sd1920.trab2.clients.soap;

import javax.ws.rs.core.Response;
import javax.xml.ws.WebServiceException;

import sd1920.trab2.api.soap.MessagesException;
import sd1920.trab2.clients.EmailResponse;
import sd1920.trab2.clients.MessagesEmailClient;

import java.net.MalformedURLException;
import java.net.URI;

/*
 * Base class for SOAP clients,
 * contains the code which is used in both the Message and User clients such as timeouts
 * and the logic to retry the request until it is successful
 */

public abstract class EmailClientSoap {
    public final static int CONNECTION_TIMEOUT = 1000;
    public final static int REPLY_TIMEOUT = 600;

    int maxRetries;
    int retryPeriod;
    URI serverUri;


    public EmailClientSoap(URI serverUri, int maxRetries, int retryPeriod) {
        this.serverUri = serverUri;
        this.maxRetries = maxRetries;
        this.retryPeriod = retryPeriod;
    }

    //This method receives and operation (called from MessageClientSoap and UserClientSoap), and retries it
    //until it is successful or until a given number of tries is reached
    protected  <T> EmailResponse<T> tryMultiple(SoapSupplier<EmailResponse<T>> operation) {
        int nTries = 0;
        while (nTries < maxRetries)
            try {
                return operation.get();
            } catch (MessagesException me) { //Error executing the method in the server
                System.err.println("Soap operation failed: " + me.getMessage());
                return EmailResponse.error(400);
            } catch (MalformedURLException wse) {
                System.err.println("Malformed URL: " + wse.getMessage());
                return EmailResponse.error(400);
            } catch (WebServiceException wse) {
                nTries++;
                System.out.println("Timeout on soap client for " + serverUri + " (" + nTries + "/" + maxRetries + ")"  + wse.getMessage());
                try {
                    Thread.sleep(retryPeriod);
                } catch (InterruptedException ignored) {
                }
            }
        return EmailResponse.error(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @FunctionalInterface
    public interface SoapSupplier<T> {
        T get() throws MessagesException, MalformedURLException, WebServiceException;
    }

}
