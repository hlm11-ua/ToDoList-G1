package madstodolist.repository;

import madstodolist.model.Equipo;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface EquipoRepository extends CrudRepository<Equipo, Long> {
    public List<Equipo> findAll();
    public Optional<Equipo> findByNombre(String name);
}
