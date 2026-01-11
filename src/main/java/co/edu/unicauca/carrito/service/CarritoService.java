package co.edu.unicauca.carrito.service;

import co.edu.unicauca.carrito.model.Carrito;
import co.edu.unicauca.carrito.model.Orden;
import co.edu.unicauca.carrito.model.Usuario;

import java.math.BigDecimal;

public interface CarritoService {
    Carrito obtenerCarritoActivo(Usuario usuario);

    void agregarProducto(Long carritoId, Long productoId, Integer cantidad);

    void actualizarCantidad(Long itemId, Integer nuevaCantidad);

    void eliminarItem(Long itemId);

    void vaciarCarrito(Long carritoId);

    BigDecimal calcularTotal(Long carritoId);

    Orden confirmarCompra(Long carritoId);
}
