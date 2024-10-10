import java.net.UnknownHostException;
import java.util.Scanner;
import com.zeroc.Ice.Util;
import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
public class Client
{
    private static Scanner lector;
    public static void main(String[] args) throws UnknownHostException
    {
        lector=new Scanner(System.in);
        java.util.List<String> extraArgs = new java.util.ArrayList<>();

        try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args,"config.client",extraArgs)){


            // Communicator receiveCommunicator = Util.initialize(args, "config.client", extraArgs);
            // ObjectAdapter adapter = receiveCommunicator.createObjectAdapterWithEndpoints("PrinterAdapter", "tcp -p 10001");
            // PrinterI mensajero = new PrinterI();
            // adapter.add(mensajero, Util.stringToIdentity("printer"));
            // adapter.activate();

            // Thread receiverThread = new Thread(() -> {
            //     communicator.waitForShutdown();
            // });
            // receiverThread.start();


            Demo.PrinterPrx printer = Demo.PrinterPrx.checkedCast(communicator.propertyToProxy("Printer.Proxy")).ice_twoway().ice_secure(false);

            //Demo.PrinterPrx printer = Demo.PrinterPrx.checkedCast(base);

            if(printer == null)
            {
                throw new Error("Invalid proxy");
            }

            String username = System.getProperty("user.name");
            String hostname = java.net.InetAddress.getLocalHost().getHostName();
            String prev= hostname+":"+username+":";
            String message="";
            while(!message.equalsIgnoreCase("exit")){
                showMenu();
                message=lector.nextLine();
                String returnMsg=printer.printString(prev+message);
                System.out.println(returnMsg);
            }

            
        }
    }

    private static void showMenu(){
        System.out.println("Digita un mensaje con los siguientes formatos:");
        System.out.println("1) Numero positivo");
        System.out.println("2) Inicie listifs");
        System.out.println("3) Inicie listports [Direccion IP]");
        System.out.println("4) Inicie ![Comando en consola]");
        System.out.println("5) listC -> Obtener lista de clientes");
        //Se envia host/username:to X:(mensaje)
        System.out.println("6) to 'Hostname': -> Enviar un mensaje a un cliente");
        //Se envia host/username:BC:(mensaje)
        System.out.println("7) BC: -> Enviar un mensaje a todos los clientes");
        System.out.println("8) Exit");
    }


}