package org.gym.workload.service;

import org.gym.workload.dto.WorkloadRequest;
import org.gym.workload.entity.Trainer;
import org.gym.workload.entity.Year;
import org.gym.workload.exception.ServiceException;
import org.gym.workload.message.WorkloadMessage;
import org.gym.workload.repository.TrainerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerWorkloadService {
    private final TrainerRepository repository;

    private static final Logger logger = LoggerFactory.getLogger(TrainerWorkloadService.class);

    public TrainerWorkloadService(TrainerRepository repository) {
        this.repository = repository;
    }

    public void process(WorkloadRequest request) throws ServiceException {
        try {
            switch (request.getActionType()) {
                case ADD:
                    add(request);
                    break;
                case DELETE:
                    delete(request);
            }

        } catch (Exception ex) {
            logger.error("Fail to process request");
            throw new ServiceException("Fail to process request", ex);
        }
    }

    public void process(WorkloadMessage message) throws ServiceException {
        try {
            switch (message.getActionType()) {
                case ADD:
                    add(message);
                    break;
                case DELETE:
                    delete(message);
            }

        } catch (Exception ex) {
            logger.error("Fail to process message");
            throw new ServiceException("Fail to process message", ex);
        }
    }

    public int getDuration(String username, Integer year, Integer month) {
        Optional<Trainer> trainer = repository.findByUsername(username);

        return trainer.map(value -> value.getYears().stream()
                .filter(y -> y.getYearNumber() == year)
                .findFirst().orElse(new Year()).getDurationByMonths()[month]).orElse(-100);
        // need throwing exception here?
    }

    private void add(WorkloadMessage message) {
        WorkloadRequest workloadRequest = convertMessageToWorkloadRequest(message);

        add(workloadRequest);
    }

    private void delete(WorkloadMessage message) {
        WorkloadRequest workloadRequest = convertMessageToWorkloadRequest(message);
        delete(workloadRequest);
    }

    private WorkloadRequest convertMessageToWorkloadRequest(WorkloadMessage message) {
        WorkloadRequest workloadRequest = new WorkloadRequest();

        workloadRequest.setActionType(WorkloadRequest.ActionType.valueOf(message.getActionType().name()));
        workloadRequest.setTrainerUsername(message.getTrainerUsername());
        workloadRequest.setTrainerFirstName(message.getTrainerFirstName());
        workloadRequest.setTrainerLastName(message.getTrainerLastName());
        workloadRequest.setActive(message.isActive());
        workloadRequest.setTrainingDuration(message.getTrainingDuration());
        workloadRequest.setTrainingDate(message.getTrainingDate());
        return workloadRequest;
    }

    private void add(WorkloadRequest request) {
        Optional<Trainer> trainer = repository.findByUsername(request.getTrainerUsername());

        if (trainer.isPresent()) {

            trainer.get().setStatus(request.isActive());

            Year year = trainer.get().getYears()
                    .stream()
                    .filter(y -> y.getYearNumber() == request.getTrainingDate().toLocalDate().getYear())
                    .findAny().orElse(new Year());

            if (year.getYearNumber() != 0) { //Check if obj exist or was created step before

                int month = request.getTrainingDate().toLocalDate().getMonthValue() - 1;
                int monthDuration = year.getDurationByMonths()[month];
                monthDuration += request.getTrainingDuration();

                year.getDurationByMonths()[month] = monthDuration;

            } else {

                year.setYearNumber(request.getTrainingDate().toLocalDate().getYear());

                int month = request.getTrainingDate().toLocalDate().getMonthValue() - 1;

                year.getDurationByMonths()[month] = request.getTrainingDuration();

                trainer.get().getYears().add(year);

            }

            repository.save(trainer.get());

        } else { //create new Trainer

            Trainer newTrainer = new Trainer();

            newTrainer.setUsername(request.getTrainerUsername());
            newTrainer.setFirstname(request.getTrainerFirstName());
            newTrainer.setLastname(request.getTrainerLastName());
            newTrainer.setStatus(request.isActive());

            Year year = new Year();
            year.setYearNumber(request.getTrainingDate().toLocalDate().getYear());
            int month = request.getTrainingDate().toLocalDate().getMonthValue() - 1;
            year.getDurationByMonths()[month] = request.getTrainingDuration();

            newTrainer.setYears(List.of(year));

            repository.save(newTrainer);

        }
    }

    private void delete(WorkloadRequest request) {
        int month = request.getTrainingDate().toLocalDate().getMonthValue() - 1;
        int monthDuration = request.getTrainingDuration();
        Optional<Trainer> trainer = repository.findByUsername(request.getTrainerUsername());

        if (trainer.isPresent()) {

            var year = trainer.get().getYears().stream()
                    .filter(y -> y.getYearNumber() == request.getTrainingDate().toLocalDate().getYear())
                    .findFirst();

            if (year.isPresent()) {
                int currentMonthDuration = year.get().getDurationByMonths()[month];
                year.get().getDurationByMonths()[month] = monthDuration > currentMonthDuration ? 0 : currentMonthDuration - monthDuration;

                repository.save(trainer.get());
            }
        }
        //need else with throwing exception?
    }
}
