//package ai.ecma.apphistoryservice.test;
//
//import ai.ecma.apphistoryservice.entity.TestEntity;
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
//import java.lang.reflect.Field;
//import java.sql.Date;
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
//    public void test(@PathVariable Integer id,
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
//        testEntity.setManyToOne(testEntityRelRepository.findById(testEntityPayload.getManyToOneId()).orElseThrow());
////        testEntity.setOneToOne(testEntityRelRepository.findById(testEntityPayload.getOneToOneId()).orElseThrow());
//
//        testEntityRepository.save(testEntity);
//    }
//
//    @DeleteMapping("/{id}")
//    public void delete(@PathVariable Integer id){
//        testEntityRepository.deleteById(id);
////        TestEntity testEntity = testEntityRepository.findById(id).orElseThrow();
////        testEntityRepository.delete(testEntity);
//    }
//
//
//
//}
