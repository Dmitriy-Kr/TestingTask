package org.gym.basic.service;

import org.gym.basic.entity.TrainingType;
import org.gym.basic.repository.TrainingTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
@Service
@Transactional(readOnly = true)
public class TrainingTypeService {
    private TrainingTypeRepository trainingTypeRepository;

    public TrainingTypeService(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }
    public Optional<TrainingType> findByTrainingType(String trainingType) throws ServiceException {
        try {
            return trainingTypeRepository.findByTrainingType(trainingType);
        } catch (Exception e) {
            throw new ServiceException("Fail to get from DB trainingType " + trainingType, e);
        }
    }

    public Optional<TrainingType> getById(Long id) {
        return trainingTypeRepository.findById(id);
    }

    public List<TrainingType> getAll() {
        return trainingTypeRepository.findAll();
    }
}
