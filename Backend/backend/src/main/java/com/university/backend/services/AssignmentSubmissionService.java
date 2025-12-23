package com.university.backend.services;

import com.university.backend.dto.AssignmentSubmissionResponseDTO;
import com.university.backend.entity.Assignment.Assignment;
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

    /**
     * FETCH: Retrieves the full structure (Static Columns + EAV) and flattens it.
     */
    @Transactional(readOnly = true)
    public AssignmentSubmissionResponseDTO getSubmissionById(Long id) {
        // 1. Fetch optimized data
        AssignmentSubmission submission = submissionRepository.findFullSubmissionById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found with ID: " + id));

        // 2. Initialize DTO
        AssignmentSubmissionResponseDTO response = new AssignmentSubmissionResponseDTO(submission.getId());

        // 3. Map STATIC fields (From main table)
        if (submission.getAssignment() != null) {
            response.addField("Assignment_Id", submission.getAssignment().getId());
            response.addField("Assignment_Title", submission.getAssignment().getTitle());
        }
        if (submission.getStudent() != null) {
            response.addField("Student_Id", submission.getStudent().getStudentId());
        }

        // 4. Map DYNAMIC EAV fields (From attributes table)
        for (SubmissionValue val : submission.getValues()) {
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
    public AssignmentSubmissionResponseDTO createSubmission(Map<String, Object> payload) {

        // 1. Extract and Validate Static Keys
        Object assignmentIdObj = payload.get("Assignment_Id");
        String studentId = (String) payload.get("Student_Id");

        if (assignmentIdObj == null || studentId == null) {
            throw new RuntimeException("Missing required fields: Assignment_Id or Student_Id");
        }

        // 2. Fetch Real Entities for Foreign Keys
        Long assignmentId = Long.parseLong(String.valueOf(assignmentIdObj));
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

        // 3. Create Submission with Static Links
        AssignmentSubmission submission = new AssignmentSubmission(assignment, student);

        // 4. Save to generate IDs (triggers default attribute creation)
        submission = submissionRepository.save(submission);

        // 5. Create Map for Attribute Lookup
        Map<String, SubmissionAttributes> attributeMap = submission.getAttributes().stream()
                .collect(Collectors.toMap(SubmissionAttributes::getAttributeName, attr -> attr));

        // 6. Loop through Payload for DYNAMIC attributes
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Skip static keys we already processed
            if (key.equals("Assignment_Id") || key.equals("Student_Id")) {
                continue;
            }

            // Process matches
            if (attributeMap.containsKey(key)) {
                SubmissionAttributes targetAttr = attributeMap.get(key);
                SubmissionValue newValue = createValueEntity(submission, targetAttr, value);
                submission.getValues().add(newValue);
            }
        }

        // 7. Update with new values
        submissionRepository.save(submission);

        return getSubmissionById(submission.getId());
    }

    // --- HELPER METHODS ---

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