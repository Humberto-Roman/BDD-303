package com.alumnos;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Main {

    private static List<Alumno> listaAlumnos;
    private static DefaultTableModel tableModel;
    private static MongoCollection<Document> alumnosCollection;
    private static Alumno[] arregloAlumnos;
    private static JTable table;

    public static void main(String[] args) {
        // Conexión a la base de datos MongoDB (asegúrate de tener MongoDB en ejecución)
        try {
            MongoClient mongoClient = MongoClients.create();
            MongoDatabase database = mongoClient.getDatabase("universidad");
            alumnosCollection = database.getCollection("alumnos");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al conectar con la base de datos MongoDB.");
            System.exit(1);
        }

        // Cargar alumnos previamente guardados en MongoDB
        listaAlumnos = cargarAlumnosDesdeMongo();

        SwingUtilities.invokeLater(() -> {
            // Crear un JFrame (ventana)
            JFrame frame = new JFrame("Formulario de Alumnos");
            frame.setSize(600, 400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Crear un JPanel (panel)
            JPanel panel = new JPanel();
            placeComponents(panel);

            // Agregar el panel a la ventana
            frame.add(panel);

            // Hacer visible la ventana
            frame.setVisible(true);
        });
    }

    private static void placeComponents(JPanel panel) {
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel labelCantidad = new JLabel("Cantidad de Alumnos:");
        JTextField textFieldCantidad = new JTextField(5);

        JButton buttonIngresar = new JButton("Ingresar Datos");

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(labelCantidad, gbc);

        gbc.gridx = 1;
        panel.add(textFieldCantidad, gbc);

        gbc.gridx = 2;
        panel.add(buttonIngresar, gbc);

        buttonIngresar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ingresarDatosAlumnos();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null,
                            "Por favor, ingrese un número válido para la cantidad de alumnos.");
                }
            }
        });

        JButton buttonMostrarTabla = new JButton("Mostrar Tabla");
        gbc.gridx = 2;
        panel.add(buttonMostrarTabla, gbc);

        buttonMostrarTabla.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarTabla();
            }
        });

        // Nuevo botón para eliminar registros
        JButton buttonEliminar = new JButton("Eliminar Registro");
        gbc.gridx = 2;
        gbc.gridy = 1;
        panel.add(buttonEliminar, gbc);

        buttonEliminar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eliminarRegistroSeleccionado();
            }
        });
    }

    private static void mostrarTabla() {
        JFrame tablaFrame = new JFrame("Tabla de Alumnos");
        tablaFrame.setSize(500, 300);

        JTable table = new JTable();
        tableModel = new DefaultTableModel();
        tableModel.addColumn("Apellido Paterno");
        tableModel.addColumn("Apellido Materno");
        tableModel.addColumn("Nombres");
        tableModel.addColumn("Edad");

        // Leer registros desde la base de datos y agregarlos al modelo de la tabla
        List<Alumno> alumnosDesdeDB = cargarAlumnosDesdeMongo();
        for (Alumno alumno : alumnosDesdeDB) {
            Object[] rowData = { alumno.getApellidoPaterno(), alumno.getApellidoMaterno(), alumno.getNombres(),
                    alumno.getEdad() };
            tableModel.addRow(rowData);
        }

        table.setModel(tableModel);

        // Añadir función para obtener el índice de la fila seleccionada
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // No es necesario actualizar ningún índice en la clase Alumno
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        tablaFrame.add(scrollPane);

        tablaFrame.setVisible(true);
    }

    private static void eliminarRegistroSeleccionado() {
        int selectedRowIndex = table.getSelectedRow(); // Obtener el índice de la fila seleccionada

        if (selectedRowIndex != -1) {
            // Obtener el ID del documento a eliminar desde el modelo de la tabla
            String apellidoPaterno = (String) tableModel.getValueAt(selectedRowIndex, 0);
            String apellidoMaterno = (String) tableModel.getValueAt(selectedRowIndex, 1);
            String nombres = (String) tableModel.getValueAt(selectedRowIndex, 2);
            int edad = (int) tableModel.getValueAt(selectedRowIndex, 3);

            // Eliminar el registro de la base de datos
            eliminarRegistroEnMongo(apellidoPaterno, apellidoMaterno, nombres, edad);

            // Eliminar la fila del modelo de la tabla
            tableModel.removeRow(selectedRowIndex);
        } else {
            JOptionPane.showMessageDialog(null, "Seleccione un registro para eliminar.");
        }
    }

    private static void eliminarRegistroEnMongo(String apellidoPaterno, String apellidoMaterno, String nombres,
            int edad) {
        // Eliminar el registro en MongoDB
        Document query = new Document()
                .append("apellidoPaterno", apellidoPaterno)
                .append("apellidoMaterno", apellidoMaterno)
                .append("nombres", nombres)
                .append("edad", edad);
        alumnosCollection.deleteOne(query);
    }

    private static List<Alumno> cargarAlumnosDesdeMongo() {
        List<Alumno> alumnos = new ArrayList<>();
        for (Document doc : alumnosCollection.find()) {
            String apellidoPaterno = doc.getString("apellidoPaterno");
            String apellidoMaterno = doc.getString("apellidoMaterno");
            String nombres = doc.getString("nombres");
            int edad = doc.getInteger("edad");

            Alumno alumno = new Alumno(apellidoPaterno, apellidoMaterno, nombres, edad);
            alumnos.add(alumno);
        }
        return alumnos;
    }

    /**
     * 
     */

    private static void ingresarDatosAlumnos() {
        // Si ya hay alumnos cargados
        if (!listaAlumnos.isEmpty()) {
            // Mostrar la tabla directamente
            arregloAlumnos = listaAlumnos.toArray(new Alumno[0]);
            mostrarTabla();
        } else {
            // Si no hay alumnos cargados, ingresar datos para el primer alumno
            arregloAlumnos = new Alumno[1]; // Inicializar el arreglo con espacio para un alumno
            for (int i = 0; i < arregloAlumnos.length; i++) {
                JFrame alumnoFrame = new JFrame("Datos del Alumno #" + (i + 1));
                alumnoFrame.setSize(300, 200);

                JPanel alumnoPanel = new JPanel(new GridBagLayout());
                placeAlumnoComponents(alumnoPanel, i, alumnoFrame);

                alumnoFrame.add(alumnoPanel);
                alumnoFrame.setVisible(true);
            }
        }
    }

    private static void placeAlumnoComponents(JPanel panel, int index, JFrame alumnoFrame) {
        // Configurar el diseño del panel principal
        panel.setLayout(new GridLayout(1, 2, 10, 5)); // 1 fila, 2 columnas, espacio horizontal de 10 y vertical de 5

        // Panel para la primera columna (labels)
        JPanel panelColumna1 = new JPanel();
        panelColumna1.setLayout(new GridLayout(5, 1, 5, 5)); // 4 filas, 1 columna, espacio horizontal de 5 y vertical
                                                             // de 5

        // Etiquetas
        JLabel labelApellidoPaterno = new JLabel("Apellido Paterno:");
        JLabel labelApellidoMaterno = new JLabel("Apellido Materno:");
        JLabel labelNombres = new JLabel("Nombres:");
        JLabel labelEdad = new JLabel("Edad:");
        JLabel labelvoid = new JLabel(" ");

        // Agregar etiquetas al panel de la primera columna
        panelColumna1.add(labelApellidoPaterno);
        panelColumna1.add(labelApellidoMaterno);
        panelColumna1.add(labelNombres);
        panelColumna1.add(labelEdad);
        panelColumna1.add(labelvoid);

        // Panel para la segunda columna (textfields)
        JPanel panelColumna2 = new JPanel();
        panelColumna2.setLayout(new GridLayout(5, 1, 5, 5)); // 5 filas, 1 columna, espacio horizontal de 5 y vertical
                                                             // de 5

        // Campos de texto
        JTextField textFieldApellidoPaterno = new JTextField(20);
        JTextField textFieldApellidoMaterno = new JTextField(20);
        JTextField textFieldNombres = new JTextField(20);
        JTextField textFieldEdad = new JTextField(5);

        // Botón para guardar
        JButton buttonGuardar = new JButton("Guardar");

        // Agregar campos de texto y botón al panel de la segunda columna
        panelColumna2.add(textFieldApellidoPaterno);
        panelColumna2.add(textFieldApellidoMaterno);
        panelColumna2.add(textFieldNombres);
        panelColumna2.add(textFieldEdad);
        panelColumna2.add(buttonGuardar);

        // Agregar los paneles de las columnas al panel principal
        panel.add(panelColumna1);
        panel.add(panelColumna2);

        // Acción del botón para guardar los datos del alumno
        buttonGuardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Obtener datos del formulario
                    String apellidoPaterno = textFieldApellidoPaterno.getText().trim();
                    String apellidoMaterno = textFieldApellidoMaterno.getText().trim();
                    String nombres = textFieldNombres.getText().trim();
                    String edadText = textFieldEdad.getText().trim();

                    // Validar que los campos no estén vacíos
                    if (apellidoPaterno.isEmpty() || apellidoMaterno.isEmpty() || nombres.isEmpty()
                            || edadText.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Por favor, complete todos los campos.");
                        return; // No continuar si hay campos vacíos
                    }

                    // Validar que la edad sea un número entero
                    int edad = Integer.parseInt(edadText);

                    // Guardar en MongoDB
                    Document alumnoDoc = new Document()
                            .append("apellidoPaterno", apellidoPaterno)
                            .append("apellidoMaterno", apellidoMaterno)
                            .append("nombres", nombres)
                            .append("edad", edad);
                    alumnosCollection.insertOne(alumnoDoc);

                    // Guardar en el arreglo local
                    Alumno alumno = new Alumno(apellidoPaterno, apellidoMaterno, nombres, edad);
                    arregloAlumnos[index] = alumno;

                    // Cerrar la ventana de ingreso de datos
                    alumnoFrame.dispose();

                    // Mostrar la tabla si es el último alumno
                    if (index == arregloAlumnos.length - 1) {
                        mostrarTabla();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Por favor, ingrese un número válido para la edad.");
                }
            }
        });
    }

    private static class Alumno {
        private String apellidoPaterno;
        private String apellidoMaterno;
        private String nombres;
        private int edad;

        public Alumno(String apellidoPaterno, String apellidoMaterno, String nombres, int edad) {
            this.apellidoPaterno = apellidoPaterno;
            this.apellidoMaterno = apellidoMaterno;
            this.nombres = nombres;
            this.edad = edad;
        }

        public String getApellidoPaterno() {
            return apellidoPaterno;
        }

        public String getApellidoMaterno() {
            return apellidoMaterno;
        }

        public String getNombres() {
            return nombres;
        }

        public int getEdad() {
            return edad;
        }

        public Document toDocument() {
            return new Document()
                    .append("apellidoPaterno", apellidoPaterno)
                    .append("apellidoMaterno", apellidoMaterno)
                    .append("nombres", nombres)
                    .append("edad", edad);
        }
    }
}