package org.gym.workload.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gym.workload.dto.WorkloadRequest;
import org.gym.workload.exception.ServiceException;
import org.gym.workload.service.TrainerWorkloadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainerWorkloadController.class)
public class TrainerWorkloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrainerWorkloadService service;

    @Test
    void testProcessSuccess() throws Exception {
        WorkloadRequest request = new WorkloadRequest();
        request.setTrainerUsername("Ward.Mejia");
        request.setTrainerFirstName("Ward");
        request.setTrainerLastName("Mejia");
        request.setTrainingDuration(300);
        request.setTrainingDate(Date.valueOf("2025-12-01"));
        request.setActionType(WorkloadRequest.ActionType.ADD);

        mockMvc.perform(post("/workload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("successful"));
    }

    @Test
    void testProcessValidationError() throws Exception {
        WorkloadRequest invalidRequest = new WorkloadRequest(); // Missing fields

        mockMvc.perform(post("/workload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.trainerUsername").exists())
                .andExpect(jsonPath("$.trainerLastName").exists())
                .andExpect(jsonPath("$.trainerFirstName").exists())
                .andExpect(jsonPath("$.trainingDate").exists())
                .andExpect(jsonPath("$.trainingDuration").exists())
                .andExpect(jsonPath("$.actionType").exists());
    }

    @Test
    void testProcessServiceException() throws Exception {
        WorkloadRequest request = new WorkloadRequest();
        request.setTrainerUsername("Ward.Mejia");
        request.setTrainerFirstName("Ward");
        request.setTrainerLastName("Mejia");
        request.setTrainingDuration(300);
        request.setTrainingDate(Date.valueOf("2025-12-01"));
        request.setActionType(WorkloadRequest.ActionType.ADD);

        doThrow(new ServiceException("Service error"))
                .when(service).process(any(WorkloadRequest.class));

        mockMvc.perform(post("/workload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Service error"));
    }

    @Test
    void testGetDurationSuccess() throws Exception {
        when(service.getDuration("Ward.Mejia", 2025, 1)).thenReturn(125);

        mockMvc.perform(get("/workload")
                        .param("username", "Ward.Mejia")
                        .param("year", "2025")
                        .param("month", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Workload for Ward.Mejia in JANUARY 2025 is 2 hrs 5 min"));
    }

    @Test
    void testGetDurationInvalidParams() throws Exception {
        mockMvc.perform(get("/workload")
                        .param("username", "")
                        .param("year", "2025")
                        .param("month", "13")) // Invalid month
                .andExpect(status().isBadRequest());
    }

}

