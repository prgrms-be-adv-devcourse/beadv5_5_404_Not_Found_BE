package com.notfound.settlement.application.port.out;

public interface ProcessedEventRepository {

    boolean existsById(String eventId);

    void save(String eventId);
}
