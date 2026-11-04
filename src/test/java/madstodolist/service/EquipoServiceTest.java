package madstodolist.service;

import madstodolist.dto.UsuarioData;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import madstodolist.dto.EquipoData;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class EquipoServiceTest {

    @Autowired
    EquipoService equipoService;
    @Autowired
    UsuarioService usuarioService;

    @Test
    public void crearRecuperarEquipo() {
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        assertThat(equipo.getId()).isNotNull();

        EquipoData equipoBd = equipoService.recuperarEquipo(equipo.getId());
        assertThat(equipoBd).isNotNull();
        assertThat(equipoBd.getNombre()).isEqualTo("Proyecto 1");
    }

    @Test
    public void listadoEquiposOrdenAlfabetico() {
        // GIVEN
        // Dos equipos en la base de datos
        equipoService.crearEquipo("Proyecto BBB");
        equipoService.crearEquipo("Proyecto AAA");

        // WHEN
        // Recuperamos los equipos
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();

        // THEN
        // Los equipos están ordenados por nombre
        assertThat(equipos).hasSize(2);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Proyecto AAA");
        assertThat(equipos.get(1).getNombre()).isEqualTo("Proyecto BBB");
    }

    @Test
    public void recuperarEquiposDeUsuario() {
        // GIVEN
        // Un usuario y dos equipos en la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipo1 = equipoService.crearEquipo("Proyecto 1");
        EquipoData equipo2 = equipoService.crearEquipo("Proyecto 2");
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), usuario.getId());
        equipoService.añadirUsuarioAEquipo(equipo2.getId(), usuario.getId());

        // WHEN
        // Recuperamos los equipos del usuario
        List<EquipoData> equipos = equipoService.equiposUsuario(usuario.getId());

        // THEN
        // El usuario pertenece a los dos equipos
        assertThat(equipos).hasSize(2);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Proyecto 1");
        assertThat(equipos.get(1).getNombre()).isEqualTo("Proyecto 2");
    }
    @Test
    public void recuperarUsuariosDeEquipo(){
        EquipoData equipo= equipoService.crearEquipo("Proyecto");

        UsuarioData user1 = new UsuarioData();
        UsuarioData user2 = new UsuarioData();
        user1.setEmail("user1@ua");
        user1.setPassword("1234");
        user1=usuarioService.registrar(user1);

        user2.setEmail("user2@ua");
        user2.setPassword("1234");
        user2=usuarioService.registrar(user2);

        equipoService.añadirUsuarioAEquipo(equipo.getId(), user1.getId());
        equipoService.añadirUsuarioAEquipo(equipo.getId(), user2.getId());

        List<UsuarioData> users = equipoService.usuariosEquipo(equipo.getId());

        assertThat(users).hasSize(2);
        assertThat(users.get(0).getEmail()).isEqualTo("user1@ua");
        assertThat(users.get(1).getEmail()).isEqualTo("user2@ua");
    }
    @Test
    public void comprobarExcepciones() {
        // Comprobamos las excepciones lanzadas por los métodos
        // recuperarEquipo, añadirUsuarioAEquipo, usuariosEquipo y equiposUsuario
        assertThatThrownBy(() -> equipoService.recuperarEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(1L, 1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.usuariosEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.equiposUsuario(1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.eliminarUsuarioDelEquipo(1L, 1L))
                .isInstanceOf(EquipoServiceException.class);

        // Creamos un equipo pero no un usuario y comprobamos que también se lanza una excepción
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(equipo.getId(), 1L))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void eliminarUsuarioDelEquipo() {
        // Creamos un equipo y un usuario
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);

        // Añadimos el usuario al equipo
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // Comprobamos que el usuario está en el equipo
        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuarios).hasSize(1);
        assertThat(usuarios.get(0).getId()).isEqualTo(usuario.getId());

        // Eliminamos el usuario del equipo
        equipoService.eliminarUsuarioDelEquipo(equipo.getId(), usuario.getId());

        // Comprobamos que el usuario ya no está en el equipo
        usuarios = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuarios).isEmpty();
    }

    @Test
    public void listadoEquiposIncluyeUsuariosDeCadaEquipo() {
        // GIVEN
        // Dos equipos, uno con un usuario y otro vacío
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipoConUsuario = equipoService.crearEquipo("Proyecto AAA");
        equipoService.crearEquipo("Proyecto BBB");
        equipoService.añadirUsuarioAEquipo(equipoConUsuario.getId(), usuario.getId());

        // WHEN
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();

        // THEN
        // Cada DTO lleva su lista de usuarios
        assertThat(equipos).hasSize(2);
        assertThat(equipos.get(0).getUsuarios()).hasSize(1);
        assertThat(equipos.get(0).getUsuarios().get(0).getEmail()).isEqualTo("user@ua");
        assertThat(equipos.get(1).getUsuarios()).isEmpty();
    }

    @Test
    public void listadoEquiposVacioDevuelveListaVacia() {
        assertThat(equipoService.findAllOrdenadoPorNombre()).isEmpty();
    }

    @Test
    public void crearEquipoConNombreEnBlancoLanzaExcepcion() {
        assertThatThrownBy(() -> equipoService.crearEquipo(""))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.crearEquipo("   "))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void crearEquipoConNombreRepetidoLanzaExcepcion() {
        equipoService.crearEquipo("Proyecto 1");

        assertThatThrownBy(() -> equipoService.crearEquipo("Proyecto 1"))
                .isInstanceOf(EquipoServiceException.class);
        assertThat(equipoService.findAllOrdenadoPorNombre()).hasSize(1);
    }

    @Test
    public void actualizarEquipoCambiaElNombre() {
        // GIVEN
        // Un equipo en la base de datos
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");

        // WHEN
        // Le cambiamos el nombre
        EquipoData actualizado = equipoService.actualizarEquipo(equipo.getId(), "Proyecto Renombrado");

        // THEN
        // El cambio se devuelve y queda guardado en la base de datos
        assertThat(actualizado.getId()).isEqualTo(equipo.getId());
        assertThat(actualizado.getNombre()).isEqualTo("Proyecto Renombrado");
        assertThat(equipoService.recuperarEquipo(equipo.getId()).getNombre()).isEqualTo("Proyecto Renombrado");
    }

    @Test
    public void actualizarEquipoMantieneSusUsuarios() {
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        equipoService.actualizarEquipo(equipo.getId(), "Proyecto Renombrado");

        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuarios).hasSize(1);
        assertThat(usuarios.get(0).getId()).isEqualTo(usuario.getId());
    }

    @Test
    public void actualizarEquipoConSuMismoNombreNoLanzaExcepcion() {
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");

        EquipoData actualizado = equipoService.actualizarEquipo(equipo.getId(), "Proyecto 1");

        assertThat(actualizado.getNombre()).isEqualTo("Proyecto 1");
    }

    @Test
    public void actualizarEquipoInexistenteLanzaExcepcion() {
        assertThatThrownBy(() -> equipoService.actualizarEquipo(1L, "Proyecto 1"))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void actualizarEquipoConNombreNuloOEnBlancoLanzaExcepcion() {
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");

        assertThatThrownBy(() -> equipoService.actualizarEquipo(equipo.getId(), null))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.actualizarEquipo(equipo.getId(), ""))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.actualizarEquipo(equipo.getId(), "   "))
                .isInstanceOf(EquipoServiceException.class);

        // El nombre no ha cambiado
        assertThat(equipoService.recuperarEquipo(equipo.getId()).getNombre()).isEqualTo("Proyecto 1");
    }

    @Test
    public void actualizarEquipoConNombreDeOtroEquipoLanzaExcepcion() {
        equipoService.crearEquipo("Proyecto 1");
        EquipoData equipo2 = equipoService.crearEquipo("Proyecto 2");

        assertThatThrownBy(() -> equipoService.actualizarEquipo(equipo2.getId(), "Proyecto 1"))
                .isInstanceOf(EquipoServiceException.class)
                .hasMessage("Equipo encontrado con ese nombre");

        // El nombre no ha cambiado
        assertThat(equipoService.recuperarEquipo(equipo2.getId()).getNombre()).isEqualTo("Proyecto 2");
    }

    @Test
    public void eliminarEquipoLoBorraDeLaBaseDeDatos() {
        // GIVEN
        // Un equipo en la base de datos
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");

        // WHEN
        // Lo eliminamos
        equipoService.eliminarEquipo(equipo.getId());

        // THEN
        // Ya no se puede recuperar ni aparece en el listado
        assertThatThrownBy(() -> equipoService.recuperarEquipo(equipo.getId()))
                .isInstanceOf(EquipoServiceException.class);
        assertThat(equipoService.findAllOrdenadoPorNombre()).isEmpty();
    }

    @Test
    public void eliminarEquipoNoBorraLosDemasEquipos() {
        EquipoData equipo1 = equipoService.crearEquipo("Proyecto 1");
        EquipoData equipo2 = equipoService.crearEquipo("Proyecto 2");

        equipoService.eliminarEquipo(equipo1.getId());

        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
        assertThat(equipos).hasSize(1);
        assertThat(equipos.get(0).getId()).isEqualTo(equipo2.getId());
    }

    @Test
    public void eliminarEquipoConUsuariosNoBorraLosUsuarios() {
        // GIVEN
        // Un equipo con un usuario
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // WHEN
        // Eliminamos el equipo
        equipoService.eliminarEquipo(equipo.getId());

        // THEN
        // El usuario sigue existiendo y ya no pertenece a ningún equipo
        assertThat(usuarioService.findById(usuario.getId())).isNotNull();
        assertThat(equipoService.equiposUsuario(usuario.getId())).isEmpty();
    }

    @Test
    public void eliminarEquipoInexistenteLanzaExcepcion() {
        assertThatThrownBy(() -> equipoService.eliminarEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void eliminarUsuarioQueNoPerteneceAlEquipoLanzaExcepcion() {
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        UsuarioData usuarioRegistrado = usuarioService.registrar(usuario);

        assertThatThrownBy(() -> equipoService.eliminarUsuarioDelEquipo(equipo.getId(), usuarioRegistrado.getId()))
                .isInstanceOf(EquipoServiceException.class)
                .hasMessage("El usuario no pertenece al equipo");
    }

}