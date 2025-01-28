package org.gym.basic.feignclient;

import org.gym.workload.dto.WorkloadRequest;
import org.springframework.stereotype.Component;

@Component
public class WorkloadClientFallback implements WorkloadClient{
    @Override
    public String getDuration(String username, Integer year, Integer month) {
        return "The service is temporarily unavailable.";
    }

    @Override
    public String process(WorkloadRequest request) {
        return "The service is temporarily unavailable.";
    }
}
