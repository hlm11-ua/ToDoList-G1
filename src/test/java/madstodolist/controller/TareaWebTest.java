package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.TareaService;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import madstodolist.model.EstadoTarea;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/clean-db.sql")
public class TareaWebTest {

    @Autowired
    private MockMvc mockMvc;

    // Declaramos los servicios como Autowired
    @Autowired
    private TareaService tareaService;

    @Autowired
    private UsuarioService usuarioService;

    // Moqueamos el managerUserSession para poder moquear el usuario logeado
    @MockBean
    private ManagerUserSession managerUserSession;

    // Método para inicializar los datos de prueba en la BD
    // Devuelve un mapa con los identificadores del usuario y de la primera tarea añadida

    Map<String, Long> addUsuarioTareasBD() {
        // Añadimos un usuario a la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);

        // Y añadimos dos tareas asociadas a ese usuario
        TareaData tarea1 = tareaService.nuevaTareaUsuario(usuario.getId(), "Lavar coche");
        tareaService.nuevaTareaUsuario(usuario.getId(), "Renovar DNI");

        // Devolvemos los ids del usuario y de la primera tarea añadida
        Map<String, Long> ids = new HashMap<>();
        ids.put("usuarioId", usuario.getId());
        ids.put("tareaId", tarea1.getId());
        return ids;

    }

    @Test
    public void listaTareas() throws Exception {
        // GIVEN
        // Un usuario con dos tareas en la BD
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");

        // Moqueamos el método usuarioLogeado para que devuelva el usuario 1L,
        // el mismo que se está usando en la petición. De esta forma evitamos
        // que salte la excepción de que el usuario que está haciendo la
        // petición no está logeado.
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // se realiza la petición GET al listado de tareas del usuario,
        // el HTML devuelto contiene las descripciones de sus tareas.

        String url = "/usuarios/" + usuarioId.toString() + "/tareas";

        this.mockMvc.perform(get(url))
                .andExpect((content().string(allOf(
                        containsString("Lavar coche"),
                        containsString("Renovar DNI")
                ))));
    }

    @Test
    public void listaTareasMuestraNavbarConDesplegableDeUsuario() throws Exception {
        // GIVEN
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("idUsuarioLogeado", usuarioId);
        session.setAttribute("nombreUsuarioLogeado", "user@ua");

        // WHEN & THEN
        this.mockMvc.perform(get("/usuarios/" + usuarioId + "/tareas").session(session))
                .andExpect(content().string(containsString("user@ua")))
                .andExpect(content().string(containsString("Cuenta")))
                .andExpect(content().string(containsString("Cerrar sesión")))
                .andExpect(content().string(containsString("Tareas")));
    }

    @Test
    public void getNuevaTareaDevuelveForm() throws Exception {
        // GIVEN
        // Un usuario con dos tareas en la BD
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");

        // Ver el comentario en el primer test
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // si ejecutamos una petición GET para crear una nueva tarea de un usuario,
        // el HTML resultante contiene un formulario y la ruta con
        // la acción para crear la nueva tarea.

        String urlPeticion = "/usuarios/" + usuarioId.toString() + "/tareas/nueva";
        String urlAction = "action=\"/usuarios/" + usuarioId.toString() + "/tareas/nueva\"";

        this.mockMvc.perform(get(urlPeticion))
                .andExpect((content().string(allOf(
                        containsString("form method=\"post\""),
                        containsString(urlAction)
                ))));
    }

