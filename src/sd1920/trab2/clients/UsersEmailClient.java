package sd1920.trab2.clients;

import sd1920.trab2.api.User;

/*
 * Interface used by both User clients
 */
public interface UsersEmailClient {

    EmailResponse<User> getUser(String name, String pwd);
}
