package com.university.backend.services;

import com.university.backend.dto.AssignmentResponseDTO;
import com.university.backend.entity.Assignment.Assignment;
import com.university.backend.entity.Assignment.AssignmentAttributes;
import com.university.backend.entity.Assignment.AssignmentValue;
import com.university.backend.entity.Course;
import com.university.backend.entity.Professor;
import com.university.backend.repository.AssignmentRepository;
import com.university.backend.repository.CourseRepository;
import com.university.backend.repository.ProfessorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final ProfessorRepository professorRepository;

    @Autowired
    public AssignmentService(AssignmentRepository assignmentRepository,
            CourseRepository courseRepository,
            ProfessorRepository professorRepository) {
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
        this.professorRepository = professorRepository;
    }

    /**
     * FETCH: Retrieves the full structure (Static Columns + EAV) and flattens it.
     */
    @Transactional(readOnly = true)
    public AssignmentResponseDTO getAssignmentById(Long id) {
        // 1. Fetch optimized data
        Assignment assignment = assignmentRepository.findFullAssignmentById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found with ID: " + id));

        // 2. Initialize DTO
        AssignmentResponseDTO response = new AssignmentResponseDTO(assignment.getId());

        // 3. Map STATIC fields (From main table)
        response.addField("Title", assignment.getTitle());

        // Ensure you use the correct getter names for your Entities
        // e.g., getCourseCode() vs getCourseId() depending on your Course entity
        if (assignment.getCourse() != null) {
            response.addField("Course_Id", assignment.getCourse().getCourseCode());
        }
        if (assignment.getProfessor() != null) {
            response.addField("Professor_Id", assignment.getProfessor().getProfessorId());
        }

        // 4. Map DYNAMIC EAV fields (From attributes table)
        for (AssignmentValue val : assignment.getValues()) {
            String key = val.getAttribute().getAttributeName();
            Object value = extractValue(val);
            response.addField(key, value);
        }

        return response;
    }

    /**
     * CREATE: Handles both Static Columns and Dynamic Attributes from one JSON
     * payload.
     */
    @Transactional
    public AssignmentResponseDTO createAssignment(Map<String, Object> payload) {

        // 1. Extract and Validate Static Keys
        String title = (String) payload.get("Title");
        String courseId = (String) payload.get("Course_Id");
        Object profIdObj = payload.get("Professor_Id");

        if (title == null || courseId == null || profIdObj == null) {
            throw new RuntimeException("Missing required fields: Title, Course_Id, or Professor_Id");
        }

        // 2. Fetch Real Entities for Foreign Keys
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));

        // Handle Professor ID (Assuming it might come as String or Integer from JSON)
        String professorId = String.valueOf(profIdObj);
        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new RuntimeException("Professor not found: " + professorId));

        // 3. Create Assignment with Static Links
        Assignment assignment = new Assignment(title, course, professor);

        // 4. Save to generate IDs (triggers default attribute creation)
        assignment = assignmentRepository.save(assignment);

        // 5. Create Map for Attribute Lookup
        Map<String, AssignmentAttributes> attributeMap = assignment.getAttributes().stream()
                .collect(Collectors.toMap(AssignmentAttributes::getAttributeName, attr -> attr));

        // 6. Loop through Payload for DYNAMIC attributes
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Skip static keys we already processed
            if (key.equals("Title") || key.equals("Course_Id") || key.equals("Professor_Id")) {
                continue;
            }

            // Process matches or CREATE NEW ATTRIBUTE
            if (attributeMap.containsKey(key)) {
                AssignmentAttributes targetAttr = attributeMap.get(key);
                AssignmentValue newValue = createValueEntity(assignment, targetAttr, value);
                assignment.getValues().add(newValue);
            } else {
                // DYNAMICALLY CREATE NEW ATTRIBUTE
                // Defaulting to STRING for custom attributes for simplicity
                AssignmentAttributes newAttr = new AssignmentAttributes(assignment, key, "STRING");
                assignment.getAttributes().add(newAttr);

                // We need to save the attribute first or rely on Cascade.
                // Since mappedBy="assignment" and CascadeType.ALL, adding to list should work
                // if we save assignment.
                // However, createValueEntity needs the attribute entity.
                // Safe bet: The attribute object is connected to assignment.

                AssignmentValue newValue = createValueEntity(assignment, newAttr, value);
                assignment.getValues().add(newValue);
            }
        }

        // 7. Update with new values
        Assignment savedAssignment = assignmentRepository.save(assignment);

        System.out.println("DEBUG: ASSIGNMENT SAVED. ID: " + savedAssignment.getId());
        System.out.println("DEBUG: ATTRIBUTES COUNT: " + savedAssignment.getAttributes().size());
        System.out.println("DEBUG: VALUES COUNT: " + savedAssignment.getValues().size());

        return getAssignmentById(savedAssignment.getId());
    }

    /**
     * UPDATE: Updates Static Columns and Dynamic Attributes.
     */
    @Transactional
    public AssignmentResponseDTO updateAssignment(Long id, Map<String, Object> payload) {
        // 1. Fetch Existing Assignment
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found with ID: " + id));

        // 2. Update Static Fields if present
        if (payload.containsKey("Title")) {
            assignment.setTitle((String) payload.get("Title"));
            // Note: Updating Course/Professor is usually restricted, so we skip them here
            // unless needed
        }

        // 3. Update/Create Dynamic Attributes
        Map<String, AssignmentAttributes> attributeMap = assignment.getAttributes().stream()
                .collect(Collectors.toMap(AssignmentAttributes::getAttributeName, attr -> attr));

        // Create a map of existing VALUES for quick lookup (to update instead of
        // duplicate)
        // We map Attribute Name -> AssignmentValue entity
        Map<String, AssignmentValue> valueMap = assignment.getValues().stream()
                .collect(Collectors.toMap(v -> v.getAttribute().getAttributeName(), v -> v));

        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Skip static keys
            if (key.equals("Title") || key.equals("Course_Id") || key.equals("Professor_Id") || key.equals("id")) {
                continue;
            }

            // Check for match or CREATE NEW
            if (attributeMap.containsKey(key)) {
                AssignmentAttributes targetAttr = attributeMap.get(key);

                if (valueMap.containsKey(key)) {
                    // UPDATE existing value
                    AssignmentValue existingValue = valueMap.get(key);
                    updateValueEntity(existingValue, targetAttr, value);
                } else {
                    // CREATE new value for existing attribute
                    AssignmentValue newValue = createValueEntity(assignment, targetAttr, value);
                    assignment.getValues().add(newValue);
                    valueMap.put(key, newValue);
                }
            } else {
                // NEW CUSTOM ATTRIBUTE (during Update)
                AssignmentAttributes newAttr = new AssignmentAttributes(assignment, key, "STRING");
                assignment.getAttributes().add(newAttr);

                AssignmentValue newValue = createValueEntity(assignment, newAttr, value);
                assignment.getValues().add(newValue);

                // Update local maps just in case
                attributeMap.put(key, newAttr);
                valueMap.put(key, newValue);
            }
        }

        Assignment saved = assignmentRepository.save(assignment);
        return getAssignmentById(saved.getId());
    }

    /**
     * DELETE: Removes an assignment and all its EAV data (cascaded).
     */
    @Transactional
    public void deleteAssignment(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new RuntimeException("Assignment not found with ID: " + id);
        }
        assignmentRepository.deleteById(id);
    }

    // --- HELPER METHODS ---

    private Object extractValue(AssignmentValue val) {
        if (val.getValString() != null)
            return val.getValString();
        if (val.getValInt() != null)
            return val.getValInt();
        if (val.getValDouble() != null)
            return val.getValDouble();
        if (val.getValBool() != null)
            return val.getValBool();
        if (val.getValDate() != null)
            return val.getValDate();
        return null;
    }

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
                    String dateStr = String.valueOf(value);
                    try {
                        // Try standard HTML5 datetime-local format first (yyyy-MM-dd'T'HH:mm)
                        SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                        valEntity.setValDate(timestampFormat.parse(dateStr));
                    } catch (ParseException e1) {
                        try {
                            // Fallback to simple date (yyyy-MM-dd)
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            valEntity.setValDate(dateFormat.parse(dateStr));
                        } catch (ParseException e2) {
                            // Last resort: Try parsing standard JS Date.toString() or generic formats if
                            // needed
                            throw new RuntimeException("Invalid Date Format for " + attr.getAttributeName() + ": "
                                    + dateStr + ". Expected yyyy-MM-dd'T'HH:mm or yyyy-MM-dd.");
                        }
                    }
                    break;
                default:
                    valEntity.setValString(String.valueOf(value));
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException(
                    "Error parsing value for attribute " + attr.getAttributeName() + ": " + e.getMessage());
        }
        return valEntity;
    }

    private void updateValueEntity(AssignmentValue valEntity, AssignmentAttributes attr, Object value) {
        // Reuse the setting logic. For simplicity, we can just call the setters
        // directly matching the type.
        // Or refactor createValueEntity to use this.
        // Let's copy the switch logic for now to be safe.

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
                    String dateStr = String.valueOf(value);
                    try {
                        SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                        valEntity.setValDate(timestampFormat.parse(dateStr));
                    } catch (ParseException e1) {
                        try {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            valEntity.setValDate(dateFormat.parse(dateStr));
                        } catch (ParseException e2) {
                            throw new RuntimeException("Invalid Date Format: " + dateStr);
                        }
                    }
                    break;
                default:
                    valEntity.setValString(String.valueOf(value));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error updating value: " + e.getMessage());
        }
    }
}