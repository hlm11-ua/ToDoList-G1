package madstodolist.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EquipoData {
    private Long id;
    private String nombre;
    private List<UsuarioData> usuarios = new ArrayList<>();

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public List<UsuarioData> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<UsuarioData> usuarios) {
        this.usuarios = (usuarios != null) ? usuarios : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EquipoData equipo = (EquipoData) o;
        if (id != null && equipo.id != null)
            // Si tenemos los ID, comparamos por ID
            return Objects.equals(id, equipo.id);
        // si no comparamos por campos obligatorios
        return nombre.equals(equipo.nombre);
    }

    @Override
    public int hashCode() {
        // Generamos un hash basado en los campos obligatorios
        return Objects.hash(nombre);
    }
}