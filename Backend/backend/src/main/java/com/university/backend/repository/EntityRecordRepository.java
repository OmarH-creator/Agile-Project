package com.universitymanagement.repository.eav;

import com.universitymanagement.model.eav.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EntityRecordRepository extends JpaRepository<EntityRecord, Long> {
    List<EntityRecord> findByEntityType(String entityType);
}

public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinition, Long> {
    AttributeDefinition findByName(String name);
}

public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {}