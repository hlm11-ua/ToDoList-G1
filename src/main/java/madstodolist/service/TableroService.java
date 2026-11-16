package madstodolist.service;

import madstodolist.dto.TableroData;
import madstodolist.model.Equipo;
import madstodolist.model.Tablero;
import madstodolist.model.Tarea;
import madstodolist.repository.EquipoRepository;
import madstodolist.repository.TableroRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import madstodolist.dto.UsuarioData;

import java.util.ArrayList;
import java.util.List;

@Service
public class TableroService {

    @Autowired
    private TableroRepository tableroRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private EquipoService equipoService;

    @Transactional
    public TableroData crearTablero(Long equipoId, String nombre) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del tablero no puede estar vacío"
            );
        }

        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Equipo no encontrado")
                );

        Tablero tablero = new Tablero(nombre);
        equipo.addTablero(tablero);

        tableroRepository.save(tablero);

        return new TableroData(tablero.getId(), tablero.getNombre());
    }

    @Transactional(readOnly = true)
    public List<TableroData> tablerosEquipo(Long equipoId) {

        if (!equipoRepository.existsById(equipoId)) {
            throw new IllegalArgumentException("Equipo no encontrado");
        }

        List<Tablero> tableros =
                tableroRepository.findByEquipo_IdOrderByIdAsc(equipoId);

        List<TableroData> resultado = new ArrayList<>();

        for (Tablero tablero : tableros) {
            resultado.add(
                    new TableroData(tablero.getId(), tablero.getNombre())
            );
        }

        return resultado;
    }

    @Transactional(readOnly = true)
    public TableroData recuperarTablero(Long id) {
        Tablero tablero = tableroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tablero no encontrado"));

        return new TableroData(tablero.getId(), tablero.getNombre());
    }

    @Transactional(readOnly = true)
    public List<UsuarioData> usuariosTablero(Long tableroId) {
        Tablero tablero = tableroRepository.findById(tableroId).orElseThrow(() -> new IllegalArgumentException("Tablero no encontrado"));
        Long equipoId = tablero.getEquipo().getId();
        return equipoService.usuariosEquipo(equipoId);
    }

    @Transactional
    public TableroData modificarTablero(Long idTablero, String nuevoNombre) {
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del tablero no puede estar vacío");
        }

        Tablero tablero = tableroRepository.findById(idTablero)
                .orElseThrow(() -> new IllegalArgumentException("Tablero no encontrado"));

        tablero.setNombre(nuevoNombre);
        tableroRepository.save(tablero);

        return new TableroData(tablero.getId(), tablero.getNombre());
    }

    @Transactional
    public void borrarTablero(Long idTablero) {
        Tablero tablero = tableroRepository.findById(idTablero)
                .orElseThrow(() -> new IllegalArgumentException("Tablero no encontrado"));

        // 1. Desvincular (o eliminar) las tareas asociadas para evitar error de FK en PostgreSQL
        for (Tarea tarea : new ArrayList<>(tablero.getTareas())) {
            tarea.setTablero(null);
        }
        tablero.getTareas().clear();

        // 2. Quitar el tablero de la colección del equipo en memoria
        if (tablero.getEquipo() != null) {
            tablero.getEquipo().getTableros().remove(tablero);
        }

        // 3. Eliminar el tablero
        tableroRepository.delete(tablero);
    }
}