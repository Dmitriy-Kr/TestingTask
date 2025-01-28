package org.gym.workload.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection =  "workloads")
@CompoundIndex(name = "name_idx", def = "{'firstname': 1, 'lastname': 1}")
public class Trainer {
    @Id
    private String id;


    private String username;

    private String firstname;

    private String lastname;

    private Boolean status;

    private List<Year> years;

    public Trainer() {
    }

    public Trainer(String username, String firstname, String lastname, Boolean status, List<Year> years) {
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.status = status;
        this.years = years;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public List<Year> getYears() {
        return years;
    }

    public void setYears(List<Year> years) {
        this.years = years;
    }
}
