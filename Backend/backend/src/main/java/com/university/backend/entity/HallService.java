package com.universitymanagement.service.facility;

import com.universitymanagement.model.eav.*;
import com.universitymanagement.repository.eav.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class HallService {

    private final EntityRecordRepository entityRecordRepository;
    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final AttributeValueRepository attributeValueRepository;

    public HallService(EntityRecordRepository entityRecordRepository,
                       AttributeDefinitionRepository attributeDefinitionRepository,
                       AttributeValueRepository attributeValueRepository) {
        this.entityRecordRepository = entityRecordRepository;
        this.attributeDefinitionRepository = attributeDefinitionRepository;
        this.attributeValueRepository = attributeValueRepository;
    }

    @Transactional
    public EntityRecord addHall(String name, String location, int capacity, String type) {
        EntityRecord hall = new EntityRecord("Hall", name);
        hall = entityRecordRepository.save(hall);

        saveAttribute(hall, "name", name);
        saveAttribute(hall, "location", location);
        saveAttribute(hall, "capacity", String.valueOf(capacity));
        saveAttribute(hall, "type", type);

        return hall;
    }

    private void saveAttribute(EntityRecord entity, String name, String value) {
        AttributeDefinition attr = attributeDefinitionRepository.findByName(name);
        if (attr == null) {
            attr = new AttributeDefinition(name, "STRING");
            attributeDefinitionRepository.save(attr);
        }
        AttributeValue val = new AttributeValue(entity, attr, value);
        attributeValueRepository.save(val);
    }

    public void deleteHall(Long hallId) {
        entityRecordRepository.deleteById(hallId);
    }

    public List<EntityRecord> listHalls() {
        return entityRecordRepository.findByEntityType("Hall");
    }
}
