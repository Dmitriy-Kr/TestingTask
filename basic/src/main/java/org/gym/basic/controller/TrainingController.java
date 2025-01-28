package org.gym.basic.controller;

import org.gym.basic.dto.TrainingDto;
import org.gym.basic.exception.InvalidDataException;
import org.gym.basic.service.ServiceException;
import org.gym.basic.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static org.gym.basic.utility.Validation.*;

@Controller
@RequestMapping("/training")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @PostMapping
    @Operation(summary = "Create new Training")
    @ResponseStatus(HttpStatus.OK)
    public void create(@RequestBody TrainingDto trainingDto) throws InvalidDataException, ServiceException {
        validateLogin(trainingDto.getTraineeUsername());

        validateName(trainingDto.getTrainingName());

        validateDate(trainingDto.getTrainingDay());

        trainingService.create(trainingDto);

    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Training")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") long id) throws ServiceException {
        trainingService.deleteTrainingById(id);
    }
}
