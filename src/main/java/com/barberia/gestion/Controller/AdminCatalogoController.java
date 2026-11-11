package com.barberia.gestion.Controller;

import com.barberia.gestion.Repository.*;
import com.barberia.gestion.model.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * CRUD de administracion sobre las colecciones servicios, barberos, cubiculos y citas.
 * Todas las rutas /admin/** exigen el rol ADMIN (ver SecurityConfig).
 * Crear/editar/eliminar se hacen siempre por POST (protegidos con token CSRF).
 */
@Controller
@RequestMapping("/admin")
public class AdminCatalogoController {

    private final ServicioRepository servicioRepository;
    private final BarberoRepository barberoRepository;
    private final CubiculoRepository cubiculoRepository;
    private final CitaRepository citaRepository;
    private final HorarioRepository horarioRepository;
    private final CubiculoBarberoRepository cubiculoBarberoRepository;
    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminCatalogoController(ServicioRepository servicioRepository, BarberoRepository barberoRepository,
                                   CubiculoRepository cubiculoRepository, CitaRepository citaRepository,
                                   HorarioRepository horarioRepository,
                                   CubiculoBarberoRepository cubiculoBarberoRepository,
                                   ClienteRepository clienteRepository,
                                   AdministradorRepository administradorRepository,
                                   PasswordEncoder passwordEncoder) {
        this.servicioRepository = servicioRepository;
        this.barberoRepository = barberoRepository;
        this.cubiculoRepository = cubiculoRepository;
        this.citaRepository = citaRepository;
        this.horarioRepository = horarioRepository;
        this.cubiculoBarberoRepository = cubiculoBarberoRepository;
        this.clienteRepository = clienteRepository;
        this.administradorRepository = administradorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =====================================================================
    //  SERVICIOS
    // =====================================================================

    @GetMapping("/servicios")
    public String listarServicios(Model model) {
        model.addAttribute("servicios", servicioRepository.findAll());
        return "admin_servicios";
    }

    @GetMapping("/servicios/nuevo")
    public String nuevoServicio(Model model) {
        model.addAttribute("servicio", new Servicio());
        model.addAttribute("tipos", Servicio.TipoServicio.values());
        return "admin_servicio_form";
    }

    @GetMapping("/servicios/editar/{id}")
    public String editarServicio(@PathVariable Long id, Model model) {
        model.addAttribute("servicio", servicioRepository.findById(id).orElseThrow());
        model.addAttribute("tipos", Servicio.TipoServicio.values());
        return "admin_servicio_form";
    }

    @PostMapping("/servicios/guardar")
    public String guardarServicio(@RequestParam(required = false) Long id,
                                  @RequestParam String nombre,
                                  @RequestParam(required = false) String descripcion,
                                  @RequestParam Double precio,
                                  @RequestParam Integer duracionMinutos,
                                  @RequestParam Servicio.TipoServicio tipo,
                                  Model model, RedirectAttributes flash) {
        Servicio servicio = (id == null) ? new Servicio() : servicioRepository.findById(id).orElseThrow();
        servicio.setNombre(nombre == null ? null : nombre.trim());
        servicio.setDescripcion(descripcion == null ? null : descripcion.trim());
        servicio.setPrecio(precio);
        servicio.setDuracionMinutos(duracionMinutos);
        servicio.setTipo(tipo);

        String error = null;
        if (servicio.getNombre() == null || servicio.getNombre().length() < 2) error = "El nombre es obligatorio";
        else if (precio == null || precio <= 0) error = "El precio debe ser mayor que 0";
        else if (duracionMinutos == null || duracionMinutos < 5 || duracionMinutos > 480)
            error = "La duracion debe estar entre 5 y 480 minutos";
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("servicio", servicio);
            model.addAttribute("tipos", Servicio.TipoServicio.values());
            return "admin_servicio_form";
        }
        servicioRepository.save(servicio);
        flash.addFlashAttribute("msg", id == null ? "Servicio creado" : "Servicio actualizado");
        return "redirect:/admin/servicios";
    }

    @PostMapping("/servicios/eliminar/{id}")
    public String eliminarServicio(@PathVariable Long id, RedirectAttributes flash) {
        Servicio servicio = servicioRepository.findById(id).orElseThrow();
        if (citaRepository.existsByServicioId(id)) {
            flash.addFlashAttribute("err", "No se puede eliminar \"" + servicio.getNombre() + "\": ya esta en citas agendadas");
        } else {
            servicioRepository.deleteById(id);
            flash.addFlashAttribute("msg", "Servicio eliminado");
        }
        return "redirect:/admin/servicios";
    }

    // =====================================================================
    //  BARBEROS
    // =====================================================================

    @GetMapping("/barberos")
    public String listarBarberos(Model model) {
        model.addAttribute("barberos", barberoRepository.findAll());
        return "admin_barberos";
    }

    @GetMapping("/barberos/nuevo")
    public String nuevoBarbero(Model model) {
        Barbero b = new Barbero();
        model.addAttribute("barbero", b);
        return "admin_barbero_form";
    }

    @GetMapping("/barberos/editar/{id}")
    public String editarBarbero(@PathVariable Long id, Model model) {
        model.addAttribute("barbero", barberoRepository.findById(id).orElseThrow());
        return "admin_barbero_form";
    }

