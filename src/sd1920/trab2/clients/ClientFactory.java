package sd1920.trab2.clients;

import java.net.URI;

import sd1920.trab2.clients.rest.MessageClientRest;
import sd1920.trab2.clients.rest.UserClientRest;
import sd1920.trab2.clients.soap.MessageClientSoap;
import sd1920.trab2.clients.soap.UserClientSoap;
import sd1920.trab2.clients.proxy.MessageClientRestProxy;
import sd1920.trab2.clients.proxy.UserClientRestProxy;

/**
 * Class responsible for creating the correct client from a given server URL
 * Simply checks if the URL end with "SOAP" or "REST" and returns a new client of the corresponding type.
 */
public class ClientFactory {

    public static MessagesEmailClient getMessagesClient(URI url, int maxRetries, int retryPeriod){
        String[] split = url.toString().split("/");
        String type = split[split.length-1];
        if(type.equals("soap")){
            return new MessageClientSoap(url, maxRetries, retryPeriod);
        } else if(type.equals("rest")){
            return new MessageClientRest(url, maxRetries, retryPeriod);
        } else {
            throw new AssertionError("Unknown url: " + url + " - " + type);
        }
    }
    
    public static MessagesEmailClient getMessagesClientProxy(URI url, int maxRetries, int retryPeriod){
        String[] split = url.toString().split("/");
        String type = split[split.length-1];
       if(type.equals("rest"))
            return new MessageClientRestProxy(url, maxRetries, retryPeriod);
       else
    	   throw new AssertionError("Unknown url: " + url + " - " + type);
        
    }

    public static UsersEmailClient getUsersClient(URI url, int maxRetries, int retryPeriod){
        String[] split = url.toString().split("/");
        String type = split[split.length-1];
        if(type.equals("soap")){
            return new UserClientSoap(url, maxRetries, retryPeriod);
        } else if(type.equals("rest")){
            return new UserClientRest(url, maxRetries, retryPeriod);
        } else {
            throw new AssertionError("Unknown url: " + url + " - " + type);
        }
    }
    
    public static UsersEmailClient getUsersClientProxy(URI url, int maxRetries, int retryPeriod){
        String[] split = url.toString().split("/");
        String type = split[split.length-1];
        if(type.equals("rest"))
            return new UserClientRestProxy(url, maxRetries, retryPeriod);
         else 
            throw new AssertionError("Unknown url: " + url + " - " + type);
        
    }
}
