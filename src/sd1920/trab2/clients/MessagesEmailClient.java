package sd1920.trab2.clients;

import sd1920.trab2.api.Message;

/*
 * Interface used by both Message clients
 */
public interface MessagesEmailClient {

    EmailResponse<Void> deleteUserInfo(String user, String secret);

    EmailResponse<Void> setupUserInfo(String user, String secret);

    EmailResponse<Void> forwardSendMessage(String user, Message msg, String secret);

    EmailResponse<Void> forwardDeleteSentMessage(String user, long mid, String secret);

}
