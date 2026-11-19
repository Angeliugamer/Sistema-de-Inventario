package Services;

import Interfaces.Identificable;

import java.nio.file.Path;
import java.util.List;

public class JsonListReader<T extends Identificable> {
    private final JsonListService<T> service;

    public JsonListReader(Path file, Class<T> elementType) {
        service = new JsonListService<>(file, elementType);
    }

    public List<T> readAll() {
        return service.readAll();
    }
}
