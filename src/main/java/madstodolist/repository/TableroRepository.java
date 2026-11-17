package madstodolist.repository;

import madstodolist.model.Tablero;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TableroRepository extends CrudRepository<Tablero, Long>  {
    List<Tablero> findByEquipo_IdOrderByIdAsc(Long equipoId);
}
