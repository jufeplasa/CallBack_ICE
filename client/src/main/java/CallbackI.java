public class CallbackI implements Demo.Callback {
    public void reportResponse(String response, com.zeroc.Ice.Current current) {
        System.out.println(response);
        System.out.println("\nDigita un mensaje con los siguientes formatos:");
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
        System.out.println("--------");
    }
}

