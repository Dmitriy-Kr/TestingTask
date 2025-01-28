package org.gym.workload.entity;

public class Year {

    private int yearNumber;

    private int[] durationByMonths;

    public Year() {
        durationByMonths = new int[12];
    }

    public Year(int yearNumber, int[] durationByMonths) {
        if(durationByMonths.length != 12){
            throw new IllegalArgumentException("Invalid array length. Must be 12.");
        }
        this.yearNumber = yearNumber;
        this.durationByMonths = durationByMonths;
    }
    public int getYearNumber() {
        return yearNumber;
    }

    public void setYearNumber(int yearNumber) {
        this.yearNumber = yearNumber;
    }

    public int[] getDurationByMonths() {
        return durationByMonths;
    }

    public void setDurationByMonths(int[] durationByMonths) {
        if(durationByMonths.length != 12){
            throw new IllegalArgumentException("Invalid array length. Must be 12.");
        }
        this.durationByMonths = durationByMonths;
    }
}
