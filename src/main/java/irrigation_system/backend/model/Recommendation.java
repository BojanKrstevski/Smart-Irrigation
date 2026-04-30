package irrigation_system.backend.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "recommendations")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean irrigationNeeded;

    private String bestTime;

    private double recommendedWaterAmount;

    private String explanation;

    private LocalDate createdAt;

    @ManyToOne
    @JoinColumn(name = "parcel_id")
    private Parcel parcel;

    public Recommendation() {}

    // getters & setters
}