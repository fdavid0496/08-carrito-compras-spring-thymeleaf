package co.edu.unicauca.carrito.controller;

import co.edu.unicauca.carrito.dto.ProductoDTO;
import co.edu.unicauca.carrito.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/productos")
public class AdminProductoController {

    private final ProductoService productoService;

    public AdminProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public String listarProductos(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<ProductoDTO> productosPage = productoService.listarTodos(PageRequest.of(page, 5));
        model.addAttribute("productos", productosPage);
        return "admin/productos/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("producto", new ProductoDTO());
        return "admin/productos/formulario";
    }

    @PostMapping
    public String guardarProducto(@Valid @ModelAttribute("producto") ProductoDTO productoDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (result.hasErrors()) {
            return "admin/productos/formulario";
        }
        productoService.guardar(productoDTO);
        redirectAttributes.addFlashAttribute("mensaje", "Producto creado exitosamente");
        redirectAttributes.addFlashAttribute("tipo", "success");
        return "redirect:/admin/productos";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        ProductoDTO productoDTO = productoService.buscarPorId(id);
        model.addAttribute("producto", productoDTO);
        return "admin/productos/formulario";
    }

    @PostMapping("/editar/{id}")
    public String actualizarProducto(@PathVariable Long id,
            @Valid @ModelAttribute("producto") ProductoDTO productoDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (result.hasErrors()) {
            return "admin/productos/formulario";
        }
        productoService.actualizar(id, productoDTO);
        redirectAttributes.addFlashAttribute("mensaje", "Producto actualizado exitosamente");
        redirectAttributes.addFlashAttribute("tipo", "info");
        return "redirect:/admin/productos";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productoService.eliminar(id);
        redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado (desactivado) exitosamente");
        redirectAttributes.addFlashAttribute("tipo", "warning");
        return "redirect:/admin/productos";
    }
}
