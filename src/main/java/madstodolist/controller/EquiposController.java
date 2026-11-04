package madstodolist.controller;

import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Usuario;
import madstodolist.service.EquipoService;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class EquiposController {

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/equipos")
    public String listaEquipos(Model model, HttpSession session) {
        Long usuarioLogeado = (Long) session.getAttribute("idUsuarioLogeado");
        if (usuarioLogeado == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();

        model.addAttribute("equipos", equipos);
        return "formEquipos";
    }

    @PostMapping("/equipos/crear")
    public String crearEquipo(@RequestParam("nombre") String nombre, HttpSession session) {
        // Validación de usuario autenticado
        Long idUsuario = (Long) session.getAttribute("idUsuarioLogeado");
        if (idUsuario == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        // Creación del equipo en la capa de servicio
        equipoService.crearEquipo(nombre);

        // Redirección a la lista de equipos tras guardar
        return "redirect:/equipos";
    }
    @PostMapping("/equipos/{id}/unirse")
    public String unirseAEquipo(@PathVariable("id") Long idEquipo, HttpSession session) {
        Long idUsuario = (Long) session.getAttribute("idUsuarioLogeado");
        if (idUsuario == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        equipoService.añadirUsuarioAEquipo(idEquipo, idUsuario); // Ajusta al nombre real de tu método de servicio
        return "redirect:/equipos";
    }

    @PostMapping("/equipos/{id}/salir")
    public String salirDeEquipo(@PathVariable("id") Long idEquipo, HttpSession session) {
        Long idUsuario = (Long) session.getAttribute("idUsuarioLogeado");
        if (idUsuario == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        equipoService.eliminarUsuarioDelEquipo(idEquipo, idUsuario); // Ajusta al nombre real de tu método de servicio
        return "redirect:/equipos";
    }

    @PostMapping("/equipos/{id}/update")
    public String updateEquipo(@PathVariable("id") Long idEquipo, @RequestParam("nombre") String nombre, HttpSession session){
        Long idUsuario = (Long) session.getAttribute("idUsuarioLogeado");
        if (idUsuario==null){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        UsuarioData usuarioActual = usuarioService.findById(idUsuario);
        if (!usuarioActual.isAdmin()){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        equipoService.actualizarEquipo(idEquipo, nombre);

        return "redirect:/equipos";
    }

    @PostMapping("/equipos/{id}/delete")
    public String deleteEquipo(@PathVariable("id") Long idEquipo, HttpSession session){
        Long idUsuario = (Long) session.getAttribute("idUsuarioLogeado");
        if (idUsuario==null){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        UsuarioData usuarioActual = usuarioService.findById(idUsuario);
        if (!usuarioActual.isAdmin()){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        equipoService.eliminarEquipo(idEquipo);

        return "redirect:/equipos";

    }
}