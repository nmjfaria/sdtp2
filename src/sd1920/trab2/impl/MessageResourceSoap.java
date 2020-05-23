package sd1920.trab2.impl;

import javax.inject.Singleton;
import javax.jws.WebService;
import javax.ws.rs.WebApplicationException;

import sd1920.trab2.api.Message;
import sd1920.trab2.api.soap.MessageServiceSoap;
import sd1920.trab2.api.soap.MessagesException;

import java.net.URI;
import java.util.*;

/**
 * This class simply wraps the REST implementation, translating REST errors into SOAP errors.
 */
@Singleton
@WebService(serviceName = MessageServiceSoap.NAME,
        targetNamespace = MessageServiceSoap.NAMESPACE,
        endpointInterface = MessageServiceSoap.INTERFACE)

public class MessageResourceSoap implements MessageServiceSoap {

    MessageResource resource;

    public MessageResourceSoap(String domain, URI selfURI, int midPrefix, String internalSecret) {
        System.out.println("Constructed MessageResourceSoap in domain " + domain);
        resource = new MessageResource(domain, selfURI, midPrefix, internalSecret);
    }

    @Override
    public long postMessage(String pwd, Message msg) throws MessagesException {
        try {
            return resource.postMessage(pwd, msg);
        } catch (WebApplicationException e) {
            throw new MessagesException(e.getResponse().getStatus());
        }
    }

    @Override
    public Message getMessage(String user, String pwd, long mid) throws MessagesException {
        try {
            return resource.getMessage(user, mid, pwd);
        } catch (WebApplicationException e) {
            throw new MessagesException(e.getResponse().getStatus());
        }
    }

    @Override
    public List<Long> getMessages(String user, String pwd) throws MessagesException {
        try {
            return resource.getMessages(user, pwd);
        } catch (WebApplicationException e) {
            throw new MessagesException(e.getResponse().getStatus());
        }
    }

    @Override
    public void removeFromUserInbox(String user, String pwd, long mid) throws MessagesException {
        try {
            resource.removeFromUserInbox(user, mid, pwd);
        } catch (WebApplicationException e) {
            throw new MessagesException(e.getResponse().getStatus());
        }
    }

    @Override
    public void deleteMessage(String user, String pwd, long mid) throws MessagesException {
        try {
            resource.deleteMessage(user, mid, pwd);
        } catch (WebApplicationException e) {
            throw new MessagesException(e.getResponse().getStatus());
        }
    }

    @Override
    public void deleteUserInfo(String user, String secret) throws MessagesException {
        try {
            resource.deleteUserInfo(user, secret);
        } catch (WebApplicationException e) {
            throw new MessagesException(e.getResponse().getStatus());
        }
    }

    @Override
    public void setupUserInfo(String user, String secret) throws MessagesException {
        try {
            resource.setupUserInfo(user, secret);
        } catch (WebApplicationException e) {
            throw new MessagesException(e.getResponse().getStatus());
        }
    }

    @Override
    public void forwardSendMessage(String user, String secret, Message message) throws MessagesException {
        try {
            resource.forwardSendMessage(user, secret, message);
        } catch (WebApplicationException e) {
            throw new MessagesException(e.getResponse().getStatus());
        }
    }

    @Override
    public void forwardDeleteSentMessage(String user, long mid, String secret) throws MessagesException {
        try {
            resource.forwardDeleteSentMessage(user, mid, secret);
        } catch (WebApplicationException e) {
            throw new MessagesException(e.getResponse().getStatus());
        }
    }
}
