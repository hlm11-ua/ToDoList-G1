package madstodolist.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "equipos" )
public class Equipo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String nombre;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "equipo_usuario",
            joinColumns = { @JoinColumn(name = "fk_equipo") },
            inverseJoinColumns = {@JoinColumn(name = "fk_usuario")})
    Set<Usuario> usuarios = new HashSet<>();


    public Equipo(String nombre) {this.nombre = nombre;}

    public Equipo() {}

    public String getNombre() {return nombre;}

    public void setNombre(String nombre) {this.nombre = nombre;}

    public Long getId() {return id;}
    public void setId(Long id){ this.id=id;}

    public Set<Usuario> getUsuarios() {
        return usuarios;
    }

    public void addUsuario(Usuario usuario) {
        // Hay que actualiar ambas colecciones, porque
        // JPA/Hibernate no lo hace automáticamente
        this.getUsuarios().add(usuario);
        usuario.getEquipos().add(this);
    }

    public static List<Equipo> ordenarPorNombre (List<Equipo> equipoList){
        int size = equipoList.size();

        // 1. Construir el Max-Heap
        for (int i = size / 2 - 1; i >= 0; i--) {
            heapify(equipoList, size, i);
        }

        // 2. Extraer elementos uno a uno
        for (int i = size - 1; i >= 0; i--) {
            Collections.swap(equipoList, 0, i);
            heapify(equipoList, i, 0);
        }

        return equipoList;
    }

    public static void heapify(List<Equipo> arr, int size, int i) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        if (left < size && arr.get(left).nombre.compareTo(arr.get(largest).nombre) > 0) {
            largest = left;
        }

        if (right < size && arr.get(right).nombre.compareTo(arr.get(largest).nombre) > 0) {
            largest = right;
        }

        if (largest != i) {
            Collections.swap(arr, i, largest);
            heapify(arr, size, largest);
        }
    }

    public void removeUsuario(Usuario usuario) {
        this.getUsuarios().remove(usuario);
        usuario.getEquipos().remove(this);
    }


    @Override
    public int hashCode() {return Objects.hashCode(nombre);}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipo equipo = (Equipo) o;
        if (id != null && equipo.id != null)
            // Si tenemos los ID, comparamos por ID
            return Objects.equals(id, equipo.id);
        // si no comparamos por campos obligatorios
        return nombre.equals(equipo.nombre);
    }
}