package irrigation_system.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "parcels")
public class Parcel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private double size;
    private String cropType;

    private LocalDate lastIrrigation;

    private String notes;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "parcel", cascade = CascadeType.ALL)
    private List<IrrigationHistory> irrigationHistory;

    @OneToOne(mappedBy = "parcel", cascade = CascadeType.ALL)
    private IrrigationSchedule irrigationSchedule;

    public Parcel() {}

    // getters & setters
}