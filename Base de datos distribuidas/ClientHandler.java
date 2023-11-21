import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private List<ClientHandler> clients;
    private PrintWriter out;
    private BufferedReader in;
    private String userName;

    public ClientHandler(Socket socket, List<ClientHandler> clients) {
        this.clientSocket = socket;
        this.clients = clients;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // out.println("Bienvenido al chat. Por favor, ingresa tu nombre:");
            userName = in.readLine();
            sendMessageToAllExceptMe("Usuario " + userName + " se ha unido al chat.");

            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                saveMessageToDatabase(userName, clientMessage);
                saveMessageToFile(userName, clientMessage);
                sendMessageToAllExceptMe(userName + ": " + clientMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clients.remove(this);
                sendMessageToAllExceptMe("Usuario " + userName + " ha abandonado el chat.");
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessageToAllExceptMe(String message) {
        for (ClientHandler client : clients) {
            if (client != this) {
                client.sendMessage(message);
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    private void saveMessageToFile(String userName, String message) {
        // Ruta completa en el disco local D (ajusta según tus necesidades)
        String filePath = "c:/conversation.txt";

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)))) {
            // Formato: [timestamp] usuario: mensaje
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.println("[" + timestamp + "] " + userName + ": " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveMessageToDatabase(String userName, String message) {
        // Agregar lógica para guardar en la base de datos si es necesario
    }
}
