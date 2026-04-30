package irrigation_system.repository;

import irrigation_system.model.IrrigationSchedule;
import irrigation_system.model.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IrrigationScheduleRepository extends JpaRepository<IrrigationSchedule, Long> {

    Optional<IrrigationSchedule> findByParcel(Parcel parcel);
}