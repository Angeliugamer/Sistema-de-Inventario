package Interfaces;

@FunctionalInterface
public interface EstrategiaPrecio {
    double calcularTotal(double subtotal, int cantidad);
}