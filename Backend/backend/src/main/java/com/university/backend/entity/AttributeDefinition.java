package com.universitymanagement.model.eav;

import jakarta.persistence.*;
import com.universitymanagement.model.BaseEntity;

@Entity
public class AttributeDefinition extends BaseEntity {

    private String name; // e.g., "Department", "Grade", "Capacity"
    private String dataType; // e.g., "STRING", "INTEGER", "DOUBLE"

    public AttributeDefinition() {}

    public AttributeDefinition(String name, String dataType) {
        this.name = name;
        this.dataType = dataType;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
}
