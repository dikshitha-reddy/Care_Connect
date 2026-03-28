package com.example.demo.controller;

import com.example.demo.dto.PrescriptionRequest;
import com.example.demo.model.Appointment;
import com.example.demo.model.AppointmentStatus;
import com.example.demo.model.Prescription;
import com.example.demo.model.User;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.PrescriptionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
@CrossOrigin("*")
public class DoctorController {

    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;

    public DoctorController(AppointmentRepository appointmentRepository, PrescriptionRepository prescriptionRepository) {
        this.appointmentRepository = appointmentRepository;
        this.prescriptionRepository = prescriptionRepository;
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getMyAppointments(@RequestParam Long doctorId) {
        return ResponseEntity.ok(appointmentRepository.findByDoctorId(doctorId));
    }

    @PutMapping("/appointments/{id}/status")
    public ResponseEntity<?> updateAppointmentStatus(
            @PathVariable Long id, 
            @RequestParam String status, 
            @RequestParam Long doctorId) {
        
        System.out.println("[DEBUG] Received status update request for appt " + id + " to " + status);
        
        // No more authentication, we just use the doctorId from the request
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getDoctor().getId().equals(doctorId)) {
            System.err.println("[AUTH] Doctor " + doctorId + " unauthorized for appt " + id);
            return ResponseEntity.status(403).body("Unauthorized");
        }

        try {
            AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status.toUpperCase());
            appointment.setStatus(appointmentStatus);
            
            Appointment savedAppt = appointmentRepository.save(appointment);
            
            // SIMULATE SMS NOTIFICATION
            if (appointmentStatus == AppointmentStatus.ACCEPTED) {
                String docName = appointment.getDoctor().getName() != null ? appointment.getDoctor().getName() : "Doctor";
                String docPhone = appointment.getDoctor().getPhone() != null ? appointment.getDoctor().getPhone() : "N/A";
                
                System.out.println("[SMS] SENT TO DR. " + docName.toUpperCase() + " (" + docPhone + "): " +
                    "New appointment confirmed with " + appointment.getPatient().getName() + " on " + appointment.getAppointmentDate());
                
                // Also simulate for customer if needed
            } else if (appointmentStatus == AppointmentStatus.REJECTED) {
                System.out.println("[SMS] SENT: Appointment " + id + " rejected and moved to bin.");
            }

            return ResponseEntity.ok(savedAppt);
        } catch (IllegalArgumentException e) {
            System.err.println("[ERROR] Invalid status for appt " + id + ": " + status);
            return ResponseEntity.badRequest().body("Invalid status: " + status);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to save appt " + id + ": " + e.getMessage());
            throw e;
        }
    }

    @PostMapping("/appointments/{id}/prescription")
    public ResponseEntity<?> addPrescription(
            @PathVariable Long id, 
            @RequestBody PrescriptionRequest request, 
            @RequestParam Long doctorId) {
            
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getDoctor().getId().equals(doctorId)) {
            return ResponseEntity.status(403).body("Unauthorized");
        }
        
        if (!appointment.getStatus().equals(AppointmentStatus.ACCEPTED) && 
            !appointment.getStatus().equals(AppointmentStatus.CONFIRMED) &&
            !appointment.getStatus().equals(AppointmentStatus.COMPLETED)) {
            return ResponseEntity.badRequest().body("Appointment must be ACCEPTED or CONFIRMED to add prescription");
        }

        Prescription prescription = new Prescription(
                null,
                appointment,
                request.getMedicines(),
                request.getInstructions()
        );

        Prescription savedPrescription = prescriptionRepository.save(prescription);
        
        // Auto-complete the appointment
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        return ResponseEntity.ok(savedPrescription);
    }
}
