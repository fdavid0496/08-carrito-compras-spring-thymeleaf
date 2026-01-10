package co.edu.unicauca.carrito.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carritos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCarrito estado;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemCarrito> items = new ArrayList<>();

    public void agregarItem(ItemCarrito item) {
        items.add(item);
        item.setCarrito(this);
    }

    public void removerItem(ItemCarrito item) {
        items.remove(item);
        item.setCarrito(null);
    }
}
