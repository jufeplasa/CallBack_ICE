public class ClientDTO {
    private String username;
    private String hostname;
    private Demo.CallbackPrx callback;

    public ClientDTO(){}

    public String getHostname() {
        return hostname;
    }
    public String getUsername() {
        return username;
    }
    public Demo.CallbackPrx getCallbackPrx() {
        return callback;
    }
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setClientCallback(Demo.CallbackPrx callback) {
        this.callback = callback;
    }
}
