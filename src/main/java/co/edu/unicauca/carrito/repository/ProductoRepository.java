package co.edu.unicauca.carrito.repository;

import co.edu.unicauca.carrito.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByActivoTrue();

    Page<Producto> findByActivoTrue(Pageable pageable);

    // For search
    Page<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre, Pageable pageable);
}
