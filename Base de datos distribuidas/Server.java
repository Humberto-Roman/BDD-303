import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Server {
    private static final int PORT = 8001;
    private static List<ClientHandler> clients = new ArrayList<>();
    private static final String CONVERSATION_FILE = "conversation.txt";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado. Esperando conexiones...");

            // Inicializar la conexión a la base de datos
            try {
                initializeDatabase();
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(1);
            }

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuevo cliente conectado: " + clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket, clients);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initializeDatabase() throws SQLException {
        // Código de inicialización de la base de datos
    }

    public static void saveMessageToDatabase(String userName, String message) {
        String jdbcUrl = "jdbc:mysql://localhost:3306/chat";
        String username = "root";
        String password = "Asdfgh";

        String insertQuery = "INSERT INTO conversacion (usuario, mensaje) VALUES (?, ?)";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
                PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, message);
            preparedStatement.executeUpdate();

            // También guardar en el archivo de texto
            saveMessageToFile(userName, message);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void saveMessageToFile(String userName, String message) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(CONVERSATION_FILE, true)))) {
            // Formato: [timestamp] usuario: mensaje
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.println("[" + timestamp + "] " + userName + ": " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
