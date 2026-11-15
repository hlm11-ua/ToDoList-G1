package madstodolist.service;

import madstodolist.model.*;
import madstodolist.repository.TableroRepository;
import madstodolist.repository.TareaRepository;
import madstodolist.repository.UsuarioRepository;
import madstodolist.dto.TareaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.stream.Collectors;


@Service
public class TareaService {

    Logger logger = LoggerFactory.getLogger(TareaService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private TableroRepository tableroRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public TareaData nuevaTareaUsuario(Long idUsuario, String tituloTarea) {
        logger.debug("Añadiendo tarea " + tituloTarea + " al usuario " + idUsuario);
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) {
            throw new TareaServiceException("Usuario " + idUsuario + " no existe al crear tarea " + tituloTarea);
        }
        Tarea tarea = new Tarea(usuario, tituloTarea);
        tareaRepository.save(tarea);
        return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional(readOnly = true)
    public List<TareaData> allTareasUsuario(Long idUsuario) {
        logger.debug("Devolviendo todas las tareas del usuario " + idUsuario);
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) {
            throw new TareaServiceException("Usuario " + idUsuario + " no existe al listar tareas ");
        }
        // Hacemos uso de Java Stream API para mapear la lista de entidades a DTOs.
        List<TareaData> tareas = usuario.getTareas().stream()
                .map(tarea -> modelMapper.map(tarea, TareaData.class))
                .collect(Collectors.toList());
        // Ordenamos la lista por id de tarea
        Collections.sort(tareas, (a, b) -> a.getId() < b.getId() ? -1 : a.getId() == b.getId() ? 0 : 1);
        return tareas;
    }

    @Transactional(readOnly = true)
    public TareaData findById(Long tareaId) {
        logger.debug("Buscando tarea " + tareaId);
        Tarea tarea = tareaRepository.findById(tareaId).orElse(null);
        if (tarea == null) return null;
        else return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional
    public TareaData modificaTarea(Long idTarea, String nuevoTitulo) {
        logger.debug("Modificando tarea " + idTarea + " - " + nuevoTitulo);
        Tarea tarea = tareaRepository.findById(idTarea).orElse(null);
        if (tarea == null) {
            throw new TareaServiceException("No existe tarea con id " + idTarea);
        }
        tarea.setTitulo(nuevoTitulo);
        tarea = tareaRepository.save(tarea);
        return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional
    public void borraTarea(Long idTarea) {
        logger.debug("Borrando tarea " + idTarea);
        Tarea tarea = tareaRepository.findById(idTarea).orElse(null);
        if (tarea == null) {
            throw new TareaServiceException("No existe tarea con id " + idTarea);
        }
        tareaRepository.delete(tarea);
    }

    @Transactional
    public boolean usuarioContieneTarea(Long usuarioId, Long tareaId) {
        Tarea tarea = tareaRepository.findById(tareaId).orElse(null);
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (tarea == null || usuario == null) {
            throw new TareaServiceException("No existe tarea o usuario id");
        }
        return usuario.getTareas().contains(tarea);
    }

    @Transactional
    public TareaData nuevaTareaTablero(Long idTablero, Long idUsuario, String titulo, EstadoTarea estado) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new TareaServiceException("El título de la tarea no puede estar vacío");
        }

        Tablero tablero = tableroRepository.findById(idTablero).orElse(null);
        if (tablero == null) {
            throw new TareaServiceException("No existe el tablero");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) {
            throw new TareaServiceException("No existe el usuario");
        }

        Tarea tarea = new Tarea(usuario, titulo.trim());
        tarea.setTablero(tablero);
        tarea.setEstado(estado == null ? EstadoTarea.PENDIENTE : estado);

        tareaRepository.save(tarea);

        TareaData resultado = new TareaData();
        resultado.setId(tarea.getId());
        resultado.setTitulo(tarea.getTitulo());
        resultado.setTableroId(tablero.getId());
        resultado.setEstado(tarea.getEstado());
        resultado.setUsuarioAsignadoId(tarea.getUsuarioAsignado() == null ? null : tarea.getUsuarioAsignado().getId());

        return resultado;
    }


    @Transactional(readOnly = true)
    public List<TareaData> tareasTablero(Long idTablero) {
        List<Tarea> tareas = tareaRepository.findByTablero_IdOrderByIdAsc(idTablero);
        List<TareaData> resultado = new ArrayList<>();

        for (Tarea tarea : tareas) {
            TareaData tareaData = new TareaData();
            tareaData.setId(tarea.getId());
            tareaData.setTitulo(tarea.getTitulo());
            tareaData.setTableroId(idTablero);
            tareaData.setEstado(tarea.getEstado());

            if (tarea.getUsuarioAsignado() != null) {
                tareaData.setUsuarioAsignadoId(tarea.getUsuarioAsignado().getId());
                tareaData.setUsuarioAsignadoNombre(tarea.getUsuarioAsignado().getNombre());
            }

            resultado.add(tareaData);
        }
        return resultado;
    }

    @Transactional
    public void asignarUsuarioATarea(Long tareaId, Long usuarioId) {
        Tarea tarea = tareaRepository.findById(tareaId).orElseThrow(() -> new TareaServiceException("La tarea no existe"));
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow(() -> new TareaServiceException("El usuario no existe"));

        if (tarea.getTablero() == null || tarea.getTablero().getEquipo() == null) {
            throw new TareaServiceException("La tarea no pertenece a un tablero de un equipo");
        }

        Equipo equipo = tarea.getTablero().getEquipo();

        if (!equipo.getUsuarios().contains(usuario)) {
            throw new TareaServiceException(
                    "El usuario no pertenece al equipo del tablero");
        }

        tarea.setUsuarioAsignado(usuario);
        tareaRepository.save(tarea);
    }

    @Transactional
    public void desasignarUsuarioDeTarea(Long tareaId) {
        Tarea tarea = tareaRepository.findById(tareaId).orElseThrow(() -> new TareaServiceException("La tarea no existe"));

        tarea.setUsuarioAsignado(null);
        tareaRepository.save(tarea);
    }

    @Transactional
    public TareaData modificaEstado(Long idTarea, EstadoTarea nuevoEstado){
        Tarea tarea = tareaRepository.findById(idTarea).orElse(null);
       if(tarea == null){
           throw new TareaServiceException("Id " + idTarea + " no existe");
       }
        tarea.setEstado(nuevoEstado);
        tareaRepository.save(tarea);
        return modelMapper.map(tarea, TareaData.class);
    }
}
