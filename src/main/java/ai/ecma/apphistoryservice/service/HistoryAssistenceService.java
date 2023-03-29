package ai.ecma.apphistoryservice.service;

import java.util.UUID;

public interface HistoryAssistenceService {

    UUID currentUserId();

    void sendErrorToBot(Exception exception);

}
