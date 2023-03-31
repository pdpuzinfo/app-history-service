package ai.ecma.apphistoryservice.component;

import ai.ecma.apphistoryservice.repository.HistoryRepository;
import ai.ecma.apphistoryservice.service.EntityListenerHistoryService;
import ai.ecma.apphistoryservice.service.HistoryAssistenceService;
import ai.ecma.apphistoryservice.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoaderHistory implements CommandLineRunner {
    private final HistoryRepository historyRepository;

    @Override
    public void run(String... args) {

        if (CommonUtils.historyDisabled()) {
            log.info("History service is not working. To start history service please set true to " + CommonUtils.enabledVarName + " in your yml config");
            return;
        }

        log.info("History service is working...");

        historyRepository.initIndexes();
        checkAssistanceBeansExist();
        checkEntityListener();
    }

    private void checkEntityListener() {
        EntityManagerFactory entityManagerFactory = BeanUtilHistory.getBean(EntityManagerFactory.class);

        // Get the JPA EntityManagerFactory and create a Metamodel object
        Metamodel metamodel = entityManagerFactory.getMetamodel();

        // Get all entity types and add their Java classes to a list
        List<Class<?>> managedClasses = new ArrayList<>();
        for (EntityType<?> entityType : metamodel.getEntities()) {
            managedClasses.add(entityType.getJavaType());
        }

        List<Class> notCloneableClasses = new ArrayList<>();
        for (Class<?> managedClass : managedClasses)
            if (entityNotCloneable(managedClass))
                notCloneableClasses.add(managedClass);

        if (notCloneableClasses.isEmpty())
            return;

        System.err.println("Listened by EntityListenerHistoryService Entity implementation Cloneable required");
        System.err.println("Not implemented classes : ");
        notCloneableClasses.forEach(aClass -> System.err.println(aClass.getSimpleName()));
        System.exit(1);
    }

    private boolean entityNotCloneable(Class<?> clazz) {
        EntityListeners entityListeners = clazz.getAnnotation(EntityListeners.class);
        if (Objects.isNull(entityListeners))
            return false;

        Entity entity = clazz.getAnnotation(Entity.class);
        if (Objects.isNull(entity))
            return false;

        Class[] value = entityListeners.value();

        for (Class oneClass : value) {
            if (Objects.equals(oneClass, EntityListenerHistoryService.class)) {
                if (!Cloneable.class.isAssignableFrom(clazz)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkAssistanceBeansExist() {
        try {
            HistoryAssistenceService bean = BeanUtilHistory.getBean(HistoryAssistenceService.class);
        } catch (Exception e) {
            System.err.println("implementation of history assistance bean service is requeired!!!");
            System.exit(1);
            throw e;
        }
    }
}
