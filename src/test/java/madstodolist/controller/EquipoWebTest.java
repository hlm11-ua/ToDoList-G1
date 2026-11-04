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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EquipoWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private EquipoService equipoService;

    @Test
    public void listaEquiposDevuelveVistaYListaDeEquipos() throws Exception {
        // Mock de usuario
        UsuarioData usuarioMock = new UsuarioData();
        usuarioMock.setId(1L);
        usuarioMock.setNombre("Usuario Test");
        when(usuarioService.findById(1L)).thenReturn(usuarioMock);

        // Mock de equipos
        EquipoData equipo1 = new EquipoData();
        equipo1.setId(1L);
        equipo1.setNombre("Proyecto Alpha");

        EquipoData equipo2 = new EquipoData();
        equipo2.setId(2L);
        equipo2.setNombre("Proyecto Beta");

        List<EquipoData> equiposMock = Arrays.asList(equipo1, equipo2);
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(equiposMock);

        // Mocks adicionales por si la plantilla consulta miembros de equipos
        when(equipoService.usuariosEquipo(1L)).thenReturn(Collections.emptyList());
        when(equipoService.usuariosEquipo(2L)).thenReturn(Collections.emptyList());

        // Sesión completa
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("idUsuarioLogeado", 1L);
        session.setAttribute("nombreUsuarioLogeado", "Usuario Test");
        session.setAttribute("esAdmin", false);

        this.mockMvc.perform(get("/equipos").session(session))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("formEquipos"))
                .andExpect(model().attributeExists("equipos"))
                .andExpect(model().attribute("equipos", equiposMock))
                .andExpect(content().string(containsString("Proyecto Alpha")))
                .andExpect(content().string(containsString("Proyecto Beta")))
                .andExpect(content().string(containsString("collapse-equipo-1")))
                .andExpect(content().string(containsString("Ver Usuarios")))
                .andExpect(content().string(containsString("data-toggle=\"collapse\"")));
    }

    @Test
    public void listaEquiposVaciaMuestraListaVacia() throws Exception {
        UsuarioData usuarioMock = new UsuarioData();
        usuarioMock.setId(1L);
        when(usuarioService.findById(1L)).thenReturn(usuarioMock);

        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Collections.emptyList());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("idUsuarioLogeado", 1L);
        session.setAttribute("nombreUsuarioLogeado", "Usuario Test");

        this.mockMvc.perform(get("/equipos").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("formEquipos"))
                .andExpect(model().attributeExists("equipos"))
                .andExpect(model().attribute("equipos", Collections.emptyList()))
                .andExpect(content().string(containsString("No hay equipos creados.")));
    }

    @Test
    public void listaEquiposSinUsuarioLogeadoDevuelve403() throws Exception {
        this.mockMvc.perform(get("/equipos"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void crearEquipoRedirigeAEquipos() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("idUsuarioLogeado", 1L);

        this.mockMvc.perform(post("/equipos/crear")
                        .param("nombre", "Nuevo Equipo")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        verify(equipoService).crearEquipo("Nuevo Equipo");
    }

    @Test
    public void unirseAEquipoRedirigeAEquipos() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("idUsuarioLogeado", 1L);

        this.mockMvc.perform(post("/equipos/1/unirse").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        verify(equipoService).añadirUsuarioAEquipo(1L, 1L);
    }

    @Test
    public void salirDeEquipoRedirigeAEquipos() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("idUsuarioLogeado", 1L);

        this.mockMvc.perform(post("/equipos/1/salir").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        verify(equipoService).eliminarUsuarioDelEquipo(1L, 1L);
    }

    @Test
    public void crearEquipoSinUsuarioLogeadoDevuelve403() throws Exception {
        this.mockMvc.perform(post("/equipos/crear").param("nombre", "Nuevo Equipo"))
                .andExpect(status().isForbidden());

        verify(equipoService, never()).crearEquipo(anyString());
    }

    @Test
    public void unirseAEquipoSinUsuarioLogeadoDevuelve403() throws Exception {
        this.mockMvc.perform(post("/equipos/1/unirse"))
                .andExpect(status().isForbidden());

        verify(equipoService, never()).añadirUsuarioAEquipo(anyLong(), anyLong());
    }

    @Test
    public void salirDeEquipoSinUsuarioLogeadoDevuelve403() throws Exception {
        this.mockMvc.perform(post("/equipos/1/salir"))
                .andExpect(status().isForbidden());

        verify(equipoService, never()).eliminarUsuarioDelEquipo(anyLong(), anyLong());
    }

    @Test
    public void listaEquiposMuestraFormularioDeCreacion() throws Exception {
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Collections.emptyList());

        this.mockMvc.perform(get("/equipos").session(sesionUsuario(1L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/equipos/crear\"")))
                .andExpect(content().string(containsString("Crear Equipo")));
    }

    @Test
    public void listaEquiposMuestraBotonSalirSiUsuarioEsMiembro() throws Exception {
        EquipoData equipo = equipoConUsuarios(1L, "Proyecto Alpha", usuario(1L, "Usuario Test", "user@ua"));
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Collections.singletonList(equipo));

        this.mockMvc.perform(get("/equipos").session(sesionUsuario(1L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/equipos/1/salir\"")))
                .andExpect(content().string(containsString("Salir del Equipo")))
                .andExpect(content().string(not(containsString("action=\"/equipos/1/unirse\""))));
    }

    @Test
    public void listaEquiposMuestraBotonUnirseSiUsuarioNoEsMiembro() throws Exception {
        EquipoData equipo = equipoConUsuarios(1L, "Proyecto Alpha", usuario(2L, "Otro Usuario", "otro@ua"));
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Collections.singletonList(equipo));

        this.mockMvc.perform(get("/equipos").session(sesionUsuario(1L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/equipos/1/unirse\"")))
                .andExpect(content().string(containsString("Unirse al Equipo")))
                .andExpect(content().string(not(containsString("action=\"/equipos/1/salir\""))));
    }

    @Test
    public void listaEquiposMuestraUsuariosDeCadaEquipo() throws Exception {
        EquipoData equipo = equipoConUsuarios(1L, "Proyecto Alpha",
                usuario(1L, "Usuario Test", "user@ua"),
                usuario(2L, "Otro Usuario", "otro@ua"));
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Collections.singletonList(equipo));

        this.mockMvc.perform(get("/equipos").session(sesionUsuario(1L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Usuario Test")))
                .andExpect(content().string(containsString("user@ua")))
                .andExpect(content().string(containsString("Otro Usuario")))
                .andExpect(content().string(containsString("otro@ua")))
                .andExpect(content().string(not(containsString("Sin usuarios registrados en este equipo."))));
    }

    @Test
    public void listaEquiposSinMiembrosMuestraMensaje() throws Exception {
        EquipoData equipo = equipoConUsuarios(1L, "Proyecto Alpha");
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Collections.singletonList(equipo));
        when(equipoService.usuariosEquipo(1L)).thenReturn(Collections.emptyList());

        this.mockMvc.perform(get("/equipos").session(sesionUsuario(1L)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sin usuarios registrados en este equipo.")));
    }

    @Test
    public void listaEquiposMuestraOpcionModificarAAdmin() throws Exception {
        EquipoData equipo = equipoConUsuarios(1L, "Proyecto Alpha");
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Collections.singletonList(equipo));

        MockHttpSession session = sesionUsuario(1L);
        session.setAttribute("esAdmin", true);

        this.mockMvc.perform(get("/equipos").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Modificar")))
                .andExpect(content().string(containsString("editar-equipo-1")))
                .andExpect(content().string(containsString("action=\"/equipos/1/update\"")))
                .andExpect(content().string(containsString("value=\"Proyecto Alpha\"")));
    }

    @Test
    public void listaEquiposNoMuestraOpcionModificarANoAdmin() throws Exception {
        EquipoData equipo = equipoConUsuarios(1L, "Proyecto Alpha");
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Collections.singletonList(equipo));

        MockHttpSession session = sesionUsuario(1L);
        session.setAttribute("esAdmin", false);

        this.mockMvc.perform(get("/equipos").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("editar-equipo-1"))))
                .andExpect(content().string(not(containsString("action=\"/equipos/1/update\""))));
    }

    @Test
    public void actualizarEquipoComoAdminRedirigeAEquipos() throws Exception {
        UsuarioData admin = usuario(1L, "Admin", "admin@ua");
        admin.setAdmin(true);
        when(usuarioService.findById(1L)).thenReturn(admin);

        this.mockMvc.perform(post("/equipos/1/update")
                        .param("nombre", "Nuevo Nombre")
                        .session(sesionUsuario(1L)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        verify(equipoService).actualizarEquipo(1L, "Nuevo Nombre");
    }

    @Test
    public void actualizarEquipoSinSerAdminDevuelve403() throws Exception {
        when(usuarioService.findById(1L)).thenReturn(usuario(1L, "Usuario Test", "user@ua"));

        this.mockMvc.perform(post("/equipos/1/update")
                        .param("nombre", "Nuevo Nombre")
                        .session(sesionUsuario(1L)))
                .andExpect(status().isForbidden());

        verify(equipoService, never()).actualizarEquipo(anyLong(), anyString());
    }

    @Test
    public void actualizarEquipoSinUsuarioLogeadoDevuelve403() throws Exception {
        this.mockMvc.perform(post("/equipos/1/update").param("nombre", "Nuevo Nombre"))
                .andExpect(status().isForbidden());

        verify(equipoService, never()).actualizarEquipo(anyLong(), anyString());
    }

    @Test
    public void listaEquiposMuestraBotonEliminarAAdmin() throws Exception {
        EquipoData equipo = equipoConUsuarios(1L, "Proyecto Alpha");
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Collections.singletonList(equipo));

        MockHttpSession session = sesionUsuario(1L);
        session.setAttribute("esAdmin", true);

        this.mockMvc.perform(get("/equipos").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("action=\"/equipos/1/delete\"")))
                .andExpect(content().string(containsString("Eliminar")));
    }

    @Test
    public void listaEquiposNoMuestraBotonEliminarANoAdmin() throws Exception {
        EquipoData equipo = equipoConUsuarios(1L, "Proyecto Alpha");
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Collections.singletonList(equipo));

        MockHttpSession session = sesionUsuario(1L);
        session.setAttribute("esAdmin", false);

        this.mockMvc.perform(get("/equipos").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("action=\"/equipos/1/delete\""))));
    }

    @Test
    public void eliminarEquipoComoAdminRedirigeAEquipos() throws Exception {
        UsuarioData admin = usuario(1L, "Admin", "admin@ua");
        admin.setAdmin(true);
        when(usuarioService.findById(1L)).thenReturn(admin);

        this.mockMvc.perform(post("/equipos/1/delete").session(sesionUsuario(1L)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        verify(equipoService).eliminarEquipo(1L);
    }

    @Test
    public void eliminarEquipoSinSerAdminDevuelve403() throws Exception {
        when(usuarioService.findById(1L)).thenReturn(usuario(1L, "Usuario Test", "user@ua"));

        this.mockMvc.perform(post("/equipos/1/delete").session(sesionUsuario(1L)))
                .andExpect(status().isForbidden());

        verify(equipoService, never()).eliminarEquipo(anyLong());
    }

    @Test
    public void eliminarEquipoSinUsuarioLogeadoDevuelve403() throws Exception {
        this.mockMvc.perform(post("/equipos/1/delete"))
                .andExpect(status().isForbidden());

        verify(equipoService, never()).eliminarEquipo(anyLong());
    }

    private MockHttpSession sesionUsuario(Long id) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("idUsuarioLogeado", id);
        session.setAttribute("nombreUsuarioLogeado", "Usuario Test");
        return session;
    }

    private UsuarioData usuario(Long id, String nombre, String email) {
        UsuarioData usuario = new UsuarioData();
        usuario.setId(id);
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        return usuario;
    }

    private EquipoData equipoConUsuarios(Long id, String nombre, UsuarioData... usuarios) {
        EquipoData equipo = new EquipoData();
        equipo.setId(id);
        equipo.setNombre(nombre);
        equipo.setUsuarios(new ArrayList<>(Arrays.asList(usuarios)));
        return equipo;
    }
}