    @Test
    public void postNuevaTareaDevuelveRedirectYAñadeTarea() throws Exception {
        // GIVEN
        // Un usuario con dos tareas en la BD
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");

        // Ver el comentario en el primer test
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // realizamos la petición POST para añadir una nueva tarea,
        // el estado HTTP que se devuelve es un REDIRECT al listado
        // de tareas.

        String urlPost = "/usuarios/" + usuarioId.toString() + "/tareas/nueva";
        String urlRedirect = "/usuarios/" + usuarioId.toString() + "/tareas";

        this.mockMvc.perform(post(urlPost)
                        .param("titulo", "Estudiar examen MADS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        // y si después consultamos el listado de tareas con una petición
        // GET el HTML contiene la tarea añadida.

        this.mockMvc.perform(get(urlRedirect))
                .andExpect((content().string(containsString("Estudiar examen MADS"))));
    }

    @Test
    public void deleteTareaDevuelveOKyBorraTarea() throws Exception {
        // GIVEN
        // Un usuario con dos tareas en la BD
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaLavarCocheId = ids.get("tareaId");

        // Ver el comentario en el primer test
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // realizamos la petición DELETE para borrar una tarea,
        // se devuelve el estado HTTP que se devuelve es OK,

        String urlDelete = "/tareas/" + tareaLavarCocheId.toString();

        this.mockMvc.perform(delete(urlDelete))
                .andExpect(status().isOk());

        // y cuando se pide un listado de tareas del usuario, la tarea borrada ya no aparece.

        String urlListado = "/usuarios/" + usuarioId + "/tareas";

        this.mockMvc.perform(get(urlListado))
                .andExpect(content().string(
                        allOf(not(containsString("Lavar coche")),
                                containsString("Renovar DNI"))));
    }

    @Test
    public void editarTareaActualizaLaTarea() throws Exception {
        // GIVEN
        // Un usuario con dos tareas en la BD
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaLavarCocheId = ids.get("tareaId");

        // Ver el comentario en el primer test
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // realizamos una petición POST al endpoint para editar una tarea

        String urlEditar = "/tareas/" + tareaLavarCocheId + "/editar";
        String urlRedirect = "/usuarios/" + usuarioId + "/tareas";

        this.mockMvc.perform(post(urlEditar)
                        .param("titulo", "Limpiar cristales coche"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        // Y si realizamos un listado de las tareas del usuario
        // ha cambiado el título de la tarea modificada

        String urlListado = "/usuarios/" + usuarioId + "/tareas";

        this.mockMvc.perform(get(urlListado))
                .andExpect(content().string(containsString("Limpiar cristales coche")));
    }

    // Test: el dueño cambia el estado de su tarea y se guarda en la BD
    @Test
    public void postEstadoTareaPropiaCambiaEstadoYRedirige() throws Exception {
        // GIVEN un usuario logeado con dos tareas
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN cambiamos el estado y se redirige a su listado de tareas
        this.mockMvc.perform(post("/tareas/" + tareaId + "/estado")
                        .param("estado", "COMPLETADA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/" + usuarioId + "/tareas"));

        // y el estado queda guardado en la BD
        assertThat(tareaService.findById(tareaId).getEstado()).isEqualTo(EstadoTarea.COMPLETADA);
    }

    // Test: otro usuario no puede cambiar el estado de una tarea personal que no es suya
    @Test
    public void postEstadoTareaDeOtroUsuarioDevuelve401() throws Exception {
        // GIVEN una tarea personal y un usuario logeado que NO es su dueño
        Map<String, Long> ids = addUsuarioTareasBD();
        Long tareaId = ids.get("tareaId");
        when(managerUserSession.usuarioLogeado()).thenReturn(ids.get("usuarioId") + 1);

        // WHEN, THEN no está autorizado
        this.mockMvc.perform(post("/tareas/" + tareaId + "/estado")
                        .param("estado", "COMPLETADA"))
                .andExpect(status().isUnauthorized());

        // y el estado no ha cambiado
        assertThat(tareaService.findById(tareaId).getEstado()).isEqualTo(EstadoTarea.PENDIENTE);
    }

    // Test: sin iniciar sesión no se puede cambiar el estado de una tarea
    @Test
    public void postEstadoSinLoginDevuelve401() throws Exception {
        // GIVEN una tarea y nadie logeado
        Long tareaId = addUsuarioTareasBD().get("tareaId");
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        // WHEN, THEN
        this.mockMvc.perform(post("/tareas/" + tareaId + "/estado")
                        .param("estado", "COMPLETADA"))
                .andExpect(status().isUnauthorized());
    }

    // Test: cambiar el estado de una tarea que no existe devuelve error 404
    @Test
    public void postEstadoTareaInexistenteDevuelve404() throws Exception {
        // GIVEN un usuario logeado
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN cambiar una tarea que no existe devuelve 404
        this.mockMvc.perform(post("/tareas/999/estado")
                        .param("estado", "COMPLETADA"))
                .andExpect(status().isNotFound());
    }

    // Test: el listado de tareas muestra el estado y el formulario para cambiarlo
    @Test
    public void listaTareasMuestraEstadoYFormulario() throws Exception {
        // GIVEN un usuario logeado con dos tareas (empiezan en PENDIENTE)
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN el listado muestra el estado y el formulario para cambiarlo
        this.mockMvc.perform(get("/usuarios/" + usuarioId + "/tareas"))
                .andExpect(content().string(allOf(
                        containsString("Pendiente"),
                        containsString("action=\"/tareas/" + ids.get("tareaId") + "/estado\"")
                )));
    }
}
