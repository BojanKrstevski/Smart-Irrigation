package irrigation_system.service;

import irrigation_system.dto.ParcelRequest;
import irrigation_system.model.Parcel;

import java.util.List;

public interface ParcelService {

    Parcel createParcel(Long userId, ParcelRequest request);

    List<Parcel> getUserParcels(Long userId);

    Parcel updateParcel(Long id, ParcelRequest request);

    void deleteParcel(Long id);

    Parcel getById(Long id);
}
