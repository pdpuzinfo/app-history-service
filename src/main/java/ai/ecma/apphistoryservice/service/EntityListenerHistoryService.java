package ai.ecma.apphistoryservice.service;

import ai.ecma.apphistoryservice.component.BeanUtilHistory;
import ai.ecma.apphistoryservice.entity.History;
import ai.ecma.apphistoryservice.enums.ActionTypeEnum;
import ai.ecma.apphistoryservice.repository.HistoryRepository;
import ai.ecma.apphistoryservice.utils.AppConstant;
import ai.ecma.apphistoryservice.utils.CommonUtils;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.PostPersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class EntityListenerHistoryService {


    @PostPersist
    public void postPersist(Object afterObj) {
        try {
            ActionTypeEnum actionType = ActionTypeEnum.PERSIST;

            String entityName = CommonUtils.getEntityName(afterObj.getClass());

            String rowId = CommonUtils.getRowId(afterObj);

            List<Field> fields = CommonUtils.getFields(afterObj.getClass());

            Field idField = afterObj.getClass().getDeclaredField(AppConstant.ID);

            Set<String> ignoredFields = CommonUtils.getIgnoredFields(fields, afterObj.getClass());

            HistoryRepository historyRepository = BeanUtilHistory.getBean(HistoryRepository.class);
            HistoryAssistenceService historyAssistenceService = BeanUtilHistory.getBean(HistoryAssistenceService.class);

            History history = new History(
                    rowId,
                    CommonUtils.getClassName(idField),
                    entityName,
                    afterObj.getClass().getName(),
                    new ArrayList<>(),
                    null,
                    afterObj,
                    actionType,
                    ignoredFields,
                    historyAssistenceService.currentUserId()
            );

            historyRepository.save(history);

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorToBotQueu(e, afterObj);
        }

    }

    @PreUpdate
    public void preUpdate(Object after) {

        try {

            ActionTypeEnum actionType = ActionTypeEnum.UPDATE;

            ExecutorService executorService = Executors.newCachedThreadPool();

            Callable<Object> getBeforeObj = () -> getBefore(after);

            Object before = executorService.submit(getBeforeObj).get();

            compareTwoObject(before, after, actionType);

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorToBotQueu(e, after);
        }

    }

    @PreRemove
    public void preRemove(Object beforeObj) {

        try {

            ActionTypeEnum actionType = ActionTypeEnum.REMOVE;

            String entityName = CommonUtils.getEntityName(beforeObj.getClass());

            String rowId = CommonUtils.getRowId(beforeObj);

            List<Field> fields = CommonUtils.getFields(beforeObj.getClass());

            Field idField = beforeObj.getClass().getDeclaredField(AppConstant.ID);

            Timestamp eventTime = new Timestamp(System.currentTimeMillis());

            Set<String> ignoredFields = CommonUtils.getIgnoredFields(fields, beforeObj.getClass());

            HistoryAssistenceService historyAssistenceService = BeanUtilHistory.getBean(HistoryAssistenceService.class);

            History history = new History(
                    rowId,
                    CommonUtils.getClassName(idField),
                    entityName,
                    beforeObj.getClass().getName(),
                    new ArrayList<>(),
                    beforeObj,
                    null,
                    actionType,
                    ignoredFields,
                    historyAssistenceService.currentUserId()
            );

            HistoryRepository historyRepository = BeanUtilHistory.getBean(HistoryRepository.class);
            historyRepository.save(history);

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorToBotQueu(e, beforeObj);
        }

    }

    private void sendErrorToBotQueu(Exception e, Object object) {
        HistoryAssistenceService historyAssistenceService = BeanUtilHistory.getBean(HistoryAssistenceService.class);
        historyAssistenceService.sendErrorToBot(e);
    }

    private Object getBefore(Object object) {
        try {
            JpaRepository<?, Object> jpaRepository = getRepository(object.getClass());

            Field idField = object.getClass().getDeclaredField(AppConstant.ID);
            idField.setAccessible(true);
            Object id = idField.get(object);

            return jpaRepository.findById(id).orElseThrow();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
            Field idField = beforeObj.getClass().getDeclaredField(AppConstant.ID);

            //QAYIS FIELD LARI O'ZGARYOTGANINI YIG'ISH UCHUN
            List<String> changedFields = new ArrayList<>();

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
                    historyAssistenceService.currentUserId()
            );

            HistoryRepository historyRepository = BeanUtilHistory.getBean(HistoryRepository.class);
            historyRepository.save(history);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private void compareSimpleFields(Object beforeObj, Object afterObj, List<Field> fields, List<String> changedFields) throws IllegalAccessException {
        //ODDIY FIELD LARNI SOLISHTIRISH
        for (Field field : fields) {

            field.setAccessible(true);

            Object before = field.get(beforeObj);
            Object after = field.get(afterObj);

            //SQL DATE UCHUN BOSHQA LOGIKA
            if (field.getType().equals(Date.class)) {
                LocalDate beforeLocal = Objects.isNull(before) ? null : ((Date) before).toLocalDate();
                LocalDate afterLocal = Objects.isNull(after) ? null : ((Date) after).toLocalDate();
                if (Objects.equals(beforeLocal, afterLocal))
                    continue;
            }

            //TENGMI
            if (Objects.equals(before, after))
                continue;

            changedFields.add(field.getName());
        }
    }

    private void compareRelationalObjectIds(Object beforeObj, Object afterObj, List<String> changedFields) throws IllegalAccessException, NoSuchFieldException {
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


    public <T> JpaRepository<T, Object> getRepository(Class<T> entityClass) {
        ApplicationContext context = BeanUtilHistory.getContext();
        Map<String, JpaRepository> beansOfType = context.getBeansOfType(JpaRepository.class);
        for (JpaRepository repository : beansOfType.values()) {
            Class<?>[] genericTypes = GenericTypeResolver.resolveTypeArguments(repository.getClass(), JpaRepository.class);
            if (genericTypes != null && genericTypes.length == 2 && genericTypes[0].equals(entityClass))
                return repository;
        }
        throw new IllegalArgumentException("No repository found for entity class " + entityClass);
    }


}
