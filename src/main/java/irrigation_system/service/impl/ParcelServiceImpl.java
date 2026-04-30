package irrigation_system.service.impl;

import irrigation_system.dto.ParcelRequest;
import irrigation_system.model.Parcel;
import irrigation_system.model.User;
import irrigation_system.repository.ParcelRepository;
import irrigation_system.repository.UserRepository;
import irrigation_system.service.ParcelService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParcelServiceImpl implements ParcelService {

    private final ParcelRepository parcelRepository;
    private final UserRepository userRepository;

    public ParcelServiceImpl(ParcelRepository parcelRepository, UserRepository userRepository) {
        this.parcelRepository = parcelRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Parcel createParcel(Long userId, ParcelRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Parcel parcel = new Parcel();
        parcel.setName(request.name());
        parcel.setLocation(request.location());
        parcel.setSize(request.size());
        parcel.setCropType(request.cropType());
        parcel.setLastIrrigation(request.lastIrrigation());
        parcel.setNotes(request.notes());
        parcel.setUser(user);

        return parcelRepository.save(parcel);
    }

    @Override
    public List<Parcel> getUserParcels(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        return parcelRepository.findAllByUser(user);
    }

    @Override
    public Parcel updateParcel(Long id, ParcelRequest request) {
        Parcel existingParcel = getById(id);

        existingParcel.setName(request.name());
        existingParcel.setLocation(request.location());
        existingParcel.setSize(request.size());
        existingParcel.setCropType(request.cropType());
        existingParcel.setLastIrrigation(request.lastIrrigation());
        existingParcel.setNotes(request.notes());

        return parcelRepository.save(existingParcel);
    }

    @Override
    public void deleteParcel(Long id) {
        if (!parcelRepository.existsById(id)) {
            throw new IllegalArgumentException("Parcel not found with id: " + id);
        }

        parcelRepository.deleteById(id);
    }

    @Override
    public Parcel getById(Long id) {
        return parcelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found with id: " + id));
    }
}
