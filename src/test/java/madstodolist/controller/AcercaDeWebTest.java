package madstodolist.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
public class AcercaDeWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getAboutDevuelveNombreAplicacion() throws Exception {
        this.mockMvc.perform(get("/about"))
                .andExpect(content().string(containsString("ToDoList")));
    }

    @Test
    public void getAboutSinSesionMuestraLoginYRegistro() throws Exception {
        this.mockMvc.perform(get("/about"))
                .andExpect(content().string(containsString("Login")))
                .andExpect(content().string(containsString("Registro")))
                .andExpect(content().string(containsString("/login")))
                .andExpect(content().string(containsString("/registro")));
    }

    @Test
    public void getAboutConSesionMuestraMenuDeUsuario() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("idUsuarioLogeado", 7L);
        session.setAttribute("nombreUsuarioLogeado", "Ana García");

        this.mockMvc.perform(get("/about").session(session))
                .andExpect(content().string(containsString("Ana García")))
                .andExpect(content().string(containsString("Tareas")))
                .andExpect(content().string(containsString("Cuenta")))
                .andExpect(content().string(containsString("Cerrar sesión")));
    }
}