package irrigation_system.backend.service.impl;

import irrigation_system.backend.dto.IrrigationScheduleRequest;
import irrigation_system.backend.model.IrrigationHistory;
import irrigation_system.backend.model.IrrigationSchedule;
import irrigation_system.backend.model.Parcel;
import irrigation_system.backend.repository.IrrigationHistoryRepository;
import irrigation_system.backend.repository.IrrigationScheduleRepository;
import irrigation_system.backend.repository.ParcelRepository;
import irrigation_system.backend.service.IrrigationScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class IrrigationScheduleServiceImpl implements IrrigationScheduleService {

    private final IrrigationScheduleRepository irrigationScheduleRepository;
    private final IrrigationHistoryRepository irrigationHistoryRepository;
    private final ParcelRepository parcelRepository;

    public IrrigationScheduleServiceImpl(
            IrrigationScheduleRepository irrigationScheduleRepository,
            IrrigationHistoryRepository irrigationHistoryRepository,
            ParcelRepository parcelRepository
    ) {
        this.irrigationScheduleRepository = irrigationScheduleRepository;
        this.irrigationHistoryRepository = irrigationHistoryRepository;
        this.parcelRepository = parcelRepository;
    }

    @Override
    public IrrigationSchedule createSchedule(Long parcelId, IrrigationScheduleRequest request) {
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found with id: " + parcelId));

        IrrigationSchedule schedule = new IrrigationSchedule();
        schedule.setIntervalDays(request.intervalDays());
        schedule.setWaterAmount(request.waterAmount());
        schedule.setActive(request.active());
        schedule.setParcel(parcel);

        return irrigationScheduleRepository.save(schedule);
    }

    @Override
    public IrrigationSchedule updateSchedule(Long id, IrrigationScheduleRequest request) {
        IrrigationSchedule existingSchedule = irrigationScheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found with id: " + id));

        existingSchedule.setIntervalDays(request.intervalDays());
        existingSchedule.setWaterAmount(request.waterAmount());
        existingSchedule.setActive(request.active());

        return irrigationScheduleRepository.save(existingSchedule);
    }

    @Override
    public void deleteSchedule(Long id) {
        if (!irrigationScheduleRepository.existsById(id)) {
            throw new IllegalArgumentException("Schedule not found with id: " + id);
        }

        irrigationScheduleRepository.deleteById(id);
    }

    @Override
    public List<IrrigationSchedule> getSchedulesByParcel(Long parcelId) {
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found with id: " + parcelId));

        return irrigationScheduleRepository.findByParcel(parcel)
                .map(List::of)
                .orElseGet(List::of);
    }

    @Override
    @Transactional
    public void executeScheduledIrrigation(Long scheduleId) {
        IrrigationSchedule schedule = irrigationScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found with id: " + scheduleId));

        if (!schedule.isActive()) {
            throw new IllegalStateException("Cannot execute inactive irrigation schedule.");
        }

        Parcel parcel = schedule.getParcel();

        IrrigationHistory history = new IrrigationHistory();
        history.setParcel(parcel);
        history.setWaterAmount(schedule.getWaterAmount());
        history.setIrrigationDate(LocalDate.now());
        history.setIrrigationTime(LocalTime.now());

        parcel.setLastIrrigation(history.getIrrigationDate());
        parcelRepository.save(parcel);
        irrigationHistoryRepository.save(history);
    }
}
