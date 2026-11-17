
package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.EquipoData;
import madstodolist.dto.TableroData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Equipo;
import madstodolist.model.EstadoTarea;
import madstodolist.model.Tarea;
import madstodolist.repository.EquipoRepository;
import madstodolist.repository.TareaRepository;
import madstodolist.service.EquipoService;
import madstodolist.service.TableroService;
import madstodolist.service.TareaService;
import madstodolist.service.UsuarioService;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.allOf;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/clean-db.sql")
public class TableroWebTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TableroService tableroService;

    @Autowired
    private TareaService tareaService;

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private TareaRepository tareaRepository;

    @MockBean
    private ManagerUserSession managerUserSession;

    // Crea un usuario, un equipo y un tablero para las pruebas.
    private Map<String, Long> crearUsuarioYTableroBD() {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        UsuarioData usuarioNuevo = usuarioService.registrar(usuario);
        Equipo equipo = equipoRepository.save(new Equipo("Equipo de prueba"));

        TableroData tablero = tableroService.crearTablero(equipo.getId(),"Tablero de prueba");

        Map<String, Long> ids = new HashMap<>();
        ids.put("usuarioId", usuarioNuevo.getId());
        ids.put("tableroId", tablero.getId());
        ids.put("equipoId", equipo.getId());

        return ids;
    }


    // Prepara una sesión con un usuario autenticado.
    private MockHttpSession crearSesion(Long usuarioId) {
        MockHttpSession session = new MockHttpSession();

        session.setAttribute("idUsuarioLogeado", usuarioId);
        session.setAttribute("nombreUsuarioLogeado", "user@ua");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        return session;
    }


    @Test
    public void verTableroMuestraLasTresColumnasYLosFormularios() throws Exception {
        // GIVEN
        Map<String, Long> ids = crearUsuarioYTableroBD();
        Long tableroId = ids.get("tableroId");
        Long usuarioId = ids.get("usuarioId");
        MockHttpSession session = crearSesion(usuarioId);

        // WHEN, THEN
        mockMvc.perform(get("/tableros/" + tableroId).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("tablero"))
                .andExpect(model().attributeExists("tablero"))
                .andExpect(content().string(containsString("Pendiente")))
                .andExpect(content().string(containsString("En progreso")))
                .andExpect(content().string(containsString("Completada")))
                .andExpect(content().string(containsString("+ Añadir una tarea")))
                .andExpect(content().string(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("Introduzca un título"),
                        org.hamcrest.Matchers.containsString("Añadir tarea")
                )));
    }


    @Test
    public void verTableroMuestraCadaTareaEnSuColumna() throws Exception {
        // GIVEN
        Map<String, Long> ids = crearUsuarioYTableroBD();
        Long tableroId = ids.get("tableroId");
        Long usuarioId = ids.get("usuarioId");
        MockHttpSession session = crearSesion(usuarioId);

        tareaService.nuevaTareaTablero(tableroId, usuarioId, "Tarea pendiente", EstadoTarea.PENDIENTE);

        tareaService.nuevaTareaTablero(tableroId, usuarioId, "Tarea en progreso", EstadoTarea.EN_PROGRESO);

        tareaService.nuevaTareaTablero(tableroId, usuarioId, "Tarea completada", EstadoTarea.COMPLETADA);

        // WHEN, THEN
        mockMvc.perform(get("/tableros/" + tableroId).session(session))
                .andExpect(status().isOk())
                .andExpect(xpath("//*[@id='columna-pendiente']" + "//*[contains(@class,'tarea-card')]" + "[contains(.,'Tarea pendiente')]").exists())
                .andExpect(xpath("//*[@id='columna-en-progreso']" + "//*[contains(@class,'tarea-card')]" + "[contains(.,'Tarea en progreso')]").exists())
                .andExpect(xpath("//*[@id='columna-completada']" + "//*[contains(@class,'tarea-card')]" + "[contains(.,'Tarea completada')]").exists());
    }



    @Test
    public void postNuevaTareaTableroCreaTareaYDevuelveRedirect() throws Exception {
        // GIVEN
        // Un usuario y un tablero en la base de datos.
        Map<String, Long> ids = crearUsuarioYTableroBD();
        Long usuarioId = ids.get("usuarioId");
        Long tableroId = ids.get("tableroId");
        MockHttpSession session = crearSesion(usuarioId);

        // WHEN
        // Enviamos una petición POST para crear una tarea en la columna "En progreso".
        mockMvc.perform(post("/tableros/" + tableroId + "/tareas/nueva")
                        .session(session)
                        .param("titulo", "Preparar entrega")
                        .param("estado", "EN_PROGRESO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tableros/" + tableroId));

        // THEN
        List<Tarea> tareas = tareaRepository.findByTablero_IdOrderByIdAsc(tableroId);
        assertThat(tareas).hasSize(1);
        Tarea tarea = tareas.get(0);
        assertThat(tarea.getTitulo()).isEqualTo("Preparar entrega");
        assertThat(tarea.getTablero().getId()).isEqualTo(tableroId);
        assertThat(tarea.getEstado()).isEqualTo(EstadoTarea.EN_PROGRESO);
        assertThat(tarea.getUsuario().getId()).isEqualTo(usuarioId);
        assertThat(tarea.getUsuarioAsignado()).isNull();
    }

    @Test
    public void verTableroMuestraFiltroDeMisTareas() throws Exception {
        // GIVEN
        Map<String, Long> ids = crearUsuarioYTableroBD();
        Long usuarioId = ids.get("usuarioId");
        Long tableroId = ids.get("tableroId");
        Long equipoId = ids.get("equipoId");
        MockHttpSession session = crearSesion(usuarioId);

        // El usuario pertenece al equipo del tablero
        equipoService.añadirUsuarioAEquipo(equipoId, usuarioId);

        // Creamos una tarea y se la asignamos al usuario
        tareaService.nuevaTareaTablero(tableroId, usuarioId,"Mi tarea asignada", EstadoTarea.PENDIENTE);
        Tarea tarea = tareaRepository.findByTablero_IdOrderByIdAsc(tableroId).get(0);
        tareaService.asignarUsuarioATarea(tarea.getId(), usuarioId);

        // WHEN, THEN
        mockMvc.perform(get("/tableros/" + tableroId).session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("usuarioLogeadoId", usuarioId))
                .andExpect(content().string(containsString("Filtrar por mis tareas")))
                .andExpect(content().string(containsString("draggable=\"true\"")))
                .andExpect(content().string(containsString("data-usuario-asignado-id=\"" + usuarioId + "\"")));
    }

    // Test: cualquier miembro puede mover una tarea del tablero aunque no la haya creado
    @Test
    public void miembroPuedeCambiarEstadoDeTareaDelTablero() throws Exception {
        // GIVEN un tablero con una tarea creada por un usuario
        Map<String, Long> ids = crearUsuarioYTableroBD();
        Long tableroId = ids.get("tableroId");
        Long tareaId = tareaService.nuevaTareaTablero(tableroId, ids.get("usuarioId"),
                "Tarea del tablero", EstadoTarea.PENDIENTE).getId();

        // y otro usuario logeado que NO es el creador de la tarea
        UsuarioData otro = new UsuarioData();
        otro.setEmail("otro@ua");
        otro.setPassword("123");
        Long otroId = usuarioService.registrar(otro).getId();
        when(managerUserSession.usuarioLogeado()).thenReturn(otroId);

        // WHEN, THEN puede moverla y se redirige al tablero
        this.mockMvc.perform(post("/tareas/" + tareaId + "/estado")
                        .param("estado", "EN_PROGRESO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tableros/" + tableroId));

        assertThat(tareaService.findById(tareaId).getEstado()).isEqualTo(EstadoTarea.EN_PROGRESO);
    }

    // Test: el tablero incluye los datos que necesita el drag & drop para guardar el estado
    @Test
    public void verTableroIncluyeDatosParaGuardarEstadoAlArrastrar() throws Exception {
        // GIVEN un tablero con una tarea
        Map<String, Long> ids = crearUsuarioYTableroBD();
        Long tableroId = ids.get("tableroId");
        Long tareaId = tareaService.nuevaTareaTablero(tableroId, ids.get("usuarioId"),
                "Tarea del tablero", EstadoTarea.PENDIENTE).getId();
        MockHttpSession session = crearSesion(ids.get("usuarioId"));

        // WHEN, THEN las columnas indican su estado y la tarjeta indica su id
        mockMvc.perform(get("/tableros/" + tableroId).session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("data-estado=\"PENDIENTE\""),
                        containsString("data-estado=\"EN_PROGRESO\""),
                        containsString("data-estado=\"COMPLETADA\""),
                        containsString("data-tarea-id=\"" + tareaId + "\"")
                )));
    }

    @Test
    public void adminPuedeModificarNombreTablero() throws Exception {
        UsuarioData admin = new UsuarioData();
        admin.setEmail("admin@ua.es");
        admin.setPassword("1234");
        admin.setAdmin(true);
        admin = usuarioService.registrar(admin);

        EquipoData equipo = equipoService.crearEquipo("Equipo MADS");
        TableroData tablero = tableroService.crearTablero(equipo.getId(), "Tablero Antiguo");

        this.mockMvc.perform(post("/tableros/" + tablero.getId() + "/update")
                        .param("nombre", "Tablero Nuevo")
                        .sessionAttr("idUsuarioLogeado", admin.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos/" + equipo.getId()));

        TableroData tableroActualizado = tableroService.recuperarTablero(tablero.getId());
        assertThat(tableroActualizado.getNombre()).isEqualTo("Tablero Nuevo");
    }

    @Test
    public void adminPuedeBorrarTablero() throws Exception {
        UsuarioData admin = new UsuarioData();
        admin.setEmail("admin2@ua.es");
        admin.setPassword("1234");
        admin.setAdmin(true);
        admin = usuarioService.registrar(admin);

        EquipoData equipo = equipoService.crearEquipo("Equipo MADS");
        TableroData tablero = tableroService.crearTablero(equipo.getId(), "Tablero A Borrar");

        this.mockMvc.perform(post("/tableros/" + tablero.getId() + "/delete")
                        .sessionAttr("idUsuarioLogeado", admin.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos/" + equipo.getId()));

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            tableroService.recuperarTablero(tablero.getId());
        });
    }

    @Test
    public void usuarioNoAdminNoPuedeModificarNiBorrarTablero() throws Exception {
        UsuarioData usuarioNormal = new UsuarioData();
        usuarioNormal.setEmail("normal@ua.es");
        usuarioNormal.setPassword("1234");
        usuarioNormal.setAdmin(false);
        usuarioNormal = usuarioService.registrar(usuarioNormal);

        EquipoData equipo = equipoService.crearEquipo("Equipo MADS");
        TableroData tablero = tableroService.crearTablero(equipo.getId(), "Tablero Protegido");

        this.mockMvc.perform(post("/tableros/" + tablero.getId() + "/update")
                        .param("nombre", "Hackeado")
                        .sessionAttr("idUsuarioLogeado", usuarioNormal.getId()))
                .andExpect(status().isForbidden());

        this.mockMvc.perform(post("/tableros/" + tablero.getId() + "/delete")
                        .sessionAttr("idUsuarioLogeado", usuarioNormal.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void usuarioNoLogeadoRecibeForbidden() throws Exception {
        EquipoData equipo = equipoService.crearEquipo("Equipo MADS");
        TableroData tablero = tableroService.crearTablero(equipo.getId(), "Tablero Publico");

        this.mockMvc.perform(post("/tableros/" + tablero.getId() + "/update")
                        .param("nombre", "Sin Sesion"))
                .andExpect(status().isForbidden());

        this.mockMvc.perform(post("/tableros/" + tablero.getId() + "/delete"))
                .andExpect(status().isForbidden());
    }
}
