package irrigation_system.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "irrigation_schedule")
public class IrrigationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer intervalDays;

    private double waterAmount;

    private boolean active;

    @OneToOne
    @JoinColumn(name = "parcel_id")
    private Parcel parcel;

    public IrrigationSchedule() {}

    // getters & setters
}