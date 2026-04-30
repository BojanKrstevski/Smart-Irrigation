package irrigation_system.backend.service.impl;

import irrigation_system.backend.model.IrrigationHistory;
import irrigation_system.backend.model.Parcel;
import irrigation_system.backend.repository.IrrigationHistoryRepository;
import irrigation_system.backend.repository.ParcelRepository;
import irrigation_system.backend.service.IrrigationHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class IrrigationHistoryServiceImpl implements IrrigationHistoryService {

    private final IrrigationHistoryRepository irrigationHistoryRepository;
    private final ParcelRepository parcelRepository;

    public IrrigationHistoryServiceImpl(
            IrrigationHistoryRepository irrigationHistoryRepository,
            ParcelRepository parcelRepository
    ) {
        this.irrigationHistoryRepository = irrigationHistoryRepository;
        this.parcelRepository = parcelRepository;
    }

    @Override
    @Transactional
    public IrrigationHistory addManualIrrigation(Long parcelId, double waterAmount) {
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found with id: " + parcelId));

        IrrigationHistory history = new IrrigationHistory();
        history.setParcel(parcel);
        history.setWaterAmount(waterAmount);
        history.setIrrigationDate(LocalDate.now());
        history.setIrrigationTime(LocalTime.now());

        parcel.setLastIrrigation(history.getIrrigationDate());
        parcelRepository.save(parcel);

        return irrigationHistoryRepository.save(history);
    }

    @Override
    public List<IrrigationHistory> getHistoryByParcel(Long parcelId) {
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found with id: " + parcelId));

        return irrigationHistoryRepository.findAllByParcel(parcel);
    }
}
