package irrigation_system.backend.repository;

import irrigation_system.backend.model.IrrigationHistory;
import irrigation_system.backend.model.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IrrigationHistoryRepository extends JpaRepository<IrrigationHistory, Long> {

    List<IrrigationHistory> findAllByParcel(Parcel parcel);
}