package co.edu.unicauca.carrito.service;

import co.edu.unicauca.carrito.model.*;
import co.edu.unicauca.carrito.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
public class CarritoServiceImpl implements CarritoService {

    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;
    private final OrdenRepository ordenRepository;
    private final ItemCarritoRepository itemCarritoRepository;

    public CarritoServiceImpl(CarritoRepository carritoRepository,
            ProductoRepository productoRepository,
            OrdenRepository ordenRepository,
            ItemCarritoRepository itemCarritoRepository) {
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
        this.ordenRepository = ordenRepository;
        this.itemCarritoRepository = itemCarritoRepository;
    }

    @Override
    public Carrito obtenerCarritoActivo(Usuario usuario) {
        return carritoRepository.findByUsuarioAndEstado(usuario, EstadoCarrito.ACTIVO)
                .orElseGet(() -> {
                    Carrito nuevoCarrito = new Carrito();
                    nuevoCarrito.setUsuario(usuario);
                    nuevoCarrito.setEstado(EstadoCarrito.ACTIVO);
                    return carritoRepository.save(nuevoCarrito);
                });
    }

    @Override
    public void agregarProducto(Long carritoId, Long productoId, Integer cantidad) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new IllegalArgumentException("Carrito no encontrado"));

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        if (producto.getStock() < cantidad) {
            throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getNombre());
        }

        // Buscar si el item ya existe en el carrito
        Optional<ItemCarrito> itemExistente = carrito.getItems().stream()
                .filter(item -> item.getProducto().getId().equals(productoId))
                .findFirst();

        if (itemExistente.isPresent()) {
            ItemCarrito item = itemExistente.get();
            int nuevaCantidad = item.getCantidad() + cantidad;
            if (producto.getStock() < nuevaCantidad) {
                throw new IllegalArgumentException("No hay suficiente stock para agregar más unidades");
            }
            item.setCantidad(nuevaCantidad);
            item.calcularSubtotal();
            itemCarritoRepository.save(item);
        } else {
            ItemCarrito nuevoItem = ItemCarrito.builder()
                    .carrito(carrito)
                    .producto(producto)
                    .cantidad(cantidad)
                    .build();
            nuevoItem.calcularSubtotal();
            // carrito.agregarItem ya hace la relación bidireccional, pero JPA necesita
            // persistir el hijo o usar Cascade
            // Como Carrito tiene Cascade.ALL, al guardar el carrito se guardaría el item, o
            // guardamos item directo.
            carrito.agregarItem(nuevoItem);
            itemCarritoRepository.save(nuevoItem); // Guardamos explícitamente para asegurar ID
        }
    }

    @Override
    public void actualizarCantidad(Long itemId, Integer nuevaCantidad) {
        ItemCarrito item = itemCarritoRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item no encontrado"));

        if (nuevaCantidad <= 0) {
            eliminarItem(itemId);
            return;
        }

        if (item.getProducto().getStock() < nuevaCantidad) {
            throw new IllegalArgumentException("Stock insuficiente");
        }

        item.setCantidad(nuevaCantidad);
        item.calcularSubtotal();
        itemCarritoRepository.save(item);
    }

    @Override
    public void eliminarItem(Long itemId) {
        ItemCarrito item = itemCarritoRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item no encontrado"));
        Carrito carrito = item.getCarrito();
        carrito.removerItem(item);
        itemCarritoRepository.delete(item);
    }

    @Override
    public void vaciarCarrito(Long carritoId) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new IllegalArgumentException("Carrito no encontrado"));

        // Opción 1: Borrar items uno a uno (más seguro para integridad si hay lógica
        // compleja)
        // carrito.getItems().clear(); // Solo limpia lista en memoria si no se gestiona
        // bien

        // Opción 2: Repository delete
        itemCarritoRepository.deleteAll(carrito.getItems());
        carrito.getItems().clear();
        carritoRepository.save(carrito);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotal(Long carritoId) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new IllegalArgumentException("Carrito no encontrado"));

        return carrito.getItems().stream()
                .map(ItemCarrito::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Orden confirmarCompra(Long carritoId) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new IllegalArgumentException("Carrito no encontrado"));

        if (carrito.getItems().isEmpty()) {
            throw new IllegalStateException("El carrito está vacío");
        }

        // Validar stocks finales antes de confirmar
        for (ItemCarrito item : carrito.getItems()) {
            if (item.getProducto().getStock() < item.getCantidad()) {
                throw new IllegalStateException("Stock insuficiente para: " + item.getProducto().getNombre());
            }
        }

        // Crear Orden
        Orden orden = new Orden();
        orden.setUsuario(carrito.getUsuario());
        orden.setTotal(calcularTotal(carritoId));
        // La fecha se pone automática por @CreatedDate al guardar

        // Guardar orden para tener ID
        orden = ordenRepository.save(orden);

        // Mover items y descontar stock
        for (ItemCarrito itemCarrito : carrito.getItems()) {
            Producto producto = itemCarrito.getProducto();

            // Descontar stock
            producto.setStock(producto.getStock() - itemCarrito.getCantidad());
            productoRepository.save(producto);

            // Crear ItemOrden
            ItemOrden itemOrden = ItemOrden.builder()
                    .orden(orden)
                    .producto(producto)
                    .cantidad(itemCarrito.getCantidad())
                    .precioUnitario(producto.getPrecio()) // Precio congelado al momento de compra
                    .build();

            // No hay método helper en Orden para agregar directo a la lista y setear en una
            // línea,
            // pero podemos hacerlo manual o crear el helper si existe.
            // itemOrden.setOrden(orden); // ya está en el builder
            // orden.getItems().add(itemOrden); // opcional si se guardan aparte

            // Repositorio de ItemOrden? No se pidió explícitamente en el primer prompt,
            // pero Orden tiene CascadeType.ALL.
            // Verificando entidad Orden... tiene lista items.
            orden.agregarItem(itemOrden);
        }

        // Guardar Orden completa (por Cascade persistirá items)
        ordenRepository.save(orden);

        // Actualizar estado Carrito
        carrito.setEstado(EstadoCarrito.CONFIRMADO);
        carritoRepository.save(carrito);

        // Crear nuevo carrito limpio para el usuario (opcional, o se hace en
        // obtenerCarritoActivo la próxima vez)

        return orden;
    }
}
