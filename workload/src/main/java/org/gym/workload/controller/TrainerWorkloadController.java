package org.gym.workload.controller;

import jakarta.validation.Valid;
import org.gym.workload.dto.WorkloadRequest;
import org.gym.workload.exception.ServiceException;
import org.gym.workload.service.TrainerWorkloadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.DateTimeException;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("workload")
public class TrainerWorkloadController {
    private final TrainerWorkloadService service;

    public TrainerWorkloadController(TrainerWorkloadService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public String process(@Valid @RequestBody WorkloadRequest request) throws ServiceException {
        service.process(request);
        return "successful";
    }

    @GetMapping
    @ResponseBody
    public String getDuration(@RequestParam("username") String username,
                              @RequestParam("year") Integer year,
                              @RequestParam("month") Integer month) {
        int workload = service.getDuration(username, year, month);
        return String.format("Workload for %s in %s %s is %s hrs %s min", username, Month.of(month), year, workload / 60, workload % 60);

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<String> handleServiceExceptions(ServiceException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(DateTimeException.class)
    public ResponseEntity<String> handleDateTimeExceptions(DateTimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

}
