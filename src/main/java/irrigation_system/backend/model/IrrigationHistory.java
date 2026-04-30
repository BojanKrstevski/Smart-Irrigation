package irrigation_system.backend.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "irrigation_history")
public class IrrigationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate irrigationDate;

    private LocalTime irrigationTime;

    private double waterAmount;

    @ManyToOne
    @JoinColumn(name = "parcel_id")
    private Parcel parcel;

    public IrrigationHistory() {}

    // getters & setters
}