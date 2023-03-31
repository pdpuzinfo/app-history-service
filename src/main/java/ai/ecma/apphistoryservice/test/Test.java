//package ai.ecma.apphistoryservice.test;
//
//import ai.ecma.apphistoryservice.entity.TestEntity;
//import ai.ecma.apphistoryservice.entity.TestEntityRelation;
//import ai.ecma.apphistoryservice.repository.TestEntityRelRepository;
//import ai.ecma.apphistoryservice.repository.TestEntityRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.orm.jpa.EntityManagerFactoryUtils;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.annotation.*;
//
//import javax.persistence.EntityManager;
//import javax.persistence.EntityManagerFactory;
//import javax.persistence.PersistenceUnit;
//import javax.transaction.Transactional;
//import java.lang.reflect.Field;
//import java.sql.Date;
//import java.util.List;
//import java.util.UUID;
//
//@Component
//@EnableScheduling
//@RequiredArgsConstructor
//@RestController
//@RequestMapping("/test")
//public class Test {
//    private final TestEntityRepository testEntityRepository;
//    private final TestEntityRelRepository testEntityRelRepository;
//
////    @PersistenceUnit
////    private EntityManagerFactory entityManagerFactory;
//
////    @Scheduled(fixedRate = 3000)
//
//    @PostMapping("/{id}")
//    @Transactional
//    public void test(@PathVariable UUID id,
//                     @RequestBody TestEntityDTO testEntityPayload) {
//
//        TestEntity testEntity = testEntityRepository.findById(id).orElse(new TestEntity());
//
//
//        testEntity.setName(testEntityPayload.getName());
//        testEntity.setDate(testEntityPayload.getDate());
//        testEntity.setOk(testEntityPayload.getOk());
//        testEntity.setAge(testEntityPayload.getAge());
//
//        TestEntityRelation relation = new TestEntityRelation();
//        relation.setAge(12);
//        relation.setName(UUID.randomUUID().toString());
//        relation.setOk(true);
//
//        testEntityRelRepository.save(relation);
//
////        testEntity.setManyToOne(testEntityRelRepository.findById(testEntityPayload.getManyToOneId()).orElseThrow());
////        testEntity.setOneToOne(testEntityRelRepository.findById(testEntityPayload.getOneToOneId()).orElseThrow());
//
//        testEntityRepository.save(testEntity);
//
//        if (testEntityPayload.getOk()) {
//            testEntity.setAge((int) (Math.random() * 20));
//            testEntity.setName("changed " + (int) (Math.random() * 45));
//            testEntityRepository.save(testEntity);
//        }
//
////        List<TestEntity> all = testEntityRepository.findAll();
////        System.out.println(all);
//    }
//
//    @DeleteMapping("/{id}")
//    public void delete(@PathVariable UUID id){
//        testEntityRepository.deleteById(id);
////        TestEntity testEntity = testEntityRepository.findById(id).orElseThrow();
////        testEntityRepository.delete(testEntity);
//    }
//
//
//
//}
