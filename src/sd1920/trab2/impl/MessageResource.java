package sd1920.trab2.impl;

import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import sd1920.trab2.api.Message;
import sd1920.trab2.api.User;
import sd1920.trab2.api.rest.MessageService;
import sd1920.trab2.clients.ClientFactory;
import sd1920.trab2.clients.EmailResponse;
import sd1920.trab2.clients.UsersEmailClient;
import sd1920.trab2.util.Address;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class MessageResource implements MessageService {

    String domain;
    //Counter for message ids (to avoid duplicates)
    AtomicInteger midCounter;
    //Prefix for message ids (numerical representation of the server IP, unique per server)
    int midPrefix;

    //Client for local communications with the UserResource
    UsersEmailClient localUserClient;

    //Per user
    final Map<String, Map<Long, Message>> inBoxes; //inbox of each user
    final Map<String, Map<Long, Message>> outBoxes; //outbox (sent messages) of each user

    //Per domain
    final Map<String, Dispatcher> dispatchers; //Map of all dispatchers -> each dispatcher contains a queue and a thread
    String internalSecret;

    public MessageResource(String domain, URI selfURI, int midPrefix, String internalSecret) {
        System.out.println("Constructed MessageResource in domain " + domain);
        System.out.println("Prefix: " + midPrefix);

        this.domain = domain;
        this.midCounter = new AtomicInteger(0);
        this.midPrefix = midPrefix;
        this.internalSecret = internalSecret;
        localUserClient = ClientFactory.getUsersClient(selfURI, 5, 1000);

        inBoxes = new HashMap<>();
        outBoxes = new HashMap<>();
        dispatchers = new ConcurrentHashMap<>();
    }

    public long nextMessageId() {
        //Message id is constructed using the (server-unique) prefix and a local counter
        int counter = midCounter.incrementAndGet();
        return ((long) counter << 32) | (midPrefix & 0xFFFFFFFFL);
    }

    //***************** INBOX OPERATIONS **************************
    @Override
    public Message getMessage(String user, long mid, String pwd) {
        System.out.println("GetMessage: " + user + " " + mid + " " + pwd);
        //Request to UserResource to check if user is valid
        EmailResponse<User> validate = localUserClient.getUser(user, pwd);
        if (validate.getStatusCode() != Response.Status.OK.getStatusCode())
            throw new WebApplicationException(validate.getStatusCode());
        Message result;
        //synchronized since we are handling data structures
        synchronized (this) {
            Map<Long, Message> inbox = inBoxes.get(user);
            if (inbox == null) //if the user exists, then so should the inbox.
                throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
            result = inbox.get(mid);
            if (result == null) //throw 404 if the message does not exist
                throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return result; //return the message
    }

    @Override
    public List<Long> getMessages(String user, String pwd) {
        //Same as the above method, but returns all message ids, instead of a single message
        System.out.println("GetInbox: " + user + " " + pwd);
        EmailResponse<User> validate = localUserClient.getUser(user, pwd);
        if (validate.getStatusCode() != Response.Status.OK.getStatusCode())
            throw new WebApplicationException(validate.getStatusCode());
        List<Long> results;
        synchronized (this) {
            Map<Long, Message> inbox = inBoxes.get(user);
            if (inbox == null)
                throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
            results = new LinkedList<>(inbox.keySet()); //adds all the keys of the map to a list
        }
        return results;
    }

    @Override
    public void removeFromUserInbox(String user, long mid, String pwd) {
        //Similar logic to "getMessage"
        System.out.println("RmvInbox: " + user + " " + mid + " " + pwd);
        EmailResponse<User> validate = localUserClient.getUser(user, pwd);
        if (validate.getStatusCode() != Response.Status.OK.getStatusCode())
            throw new WebApplicationException(validate.getStatusCode());
        synchronized (this) {
            Map<Long, Message> inbox = inBoxes.get(user);
            if (inbox == null)
                throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
            if (inbox.remove(mid) == null)
                throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    //Method called by other servers to forward sent messages
    @Override
    public void forwardSendMessage(String user, String secret, Message message) {
        //Checks the secret, to make sure clients cannot call this method
        if (secret == null || !secret.equals(internalSecret))
            throw new WebApplicationException(Response.Status.FORBIDDEN);

        //Synchronized since we are handling data structures (inboxes)
        //Adds to the user inbox, or throws a 404 if the inbox (and consequently the user) does not exist
        synchronized (this) {
            Map<Long, Message> inbox = inBoxes.get(user);
            if (inbox == null)
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            inbox.put(message.getId(), message);
            System.out.println("Added " + message.getId() + " to inbox of " + user);
        }
    }

    //Method called by other servers to forward message deletion
    @Override
    public void forwardDeleteSentMessage(String user, long mid, String secret) {
        if (secret == null || !secret.equals(internalSecret))
            throw new WebApplicationException(Response.Status.FORBIDDEN);

        //Similar logic as "forwardSendMessage"
        synchronized (this) {
            Map<Long, Message> inbox = inBoxes.get(user);
            if (inbox == null || inbox.remove(mid) == null) {
                System.out.println("Not found in forwardDeleteSentMessage, probably deleted by user");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }
    }

    //Called by "postMessage" or by the dispatcher to add an error message to the user inbox
    public void createErrorMessage(String formattedSender, String senderName, long msgId, String destination) {
        try {
            //To avoid duplicate code, calls "forwardSendMessage"
            forwardSendMessage(senderName, internalSecret,
                    new Message(nextMessageId(), formattedSender, senderName + "@" + domain,
                            "FALHA NO ENVIO DE " + msgId + " PARA " + destination, new byte[0]));
        } catch (WebApplicationException e) {
            //Could happen for instance, if the user sent a message to an invalid destination
            //and was deleted immediately after
            System.out.println("Unexpected WebAppExc in createErrorMessage... " + e.getMessage());
        }
    }
    //***************** OUTBOX OPERATIONS **************************

    @Override
    public long postMessage(String pwd, Message msg) {
        //Validate msg params
        System.out.println("SendMessage: " + msg);

        //Parses sender into a class
        Address sender = Address.fromString(msg.getSender(), domain);
        //Checks for errors
        if (sender == null || !sender.getDomain().equals(domain))
            throw new WebApplicationException(Response.Status.CONFLICT);
        if (msg.getDestination() == null || msg.getDestination().size() == 0)
            throw new WebApplicationException(Response.Status.CONFLICT);

        //Checks if user is valid
        EmailResponse<User> validate = localUserClient.getUser(sender.getName(), pwd);
        if (validate.getStatusCode() != Response.Status.OK.getStatusCode())
            throw new WebApplicationException(validate.getStatusCode());
        User u = validate.getEntity();

        //Formats the sender of the message to the correct format
        msg.setSender(u.getDisplayName() + " <" + u.getName() + "@" + u.getDomain() + ">");
        //Sets the id to a new unique id
        msg.setId(nextMessageId());

        //Adds to the outbox (useful for the deleteMessage operation)
        //synchronized since we are handling a data structure)
        synchronized (this) {
            outBoxes.get(u.getName()).put(msg.getId(), msg);
        }

        //For each destination...
        for (String d : msg.getDestination()) {
            Address destination = Address.fromString(d);
            //If it is invalid, creates an error message in the sender inbox
            if (destination == null) {
                System.out.println("Dest: " + d + " is invalid");
                createErrorMessage(msg.getSender(), u.getName(), msg.getId(), d);
            } else if (destination.getDomain().equals(domain)) {
                //if it is a local domain, simply call the forwardSendMessage function
                //and creates an error message if the destination is invalid
                try {
                    forwardSendMessage(destination.getName(), internalSecret, msg);
                } catch (WebApplicationException e) {
                    System.out.println("Failed to deliver locally " + e.getMessage());
                    createErrorMessage(msg.getSender(), u.getName(), msg.getId(), d);
                }
            } else {
                //if it is in another domain, creates a dispatcher if there is not one already, and submits
                //a new deliverJob to it.
                //The method "computeIfAbsent" creates a new dispatcher, of fetches the existing one if it exists already
                dispatchers.computeIfAbsent(destination.getDomain(), k ->
                        new Dispatcher(k, this)).addDeliverJob(msg, destination.getName(), u.getName());
            }
        }
        return msg.getId();
    }

    @Override
    public void deleteMessage(String user, long mid, String pwd) {
        //Similar logic to "postMessage"
        System.out.println("DeleteMessage: " + user + " " + mid + " " + pwd);
        //Checks if user is valid
        EmailResponse<User> validate = localUserClient.getUser(user, pwd);
        if (validate.getStatusCode() != Response.Status.OK.getStatusCode())
            throw new WebApplicationException(validate.getStatusCode());
        Message msg;
        //Checks if the message is in the user outbox, which means it was sent by that user
        //and has not yet been deleted
        synchronized (this) {
            msg = outBoxes.get(user).remove(mid);
            if (msg == null) {
                System.out.println("Not found");
                return;
            }
        }

        //For each destination in the original message, either deletes from the inbox if it is a local user
        //or creates a deleteJob in the corresponding dispatcher
        for (String d : msg.getDestination()) {
            Address addr = Address.fromString(d);
            if (addr != null) {
                if (addr.getDomain().equals(domain)) {
                    try {
                        forwardDeleteSentMessage(addr.getName(), msg.getId(), internalSecret);
                    } catch (WebApplicationException ignored) {
                    }
                } else {
                    dispatchers.computeIfAbsent(addr.getDomain(), k -> new Dispatcher(k, this))
                            .addDeleteJob(msg.getId(), addr.getName());
                }
            }
        }
    }

    @Override
    public void deleteUserInfo(String user, String secret) {
        //Used by UserResource to delete the inbox and outbox when the user is deleted
        if (secret == null || !secret.equals(internalSecret))
            throw new WebApplicationException(Response.Status.FORBIDDEN);

        synchronized (this) {
            Map<Long, Message> remove = inBoxes.remove(user);
            Map<Long, Message> remove1 = outBoxes.remove(user);
            if (remove == null || remove1 == null) {
                System.err.println("deleteUserInfo was called, but inbox or outbox did not exist...");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }
    }

    @Override
    public void setupUserInfo(String user, String secret) {
        //Used by UserResource to create the inbox and outbox when the user is created
        if (secret == null || !secret.equals(internalSecret))
            throw new WebApplicationException(Response.Status.FORBIDDEN);

        synchronized (this) {
            if (inBoxes.containsKey(user) || outBoxes.containsKey(user)) {
                System.err.println("setupUserInfo was called, but inbox or outbox already exists...");
                throw new WebApplicationException(Response.Status.CONFLICT);
            } else
                inBoxes.put(user, new HashMap<>());
            outBoxes.put(user, new HashMap<>());
        }
    }
    
    protected String getInternalSecret()
    {
    	return internalSecret;
    }
  
}
