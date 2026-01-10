package co.edu.unicauca.carrito.repository;

import co.edu.unicauca.carrito.model.Carrito;
import co.edu.unicauca.carrito.model.EstadoCarrito;
import co.edu.unicauca.carrito.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    Optional<Carrito> findByUsuarioAndEstado(Usuario usuario, EstadoCarrito estado);
}
