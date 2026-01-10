package co.edu.unicauca.carrito.service;

import co.edu.unicauca.carrito.dto.ProductoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductoService {
    Page<ProductoDTO> listarTodos(Pageable pageable);

    Page<ProductoDTO> buscarPorNombre(String nombre, Pageable pageable);

    ProductoDTO buscarPorId(Long id);

    ProductoDTO guardar(ProductoDTO productoDTO);

    ProductoDTO actualizar(Long id, ProductoDTO productoDTO);

    void eliminar(Long id);
}
