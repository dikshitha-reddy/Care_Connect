package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AppointmentRequest;
import com.example.demo.model.Appointment;
import com.example.demo.model.AppointmentStatus;
import com.example.demo.model.Prescription;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.PrescriptionRepository;
import com.example.demo.repository.UserRepository;

@RestController
@RequestMapping("/api/patient")
@CrossOrigin("**")
public class PatientController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @GetMapping("/doctors")
    public ResponseEntity<List<User>> getAllDoctors() {
        return ResponseEntity.ok(userRepository.findByRole(Role.DOCTOR));
    }

    @PostMapping("/appointments")
    public ResponseEntity<?> bookAppointment(@RequestBody AppointmentRequest request, @RequestParam Long patientId) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (!doctor.getRole().equals(Role.DOCTOR)) {
            return ResponseEntity.badRequest().body("Selected user is not a doctor");
        }

        Appointment appointment = new Appointment(
                null,
                patient,
                doctor,
                request.getAppointmentDate(),
                request.getAppointmentTime(),
                AppointmentStatus.PENDING,
                request.getNotes());

        return ResponseEntity.ok(appointmentRepository.save(appointment));
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getMyAppointments(@RequestParam Long patientId) {
        return ResponseEntity.ok(appointmentRepository.findByPatientId(patientId));
    }

    @GetMapping("/appointments/{id}/prescription")
    public ResponseEntity<?> getPrescription(@PathVariable Long id, @RequestParam Long patientId) {

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getPatient().getId().equals(patientId)) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        Prescription prescription = prescriptionRepository.findByAppointmentId(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));

        return ResponseEntity.ok(prescription);
    }
}
