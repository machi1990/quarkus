package io.quarkus.hibernate.orm.panache.deployment;

import io.quarkus.builder.item.MultiBuildItem;

final public class PersistenceUnitBuildItem extends MultiBuildItem {
    private String entityClass;
    private String persistenceUnitName;

    public PersistenceUnitBuildItem(String entityClass, String persistenceUnitName) {
        this.entityClass = entityClass;
        this.persistenceUnitName = persistenceUnitName;
    }

    public String getEntityClass() {
        return entityClass;
    }

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }
}
