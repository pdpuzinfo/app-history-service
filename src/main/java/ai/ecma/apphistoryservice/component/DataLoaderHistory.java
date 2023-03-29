package ai.ecma.apphistoryservice.component;

import ai.ecma.apphistoryservice.repository.HistoryRepository;
import ai.ecma.apphistoryservice.service.HistoryAssistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoaderHistory implements CommandLineRunner {
    private final HistoryRepository historyRepository;

    @Override
    public void run(String... args) {
        historyRepository.initIndexes();
        checkAssistanceBeansExist();
    }

    private void checkAssistanceBeansExist() {
        try {
            HistoryAssistenceService bean = BeanUtilHistory.getBean(HistoryAssistenceService.class);
        }catch (Exception e){
            System.err.println("implementation of history assistance bean service is requeired!!!");
            System.exit(1);
            throw e;
        }
    }
}
