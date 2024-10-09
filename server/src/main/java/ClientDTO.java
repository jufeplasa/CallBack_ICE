public class ClientDTO {
    private String username;
    private String hostname;

    public ClientDTO(){}

    public String getHostname() {
        return hostname;
    }
    public String getUsername() {
        return username;
    }
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    public void setUsername(String username) {
        this.username = username;
    }
}
