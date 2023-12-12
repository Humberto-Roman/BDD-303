package com.bdd;

import com.mongodb.client.MongoClients;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class Main {

    private static String[][] datosIngresados = new String[10][4];
    private static int filas = 10;
    private static int indiceDatos = 0;

    private static final String MONGO_URI = "mongodb://admin:admin@localhost:27017";
    private static final String DATABASE_NAME = "mydatabase";
    private static final String COLLECTION_NAME = "mycollection";

    public static class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    public static class ButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

        private final JButton button;
        private String value;

        public ButtonEditor(JTextField textField) {
            button = new JButton("Eliminar");
            button.addActionListener(this);
            button.setBorderPainted(false);
        }

        @Override
        public Object getCellEditorValue() {
            return value;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            this.value = (String) value;
            return button;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int confirmado = JOptionPane.showConfirmDialog(null, "¿Seguro que quieres eliminar este registro?",
                    "Confirmación", JOptionPane.YES_NO_OPTION);

            if (confirmado == JOptionPane.YES_OPTION) {
                // Eliminar la fila correspondiente en la matriz
                for (int i = 0; i < datosIngresados.length; i++) {
                    if (datosIngresados[i][4] != null && datosIngresados[i][4].equals(value)) {
                        datosIngresados[i] = null;
                        break;
                    }
                }

                // Actualizar la tabla
                fireEditingStopped();
            }
        }
    }

    // Main
    public static void main(String[] args) {
        JFrame marcoPrincipal = new JFrame("ABC Mongo");
        marcoPrincipal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String connectionString = "mongodb://admin:admin@localhost:27017,localhost:27018,localhost:27019/?replicaSet=myReplicaSet";

        ConnectionString connString = new ConnectionString(connectionString);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);

        JPanel panelPrincipal = new JPanel();

        JButton boton2 = new JButton("Ingresar Datos");
        JButton boton3 = new JButton("Leer Datos");
        JButton boton4 = new JButton("Editar Datos");

        panelPrincipal.add(boton2);
        panelPrincipal.add(boton3);
        panelPrincipal.add(boton4);

        boton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarFormularioIngresoDatos();
            }
        });

        boton3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarTablaDatos(false);
            }
        });

        boton4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarTablaDatos(true);
            }
        });

        marcoPrincipal.add(panelPrincipal);
        marcoPrincipal.setSize(300, 150);
        marcoPrincipal.setVisible(true);
    }

    private static void mostrarFormularioIngresoDatos() {
        JFrame ventanaIngresoDatos = new JFrame("Ingreso de Datos");
        ventanaIngresoDatos.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;

        String[] nombresCampos = { "Apellido Paterno", "Apellido Materno", "Nombres", "Edad" };

        for (String nombreCampo : nombresCampos) {
            JLabel labelCampo = new JLabel(nombreCampo + ":");
            JTextField textFieldCampo = new JTextField(20);
            gbc.gridx = 0;
            panelFormulario.add(labelCampo, gbc);
            gbc.gridx = 1;
            panelFormulario.add(textFieldCampo, gbc);
            gbc.gridy++;
        }

        JButton botonGuardar = new JButton("Guardar");
        botonGuardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarDatosIngresados(panelFormulario);
                mostrarMensaje("Datos guardados correctamente");
                ventanaIngresoDatos.dispose();
            }
        });
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panelFormulario.add(botonGuardar, gbc);

        ventanaIngresoDatos.add(panelFormulario);
        ventanaIngresoDatos.setSize(300, 200);
        ventanaIngresoDatos.setVisible(true);
    }

    private static void guardarDatosIngresados(JPanel panelFormulario) {
        Component[] componentes = panelFormulario.getComponents();
        int columna = 0;

        for (Component componente : componentes) {
            if (componente instanceof JTextField) {
                JTextField textField = (JTextField) componente;
                datosIngresados[indiceDatos][columna] = textField.getText();
                columna++;

                if (columna == 4) {
                    columna = 0;
                    indiceDatos++;

                    if (indiceDatos == filas) {
                        filas *= 2;
                        datosIngresados = Arrays.copyOf(datosIngresados, filas);
                        for (int i = filas / 2; i < filas; i++) {
                            datosIngresados[i] = new String[4];
                        }
                    }
                }
            }
        }

        // Agregar a MongoDB
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            Document document = new Document("apellido_paterno", datosIngresados[indiceDatos - 1][0])
                    .append("apellido_materno", datosIngresados[indiceDatos - 1][1])
                    .append("nombres", datosIngresados[indiceDatos - 1][2])
                    .append("edad", Integer.parseInt(datosIngresados[indiceDatos - 1][3]));

            collection.insertOne(document);

            // Actualizar la matriz con los datos recién insertados
            datosIngresados[indiceDatos - 1][4] = document.getObjectId("_id").toString();
        }
    }

    private static void mostrarTablaDatos(boolean permitirEdicion) {
        JFrame ventanaTablaDatos = new JFrame("Datos Ingresados");
        ventanaTablaDatos.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panelTabla = new JPanel();

        String[] columnas = { "Apellido Paterno", "Apellido Materno", "Nombres", "Edad", "Eliminar" };
        EditableTableModel modeloTabla = new EditableTableModel(datosIngresados, columnas, permitirEdicion);
        JTable tablaDatos = new JTable(modeloTabla);
        // Obtener datos desde MongoDB
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            // Obtener datos desde MongoDB y actualizar la matriz
            // (puedes modificar según tus necesidades)
            datosIngresados = obtenerDatosDesdeMongoDB(collection);
        }
        // Agregar una columna para el botón de eliminar
        tablaDatos.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        tablaDatos.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JTextField()));

        JScrollPane scrollPane = new JScrollPane(tablaDatos);
        panelTabla.add(scrollPane);

        // Agregar botón de guardar en la ventana de edición
        JButton botonGuardar = new JButton("Guardar");
        botonGuardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarCambiosTabla(modeloTabla);
                mostrarMensaje("Cambios guardados correctamente");
                ventanaTablaDatos.dispose();
            }
        });
        panelTabla.add(botonGuardar);

        ventanaTablaDatos.add(panelTabla);
        ventanaTablaDatos.setSize(500, 300);
        ventanaTablaDatos.setVisible(true);

    }

    private static String[][] obtenerDatosDesdeMongoDB(MongoCollection<Document> collection) {
        // Implementa la lógica para obtener datos desde MongoDB y retornarlos como una
        // matriz
        // (puedes modificar según tus necesidades)
        // Por ahora, simplemente devuelve una matriz vacía
        return new String[0][];
    }

    private static void guardarCambiosTabla(EditableTableModel modeloTabla) {
        // Puedes realizar acciones adicionales aquí según sea necesario
        // En este ejemplo, simplemente llamamos al método de modeloTabla para guardar
        // los cambios
        modeloTabla.guardarCambios();
    }

    private static void mostrarMensaje(String mensaje) {
        JFrame ventana = new JFrame("Mensaje");
        ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panelVentana = new JPanel();

        JLabel labelMensaje = new JLabel(mensaje);
        panelVentana.add(labelMensaje);

        ventana.add(panelVentana);
        ventana.setSize(200, 100);

        ventana.setVisible(true);
    }

    // DefaultTableModel que permite la edición de celdas
    private static class EditableTableModel extends DefaultTableModel {

        private final boolean[] isEditable;

        EditableTableModel(Object[][] data, Object[] columnNames, boolean permitirEdicion) {
            super(data, columnNames);
            isEditable = new boolean[columnNames.length];
            Arrays.fill(isEditable, permitirEdicion);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return isEditable[column];
        }

        // Método para guardar los cambios realizados en la tabla
        public void guardarCambios() {
            // Puedes realizar acciones adicionales aquí según sea necesario
            // En este ejemplo, no es necesario realizar ninguna acción
        }
    }

}

// Prueba de botones (crea una ventana con un mensaje)
/*
 * private static void abrirVentana(String nombreVentana) {
 * // Crear una nueva ventana
 * JFrame ventana = new JFrame(nombreVentana);
 * ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cerrar solo la
 * ventana actual
 * 
 * // Crear un panel para la nueva ventana
 * JPanel panelVentana = new JPanel();
 * 
 * // Crear un JLabel para mostrar el saludo
 * JLabel labelSaludo = new JLabel("¡Hola desde " + nombreVentana + "!");
 * panelVentana.add(labelSaludo);
 * 
 * // Agregar el panel a la ventana
 * ventana.add(panelVentana);
 * 
 * // Establecer el tamaño de la ventana
 * ventana.setSize(200, 100);
 * 
 * // Hacer visible la nueva ventana
 * ventana.setVisible(true);
 * }
 */
