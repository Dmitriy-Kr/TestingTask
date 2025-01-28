package org.gym.basic.message;

import org.gym.basic.entity.Training;

import java.sql.Date;

public class WorkloadMessage {
    private String trainerUsername;

    private String trainerFirstName;

    private String trainerLastName;

    private boolean active;

    private Date trainingDate;

    private Integer trainingDuration;

    private ActionType actionType;

    public static WorkloadMessage createFromTraining(Training training, ActionType actionType){
        WorkloadMessage workloadMessage = new WorkloadMessage();

        workloadMessage.setActionType(actionType);
        workloadMessage.setTrainerUsername(training.getTrainer().getUser().getUsername());
        workloadMessage.setTrainerFirstName(training.getTrainer().getUser().getFirstname());
        workloadMessage.setTrainerLastName(training.getTrainer().getUser().getLastname());
        workloadMessage.setActive(training.getTrainer().getUser().isActive());
        workloadMessage.setTrainingDuration(training.getTrainingDuration());
        workloadMessage.setTrainingDate(training.getTrainingDay());

        return workloadMessage;
    }
    // Enum for action type
    public enum ActionType {
        ADD, DELETE
    }

    public WorkloadMessage() {
    }

    public WorkloadMessage(String trainerUsername, String trainerFirstName, String trainerLastName, boolean active, Date trainingDate, Integer trainingDuration, ActionType actionType) {
        this.trainerUsername = trainerUsername;
        this.trainerFirstName = trainerFirstName;
        this.trainerLastName = trainerLastName;
        this.active = active;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
        this.actionType = actionType;
    }

    public String getTrainerUsername() {
        return trainerUsername;
    }

    public void setTrainerUsername(String trainerUsername) {
        this.trainerUsername = trainerUsername;
    }

    public String getTrainerFirstName() {
        return trainerFirstName;
    }

    public void setTrainerFirstName(String trainerFirstName) {
        this.trainerFirstName = trainerFirstName;
    }

    public String getTrainerLastName() {
        return trainerLastName;
    }

    public void setTrainerLastName(String trainerLastName) {
        this.trainerLastName = trainerLastName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(Date trainingDate) {
        this.trainingDate = trainingDate;
    }

    public Integer getTrainingDuration() {
        return trainingDuration;
    }

    public void setTrainingDuration(Integer trainingDuration) {
        this.trainingDuration = trainingDuration;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }
}
