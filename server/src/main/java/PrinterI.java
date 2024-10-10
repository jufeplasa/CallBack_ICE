import com.zeroc.Ice.SocketException;
import java.net.NetworkInterface;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class PrinterI implements Demo.Printer {

    
    private long initialTime = System.nanoTime();
    private int numRequest;
    private double throuhgput;
    private double averageTimeResponse;
    private List<ClientDTO> clients =new ArrayList<ClientDTO>();

    public String printString(String s, com.zeroc.Ice.Current current)
    {   
        long startTime = System.nanoTime();
        numRequest++;
        System.out.println(s);
        String rMessage=procesingString(s);
        long endTime = System.nanoTime();
        calculatePerformMeasures(startTime,endTime);
        System.out.println(showPerformMeasures());
        return rMessage;
    }
  
    public void addClient(String infoClient, com.zeroc.Ice.Current current){
        
        String[] command = infoClient.split(":");
        if(findClientDTO(command[1])==null){
            ClientDTO newClient = new ClientDTO();
            Demo.PrinterPrx proxy = Demo.PrinterPrx.checkedCast(current.con.createProxy(current.id));
            newClient.setHostname(command[0]);
            newClient.setUsername(command[1]);
            newClient.setClientPrx(proxy);
            clients.add(newClient);
        }
    }

    private ClientDTO findClientDTO(String username){
        for (ClientDTO client : clients) {
            if(client.getUsername().equals(username)){
                return client;
            }
        }
        return null;
    }

    public void deleteClient(String username){
        ClientDTO client=findClientDTO(username);
        if(client!=null){
            clients.remove(client);
        }
    }

    public void sendMessage(String who){
        
    }

    public String procesingString(String s){

        String[] command = s.split(":");
        try {
            int number = Integer.parseInt(command[2]);
            String serieF=fibonacci(number);
            String factores=findfactores(number);
            String message=command[0]+"/"+command[1]+": "+serieF+"\nFactores primos: "+factores;
            return message;
        } 
        catch (NumberFormatException e) {
            if (command[2].equalsIgnoreCase("listifs")) {
                String result = listNetworkInterfaces();
                System.out.println(command[0]+"/"+command[1]+":"+result);
                return result;
            } 
            else if (command[2].startsWith("listports")) {
                String ip = command[2].split(" ")[1]; // Extrae la IP de la entrada del mensaje
                String result = scannerIp(ip);
                System.out.println(command[0]+"/"+command[1]+":"+result);
                return result;
            }
            else if (command[2].startsWith("!")) {
                String commando = command[2].substring(1); // Extrae el comando quitando el "!"
                String commandResult = executeCommand(commando);
                String result = command[0]+"/"+command[1] + ": " + commandResult;
                System.out.println(result);
                return result;
            }
            else if (command[2].equalsIgnoreCase("exit")) {
                return "Hasta luego :D";
            }
            else {
                return "El valor no es un numero entero ni 'tlist' o 'portlist': " + command[2];
            }
        }
    } 

    public String fibonacci(int n){
        StringBuilder serieF = new StringBuilder("[");
        int a1=1;
        int a2=1;
        for (int i = 0; i < n; i++) {
            if (i==0||i==1) {
                serieF.append("1 ");
            }
            else{
                serieF.append(", ");
                int c= a1+a2;
                serieF.append(c);
                a2=a1;
                a1=c;
            }
        }
        serieF.append("]");
        return serieF.toString();
    }

    public String findfactores(int n  ){
        String factores="[";
        for (int i = 2; i <= n; i++) {
            while (n % i == 0) {
                factores+=i+" ";
                n /= i;
            }
        }
        factores+="]";
        return factores;
    }

    //Retorna un arreglo con los puertos y servicios
    public String scannerIp(String ip) {
        String command = "nmap -p- " + ip;
        StringBuilder result = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (!firstLine) {
                    result.append(", "); // Agrega coma antes de cada línea (excepto la primera)
                }
                result.append(line);
                firstLine = false;
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }
    
    public String listNetworkInterfaces() throws SocketException {
        StringBuilder sb = new StringBuilder();
        Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
            boolean first = true;
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                // Verificar que no sea una interfaz virtual y que esté activa
                if (!networkInterface.isLoopback() && networkInterface.isUp()) {
                    if (!first) {
                        sb.append(", ");
                    }
                    sb.append(networkInterface.getName());
                    first = false;
                }
            }
        } catch (java.net.SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "No se encontro o no existe la direccion.";
        }
        return sb.toString();
    }

    public static String executeCommand(String command) {
        StringBuilder result = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (!firstLine) {
                    result.append("\n"); // Agrega una nueva línea antes de cada salida (excepto la primera)
                }
                result.append(line);
                firstLine = false;
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            result.append("Error ejecutando el comando: ").append(e.getMessage());
        }
        return result.toString();
    }

    public void calculatePerformMeasures(long startTime,long endTime){
        double elapsedSeconds  = (endTime - startTime) / 1000000000.0;
        double completeTime = (endTime - initialTime) / 1000000000.0;
        throuhgput=numRequest/completeTime;
        if(numRequest>1){
            averageTimeResponse=((averageTimeResponse*(numRequest-1))+elapsedSeconds)/numRequest;
        }
        else{
            averageTimeResponse=elapsedSeconds;
        }
    }

    public String showPerformMeasures(){
        String message;
        message="Throuhgput :"+throuhgput+
        "\nAverage of Response Time (seconds): "+averageTimeResponse+
        "\nNumer of Request: "+numRequest;
        return message;
    }

}

