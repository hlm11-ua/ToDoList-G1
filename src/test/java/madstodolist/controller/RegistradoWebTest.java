package madstodolist.controller;

import madstodolist.dto.EquipoData;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RegistradoWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private EquipoService equipoService;

    @Test
    public void detallesRegistradoDevuelveVistaYDatosDelUsuario() throws Exception {
        UsuarioData usuarioMock = new UsuarioData();
        usuarioMock.setId(2L);
        usuarioMock.setNombre("Juan Pérez");
        usuarioMock.setEmail("juan@ua.es");
        usuarioMock.setAdmin(true);

        when(usuarioService.findById(2L)).thenReturn(usuarioMock);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("idUsuarioLogeado", 2L);
        session.setAttribute("nombreUsuarioLogeado", "Juan Pérez");
        session.setAttribute("esAdmin", true);

        this.mockMvc.perform(get("/registrados/2").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("detallesRegistrado"))
                .andExpect(model().attributeExists("usuario"))
                .andExpect(model().attribute("usuario", usuarioMock))
                .andExpect(content().string(containsString("Juan Pérez")))
                .andExpect(content().string(containsString("juan@ua.es")));
    }

    @Test
    public void detallesRegistradoUsuarioNoExistente() throws Exception {
        when(usuarioService.findById(999L)).thenReturn(null);
        this.mockMvc.perform(get("/registrados/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void listaRegistradosVaciaMuestraMensaje() throws Exception {
        UsuarioData usuarioAdmin = new UsuarioData();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setNombre("Admin");
        usuarioAdmin.setEmail("admin@ua.es");
        usuarioAdmin.setAdmin(true);

        when(usuarioService.findById(1L)).thenReturn(usuarioAdmin);
        when(usuarioService.findAll()).thenReturn(Collections.emptyList());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("idUsuarioLogeado", 1L);
        session.setAttribute("nombreUsuarioLogeado", "Admin");
        session.setAttribute("esAdmin", true);

        this.mockMvc.perform(get("/registrados").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("listaRegistrados"))
                .andExpect(model().attributeExists("usuarios"))
                .andExpect(model().attribute("usuarios", Collections.emptyList()));
    }

    @Test
    public void listaRegistradosDevuelveVistaYListaDeUsuarios() throws Exception {
        UsuarioData usuarioAdmin = new UsuarioData();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setNombre("Admin");
        usuarioAdmin.setEmail("admin@ua.es");
        usuarioAdmin.setAdmin(true);

        UsuarioData usuario1 = new UsuarioData();
        usuario1.setId(1L);
        usuario1.setNombre("Juan Pérez");
        usuario1.setEmail("juan@ua.es");

        UsuarioData usuario2 = new UsuarioData();
        usuario2.setId(2L);
        usuario2.setNombre("Ana López");
        usuario2.setEmail("ana@ua.es");

        List<UsuarioData> usuariosMock = Arrays.asList(usuario1, usuario2);

        when(usuarioService.findById(1L)).thenReturn(usuarioAdmin);
        when(usuarioService.findAll()).thenReturn(usuariosMock);

        // Respuestas vacías por defecto al renderizar el desplegable en Thymeleaf
        when(equipoService.equiposUsuario(1L)).thenReturn(Collections.emptyList());
        when(equipoService.equiposUsuario(2L)).thenReturn(Collections.emptyList());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("idUsuarioLogeado", 1L);
        session.setAttribute("nombreUsuarioLogeado", "Admin");
        session.setAttribute("esAdmin", true);

        this.mockMvc.perform(get("/registrados").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("listaRegistrados"))
                .andExpect(model().attributeExists("usuarios"))
                .andExpect(model().attribute("usuarios", usuariosMock))
                .andExpect(content().string(containsString("Juan Pérez")))
                .andExpect(content().string(containsString("Ana López")));
    }

    @Test
    public void listaRegistradosMuestraEquiposEnDesplegable() throws Exception {
        UsuarioData usuarioAdmin = new UsuarioData();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setNombre("Admin");
        usuarioAdmin.setAdmin(true);

        UsuarioData usuario = new UsuarioData();
        usuario.setId(2L);
        usuario.setNombre("Carlos Gómez");

        EquipoData equipo1 = new EquipoData();
        equipo1.setId(10L);
        equipo1.setNombre("Proyecto Alpha");

        EquipoData equipo2 = new EquipoData();
        equipo2.setId(20L);
        equipo2.setNombre("Proyecto Beta");

        when(usuarioService.findById(1L)).thenReturn(usuarioAdmin);
        when(usuarioService.findAll()).thenReturn(Collections.singletonList(usuario));

        // Mock de la llamada realizada por Thymeleaf (@equipoService.equiposUsuario)
        when(equipoService.equiposUsuario(2L)).thenReturn(Arrays.asList(equipo1, equipo2));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("idUsuarioLogeado", 1L);
        session.setAttribute("nombreUsuarioLogeado", "Admin");
        session.setAttribute("esAdmin", true);

        this.mockMvc.perform(get("/registrados").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("collapse-usuario-2")))
                .andExpect(content().string(containsString("Ver Equipos")))
                .andExpect(content().string(containsString("Proyecto Alpha")))
                .andExpect(content().string(containsString("Proyecto Beta")));
    }

    @Test
    public void listaRegistradosUsuarioSinEquiposMuestraMensaje() throws Exception {
        UsuarioData usuarioAdmin = new UsuarioData();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setNombre("Admin");
        usuarioAdmin.setAdmin(true);

        UsuarioData usuario = new UsuarioData();
        usuario.setId(3L);
        usuario.setNombre("Sin Equipo User");

        when(usuarioService.findById(1L)).thenReturn(usuarioAdmin);
        when(usuarioService.findAll()).thenReturn(Collections.singletonList(usuario));

        when(equipoService.equiposUsuario(3L)).thenReturn(Collections.emptyList());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("idUsuarioLogeado", 1L);
        session.setAttribute("nombreUsuarioLogeado", "Admin");
        session.setAttribute("esAdmin", true);

        this.mockMvc.perform(get("/registrados").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Este usuario no pertenece a ningún equipo.")));
    }
}