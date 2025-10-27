package com.universitymanagement.model.eav;

import jakarta.persistence.*;
import com.universitymanagement.model.BaseEntity;
import java.util.List;

@Entity
public class EntityRecord extends BaseEntity {

    private String entityType; // e.g., "Student", "Course", "Hall"
    private String referenceId; // External ID for domain link (like Student ID)

    @OneToMany(mappedBy = "entityRecord", cascade = CascadeType.ALL)
    private List<AttributeValue> attributes;

    public EntityRecord() {}

    public EntityRecord(String entityType, String referenceId) {
        this.entityType = entityType;
        this.referenceId = referenceId;
    }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public List<AttributeValue> getAttributes() { return attributes; }
    public void setAttributes(List<AttributeValue> attributes) { this.attributes = attributes; }
}
