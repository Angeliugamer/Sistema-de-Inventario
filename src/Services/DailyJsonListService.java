package Services;

import Interfaces.Identificable;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class DailyJsonListService<T extends Identificable> {
    private final Path directory;
    private final Type listType;
    private final Function<T, LocalDate> dateExtractor;

    public DailyJsonListService(
            Path directory, Class<T> elementType,
            Function<T, LocalDate> dateExtractor) {

        if (directory == null || elementType == null || dateExtractor == null) {
            throw new IllegalArgumentException(
                    "Los parámetros son obligatorios."
            );
        }

        this.directory = directory;
        this.dateExtractor = dateExtractor;

        this.listType = TypeToken
                .getParameterized(ArrayList.class, elementType)
                .getType();
    }

    // OBTENER LA RUTA DE UN DÍA
    private Path getFile(LocalDate date) {
        return directory
                .resolve(String.valueOf(date.getYear()))
                .resolve(String.format("%02d", date.getMonthValue()))
                .resolve(String.format("%02d.json", date.getDayOfMonth()));
    }

    // REGISTRAR UN ELEMENTO
    public void add(T item) {
        if (item == null || item.getId() == null || item.getId().isBlank()) {
            throw new IllegalArgumentException(
                    "El registro no tiene un ID válido."
            );
        }

        LocalDate date = dateExtractor.apply(item);

        if (date == null) {
            throw new IllegalArgumentException(
                    "El registro no tiene una fecha válida."
            );
        }

        synchronized (JsonStorage.class) {
            Path file = getFile(date);
            List<T> data = JsonStorage.read(file, listType);

            boolean exists = data.stream()
                    .anyMatch(existing ->
                            existing.getId().equals(item.getId())
                    );

            if (exists) {
                throw new IllegalArgumentException(
                        "Ya existe un registro con ese ID."
                );
            }

            data.add(item);
            JsonStorage.write(file, data);
        }
    }

    // REGISTRAR VARIOS ELEMENTOS EN UNA SOLA ESCRITURA.
    public void addAll(List<T> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("No hay registros para guardar.");
        }

        synchronized (JsonStorage.class) {
            LocalDate date = dateExtractor.apply(items.get(0));

            if (date == null) {
                throw new IllegalArgumentException("La fecha del registro no es válida.");
            }

            for (T item : items) {
                if (item == null || item.getId() == null || item.getId().isBlank() || !date.equals(dateExtractor.apply(item))) {
                    throw new IllegalArgumentException("Los registros deben tener IDs y fechas válidas.");
                }
            }

            Path file = getFile(date);
            List<T> data = new ArrayList<>(JsonStorage.read(file, listType));

            for (T item : items) {
                boolean exists = data.stream().anyMatch(existing ->
                        existing.getId().equals(item.getId())
                );

                if (exists) {
                    throw new IllegalArgumentException("Ya existe un registro con ese ID.");
                }

                data.add(item);
            }

            JsonStorage.write(file, data);
        }
    }

    // LEER UN DÍA CONCRETO
    public List<T> readByDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException(
                    "La fecha no puede ser nula."
            );
        }

        synchronized (JsonStorage.class) {
            return List.copyOf(
                    JsonStorage.read(getFile(date), listType)
            );
        }
    }

    // LEER UN INTERVALO DE FECHAS
    public List<T> readBetween(
            LocalDate start, LocalDate end) {
        if (start == null || end == null || start.isAfter(end)) {
            throw new IllegalArgumentException(
                    "El intervalo de fechas no es válido."
            );
        }

        List<T> result = new ArrayList<>();

        LocalDate current = start;

        while (!current.isAfter(end)) {
            result.addAll(readByDate(current));
            current = current.plusDays(1);
        }

        return List.copyOf(result);
    }

    // LEER TODOS LOS ARCHIVOS HISTÓRICOS
    public List<T> readAll() {
        List<T> result = new ArrayList<>();

        if (Files.notExists(directory)) {
            return result;
        }

        synchronized (JsonStorage.class) {
            try (Stream<Path> files = Files.walk(directory)) {
                files.filter(Files::isRegularFile)
                        .filter(path ->
                                path.getFileName().toString()
                                        .endsWith(".json")
                        )
                        .sorted()
                        .forEach(path ->
                                result.addAll(
                                        JsonStorage.read(path, listType)
                                )
                        );

            } catch (IOException e) {
                throw new RuntimeException(
                        "Error leyendo los registros diarios.", e
                );
            }
        }

        return List.copyOf(result);
    }
}
