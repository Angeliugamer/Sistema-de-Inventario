package Services.Strategies;

import Interfaces.EstrategiaPrecio;

public class DescuentoVolumenStrategy implements EstrategiaPrecio {
    private final int cantidadMinima;
    private final double porcentajeDescuento; // Ejemplo: 0.10 = 10%

    public DescuentoVolumenStrategy(int cantidadMinima, double porcentajeDescuento) {
        if (cantidadMinima <= 0) {
            throw new IllegalArgumentException("La cantidad mínima debe ser mayor a 0.");
        }
        if (porcentajeDescuento < 0 || porcentajeDescuento >= 1) {
            throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 1.");
        }
        this.cantidadMinima = cantidadMinima;
        this.porcentajeDescuento = porcentajeDescuento;
    }

    @Override
    public double calcularTotal(double subtotal, int cantidad) {
        if (cantidad >= cantidadMinima) {
            return subtotal * (1.0 - porcentajeDescuento);
        }
        return subtotal;
    }
}