    @PostMapping("/barberos/guardar")
    public String guardarBarbero(@RequestParam(required = false) Long id,
                                 @RequestParam String nombre,
                                 @RequestParam(required = false) String especialidad,
                                 @RequestParam String telefono,
                                 @RequestParam String email,
                                 @RequestParam(required = false) String password,
                                 @RequestParam(required = false) String imagenUrl,
                                 @RequestParam(defaultValue = "false") boolean activo,
                                 Model model, RedirectAttributes flash) {
        Barbero barbero = (id == null) ? new Barbero() : barberoRepository.findById(id).orElseThrow();
        barbero.setNombre(nombre == null ? null : nombre.trim());
        barbero.setEspecialidad(especialidad == null ? null : especialidad.trim());
        barbero.setTelefono(telefono == null ? null : telefono.trim());
        barbero.setEmail(email == null ? null : email.trim());
        barbero.setImagenUrl(imagenUrl == null || imagenUrl.isBlank() ? null : imagenUrl.trim());
        barbero.setActivo(activo);

        String error = LoginController.validarDatos(barbero.getNombre(), barbero.getEmail(), barbero.getTelefono());
        boolean cambiaPassword = password != null && !password.isEmpty();
        if (error == null && (id == null || cambiaPassword)) error = LoginController.validarPassword(password);
        if (error == null && emailEnUso(barbero.getEmail(), id)) error = "Este email ya esta registrado";
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("barbero", barbero);
            return "admin_barbero_form";
        }
        if (id == null || cambiaPassword) barbero.setPassword(passwordEncoder.encode(password));
        barberoRepository.save(barbero);
        flash.addFlashAttribute("msg", id == null ? "Barbero creado" : "Barbero actualizado");
        return "redirect:/admin/barberos";
    }

    @PostMapping("/barberos/eliminar/{id}")
    public String eliminarBarbero(@PathVariable Long id, RedirectAttributes flash) {
        Barbero barbero = barberoRepository.findById(id).orElseThrow();
        if (!citaRepository.findByBarbero(barbero).isEmpty()) {
            flash.addFlashAttribute("err", "\"" + barbero.getNombre()
                    + "\" tiene citas asociadas y no se puede eliminar. Desactivalo para ocultarlo.");
        } else {
            horarioRepository.deleteAll(horarioRepository.findByBarbero(barbero));
            cubiculoBarberoRepository.deleteAll(cubiculoBarberoRepository.findByBarbero(barbero));
            barberoRepository.deleteById(id);
            flash.addFlashAttribute("msg", "Barbero eliminado");
        }
        return "redirect:/admin/barberos";
    }

    /** El email debe ser unico entre administradores, barberos y clientes (es el usuario de login). */
    private boolean emailEnUso(String email, Long barberoIdActual) {
        if (clienteRepository.existsByEmail(email)) return true;
        if (administradorRepository.findByEmail(email).isPresent()) return true;
        return barberoRepository.findByEmail(email)
                .map(b -> barberoIdActual == null || !b.getId().equals(barberoIdActual))
                .orElse(false);
    }

    // =====================================================================
    //  CUBICULOS
    // =====================================================================

    @GetMapping("/cubiculos")
    public String listarCubiculos(Model model) {
        model.addAttribute("cubiculos", cubiculoRepository.findAll());
        return "admin_cubiculos";
    }

    @GetMapping("/cubiculos/nuevo")
    public String nuevoCubiculo(Model model) {
        Cubiculo c = new Cubiculo();
        c.setDisponible(true);
        model.addAttribute("cubiculo", c);
        return "admin_cubiculo_form";
    }

    @GetMapping("/cubiculos/editar/{id}")
    public String editarCubiculo(@PathVariable Long id, Model model) {
        model.addAttribute("cubiculo", cubiculoRepository.findById(id).orElseThrow());
        return "admin_cubiculo_form";
    }

    @PostMapping("/cubiculos/guardar")
    public String guardarCubiculo(@RequestParam(required = false) Long id,
                                  @RequestParam String nombre,
                                  @RequestParam(required = false) String ubicacion,
                                  @RequestParam(defaultValue = "false") boolean disponible,
                                  Model model, RedirectAttributes flash) {
        Cubiculo cubiculo = (id == null) ? new Cubiculo() : cubiculoRepository.findById(id).orElseThrow();
        cubiculo.setNombre(nombre == null ? null : nombre.trim());
        cubiculo.setUbicacion(ubicacion == null ? null : ubicacion.trim());
        cubiculo.setDisponible(disponible);
        if (cubiculo.getNombre() == null || cubiculo.getNombre().length() < 1) {
            model.addAttribute("error", "El nombre es obligatorio");
            model.addAttribute("cubiculo", cubiculo);
            return "admin_cubiculo_form";
        }
        cubiculoRepository.save(cubiculo);
        flash.addFlashAttribute("msg", id == null ? "Cubiculo creado" : "Cubiculo actualizado");
        return "redirect:/admin/cubiculos";
    }

    @PostMapping("/cubiculos/eliminar/{id}")
    public String eliminarCubiculo(@PathVariable Long id, RedirectAttributes flash) {
        Cubiculo cubiculo = cubiculoRepository.findById(id).orElseThrow();
        if (citaRepository.existsByCubiculoId(id)) {
            flash.addFlashAttribute("err", "\"" + cubiculo.getNombre()
                    + "\" tiene citas asociadas y no se puede eliminar. Marcalo como no disponible.");
        } else {
            cubiculoBarberoRepository.deleteAll(cubiculoBarberoRepository.findByCubiculo(cubiculo));
            cubiculoRepository.deleteById(id);
            flash.addFlashAttribute("msg", "Cubiculo eliminado");
        }
        return "redirect:/admin/cubiculos";
    }

    // =====================================================================
    //  CITAS (el admin puede ver todas y eliminar)
    // =====================================================================

    @PostMapping("/citas/eliminar/{id}")
    public String eliminarCita(@PathVariable Long id, RedirectAttributes flash) {
        citaRepository.findById(id).orElseThrow();
        citaRepository.deleteById(id);
        flash.addFlashAttribute("msg", "Cita eliminada");
        return "redirect:/admin/citas";
    }
}
