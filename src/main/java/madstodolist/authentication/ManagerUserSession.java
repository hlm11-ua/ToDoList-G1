package madstodolist.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;

@Component
public class ManagerUserSession {

    @Autowired
    HttpSession session;

    // Añadimos el id de usuario en la sesión HTTP para hacer
    // una autorización sencilla. En los métodos de controllers
    // comprobamos si el id del usuario logeado coincide con el obtenido
    // desde la URL
    public void logearUsuario(Long idUsuario, String nombreUsuario, boolean esAdmin) {
        session.setAttribute("idUsuarioLogeado", idUsuario);
        session.setAttribute("nombreUsuarioLogeado", nombreUsuario);
        session.setAttribute("esAdmin", esAdmin);
    }

    public Long usuarioLogeado() {
        return (Long) session.getAttribute("idUsuarioLogeado");
    }

    public boolean isAdmin() {
        Object admin = session.getAttribute("esAdmin");
        return admin instanceof Boolean && (Boolean) admin;
    }

    public void logout() {
        session.removeAttribute("idUsuarioLogeado");
        session.removeAttribute("nombreUsuarioLogeado");
        session.removeAttribute("esAdmin");
    }
}
