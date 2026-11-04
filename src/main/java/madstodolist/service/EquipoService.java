package madstodolist.service;

import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Equipo;
import madstodolist.model.Usuario;
import madstodolist.repository.EquipoRepository;
import madstodolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.yaml.snakeyaml.events.Event;

import java.nio.ReadOnlyBufferException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EquipoService {

    Logger logger = LoggerFactory.getLogger(TareaService.class);

    @Autowired
    private EquipoRepository equipoRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public EquipoData crearEquipo(String nombre) {
        logger.debug("creando equipo con nombre "+nombre+".");

        if(nombre.isBlank()){
            throw new EquipoServiceException("El nombre es nulo o caracter en blanco");
        }
        logger.debug("Nombre en formato correcto");

        Optional<Equipo> equipoBD = equipoRepository.findByNombre(nombre);

        if(equipoBD.isPresent()){
            throw new EquipoServiceException("Equipo encontrado con ese nombre");
        }
        logger.debug("Equipo con ese nombre inexistente");

        Equipo equipo = new Equipo(nombre);
        equipo = equipoRepository.save(equipo);
        return modelMapper.map(equipo, EquipoData.class);
    }

    @Transactional(readOnly = true)
    public EquipoData recuperarEquipo(Long id) {
        logger.debug("Recuperando equipo con id "+id+".");

        Equipo equipo= equipoRepository.findById(id).orElse(null);
        if (equipo == null) throw new EquipoServiceException("Equipo con id "+id+" no encontrado");
        else {
            return modelMapper.map(equipo, EquipoData.class);
        }
    }

    @Transactional(readOnly = true)
    public List<EquipoData> findAllOrdenadoPorNombre(){
        List<Equipo> equiposLista = equipoRepository.findAll();
        if(equiposLista.isEmpty()){
            return new ArrayList<>();
        }
        equiposLista = Equipo.ordenarPorNombre(equiposLista);

        List<EquipoData> equipoDataList = new ArrayList<>();
        for(Equipo equipo : equiposLista){
            EquipoData dto = modelMapper.map(equipo, EquipoData.class);

            List<UsuarioData> usuariosDto = new ArrayList<>();
            for (Usuario usuario : equipo.getUsuarios()) {
                usuariosDto.add(modelMapper.map(usuario, UsuarioData.class));
            }
            dto.setUsuarios(usuariosDto);

            equipoDataList.add(dto);
        }
        return equipoDataList;
    }

    @Transactional
    public void añadirUsuarioAEquipo(Long equipoID, Long usuarioID){
        Equipo equipo = equipoRepository.findById(equipoID).orElse(null);
        if (equipo == null) {
            throw new EquipoServiceException("Equipo con id "+equipoID+" no encontrado");
        }

        Usuario usuario = usuarioRepository.findById(usuarioID).orElse(null);
        if (usuario == null){
            throw new EquipoServiceException("Usuario con id "+usuarioID+" no encontrado");
        }
        equipo.addUsuario(usuario);
    }

    @Transactional(readOnly = true)
    public List<EquipoData> equiposUsuario(Long usuarioID) {
        Usuario usuario = usuarioRepository.findById(usuarioID).orElse(null);
        if (usuario == null) {
            throw new EquipoServiceException("Usuario con id "+usuarioID+" no encontrado");
        }
        
        List<Equipo> equipos = new ArrayList<>(usuario.getEquipos());

        equipos = Equipo.ordenarPorNombre(equipos);

        List<EquipoData> equipoDataList = new ArrayList<>();
        for (Equipo equipo : equipos) {
            equipoDataList.add(modelMapper.map(equipo, EquipoData.class));
        }

        return equipoDataList;
    }

    @Transactional(readOnly = true)
    public List<UsuarioData> usuariosEquipo(Long equipoID) {
        Equipo equipo = equipoRepository.findById(equipoID).orElse(null);
        if (equipo == null){
            throw new EquipoServiceException("Equipo con id "+ equipoID+" no encontrado");
        }

        List<Usuario> usuarios= new ArrayList<>(equipo.getUsuarios());

        List<UsuarioData> usuarioDataList = new ArrayList<>();
        for(Usuario usuario: usuarios){
            usuarioDataList.add(modelMapper.map(usuario, UsuarioData.class));
        }
        return usuarioDataList;
    }

    @Transactional
    public void eliminarUsuarioDelEquipo(Long equipoId, Long usuarioId) {
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new EquipoServiceException("Equipo no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EquipoServiceException("Usuario no encontrado"));

        if (!equipo.getUsuarios().contains(usuario)) {
            throw new EquipoServiceException("El usuario no pertenece al equipo");
        }
         equipo.removeUsuario(usuario);
    }

    @Transactional
    public EquipoData actualizarEquipo(Long equipoId, String nombre){
        if (nombre == null || nombre.isBlank()) {
            throw new EquipoServiceException("El nombre es nulo o caracter en blanco");
        }

        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new EquipoServiceException("Equipo con id " + equipoId + " no encontrado"));

        Optional<Equipo> otro = equipoRepository.findByNombre(nombre);
        if (otro.isPresent() && !otro.get().getId().equals(equipoId)) {
            throw new EquipoServiceException("Equipo encontrado con ese nombre");
        }

        equipo.setNombre(nombre);
        return modelMapper.map(equipo, EquipoData.class);
    }

    @Transactional
    public void eliminarEquipo (Long equipoId){
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new EquipoServiceException("Equipo con id " + equipoId + " no encontrado"));
        equipoRepository.delete(equipo);
    }
}