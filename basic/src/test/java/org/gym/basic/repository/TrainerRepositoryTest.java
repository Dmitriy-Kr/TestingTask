package org.gym.basic.repository;
import org.gym.basic.entity.Trainer;
import org.gym.basic.entity.TrainingType;
import org.gym.basic.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
class TrainerRepositoryTest {

    @Autowired
    private TrainerRepository trainerRepository;

    @AfterEach
    void tearDown() {
        trainerRepository.deleteAll();
    }

    @Test
    void findByUsernameIfTrainerExists() {
        Trainer trainer = new Trainer();
        trainer.setSpecialization(new TrainingType(2L, "fitness"));
        User user = new User();
        user.setFirstname("Fred");
        user.setLastname("Mercury");
        user.setUsername("Fred.Mercury");
        user.setPassword("1234567898");
        user.setIsActive(true);
        trainer.setUser(user);

        trainerRepository.save(trainer);

        assertEquals(trainer.getUser().getPassword(), trainerRepository.findByUsername("Fred.Mercury").get().getUser().getPassword());
    }

    @Test
    void findByUsernameIfTrainerDoesNotExists() {

        assertTrue(trainerRepository.findByUsername("Fred.Mercury").isEmpty());

    }
}