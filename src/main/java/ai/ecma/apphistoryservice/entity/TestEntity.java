//package ai.ecma.apphistoryservice.entity;
//
//import ai.ecma.apphistoryservice.aop.HistoryEntity;
//import ai.ecma.apphistoryservice.service.EntityListenerHistoryService;
//import lombok.*;
//
//import javax.persistence.*;
//import java.sql.Date;
//import java.util.List;
//
//@AllArgsConstructor
//@NoArgsConstructor
//@Getter
//@Setter
//@ToString
//@Entity
//@HistoryEntity
//@EntityListeners(value = EntityListenerHistoryService.class)
//public class TestEntity {
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
//}
