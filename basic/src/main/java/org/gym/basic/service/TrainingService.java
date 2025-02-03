package org.gym.basic.service;

import org.gym.basic.dto.TrainingDto;
import org.gym.basic.entity.Training;
import org.gym.basic.feignclient.WorkloadClient;
import org.gym.basic.message.WorkloadMessage;
import org.gym.basic.repository.TrainingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingTypeService trainingTypeService;

    private final WorkloadClient workloadClient;

    private final JmsTemplate jmsTemplate;

    private static final Logger logger = LoggerFactory.getLogger(TrainingService.class);

    public TrainingService(WorkloadClient workloadClient, TrainingTypeService trainingTypeService, TrainerService trainerService, TraineeService traineeService, TrainingRepository trainingRepository, JmsTemplate jmsTemplate) {
        this.workloadClient = workloadClient;
        this.trainingTypeService = trainingTypeService;
        this.trainerService = trainerService;
        this.traineeService = traineeService;
        this.trainingRepository = trainingRepository;
        this.jmsTemplate = jmsTemplate;
    }

    @Transactional
    public Training create(TrainingDto trainingDto) throws ServiceException {
        try {
            Training training = new Training();

            training.setTrainingType(trainingTypeService
                    .findByTrainingType(trainingDto.getTrainingType())
                    .orElseThrow(() -> new ServiceException("Fail to create training. No such training type present in DB")));

            training.setTrainee(traineeService
                    .findByUsername(trainingDto.getTraineeUsername())
                    .orElseThrow(() -> new ServiceException("Fail to create training. No such trainee present in DB")));

            training.setTrainer(trainerService
                    .findByUsername(trainingDto.getTrainerUsername())
                    .orElseThrow(() -> new ServiceException("Fail to create training. No such trainer present in DB")));

            training.setTrainingName(trainingDto.getTrainingName());
            training.setTrainingDay(Date.valueOf(LocalDate.parse(trainingDto.getTrainingDay())));
            training.setTrainingDuration(trainingDto.getTrainingDuration());

            WorkloadMessage workloadMessage = WorkloadMessage.createFromTraining(training, WorkloadMessage.ActionType.ADD);

            try {
                jmsTemplate.convertAndSend("workload-queue", workloadMessage);
            } catch (JmsException ex) {
                throw new ServiceException("Fail to send message to Queue", ex);
            }

            return trainingRepository.save(training);

        } catch (Exception e) {
            logger.error("Fail to create training with trainingName {} ", trainingDto.getTrainingName());
            throw new ServiceException("Fail to create training with trainingName " + trainingDto.getTrainingName() + ". " + e.getMessage(), e);
        }
    }

    public Training getTrainingById(long id) throws ServiceException {
        return trainingRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Fail to find training. No such training present in DB"));
    }

    @Transactional
    public void deleteTrainingById(long id) throws ServiceException {
        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Fail to find training. No such training present in DB"));

        WorkloadMessage workloadMessage = WorkloadMessage.createFromTraining(training, WorkloadMessage.ActionType.DELETE);

        try {
            jmsTemplate.convertAndSend("workload-queue", workloadMessage);
        } catch (JmsException ex) {
            throw new ServiceException("Fail to send message to Queue", ex);
        }

        trainingRepository.deleteById(id);
    }

}
