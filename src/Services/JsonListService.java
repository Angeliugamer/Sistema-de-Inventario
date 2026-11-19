package Services;

import Interfaces.Identificable;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JsonListService<T extends Identificable> {
    private final Path file;
    private final Type listType;

    public JsonListService(Path file, Class<T> elementType) {
        if (file == null || elementType == null) {
            throw new IllegalArgumentException(
                    "El archivo y el tipo de dato son obligatorios."
            );
        }

        this.file = file;

        this.listType = TypeToken
                .getParameterized(ArrayList.class, elementType)
                .getType();
    }

    // LEER TODOS LOS REGISTROS
    public synchronized List<T> readAll() {
        return List.copyOf(
                JsonStorage.read(file, listType)
        );
    }

    // AGREGAR UN REGISTRO
    public synchronized void add(T item) {
        if (item == null || item.getId() == null || item.getId().isBlank()) {
            throw new IllegalArgumentException(
                    "El registro o su identificador no son válidos."
            );
        }

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

    // ELIMINAR UN REGISTRO POR SU ID
    public synchronized boolean deleteById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "El ID no puede estar vacío."
            );
        }

        List<T> data = JsonStorage.read(file, listType);

        boolean removed = data.removeIf(
                item -> id.equals(item.getId())
        );

        if (removed) {
            JsonStorage.write(file, data);
        }

        return removed;
    }
}
