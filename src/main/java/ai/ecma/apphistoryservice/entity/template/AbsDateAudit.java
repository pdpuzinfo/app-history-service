package ai.ecma.apphistoryservice.entity.template;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * OBJECTLAR DB GA QO'SHILGANDA YOKI O'ZGARTIRILGANDA
 * AVTOMAT RAVISHDA O'SHA VAQTNI OLISHI UCHUN
 */
@Getter
@Setter
@ToString
@MappedSuperclass
public abstract class AbsDateAudit implements Serializable {

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Timestamp createdAt;//OBJECT YANGI OCHIGANDA ISHLATILADI

    private Boolean deleted = false;

}
