package com.university.backend.services;

import com.university.backend.dto.HallResponseDTO;
import com.university.backend.entity.Hall.Hall;
import com.university.backend.entity.Hall.HallAttribute;
import com.university.backend.entity.Hall.HallValue;
import com.university.backend.repository.HallRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HallService {

    private final HallRepository hallRepository;

    @Autowired
    public HallService(HallRepository hallRepository) {
        this.hallRepository = hallRepository;
    }

    /**
     * FETCH: Retrieves the full structure (Static Columns + EAV) and flattens it.
     */
    @Transactional(readOnly = true)
    public HallResponseDTO getHallById(Long id) {
        // 1. Fetch optimized data
        Hall hall = hallRepository.findFullHallById(id)
                .orElseThrow(() -> new RuntimeException("Hall not found with ID: " + id));

        // 2. Initialize DTO
        HallResponseDTO response = new HallResponseDTO(hall.getId());

        // 3. Map STATIC fields (From main table)
        response.addField("Hall_Name", hall.getHallName());

        // 4. Map DYNAMIC EAV fields (From attributes table)
        for (HallValue val : hall.getValues()) {
            String key = val.getAttribute().getAttributeName();
            Object value = extractValue(val);
            response.addField(key, value);
        }

        return response;
    }

    /**
     * CREATE: Handles both Static Columns and Dynamic Attributes from one JSON payload.
     */
    @Transactional
    public HallResponseDTO createHall(Map<String, Object> payload) {

        // 1. Extract and Validate Static Keys
        String hallName = (String) payload.get("Hall_Name");

        if (hallName == null) {
            throw new RuntimeException("Missing required field: Hall_Name");
        }

        // 2. Create Hall with Static Links
        Hall hall = new Hall(hallName);

        // 3. Save to generate IDs (triggers default attribute creation)
        hall = hallRepository.save(hall);

        // 4. Create Map for Attribute Lookup
        Map<String, HallAttribute> attributeMap = hall.getAttributes().stream()
                .collect(Collectors.toMap(HallAttribute::getAttributeName, attr -> attr));

        // 5. Loop through Payload for DYNAMIC attributes
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Skip static keys we already processed
            if (key.equals("Hall_Name")) {
                continue;
            }

            // Process matches
            if (attributeMap.containsKey(key)) {
                HallAttribute targetAttr = attributeMap.get(key);
                HallValue newValue = createValueEntity(hall, targetAttr, value);
                hall.getValues().add(newValue);
            }
        }

        // 6. Update with new values
        hallRepository.save(hall);

        return getHallById(hall.getId());
    }

    // --- HELPER METHODS ---

    private Object extractValue(HallValue val) {
        if (val.getValString() != null) return val.getValString();
        if (val.getValInt() != null) return val.getValInt();
        if (val.getValDouble() != null) return val.getValDouble();
        if (val.getValBool() != null) return val.getValBool();
        if (val.getValDate() != null) return val.getValDate();
        return null;
    }

    private HallValue createValueEntity(Hall hall, HallAttribute attr, Object value) {
        HallValue valEntity = new HallValue();
        valEntity.setHall(hall);
        valEntity.setAttribute(attr);

        String type = attr.getDataType().toUpperCase();

        try {
            switch (type) {
                case "STRING":
                    valEntity.setValString(String.valueOf(value));
                    break;
                case "INTEGER":
                case "INT":
                    valEntity.setValInt(Integer.parseInt(String.valueOf(value)));
                    break;
                case "DOUBLE":
                    valEntity.setValDouble(Double.parseDouble(String.valueOf(value)));
                    break;
                case "BOOLEAN":
                    valEntity.setValBool(Boolean.parseBoolean(String.valueOf(value)));
                    break;
                case "DATE":
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    valEntity.setValDate(sdf.parse(String.valueOf(value)));
                    break;
                default:
                    valEntity.setValString(String.valueOf(value));
            }
        } catch (ParseException | NumberFormatException e) {
            throw new RuntimeException("Error parsing value for attribute " + attr.getAttributeName() + ": " + e.getMessage());
        }
        return valEntity;
    }
}