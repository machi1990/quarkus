package io.quarkus.hibernate.orm.panache.runtime;

import java.util.HashMap;
import java.util.Map;

public class PersistenceUnitHolder {
    private static Map<String, String> persistenceUnits;

    static Map<String, String> getPersistenceUnits() {
        return persistenceUnits;
    }

    static void setPersistenceUnits(Map<String, String> persistenceUnits) {
        PersistenceUnitHolder.persistenceUnits = new HashMap<>(persistenceUnits);
    }
}
