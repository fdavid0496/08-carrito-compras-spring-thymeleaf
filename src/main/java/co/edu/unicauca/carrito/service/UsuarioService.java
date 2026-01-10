package co.edu.unicauca.carrito.service;

import co.edu.unicauca.carrito.dto.UsuarioDTO;
import co.edu.unicauca.carrito.model.Rol;
import co.edu.unicauca.carrito.model.Usuario;
import co.edu.unicauca.carrito.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrar(UsuarioDTO usuarioDTO) {
        if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        Usuario usuario = Usuario.builder()
                .nombre(usuarioDTO.getNombre())
                .email(usuarioDTO.getEmail())
                .password(passwordEncoder.encode(usuarioDTO.getPassword()))
                .rol(Rol.USER) // Default role
                .build();

        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
}
