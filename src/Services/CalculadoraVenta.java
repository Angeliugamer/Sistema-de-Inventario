package Services;

import Interfaces.EstrategiaPrecio;
import Services.Strategies.PrecioNormalStrategy;

public class CalculadoraVenta {
    private EstrategiaPrecio estrategia;

    public CalculadoraVenta() {
        this.estrategia = new PrecioNormalStrategy();
    }

    public CalculadoraVenta(EstrategiaPrecio estrategia) {
        setEstrategia(estrategia);
    }

    public void setEstrategia(EstrategiaPrecio estrategia) {
        if (estrategia == null) {
            throw new IllegalArgumentException("La estrategia de precio no puede ser nula.");
        }
        this.estrategia = estrategia;
    }

    public EstrategiaPrecio getEstrategia() {
        return estrategia;
    }

    public double calcular(double precioUnitario, int cantidad) {
        if (precioUnitario < 0 || cantidad <= 0) {
            throw new IllegalArgumentException("Precio o cantidad inválidos para calcular la venta.");
        }
        double subtotal = precioUnitario * cantidad;
        return estrategia.calcularTotal(subtotal, cantidad);
    }
}