package org.gym.basic.service;

import org.gym.basic.dto.TraineeCreatedDto;
import org.gym.basic.entity.Trainee;
import org.gym.basic.entity.Trainer;
import org.gym.basic.entity.Training;
import org.gym.basic.entity.User;
import org.gym.basic.feignclient.WorkloadClient;
import org.gym.basic.jwtbearerauth.JwtTokenService;
import org.gym.basic.message.WorkloadMessage;
import org.gym.basic.repository.TraineeRepository;
import org.gym.basic.repository.TrainerRepository;
import org.gym.basic.repository.TrainingRepository;
import org.gym.basic.repository.UserRepository;
import org.gym.workload.dto.WorkloadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.gym.basic.utility.MappingUtils.createRequest;
import static org.gym.basic.utility.PasswordGenerator.generatePassword;

@Service
@Transactional(readOnly = true)
public class TraineeService {

    private final TraineeRepository traineeRepository;
    private final UserRepository userRepository;
    private final TrainerRepository trainerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final WorkloadClient workloadClient;
    private final TrainingRepository trainingRepository;
    private final JmsTemplate jmsTemplate;

    private static final Logger logger = LoggerFactory.getLogger(TraineeService.class);

    public TraineeService(TraineeRepository traineeRepository, UserRepository userRepository, TrainerRepository trainerRepository, PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService, WorkloadClient workloadClient, TrainingRepository trainingRepository, JmsTemplate jmsTemplate) {
        this.traineeRepository = traineeRepository;
        this.userRepository = userRepository;
        this.trainerRepository = trainerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.workloadClient = workloadClient;
        this.trainingRepository = trainingRepository;
        this.jmsTemplate = jmsTemplate;
    }

    @Transactional
    public TraineeCreatedDto save(Trainee trainee) {

        String password = generatePassword();

        trainee.getUser().setUsername(createValidUserName(trainee));
        trainee.getUser().setPassword(passwordEncoder.encode(password));
        trainee.getUser().setIsActive(true);

        trainee = traineeRepository.save(trainee);

        String jwtToken = jwtTokenService.getJwtToken(trainee.getUser());

        trainee.getUser().setToken(jwtToken);

        return new TraineeCreatedDto(trainee.getUser().getUsername(), password, jwtToken);
    }

    public Optional<Trainee> usernameAndPasswordMatching(String username, String password) throws ServiceException {
        Optional<Trainee> trainee = findByUsername(username);
        if (trainee.isPresent()) {
            if (password.equals(trainee.get().getUser().getPassword())) {
                return trainee;
            }
        }
        return Optional.empty();
    }

    public Optional<Trainee> findByUsername(String username) throws ServiceException {
        try {

            Optional<User> userFromDB = userRepository.findByUsername(username);

            if (userFromDB.isPresent() && Objects.nonNull(userFromDB.get().getTrainee())) {

                return Optional.of(userFromDB.get().getTrainee());

            } else {
                return Optional.empty();
            }

        } catch (Exception e) {
            logger.error("Fail to get trainee with username {}  from DB", username);
            throw new ServiceException("Fail to get from DB trainee with username " + username, e);
        }
    }

    @Transactional
    public Optional<Trainee> update(Trainee trainee) throws ServiceException {

        Optional<Trainee> traineeFromDB = traineeRepository.findById(trainee.getId());

        if (traineeFromDB.isPresent()) {

            traineeFromDB.get().getUser().setFirstname(trainee.getUser().getFirstname());
            traineeFromDB.get().getUser().setLastname(trainee.getUser().getLastname());
            traineeFromDB.get().getUser().setIsActive(trainee.getUser().isActive());

            if (trainee.getDateOfBirth() != null) {
                traineeFromDB.get().setDateOfBirth(trainee.getDateOfBirth());
            }
            if (trainee.getAddress() != null) {
                traineeFromDB.get().setAddress(trainee.getAddress());
            }

            trainee = traineeRepository.save(traineeFromDB.get());
        } else {
            return Optional.empty();
        }

        return Optional.of(trainee);

    }

    @Transactional
    public Optional<Trainee> changeStatus(Trainee trainee) throws ServiceException {

        Optional<Trainee> traineeFromDB = traineeRepository.findById(trainee.getId());

        if (traineeFromDB.isPresent()) {
            traineeFromDB.get().getUser().setIsActive(trainee.getUser().isActive());
            trainee = traineeRepository.save(traineeFromDB.get());
        } else {
            return Optional.empty();
        }

        return Optional.of(trainee);

    }

