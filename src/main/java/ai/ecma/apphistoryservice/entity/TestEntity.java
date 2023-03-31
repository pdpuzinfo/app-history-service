//package ai.ecma.apphistoryservice.entity;
//
//import ai.ecma.apphistoryservice.service.EntityListenerHistoryService;
//import lombok.*;
//import org.hibernate.annotations.GenericGenerator;
//import org.hibernate.annotations.Type;
//
//import javax.persistence.*;
//import java.sql.Date;
//import java.util.List;
//import java.util.UUID;
//
//@AllArgsConstructor
//@NoArgsConstructor
//@Getter
//@Setter
//@ToString
//@Entity
//@EntityListeners(value = EntityListenerHistoryService.class)
//public class TestEntity implements Cloneable {
//
//    @Id
//    @Type(type = "org.hibernate.type.PostgresUUIDType")
//    @GeneratedValue(generator = "uuid2")
//    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
//    private UUID id;
//
//    private String name;
//
//    private Date date;
//
//    private Boolean ok;
//
//    private int age;
//
//    @ElementCollection
//    private List<Integer> ints;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @ToString.Exclude
//    private TestEntityRelation manyToOne;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @ToString.Exclude
//    private TestEntityRelation oneToOne;
//
//    @Override
//    public TestEntity clone() {
//        try {
//            TestEntity clone = (TestEntity) super.clone();
//            // TODO: copy mutable state here, so the clone can't change the internals of the original
//            return clone;
//        } catch (CloneNotSupportedException e) {
//            throw new AssertionError();
//        }
//    }
//}
