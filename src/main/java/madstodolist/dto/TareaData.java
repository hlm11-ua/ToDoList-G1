package madstodolist.dto;

import java.io.Serializable;
import java.util.Objects;
import madstodolist.model.EstadoTarea;

// Data Transfer Object para la clase Tarea
public class TareaData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String titulo;
    private Long usuarioId;  // Esta es la ID del usuario asociado

    private Long tableroId;
    private Long usuarioAsignadoId;
    private String usuarioAsignadoNombre;
    private EstadoTarea estado;

    // Getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getTableroId() {
        return tableroId;
    }

    public void setTableroId(Long tableroId) {
        this.tableroId = tableroId;
    }

    public Long getUsuarioAsignadoId() {
        return usuarioAsignadoId;
    }

    public void setUsuarioAsignadoId(Long usuarioAsignadoId) {
        this.usuarioAsignadoId = usuarioAsignadoId;
    }

    public String getUsuarioAsignadoNombre() {
        return usuarioAsignadoNombre;
    }

    public void setUsuarioAsignadoNombre(String usuarioAsignadoNombre) {
        this.usuarioAsignadoNombre = usuarioAsignadoNombre;
    }

    public EstadoTarea getEstado() {
        return estado;
    }

    public void setEstado(EstadoTarea estado) {
        this.estado = estado;
    }

    // Sobreescribimos equals y hashCode para que dos tareas sean iguales
    // si tienen el mismo ID (ignoramos el resto de atributos)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TareaData)) return false;
        TareaData tareaData = (TareaData) o;
        return Objects.equals(id, tareaData.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