    @Transactional
    public void deleteByUsername(String username) throws ServiceException {
        Optional<Trainee> traineeFromDB = findByUsername(username);

        if (traineeFromDB.isEmpty()) {
            logger.error("Fail to delete, no trainee with userName {} in DB ", username);
            throw new ServiceException("Fail to delete, no trainee with userName {} in DB " + username);
        }

        try {
            Date currentDate = new Date(System.currentTimeMillis());

            List<Training> trainingList = traineeFromDB.get().getTrainings().stream()
                    .filter(t -> t.getTrainingDay().after(currentDate))
                    .toList();

            for (Training training : trainingList) {
                WorkloadMessage workloadMessage = WorkloadMessage.createFromTraining(training, WorkloadMessage.ActionType.DELETE);

                try {
                    jmsTemplate.convertAndSend("workload-queue", workloadMessage);
                } catch (JmsException ex) {
                    throw new ServiceException("Fail to send message to Queue", ex);
                }

                trainingRepository.deleteById(training.getId());
            }

            traineeRepository.delete(traineeFromDB.get());
        } catch (Exception e) {
            logger.error("Fail to delete trainee with userName {} from DB ", username);
            throw new ServiceException("Fail to delete trainee from DB with userName" + username, e);
        }
    }

    public List<Training> getTrainings(String traineeUsername, Date fromDate, Date toDate, String trainerName, String trainingType) throws ServiceException {
        Optional<Trainee> traineeFromDB = findByUsername(traineeUsername);

        Predicate<Training> fromDateTest = fromDate != null ? t -> t.getTrainingDay().compareTo(fromDate) >= 0 : t -> true;
        Predicate<Training> toDateTest = toDate != null ? t -> t.getTrainingDay().compareTo(toDate) <= 0 : t -> true;
        Predicate<Training> trainerNameTest = trainerName != null ? t -> t.getTrainer().getUser().getFirstname().equals(trainerName) : t -> true;
        Predicate<Training> trainingTypeTest = trainingType != null ? t -> t.getTrainingType().getTrainingType().equals(trainingType) : t -> true;

        return traineeFromDB
                .map(
                        trainee -> trainee.getTrainings().stream()
                                .filter(fromDateTest.and(toDateTest).and(trainerNameTest).and(trainingTypeTest))
                                .collect(Collectors.toList())
                )
                .orElseGet(ArrayList::new);
    }

    @Transactional
    public List<Trainer> updateTrainersList(Trainee trainee) throws ServiceException {
        Optional<Trainee> traineeFromDB = traineeRepository.findById(trainee.getId());

        if (traineeFromDB.isPresent()) {

            List<Trainer> traineeTrainersList = traineeFromDB.get().getTrainers();

            for (Trainer trainer : trainee.getTrainers()) {

                Optional<Trainer> trainerFromDB;

                try {
                    trainerFromDB = trainerRepository.findByUsername(trainer.getUser().getUsername());
                } catch (Exception e) {
                    throw new ServiceException("Something went wrong!!!", e);
                }

                if (trainerFromDB.isPresent()) {

                    if (!traineeTrainersList.contains(trainerFromDB.get())) {

                        traineeTrainersList.add(trainerFromDB.get());
                    }
                }
            }
        }

        return traineeRepository.save(traineeFromDB.get()).getTrainers();

    }

    public List<Trainer> getTrainerList(String username) throws ServiceException {
        Optional<Trainee> traineeFromDB = findByUsername(username);

        if (traineeFromDB.isPresent()) {
            traineeFromDB.get().getTrainers().size();
            return traineeFromDB.get().getTrainers();
        }

        return new ArrayList<>();
    }

    public List<Trainer> getNotAssignedOnTraineeTrainersByTraineeUsername(String traineeUsername) throws ServiceException {
        Optional<Trainee> traineeFromDB = findByUsername(traineeUsername);

        List<Trainer> traineeTrainers;
        List<Trainer> trainers;

        if (traineeFromDB.isPresent()) {
            traineeTrainers = traineeFromDB.get().getTrainers();
        } else {
            throw new ServiceException("Fail to get trainers list by trainee name from DB");
        }

        try {
            trainers = trainerRepository.findAll();
        } catch (Exception e) {
            logger.error("Fail to get trainers from DB");
            throw new ServiceException("Fail to get trainers from DB", e);
        }

        trainers.removeAll(traineeTrainers);

        return trainers.stream().filter(t -> t.getUser().isActive()).collect(Collectors.toList());
    }

    private String createValidUserName(Trainee trainee) {

        String username = trainee.getUser().getFirstname() + "." + trainee.getUser().getLastname();

        if (userRepository.findByUsername(username).isEmpty()) {
            return username;
        }

        for (long i = 0; i < Long.MAX_VALUE; i++) {
            StringBuilder newUsername = new StringBuilder(username + i);
            if (userRepository.findByUsername(newUsername.toString()).isEmpty()) {
                return newUsername.toString();
            }
        }

        return username;
    }
}
