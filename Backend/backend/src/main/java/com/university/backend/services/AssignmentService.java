package com.university.backend.services;

import com.university.backend.dto.AssignmentResponseDTO;
import com.university.backend.entity.*;
import com.university.backend.repository.AssignmentRepository;
import com.university.backend.repository.CourseRepository;
import com.university.backend.repository.ProfessorRepository; // Or UserRepository
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
     * CREATE: Handles both Static Columns and Dynamic Attributes from one JSON payload.
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

            // Process matches
            if (attributeMap.containsKey(key)) {
                AssignmentAttributes targetAttr = attributeMap.get(key);
                AssignmentValue newValue = createValueEntity(assignment, targetAttr, value);
                assignment.getValues().add(newValue);
            }
        }

        // 7. Update with new values
        assignmentRepository.save(assignment);

        return getAssignmentById(assignment.getId());
    }

    // --- HELPER METHODS ---

    private Object extractValue(AssignmentValue val) {
        if (val.getValString() != null) return val.getValString();
        if (val.getValInt() != null) return val.getValInt();
        if (val.getValDouble() != null) return val.getValDouble();
        if (val.getValBool() != null) return val.getValBool();
        if (val.getValDate() != null) return val.getValDate();
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