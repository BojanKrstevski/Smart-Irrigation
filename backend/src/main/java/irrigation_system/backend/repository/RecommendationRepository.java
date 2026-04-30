package irrigation_system.backend.repository;

import irrigation_system.backend.model.Recommendation;
import irrigation_system.backend.model.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    List<Recommendation> findAllByParcel(Parcel parcel);
}