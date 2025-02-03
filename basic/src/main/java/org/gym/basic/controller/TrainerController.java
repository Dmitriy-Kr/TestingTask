package org.gym.basic.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.gym.basic.dto.TrainerCreatedDto;
import org.gym.basic.dto.TrainerDto;
import org.gym.basic.dto.TrainerTrainingDto;
import org.gym.basic.entity.Trainer;
import org.gym.basic.exception.InvalidDataException;
import org.gym.basic.exception.NoResourcePresentException;
import org.gym.basic.feignclient.WorkloadClient;
import org.gym.basic.service.ServiceException;
import org.gym.basic.service.TrainerService;
import org.gym.basic.utility.MappingUtils;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.gym.basic.utility.MappingUtils.*;
import static org.gym.basic.utility.Validation.*;
import static org.gym.basic.utility.Validation.validateDate;

@Controller
@RequestMapping("/trainer")
public class TrainerController {
    private final TrainerService trainerService;
    private final WorkloadClient workloadClient;

    public TrainerController(TrainerService trainerService, WorkloadClient workloadClient) {
        this.trainerService = trainerService;
        this.workloadClient = workloadClient;
    }

    @GetMapping
    @ResponseBody
    @Operation(summary = "Get trainer profile by Username")
    public TrainerDto findByUsername(@RequestParam("username") String username) throws ServiceException, NoResourcePresentException, InvalidDataException {
        Optional<Trainer> trainer;
        validateLogin(username);
        trainer = trainerService.findByUsername(username);
        if (trainer.isPresent()) {
            return mapToTrainerDto(trainer.get());
        } else {
            throw new NoResourcePresentException("Cannot find trainer with username " + username);
        }
    }

    @PostMapping
    @ResponseBody
    @Operation(summary = "New Trainer registration")
    public TrainerCreatedDto create(@RequestBody TrainerDto trainerDto) throws InvalidDataException, ServiceException {
        validateName(trainerDto.getFirstname());
        validateName(trainerDto.getLastname());
        return trainerService.save(mapToTrainer(trainerDto));
    }

    @PutMapping("/{id}")
    @ResponseBody
    @Operation(summary = "Update Trainer Profile")
    public TrainerDto update(@PathVariable("id") Long id, @RequestBody TrainerDto trainerDto) throws ServiceException, InvalidDataException {

        validateName(trainerDto.getFirstname());
        validateName(trainerDto.getLastname());
        Trainer trainer = mapToTrainer(trainerDto);
        trainer.setId(id);
        return mapToTrainerDto(trainerService.update(trainer).orElseThrow(() -> new ServiceException("Update trainer failed")));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activate/De-Activate Trainer")
    public void changeStatus(@PathVariable Long id, @RequestBody TrainerDto trainerDto) throws ServiceException, InvalidDataException {

        validateLogin(trainerDto.getUsername());
        Trainer trainer = mapToTrainer(trainerDto);
        trainer.setId(id);
        trainerService.changeStatus(trainer).orElseThrow(() -> new ServiceException("Cannot change trainer status"));
    }

    @GetMapping("/trainings")
    @ResponseBody
    @Operation(summary = "Get Trainer Trainings List")
    public List<TrainerTrainingDto> findTrainings(@RequestParam("username") String username,
                                                  @RequestParam(value = "fromDate", required = false) String fromDateParameter,
                                                  @RequestParam(value = "toDate", required = false) String toDateParameter,
                                                  @RequestParam(value = "traineeName", required = false) String traineeName) throws ServiceException, InvalidDataException {

        validateLogin(username);

        Date fromDate = null;
        Date toDate = null;

        if (fromDateParameter != null) {
            if (validateDate(fromDateParameter)) {
                fromDate = Date.valueOf(LocalDate.parse(fromDateParameter));
            }
        }

        if (toDateParameter != null) {
            if (validateDate(toDateParameter)) {
                toDate = Date.valueOf(LocalDate.parse(toDateParameter));
            }
        }

        if (traineeName != null) {
            validateName(traineeName);
        }

        return trainerService.getTrainings(username, fromDate, toDate, traineeName).stream()
                .map(MappingUtils::mapToTrainerTrainingDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/workload")
    @ResponseBody
    @Operation(summary = "Get trainer workload")
    public String getWorkload(@RequestParam("username") String username,
                           @RequestParam("year") Integer year,
                           @RequestParam("month") Integer month) {

        return  workloadClient.getDuration(username, year, month);

    }

}
