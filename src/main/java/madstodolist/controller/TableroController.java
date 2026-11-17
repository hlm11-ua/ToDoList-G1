package madstodolist.controller;

import madstodolist.dto.UsuarioData;
import madstodolist.service.TableroService;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;

@Controller
public class TableroController {

    @Autowired
    private TableroService tableroService;

    @Autowired
    private UsuarioService usuarioService;

    private void comprobarAdmin(HttpSession session) {
        Long idUsuario = (Long) session.getAttribute("idUsuarioLogeado");
        if (idUsuario == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        UsuarioData usuarioActual = usuarioService.findById(idUsuario);
        if (usuarioActual == null || !usuarioActual.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }
    }

    @PostMapping("/tableros/{id}/update")
    public String updateTablero(@PathVariable("id") Long idTablero,
                                @RequestParam("nombre") String nombre,
                                HttpSession session) {
        comprobarAdmin(session);

        Long equipoId = tableroService.obtenerEquipoIdDeTablero(idTablero);
        tableroService.modificarTablero(idTablero, nombre);

        return "redirect:/equipos/" + equipoId;
    }

    @PostMapping("/tableros/{id}/delete")
    public String deleteTablero(@PathVariable("id") Long idTablero,
                                HttpSession session) {
        comprobarAdmin(session);

        Long equipoId = tableroService.obtenerEquipoIdDeTablero(idTablero);
        tableroService.borrarTablero(idTablero);

        return "redirect:/equipos/" + equipoId;
    }
}