package ai.ecma.apphistoryservice.entity.template;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

/**
 * OBJECTNI OCHGAN YOKI UNI
 * O'ZGARTIRGAN USERNI OLIB BERISH UCHUN XIZMAT QILADI
 */
@Getter
@Setter
@ToString
@MappedSuperclass
public abstract class AbsUserAudit extends AbsDateAudit {

    @Column(name = "created_by_id", updatable = false)
    private UUID createdById;

}
