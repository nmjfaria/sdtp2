package sd1920.trab2.impl;

import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import sd1920.trab2.api.User;
import sd1920.trab2.api.rest.UserService;
import sd1920.trab2.clients.ClientFactory;
import sd1920.trab2.clients.EmailResponse;
import sd1920.trab2.clients.MessagesEmailClient;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Singleton
public class UserResource implements UserService {

    //Since there is a single data structure, using a concurrent structure (ConcurrentHashMap)
    //is simpler than using a synchronized block
    ConcurrentMap<String, User> users;
    String domain;
    String internalSecret;
    
    MessagesEmailClient localMessageClient;

    public UserResource(String domain, URI selfURI, String internalSecret) {
        System.out.println("Constructed UserResource in domain " + domain);
        this.users = new ConcurrentHashMap<>();
        this.domain = domain;
        this.internalSecret = internalSecret;
        localMessageClient = ClientFactory.getMessagesClient(selfURI, 2, 1000);
    }

    @Override
    public String postUser(User user) {
        System.out.println("PostUser: " + user);
        //Check errors in the request
        if (user.getName() == null || user.getName().isEmpty() || user.getName().contains("@") ||
                user.getPwd() == null || user.getPwd().isEmpty() || user.getDomain() == null ||
                user.getDomain().isEmpty())
            throw new WebApplicationException("Invalid or empty user or password", Response.Status.CONFLICT);
        if ( !user.getDomain().equals(this.domain))
            throw new WebApplicationException("Invalid domain", Response.Status.FORBIDDEN);


        //putIfAbsent atomically checks if the user already exists, and if it does not, inserts it into the map
        User old = users.putIfAbsent(user.getName(), user);
        if (old != null)
            throw new WebApplicationException("User already exists", Response.Status.CONFLICT);

        //Create the inbox in the MessageResource
        EmailResponse<Void> post = localMessageClient.setupUserInfo(user.getName(), internalSecret);
        //Errors in local requests should never happen, but we are handling them just in case
        if (post.getStatusCode() != Response.Status.NO_CONTENT.getStatusCode()) {
            System.err.println("Unexpected error in setupUserInfo: " + post);
            throw new WebApplicationException(500);
        }
        return user.getName() + "@" + user.getDomain();
    }

    @Override
    public User getUser(String name, String pwd) {
        //This method is also used from other methods in this class and in
        // MessageResource to validate user/password combinations.

        //Checks parameters, and if the user exists, returns it.
        if (name == null || name.isEmpty() || pwd == null || pwd.isEmpty())
            throw new WebApplicationException("Invalid or empty user or password", Response.Status.FORBIDDEN);
        User user = users.get(name);
        if (user == null)
            throw new WebApplicationException("Invalid user", Response.Status.FORBIDDEN);
        if (!user.getPwd().equals(pwd))
            throw new WebApplicationException("Invalid password", Response.Status.FORBIDDEN);
        return user;    }

    @Override
    public User updateUser(String name, String pwd, User user) {
        //fetches the user
        User oldUser = getUser(name, pwd);

        User newUser = new User();
        newUser.setName(oldUser.getName());
        newUser.setDomain(oldUser.getDomain());
        newUser.setPwd((user.getPwd() != null && !user.getPwd().isEmpty()) ?
                user.getPwd() : oldUser.getPwd());
        newUser.setDisplayName((user.getDisplayName() != null && !user.getDisplayName().isEmpty()) ?
                user.getDisplayName() : oldUser.getDisplayName());
        //replaces with the new one, returns null if the user was deleted between the fetch and now
        if (users.replace(name, newUser) == null)
            throw new WebApplicationException("User does not exist", Response.Status.CONFLICT);
        return newUser;
    }

    @Override
    public User deleteUser(String user, String pwd) {
        User u;
        //synchronized block since the "getUser" method also handles the "users" structure
        //and we do not want concurrency errors in two simultaneous deletes
        synchronized (this) {
            u = getUser(user, pwd);
            users.remove(user);
        }
        //Deletes the users' inbox and outbox from MessageResource
        EmailResponse<Void> delete = localMessageClient.deleteUserInfo(user, internalSecret);
        //Errors in local requests should never happen, but we are handling them just in case
        if (delete.getStatusCode() != Response.Status.NO_CONTENT.getStatusCode()){
            System.err.println("Unexpected error in deleteUserInfo: " + delete);
            throw new WebApplicationException(500);
        }
        return u;
    }
}
