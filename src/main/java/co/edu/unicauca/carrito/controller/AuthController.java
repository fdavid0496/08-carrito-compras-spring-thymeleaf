package co.edu.unicauca.carrito.controller;

import co.edu.unicauca.carrito.dto.UsuarioDTO;
import co.edu.unicauca.carrito.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login() {
        // Si el usuario ya está autenticado, redirigir al catálogo
        if (estaAutenticado()) {
            return "redirect:/catalogo";
        }
        return "login";
    }

    @GetMapping("/registro")
    public String registroForm(Model model) {
        // Si el usuario ya está autenticado, no dejarlo registrarse de
        // nuevo
        if (estaAutenticado()) {
            return "redirect:/catalogo";
        }
        model.addAttribute("usuario", new UsuarioDTO());
        return "registro";
    }

    @PostMapping("/registro")
    public String registro(@Valid @ModelAttribute("usuario") UsuarioDTO usuarioDTO,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "registro";
        }

        try {
            usuarioService.registrar(usuarioDTO);
            return "redirect:/login?registrado";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "registro";
        }
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/catalogo";
    }

    // Método auxiliar para verificar si hay una sesión activa
    private boolean estaAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }
}