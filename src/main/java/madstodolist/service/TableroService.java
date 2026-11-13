package madstodolist.service;

import madstodolist.dto.TableroData;
import madstodolist.model.Equipo;
import madstodolist.model.Tablero;
import madstodolist.repository.EquipoRepository;
import madstodolist.repository.TableroRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TableroService {

    @Autowired
    private TableroRepository tableroRepository;

    @Autowired
    private EquipoRepository equipoRepository;

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
}