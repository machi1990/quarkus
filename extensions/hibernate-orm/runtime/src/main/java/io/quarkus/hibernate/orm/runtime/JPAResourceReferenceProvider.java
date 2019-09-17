package io.quarkus.hibernate.orm.runtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.arc.ResourceReferenceProvider;
import io.quarkus.hibernate.orm.runtime.entitymanager.ForwardingEntityManager;

public class JPAResourceReferenceProvider implements ResourceReferenceProvider {
    private static final Map<String, InstanceHandle<EntityManager>> entityManagers = new ConcurrentHashMap<>();
    private static final Map<String, InstanceHandle<Object>> entityManagerFactories = new ConcurrentHashMap<>();

    @Override
    public InstanceHandle<Object> get(Type type, Set<Annotation> annotations) {
        if (EntityManagerFactory.class.equals(type)) {
            JPAConfig jpaConfig = Arc.container().instance(JPAConfig.class).get();
            PersistenceUnit pu = getAnnotation(annotations, PersistenceUnit.class);
            if (pu != null) {
                return getEntityManagerFactory(jpaConfig, pu.unitName());
            }
        } else if (EntityManager.class.equals(type)) {
            PersistenceContext pc = getAnnotation(annotations, PersistenceContext.class);
            if (pc != null) {
                InstanceHandle<EntityManager> entityManager = getEntityManager(pc.unitName());
                return () -> entityManager.get();
            }
        }

        return null;
    }

    /**
     * Get an entity manager handle of a given persistence unit
     * 
     * @param unitName
     * @return - the entity manager {@link EntityManager}
     */
    public InstanceHandle<EntityManager> getEntityManager(String unitName) {
        JPAConfig jpaConfig = Arc.container().instance(JPAConfig.class).get();
        if (jpaConfig == null) {
            return null;
        }

        if (jpaConfig.isJtaEnabled()) {
            return getTransactionEntityManager(unitName);
        } else {
            return getEntityManager(jpaConfig, unitName);
        }
    }

    private InstanceHandle<Object> getEntityManagerFactory(JPAConfig jpaConfig, String unitName) {
        InstanceHandle<Object> instanceHandle = entityManagerFactories.get(unitName);
        if (instanceHandle != null) {
            return instanceHandle;
        }

        EntityManagerFactory entityManagerFactory = jpaConfig.getEntityManagerFactory(unitName);

        instanceHandle = new InstanceHandle<Object>() {
            @Override
            public Object get() {
                return entityManagerFactory;
            }
        };

        entityManagerFactories.put(unitName, instanceHandle);
        return instanceHandle;
    }

    private InstanceHandle<EntityManager> getTransactionEntityManager(String unitName) {
        TransactionEntityManagers transactionEntityManagers = Arc.container()
                .instance(TransactionEntityManagers.class).get();
        ForwardingEntityManager entityManager = new ForwardingEntityManager() {
            @Override
            protected EntityManager delegate() {
                return transactionEntityManagers.getEntityManager(unitName);
            }
        };

        return new InstanceHandle<EntityManager>() {
            @Override
            public EntityManager get() {
                return entityManager;
            }
        };
    }

    private InstanceHandle<EntityManager> getEntityManager(JPAConfig jpaConfig, String unitName) {
        InstanceHandle<EntityManager> entityManagerInstanceHandle = entityManagers.get(unitName);

        if (entityManagerInstanceHandle != null) {
            return entityManagerInstanceHandle;
        }

        InstanceHandle<Object> entityManagerFactoryInstanceHandle = getEntityManagerFactory(jpaConfig, unitName);

        EntityManager entityManager = EntityManagerFactory.class.cast(entityManagerFactoryInstanceHandle.get())
                .createEntityManager();

        InstanceHandle<EntityManager> instanceHandle = new InstanceHandle<EntityManager>() {
            @Override
            public EntityManager get() {
                return entityManager;
            }

            @Override
            public void destroy() {
                entityManager.close();
            }
        };

        entityManagers.put(unitName, instanceHandle);
        return instanceHandle;
    }

}
