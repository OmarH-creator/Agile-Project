package com.university.backend.services;

import com.university.backend.entity.AssignmentSubmissions.AssignmentSubmission;
import com.university.backend.entity.CourseGradingItem;
import com.university.backend.repository.AssignmentSubmissionRepository;
import com.university.backend.repository.CourseGradingItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GradingService {

    @Autowired
    private AssignmentSubmissionRepository submissionRepo;

    @Autowired
    private CourseGradingItemRepository gradingItemRepo;

    /**
     * CALCULATE: Computes the student's score for a specific bucket (e.g., "Labs").
     * Returns the weighted score (e.g., if Labs are 20%, student might get 18.5).
     */
    public double calculateBucketScore(String studentId, Long gradingItemId) {

        // 1. Get the Rule (The Bucket)
        CourseGradingItem bucket = gradingItemRepo.findById(gradingItemId)
                .orElseThrow(() -> new RuntimeException("Bucket not found"));

        // 2. Get the Work (The Submissions inside this bucket)
        List<AssignmentSubmission> submissions =
                submissionRepo.findByStudentAndGradingItem(studentId, gradingItemId);

        if (submissions.isEmpty()) {
            return 0.0;
        }

        // 3. Do the Math
        double totalEarned = 0.0;
        double totalPossible = 0.0;

        for (AssignmentSubmission sub : submissions) {

            // 1. EXTRACT STUDENT GRADE (From Submission Values)
            // We look for the value linked to the attribute named "Grade"
            Double studentScore = sub.getValues().stream()
                    .filter(val -> "Grade".equals(val.getAttribute().getAttributeName()))
                    .findFirst()
                    .map(val -> {
                        // Check where the value is stored (Double or Int)
                        if (val.getValDouble() != null) return val.getValDouble();
                        if (val.getValInt() != null) return val.getValInt().doubleValue();
                        return null; // Return null if value is empty
                    })
                    .orElse(null); // Result is null if attribute not found

            // 2. EXTRACT MAX GRADE (From Assignment Values)
            // We look for the value linked to the attribute named "Max_Grade"
            Double maxGrade = sub.getAssignment().getValues().stream()
                    .filter(val -> "Max_Grade".equals(val.getAttribute().getAttributeName()))
                    .findFirst()
                    .map(val -> {
                        if (val.getValInt() != null) return val.getValInt().doubleValue();
                        if (val.getValDouble() != null) return val.getValDouble();
                        return 0.0;
                    })
                    .orElse(0.0);

            // 3. DO THE MATH
            // Only count if the student has actually been graded (score is not null)
            if (studentScore != null && maxGrade > 0) {
                totalEarned += studentScore;
                totalPossible += maxGrade;
            }
        }

        if (totalPossible == 0) return 0.0;

        // 4. Calculate Weighted Result
        // Formula: (Earned / Possible) * Weight_Percentage
        double ratio = totalEarned / totalPossible;
        double weightedScore = ratio * bucket.getWeightPercentage();

        return weightedScore;
    }
}