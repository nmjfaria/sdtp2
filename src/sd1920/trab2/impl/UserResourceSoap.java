package sd1920.trab2.impl;

import javax.inject.Singleton;
import javax.jws.WebService;
import javax.ws.rs.WebApplicationException;

import sd1920.trab2.api.User;
import sd1920.trab2.api.soap.MessagesException;
import sd1920.trab2.api.soap.UserServiceSoap;

import java.net.URI;

/**
 * This class simply wraps the REST implementation, translating REST errors into SOAP errors.
 */
@WebService(serviceName = UserServiceSoap.NAME,
        targetNamespace = UserServiceSoap.NAMESPACE,
        endpointInterface = UserServiceSoap.INTERFACE)
@Singleton
public class UserResourceSoap implements UserServiceSoap {

    UserResource resource;

    public UserResourceSoap(String domain, URI selfURI, String internalSecret) {
        System.out.println("Constructed UserResourceSoap in domain " + domain);
        resource = new UserResource(domain, selfURI, internalSecret);
    }

    @Override
    public String postUser(User user) throws MessagesException {
        try {
            return resource.postUser(user);
        } catch (WebApplicationException e) {
            throw new MessagesException(e.getResponse().getStatus());
        }
    }

    @Override
    public User getUser(String name, String pwd) throws MessagesException {
        try {
            return resource.getUser(name, pwd);
        } catch (WebApplicationException e) {
            throw new MessagesException(e.getResponse().getStatus());
        }
    }

    @Override
    public User updateUser(String name, String pwd, User user) throws MessagesException {
        try {
            return resource.updateUser(name, pwd, user);
        } catch (WebApplicationException e) {
            throw new MessagesException(e.getResponse().getStatus());
        }
    }

    @Override
    public User deleteUser(String user, String pwd) throws MessagesException {
        try {
            return resource.deleteUser(user, pwd);
        } catch (WebApplicationException e) {
            throw new MessagesException(e.getResponse().getStatus());
        }
    }
}
