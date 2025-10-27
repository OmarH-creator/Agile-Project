package com.universitymanagement.model.user;

import jakarta.persistence.Entity;
import com.universitymanagement.model.BaseEntity;

@Entity
public class Admin extends User {

    public Admin() {}

    public Admin(String firstName, String lastName, String email, String password) {
        super(firstName, lastName, email, password, "ADMIN");
    }

    // Admin can later have audit logs or assigned permissions
}
