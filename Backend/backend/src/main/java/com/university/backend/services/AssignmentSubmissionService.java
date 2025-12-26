package com.university.backend.services;

import com.university.backend.dto.AssignmentSubmissionResponseDTO;
import com.university.backend.entity.Assignment.Assignment;
import com.university.backend.entity.Assignment.AssignmentAttributes;
import com.university.backend.entity.AssignmentSubmissions.AssignmentSubmission;
import com.university.backend.entity.AssignmentSubmissions.SubmissionAttributes;
import com.university.backend.entity.AssignmentSubmissions.SubmissionValue;
import com.university.backend.entity.Student;
import com.university.backend.repository.AssignmentRepository;
import com.university.backend.repository.AssignmentSubmissionRepository;
import com.university.backend.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssignmentSubmissionService {

    private final AssignmentSubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final StudentRepository studentRepository;

    @Autowired
    public AssignmentSubmissionService(AssignmentSubmissionRepository submissionRepository,
                                       AssignmentRepository assignmentRepository,
                                       StudentRepository studentRepository) {
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.studentRepository = studentRepository;
    }

    // 1. FETCH (GET)
    @Transactional(readOnly = true)
    public AssignmentSubmissionResponseDTO getSubmissionById(Long id) {
        AssignmentSubmission submission = submissionRepository.findFullSubmissionById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found with ID: " + id));

        AssignmentSubmissionResponseDTO response = new AssignmentSubmissionResponseDTO(submission.getId());

        // 1. Static Links (Assignment & Student)
        if (submission.getAssignment() != null) {
            response.addField("Assignment_Id", submission.getAssignment().getId());
            response.addField("Assignment_Title", submission.getAssignment().getTitle());
        }
        if (submission.getStudent() != null) {
            response.addField("Student_Id", submission.getStudent().getStudentId());
        }

        // 2. All Data is EAV (Content, Grade, Custom Answers)
        for (SubmissionValue val : submission.getValues()) {
            if (val.getAttribute() != null) {
                String key = val.getAttribute().getAttributeName();
                Object value = extractValue(val);
                response.addField(key, value);
            }
        }

        return response;
    }

    // 2. CREATE (POST)
    @Transactional
    public AssignmentSubmissionResponseDTO createSubmission(Map<String, Object> payload) {

        // 1. Extract IDs
        Object assignmentIdObj = payload.get("Assignment_Id");
        String studentId = (String) payload.get("Student_Id");

        if (assignmentIdObj == null || studentId == null) {
            throw new RuntimeException("Missing required fields: Assignment_Id or Student_Id");
        }

        Long assignmentId = Long.parseLong(String.valueOf(assignmentIdObj));

        // 2. Fetch or Create Submission
        AssignmentSubmission submission = submissionRepository
                .findByAssignment_IdAndStudent_StudentId(assignmentId, studentId)
                .orElse(null);

        if (submission == null) {
            Assignment assignment = assignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("Assignment not found"));
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            // This Constructor adds "Content", "Grade", "Feedback", "Submission_Date" to 'attributes' list
            submission = new AssignmentSubmission(assignment, student);
        }

        // 3. Map Attributes for easy lookup
        Map<String, SubmissionAttributes> existingAttrs = submission.getAttributes().stream()
                .collect(Collectors.toMap(SubmissionAttributes::getAttributeName, attr -> attr));

        Map<String, AssignmentAttributes> allowedCustomQuestions = submission.getAssignment().getAttributes().stream()
                .collect(Collectors.toMap(AssignmentAttributes::getAttributeName, attr -> attr));

        // 4. Iterate Payload
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Skip ID keys
            if (key.equals("Assignment_Id") || key.equals("Student_Id")) continue;

            SubmissionAttributes targetAttr = null;

            // CASE A: It is a Default Attribute (Content, Grade, Feedback, Date)
            if (existingAttrs.containsKey(key)) {
                targetAttr = existingAttrs.get(key);
            }
            // CASE B: It is a Custom Question (from Assignment)
            else if (allowedCustomQuestions.containsKey(key)) {
                AssignmentAttributes def = allowedCustomQuestions.get(key);
                targetAttr = new SubmissionAttributes(submission, key, def.getDataType());
                submission.getAttributes().add(targetAttr);
                existingAttrs.put(key, targetAttr);
            }

            // 5. If valid, Save Value
            if (targetAttr != null) {
                SubmissionValue valEntity = createValueEntity(submission, targetAttr, value);
                submission.getValues().add(valEntity);
            }
        }

        submissionRepository.save(submission);
        return getSubmissionById(submission.getId());
    }

    // 3. UPDATE (PUT)
    @Transactional
    public AssignmentSubmissionResponseDTO updateSubmission(Long id, Map<String, Object> payload) {
        // 1. Fetch existing submission (Use Custom Query)
        AssignmentSubmission submission = submissionRepository.findFullSubmissionById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found with ID: " + id));

        // 2. Map EXISTING Values for O(1) lookup
        Map<String, SubmissionValue> currentValuesMap = submission.getValues().stream()
                .filter(val -> val.getAttribute() != null)
                .collect(Collectors.toMap(
                        val -> val.getAttribute().getAttributeName(),
                        val -> val,
                        (existing, replacement) -> existing // Safe fallback if duplicates exist
                ));

        // 3. Map VALID Attributes for this submission
        Map<String, SubmissionAttributes> validAttributesMap = submission.getAttributes().stream()
                .collect(Collectors.toMap(SubmissionAttributes::getAttributeName, attr -> attr));

        // 4. Iterate through payload updates
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String key = entry.getKey();
            Object newValue = entry.getValue();

            if (key.equals("id") || key.equals("Assignment_Id") || key.equals("Student_Id")) continue;

            if (validAttributesMap.containsKey(key)) {
                SubmissionAttributes targetAttr = validAttributesMap.get(key);

                if (currentValuesMap.containsKey(key)) {
                    // A. UPDATE EXISTING VALUE
                    SubmissionValue existingVal = currentValuesMap.get(key);
                    updateValueEntity(existingVal, newValue, targetAttr.getDataType());
                } else {
                    // B. INSERT NEW VALUE
                    SubmissionValue newVal = createValueEntity(submission, targetAttr, newValue);
                    submission.getValues().add(newVal);
                }
            }
        }

        submissionRepository.save(submission);
        return getSubmissionById(id);
    }

    // 4. DELETE (DELETE)
    @Transactional
    public void deleteSubmission(Long id) {
        // 1. Fetch the entity first (We need access to the lists)
        AssignmentSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission with ID " + id + " not found."));

        // clear values first
        submission.getValues().clear();

        // This ensures the "Values" table is clean before we touch the rest.
        submissionRepository.saveAndFlush(submission);

        // 4. Now it is safe to delete the Parent (Submission) + Attributes
        submissionRepository.delete(submission);
    }

    // ==================================================================================
    // 5. FILTERS
    // ==================================================================================

    @Transactional(readOnly = true)
    public List<AssignmentSubmissionResponseDTO> getSubmissionsByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignment_Id(assignmentId).stream()
                .map(sub -> getSubmissionById(sub.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AssignmentSubmissionResponseDTO getStudentSubmission(Long assignmentId, String studentId) {
        return submissionRepository.findByAssignment_IdAndStudent_StudentId(assignmentId, studentId)
                .map(sub -> getSubmissionById(sub.getId()))
                .orElse(null);
    }

    // ==================================================================================
    // HELPERS
    // ==================================================================================

    private Object extractValue(SubmissionValue val) {
        if (val.getValString() != null) return val.getValString();
        if (val.getValInt() != null) return val.getValInt();
        if (val.getValDouble() != null) return val.getValDouble();
        if (val.getValBool() != null) return val.getValBool();
        if (val.getValDate() != null) return val.getValDate();
        return null;
    }

    private SubmissionValue createValueEntity(AssignmentSubmission sub, SubmissionAttributes attr, Object value) {
        SubmissionValue valEntity = new SubmissionValue();
        valEntity.setSubmission(sub);
        valEntity.setAttribute(attr);

        String type = attr.getDataType().toUpperCase();
        try {
            setValueBasedOnType(valEntity, value, type);
        } catch (Exception e) {
            valEntity.setValString(String.valueOf(value)); // Fallback
        }
        return valEntity;
    }

    private void updateValueEntity(SubmissionValue valEntity, Object value, String type) {
        // Reset old values to avoid data type conflicts
        valEntity.setValString(null); valEntity.setValInt(null);
        valEntity.setValDouble(null); valEntity.setValBool(null); valEntity.setValDate(null);

        try {
            setValueBasedOnType(valEntity, value, type.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Error updating value: " + e.getMessage());
        }
    }

    // Shared logic to reduce code duplication
    private void setValueBasedOnType(SubmissionValue valEntity, Object value, String type) throws ParseException {
        switch (type) {
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
    }
}