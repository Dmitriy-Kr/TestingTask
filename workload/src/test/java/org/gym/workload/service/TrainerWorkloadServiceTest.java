package org.gym.workload.service;

import org.gym.workload.dto.WorkloadRequest;
import org.gym.workload.entity.Trainer;
import org.gym.workload.entity.Year;
import org.gym.workload.exception.ServiceException;
import org.gym.workload.repository.TrainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class})
class TrainerWorkloadServiceTest {

    private TrainerWorkloadService service;

    @Mock
    private TrainerRepository repository;

    @BeforeEach
    void setUp() {

        service = new TrainerWorkloadService(repository);
    }

    @Test
    void testGetDuration_TrainerExists() {
        // Arrange
        String username = "trainer1";
        int year = 2024;
        int month = 12;

        Trainer trainer = new Trainer();
        Year trainingYear = new Year();
        trainingYear.setYearNumber(year);
//        Month trainingMonth = new Month();
//        trainingMonth.setMonthNumber(month);
//        trainingMonth.setTrainingSummaryDuration(120);
//        trainingYear.setDurationByMonths(List.of(trainingMonth));
        trainer.setYears(List.of(trainingYear));

        when(repository.findByUsername(username)).thenReturn(Optional.of(trainer));

        // Act
        int duration = service.getDuration(username, year, month);

        // Assert
        assertEquals(120, duration);
        verify(repository).findByUsername(username);
    }

    @Test
    void testGetDuration_TrainerDoesNotExist() {
        // Arrange
        String username = "nonexistent_trainer";
        when(repository.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        int duration = service.getDuration(username, 2024, 12);

        // Assert
        assertEquals(-100, duration); // Ensure default return value
        verify(repository).findByUsername(username);
    }

    @Test
    void testProcess_AddAction() throws ServiceException {
        // Arrange
        WorkloadRequest request = new WorkloadRequest();
        request.setActionType(WorkloadRequest.ActionType.ADD);
        request.setTrainerUsername("trainer1");
        request.setTrainingDate(Date.valueOf("2024-12-01"));
        request.setTrainingDuration(60);
        request.setActive(true);

        Trainer trainer = new Trainer();
        trainer.setUsername("trainer1");
        trainer.setYears(new ArrayList<>());
        when(repository.findByUsername("trainer1")).thenReturn(Optional.of(trainer));

        // Act
        service.process(request);

        // Assert
        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(repository).save(captor.capture());

        Trainer savedTrainer = captor.getValue();
        assertEquals(1, savedTrainer.getYears().size());
    }

    @Test
    void testProcess_DeleteAction() throws ServiceException {
        // Arrange
        WorkloadRequest request = new WorkloadRequest();
        request.setActionType(WorkloadRequest.ActionType.DELETE);
        request.setTrainerUsername("trainer1");
        request.setTrainingDate(Date.valueOf("2024-12-01"));
        request.setTrainingDuration(30);

        Trainer trainer = new Trainer();
        Year year = new Year();
        year.setYearNumber(2024);
//        Month month = new Month();
//        month.setMonthNumber(12);
//        month.setTrainingSummaryDuration(60);
//        year.setDurationByMonths(List.of(month));
        trainer.setYears(List.of(year));
        when(repository.findByUsername("trainer1")).thenReturn(Optional.of(trainer));

        // Act
        service.process(request);

        // Assert
        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(repository).save(captor.capture());

        Trainer savedTrainer = captor.getValue();
//        assertEquals(30, savedTrainer.getYears().get(0).getDurationByMonths().get(0).getTrainingSummaryDuration());
    }
}
