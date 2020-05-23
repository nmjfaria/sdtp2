package sd1920.trab2.clients.soap;

import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import sd1920.trab2.api.Message;
import sd1920.trab2.api.soap.MessageServiceSoap;
import sd1920.trab2.clients.EmailResponse;
import sd1920.trab2.clients.MessagesEmailClient;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static com.sun.xml.ws.developer.JAXWSProperties.CONNECT_TIMEOUT;
import static com.sun.xml.ws.developer.JAXWSProperties.REQUEST_TIMEOUT;

public class MessageClientSoap extends EmailClientSoap implements MessagesEmailClient {

    private static final String MESSAGES_WSDL = "/messages/?wsdl";

    MessageServiceSoap messageProxy;

    public MessageClientSoap(URI serverUrl, int maxRetries, int retryPeriod) {
        super(serverUrl, maxRetries, retryPeriod);
    }

    //These method work like in UserClientSoap, using "tryMultiple" to retry the operation until it is successful,
    // and then translating the result to an EmailResponse
    public EmailResponse<Void>  deleteUserInfo(String user, String secret) {
        return tryMultiple(() -> {
            getClient().deleteUserInfo(user, secret);
            return EmailResponse.noContent();
        });
    }

    public EmailResponse<Void>  setupUserInfo(String user, String secret) {
        return tryMultiple(() -> {
            getClient().setupUserInfo(user, secret);
            return EmailResponse.noContent();
        });
    }

    public EmailResponse<Void>  forwardSendMessage(String user, Message msg, String secret) {
        return tryMultiple(() -> {
            getClient().forwardSendMessage(user, secret, msg);
            return EmailResponse.noContent();
        });
    }

    public EmailResponse<Void>  forwardDeleteSentMessage(String user, long mid, String secret) {
        return tryMultiple(() -> {
            getClient().forwardDeleteSentMessage(user, mid, secret);
            return EmailResponse.noContent();
        });
    }

    //this method creates a new MessageServiceSoap proxy, or returns the existing one if it has already been created
    //this method is called from within "tryMultiple" since it can fail
    private MessageServiceSoap getClient() throws MalformedURLException, WebServiceException {
        synchronized (this) {
            if (messageProxy == null) {
                QName QNAME = new QName(MessageServiceSoap.NAMESPACE, MessageServiceSoap.NAME);
                Service service = Service.create(new URL(serverUri + MESSAGES_WSDL), QNAME);
                messageProxy = service.getPort(MessageServiceSoap.class);
                //Set timeouts
                ((BindingProvider) messageProxy).getRequestContext().put(CONNECT_TIMEOUT, CONNECTION_TIMEOUT);
                ((BindingProvider) messageProxy).getRequestContext().put(REQUEST_TIMEOUT, REPLY_TIMEOUT);
            }
            return messageProxy;
        }
    }
}
