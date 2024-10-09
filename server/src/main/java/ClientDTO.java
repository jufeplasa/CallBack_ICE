public class ClientDTO {
    private String username;
    private String hostname;
    private Demo.PrinterPrx clientPrx;

    public ClientDTO(){}

    public String getHostname() {
        return hostname;
    }
    public String getUsername() {
        return username;
    }
    public Demo.PrinterPrx getClientPrx() {
        return clientPrx;
    }
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    public void setUsername(String username) {
        this.username = username;
    }
}
