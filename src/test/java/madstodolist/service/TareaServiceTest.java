package madstodolist.service;

import madstodolist.dto.TableroData;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Equipo;
import madstodolist.model.EstadoTarea;
import madstodolist.model.Tarea;
import madstodolist.repository.EquipoRepository;
import madstodolist.repository.TareaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

// Hemos eliminado todos los @Transactional de los tests
// y usado un script para limpiar la BD de test después de
// cada test
// https://dev.to/henrykeys/don-t-use-transactional-in-tests-40eb

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class TareaServiceTest {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    TareaService tareaService;

    @Autowired
    TableroService tableroService;

    @Autowired
    EquipoRepository equipoRepository;

    @Autowired
    TareaRepository tareaRepository;

    @Autowired
    EquipoService equipoService;

    // Método para inicializar los datos de prueba en la BD
    // Devuelve un mapa con los identificadores del usuario y de la primera tarea añadida
    Map<String, Long> addUsuarioTareasBD() {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");

        // Añadimos un usuario a la base de datos
        UsuarioData usuarioNuevo = usuarioService.registrar(usuario);

        // Y añadimos dos tareas asociadas a ese usuario
        TareaData tarea1 = tareaService.nuevaTareaUsuario(usuarioNuevo.getId(), "Lavar coche");
        tareaService.nuevaTareaUsuario(usuarioNuevo.getId(), "Renovar DNI");

        // Devolvemos los ids del usuario y de la primera tarea añadida
        Map<String, Long> ids = new HashMap<>();
        ids.put("usuarioId", usuarioNuevo.getId());
        ids.put("tareaId", tarea1.getId());
        return ids;
    }

    private Map<String, Long> crearDatosParaAsignacion() {
        // Creamos dos usuarios
        UsuarioData usuario1 = new UsuarioData();
        usuario1.setEmail("miembro@ua");
        usuario1.setPassword("123");
        usuario1 = usuarioService.registrar(usuario1);

        UsuarioData usuario2 = new UsuarioData();
        usuario2.setEmail("externo@ua");
        usuario2.setPassword("123");
        usuario2 = usuarioService.registrar(usuario2);

        // Creamos un equipo y añadimos solamente al primer usuario
        Equipo equipo = equipoRepository.save(new Equipo("Equipo de prueba"));
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario1.getId());

        // Creamos un tablero y una tarea
        TableroData tablero = tableroService.crearTablero(
                equipo.getId(), "Tablero de prueba");

        TareaData tarea = tareaService.nuevaTareaTablero(tablero.getId(), usuario1.getId(),"Preparar entrega", EstadoTarea.PENDIENTE);

        Map<String, Long> ids = new HashMap<>();
        ids.put("usuarioMiembroId", usuario1.getId());
        ids.put("usuarioExternoId", usuario2.getId());
        ids.put("tableroId", tablero.getId());
        ids.put("tareaId", tarea.getId());

        return ids;
    }

    @Test
    public void testNuevaTareaUsuario() {
        // GIVEN
        // Un usuario en la BD

        Long usuarioId = addUsuarioTareasBD().get("usuarioId");

        // WHEN
        // creamos una nueva tarea asociada al usuario,
        TareaData nuevaTarea = tareaService.nuevaTareaUsuario(usuarioId, "Práctica 1 de MADS");

        // THEN
        // al recuperar la lista de tareas del usuario, la nueva tarea
        // está en la lista de tareas del usuario.

        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);

        assertThat(tareas).hasSize(3);
        assertThat(tareas).contains(nuevaTarea);
    }

    @Test
    public void testBuscarTarea() {
        // GIVEN
        // Una tarea en la BD

        Long tareaId = addUsuarioTareasBD().get("tareaId");

        // WHEN
        // recuperamos una tarea de la base de datos a partir de su ID,

        TareaData lavarCoche = tareaService.findById(tareaId);

        // THEN
        // los datos de la tarea recuperada son correctos.

        assertThat(lavarCoche).isNotNull();
        assertThat(lavarCoche.getTitulo()).isEqualTo("Lavar coche");
    }

    @Test
    public void testModificarTarea() {
        // GIVEN
        // Un usuario y una tarea en la BD

        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        // WHEN
        // modificamos la tarea correspondiente al identificador,

        tareaService.modificaTarea(tareaId, "Limpiar los cristales del coche");

        // THEN
        // al buscar por el identificador en la base de datos se devuelve la tarea modificada

        TareaData tareaBD = tareaService.findById(tareaId);
        assertThat(tareaBD.getTitulo()).isEqualTo("Limpiar los cristales del coche");

        // y el usuario tiene también esa tarea modificada.
        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);
        assertThat(tareas).contains(tareaBD);
    }

    @Test
    public void testBorrarTarea() {
        // GIVEN
        // Un usuario y una tarea en la BD

        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        // WHEN
        // borramos la tarea correspondiente al identificador,

        tareaService.borraTarea(tareaId);

        // THEN
        // la tarea ya no está en la base de datos ni en las tareas del usuario.

        assertThat(tareaService.findById(tareaId)).isNull();

        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);
        assertThat(tareas).hasSize(1);
    }

    @Test
    public void asignarEtiquetaATarea(){

        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        assertThat(tareaService.usuarioContieneTarea(usuarioId,tareaId)).isTrue();
    }


    @Test
    public void testNuevaTareaTablero() {
        // GIVEN
        // Un usuario y un tablero en la base de datos.
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");
        Equipo equipo = equipoRepository.save(new Equipo("Equipo de prueba"));
        TableroData tablero = tableroService.crearTablero(equipo.getId(),"Tablero de prueba");

        // WHEN
        // Creamos una tarea pendiente en ese tablero.
        TareaData nuevaTarea = tareaService.nuevaTareaTablero(tablero.getId(), usuarioId, "Hacer algo", EstadoTarea.PENDIENTE);

        // THEN
        // Comprobamos que la tarea se ha guardado correctamente.
        Tarea tareaBD = tareaRepository.findById(nuevaTarea.getId()).orElseThrow();
        assertThat(tareaBD.getTitulo()).isEqualTo("Hacer algo");
        assertThat(tareaBD.getTablero().getId()).isEqualTo(tablero.getId());
        assertThat(tareaBD.getEstado()).isEqualTo(EstadoTarea.PENDIENTE);
        assertThat(tareaBD.getUsuario().getId()).isEqualTo(usuarioId);
        assertThat(tareaBD.getUsuarioAsignado()).isNull();
    }


    @Test
    public void testTareasTablero() {
        // GIVEN
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");
        Equipo equipo = equipoRepository.save(new Equipo("Equipo de prueba"));
        TableroData tablero1 = tableroService.crearTablero(equipo.getId(), "Tablero 1");
        TableroData tablero2 = tableroService.crearTablero(equipo.getId(),"Tablero 2");

        tareaService.nuevaTareaTablero(tablero1.getId(), usuarioId, "Tarea del tablero 1", EstadoTarea.PENDIENTE);
        tareaService.nuevaTareaTablero(tablero2.getId(), usuarioId, "Tarea del tablero 2", EstadoTarea.EN_PROGRESO);

        // WHEN
        List<TareaData> tareas = tareaService.tareasTablero(tablero1.getId());

        // THEN
        // Solo obtenemos las tareas del tablero solicitado.
        assertThat(tareas).extracting(TareaData::getTitulo).containsExactly("Tarea del tablero 1");
    }


    @Test
    public void testNuevaTareaTableroRechazaTituloVacio() {
        // GIVEN
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");
        Equipo equipo = equipoRepository.save(new Equipo("Equipo de prueba"));
        TableroData tablero = tableroService.crearTablero(equipo.getId(),"Tablero de prueba");

        // WHEN, THEN
        assertThatThrownBy(() -> tareaService.nuevaTareaTablero(tablero.getId(), usuarioId, "   ", EstadoTarea.PENDIENTE)).isInstanceOf(TareaServiceException.class);
    }

    @Test
    public void testAsignarUsuarioATarea() {
        // GIVEN
        Map<String, Long> ids = crearDatosParaAsignacion();
        Long tareaId = ids.get("tareaId");
        Long usuarioId = ids.get("usuarioMiembroId");

        // WHEN
        tareaService.asignarUsuarioATarea(tareaId, usuarioId);

        // THEN
        Tarea tareaBD = tareaRepository.findById(tareaId).orElseThrow();

        assertThat(tareaBD.getUsuarioAsignado()).isNotNull();
        assertThat(tareaBD.getUsuarioAsignado().getId()).isEqualTo(usuarioId);
    }

    @Test
    public void testNoSePuedeAsignarUsuarioDeOtroEquipo() {
        // GIVEN
        Map<String, Long> ids = crearDatosParaAsignacion();
        Long tareaId = ids.get("tareaId");
        Long usuarioExternoId = ids.get("usuarioExternoId");

        // WHEN, THEN
        assertThatThrownBy(() -> tareaService.asignarUsuarioATarea(tareaId, usuarioExternoId)).isInstanceOf(TareaServiceException.class);
        Tarea tareaBD = tareaRepository.findById(tareaId).orElseThrow();
        assertThat(tareaBD.getUsuarioAsignado()).isNull();
    }

    @Test
    public void testDesasignarUsuarioDeTarea() {
        // GIVEN
        Map<String, Long> ids = crearDatosParaAsignacion();
        Long tareaId = ids.get("tareaId");
        Long usuarioId = ids.get("usuarioMiembroId");

        tareaService.asignarUsuarioATarea(tareaId, usuarioId);

        // WHEN
        tareaService.desasignarUsuarioDeTarea(tareaId);

        // THEN
        Tarea tareaBD = tareaRepository.findById(tareaId).orElseThrow();
        assertThat(tareaBD.getUsuarioAsignado()).isNull();
    }
}
