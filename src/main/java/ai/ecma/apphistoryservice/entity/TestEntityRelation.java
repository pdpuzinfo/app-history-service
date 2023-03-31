//package ai.ecma.apphistoryservice.entity;
//
//import ai.ecma.apphistoryservice.service.EntityListenerHistoryService;
//import lombok.*;
//
//import javax.persistence.*;
//import java.sql.Date;
//
//@AllArgsConstructor
//@NoArgsConstructor
//@Getter
//@Setter
//@ToString
//@Entity
//@EntityListeners(value = EntityListenerHistoryService.class)
//public class TestEntityRelation implements Cloneable {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Integer id;
//
//    private String name;
//
//    private Date date;
//
//    private Boolean ok;
//
//    private int age;
//
//
//    @Override
//    public TestEntityRelation clone() {
//        try {
//            TestEntityRelation clone = (TestEntityRelation) super.clone();
//            // TODO: copy mutable state here, so the clone can't change the internals of the original
//            return clone;
//        } catch (CloneNotSupportedException e) {
//            throw new AssertionError();
//        }
//    }
//}
