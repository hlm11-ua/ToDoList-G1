package madstodolist.service;

import madstodolist.dto.EquipoData;
import madstodolist.dto.TableroData;
import madstodolist.dto.UsuarioData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class TableroServiceTest {

    @Autowired
    private TableroService tableroService;

    @Autowired
    private EquipoService equipoService;

    @Test
    public void modificarTableroCambiaElNombre() {
        // GIVEN: Un equipo y un tablero creado dentro de ese equipo
        EquipoData equipo = equipoService.crearEquipo("Equipo MADS");
        TableroData tablero = tableroService.crearTablero(equipo.getId(), "Tablero Sprint 1");

        // WHEN: Modificamos el nombre del tablero
        tableroService.modificarTablero(tablero.getId(), "Tablero Sprint 1 Actualizado");

        // THEN: Al recuperarlo tiene el nuevo nombre
        TableroData tableroModificado = tableroService.recuperarTablero(tablero.getId());
        assertThat(tableroModificado.getNombre()).isEqualTo("Tablero Sprint 1 Actualizado");
    }

    @Test
    public void modificarTableroConNombreVacioLanzaExcepcion() {
        // GIVEN: Un equipo y un tablero válido
        EquipoData equipo = equipoService.crearEquipo("Equipo MADS");
        TableroData tablero = tableroService.crearTablero(equipo.getId(), "Tablero Original");

        // WHEN & THEN: Pasar un nombre vacío o nulo lanza IllegalArgumentException
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            tableroService.modificarTablero(tablero.getId(), "");
        });
    }

    @Test
    public void borrarTableroLoEliminaDeLaBD() {
        // GIVEN: Un equipo y un tablero creado
        EquipoData equipo = equipoService.crearEquipo("Equipo MADS");
        TableroData tablero = tableroService.crearTablero(equipo.getId(), "Tablero Temporal");

        // WHEN: Borramos el tablero
        tableroService.borrarTablero(tablero.getId());

        // THEN: Al intentar recuperarlo lanza IllegalArgumentException ("Tablero no encontrado")
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            tableroService.recuperarTablero(tablero.getId());
        });
    }

    @Test
    public void borrarTableroInexistenteLanzaExcepcion() {
        // WHEN & THEN: Intentar borrar un tablero que no existe lanza IllegalArgumentException
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            tableroService.borrarTablero(999L);
        });
    }
}