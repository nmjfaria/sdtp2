package sd1920.trab2.util;

/*
 * Helper class to parser email formats (<user>@<domain>)
 */
public class Address {
    String name;
    String domain;

    private Address(String name, String domain) {
        this.name = name;
        this.domain = domain;
    }

    public static Address fromString(String email, String defaultDomain) {
        String[] components = email.split("@");
        switch (components.length) {
            case 1:
                return new Address(components[0], defaultDomain);
            case 2:
                return new Address(components[0], components[1]);
            default:
                return null;
        }
    }

    public static Address fromString(String email) {
        String[] components = email.split("@");
        if (components.length == 2)
            return new Address(components[0], components[1]);
        else
            return null;
    }

    public String getName() {
        return name;
    }

    public String getDomain() {
        return domain;
    }

    @Override
    public String toString() {
        return "Address{" +
                "name='" + name + '\'' +
                ", domain='" + domain + '\'' +
                '}';
    }
}
