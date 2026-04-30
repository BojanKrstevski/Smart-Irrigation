package irrigation_system.backend.repository;

import irrigation_system.backend.model.Parcel;
import irrigation_system.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParcelRepository extends JpaRepository<Parcel, Long> {

    List<Parcel> findAllByUser(User user);
}