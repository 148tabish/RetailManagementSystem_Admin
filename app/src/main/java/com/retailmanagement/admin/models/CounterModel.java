package com.retailmanagement.admin.models;

public class CounterModel {
    String counterId, counterName, counterPass;

    public CounterModel(String counterId, String counterName, String counterPass) {
        this.counterId = counterId;
        this.counterName = counterName;
        this.counterPass = counterPass;
    }


    public String getCounterId() {
        return counterId;
    }

    public void setCounterId(String counterId) {
        this.counterId = counterId;
    }

    public String getCounterName() {
        return counterName;
    }

    public void setCounterName(String counterName) {
        this.counterName = counterName;
    }

    public String getCounterPass() {
        return counterPass;
    }

    public void setCounterPass(String counterPass) {
        this.counterPass = counterPass;
    }
}
