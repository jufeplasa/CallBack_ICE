import com.zeroc.Ice.SocketException;

import Demo.PrinterPrx;
import Demo.CallbackPrx;

import java.net.NetworkInterface;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class PrinterI implements Demo.Printer {

    ExecutorService executor = Executors.newFixedThreadPool(5);
    private long initialTime = System.nanoTime();
    private int numRequest;
    private double throuhgput;
    private double averageTimeResponse;
    private List<ClientDTO> clients =new ArrayList<ClientDTO>();
    Semaphore semaphore = new Semaphore(1);

    public PrinterI() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    checkConnections();
                } catch (Exception e) {
                }
            }
        });
        thread.start();
    }

    /* 
     * Imprimir cadena
     * El metodo hace inicios de variables para el calcula de una medidas de desempeño.
     * Luego llama al metodo de procesar cadena. Cuando el metodo devuelve una respuesta se hace
     * un calculo parical de las medidas del desempeño. Imprime las metricas actulizadas y
     * devuelve la respuesta al cliente respectivo.
     */
    public void printString(String s, Demo.CallbackPrx callback, com.zeroc.Ice.Current current)
    {   
        addClient(s, callback, current);
        System.out.println("cliente agregado");
        executor.submit(() -> {
            long startTime = System.nanoTime();
            numRequest++;
            procesingString(s, callback);
            long endTime = System.nanoTime();
            calculatePerformMeasures(startTime,endTime);
            System.out.println(showPerformMeasures());
        });
    }

    /* 
     * Agregar Cliente
     * El metodo se encarga primero de verificar si el cliente existe,
     * luego crea un cliente y lo agrega en la lista "clients"
     * Hostname
     * Username
     * Proxy
     */
    public void addClient(String infoClient, Demo.CallbackPrx callback, com.zeroc.Ice.Current current){
        try {
            semaphore.acquire();
            String[] command = infoClient.split(":");
            if(findClientDTO(command[1])==null){
                ClientDTO newClient = new ClientDTO();
                newClient.setHostname(command[0]);
                newClient.setUsername(command[1]);
                newClient.setClientCallback(callback);
                clients.add(newClient);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            semaphore.release();
        }
    }

    /* 
     * Encontrar cliente
     * El metodo se encarga de buscar un cliente por medio de su hostname,
     * si lo encuentra devuelve el objeto sino devuelve "null"
     */
    private ClientDTO findClientDTO(String username){
        System.out.println("entro y busca");
        for (ClientDTO client : clients) {
            
            if(client.getUsername().equals(username)){
                return client;
            }
        }
        System.out.println("sale y no encontro");
        return null;
    }

    /* 
     * Eliminar cliente
     * El metodo se encarga de buscar un cliente por medio de su username,
     * si lo encuentra elimina el objeto de la lista "clients". En caso de
     * que el cliente no exista, no hace algo.
     */
    public void deleteClient(String username){
        try {
            semaphore.acquire();
            ClientDTO client=findClientDTO(username);
            if(client!=null){
                clients.remove(client);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            semaphore.release();
        }
    }

    /* 
     * Verificar conexiones
     * El metodo se encarga de verificar las conexiones de los clientes.
     * Si un cliente se desconecta, se elimina de la lista "users".
     */
    public void checkConnections() {
        for (ClientDTO user : clients) {
            String username = user.getUsername();
            try {
                Demo.CallbackPrx callback = user.getCallbackPrx();
                callback.ice_ping();
            } catch (Exception e) {
                deleteClient(username);
                System.out.println("Usuario " + username + " desconectado");
            }
        }
    }

    /* 
     * Enviar mensaje
     * El metodo se encarga de enviar un mensaje. Primero mira si el mensaje va para todos los clientes conectados
     * si es asi, entonces llama a un metodo que se encarge de mandar los mensajes a todos.
     * En caso contrario llama a un metodo que mande el mensaje a un cliente en especifico.
     */
    public void sendMessage(String who, String from, String message){
        message = "Mensaje de " + from + ": " + message;
        if(who.equalsIgnoreCase("All")||who.equalsIgnoreCase("BC")){
            sendToAll(message, from);
        }
        sendMessageClient(who, message);
    }
    /*
     * Mandar un mensaje a un cliente
     * El metodo consiste en buscar el objeto clienteDTO por medio del username, y verificar que exista,
     * una vez validado se obtinene el objeto prx del cliente y envía el mensaje al cliente receptor.
     */
    private void sendMessageClient(String who, String message){
        ClientDTO receiver=findClientDTO(who);
        if(receiver!=null){
            Demo.CallbackPrx sender = receiver.getCallbackPrx();
            sender.reportResponse(message);
        }
    }
    /*
     * Envir a todos
     * Se encarga de recorrer la lista y a cada uno enviarle el mensaje
     * por su respectivo prx
     */
    private void sendToAll(String message, String from){
        for (ClientDTO client : clients) {
            if(client.getUsername().equals(from)){
                continue;
            }  
            client.getCallbackPrx().reportResponse(message);
        }
    }

    /*
     * Mostrar clientes
     * Este metodo recorre la lista "clients" y devuelve un listado de
     * los username de los clientes conectados.
     */
    public String showAllClients(){
        String message="Lista de clientes:\n";
        for (ClientDTO client : clients) {
            message+="- "+client.getUsername()+"\n";
        }
        return message;
    }
    
    /* 
     * Procesar cadena
     * Descompone la cadena "s", donde obtenemos [hostname, username, comando]
     * Luego se verifica si el comando es un entero para realizar la sucesion de fibonnacci.
     * En caso contrario verifica si corresponde a otro caso como lista de interfaces,
     * lista de puertos, ! para un comando de consola.
     * 
     * En caso tampoco llamar alguna funcion de las que se menciona es porque el cliente
     * ha salido o cometio un error al ingresar la informacion.
     */
    public void procesingString(String s, Demo.CallbackPrx callback){

        String[] command = s.split(":", 3);
        if(command[2].equals("zhk")) {
            // go back before it is too late
            return;
        } else if(command[2].equals("exit")) {
            deleteClient(command[1]);
            callback.reportResponse("Hasta luego :D");
            return;
        }
        try {
            long number = Long.parseLong(command[2]);
            String serieF=fibonacci(number);
            String factores=findfactores(number);
            String message=command[0]+"/"+command[1]+": "+serieF+"\nFactores primos: "+factores;
            
            callback.reportResponse(message);
        } 
        catch (NumberFormatException e) {
            if (command[2].trim().equalsIgnoreCase("listifs")) {
                String result = listNetworkInterfaces();
                System.out.println(command[0]+"/"+command[1]+":"+result);
                callback.reportResponse(result);
            } 
            else if (command[2].startsWith("listports")) {
                String ip = command[2].split(" ")[1]; // Extrae la IP de la entrada del mensaje
                String result = scannerIp(ip);
                System.out.println(command[0]+"/"+command[1]+":"+result);
                callback.reportResponse(result);
            }
            else if (command[2].startsWith("!")) {
                String commando = command[2].substring(1); // Extrae el comando quitando el "!"
                String commandResult = executeCommand(commando);
                String result = command[0]+"/"+command[1] + ": " + commandResult;
                System.out.println(result);
                callback.reportResponse(result);
            }
            else if (command[2].equalsIgnoreCase("listC")) {
                callback.reportResponse(showAllClients());
            }
            else if (command[2].startsWith("to")) {
                String[] complement = command[2].split(":");
                String receiver=complement[0].split(" ")[1];
                String message=complement[1];
                sendMessage(receiver, command[1], message);
                callback.reportResponse("");
            } else if(command[2].startsWith("BC")) {
                String[] complement = command[2].split(":");
                sendMessage("All", command[1], complement[1]);
            } else if (command[2].equalsIgnoreCase("exit")) {
                deleteClient(command[0]);
                callback.reportResponse("Hasta luego :D");
            }

            else {
                callback.reportResponse("El valor no es pertenece a ningun comando: " + command[2]);
            }
        }
    } 

    public String fibonacci(long n){
        StringBuilder serieF = new StringBuilder("[");
        long a1=1;
        long a2=1;
        for (long i = 0; i < n; i++) {
            if (i==0||i==1) {
                serieF.append("1 ");
            }
            else{
                serieF.append(", ");
                long c= a1+a2;
                System.out.println(c);
                serieF.append(c);
                a2=a1;
                a1=c;
            }
        }
        serieF.append("]");
        return serieF.toString();
    }

    public String findfactores(long n  ){
        String factores="[";
        for (long i = 2; i <= n; i++) {
            while (n % i == 0) {
                factores+=i+" ";
                n /= i;
            }
        }
        factores+="]";
        return factores.toString();
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
    
    public String listNetworkInterfaces()  {
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

