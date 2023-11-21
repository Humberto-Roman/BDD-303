import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
    public static void main(String[] args) {
        // Conectar al servidor
        String host = "localhost";
        int port = 8001;

        try (Socket socket = new Socket(host, port);
                BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Enviar un nombre de usuario al servidor (puedes ajustar esto según tus
            // necesidades)
            String userName = "Soto Roman";
            out.println(userName);

            // Recibir mensajes del servidor e imprimirlos en la consola
            new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = serverIn.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Leer mensajes del usuario y enviarlos al servidor
            BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));
            String userInput;
            while ((userInput = userIn.readLine()) != null) {
                out.println(userInput);
            }

        } catch (IOException e) {
            System.err.println("Error al conectar al servidor.");
            e.printStackTrace();
        }
    }
}
