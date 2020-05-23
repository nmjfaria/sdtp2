package sd1920.trab2.api.soap;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import sd1920.trab2.api.Message;

@WebService(serviceName=MessageServiceSoap.NAME, 
	targetNamespace=MessageServiceSoap.NAMESPACE, 
	endpointInterface=MessageServiceSoap.INTERFACE)
public interface MessageServiceSoap {
	
	String NAME = "messages";
	String NAMESPACE = "http://sd2019";
	String INTERFACE = "sd1920.trab2.api.soap.MessageServiceSoap";
	
	/**
	 * Posts a new message to the server, associating it to the inbox of every individual destination.
	 * An outgoing message should be modified before delivering it to the outbox, by changing the 
	 * sender to be in the format "display name <name@domain>", with display name the display name
	 * associated with a user.
	 * NOTE: there might be some destinations that are not from the local domain (see grading for 
	 * how addressing this feature is valued).
	 * 
	 * @param msg the message object to be posted to the server
	 * @param pwd password of the user sending the message
	 * @return the unique numerical identifier for the posted message. 
	 * @throws MessagesException exception in case of error.
	 */
	@WebMethod
	long postMessage(String pwd, Message msg) throws MessagesException;
	
	/**
	 * Obtains the message identified by mid of user user
	 * @param user user name for the operation
	 * @param mid the identifier of the message
	 * @param pwd password of the user
	 * @return the message if it exists.
	 * @throws MessagesException exception in case of error.
	 */
	@WebMethod
	Message getMessage(String user, String pwd, long mid) throws MessagesException;
		
	/**
	 * Returns a list of all messages stored in the server for a given user
	 * @param user the username of the user whose messages should be returned (optional)
	 * @param pwd password of the user
	 * @return a list of messages potentially empty;
	 * @throws MessagesException exception in case of error.
	 */
	@WebMethod
	List<Long> getMessages(String user, String pwd) throws MessagesException;
	
	/**
	 * Removes a message identified by mid from the inbox of user identified by user.
	 * @param user the username of the inbox that is manipulated by this method
	 * @param mid the identifier of the message to be deleted
	 * @param pwd password of the user
	 * @throws MessagesException exception in case of error.
	 */
	@WebMethod
	void removeFromUserInbox(String user, String pwd, long mid) throws MessagesException;

	/**
	 * Removes the message identified by mid from the inboxes of any server that holds the message.
	 * The deletion can be executed asynchronously and does not generate any error message if the
	 * message does not exist.
	 * 
	 * @param user the username of the sender of the message to be deleted
	 * @param mid the identifier of the message to be deleted
	 * @param pwd password of the user that sent the message
	 * @throws MessagesException exception in case of error.
	 */
	@WebMethod
	void deleteMessage(String user, String pwd, long mid) throws MessagesException;

	/**
	 * NOT REQUIRED
	 *
	 * Removes the inbox and outbox of a given user.
	 *
	 * @param user the username of the user which inbox and outbox will be deleted
	 * @param secret internal secret
	 * @throws MessagesException exception in case of error.
	 */
	void deleteUserInfo(String user, String secret) throws MessagesException;

	/**
	 * NOT REQUIRED
	 *
	 * Creates an inbox and outbox for a given user.
	 *
	 * @param user the username of the user which inbox and outbox will be deleted
	 * @param secret internal secret
	 * @throws MessagesException exception in case of error.
	 */
	void setupUserInfo(String user, String secret) throws MessagesException;

	/**
	 * NOT REQUIRED
	 *
	 * Receives requests to add message to a user's inbox from other servers.
	 *
	 * @param user the user name
	 * @param secret internal secret
	 * @param message the message object to be inserted in the users inbox
	 * @throws MessagesException exception in case of error.
	 */
	void forwardSendMessage(String user, String secret, Message message) throws MessagesException;

	/**
	 * NOT REQUIRED
	 *
	 * Receives requests to delete a message from a user's inbox from other servers.
	 *
	 * @param user the user name
	 * @param mid the identifier of the message to be deleted
	 * @param secret internal secret
	 * @throws MessagesException exception in case of error.
	 */
	void forwardDeleteSentMessage(String user, long mid, String secret) throws MessagesException;

}
