package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.controller.exception.TareaNotFoundException;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.TareaService;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import madstodolist.model.EstadoTarea;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class TareaController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    TareaService tareaService;

    @Autowired
    ManagerUserSession managerUserSession;

    private void comprobarUsuarioLogeado(Long idUsuario) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (!idUsuario.equals(idUsuarioLogeado))
            throw new UsuarioNoLogeadoException();
    }

    @GetMapping("/usuarios/{id}/tareas/nueva")
    public String formNuevaTarea(@PathVariable(value="id") Long idUsuario,
                                 @ModelAttribute TareaData tareaData, Model model,
                                 HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);
        return "formNuevaTarea";
    }

    @PostMapping("/usuarios/{id}/tareas/nueva")
    public String nuevaTarea(@PathVariable(value="id") Long idUsuario, @ModelAttribute TareaData tareaData,
                             Model model, RedirectAttributes flash,
                             HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);

        tareaService.nuevaTareaUsuario(idUsuario, tareaData.getTitulo());
        flash.addFlashAttribute("mensaje", "Tarea creada correctamente");
        return "redirect:/usuarios/" + idUsuario + "/tareas";
     }

    @GetMapping("/usuarios/{id}/tareas")
    public String listadoTareas(@PathVariable(value="id") Long idUsuario, Model model, HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);

        UsuarioData usuario = usuarioService.findById(idUsuario);
        List<TareaData> tareas = tareaService.allTareasUsuario(idUsuario);
        model.addAttribute("usuario", usuario);
        model.addAttribute("tareas", tareas);
        return "listaTareas";
    }

    @GetMapping("/tareas/{id}/editar")
    public String formEditaTarea(@PathVariable(value="id") Long idTarea, @ModelAttribute TareaData tareaData,
                                 Model model, HttpSession session) {

        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        comprobarUsuarioLogeado(tarea.getUsuarioId());

        model.addAttribute("tarea", tarea);
        tareaData.setTitulo(tarea.getTitulo());
        return "formEditarTarea";
    }

    @PostMapping("/tareas/{id}/editar")
    public String grabaTareaModificada(@PathVariable(value="id") Long idTarea, @ModelAttribute TareaData tareaData,
                                       Model model, RedirectAttributes flash, HttpSession session) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        Long idUsuario = tarea.getUsuarioId();

        comprobarUsuarioLogeado(idUsuario);

        tareaService.modificaTarea(idTarea, tareaData.getTitulo());
        flash.addFlashAttribute("mensaje", "Tarea modificada correctamente");
        return "redirect:/usuarios/" + tarea.getUsuarioId() + "/tareas";
    }

    @DeleteMapping("/tareas/{id}")
    @ResponseBody
    // La anotación @ResponseBody sirve para que la cadena devuelta sea la resupuesta
    // de la petición HTTP, en lugar de una plantilla thymeleaf
    public String borrarTarea(@PathVariable(value="id") Long idTarea, RedirectAttributes flash, HttpSession session) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        comprobarUsuarioLogeado(tarea.getUsuarioId());

        tareaService.borraTarea(idTarea);
        return "";
    }

    @PostMapping("/tableros/{id}/tareas/nueva")
    public String nuevaTareaTablero(@PathVariable("id") Long idTablero, @RequestParam("titulo") String titulo, @RequestParam("estado") EstadoTarea estado) {
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        tareaService.nuevaTareaTablero(idTablero, idUsuario, titulo, estado);

        return "redirect:/tableros/" + idTablero;
    }

    @PostMapping("/tableros/tareas/{id}/asignar")
    public String asignarUsuarioATarea(@PathVariable("id") Long idTarea, @RequestParam("usuarioId") Long usuarioId) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            throw new UsuarioNoLogeadoException();
        }

        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null || tarea.getTableroId() == null) {
            throw new TareaNotFoundException();
        }

        tareaService.asignarUsuarioATarea(idTarea, usuarioId);
        return "redirect:/tableros/" + tarea.getTableroId();
    }

    @PostMapping("/tableros/tareas/{id}/desasignar")
    public String desasignarUsuarioDeTarea(@PathVariable("id") Long idTarea) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            throw new UsuarioNoLogeadoException();
        }

        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null || tarea.getTableroId() == null) {
            throw new TareaNotFoundException();
        }

        tareaService.desasignarUsuarioDeTarea(idTarea);
        return "redirect:/tableros/" + tarea.getTableroId();
    }

    //para Modificar Estado de tareas (board de equipo y personal)
    @PostMapping("/tareas/{id}/estado")
    public String cambiarEstadoTarea(@PathVariable(value = "id") Long idTarea,
                                     @RequestParam("estado") EstadoTarea estado) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        if (tarea.getTableroId() != null) {
            // Tarea de tablero: cualquier usuario logeado puede moverla
            if (managerUserSession.usuarioLogeado() == null) {
                throw new UsuarioNoLogeadoException();
            }
            tareaService.modificaEstado(idTarea, estado);
            return "redirect:/tableros/" + tarea.getTableroId();
        }

        // Tarea personal: solo su dueño
        comprobarUsuarioLogeado(tarea.getUsuarioId());
        tareaService.modificaEstado(idTarea, estado);
        return "redirect:/usuarios/" + tarea.getUsuarioId() + "/tareas";
    }
}

