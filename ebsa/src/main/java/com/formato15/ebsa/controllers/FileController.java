package com.formato15.ebsa.controllers;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.formato15.ebsa.clases.Cuenta;
import com.formato15.ebsa.clases.FormatoSiecDTO;
import com.formato15.ebsa.clases.Usuario;
import com.formato15.ebsa.repository.UsuarioRepositorio;
import com.formato15.ebsa.service.CuentaService;
import com.formato15.ebsa.service.DataService;
import com.formato15.ebsa.service.Formato15Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


@RestController
@RequestMapping("/api/excel")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final Formato15Service formato15Service;

    public FileController(Formato15Service formato15Service) {
        this.formato15Service = formato15Service;
    }

    // Códigos de ciudades permitidos
    private static final Set<String> CODES_DEPARTAMENTO_15 = new HashSet<>(Arrays.asList(
            "001", "1", "022", "047", "051", "087", "090", "092", "097", "104", "106",
            "109", "114", "131", "135", "162", "172", "176", "180", "183", "185",
            "187", "189", "204", "212", "215", "218", "223", "224", "226", "232",
            "236", "238", "244", "248", "272", "276", "293", "296", "299", "317",
            "322", "325", "332", "362", "367", "368", "377", "380", "401", "403",
            "407", "425", "442", "455", "464", "466", "469", "476", "480", "491",
            "494", "500", "507", "511", "514", "516", "518", "522", "531", "533",
            "537", "542", "550", "572", "580", "599", "600", "621", "632", "638",
            "646", "660", "664", "667", "673", "676", "681", "686", "690", "693",
            "696", "720", "723", "740", "753", "755", "757", "759", "761", "762",
            "763", "764", "774", "776", "778", "790", "798", "804", "806", "808",
            "810", "814", "816", "820", "822", "832", "835", "837", "839", "842",
            "861", "879", "897"));

    private static final Set<String> CODES_DEPARTAMENTO_68 = new HashSet<>(Arrays.asList(
            "001", "1", "013", "020", "051", "077", "079", "081", "092", "101", "121",
            "132", "147", "152", "160", "162", "167", "169", "176", "179", "190",
            "207", "209", "211", "217", "229", "235", "245", "250", "255", "264",
            "266", "271", "276", "296", "298", "307", "318", "320", "322", "324",
            "327", "344", "368", "370", "377", "385", "397", "406", "418", "425",
            "432", "444", "464", "468", "498", "500", "502", "522", "524", "533",
            "547", "549", "572", "573", "575", "615", "655", "669", "673", "679",
            "682", "684", "686", "689", "705", "720", "745", "755", "770", "773",
            "780", "820", "855", "861", "867", "872", "895"));

    private static final List<Integer> CODIGOS_DETALLE_CAUSAL_P = Arrays.asList(303, 304, 305, 306);
    private static final List<Integer> CODIGOS_DETALLE_CAUSAL_F = Arrays.asList(101, 102, 103, 104, 105, 106, 107, 108,
            109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124);


    // Variable temporal para almacenar los datos guardados
    private List<Map<String, String>> savedData = new ArrayList<>();


    @GetMapping("/loadFromFile")
    public ResponseEntity<List<Map<String, String>>> loadFile() {
        try {
            List<Map<String, String>> fileData = formato15Service.readFileFromDirectory();
            return ResponseEntity.ok(fileData);
        } catch (Exception e) {
            log.error("Error al cargar el archivo: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }


    // @Autowired
    // private Formato15Service formato15Service;

    @GetMapping("/findFullInformation")
    public ResponseEntity<List<FormatoSiecDTO>> findFullInformation(
            @RequestParam("ano") Integer ano,
            @RequestParam("mes") Integer mes) {
        try {
            List<FormatoSiecDTO> results = formato15Service.findFullInformation(ano, mes);
            if (results.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }


    @Autowired
    private DataService dataService;

    @Autowired
    private CuentaService cuentaService;
    

    // @RestController
    // @RequestMapping("/api/auth")
    // public class AuthController {

    //     @PostMapping("/login")
    //     public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    //         if ("admin".equals(request.getUsername()) && "password".equals(request.getPassword())) {
    //             String token = "fake-jwt-token"; // Generar token real con JWT
    //             return ResponseEntity.ok(Map.of("success", true, "token", token));
    //         }
    //         return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Credenciales incorrectas"));
    //     }
    // }

    // // Clase para recibir las credenciales
    // @Data
    // public class LoginRequest {
    //     private String username;
    //     private String password;
    // }

    @RestController
    @RequestMapping("/api/auth")
    public class AuthController {

        @Autowired
        private UsuarioRepositorio usuarioRepositorio;

        // @PostMapping("/login")
        // public ResponseEntity<?> login(@RequestBody Usuario request) {
        //     String usuario = request.getUsuario();
        //     String contrasena = request.getContrasena();

        //     // Log: Datos recibidos del cliente
        //     System.out.println("Login attempt: Usuario = " + usuario + ", Contraseña = " + contrasena);

        //     // Buscar el usuario en la base de datos
        //     Optional<Usuario> usuarioOptional = usuarioRepositorio.findByUsuario(usuario);

        //     if (usuarioOptional.isPresent()) {
        //         Usuario usuarios = usuarioOptional.get();

        //         // Log: Usuario encontrado
        //         System.out.println("Usuario encontrado: " + usuarios.getUsuario());
        //         System.out.println("Contraseña almacenada: " + usuarios.getContrasena());

        //         // Validar la contraseña
        //         if (contrasena.equals(usuarios.getContrasena())) {
        //             // Log: Contraseña válida
        //             System.out.println("Contraseña válida para el usuario: " + usuario);

        //             // Generar token (aquí puedes implementar JWT real)
        //             String token = "fake-jwt-token";

        //             // Log: Autenticación exitosa
        //             System.out.println("Autenticación exitosa. Generando token...");
        //             return ResponseEntity.ok(Map.of("success", true, "token", token));
        //         } else {
        //             // Log: Contraseña incorrecta
        //             System.out.println("Contraseña incorrecta para el usuario: " + usuario);
        //             return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        //                     .body(Map.of("success", false, "message", "Contraseña incorrecta."));
        //         }
        //     } else {
        //         // Log: Usuario no encontrado
        //         System.out.println("Usuario no encontrado: " + usuario);
        //         return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        //                 .body(Map.of("success", false, "message", "Usuario no encontrado."));
        //     }
        // }
        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody Usuario request) {
            String username = request.getUsuario();
            String password = request.getContrasena();

            Optional<Usuario> usuarioOptional = usuarioRepositorio.findByUsuario(username);

            if (usuarioOptional.isPresent()) {
                Usuario usuario = usuarioOptional.get();

                if (password.equals(usuario.getContrasena())) {
                    //System.out.println("Autenticación exitosa. Generando token...");
                    String token = "fake-jwt-token"; // Cambiar por un token real
                    return ResponseEntity.ok(Map.of("success", true, "token", token));
                } else {
                    System.out.println("Contraseña incorrecta.");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("success", false, "message", "Contraseña incorrecta."));
                }
            } else {
                System.out.println("Usuario no encontrado: " + username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Usuario no encontrado."));
            }
        }

    }



    @PostMapping("/validateAndSaveFile")
    public ResponseEntity<?> validateAndSaveFile(@RequestBody List<Map<String, String>> editedData) {
        // Mapa de equivalencias para encabezados
        Map<String, String> headerMappings = Map.ofEntries(
            Map.entry("Departamento DANE", "daneDpto"),
            Map.entry("Ciudad DANE", "daneMpio"),
            Map.entry("Asentamiento", "daneAsentamiento"),
            Map.entry("Radicado Recibido", "radicadoRecibido"),
            Map.entry("Fecha y Hora Radicación", "fechaReclamacion"),
            Map.entry("Tipo trámite", "tipoTramite"),
            Map.entry("Grupo Causal", "grupoCausal"),
            Map.entry("Detalle Causal", "detalleCausal"),
            Map.entry("es>Account Number", "niu"),
            Map.entry("Número Factura", "idFactura"),
            Map.entry("Tipo Respuesta", "tipoRespuesta"),
            Map.entry("Fecha Respuesta", "fechaRespuesta"),
            Map.entry("Radicado Respuesta", "radicadoRespuesta"),
            Map.entry("Fecha Notificación", "fechaNotificacion"),
            Map.entry("Tipo Notificación", "tipoNotificacion"),
            Map.entry("Fecha Transferencia SSPD", "fechaTrasladoSspd")
        );

        // Normalizar los datos con encabezados consistentes
        List<Map<String, String>> normalizedData = editedData.stream()
            .map(row -> normalizeRow(row, headerMappings))
            .collect(Collectors.toList());

        this.savedData = new ArrayList<>(normalizedData);

        // Validación de datos
        for (Map<String, String> rowData : savedData) {
            String departamentoDANEValue = rowData.get("daneDpto");
            String ciudadDANEValue = rowData.get("daneMpio");
            String grupoCausal = rowData.get("grupoCausal");
            String detalleCausalStr = rowData.get("detalleCausal");
            String accountNumber = rowData.get("niu");

            // Validar que accountNumber no sea nulo
            if (accountNumber == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("El número de cuenta no puede ser nulo.");
            }

            // Obtener los primeros 6 dígitos del número de cuenta
            Long matricula;
            try {
                matricula = Long.parseLong(accountNumber.substring(0, 6)); // Asumiendo que es un Long
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("El número de cuenta debe ser numérico y tener al menos 6 dígitos.");
            }

            Integer departamentoDANE;
            Integer ciudadDANE;
            try {
                departamentoDANE = Integer.parseInt(departamentoDANEValue); // Asumiendo que es un Integer
                ciudadDANE = Integer.parseInt(ciudadDANEValue); // Asumiendo que es un Integer
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("El código del departamento y el código de la ciudad deben ser numéricos.");
            }

            // Validar Departamento y Ciudad
            if (departamentoDANEValue == null || "0".equals(departamentoDANEValue)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("El código del departamento no puede ser nulo o igual a 0.");
            }
            if ("15".equals(departamentoDANEValue) && !CODES_DEPARTAMENTO_15.contains(ciudadDANEValue)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(String.format("El código de ciudad %s no es válido para el departamento 15.", ciudadDANEValue));
            } else if ("68".equals(departamentoDANEValue) && !CODES_DEPARTAMENTO_68.contains(ciudadDANEValue)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(String.format("El código de ciudad %s no es válido para el departamento 68.", ciudadDANEValue));
            }

            // Consultar en la base de datos los datos asociados a la matrícula
            Optional<Cuenta> cuentaOptional = cuentaService.getCuentaPorMatricula(matricula);

            if (cuentaOptional.isPresent()) {
                Cuenta cuenta = cuentaOptional.get();

                // Validar y corregir los datos de departamento y ciudad
                if (!cuenta.getDepartamento().equals(departamentoDANE) || !cuenta.getMunicipio().equals(ciudadDANE)) {
                    // Corregir los valores en rowData
                    rowData.put("daneDpto", String.valueOf(cuenta.getDepartamento()));
                    rowData.put("daneMpio", String.valueOf(cuenta.getMunicipio()));

                    // Registrar un mensaje de advertencia en los logs
                    log.warn(String.format(
                            "Fila con número de cuenta %s: El Departamento DANE (%s) o Ciudad DANE (%s) eran incorrectos. Se corrigieron automáticamente a Departamento: %s, Municipio: %s.",
                            accountNumber, departamentoDANEValue, ciudadDANEValue, cuenta.getDepartamento(), cuenta.getMunicipio()));
                }
            } else {
                // Si no se encuentra la matrícula en la base de datos, registrar un error
                log.error(String.format("No se encontró información para la matrícula %s en la base de datos.", matricula));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(String.format("No se encontró información para la matrícula %s en la base de datos.", matricula));
            }

            // Validación de Grupo Causal y Detalle Causal
            try {
                Integer detalleCausal = Integer.parseInt(detalleCausalStr);

                if ("P".equalsIgnoreCase(grupoCausal) && !CODIGOS_DETALLE_CAUSAL_P.contains(detalleCausal)) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error: Para el grupo causal 'P', el código de detalle causal debe ser uno de los siguientes: 303, 304, 305, 306.");
                } else if ("F".equalsIgnoreCase(grupoCausal) && !CODIGOS_DETALLE_CAUSAL_F.contains(detalleCausal)) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error: Para el grupo causal 'F', el código de detalle causal debe estar entre 101 y 124.");
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error: El valor de Detalle Causal debe ser un número entero.");
            }

            // Validación de fechas
            try {
                Date fechaRadicacion = parseDate(rowData.get("fechaReclamacion"));
                Date fechaRespuesta = parseDate(rowData.get("fechaRespuesta"));
                Date fechaNotificacion = parseDate(rowData.get("fechaNotificacion"));

                if (fechaRespuesta != null && fechaRadicacion != null && fechaRespuesta.before(fechaRadicacion)) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("La fecha de respuesta debe ser mayor o igual a la fecha y hora de radicación.");
                }
                if (fechaNotificacion != null && fechaRespuesta != null && fechaNotificacion.before(fechaRespuesta)) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("La fecha de notificación debe ser mayor o igual a la fecha de respuesta.");
                }
            } catch (ParseException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error al analizar las fechas. Asegúrese de que el formato de fecha sea correcto.");
            }
        }

        // Guardar datos temporalmente y generar el archivo de Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Hoja1");
            Row headerRow = sheet.createRow(0);
            Map<String, String> firstRow = savedData.get(0);
            int cellIndex = 0;

            for (String key : firstRow.keySet()) {
                Cell cell = headerRow.createCell(cellIndex++);
                cell.setCellValue(key);
            }

            int rowIndex = 1;
            for (Map<String, String> rowData : savedData) {
                Row row = sheet.createRow(rowIndex++);
                cellIndex = 0;
                for (String value : rowData.values()) {
                    Cell cell = row.createCell(cellIndex++);
                    cell.setCellValue(value);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            byte[] fileBytes = outputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "Formato15.xlsx");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al generar el archivo Excel.");
        }
    }

    // Método para normalizar las filas de datos
    private Map<String, String> normalizeRow(Map<String, String> row, Map<String, String> headerMappings) {
        return row.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> headerMappings.getOrDefault(entry.getKey(), entry.getKey()),
                Map.Entry::getValue
            ));
    }

    // Método para enviar los datos a la base de datos
    @PostMapping("/sendToDatabase")
    public ResponseEntity<String> sendDataToDatabase() {
        if (savedData == null || savedData.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No hay datos validados para enviar.");
        }

        try {
            dataService.saveData(savedData);
            return ResponseEntity.ok("Los datos han sido enviados exitosamente a la base de datos.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al guardar los datos en la base de datos.");
        }
    }


    // Método auxiliar para convertir una cadena de texto a un objeto Date
    private Date parseDate(String dateStr) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.parse(dateStr);
    }
}
