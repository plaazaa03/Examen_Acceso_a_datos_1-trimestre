import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    private static String nombreBD;
    private static String usuario;
    private static String contraseña;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Solicitar información de la base de datos al usuario
        System.out.print("Introduce la ruta de los ficheros XML: ");
        String ruta = scanner.nextLine();
        System.out.print("Nombre BD: ");
        nombreBD = scanner.nextLine();
        System.out.print("Usuario: ");
        usuario = scanner.nextLine();
        System.out.print("Contraseña: ");
        contraseña = scanner.nextLine();

        Conexion conexion = new Conexion("jdbc:mysql://localhost/" + nombreBD + "?serverTimezone=UTC", usuario, contraseña);

        try {
            // Establecer conexión con la base de datos
            Connection conexionExterna = conexion.obtenerConexion();
            System.out.println("Conectado a la base de datos");

            // Procesar tablas
            procesarTablas(ruta, conexionExterna);

            // Cerrar la conexión externa
            conexionExterna.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static void procesarTablas(String ruta, Connection conexionExterna) {
        try {
            // Obtener metadata de la base de datos para obtener las tablas
            java.sql.DatabaseMetaData metaData = conexionExterna.getMetaData();
            java.sql.ResultSet tablas = metaData.getTables(nombreBD, null, "%", null);

            // Iterar sobre las tablas de la base de datos
            while (tablas.next()) {
                // Obtener el nombre de la tabla actual
                String nombreTabla = tablas.getString(3);
                // Construir el nombre del archivo XML asociado a la tabla
                String nombreArchivoXML = ruta + File.separator + nombreTabla + ".xml";

                procesarArchivoXML(nombreArchivoXML, nombreTabla, conexionExterna);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener las tablas: " + e.getMessage());
        }
    }

    private static void procesarArchivoXML(String nombreArchivoXML, String nombreTabla, Connection conexionExterna) {
        try {
            ProcesarXML procesarXml = new ProcesarXML(nombreArchivoXML, nombreTabla, nombreBD, usuario, contraseña);
            procesarXml.procesarXML(conexionExterna);
        } catch (Exception e) {
            System.out.println("Error al procesar el archivo " + nombreArchivoXML + ": " + e.getMessage());
        }
    }
}
