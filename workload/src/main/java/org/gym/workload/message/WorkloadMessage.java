package org.gym.workload.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.sql.Date;

public class WorkloadMessage {
    @NotBlank(message = "Username is mandatory")
    private String trainerUsername;

    @NotBlank(message = "Firstname is mandatory")
    private String trainerFirstName;

    @NotBlank(message = "Lastname is mandatory")
    private String trainerLastName;

    private boolean active;

    @NotNull
    private Date trainingDate;

    @NotNull
    private Integer trainingDuration;

    @NotNull// Duration in hours
    private ActionType actionType;

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
