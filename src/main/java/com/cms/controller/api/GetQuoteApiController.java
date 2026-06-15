package com.cms.controller.api;

import com.cms.entity.auto.VehicleLead;
import com.cms.repository.auto.VehicleLeadRepository;
import com.cms.repository.auto.CallRequestRepository;
import com.cms.service.auto.SmsCallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/quote")
public class GetQuoteApiController {

    @Autowired private VehicleLeadRepository leadRepo;
    @Autowired private CallRequestRepository callRepo;
    @Autowired private SmsCallService smsCallService;

    /**
     * Submit Get Quote form — public endpoint (no auth required)
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitQuote(@RequestBody VehicleLead lead) {
        // Validate required fields
        if (lead.getName() == null || lead.getName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
        }
        if (lead.getMobile() == null || lead.getMobile().length() < 10) {
            return ResponseEntity.badRequest().body(Map.of("error", "Valid mobile number is required"));
        }

        // Duplicate check: same mobile + vehicle within 10 minutes
        if (lead.getVehicleId() != null) {
            long recentCount = leadRepo.countRecentLeads(
                lead.getMobile(), lead.getVehicleId(), LocalDateTime.now().minusMinutes(10));
            if (recentCount > 0) {
                return ResponseEntity.ok(Map.of("message", "We already have your request. Our team will contact you soon!", "duplicate", true));
            }
        }

        // Save lead
        lead.setId(null);
        lead.setIsDeleted(false);
        lead.setContactStatus("new");
        lead.setCallStatus("pending");
        VehicleLead saved = leadRepo.save(lead);

        // Trigger SMS to customer
        boolean smsSent = smsCallService.sendCustomerSms(saved);
        saved.setSmsSent(smsSent);

        // Trigger SMS to admin
        boolean adminSms = smsCallService.sendAdminSms(saved);
        saved.setAdminSmsSent(adminSms);

        // Trigger auto-call
        smsCallService.triggerAutoCall(saved);
        saved.setCallStatus("triggered");

        leadRepo.save(saved);

        return ResponseEntity.ok(Map.of(
            "message", "Thank you! Our team will contact you shortly.",
            "leadId", saved.getId(),
            "smsSent", smsSent
        ));
    }

    /**
     * Admin: List all vehicle leads
     */
    @GetMapping("/leads")
    public ResponseEntity<?> listLeads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String vehicleId) {

        if (!search.isBlank()) {
            return ResponseEntity.ok(leadRepo.searchLeads(search, PageRequest.of(page, size, Sort.by("createdAt").descending())));
        }
        if (!status.isBlank()) {
            return ResponseEntity.ok(leadRepo.findByContactStatusAndIsDeletedFalse(status, PageRequest.of(page, size, Sort.by("createdAt").descending())));
        }
        if (!vehicleId.isBlank()) {
            return ResponseEntity.ok(leadRepo.findByVehicleIdAndIsDeletedFalse(Long.parseLong(vehicleId), PageRequest.of(page, size, Sort.by("createdAt").descending())));
        }
        return ResponseEntity.ok(leadRepo.findByIsDeletedFalse(PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    /**
     * Admin: Update lead status
     */
    @PutMapping("/leads/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return leadRepo.findById(id).map(lead -> {
            if (body.get("contactStatus") != null) lead.setContactStatus(body.get("contactStatus"));
            if (body.get("callStatus") != null) lead.setCallStatus(body.get("callStatus"));
            return ResponseEntity.ok(Map.of("data", leadRepo.save(lead)));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Admin: Delete lead (soft)
     */
    @DeleteMapping("/leads/{id}")
    public ResponseEntity<?> deleteLead(@PathVariable Long id) {
        leadRepo.findById(id).ifPresent(l -> { l.setIsDeleted(true); leadRepo.save(l); });
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    /**
     * Admin: Send SMS to a specific lead
     */
    @PostMapping("/leads/{id}/sms")
    public ResponseEntity<?> sendSmsToLead(@PathVariable Long id) {
        return leadRepo.findById(id).map(lead -> {
            boolean sent = smsCallService.sendCustomerSms(lead);
            lead.setSmsSent(sent);
            leadRepo.save(lead);
            String msg = String.format("Thanks for your interest in %s. Our team will contact you shortly.", lead.getVehicleName());
            return ResponseEntity.ok(Map.of("sent", sent, "mobile", lead.getMobile(), "message", msg));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Admin: Trigger call to a specific lead
     */
    @PostMapping("/leads/{id}/call")
    public ResponseEntity<?> triggerCallToLead(@PathVariable Long id) {
        return leadRepo.findById(id).map(lead -> {
            var callReq = smsCallService.triggerAutoCall(lead);
            lead.setCallStatus(callReq.getStatus());
            leadRepo.save(lead);
            return ResponseEntity.ok(Map.of("status", callReq.getStatus(), "message", callReq.getResponseData()));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Admin: Export leads as JSON (CSV can be generated client-side)
     */
    @GetMapping("/leads/export")
    public ResponseEntity<?> exportLeads(
            @RequestParam(defaultValue = "") String from,
            @RequestParam(defaultValue = "") String to) {
        if (!from.isBlank() && !to.isBlank()) {
            LocalDateTime fromDate = LocalDateTime.parse(from + "T00:00:00");
            LocalDateTime toDate = LocalDateTime.parse(to + "T23:59:59");
            return ResponseEntity.ok(leadRepo.findByDateRange(fromDate, toDate));
        }
        return ResponseEntity.ok(leadRepo.findByIsDeletedFalse(PageRequest.of(0, 1000, Sort.by("createdAt").descending())).getContent());
    }

    /**
     * Admin: Call requests log
     */
    @GetMapping("/calls")
    public ResponseEntity<?> listCalls(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size,
                                        @RequestParam(defaultValue = "") String status) {
        if (!status.isBlank()) {
            return ResponseEntity.ok(callRepo.findByStatus(status, PageRequest.of(page, size)));
        }
        return ResponseEntity.ok(callRepo.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }
}
