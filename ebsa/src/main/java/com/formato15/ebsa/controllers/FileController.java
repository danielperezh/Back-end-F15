package com.formato15.ebsa.controllers;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.formato15.ebsa.clases.Auditoria;
import com.formato15.ebsa.clases.Cuenta;
import com.formato15.ebsa.clases.FormatoSiecDTO;
// import com.formato15.ebsa.clases.Usuario;
import com.formato15.ebsa.repository.AuditoriaFormato15Repository;
// import com.formato15.ebsa.repository.UsuarioRepositorio;
// import com.formato15.ebsa.service.AuditoriaService;
import com.formato15.ebsa.service.CuentaService;
import com.formato15.ebsa.service.Formato15Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
// import jakarta.servlet.http.HttpSession;
// import jakarta.validation.Valid;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.beans.factory.annotation.Value;




@RestController
@RequestMapping("/api/excel")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final Formato15Service formato15Service;

    public FileController(Formato15Service formato15Service) {
        this.formato15Service = formato15Service;
    }

    // Códigos de ciudades permitidos
    // private static final Set<String> CODES_DEPARTAMENTO_15 = new HashSet<>(Arrays.asList(
    //         "001", "1", "022", "047", "051", "087", "090", "092", "097", "104", "106",
    //         "109", "114", "131", "135", "162", "172", "176", "180", "183", "185",
    //         "187", "189", "204", "212", "215", "218", "223", "224", "226", "232",
    //         "236", "238", "244", "248", "272", "276", "293", "296", "299", "317",
    //         "322", "325", "332", "362", "367", "368", "377", "380", "401", "403",
    //         "407", "425", "442", "455", "464", "466", "469", "476", "480", "491",
    //         "494", "500", "507", "511", "514", "516", "518", "522", "531", "533",
    //         "537", "542", "550", "572", "580", "599", "600", "621", "632", "638",
    //         "646", "660", "664", "667", "673", "676", "681", "686", "690", "693",
    //         "696", "720", "723", "740", "753", "755", "757", "759", "761", "762",
    //         "763", "764", "774", "776", "778", "790", "798", "804", "806", "808",
    //         "810", "814", "816", "820", "822", "832", "835", "837", "839", "842",
    //         "861", "879", "897"));

    // private static final Set<String> CODES_DEPARTAMENTO_68 = new HashSet<>(Arrays.asList(
    //         "001", "1", "013", "020", "051", "077", "079", "081", "092", "101", "121",
    //         "132", "147", "152", "160", "162", "167", "169", "176", "179", "190",
    //         "207", "209", "211", "217", "229", "235", "245", "250", "255", "264",
    //         "266", "271", "276", "296", "298", "307", "318", "320", "322", "324",
    //         "327", "344", "368", "370", "377", "385", "397", "406", "418", "425",
    //         "432", "444", "464", "468", "498", "500", "502", "522", "524", "533",
    //         "547", "549", "572", "573", "575", "615", "655", "669", "673", "679",
    //         "682", "684", "686", "689", "705", "720", "745", "755", "770", "773",
    //         "780", "820", "855", "861", "867", "872", "895"));

    private static final List<Integer> CODIGOS_DETALLE_CAUSAL_P = Arrays.asList(303, 304, 305, 306);
    private static final List<Integer> CODIGOS_DETALLE_CAUSAL_F = Arrays.asList(101, 102, 103, 104, 105, 106, 107, 108,
            109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124);


    // Variable temporal para almacenar los datos guardados
    private List<Map<String, String>> savedData = new ArrayList<>();


    @GetMapping("/loadFromFile")
    public ResponseEntity<List<Map<String, String>>> loadFile(
            @RequestParam("year") String year,
            @RequestParam("month") String month) {
        try {
            List<Map<String, String>> fileData = formato15Service.readFileFromDirectory(year, month);
            return ResponseEntity.ok(fileData);
        } catch (FileNotFoundException e) {
            log.warn("Archivo no encontrado para el año {} y mes {}: {}", year, month, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        } catch (Exception e) {
            log.error("Error al cargar el archivo: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }


    @GetMapping("/findFullInformation")
    public ResponseEntity<?> findFullInformation(
            @RequestParam("ano") Integer ano,
            @RequestParam("mes") Integer mes) {
        try {
            List<FormatoSiecDTO> results = formato15Service.findFullInformation(ano, mes);

            if (results.isEmpty()) {
                // Devolver un mensaje de error si no hay resultados
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "No se encontraron datos para la fecha indicada.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            // Manejar cualquier otro error
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ocurrió un error al procesar la solicitud.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    

    @Autowired
    private CuentaService cuentaService;
    

    @CrossOrigin(origins = {"http://formato15.ebsa.com.co:8080", "http://formato15.ebsa.com.co:8086", "https://formato15.ebsa.com.co:8082", "http://localhost:8080"})
    @RestController
    @RequestMapping("/api/auth")
    public class AuthController {

        @Value("${spring.datasource.url}")
        private String databaseUrl;

        @Value("${jwt.secret}")
        private String jwtSecret;

        @Value("${jwt.expiration}")
        private long jwtExpiration; 

        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
            String username = request.get("usuario");
            String password = request.get("contrasena");

            if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Usuario y contraseña son obligatorios."));
            }

            System.out.println("Intento de inicio de sesión para el usuario: " + username);

            if (validateOracleUser(username, password)) {
                String token = generateJwtToken(username);

                // Configurar el contexto de seguridad de Spring
                List<GrantedAuthority> authorities = List.of(); // Define roles si es necesario
                Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Confirmar autenticación exitosa
                System.out.println("Autenticación exitosa para el usuario: " + username);
                //System.out.println("Token generado: " + token);

                return ResponseEntity.ok(Map.of("success", true, "token", token));
            } else {
                System.out.println("Error: Autenticación fallida para el usuario: " + username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Usuario o contraseña incorrectos."));
            }
        }

        private boolean validateOracleUser(String username, String password) {
            try {
                Connection connection = DriverManager.getConnection(databaseUrl, username, password);
                System.out.println("Conexión exitosa a Oracle para el usuario: " + username);
                connection.close();
                return true;
            } catch (SQLException e) {
                System.out.println("Error al conectar con Oracle: " + e.getMessage());
                System.out.println("Error al conectar con Oracle para el usuario: " + username);
                return false;
            }
        }

        private String generateJwtToken(String username) {
            // Generar una clave secreta
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    
            // Crear el token JWT
            String token = Jwts.builder()
                    .setSubject(username)
                    .claim("nombre", username)
                    .setIssuedAt(new Date()) // Fecha de emisión
                    .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration * 1000)) // Expiración
                    .signWith(key, SignatureAlgorithm.HS256) // Firma con la clave secreta
                    .compact();
    
            System.out.println("JWT generado para el usuario: " + username);
            return token;
        }
    }

    @Autowired
    private AuditoriaFormato15Repository auditoriaRepository;

    // @Autowired
    // private AuditoriaService auditoriaService;


    @PostMapping("/validateAndSaveFile")
    public ResponseEntity<?> validateAndSaveFile(@RequestBody List<Map<String, String>> editedData, @RequestParam(required = false, defaultValue = "json") String returnType) {

        //Obtener el usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String usuarioLogueado = authentication != null ? authentication.getName() : "UsuarioDesconocido";

        System.out.println("-- Usuario --: " + usuarioLogueado);
         
         

        // Mapa de equivalencias para encabezados
        Map<String, String> headerMappings = Map.ofEntries(
            Map.entry("columna_1", "daneDpto"),
            Map.entry("columna_2", "daneMpio"),
            Map.entry("columna_3", "daneAsentamiento"),
            Map.entry("columna_4", "radicadoRecibido"),
            Map.entry("columna_5", "fechaReclamacion"),
            Map.entry("columna_6", "tipoTramite"),
            Map.entry("columna_7", "grupoCausal"),
            Map.entry("columna_8", "detalleCausal"),
            Map.entry("columna_9", "niu"),
            Map.entry("columna_10", "idFactura"),
            Map.entry("columna_11", "tipoRespuesta"),
            Map.entry("columna_12", "fechaRespuesta"),
            Map.entry("columna_13", "radicadoRespuesta"),
            Map.entry("columna_14", "fechaNotificacion"),
            Map.entry("columna_15", "tipoNotificacion"),
            Map.entry("columna_16", "fechaTrasladoSspd")
        );

        // Normalizar los datos con encabezados consistentes
        List<Map<String, String>> normalizedData = editedData.stream()
            .map(row -> normalizeRow(row, headerMappings))
            .collect(Collectors.toList());

        this.savedData = new ArrayList<>(normalizedData);

        // Validación de datos

        int rowIndex = 0; // Índice para las filas
        for (Map<String, String> rowData : savedData) {
            rowIndex++;
            String departamentoDANEValue = rowData.get("daneDpto");
            String ciudadDANEValue = rowData.get("daneMpio");
            String grupoCausal = rowData.get("grupoCausal");
            String detalleCausalStr = rowData.get("detalleCausal");
            String accountNumber = rowData.get("niu");
            String accion = "MODIFICAR"; // Obtén el usuario autenticado
            // String nombreArchivo = "formato15.xlsx"; 


            // Validar que accountNumber no sea nulo
            if (accountNumber == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("El número de cuenta no puede ser nulo.");
            }

            // Obtener los primeros 6 dígitos del número de cuenta
            Long matricula;
            try {
                matricula = Long.parseLong(accountNumber.substring(0, 6));
            } catch (NumberFormatException e) {
                String errorMessage = String.format("Error en la fila %d, columna 'Número de Cuenta': El número de cuenta debe ser numérico y tener al menos 6 dígitos.", rowIndex);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            }

            Integer departamentoDANE;
            Integer ciudadDANE;
            
            try {
                departamentoDANE = Integer.parseInt(departamentoDANEValue);
                ciudadDANE = Integer.parseInt(ciudadDANEValue);
            } catch (NumberFormatException e) {
                String errorMessage = String.format("Error en la fila %d: Los valores de 'Departamento DANE' o 'Ciudad DANE' deben ser numéricos.", rowIndex);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            }


            // Validar Departamento y Ciudad
            if (departamentoDANEValue == null || "0".equals(departamentoDANEValue)) {
                String errorMessage = String.format("Error en la fila %d, columna 'Departamento DANE': El código del departamento no puede ser nulo o igual a 0.", rowIndex);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
            }
            

            // Consultar en la base de datos los datos asociados a la matrícula
            Optional<Cuenta> cuentaOptional = cuentaService.getCuentaPorMatricula(matricula);
            

            // Validar discrepancias
            if (cuentaOptional.isPresent()) {
                Cuenta cuenta = cuentaOptional.get();
            
                boolean hasChanges = false; // Bandera para verificar si hubo cambios
            
                // Validar y corregir los datos de departamento y ciudad
                if (!cuenta.getDepartamento().equals(departamentoDANE)) {
                    // Registro para cambio en Departamento DANE
                    // Auditoria auditoriaDepartamento = new Auditoria();
                    // auditoriaDepartamento.setUsuario(usuarioLogueado);
                    // auditoriaDepartamento.setAccion(accion);
                    // auditoriaDepartamento.setCampoModificado("Departamento DANE");
                    // auditoriaDepartamento.setValorAnterior(departamentoDANEValue); 
                    // auditoriaDepartamento.setValorNuevo(String.valueOf(cuenta.getDepartamento())); 
                    // auditoriaDepartamento.setFechaModificacion(LocalDateTime.now());
                    // auditoriaRepository.save(auditoriaDepartamento);
            
                    hasChanges = true; // Indicar que hubo un cambio
                }
            
                if (!cuenta.getMunicipio().equals(ciudadDANE)) {
                    // Registro para cambio en Ciudad DANE
                    // Auditoria auditoriaMunicipio = new Auditoria();
                    // auditoriaMunicipio.setUsuario(usuarioLogueado);
                    // auditoriaMunicipio.setAccion(accion);
                    // auditoriaMunicipio.setCampoModificado("Ciudad DANE");
                    // auditoriaMunicipio.setValorAnterior(ciudadDANEValue); 
                    // auditoriaMunicipio.setValorNuevo(String.valueOf(cuenta.getMunicipio()));
                    // auditoriaMunicipio.setFechaModificacion(LocalDateTime.now());
                    // auditoriaRepository.save(auditoriaMunicipio);
            
                    hasChanges = true; // Indicar que hubo un cambio
                }
            
                // Solo corregir los datos en rowData si hubo cambios
                if (hasChanges) {
                    rowData.put("daneDpto", String.valueOf(cuenta.getDepartamento()));
                    rowData.put("daneMpio", String.valueOf(cuenta.getMunicipio()));
            
                    // Registrar un mensaje de advertencia en los logs
                    log.warn(String.format(
                            "Fila con número de cuenta %s: El Departamento DANE (%s) o Ciudad DANE (%s) eran incorrectos. Se corrigieron automáticamente a Departamento: %s, Municipio: %s.",
                            accountNumber, departamentoDANEValue, ciudadDANEValue, cuenta.getDepartamento(), cuenta.getMunicipio()));
                } else {
                    log.info(String.format("No hubo cambios en Departamento DANE (%s) ni Ciudad DANE (%s) para la cuenta %s.",
                            departamentoDANE, ciudadDANE, accountNumber));
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
                    // Registro de auditoría
                    // Auditoria auditoria = new Auditoria();
                    // auditoria.setUsuario(usuarioLogueado);
                    // auditoria.setAccion(accion);
                    // auditoria.setCampoModificado("Detalle Causal");
                    // auditoria.setValorAnterior(detalleCausalStr);
                    // auditoria.setValorNuevo(rowData.get("detalleCausal")); 
                    // auditoria.setFechaModificacion(LocalDateTime.now());
                    // auditoriaRepository.save(auditoria);

                    String errorMessage = String.format(
                    "Error en la fila <b>%d</b>, columna <b>'Detalle Causal'</b>: Para el grupo causal 'P', el código de detalle causal debe ser uno de los siguientes: 303, 304, 305, 306.",
                    rowIndex
                    );
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
                } else if ("F".equalsIgnoreCase(grupoCausal) && !CODIGOS_DETALLE_CAUSAL_F.contains(detalleCausal)) {
                    // Registro de auditoría
                    // Auditoria auditoria = new Auditoria();
                    // auditoria.setUsuario(usuarioLogueado);
                    // auditoria.setAccion(accion);
                    // auditoria.setCampoModificado("Detalle Causal");
                    // auditoria.setValorAnterior(detalleCausalStr);
                    // auditoria.setValorNuevo(rowData.get("detalleCausal"));
                    // auditoria.setFechaModificacion(LocalDateTime.now());
                    // auditoriaRepository.save(auditoria);

                    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    //         .body("Error: Para el grupo causal 'F', el código de detalle causal debe estar entre 101 y 124.");
                    
                    String errorMessage = String.format(
                    "Error en la fila <b>%d</b>, columna <b>'Detalle Causal'</b>: Para el grupo causal 'F', el código de detalle causal debe estar entre 101 y 124.",
                    rowIndex
                    );
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
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
                    // Registro de auditoría por inconsistencia en fechas
                    // Auditoria auditoria = new Auditoria();
                    // auditoria.setUsuario(usuarioLogueado);
                    // auditoria.setAccion("Modificación Fecha");
                    // auditoria.setCampoModificado("Fecha Respuesta");
                    // auditoria.setValorAnterior(rowData.get("fechaRespuesta"));
                    // auditoria.setValorNuevo(rowData.get("fechaRespuesta"));
                    // auditoria.setFechaModificacion(LocalDateTime.now());
                    // auditoriaRepository.save(auditoria);

                     // Agregar fila y columna al mensaje de error
                     String errorMessage = String.format(
                        "Error en la fila <b>%d</b>, columna '<b>Fecha Respuesta</b>': La fecha de respuesta debe ser mayor o igual a la fecha de radicación.",
                        rowIndex
                    );

                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
                }
                if (fechaNotificacion != null && fechaRespuesta != null && fechaNotificacion.before(fechaRespuesta)) {
                    // Registro de auditoría por inconsistencia en fechas
                    // Auditoria auditoria = new Auditoria();
                    // auditoria.setUsuario(usuarioLogueado);
                    // auditoria.setAccion("Modificación Fecha");
                    // auditoria.setCampoModificado("Fecha Notificación");
                    // auditoria.setValorAnterior(rowData.get("fechaNotificacion"));
                    // auditoria.setValorNuevo(rowData.get("fechaNotificacion"));
                    // auditoria.setFechaModificacion(LocalDateTime.now());
                    // auditoriaRepository.save(auditoria);

                    String errorMessage = String.format(
                    "Error en la fila <b>%d</b>, columna <b>'Fecha Notificación'</b>: La fecha de notificación debe ser mayor o igual a la fecha de respuesta.",
                    rowIndex
                    );
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
                }
            } catch (ParseException e) {

                String errorMessage = String.format("Error en la fila %d: Formato de fecha incorrecto.", rowIndex);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
                
            }

            // Validación de fechas
            // try {
            //     Date fechaRadicacion = parseDate(rowData.get("fechaReclamacion"));
            //     Date fechaRespuesta = parseDate(rowData.get("fechaRespuesta"));
            //     Date fechaNotificacion = parseDate(rowData.get("fechaNotificacion"));

            //     if (fechaRespuesta != null && fechaRadicacion != null && fechaRespuesta.before(fechaRadicacion)) {
            //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            //                 .body("La fecha de respuesta debe ser mayor o igual a la fecha y hora de radicación.");
            //     }
            //     if (fechaNotificacion != null && fechaRespuesta != null && fechaNotificacion.before(fechaRespuesta)) {
            //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            //                 .body("La fecha de notificación debe ser mayor o igual a la fecha de respuesta.");
            //     }
            // } catch (ParseException e) {
            //     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            //             .body("Error al analizar las fechas. Asegúrese de que el formato de fecha sea correcto.");
            // }
            
            
        }
        // Verificar el tipo de retorno solicitado
        if ("json".equalsIgnoreCase(returnType)) {
            // Retornar los datos validados en formato JSON
            return ResponseEntity.ok(savedData);
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

            // int rowIndex = 1;
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

    
    // private String getOldValueFromCuenta(String field, Cuenta cuenta) {
    //     switch (field) {
    //         case "daneDpto":
    //             return String.valueOf(cuenta.getDepartamento());
    //         case "daneMpio":
    //             return String.valueOf(cuenta.getMunicipio());
            
    //         default:
    //             return null; 
    //     }
    // }

    // Método para normalizar las filas de datos
    private Map<String, String> normalizeRow(Map<String, String> row, Map<String, String> headerMappings) {
        return row.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> headerMappings.getOrDefault(entry.getKey(), entry.getKey()),
                Map.Entry::getValue
            ));
    }

    public Date parseDate(String dateString) throws ParseException {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
    
        List<String> dateFormats = List.of("dd-MM-yyyy", "dd/MM/yyyy HH:mm:ss");
        ParseException parseException = null;
    
        for (String format : dateFormats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setLenient(false); // Evita fechas inválidas como 31-02-2023
                return sdf.parse(dateString);
            } catch (ParseException e) {
                parseException = e;
            }
        }
    
        // Si ninguno funcionó, lanza una excepción
        throw parseException;
    }
}
