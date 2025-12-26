package com.university.backend.services;

import com.university.backend.dto.AssignmentResponseDTO;
import com.university.backend.dto.AssignmentSubmissionResponseDTO;
import com.university.backend.entity.Assignment.Assignment;
import com.university.backend.entity.Assignment.AssignmentAttributes;
import com.university.backend.entity.Assignment.AssignmentValue;
import com.university.backend.entity.Course;
import com.university.backend.entity.CourseGradingItem; // <--- Import this
import com.university.backend.entity.Professor;
import com.university.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final ProfessorRepository professorRepository;
    private final CourseGradingItemRepository gradingItemRepository; // <--- Dependency
    private final AssignmentSubmissionService submissionService;
    @Autowired
    public AssignmentService(AssignmentRepository assignmentRepository,
                             CourseRepository courseRepository,
                             ProfessorRepository professorRepository,
                             CourseGradingItemRepository gradingItemRepository, AssignmentSubmissionService submissionService) { // <--- Inject here
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
        this.professorRepository = professorRepository;
        this.gradingItemRepository = gradingItemRepository;
        this.submissionService = submissionService;
    }

    /**
     * LIST VIEW: Fetches all assignments for a specific course.
     */
    @Transactional(readOnly = true)
    public List<AssignmentResponseDTO> getAssignmentsByCourse(String courseCode) {
        List<Assignment> assignments = assignmentRepository.findAllByCourseCode(courseCode);

        return assignments.stream().map(assignment -> {
            AssignmentResponseDTO dto = new AssignmentResponseDTO(assignment.getId());
            dto.addField("Title", assignment.getTitle());
            dto.addField("Course_Id", assignment.getCourse().getCourseCode());

            // Helpful for frontend to see which bucket this belongs to
            if (assignment.getGradingItem() != null) {
                dto.addField("Grading_Category", assignment.getGradingItem().getCategoryName());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * FETCH: Retrieves the full structure.
     */
    @Transactional(readOnly = true)
    public AssignmentResponseDTO getAssignmentById(Long id) {
        // 1. Use the CUSTOM QUERY
        Assignment assignment = assignmentRepository.findFullAssignmentById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found with ID: " + id));

        AssignmentResponseDTO response = new AssignmentResponseDTO(assignment.getId());

        // 2. Static Fields
        response.addField("Title", assignment.getTitle());
        if (assignment.getCourse() != null) {
            response.addField("Course_Id", assignment.getCourse().getCourseCode());
        }
        if (assignment.getProfessor() != null) {
            response.addField("Professor_Id", assignment.getProfessor().getProfessorId());
        }
        if (assignment.getGradingItem() != null) {
            response.addField("Grading_Item_Id", assignment.getGradingItem().getId());
            response.addField("Grading_Category_Name", assignment.getGradingItem().getCategoryName());
        }

        // 3. Dynamic EAV Fields
        // Because of the Repository fix, 'val.getAttribute()' will never be null/proxy.
        for (AssignmentValue val : assignment.getValues()) {
            if (val.getAttribute() != null) { // Safety check
                String key = val.getAttribute().getAttributeName();
                Object value = extractValue(val);
                response.addField(key, value);
            }
        }

        return response;
    }
    /**
     * CREATE: Handles Static Columns + Grading Item + Dynamic Attributes
     */
    @Transactional
    public AssignmentResponseDTO createAssignment(Map<String, Object> payload) {

        // 1. Extract Static Keys
        String title = (String) payload.get("Title");
        String courseId = (String) payload.get("Course_Id");
        Object profIdObj = payload.get("Professor_Id");
        Object gradingItemIdObj = payload.get("Grading_Item_Id");

        if (title == null || courseId == null || profIdObj == null) {
            throw new RuntimeException("Missing required fields: Title, Course_Id, or Professor_Id");
        }

        // 2. Fetch Entities
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));

        String professorId = String.valueOf(profIdObj);
        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new RuntimeException("Professor not found: " + professorId));

        // 3. Create Assignment (Constructor creates defaults like Description, Max_Grade)
        Assignment assignment = new Assignment(title, course, professor);

        // 4. LINK GRADING ITEM
        if (gradingItemIdObj != null) {
            Long gradingItemId = Long.parseLong(String.valueOf(gradingItemIdObj));
            CourseGradingItem gradingItem = gradingItemRepository.findById(gradingItemId)
                    .orElseThrow(() -> new RuntimeException("Grading Item not found: " + gradingItemId));

            assignment.setGradingItem(gradingItem);
        }

        // 5. Save (Generates ID and persists default attributes)
        assignment = assignmentRepository.save(assignment);

        // 6. Map Attributes for fast lookup
        Map<String, AssignmentAttributes> attributeMap = assignment.getAttributes().stream()
                .collect(Collectors.toMap(AssignmentAttributes::getAttributeName, attr -> attr));

        // 7. Loop payload for Dynamic Attributes
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Skip static keys
            if (key.equals("Title") || key.equals("Course_Id") ||
                    key.equals("Professor_Id") || key.equals("Grading_Item_Id")) {
                continue;
            }

            AssignmentAttributes targetAttr;

            // CHECK: Does this attribute definition exist?
            if (attributeMap.containsKey(key)) {
                // A. Yes -> Use existing definition
                targetAttr = attributeMap.get(key);
            } else {
                // B. No -> CREATE NEW ATTRIBUTE DEFINITION ON THE FLY
                // This handles custom questions or fields the professor adds
                String inferredType = inferDataType(value);

                targetAttr = new AssignmentAttributes();
                targetAttr.setAssignment(assignment);
                targetAttr.setAttributeName(key);
                targetAttr.setDataType(inferredType);

                // Add to list and map so we can use it immediately
                assignment.getAttributes().add(targetAttr);
                attributeMap.put(key, targetAttr);
            }

            // 8. Create and Save the Value
            // Now we are guaranteed to have a targetAttr (either old or new)
            AssignmentValue newValue = createValueEntity(assignment, targetAttr, value);
            assignment.getValues().add(newValue);
        }

        // 9. Final Save
        assignmentRepository.save(assignment);

        return getAssignmentById(assignment.getId());
    }

    // ==================================================================================
    // 4. UPDATE ASSIGNMENT (Full EAV Support + Grading Bucket)
    // ==================================================================================
    @Transactional
    public AssignmentResponseDTO updateAssignment(Long id, Map<String, Object> payload) {
        // 1. Fetch Assignment
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found with ID: " + id));

        // 2. Update Static Fields
        // A. Title
        if (payload.containsKey("Title")) {
            assignment.setTitle((String) payload.get("Title"));
        }

        // B. Grading Bucket (NEW: Moving assignment to a different bucket, e.g. Labs -> Project)
        if (payload.containsKey("Grading_Item_Id")) {
            Object gItemId = payload.get("Grading_Item_Id");
            if (gItemId != null) {
                Long newItemId = Long.parseLong(String.valueOf(gItemId));
                CourseGradingItem newItem = gradingItemRepository.findById(newItemId)
                        .orElseThrow(() -> new RuntimeException("Grading Item not found with ID: " + newItemId));
                assignment.setGradingItem(newItem);
            } else {
                assignment.setGradingItem(null); // Allow un-assigning
            }
        }

        // 3. Update DYNAMIC Fields (Description, Max_Grade, and NEW Professor Attributes)

        // Map Existing Values (Key -> Entity)
        Map<String, AssignmentValue> currentValuesMap = assignment.getValues().stream()
                .filter(val -> val.getAttribute() != null)
                .collect(Collectors.toMap(
                        val -> val.getAttribute().getAttributeName(),
                        val -> val
                ));

        // Map Existing Attribute Definitions
        Map<String, AssignmentAttributes> attributesMap = assignment.getAttributes().stream()
                .collect(Collectors.toMap(AssignmentAttributes::getAttributeName, attr -> attr));

        // Iterate Payload
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String key = entry.getKey();
            Object newValue = entry.getValue();

            // Skip Restricted Static Keys
            if (key.equals("id") || key.equals("Title") ||
                    key.equals("Course_Id") || key.equals("Professor_Id") ||
                    key.equals("Grading_Item_Id")) { // Skip because we handled it above
                continue;
            }

            // CHECK: Does this attribute definition exist?
            AssignmentAttributes targetAttr;
            if (attributesMap.containsKey(key)) {
                // Yes -> Get it
                targetAttr = attributesMap.get(key);
            } else {
                // No -> PROFESSOR ADDED A NEW FIELD (e.g., "Extra_Link")
                // We must create the Attribute Definition on the fly
                String inferredType = inferDataType(newValue);
                targetAttr = new AssignmentAttributes(); // Assuming default constructor
                targetAttr.setAssignment(assignment);
                targetAttr.setAttributeName(key);
                targetAttr.setDataType(inferredType);

                assignment.getAttributes().add(targetAttr);
                attributesMap.put(key, targetAttr); // Add to map so we don't recreate it inside loop
            }

            // Now Update/Create the Value for this Attribute
            if (currentValuesMap.containsKey(key)) {
                // Update Existing Value
                AssignmentValue existingVal = currentValuesMap.get(key);
                updateAssignmentValue(existingVal, newValue, targetAttr.getDataType());
            } else {
                // Create New Value
                AssignmentValue newVal = createValueEntity(assignment, targetAttr, newValue);
                assignment.getValues().add(newVal);
            }
        }

        assignmentRepository.save(assignment);
        return getAssignmentById(id);
    }

    @Transactional
    public void deleteAssignment(Long id) {
        // 1. Fetch the Entity first (We need access to the lists)
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found with ID: " + id));

        // 2. DELETE ALL SUBMISSIONS FIRST
        // We reuse the robust logic from SubmissionService.
        // This ensures student answers (Values) are deleted before their attributes.
        List<AssignmentSubmissionResponseDTO> submissions = submissionService.getSubmissionsByAssignment(id);
        for (AssignmentSubmissionResponseDTO sub : submissions) {
            submissionService.deleteSubmission(sub.getId());
        }
        // We must remove the "Assignment Values" (e.g., Description text)
        assignment.getValues().clear();
        // Force the database to delete these values NOW.
        assignmentRepository.saveAndFlush(assignment);
        // Now it is safe to delete the Assignment (and its Attributes)
        assignmentRepository.delete(assignment);
    }

    // --- HELPER: Infer Data Type for new Attributes ---
    private String inferDataType(Object value) {
        if (value instanceof Integer) return "INTEGER";
        if (value instanceof Double) return "DOUBLE";
        if (value instanceof Boolean) return "BOOLEAN";
        // Simple heuristic for dates (optional)
        if (value.toString().matches("\\d{4}-\\d{2}-\\d{2}")) return "DATE";
        return "STRING";
    }

    // --- HELPER: Assignment Value Update ---
    private void updateAssignmentValue(AssignmentValue valEntity, Object value, String type) {
        valEntity.setValString(null); valEntity.setValInt(null);
        valEntity.setValDouble(null); valEntity.setValBool(null); valEntity.setValDate(null);

        try {
            switch (type.toUpperCase()) {
                case "STRING": valEntity.setValString(String.valueOf(value)); break;
                case "INTEGER": case "INT": valEntity.setValInt(Integer.parseInt(String.valueOf(value))); break;
                case "DOUBLE": valEntity.setValDouble(Double.parseDouble(String.valueOf(value))); break;
                case "BOOLEAN": valEntity.setValBool(Boolean.parseBoolean(String.valueOf(value))); break;
                case "DATE":
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    valEntity.setValDate(sdf.parse(String.valueOf(value)));
                    break;
                default: valEntity.setValString(String.valueOf(value));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error updating value: " + e.getMessage());
        }
    }

    // --- HELPER METHODS ---
    // (Keep existing extractValue and createValueEntity methods exactly as they were)
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