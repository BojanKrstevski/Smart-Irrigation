package irrigation_system.repository;

import irrigation_system.model.IrrigationHistory;
import irrigation_system.model.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IrrigationHistoryRepository extends JpaRepository<IrrigationHistory, Long> {

    List<IrrigationHistory> findAllByParcel(Parcel parcel);
}