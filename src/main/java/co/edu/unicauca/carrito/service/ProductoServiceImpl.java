package co.edu.unicauca.carrito.service;

import co.edu.unicauca.carrito.dto.ProductoDTO;
import co.edu.unicauca.carrito.model.Producto;
import co.edu.unicauca.carrito.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> listarTodos(Pageable pageable) {
        // En admin mostramos todos, activos e inactivos
        return productoRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> buscarPorNombre(String nombre, Pageable pageable) {
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDTO buscarPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
        return mapToDTO(producto);
    }

    @Override
    public ProductoDTO guardar(ProductoDTO dto) {
        Producto producto = Producto.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .precio(dto.getPrecio())
                .imagenUrl(dto.getImagenUrl())
                .stock(dto.getStock())
                .activo(true) // Por defecto activo al crear
                .build();

        return mapToDTO(productoRepository.save(producto));
    }

    @Override
    public ProductoDTO actualizar(Long id, ProductoDTO dto) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));

        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setImagenUrl(dto.getImagenUrl());
        producto.setStock(dto.getStock());
        // El estado activo se maneja por separado o se mantiene

        return mapToDTO(productoRepository.save(producto));
    }

    @Override
    public void eliminar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
        producto.setActivo(false); // Soft Delete
        productoRepository.save(producto);
    }

    // Mapper simple manual (Podría usar MapStruct, pero para MVP esto es
    // suficiente)
    private ProductoDTO mapToDTO(Producto entity) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(entity.getId());
        dto.setNombre(entity.getNombre());
        dto.setDescripcion(entity.getDescripcion());
        dto.setPrecio(entity.getPrecio());
        dto.setImagenUrl(entity.getImagenUrl());
        dto.setStock(entity.getStock());
        dto.setActivo(entity.getActivo());
        return dto;
    }
}
