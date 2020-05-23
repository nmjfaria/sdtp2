package sd1920.trab2.clients;

/*
 * this class is used to abstract the response of REST and SOAP operations into an uniform format.
 * for instance, when calling the method to validate the user from the MessageResource, we do not
 * care if the UserResource is REST or SOAP, so the client transforms the (SOAP or REST) response into
 * an EmailResponse.
 */
public class EmailResponse<T> {

    int statusCode;
    T entity;

    public EmailResponse(int statusCode, T entity) {
        this.statusCode = statusCode;
        this.entity = entity;
    }

    public static <T> EmailResponse<T> create(int statusCode, T entity) {
        return new EmailResponse<>(statusCode, entity);
    }

    public static <T> EmailResponse<T> create(int statusCode) {
        return new EmailResponse<>(statusCode, null);
    }

    public static <T> EmailResponse<T> error(int statusCode) {
        return new EmailResponse<>(statusCode, null);
    }

    public static <T> EmailResponse<T> ok(T entity) {
        return EmailResponse.create(200, entity);
    }

    public static EmailResponse<Void> noContent() {
        return EmailResponse.create(204, null);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public T getEntity() {
        return entity;
    }
}
