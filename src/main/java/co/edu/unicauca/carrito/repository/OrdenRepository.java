package co.edu.unicauca.carrito.repository;

import co.edu.unicauca.carrito.model.Orden;
import co.edu.unicauca.carrito.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, Long> {
    List<Orden> findByUsuario(Usuario usuario);
}
