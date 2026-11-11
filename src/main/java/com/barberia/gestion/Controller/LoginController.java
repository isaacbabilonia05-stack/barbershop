package com.barberia.gestion.Controller;

import com.barberia.gestion.model.*;
import com.barberia.gestion.Repository.*;
import com.barberia.gestion.service.CitaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class LoginController {

    private final ClienteRepository clienteRepository;
    private final CitaRepository citaRepository;
    private final BarberoRepository barberoRepository;
    private final ServicioRepository servicioRepository;
    private final CubiculoRepository cubiculoRepository;
    private final HorarioRepository horarioRepository;
    private final CitaService citaService;
    private final PasswordEncoder passwordEncoder;
    private final AdministradorRepository administradorRepository;

    public LoginController(ClienteRepository clienteRepository, CitaRepository citaRepository,
                           BarberoRepository barberoRepository, ServicioRepository servicioRepository,
                           CubiculoRepository cubiculoRepository, HorarioRepository horarioRepository,
                           CitaService citaService,
                           PasswordEncoder passwordEncoder,
                           AdministradorRepository administradorRepository) {
        this.clienteRepository = clienteRepository;
        this.citaRepository = citaRepository;
        this.barberoRepository = barberoRepository;
        this.servicioRepository = servicioRepository;
        this.cubiculoRepository = cubiculoRepository;
        this.horarioRepository = horarioRepository;
        this.citaService = citaService;
        this.passwordEncoder = passwordEncoder;
        this.administradorRepository = administradorRepository;
    }

    /** Formulario de login. El POST /login lo procesa Spring Security (formLogin en SecurityConfig). */
    @GetMapping("/login")
    public String mostrarLogin(HttpSession session) {
        Object rol = session.getAttribute("rol");
        if ("admin".equals(rol)) return "redirect:/clientes";
        if ("barbero".equals(rol)) return "redirect:/cita/pendientes";
        if ("cliente".equals(rol)) return "redirect:/menu";
        return "login";
    }

    // ---------- Validaciones sencillas de datos de usuario ----------
    private static final java.util.regex.Pattern EMAIL_RE =
            java.util.regex.Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    static String validarDatos(String nombre, String email, String telefono) {
        if (nombre == null || nombre.trim().length() < 2) return "El nombre es obligatorio (minimo 2 caracteres)";
        if (email == null || !EMAIL_RE.matcher(email.trim()).matches()) return "El email no es valido";
        if (telefono == null || !telefono.trim().matches("\\d{7,15}")) return "El telefono debe tener entre 7 y 15 digitos";
        return null;
    }

    static String validarPassword(String password) {
        if (password == null || password.length() < 8) return "La contrasena debe tener al menos 8 caracteres";
        return null;
    }

    @GetMapping("/menu")
    public String mostrarMenu(HttpSession session, Model model) {
        if (session.getAttribute("rol") == null) return "redirect:/login";
        model.addAttribute("nombre", session.getAttribute("clienteNombre"));
        return "menu";
    }

    @GetMapping("/perfil")
    public String mostrarPerfil(HttpSession session, Model model) {
        if (session.getAttribute("clienteId") == null) return "redirect:/login";
        Long id = (Long) session.getAttribute("clienteId");
        Cliente cliente = clienteRepository.findById(id).orElseThrow();
        model.addAttribute("cliente", cliente);
        return "perfil";
    }

    @PostMapping("/perfil")
    public String guardarPerfil(HttpSession session, @RequestParam String nombre, @RequestParam String email,
                                @RequestParam String telefono, @RequestParam(required = false) String password, Model model) {
        Long id = (Long) session.getAttribute("clienteId");
        Cliente cliente = clienteRepository.findById(id).orElseThrow();
        String errorDatos = validarDatos(nombre, email, telefono);
        if (errorDatos == null && password != null && !password.isEmpty()) errorDatos = validarPassword(password);
        if (errorDatos != null) {
            model.addAttribute("error", errorDatos);
            model.addAttribute("cliente", cliente);
            return "perfil";
        }
        email = email.trim(); telefono = telefono.trim(); nombre = nombre.trim();
        if (!cliente.getEmail().equals(email) && clienteRepository.existsByEmail(email)) {
            model.addAttribute("error", "Este email ya esta registrado");
            model.addAttribute("cliente", cliente);
            return "perfil";
        }
        if (!cliente.getTelefono().equals(telefono) && clienteRepository.existsByTelefono(telefono)) {
            model.addAttribute("error", "Este telefono ya esta registrado");
            model.addAttribute("cliente", cliente);
            return "perfil";
        }
        cliente.setNombre(nombre); cliente.setEmail(email); cliente.setTelefono(telefono);
        if (password != null && !password.isEmpty()) cliente.setPassword(passwordEncoder.encode(password));
        clienteRepository.save(cliente);
        session.setAttribute("clienteNombre", nombre);
        return "redirect:/menu";
    }

    @GetMapping("/registro")
    public String mostrarRegistro() { return "registro"; }

    @PostMapping("/registro")
    public String procesarRegistro(@RequestParam String nombre, @RequestParam String email,
                                   @RequestParam String telefono, @RequestParam String password,
                                   Model model) {
        String errorDatos = validarDatos(nombre, email, telefono);
        if (errorDatos == null) errorDatos = validarPassword(password);
        if (errorDatos != null) { model.addAttribute("error", errorDatos); return "registro"; }
        nombre = nombre.trim(); email = email.trim(); telefono = telefono.trim();
        if (clienteRepository.existsByEmail(email) || barberoRepository.existsByEmail(email)
                || administradorRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Este email ya esta registrado"); return "registro";
        }
        if (clienteRepository.existsByTelefono(telefono)) { model.addAttribute("error", "Este telefono ya esta registrado"); return "registro"; }
        // El registro publico SIEMPRE crea un CLIENTE: el rol nunca viene del formulario
        clienteRepository.save(new Cliente(nombre, email, telefono, passwordEncoder.encode(password)));
        return "redirect:/login?registrado";
    }

    @GetMapping("/clientes")
    public String listarClientes(Model model) {
        model.addAttribute("clientes", clienteRepository.findAll());
        return "clientes";
    }

    @GetMapping("/clientes/nuevo")
    public String mostrarNuevoCliente(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "cliente_nuevo";
    }

    @PostMapping("/clientes/nuevo")
    public String crearCliente(@RequestParam String nombre, @RequestParam String email,
                               @RequestParam String telefono, @RequestParam String password, Model model) {
        String errorDatos = validarDatos(nombre, email, telefono);
        if (errorDatos == null) errorDatos = validarPassword(password);
        if (errorDatos == null && (clienteRepository.existsByEmail(email.trim()) || barberoRepository.existsByEmail(email.trim())
                || administradorRepository.findByEmail(email.trim()).isPresent())) errorDatos = "Este email ya esta registrado";
        if (errorDatos == null && clienteRepository.existsByTelefono(telefono.trim())) errorDatos = "Este telefono ya esta registrado";
        if (errorDatos != null) {
            model.addAttribute("error", errorDatos);
            model.addAttribute("cliente", new Cliente(nombre, email, telefono, null));
            return "cliente_nuevo";
        }
        clienteRepository.save(new Cliente(nombre.trim(), email.trim(), telefono.trim(), passwordEncoder.encode(password)));
        return "redirect:/clientes?creado";
    }

    @GetMapping("/clientes/editar/{id}")
    public String mostrarEditar(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clienteRepository.findById(id).orElseThrow());
        return "editar";
    }

    @PostMapping("/clientes/editar/{id}")
    public String guardarEditar(@PathVariable Long id, @RequestParam String nombre,
                                @RequestParam String email, @RequestParam String telefono, Model model) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow();
        String errorDatos = validarDatos(nombre, email, telefono);
        if (errorDatos != null) {
            model.addAttribute("error", errorDatos); model.addAttribute("cliente", cliente); return "editar";
        }
        email = email.trim(); telefono = telefono.trim(); nombre = nombre.trim();
        if (!cliente.getEmail().equals(email) && clienteRepository.existsByEmail(email)) {
            model.addAttribute("error", "Este email ya esta registrado"); model.addAttribute("cliente", cliente); return "editar";
        }
        if (!cliente.getTelefono().equals(telefono) && clienteRepository.existsByTelefono(telefono)) {
            model.addAttribute("error", "Este telefono ya esta registrado"); model.addAttribute("cliente", cliente); return "editar";
        }
        cliente.setNombre(nombre); cliente.setEmail(email); cliente.setTelefono(telefono);
        clienteRepository.save(cliente);
        return "redirect:/clientes?editado";
    }

    /** Eliminar cambia datos: solo POST (con token CSRF), nunca GET. */
    @PostMapping("/clientes/eliminar/{id}")
    public String eliminarCliente(@PathVariable Long id) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow();
        citaRepository.deleteAll(citaRepository.findByCliente(cliente));
        clienteRepository.deleteById(id);
        return "redirect:/clientes?eliminado";
    }

    @GetMapping("/agendar")
    public String mostrarAgendar(HttpSession session, Model model) {
        if (session.getAttribute("rol") == null) return "redirect:/login";
        model.addAttribute("servicios", servicioRepository.findAll());
        model.addAttribute("barberos", barberoRepository.findByActivoTrue());
        model.addAttribute("cubiculos", cubiculoRepository.findByDisponibleTrue());
        return "agendar";
    }

    @PostMapping("/agendar")
    public String procesarCita(@RequestParam String servicioIds, @RequestParam String cantidades,
                               @RequestParam Long barberoId, @RequestParam(required = false) Long cubiculoId,
                               @RequestParam String fecha, @RequestParam String horaInicio,
                               HttpSession session, Model model) {
        Long clienteId = (Long) session.getAttribute("clienteId");
        if (clienteId == null) return "redirect:/login";
        if (barberoId == null || cubiculoId == null) {
            model.addAttribute("error", "Debes seleccionar barbero y cubículo");
            model.addAttribute("servicios", servicioRepository.findAll());
            model.addAttribute("barberos", barberoRepository.findByActivoTrue());
            model.addAttribute("cubiculos", cubiculoRepository.findByDisponibleTrue());
            return "agendar";
        }
        Cliente cliente = clienteRepository.findById(clienteId).orElseThrow();
        Barbero barbero = barberoRepository.findById(barberoId).orElseThrow();
        Cubiculo cubiculo = cubiculoRepository.findById(cubiculoId).orElseThrow();
        List<Long> ids = new ArrayList<>();
        List<Integer> cantidadesList = new ArrayList<>();
        if (servicioIds != null && !servicioIds.isEmpty()) {
            for (String id : servicioIds.split(",")) ids.add(Long.parseLong(id.trim()));
            for (String cant : cantidades.split(",")) cantidadesList.add(Integer.parseInt(cant.trim()));
        }
        if (ids.isEmpty()) {
            model.addAttribute("error", "Debes seleccionar al menos un servicio");
            model.addAttribute("servicios", servicioRepository.findAll());
            model.addAttribute("barberos", barberoRepository.findByActivoTrue());
            model.addAttribute("cubiculos", cubiculoRepository.findByDisponibleTrue());
            return "agendar";
        }
        List<Servicio> servicios = servicioRepository.findAllById(ids);
        LocalDate fechaCita = LocalDate.parse(fecha);
        LocalTime horaInicioCita = LocalTime.parse(horaInicio);
        double totalCalculado = 0;
        for (int i = 0; i < servicios.size(); i++) totalCalculado += servicios.get(i).getPrecio() * cantidadesList.get(i);
        try {
            Cita cita = citaService.crearCita(cliente, barbero, cubiculo, fechaCita, horaInicioCita, servicios, cantidadesList);
            model.addAttribute("cita", cita);
            model.addAttribute("nombre", session.getAttribute("clienteNombre"));
            model.addAttribute("total", totalCalculado);
            return "confirmacion";
        } catch (Exception e) {
            model.addAttribute("error", "Error al agendar: " + e.getMessage());
            model.addAttribute("servicios", servicioRepository.findAll());
            model.addAttribute("barberos", barberoRepository.findByActivoTrue());
            model.addAttribute("cubiculos", cubiculoRepository.findByDisponibleTrue());
            return "agendar";
        }
    }

    @GetMapping("/miscitas")
    public String verMisCitas(HttpSession session, Model model) {
        if (session.getAttribute("clienteId") == null) return "redirect:/login";
        Long clienteId = (Long) session.getAttribute("clienteId");
        Cliente cliente = clienteRepository.findById(clienteId).orElseThrow();
        model.addAttribute("citas", citaRepository.findByCliente(cliente));
        model.addAttribute("nombre", session.getAttribute("clienteNombre"));
        return "miscitas";
    }

    @PostMapping("/cancelarCita/{id}")
    public String cancelarCita(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("clienteId") == null) return "redirect:/login";
        Long clienteId = (Long) session.getAttribute("clienteId");
        Cita cita = citaRepository.findById(id).orElseThrow();
        if (!cita.getCliente().getId().equals(clienteId)) return "redirect:/miscitas";
        citaService.actualizarEstadoCita(id, EstadoCita.CANCELADA);
        return "redirect:/miscitas?cancelada=true";
    }

    @GetMapping("/calificarCita/{id}")
    public String mostrarCalificarCita(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("clienteId") == null) return "redirect:/login";
        Long clienteId = (Long) session.getAttribute("clienteId");
        Cita cita = citaRepository.findById(id).orElseThrow();
        if (!cita.getCliente().getId().equals(clienteId)) return "redirect:/miscitas";
        model.addAttribute("cita", cita);
        return "calificar";
    }

    @PostMapping("/calificarCita/{id}")
    public String guardarCalificacion(@PathVariable Long id, @RequestParam Integer calificacion, HttpSession session) {
        if (session.getAttribute("clienteId") == null) return "redirect:/login";
        Long clienteId = (Long) session.getAttribute("clienteId");
        Cita cita = citaRepository.findById(id).orElseThrow();
        // Solo el dueno de la cita puede calificarla, una sola vez, y solo si ya se completo
        if (!cita.getCliente().getId().equals(clienteId)
                || cita.getEstado() != EstadoCita.COMPLETADA
                || cita.getCalificacion() != null
                || calificacion < 1 || calificacion > 5) {
            return "redirect:/miscitas";
        }
        cita.setCalificacion(calificacion);
        citaRepository.save(cita);
        return "redirect:/miscitas?calificada=true";
    }

    @GetMapping("/admin/citas")
    public String verTodasCitas(HttpSession session, Model model) {
        model.addAttribute("citas", citaRepository.findAll());
        return "admincitas";
    }

    @GetMapping("/disponibilidad")
    @ResponseBody
    public List<String> getHorasDisponibles(@RequestParam Long barberoId, @RequestParam String fecha,
                                            @RequestParam(required = false) String servicioIds) {
        List<String> horas = new ArrayList<>();
        try {
            Barbero barbero = barberoRepository.findById(barberoId).orElseThrow();
            LocalDate fechaCita = LocalDate.parse(fecha);
            int diaSemana = fechaCita.getDayOfWeek().getValue();
            List<Horario> horarios = horarioRepository.findByBarberoAndDiaSemana(barbero, diaSemana);
            LocalTime inicio = LocalTime.of(8, 0);
            LocalTime fin = LocalTime.of(19, 0);
            if (!horarios.isEmpty()) { inicio = horarios.get(0).getHoraApertura(); fin = horarios.get(0).getHoraCierre(); }
            List<Cita> citasExistentes = citaRepository.findByBarberoAndFecha(barbero, fechaCita);
            List<LocalTime> horasOcupadas = new ArrayList<>();
            for (Cita cita : citasExistentes) horasOcupadas.add(cita.getHoraInicio());
            for (LocalTime hora = inicio; hora.isBefore(fin); hora = hora.plusMinutes(30))
                if (!horasOcupadas.contains(hora)) horas.add(hora.toString());
        } catch (Exception e) { e.printStackTrace(); }
        return horas;
    }

    @GetMapping("/horasOcupadas")
    @ResponseBody
    public List<String> getHorasOcupadas(@RequestParam Long barberoId, @RequestParam String fecha) {
        Barbero barbero = barberoRepository.findById(barberoId).orElseThrow();
        LocalDate fechaCita = LocalDate.parse(fecha);
        List<Cita> citas = citaRepository.findByBarberoAndFecha(barbero, fechaCita);
        List<String> horasOcupadas = new ArrayList<>();
        for (Cita cita : citas) horasOcupadas.add(cita.getHoraInicio().toString());
        return horasOcupadas;
    }

    // ✅ NUEVO: Horas ocupadas con estado (PENDIENTE/ACEPTADA)
    @GetMapping("/horasOcupadasConEstado")
    @ResponseBody
    public List<Map<String, String>> getHorasOcupadasConEstado(@RequestParam Long barberoId, @RequestParam String fecha) {
        Barbero barbero = barberoRepository.findById(barberoId).orElseThrow();
        LocalDate fechaCita = LocalDate.parse(fecha);
        List<Cita> citas = citaRepository.findByBarberoAndFecha(barbero, fechaCita);
        List<Map<String, String>> resultado = new ArrayList<>();
        for (Cita cita : citas) {
            Map<String, String> info = new HashMap<>();
            info.put("hora", cita.getHoraInicio().toString());
            info.put("estado", cita.getEstado().toString());
            resultado.add(info);
        }
        return resultado;
    }

    // ✅ NUEVO: Días libres por barbero
    @GetMapping("/diasLibres")
    @ResponseBody
    public List<String> getDiasLibres(@RequestParam Long barberoId) {
        Barbero barbero = barberoRepository.findById(barberoId).orElseThrow();
        String nombre = barbero.getNombre().toLowerCase();
        List<String> diasLibres = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        for (int i = -7; i < 42; i++) {
            LocalDate fecha = hoy.plusDays(i);
            int diaSemana = fecha.getDayOfWeek().getValue(); // 1=Lun, 3=Mié
            boolean esLibre = false;
            if (nombre.contains("isaac") && diaSemana == 1) esLibre = true;
            if (nombre.contains("victor") && diaSemana == 3) esLibre = true;
            if (esLibre) diasLibres.add(fecha.toString());
        }
        return diasLibres;
    }

    @GetMapping("/barbero/agenda/{barberoId}")
    @ResponseBody
    public List<Map<String, Object>> getAgendaBarbero(@PathVariable Long barberoId, HttpSession session) {
        if (!barberoId.equals(session.getAttribute("barberoId"))) {
            throw new org.springframework.security.access.AccessDeniedException("Solo puedes ver tu propia agenda");
        }
        Barbero barbero = barberoRepository.findById(barberoId).orElseThrow();
        List<Map<String, Object>> agenda = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate fecha = hoy.plusDays(i);
            int diaSemana = fecha.getDayOfWeek().getValue();
            List<Horario> horarios = horarioRepository.findByBarberoAndDiaSemana(barbero, diaSemana);
            List<Cita> citas = citaRepository.findByBarberoAndFecha(barbero, fecha);
            Map<String, Object> diaInfo = new HashMap<>();
            diaInfo.put("fecha", fecha.toString());
            diaInfo.put("diaSemana", diaSemana);
            diaInfo.put("ocupado", !citas.isEmpty());
            diaInfo.put("cantidadCitas", citas.size());
            diaInfo.put("horaInicio", horarios.isEmpty() ? "No labora" : horarios.get(0).getHoraApertura().toString());
            agenda.add(diaInfo);
        }
        return agenda;
    }

    @GetMapping("/admin/estadisticas")
    public String verEstadisticas(HttpSession session, Model model) {
        long citasPendientes = citaRepository.findAll().stream().filter(c -> c.getEstado() == EstadoCita.PENDIENTE).count();
        long citasAceptadas = citaRepository.findAll().stream().filter(c -> c.getEstado() == EstadoCita.ACEPTADA).count();
        long citasCompletadas = citaRepository.findAll().stream().filter(c -> c.getEstado() == EstadoCita.COMPLETADA).count();
        long citasCanceladas = citaRepository.findAll().stream().filter(c -> c.getEstado() == EstadoCita.CANCELADA).count();
        long citasRechazadas = citaRepository.findAll().stream().filter(c -> c.getEstado() == EstadoCita.RECHAZADA).count();
        long totalClientes = clienteRepository.count();
        Double ingresosTotales = citaRepository.findIngresosTotales();
        if (ingresosTotales == null) ingresosTotales = 0.0;
        Double calificacionPromedio = citaRepository.findCalificacionPromedio();
        if (calificacionPromedio == null) calificacionPromedio = 0.0;
        model.addAttribute("citasPendientes", citasPendientes);
        model.addAttribute("citasAceptadas", citasAceptadas);
        model.addAttribute("citasCompletadas", citasCompletadas);
        model.addAttribute("citasCanceladas", citasCanceladas);
        model.addAttribute("citasRechazadas", citasRechazadas);
        model.addAttribute("totalClientes", totalClientes);
        model.addAttribute("ingresosTotales", ingresosTotales);
        model.addAttribute("calificacionPromedio", calificacionPromedio);
        return "estadisticas";
    }
}