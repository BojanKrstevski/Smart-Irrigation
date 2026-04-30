package irrigation_system.service;

import irrigation_system.dto.IrrigationScheduleRequest;
import irrigation_system.model.IrrigationSchedule;

import java.util.List;

public interface IrrigationScheduleService {

    IrrigationSchedule createSchedule(Long parcelId, IrrigationScheduleRequest request);

    IrrigationSchedule updateSchedule(Long id, IrrigationScheduleRequest request);

    void deleteSchedule(Long id);

    List<IrrigationSchedule> getSchedulesByParcel(Long parcelId);

    void executeScheduledIrrigation(Long scheduleId);
}
