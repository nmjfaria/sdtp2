package sd1920.trab2.api.soap;

import javax.jws.WebMethod;
import javax.jws.WebService;

import sd1920.trab2.api.User;

@WebService(serviceName=UserServiceSoap.NAME, 
	targetNamespace=UserServiceSoap.NAMESPACE, 
	endpointInterface=UserServiceSoap.INTERFACE)
public interface UserServiceSoap {

	String NAME = "users";
	String NAMESPACE = "http://sd2019";
	String INTERFACE = "sd1920.trab2.api.soap.UserServiceSoap";
	
	/**
	 * Creates a new user in the local domain.
	 * @param user User to be created
	 * @return 200: the address of the user (name@domain).
	 * @throws MessagesException exception in case of error.
	 */
	@WebMethod
	String postUser(User user) throws MessagesException;
	
	/**
	 * Obtains the information on the user identified by name
	 * Also used to authenticate a given user an its password from MessageResource
	 *
	 * @param name the name of the user
	 * @param pwd password of the user (or a special password)
	 * @return the user object, if the name exists and pwd matches the existing password 
	 * (or is the a special password allowing all operations).
	 * @throws MessagesException exception in case of error.
	 */
	@WebMethod
	User getUser(String name, String pwd) throws MessagesException;
	
	/**
	 * Modifies the information of a user. Values of null in any field of the user will be 
	 * considered as if the the fields is not to be modified (the name cannot be modified).
	 * @param name the name of the user
	 * @param pwd password of the user (or a special password)
	 * @param user Updated information
	 * @return the updated user object, if the name exists and pwd matches the existing password 
	 * (or is the a special password allowing all operations);
	 * 409 otherwise
	 */
	@WebMethod
	User updateUser(String name, String pwd, User user)  throws MessagesException;

	/**
	 * Deletes the user identified by name
	 * @param user the name of the user
	 * @param pwd password of the user (or a special password)
	 * @return the deleted user object, if the name exists and pwd matches the existing password 
	 * (or is the a special password allowing all operations).
	 * @throws MessagesException exception in case of error.
	 */
	@WebMethod
	User deleteUser(String user, String pwd) throws MessagesException;

}
