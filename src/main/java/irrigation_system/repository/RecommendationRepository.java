package irrigation_system.repository;

import irrigation_system.model.Recommendation;
import irrigation_system.model.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    List<Recommendation> findAllByParcel(Parcel parcel);
}