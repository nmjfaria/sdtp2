package sd1920.trab2.api;

/**
 * Represents a user in the system.
 */
public class User {
    private String name;
    private String pwd;
    private String displayName;
    private String domain;

    public User() {
        this.name = null;
        this.pwd = null;
        this.displayName = null;
        this.domain = null;
    }

    public User(String name, String pwd, String displayName, String domain) {
        this.name = name;
        this.pwd = pwd;
        this.displayName = displayName;
        this.domain = domain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", pwd='" + pwd + '\'' +
                ", displayName='" + displayName + '\'' +
                ", domain='" + domain + '\'' +
                '}';
    }
}
