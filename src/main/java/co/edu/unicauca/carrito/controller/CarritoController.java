package co.edu.unicauca.carrito.controller;

import co.edu.unicauca.carrito.model.Carrito;
import co.edu.unicauca.carrito.model.Orden;
import co.edu.unicauca.carrito.service.CarritoService;
import co.edu.unicauca.carrito.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/carrito")
public class CarritoController {

    private final CarritoService carritoService;
    private final UsuarioService usuarioService;

    public CarritoController(CarritoService carritoService, UsuarioService usuarioService) {
        this.carritoService = carritoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String verCarrito(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null)
            return "redirect:/login"; // Seguridad extra

        var usuario = usuarioService.buscarPorEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Carrito carrito = carritoService.obtenerCarritoActivo(usuario);
        model.addAttribute("carrito", carrito);
        model.addAttribute("total", carritoService.calcularTotal(carrito.getId()));

        return "carrito";
    }

    @PostMapping("/agregar")
    public String agregarProducto(@RequestParam Long productoId,
            @RequestParam Integer cantidad,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            var usuario = usuarioService.buscarPorEmail(userDetails.getUsername()).get();
            Carrito carrito = carritoService.obtenerCarritoActivo(usuario);
            carritoService.agregarProducto(carrito.getId(), productoId, cantidad);
            redirectAttributes.addFlashAttribute("mensaje", "Producto agregado al carrito");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensaje", e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/catalogo";
    }

    @PostMapping("/actualizar/{itemId}")
    public String actualizarCantidad(@PathVariable Long itemId,
            @RequestParam Integer cantidad,
            RedirectAttributes redirectAttributes) {
        try {
            carritoService.actualizarCantidad(itemId, cantidad);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensaje", e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/carrito";
    }

    @PostMapping("/eliminar/{itemId}")
    public String eliminarItem(@PathVariable Long itemId) {
        carritoService.eliminarItem(itemId);
        return "redirect:/carrito";
    }

    @PostMapping("/vaciar")
    public String vaciarCarrito(@RequestParam Long carritoId) {
        carritoService.vaciarCarrito(carritoId);
        return "redirect:/carrito";
    }

    @PostMapping("/confirmar")
    public String confirmarCompra(@RequestParam Long carritoId,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            Orden orden = carritoService.confirmarCompra(carritoId);
            redirectAttributes.addFlashAttribute("ordenId", orden.getId());
            return "redirect:/carrito/confirmacion";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("mensaje", e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
            return "redirect:/carrito";
        }
    }

    @GetMapping("/confirmacion")
    public String paginaConfirmacion(@ModelAttribute("ordenId") String ordenId, Model model) { // Usamos String por si
                                                                                               // llega nulo o de flash
        if (ordenId == null || ordenId.isEmpty()) {
            return "redirect:/catalogo";
        }
        model.addAttribute("ordenId", ordenId);
        return "confirmacion";
    }
}
