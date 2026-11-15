package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.UsuarioService;
import madstodolist.service.UsuarioServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
public class RegistradoController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private ManagerUserSession managerUserSession;

    @GetMapping("/registrados/{id}")
    public String detallesRegistrado(@PathVariable(value="id") Long id, Model model, HttpSession session) {
        UsuarioData usuario = usuarioService.findById(id);
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }

        Long usuarioLogeado = managerUserSession.usuarioLogeado();
        if (usuarioLogeado == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        UsuarioData usuarioActual = usuarioService.findById(usuarioLogeado);
        boolean esAdmin = usuarioActual != null && usuarioActual.isAdmin();
        if (!esAdmin && !usuarioLogeado.equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("EsAdmin", esAdmin);
        model.addAttribute("esPropioUsuario", usuarioLogeado.equals(id));
        return "detallesRegistrado";
    }

    @PostMapping("/registrados/{id}/editar")
    public String editarRegistrado(@PathVariable(value="id") Long id,
                                   @RequestParam(value="nombre", required=false) String nombre,
                                   @RequestParam(value="email", required=false) String email,
                                   @RequestParam(value="password", required=false) String password,
                                   RedirectAttributes flash) {
        // Solo el propio usuario puede editar sus datos (ni siquiera un admin puede editar a otro)
        Long usuarioLogeado = managerUserSession.usuarioLogeado();
        if (usuarioLogeado == null || !usuarioLogeado.equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        UsuarioData actual = usuarioService.findById(id);
        if (actual == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }

        UsuarioData datos = new UsuarioData();
        datos.setId(id);
        datos.setNombre(nombre);
        datos.setEmail(email);
        // Si no se escribe una contraseña nueva se mantiene la actual
        datos.setPassword(password == null || password.isEmpty() ? actual.getPassword() : password);

        try {
            UsuarioData editado = usuarioService.editarUsuario(datos);
            managerUserSession.logearUsuario(editado.getId(), editado.getNombre(), editado.isAdmin());
            flash.addFlashAttribute("mensaje", "Datos actualizados correctamente");
        } catch (UsuarioServiceException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/registrados/" + id;
    }

    @GetMapping("/registrados")
    public String listaRegistrados(Model model, HttpSession session) {
        Long usuarioLogeado = managerUserSession.usuarioLogeado();
        if (usuarioLogeado == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        UsuarioData usuarioActual = usuarioService.findById(usuarioLogeado);
        if (usuarioActual == null || !usuarioActual.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        // Pasa la lista de usuarios al modelo
        model.addAttribute("usuarios", usuarioService.findAll());
        return "listaRegistrados";
    }

    @PostMapping("/registrados/{id}/bloqueo")
    public String cambiarBloqueo(@PathVariable(value="id") Long id) {
        comprobarAdministrador();
        usuarioService.cambiarBloqueo(id);
        return "redirect:/registrados";
    }

    @PostMapping("/registrados/{id}/bloquear")
    public String bloquear(@PathVariable(value="id") Long id) {
        comprobarAdministrador();
        usuarioService.bloquear(id);
        return "redirect:/registrados";
    }

    @PostMapping("/registrados/{id}/habilitar")
    public String habilitar(@PathVariable(value="id") Long id) {
        comprobarAdministrador();
        usuarioService.habilitar(id);
        return "redirect:/registrados";
    }

    private void comprobarAdministrador() {
        Long usuarioLogeado = managerUserSession.usuarioLogeado();
        UsuarioData usuarioActual = usuarioLogeado == null ? null : usuarioService.findById(usuarioLogeado);
        if (usuarioActual == null || !usuarioActual.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }
    }
}