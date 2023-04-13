package ai.ecma.apphistoryservice.service;

import ai.ecma.apphistoryservice.component.BeanUtilHistory;
import ai.ecma.apphistoryservice.entity.History;
import ai.ecma.apphistoryservice.enums.ActionTypeEnum;
import ai.ecma.apphistoryservice.repository.HistoryRepository;
import ai.ecma.apphistoryservice.utils.AppConstant;
import ai.ecma.apphistoryservice.utils.CommonUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PreRemove;
import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EntityListenerHistoryService {
    //LOAD QILINGAN ENTITY LARNI SAQLAYDI
    ThreadLocal<Map<String, Map<String, Object>>> entitiesThreadLocal = new ThreadLocal<>();

    //BARCHA HISTORY LARNI SHU YERDA SAQLAB KEYIN OXIRIDA SAVE QILIB YUBORILADI
    ThreadLocal<List<History>> historiesThreadLocal = new ThreadLocal<>();


    @PostPersist
    public void postPersist(Object afterObj) {
        try {

            if (CommonUtils.historyDisabled())
                return;

            ActionTypeEnum actionType = ActionTypeEnum.PERSIST;

            String entityName = CommonUtils.getEntityName(afterObj.getClass());

            String rowId = CommonUtils.getRowId(afterObj);

            List<Field> fields = CommonUtils.getFields(afterObj.getClass());

            Field idField = CommonUtils.getIdField(afterObj.getClass());

            Set<String> ignoredFields = CommonUtils.getIgnoredFields(fields, afterObj.getClass());

            HistoryAssistenceService historyAssistenceService = BeanUtilHistory.getBean(HistoryAssistenceService.class);

            History history = new History(
                    rowId,
                    CommonUtils.getClassName(idField),
                    entityName,
                    afterObj.getClass().getName(),
                    new HashSet<>(),
                    null,
                    afterObj,
                    actionType,
                    ignoredFields,
                    historyAssistenceService.currentUserId(),
                    CommonUtils.getApi()
            );

            //TRANSACTION TUGAGANDAN KEYIN SAVE QILINADI
            save(history);

            postLoad(afterObj);

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorToBotQueu(e, afterObj);
        }

    }

    @PostLoad
    public void postLoad(Object object) {

        if (CommonUtils.historyDisabled())
            return;

        Object cloneEntity = CommonUtils.cloneEntity(object);

        Map<String, Map<String, Object>> loadedEntityMap = entitiesThreadLocal.get();
        if (Objects.isNull(loadedEntityMap))
            loadedEntityMap = new HashMap<>();

        String entityName = CommonUtils.getEntityName(cloneEntity.getClass());
        String rowId = CommonUtils.getRowId(cloneEntity);

        Map<String, Object> entityMap = loadedEntityMap.getOrDefault(entityName, new HashMap<>());
        entityMap.put(rowId, entityMap.getOrDefault(rowId, cloneEntity));

        loadedEntityMap.put(entityName, entityMap);
        entitiesThreadLocal.set(loadedEntityMap);
    }


    @PostUpdate
    public void postUpdate(Object after) {

        try {
            if (CommonUtils.historyDisabled())
                return;

            ActionTypeEnum actionType = ActionTypeEnum.UPDATE;

            Object before = getBefore(after);

            //BEFORE TOPILMADI
            if (Objects.isNull(before)) {
                System.err.println("post update da Before topilmadi!!!!!!");
                return;
            }

            compareTwoObject(before, after, actionType);

            postLoad(after);

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorToBotQueu(e, after);
        }

    }


    @PreRemove
    public void preRemove(Object beforeObj) {

        try {

            if (CommonUtils.historyDisabled())
                return;

            ActionTypeEnum actionType = ActionTypeEnum.REMOVE;

            String entityName = CommonUtils.getEntityName(beforeObj.getClass());

            String rowId = CommonUtils.getRowId(beforeObj);

            List<Field> fields = CommonUtils.getFields(beforeObj.getClass());

            Field idField = CommonUtils.getIdField(beforeObj.getClass());

            Timestamp eventTime = new Timestamp(System.currentTimeMillis());

            Set<String> ignoredFields = CommonUtils.getIgnoredFields(fields, beforeObj.getClass());

            HistoryAssistenceService historyAssistenceService = BeanUtilHistory.getBean(HistoryAssistenceService.class);

            History history = new History(
                    rowId,
                    CommonUtils.getClassName(idField),
                    entityName,
                    beforeObj.getClass().getName(),
                    new HashSet<>(),
                    beforeObj,
                    null,
                    actionType,
                    ignoredFields,
                    historyAssistenceService.currentUserId(),
                    CommonUtils.getApi()
            );

            save(history);

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorToBotQueu(e, beforeObj);
        }

    }

    private void save(History history) {

        //LOCAL DAN BARCHA HISTORY LARNI OLAMIZ
        List<History> histories = historiesThreadLocal.get();

        //HISTORY BORMI
        boolean historiesIsNotEmpty = Objects.nonNull(histories);

        //AGAR HISTORY NULL BO'LSA
        if (!historiesIsNotEmpty)
            histories = new LinkedList<>();

        //YANGI HISTORY NI LISTGA QO'SHAMIZ
        histories.add(history);

        //LOCAL GA SET QILAMIZ
        historiesThreadLocal.set(histories);

        //AGAR HISTORY BO'SH BO'LMASA DEMAK AVVAL TASK YARATILGAN
        if (historiesIsNotEmpty)
            return;

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                List<History> finalHistories = historiesThreadLocal.get();
                Runnable task = () -> saveHistories(finalHistories);
                ExecutorService executorService = Executors.newCachedThreadPool();
                executorService.execute(task);
            }
        });
    }

    private void saveHistories(List<History> histories){
        if (Objects.isNull(histories)) {
            System.err.println("Histories is null.................");
            return;
        }
        HistoryRepository historyRepository = BeanUtilHistory.getBean(HistoryRepository.class);
        historyRepository.saveAll(histories);
    }


    private void sendErrorToBotQueu(Exception e, Object object) {
        HistoryAssistenceService historyAssistenceService = BeanUtilHistory.getBean(HistoryAssistenceService.class);
        historyAssistenceService.sendErrorToBot(e);
    }

    private Object getBefore(Object object) {

        Map<String, Map<String, Object>> loadedEntityMap = entitiesThreadLocal.get();
        if (Objects.isNull(loadedEntityMap))
            return null;

        String entityName = CommonUtils.getEntityName(object.getClass());
        Map<String, Object> entityMap = loadedEntityMap.get(entityName);
        if (Objects.isNull(entityMap))
            return null;

        String rowId = CommonUtils.getRowId(object);
        return entityMap.get(rowId);
    }

    private void compareTwoObject(Object beforeObj, Object afterObj, ActionTypeEnum actionType) throws IllegalAccessException {

        try {

            //O'ZGARISHI MUMKIN BO'LGAN FIELD LAR (KERAKSIZLARI IGNORE QILINGAN)
            List<Field> fields = CommonUtils.getFields(beforeObj.getClass());

            //ENTITY NOMI
            String entityName = CommonUtils.getEntityName(beforeObj.getClass());

            //TABLE ID SI
            String rowId = CommonUtils.getRowId(beforeObj);

            //ID FIELD
            Field idField = CommonUtils.getIdField(beforeObj.getClass());

            //QAYIS FIELD LARI O'ZGARYOTGANINI YIG'ISH UCHUN
            Set<String> changedFields = new HashSet<>();

            //ODDIY FIELD LARNI TEKSHIRADI
            compareSimpleFields(beforeObj, afterObj, fields, changedFields);

            //RELATIONAL YA'NI ONE TO ONE YOKI MANY TO ONE BOG'LANGAN OBJECT ID LARI TENG MI YO'QMI ANIQLAYDI
            compareRelationalObjectIds(beforeObj, afterObj, changedFields);

            //IGNORE QILINADIGAN FIELLARNI RO'YXATI
            Set<String> ignoredFields = CommonUtils.getIgnoredFields(fields, afterObj.getClass());

            //AGAR HECH QAYSI FIELD O'ZGARMAGAN BO'LSA HISTORY YOZMAYDI
            if (changedFields.isEmpty())
                return;

            HistoryAssistenceService historyAssistenceService = BeanUtilHistory.getBean(HistoryAssistenceService.class);

            History history = new History(
                    rowId,
                    CommonUtils.getClassName(idField),
                    entityName,
                    afterObj.getClass().getName(),
                    changedFields,
                    beforeObj,
                    afterObj,
                    actionType,
                    ignoredFields,
                    historyAssistenceService.currentUserId(),
                    CommonUtils.getApi()
            );

            save(history);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void compareSimpleFields(Object beforeObj, Object afterObj, List<Field> fields, Set<String> changedFields) throws IllegalAccessException {
        //ODDIY FIELD LARNI SOLISHTIRISH
        for (Field field : fields) {

            field.setAccessible(true);

            Object before = field.get(beforeObj);
            Object after = field.get(afterObj);

            Class<?> fieldType = field.getType();

            //SQL DATE UCHUN BOSHQA LOGIKA
            if (fieldType.equals(Date.class)) {

                if (sqlDateEquals(before, after))
                    continue;

                //util date EQUALS METHODI
            } else if (fieldType.equals(java.util.Date.class)) {

                if (utilDateEquals(before, after))
                    continue;

                //ARRAY EQUALS METHODI
            } else if (fieldType.isArray()) {

                if (compareArray(field, before, after))
                    continue;

                //ODDIY EQUALS
            } else {
                if (Objects.equals(before, after))
                    continue;
            }

            changedFields.add(field.getName());
        }
    }


    private boolean sqlDateEquals(Object before, Object after) {
        LocalDate beforeLocal = Objects.isNull(before) ? null : ((Date) before).toLocalDate();
        LocalDate afterLocal = Objects.isNull(after) ? null : ((Date) after).toLocalDate();
        return Objects.equals(beforeLocal, afterLocal);
    }

    private boolean utilDateEquals(Object before, Object after) {
        LocalDate beforeLocal = Objects.isNull(before) ? null : ((java.util.Date) before).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate afterLocal = Objects.isNull(after) ? null : ((java.util.Date) after).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Objects.equals(beforeLocal, afterLocal);
    }

    private boolean compareArray(Field field, Object before, Object after) {

        Class<?> componentType = field.getType().getComponentType();
        //PRIMITIVE TYPE UCHUN
        if (componentType.isPrimitive()) {
            String primitiveClassName = componentType.getName();
            switch (primitiveClassName) {
                case "byte":
                    return CommonUtils.arraysEquals((byte[]) before, (byte[]) after);
                case "short":
                    return CommonUtils.arraysEquals((short[]) before, (short[]) after);
                case "int":
                    return CommonUtils.arraysEquals((int[]) before, (int[]) after);
                case "long":
                    return CommonUtils.arraysEquals((long[]) before, (long[]) after);
                case "float":
                    return CommonUtils.arraysEquals((float[]) before, (float[]) after);
                case "double":
                    return CommonUtils.arraysEquals((double[]) before, (double[]) after);
                case "char":
                    return CommonUtils.arraysEquals((char[]) before, (char[]) after);
                case "boolean":
                    return CommonUtils.arraysEquals((boolean[]) before, (boolean[]) after);
                default:
                    throw new RuntimeException("primitive type not found - > " + primitiveClassName);
            }
        } else {
            return CommonUtils.arraysEquals((Object[]) before, (Object[]) after);
        }
    }


    private void compareRelationalObjectIds(Object beforeObj, Object afterObj, Set<String> changedFields) throws IllegalAccessException, NoSuchFieldException {
        //ONE TO ONE VA MANY TO ONE FIELD LARNI SOLISHTIRISH
        List<Field> relationFields = CommonUtils.getRelationFields(beforeObj.getClass());

        for (Field relationField : relationFields) {

            relationField.setAccessible(true);

            Object beforeRelationalObj = relationField.get(beforeObj);
            Object afterRelationalObj = relationField.get(afterObj);

            Object beforeRelationalObjId = CommonUtils.getRelationalId(beforeRelationalObj);
            Object afterRelationalObjId = CommonUtils.getRelationalId(afterRelationalObj);

            if (Objects.equals(beforeRelationalObjId, afterRelationalObjId))
                continue;

            changedFields.add(relationField.getName() + AppConstant.PREFIX_ID);

        }
    }


//    public <T> JpaRepository<T, Object> getRepository(Class<T> entityClass) {
//        ApplicationContext context = BeanUtilHistory.getContext();
//        Map<String, JpaRepository> beansOfType = context.getBeansOfType(JpaRepository.class);
//        for (JpaRepository repository : beansOfType.values()) {
//            Class<?>[] genericTypes = GenericTypeResolver.resolveTypeArguments(repository.getClass(), JpaRepository.class);
//            if (genericTypes != null && genericTypes.length == 2 && genericTypes[0].equals(entityClass))
//                return repository;
//        }
//        throw new IllegalArgumentException("No repository found for entity class " + entityClass);
//    }


}
