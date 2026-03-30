package com.notfound.product.application.port.out;

public interface ProcessedEventRepository {

    boolean existsById(String eventId);

    void save(String eventId);
}
