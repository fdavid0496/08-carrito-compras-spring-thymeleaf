package co.edu.unicauca.carrito.controller;

import co.edu.unicauca.carrito.dto.ProductoDTO;
import co.edu.unicauca.carrito.service.ProductoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/catalogo")
public class CatalogoController {

    private final ProductoService productoService;

    public CatalogoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public String listarCatalogo(@RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String busqueda,
            Model model) {
        Page<ProductoDTO> productos;
        int pageSize = 8; // 8 productos por página para el grid

        if (busqueda != null && !busqueda.isEmpty()) {
            productos = productoService.buscarPorNombre(busqueda, PageRequest.of(page, pageSize));
            model.addAttribute("busqueda", busqueda);
        } else {
            // Nota: Idealmente deberíamos filtrar solo los productos activos (stock > 0 y
            // activo = true)
            // Para este MVP usamos listarTodos que ya filtra por activo=true en la
            // búsqueda,
            // pero listarTodos trae todo. Vamos a asumir que el usuario quiere ver todo por
            // ahora,
            // o mejor, usaremos buscarPorNombre con cadena vacía si queremos filtrar
            // activos,
            // pero ProductoService.listarTodos trae paginado.
            // Ajuste: Usaremos listarTodos del servicio que invoca findAll.
            // Mejora futura: agregar método listarActivos en Service.
            productos = productoService.listarTodos(PageRequest.of(page, pageSize));
        }

        model.addAttribute("productos", productos);
        return "catalogo";
    }
}
