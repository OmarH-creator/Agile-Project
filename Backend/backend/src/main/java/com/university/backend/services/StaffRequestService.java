package com.university.backend.services;

import com.university.backend.dto.StaffRequestResponseDTO;
import com.university.backend.entity.StaffRequests.RequestAttributes;
import com.university.backend.entity.StaffRequests.RequestValue;
import com.university.backend.entity.StaffRequests.StaffRequest;
import com.university.backend.entity.User;
import com.university.backend.repository.StaffRequestRepository;
import com.university.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StaffRequestService {

    private final StaffRequestRepository staffRequestRepository;
    private final UserRepository userRepository;

    @Autowired
    public StaffRequestService(StaffRequestRepository staffRequestRepository, UserRepository userRepository) {
        this.staffRequestRepository = staffRequestRepository;
        this.userRepository = userRepository;
    }

    /**
     * FETCH: Retrieves the full structure (Static Columns + EAV) and flattens it.
     */
    @Transactional(readOnly = true)
    public StaffRequestResponseDTO getRequestById(Long id) {
        // 1. Fetch optimized data
        StaffRequest request = staffRequestRepository.findFullRequestById(id)
                .orElseThrow(() -> new RuntimeException("Staff Request not found with ID: " + id));

        // 2. Initialize DTO
        StaffRequestResponseDTO response = new StaffRequestResponseDTO(request.getId());

        // 3. Map STATIC fields (From main table)
        response.addField("Request_Type", request.getRequestType());
        response.addField("Status", request.getStatus());
        
        if (request.getRequester() != null) {
            response.addField("Requester_Id", request.getRequester().getId());
            response.addField("Requester_Email", request.getRequester().getEmail());
        }

        // 4. Map DYNAMIC EAV fields (From attributes table)
        for (RequestValue val : request.getValues()) {
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
    public StaffRequestResponseDTO createRequest(Map<String, Object> payload) {

        // 1. Extract and Validate Static Keys
        String requestType = (String) payload.get("Request_Type");
        Object requesterIdObj = payload.get("Requester_Id");

        if (requestType == null || requesterIdObj == null) {
            throw new RuntimeException("Missing required fields: Request_Type or Requester_Id");
        }

        // 2. Fetch Real Entities for Foreign Keys
        Long requesterId = Long.parseLong(String.valueOf(requesterIdObj));
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("User not found: " + requesterId));

        // 3. Create Request with Static Links
        StaffRequest request = new StaffRequest(requester, requestType);

        // 4. Save to generate IDs (triggers default attribute creation)
        request = staffRequestRepository.save(request);

        // 5. Create Map for Attribute Lookup
        Map<String, RequestAttributes> attributeMap = request.getAttributes().stream()
                .collect(Collectors.toMap(RequestAttributes::getAttributeName, attr -> attr));

        // 6. Loop through Payload for DYNAMIC attributes
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Skip static keys we already processed
            if (key.equals("Request_Type") || key.equals("Requester_Id")) {
                continue;
            }

            // Process matches
            if (attributeMap.containsKey(key)) {
                RequestAttributes targetAttr = attributeMap.get(key);
                RequestValue newValue = createValueEntity(request, targetAttr, value);
                request.getValues().add(newValue);
            }
        }

        // 7. Update with new values
        staffRequestRepository.save(request);

        return getRequestById(request.getId());
    }

    // --- HELPER METHODS ---

    private Object extractValue(RequestValue val) {
        if (val.getValString() != null) return val.getValString();
        if (val.getValInt() != null) return val.getValInt();
        if (val.getValDouble() != null) return val.getValDouble();
        if (val.getValBool() != null) return val.getValBool();
        if (val.getValDate() != null) return val.getValDate();
        return null;
    }

    private RequestValue createValueEntity(StaffRequest req, RequestAttributes attr, Object value) {
        RequestValue valEntity = new RequestValue();
        valEntity.setRequest(req);
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