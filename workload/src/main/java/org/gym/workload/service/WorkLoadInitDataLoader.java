package org.gym.workload.service;

import org.gym.workload.entity.Trainer;
import org.gym.workload.entity.Year;
import org.gym.workload.repository.TrainerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkLoadInitDataLoader implements CommandLineRunner {
    private final TrainerRepository trainerRepository;

    public WorkLoadInitDataLoader(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        trainerRepository.deleteAll();

        List<Trainer> trainers = List.of(
                new Trainer("Ward.Mejia", "Ward", "Mejia", true, generateYearData()),
                new Trainer("Kathleen.Carr", "Kathleen", "Carr", true, generateYearData()),
                new Trainer("Coleman.Yates", "Coleman", "Yates", false, generateYearData()),
                new Trainer("Frazier.Richards", "Frazier", "Richards", true, generateYearData())
        );

        trainerRepository.saveAll(trainers);
    }

    private List<Year> generateYearData() {
        return List.of(
                new Year(2024, new int[]{40, 35, 30, 25, 50, 45, 55, 60, 20, 15, 10, 5}),
                new Year(2023, new int[]{30, 25, 20, 35, 45, 40, 50, 55, 60, 70, 80, 90})
        );
    }
}
