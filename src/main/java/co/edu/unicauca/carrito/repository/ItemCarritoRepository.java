package co.edu.unicauca.carrito.repository;

import co.edu.unicauca.carrito.model.ItemCarrito;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {
}
