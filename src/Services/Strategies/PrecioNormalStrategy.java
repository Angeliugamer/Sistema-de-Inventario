package Services.Strategies;

import Interfaces.EstrategiaPrecio;

public class PrecioNormalStrategy implements EstrategiaPrecio {
    @Override
    public double calcularTotal(double subtotal, int cantidad) {
        return subtotal;
    }
}