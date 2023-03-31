package ai.ecma.apphistoryservice.entity;

import ai.ecma.apphistoryservice.entity.template.AbsUUIDUserAuditEntity;
import ai.ecma.apphistoryservice.enums.ActionTypeEnum;
import ai.ecma.apphistoryservice.utils.CommonUtils;
import ai.ecma.apphistoryservice.utils.TableNameConst;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity(name = TableNameConst.HISTORY)
public class History extends AbsUUIDUserAuditEntity {

    //ID COLUMN ENTITYDAGI
    private String rowId;

    //ID NI CLASS NAME I
    private String rowIdClassName;

    //QAYSI TABLE UCHUN YOZILYAPTI
    private String entityName;

    //ENTITY NI CLASS NOMI
    private String entityClassName;

    //FIELD NOMI
    @Column(name = "changed_fields", columnDefinition = "varchar[]")
    @Type(type = "ai.ecma.apphistoryservice.type.GenericStringArrayType")
    private String[] changedFields;

    //AVVALGI QIYMATI
    @Column(columnDefinition = "text")
    private String before;

    //KEYINGI QIYMATI
    @Column(columnDefinition = "text")
    private String after;

    //QAYSI ACTION BAJARILYAPTI (PERSIST, UPDATE, REMOVE)
    @Enumerated(EnumType.STRING)
    private ActionTypeEnum actionType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        History history = (History) o;
        return getId() != null && Objects.equals(getId(), history.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public <T> T getRowId() {
        return CommonUtils.readObject(rowId, rowIdClassName);
    }

    public String getRowIdStr(){
        return rowId;
    }

    public void setRowId(Object rowId) {
        this.rowId = CommonUtils.toJson(rowId);
    }

    public <T> T getBefore() {
        return CommonUtils.readObject(before, entityClassName);
    }

    public String getBeforeStr(){
        return before;
    }

    public void setBefore(Object before) {
        this.before = CommonUtils.toJson(before);
    }

    public String getAfterStr(){
        return after;
    }

    public <T> T getAfter() {
        return CommonUtils.readObject(after, entityClassName);
    }

    public void setAfter(Object after) {
        this.after = CommonUtils.toJson(after);
    }

    public List<String> getChangedFields() {
        return Arrays.stream(changedFields).collect(Collectors.toList());
    }

    public void setChangedFields(Set<String> changedFields) {
        this.changedFields = changedFields.toArray(new String[0]);
    }

    public History(String rowId, String rowIdClassName, String entityName, String entityClassName, Set<String> changedFields, Object before, Object after, ActionTypeEnum actionType, Set<String> ignoredFields, UUID createdById) {
        this.rowId = rowId;
        this.rowIdClassName = rowIdClassName;
        this.entityName = entityName;
        this.entityClassName = entityClassName;
        setChangedFields(changedFields);
        this.before = CommonUtils.toJsonWithoutIgnoreFields(before, ignoredFields);
        this.after = CommonUtils.toJsonWithoutIgnoreFields(after, ignoredFields);
        this.actionType = actionType;
        this.setCreatedById(createdById);
    }
}
