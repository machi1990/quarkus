package io.quarkus.hibernate.orm.panache.runtime;

import java.util.Map;

import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class PanacheRecorder {
    public void addEntityPersistenceUnits(Map<String, String> units) {
        PersistenceUnitHolder.setPersistenceUnits(units);
    }
}
