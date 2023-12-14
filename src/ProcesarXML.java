import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

public class ProcesarXML {
    private String nombreArchivoXML;
    private String nombreTabla;
    private String nombreBD;
    private String usuario;
    private String contraseña;

    public ProcesarXML(String nombreArchivoXML, String nombreTabla, String nombreBD, String usuario, String contraseña) {
        this.nombreArchivoXML = nombreArchivoXML;
        this.nombreTabla = nombreTabla;
        this.nombreBD = nombreBD;
        this.usuario = usuario;
        this.contraseña = contraseña;
    }

    public void procesarXML(Connection conexionExterna) {
        try {
            // Configurar el parser para XML
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document documento = dBuilder.parse(new File(nombreArchivoXML));
            documento.getDocumentElement().normalize();

            // Obtener la lista de nodos con el nombre de la tabla
            NodeList listaNodos = documento.getElementsByTagName(nombreTabla);

            // Establecer conexión interna a la base de datos
            Conexion conexionInterna = new Conexion("jdbc:mysql://localhost/" + nombreBD + "?serverTimezone=UTC", usuario, contraseña);
            Connection conexionInternaBD = conexionInterna.obtenerConexion();
            Statement sentencia = conexionInternaBD.createStatement();

            // Iterar sobre los nodos y realizar inserciones
            for (int i = 0; i < listaNodos.getLength(); i++) {
                // Obtener el elemento actual
                Element elemento = (Element) listaNodos.item(i);
                // Construir la sentencia de inserción
                StringBuilder consultaInsercion = new StringBuilder("INSERT INTO " + nombreTabla + " VALUES (");

                // Obtener atributos y agregar a la sentencia
                if (elemento.hasAttributes()) {
                    for (int j = 0; j < elemento.getAttributes().getLength(); j++) {
                        Node atributo = elemento.getAttributes().item(j);
                        consultaInsercion.append("'").append(atributo.getNodeValue()).append("',");
                    }
                }

                // Obtener nodos hijos y agregar a la sentencia
                for (int j = 0; j < elemento.getChildNodes().getLength(); j++) {
                    Node nodoHijo = elemento.getChildNodes().item(j);
                    if (nodoHijo.getNodeType() == Node.ELEMENT_NODE) {
                        consultaInsercion.append("'").append(nodoHijo.getTextContent()).append("',");
                    }
                }

                // Eliminar la última coma y cerrar la sentencia SQL
                consultaInsercion.setLength(consultaInsercion.length() - 1);
                consultaInsercion.append(")");

                // Ejecutar la sentencia SQL de inserción
                sentencia.executeUpdate(consultaInsercion.toString());
            }

            // Cerrar la conexión interna
            conexionInternaBD.close();

        } catch (Exception e) {
            System.out.println("Error al procesar el archivo " + nombreArchivoXML + ": " + e.getMessage());
        }
    }
}
