package com.universitymanagement.model.eav;

import jakarta.persistence.*;
import com.universitymanagement.model.BaseEntity;

@Entity
public class AttributeValue extends BaseEntity {

    @ManyToOne
    private EntityRecord entityRecord;

    @ManyToOne
    private AttributeDefinition attributeDefinition;

    private String value;

    public AttributeValue() {}

    public AttributeValue(EntityRecord entityRecord, AttributeDefinition attributeDefinition, String value) {
        this.entityRecord = entityRecord;
        this.attributeDefinition = attributeDefinition;
        this.value = value;
    }

    public EntityRecord getEntityRecord() { return entityRecord; }
    public void setEntityRecord(EntityRecord entityRecord) { this.entityRecord = entityRecord; }

    public AttributeDefinition getAttributeDefinition() { return attributeDefinition; }
    public void setAttributeDefinition(AttributeDefinition attributeDefinition) { this.attributeDefinition = attributeDefinition; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
