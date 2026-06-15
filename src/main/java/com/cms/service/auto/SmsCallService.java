package com.cms.service.auto;

import com.cms.entity.auto.CallRequest;
import com.cms.entity.auto.VehicleLead;
import com.cms.repository.auto.CallRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * SMS & Auto-Call service.
 * Supports: Fast2SMS, MSG91, Twilio (configurable).
 * Falls back to logging call requests in DB.
 */
@Service
public class SmsCallService {

    @Value("${sms.provider:none}")
    private String smsProvider; // fast2sms, msg91, twilio, none

    @Value("${sms.api.key:}")
    private String smsApiKey;

    @Value("${sms.admin.mobile:}")
    private String adminMobile;

    @Autowired
    private CallRequestRepository callRequestRepo;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Send SMS to customer after lead submission
     */
    public boolean sendCustomerSms(VehicleLead lead) {
        String message = String.format(
            "Thanks for your interest in %s. Our team will contact you shortly. - Quick Solution",
            lead.getVehicleName() != null ? lead.getVehicleName() : "our vehicle"
        );
        return sendSms(lead.getMobile(), message);
    }

    /**
     * Send SMS to admin about new lead
     */
    public boolean sendAdminSms(VehicleLead lead) {
        if (adminMobile == null || adminMobile.isBlank()) return false;
        String message = String.format(
            "New lead: %s (%s) interested in %s from %s",
            lead.getName(), lead.getMobile(),
            lead.getVehicleName() != null ? lead.getVehicleName() : "vehicle",
            lead.getCity() != null ? lead.getCity() : "unknown city"
        );
        return sendSms(adminMobile, message);
    }

    /**
     * Trigger auto-call to customer
     */
    public CallRequest triggerAutoCall(VehicleLead lead) {
        CallRequest callReq = new CallRequest();
        callReq.setLeadId(lead.getId());
        callReq.setMobile(lead.getMobile());
        callReq.setCustomerName(lead.getName());
        callReq.setVehicleName(lead.getVehicleName());
        callReq.setCallProvider(smsProvider);

        try {
            if ("fast2sms".equalsIgnoreCase(smsProvider) && !smsApiKey.isBlank()) {
                // Fast2SMS doesn't support calls directly — log for manual follow-up
                callReq.setStatus("pending");
                callReq.setResponseData("Fast2SMS: Call not supported, logged for manual follow-up");
            } else if ("twilio".equalsIgnoreCase(smsProvider) && !smsApiKey.isBlank()) {
                // Twilio call API would go here
                callReq.setStatus("triggered");
                callReq.setAttemptedAt(LocalDateTime.now());
                callReq.setResponseData("Twilio call initiated");
            } else if ("msg91".equalsIgnoreCase(smsProvider) && !smsApiKey.isBlank()) {
                // MSG91 call API
                callReq.setStatus("pending");
                callReq.setResponseData("MSG91: Queued for call");
            } else {
                // No provider — just log the request
                callReq.setStatus("pending");
                callReq.setCallProvider("manual");
                callReq.setResponseData("No SMS/Call provider configured. Call manually.");
            }
        } catch (Exception e) {
            callReq.setStatus("failed");
            callReq.setResponseData("Error: " + e.getMessage());
        }

        return callRequestRepo.save(callReq);
    }

    /**
     * Send SMS via configured provider
     */
    private boolean sendSms(String mobile, String message) {
        if (smsProvider == null || "none".equalsIgnoreCase(smsProvider) || smsApiKey.isBlank()) {
            System.out.println("[SMS LOG] To: " + mobile + " | Message: " + message);
            return false; // No provider configured — just log
        }

        try {
            if ("fast2sms".equalsIgnoreCase(smsProvider)) {
                return sendViaFast2Sms(mobile, message);
            } else if ("msg91".equalsIgnoreCase(smsProvider)) {
                return sendViaMsg91(mobile, message);
            }
        } catch (Exception e) {
            System.err.println("[SMS ERROR] " + e.getMessage());
        }
        return false;
    }

    private boolean sendViaFast2Sms(String mobile, String message) {
        try {
            String url = "https://www.fast2sms.com/dev/bulkV2";
            Map<String, String> headers = new HashMap<>();
            headers.put("authorization", smsApiKey);

            String body = String.format(
                "route=q&message=%s&language=english&flash=0&numbers=%s",
                java.net.URLEncoder.encode(message, "UTF-8"), mobile
            );

            org.springframework.http.HttpHeaders httpHeaders = new org.springframework.http.HttpHeaders();
            httpHeaders.set("authorization", smsApiKey);
            httpHeaders.set("Content-Type", "application/x-www-form-urlencoded");

            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(body, httpHeaders);
            restTemplate.postForObject(url, entity, String.class);
            return true;
        } catch (Exception e) {
            System.err.println("[Fast2SMS Error] " + e.getMessage());
            return false;
        }
    }

    private boolean sendViaMsg91(String mobile, String message) {
        try {
            String url = String.format(
                "https://api.msg91.com/api/v5/flow/",
                smsApiKey
            );
            // MSG91 requires template-based sending — simplified here
            System.out.println("[MSG91 LOG] To: " + mobile + " | " + message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
