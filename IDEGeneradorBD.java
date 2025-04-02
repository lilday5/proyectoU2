import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.antlr.runtime.*;
import java.io.*;
import java.sql.*;



public class IDEGeneradorBD extends JFrame {

    private JTextArea areaEntrada;
    private JTextArea areaSQL;
    private JTextArea areaErrores;
    private JButton btnCompilar;
    private JButton btnEjecutar;

    public IDEGeneradorBD() {
        setTitle("Generador de BD - Lenguaje de Alto Nivel");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Panel principal
        JPanel panelCentro = new JPanel(new BorderLayout());

        // Área de entrada
        areaEntrada = new JTextArea();
        JScrollPane scrollEntrada = new JScrollPane(areaEntrada);
        scrollEntrada.setBorder(BorderFactory.createTitledBorder("Entrada (Lenguaje de alto nivel)"));

        // Área de SQL generado
        areaSQL = new JTextArea();
        areaSQL.setEditable(false);
        JScrollPane scrollSQL = new JScrollPane(areaSQL);
        scrollSQL.setBorder(BorderFactory.createTitledBorder("SQL generado"));

        // Panel de los dos cuadros
        JPanel panelAreas = new JPanel(new GridLayout(1, 2, 10, 10));
        panelAreas.add(scrollEntrada);
        panelAreas.add(scrollSQL);

        // Panel de botones
        btnCompilar = new JButton("Compilar");
        btnEjecutar = new JButton("Ejecutar");
        btnEjecutar.setEnabled(false);

        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new BoxLayout(panelBotones, BoxLayout.Y_AXIS));
        panelBotones.add(Box.createVerticalGlue());
        panelBotones.add(btnCompilar);
        panelBotones.add(Box.createRigidArea(new Dimension(0, 10)));
        panelBotones.add(btnEjecutar);
        panelBotones.add(Box.createVerticalGlue());

        // Añadir botones al centro entre las áreas
        JPanel panelCentroConBotones = new JPanel(new BorderLayout());
        panelCentroConBotones.add(panelAreas, BorderLayout.CENTER);
        panelCentroConBotones.add(panelBotones, BorderLayout.WEST);

        // Área de errores
        areaErrores = new JTextArea(4, 80);
        areaErrores.setEditable(false);
        JScrollPane scrollErrores = new JScrollPane(areaErrores);
        scrollErrores.setBorder(BorderFactory.createTitledBorder("Errores"));

        // Eventos
        btnCompilar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                compilar();
            }
        });

        btnEjecutar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ejecutar();
            }
        });

        // Agregar al frame
        add(panelCentroConBotones, BorderLayout.CENTER);
        add(scrollErrores, BorderLayout.SOUTH);
    }

    private void compilar() {
    String entrada = areaEntrada.getText();
    areaErrores.setText("");
    areaSQL.setText("");
    btnEjecutar.setEnabled(false);

    if (entrada.isEmpty()) {
        areaErrores.setText("Error: No se ha escrito ninguna entrada.");
        return;
    }

    try {
        // Crear input para ANTLR desde el texto del IDE
        ANTLRInputStream input = new ANTLRInputStream(new ByteArrayInputStream(entrada.getBytes()));
        TLexer lexer = new TLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TParser parser = new TParser(tokens);

        // Redireccionar salida estándar a un buffer temporal para capturar SQL generado
        ByteArrayOutputStream bufferSalida = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(bufferSalida));

        // Ejecutar la regla inicial
        parser.inicio();

        // Restaurar salida estándar
        System.setOut(originalOut);

        // Mostrar SQL generado
        String resultadoSQL = bufferSalida.toString();
        areaSQL.setText(resultadoSQL);
        btnEjecutar.setEnabled(true);

    } catch (RecognitionException e) {
        areaErrores.setText("Error de sintaxis: " + e.getMessage());
    } catch (Exception e) {
        areaErrores.setText("Error: " + e.getMessage());
        e.printStackTrace();
    }
}


    private void ejecutar() {
    String sql = areaSQL.getText();

    // Parámetros de conexión a PostgreSQL
    String url = "jdbc:postgresql://localhost:5432/"; // <-- Cambia según tu setup
    String usuario = "postgres";                      // <-- Cambia por tu usuario
    String contraseña = "";                       // <-- Cambia por tu contraseña

        try {
            // Extraer nombre de la base de datos del SQL generado (asumimos CREATE DATABASE ...)
            String[] lineas = sql.split("\n");
            String nombreBD = null;
            for (String linea : lineas) {
                if (linea.toLowerCase().startsWith("create database")) {
                    nombreBD = linea.split(" ")[2].trim();
                    break;
                }
            }

            if (nombreBD == null) {
                areaErrores.setText("No se encontró el nombre de la base de datos en el SQL.");
                return;
            }

            // Conexión a la base de datos principal para crear la nueva BD
            Connection conn = DriverManager.getConnection(url + "postgres", usuario, contraseña);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("DROP DATABASE IF EXISTS " + nombreBD);
            stmt.executeUpdate("CREATE DATABASE " + nombreBD);
            stmt.close();
            conn.close();

            // Conectarse a la base de datos recién creada y ejecutar el resto del SQL
            conn = DriverManager.getConnection(url + nombreBD, usuario, contraseña);
            stmt = conn.createStatement();

            // Ejecutar cada sentencia individual (separada por ;)
            String[] sentencias = sql.split(";");
            for (String sentencia : sentencias) {
                if (!sentencia.trim().isEmpty() && !sentencia.toLowerCase().contains("create database")) {
                    stmt.execute(sentencia + ";");
                }
            }

            stmt.close();
            conn.close();

            JOptionPane.showMessageDialog(this, "¡Base de datos creada con éxito en PostgreSQL!");

            } catch (SQLException e) {
                areaErrores.setText("Error SQL:\n" + e.getMessage());
                e.printStackTrace();
            }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new IDEGeneradorBD().setVisible(true);
        });
    }
}
