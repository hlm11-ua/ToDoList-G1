package madstodolist.service;

import madstodolist.dto.UsuarioData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    // Método para inicializar los datos de prueba en la BD
    // Devuelve el identificador del usuario de la BD
    Long addUsuarioBD() {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setNombre("Usuario Ejemplo");
        usuario.setPassword("123");
        UsuarioData nuevoUsuario = usuarioService.registrar(usuario);
        return nuevoUsuario.getId();
    }

    @Test
    public void servicioLoginUsuario() {
        // GIVEN
        // Un usuario en la BD

        addUsuarioBD();

        // WHEN
        // intentamos logear un usuario y contraseña correctos
        UsuarioService.LoginStatus loginStatus1 = usuarioService.login("user@ua", "123");

        // intentamos logear un usuario correcto, con una contraseña incorrecta
        UsuarioService.LoginStatus loginStatus2 = usuarioService.login("user@ua", "000");

        // intentamos logear un usuario que no existe,
        UsuarioService.LoginStatus loginStatus3 = usuarioService.login("pepito.perez@gmail.com", "12345678");

        // THEN

        // el valor devuelto por el primer login es LOGIN_OK,
        assertThat(loginStatus1).isEqualTo(UsuarioService.LoginStatus.LOGIN_OK);

        // el valor devuelto por el segundo login es ERROR_PASSWORD,
        assertThat(loginStatus2).isEqualTo(UsuarioService.LoginStatus.ERROR_PASSWORD);

        // y el valor devuelto por el tercer login es USER_NOT_FOUND.
        assertThat(loginStatus3).isEqualTo(UsuarioService.LoginStatus.USER_NOT_FOUND);
    }

    @Test
    public void servicioLoginUsuarioBloqueado() {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("bloqueado@ua");
        usuario.setPassword("123");
        usuario.setBloqueado(true);
        usuarioService.registrar(usuario);

        assertThat(usuarioService.login("bloqueado@ua", "123"))
                .isEqualTo(UsuarioService.LoginStatus.USER_BLOCKED);
    }

    @Test
    public void servicioCambiarBloqueoAlternaEstado() {
        Long usuarioId = addUsuarioBD();

        UsuarioData bloqueado = usuarioService.cambiarBloqueo(usuarioId);
        assertThat(bloqueado.isBloqueado()).isTrue();

        UsuarioData habilitado = usuarioService.cambiarBloqueo(usuarioId);
        assertThat(habilitado.isBloqueado()).isFalse();
    }

    @Test
    public void servicioRegistroUsuario() {
        // WHEN
        // Registramos un usuario con un e-mail no existente en la base de datos,

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("usuario.prueba2@gmail.com");
        usuario.setPassword("12345678");

        usuarioService.registrar(usuario);

        // THEN
        // el usuario se añade correctamente al sistema.

        UsuarioData usuarioBaseDatos = usuarioService.findByEmail("usuario.prueba2@gmail.com");
        assertThat(usuarioBaseDatos).isNotNull();
        assertThat(usuarioBaseDatos.getEmail()).isEqualTo("usuario.prueba2@gmail.com");
    }

    @Test
    public void servicioRegistroUsuarioGuardaAdmin() {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("admin@ua");
        usuario.setPassword("12345678");
        usuario.setAdmin(true);

        UsuarioData usuarioRegistro = usuarioService.registrar(usuario);
        UsuarioData usuarioGuardado = usuarioService.findByEmail("admin@ua");

        assertThat(usuarioRegistro.isAdmin()).isTrue();
        assertThat(usuarioGuardado).isNotNull();
        assertThat(usuarioGuardado.isAdmin()).isTrue();
    }

    @Test
    public void servicioRegistroUsuarioExcepcionConNullPassword() {
        // WHEN, THEN
        // Si intentamos registrar un usuario con un password null,
        // se produce una excepción de tipo UsuarioServiceException

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("usuario.prueba@gmail.com");

        Assertions.assertThrows(UsuarioServiceException.class, () -> {
            usuarioService.registrar(usuario);
        });
    }


    @Test
    public void servicioRegistroUsuarioExcepcionConEmailRepetido() {
        // GIVEN
        // Un usuario en la BD

        addUsuarioBD();

        // THEN
        // Si registramos un usuario con un e-mail ya existente en la base de datos,
        // , se produce una excepción de tipo UsuarioServiceException

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("12345678");

        Assertions.assertThrows(UsuarioServiceException.class, () -> {
            usuarioService.registrar(usuario);
        });
    }

    @Test
    public void servicioRegistroUsuarioDevuelveUsuarioConId() {

        // WHEN
        // Si registramos en el sistema un usuario con un e-mail no existente en la base de datos,
        // y un password no nulo,

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("usuario.prueba@gmail.com");
        usuario.setPassword("12345678");

        UsuarioData usuarioNuevo = usuarioService.registrar(usuario);

        // THEN
        // se actualiza el identificador del usuario

        assertThat(usuarioNuevo.getId()).isNotNull();

        // con el identificador que se ha guardado en la BD.

        UsuarioData usuarioBD = usuarioService.findById(usuarioNuevo.getId());
        assertThat(usuarioBD).isEqualTo(usuarioNuevo);
    }

    @Test
    public void servicioConsultaUsuarioDevuelveUsuario() {
        // GIVEN
        // Un usuario en la BD

        Long usuarioId = addUsuarioBD();

        // WHEN
        // recuperamos un usuario usando su e-mail,

        UsuarioData usuario = usuarioService.findByEmail("user@ua");

        // THEN
        // el usuario obtenido es el correcto.

        assertThat(usuario.getId()).isEqualTo(usuarioId);
        assertThat(usuario.getEmail()).isEqualTo("user@ua");
        assertThat(usuario.getNombre()).isEqualTo("Usuario Ejemplo");
    }

    @Test
    public void servicioFindAllDevuelveTodosLosUsuarios() {
        // GIVEN
        // Guardamos dos usuarios en la base de datos
        addUsuarioBD(); // Guarda "user@ua"

        UsuarioData usuario2 = new UsuarioData();
        usuario2.setEmail("usuario2@ua");
        usuario2.setNombre("Segundo Usuario");
        usuario2.setPassword("456");
        usuarioService.registrar(usuario2);

        // WHEN
        // Recuperamos todos los usuarios registrados
        Iterable<UsuarioData> usuarios = usuarioService.findAll();

        // THEN
        // Verificamos que contenga exactamente 2 elementos con los emails esperados
        assertThat(usuarios)
                .hasSize(2)
                .extracting(UsuarioData::getEmail)
                .containsExactlyInAnyOrder("user@ua", "usuario2@ua");
    }

    @Test
    public void servicioFindAllConBDVaciaDevuelveListaVacia() {
        // WHEN
        // Si no hay usuarios en la BD
        Iterable<UsuarioData> usuarios = usuarioService.findAll();

        // THEN
        // Devuelve una colección vacía (no nula)
        assertThat(usuarios).isEmpty();
    }
}