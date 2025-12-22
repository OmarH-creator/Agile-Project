package com.university.backend.service;

import com.university.backend.dto.AssignmentResponseDTO; // Assumes you created this DTO from the previous step
import com.university.backend.entity.Assignment;
import com.university.backend.entity.AssignmentAttributes;
import com.university.backend.entity.AssignmentValue;
import com.university.backend.repository.AssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;

    @Autowired
    public AssignmentService(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    /**
     * FETCH: Retrieves the full EAV structure and flattens it into a simple JSON DTO.
     */
    @Transactional(readOnly = true)
    public AssignmentResponseDTO getAssignmentById(Long id) {
        // 1. Fetch optimized data using the custom Repository Query
        Assignment assignment = assignmentRepository.findFullAssignmentById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found with ID: " + id));

        // 2. Initialize the clean DTO
        AssignmentResponseDTO response = new AssignmentResponseDTO(assignment.getId());

        // 3. Map the sparse 'Value' rows into the flat DTO map
        for (AssignmentValue val : assignment.getValues()) {
            String key = val.getAttribute().getAttributeName();
            Object value = extractValue(val); // Helper method below
            response.addField(key, value);
        }

        return response;
    }

    /**
     * CREATE: Accepts a flat Map (JSON) and distributes data into the EAV tables.
     * Example Input: { "Title": "Math 101", "Deadline": "2023-12-01" }
     */
    @Transactional
    public AssignmentResponseDTO createAssignment(Map<String, Object> payload) {
        // 1. Create new Entity (Constructor automatically generates the empty Attribute rows)
        Assignment assignment = new Assignment();

        // 2. Save immediately to generate IDs for the Assignment and its Attributes
        assignment = assignmentRepository.save(assignment);

        // 3. Create a quick lookup map for attributes: "Title" -> AttributeEntity
        // This prevents us from looping inside a loop.
        Map<String, AssignmentAttributes> attributeMap = assignment.getAttributes().stream()
                .collect(Collectors.toMap(AssignmentAttributes::getAttributeName, attr -> attr));

        // 4. Iterate over the incoming JSON payload
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String inputKey = entry.getKey();      // e.g., "Title"
            Object inputValue = entry.getValue();  // e.g., "Math HW"

            // Check if this Assignment actually defines this attribute
            if (attributeMap.containsKey(inputKey)) {
                AssignmentAttributes targetAttr = attributeMap.get(inputKey);

                // Create the Value row linking Assignment + Attribute + Data
                AssignmentValue newValue = createValueEntity(assignment, targetAttr, inputValue);
                assignment.getValues().add(newValue);
            } else {
                // Optional: Log warning if user sends data that isn't defined in the constructor
                System.out.println("Warning: Attribute '" + inputKey + "' is not defined for Assignments.");
            }
        }

        // 5. Update the assignment with the new values attached
        assignmentRepository.save(assignment);

        // 6. Return the formatted DTO
        return getAssignmentById(assignment.getId());
    }

    // --- HELPER METHODS ---

    /**
     * Extracts the correct non-null value from the sparse columns.
     */
    private Object extractValue(AssignmentValue val) {
        if (val.getValString() != null) return val.getValString();
        if (val.getValInt() != null) return val.getValInt();
        if (val.getValDouble() != null) return val.getValDouble();
        if (val.getValBool() != null) return val.getValBool();
        if (val.getValDate() != null) return val.getValDate();
        return null;
    }

    /**
     * Creates a Value entity and populates the correct column based on input type.
     */
    private AssignmentValue createValueEntity(Assignment asm, AssignmentAttributes attr, Object value) {
        AssignmentValue valEntity = new AssignmentValue();
        valEntity.setAssignment(asm);
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
                    // Assumes date comes in as String. You might need a formatter.
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