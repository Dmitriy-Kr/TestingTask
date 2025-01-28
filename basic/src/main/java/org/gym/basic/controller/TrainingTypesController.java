package org.gym.basic.controller;

import org.gym.basic.dto.TrainingTypeDto;
import org.gym.basic.exception.InvalidDataException;
import org.gym.basic.service.ServiceException;
import org.gym.basic.service.TrainingTypeService;
import org.gym.basic.utility.MappingUtils;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/training-type")
public class TrainingTypesController {
    private TrainingTypeService trainingTypeService;
    private MeterRegistry meterRegistry;

    public TrainingTypesController(TrainingTypeService trainingTypeService, MeterRegistry meterRegistry) {
        this.trainingTypeService = trainingTypeService;
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/all")
    @ResponseBody
    @Operation(summary = "Get Training types")
    public List<TrainingTypeDto> findAllTrainingType() throws ServiceException, InvalidDataException {

            Timer.Sample timer = Timer.start(meterRegistry);

            List<TrainingTypeDto> resultList = trainingTypeService.getAll().stream()
                    .map(MappingUtils::mapToTrainingTypeDto)
                    .collect(Collectors.toList());

            timer.stop(Timer.builder("find_trainingTypes_timer")
                    .description("trainingTypes searching timer")
                    .tags("version", "1.0")
                    .register(meterRegistry));

            return resultList;

    }
}
