package ai.ecma.apphistoryservice.repository;

import ai.ecma.apphistoryservice.entity.History;
import ai.ecma.apphistoryservice.utils.AppConstant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.UUID;

public interface HistoryRepository extends JpaRepository<History, UUID> {

    @Transactional
    @Modifying
    @Query(value = AppConstant.INDEXES,nativeQuery = true)
    void initIndexes();


}
