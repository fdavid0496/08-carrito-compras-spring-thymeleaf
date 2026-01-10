package co.edu.unicauca.carrito.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "items_carrito")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrito_id", nullable = false)
    private Carrito carrito;

    @ManyToOne(fetch = FetchType.EAGER) // Eager is fine for product usually
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    // Helper method to calculate subtotal
    public void calcularSubtotal() {
        if (this.producto != null && this.producto.getPrecio() != null && this.cantidad != null) {
            this.subtotal = this.producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
        }
    }
}
