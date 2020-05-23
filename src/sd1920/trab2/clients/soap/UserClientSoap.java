package sd1920.trab2.clients.soap;

import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import sd1920.trab2.api.User;
import sd1920.trab2.api.soap.UserServiceSoap;
import sd1920.trab2.clients.EmailResponse;
import sd1920.trab2.clients.UsersEmailClient;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static com.sun.xml.ws.developer.JAXWSProperties.CONNECT_TIMEOUT;
import static com.sun.xml.ws.developer.JAXWSProperties.REQUEST_TIMEOUT;

public class UserClientSoap extends EmailClientSoap implements UsersEmailClient {

    private static final String USERS_WSDL = "/users/?wsdl";

    UserServiceSoap userProxy;

    public UserClientSoap(URI serverUrl, int maxRetries, int retryPeriod) {
        super(serverUrl, maxRetries, retryPeriod);
    }

    //Method to check if a user exists and the password is valid
    public EmailResponse<User> getUser(String name, String pwd) {
        //Calls the tryMultiple method of EmailClientSoap to repeat the operation until it is successful,
        //Also translates the response to an EmailResponse in order to unify SOAP and REST responses
        return tryMultiple(() -> EmailResponse.ok(getClient().getUser(name, pwd)));
    }

    //this method creates a new UserServiceSoap proxy, or returns the existing one if it has already been created
    //this method is called from within "tryMultiple" since it can fail
    private UserServiceSoap getClient() throws MalformedURLException, WebServiceException {
        synchronized (this) {
            if (userProxy == null) {
                QName QNAME = new QName(UserServiceSoap.NAMESPACE, UserServiceSoap.NAME);
                Service service = Service.create(new URL(serverUri + USERS_WSDL), QNAME);
                userProxy = service.getPort(UserServiceSoap.class);
                //Set timeouts
                ((BindingProvider) userProxy).getRequestContext().put(CONNECT_TIMEOUT, CONNECTION_TIMEOUT);
                ((BindingProvider) userProxy).getRequestContext().put(REQUEST_TIMEOUT, REPLY_TIMEOUT);
            }
            return userProxy;
        }
    }
}
