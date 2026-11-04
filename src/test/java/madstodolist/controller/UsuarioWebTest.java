package madstodolist.controller;

import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UsuarioWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    // Se añade el MockBean de EquipoService para que Thymeleaf pueda invocarlo sin llamar a la BD real
    @MockBean
    private EquipoService equipoService;

    @Test
    public void servicioLoginUsuarioOK() throws Exception {
        // GIVEN
        UsuarioData anaGarcia = new UsuarioData();
        anaGarcia.setNombre("Ana García");
        anaGarcia.setId(1L);

        when(usuarioService.login("ana.garcia@gmail.com", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.LOGIN_OK);
        when(usuarioService.findByEmail("ana.garcia@gmail.com"))
                .thenReturn(anaGarcia);

        // WHEN, THEN
        this.mockMvc.perform(post("/login")
                        .param("eMail", "ana.garcia@gmail.com")
                        .param("password", "12345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/1/tareas"));
    }

    @Test
    public void formularioRegistroMuestraSelectorSiNoHayAdmin() throws Exception {
        when(usuarioService.AreThereAnyAdmins()).thenReturn(false);

        this.mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"rol\"")));
    }

    @Test
    public void formularioRegistroOcultaSelectorSiYaExisteAdmin() throws Exception {
        when(usuarioService.AreThereAnyAdmins()).thenReturn(true);

        this.mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("id=\"rol\""))));
    }

    @Test
    public void servicioLoginUsuarioAdminRedirigeALaListaDeUsuarios() throws Exception {
        UsuarioData admin = new UsuarioData();
        admin.setId(99L);
        admin.setNombre("Admin");
        admin.setEmail("admin@ua");
        admin.setAdmin(true);

        when(usuarioService.login("admin@ua", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.LOGIN_OK);
        when(usuarioService.findByEmail("admin@ua"))
                .thenReturn(admin);

        this.mockMvc.perform(post("/login")
                        .param("eMail", "admin@ua")
                        .param("password", "12345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrados"));
    }

    @Test
    public void adminPuedeEntrarALaListaDeUsuarios() throws Exception {
        UsuarioData admin = new UsuarioData();
        admin.setId(99L);
        admin.setNombre("Admin");
        admin.setEmail("admin@ua");
        admin.setAdmin(true);

        when(usuarioService.login("admin@ua", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.LOGIN_OK);
        when(usuarioService.findByEmail("admin@ua"))
                .thenReturn(admin);
        when(usuarioService.findById(99L))
                .thenReturn(admin);
        when(usuarioService.findAll())
                .thenReturn(java.util.List.of(admin));

        MvcResult result = this.mockMvc.perform(post("/login")
                        .param("eMail", "admin@ua")
                        .param("password", "12345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrados"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);

        this.mockMvc.perform(get("/registrados").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Lista de Registrados")))
                .andExpect(content().string(containsString("/registrados/99/bloquear")))
                .andExpect(content().string(containsString("Bloquear")));
    }

    @Test
    public void servicioLoginUsuarioBloqueadoMuestraError() throws Exception {
        when(usuarioService.login("bloqueado@ua", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.USER_BLOCKED);

        this.mockMvc.perform(post("/login")
                        .param("eMail", "bloqueado@ua")
                        .param("password", "12345678"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("El usuario está bloqueado")));
    }

    @Test
    public void adminPuedeCambiarBloqueoDeUsuario() throws Exception {
        UsuarioData admin = new UsuarioData();
        admin.setId(99L);
        admin.setAdmin(true);
        when(usuarioService.findById(99L)).thenReturn(admin);

        this.mockMvc.perform(post("/registrados/1/bloqueo")
                        .session(new MockHttpSession()))
                .andExpect(status().isForbidden());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("idUsuarioLogeado", 99L);
        when(usuarioService.cambiarBloqueo(1L)).thenReturn(new UsuarioData());

        this.mockMvc.perform(post("/registrados/1/bloqueo").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registrados"));
    }

    @Test
    public void noSePuedeEntrarEnDetallesDeOtroUsuarioSiNoEsAdmin() throws Exception {
        UsuarioData usuarioVisitado = new UsuarioData();
        usuarioVisitado.setId(2L);
        usuarioVisitado.setNombre("Otro usuario");
        usuarioVisitado.setEmail("otro@ua");
        usuarioVisitado.setAdmin(false);

        UsuarioData usuarioLogeado = new UsuarioData();
        usuarioLogeado.setId(1L);
        usuarioLogeado.setNombre("Ana");
        usuarioLogeado.setEmail("ana@ua");
        usuarioLogeado.setAdmin(false);

        when(usuarioService.findById(2L)).thenReturn(usuarioVisitado);
        when(usuarioService.findById(1L)).thenReturn(usuarioLogeado);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("idUsuarioLogeado", 1L);
        session.setAttribute("nombreUsuarioLogeado", "Ana");
        session.setAttribute("esAdmin", false);

        MvcResult result = this.mockMvc.perform(get("/registrados/2").session(session))
                .andExpect(status().isForbidden())
                .andReturn();

        assertThat(result.getResolvedException())
                .isInstanceOf(ResponseStatusException.class);
        assertThat(((ResponseStatusException) result.getResolvedException()).getReason())
                .isEqualTo("No autorizado");
    }

    @Test
    public void noSePuedeEntrarALaListaSiNoEsAdmin() throws Exception {
        UsuarioData usuarioLogeado = new UsuarioData();
        usuarioLogeado.setId(1L);
        usuarioLogeado.setNombre("Ana");
        usuarioLogeado.setEmail("ana@ua");
        usuarioLogeado.setAdmin(false);

        when(usuarioService.findById(1L)).thenReturn(usuarioLogeado);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("idUsuarioLogeado", 1L);
        session.setAttribute("nombreUsuarioLogeado", "Ana");
        session.setAttribute("esAdmin", false);

        MvcResult result = this.mockMvc.perform(get("/registrados").session(session))
                .andExpect(status().isForbidden())
                .andReturn();

        assertThat(result.getResolvedException())
                .isInstanceOf(ResponseStatusException.class);
        assertThat(((ResponseStatusException) result.getResolvedException()).getReason())
                .isEqualTo("No autorizado");
    }

    @Test
    public void servicioLoginUsuarioNotFound() throws Exception {
        when(usuarioService.login("pepito.perez@gmail.com", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.USER_NOT_FOUND);

        this.mockMvc.perform(post("/login")
                        .param("eMail","pepito.perez@gmail.com")
                        .param("password","12345678"))
                .andExpect(content().string(containsString("No existe usuario")));
    }

    @Test
    public void servicioLoginUsuarioErrorPassword() throws Exception {
        when(usuarioService.login("ana.garcia@gmail.com", "000"))
                .thenReturn(UsuarioService.LoginStatus.ERROR_PASSWORD);

        this.mockMvc.perform(post("/login")
                        .param("eMail","ana.garcia@gmail.com")
                        .param("password","000"))
                .andExpect(content().string(containsString("Contraseña incorrecta")));
    }
}