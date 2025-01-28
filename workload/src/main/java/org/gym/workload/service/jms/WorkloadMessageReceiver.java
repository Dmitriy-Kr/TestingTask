package org.gym.workload.service.jms;

import jakarta.validation.Valid;
import org.gym.workload.exception.ServiceException;
import org.gym.workload.message.WorkloadMessage;
import org.gym.workload.service.TrainerWorkloadService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class WorkloadMessageReceiver {
    private final TrainerWorkloadService service;

    public WorkloadMessageReceiver(TrainerWorkloadService service) {
        this.service = service;
    }

    @JmsListener(destination = "workload-queue")
    public void receiveMessage(@Valid WorkloadMessage message) throws ServiceException {
        service.process(message);
    }
}
