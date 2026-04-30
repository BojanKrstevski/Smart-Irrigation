package irrigation_system.backend.mapper;

import irrigation_system.backend.dto.IrrigationHistoryResponse;
import irrigation_system.backend.dto.IrrigationScheduleResponse;
import irrigation_system.backend.dto.ParcelResponse;
import irrigation_system.backend.dto.UserResponse;
import irrigation_system.backend.model.IrrigationHistory;
import irrigation_system.backend.model.IrrigationSchedule;
import irrigation_system.backend.model.Parcel;
import irrigation_system.backend.model.User;

public final class DtoMapper {

    private DtoMapper() {
    }

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }

    public static ParcelResponse toParcelResponse(Parcel parcel) {
        Long userId = parcel.getUser() != null ? parcel.getUser().getId() : null;

        return new ParcelResponse(
                parcel.getId(),
                parcel.getName(),
                parcel.getLocation(),
                parcel.getSize(),
                parcel.getCropType(),
                parcel.getLastIrrigation(),
                parcel.getNotes(),
                userId
        );
    }

    public static IrrigationHistoryResponse toIrrigationHistoryResponse(IrrigationHistory history) {
        Long parcelId = history.getParcel() != null ? history.getParcel().getId() : null;

        return new IrrigationHistoryResponse(
                history.getId(),
                history.getIrrigationDate(),
                history.getIrrigationTime(),
                history.getWaterAmount(),
                parcelId
        );
    }

    public static IrrigationScheduleResponse toIrrigationScheduleResponse(IrrigationSchedule schedule) {
        Long parcelId = schedule.getParcel() != null ? schedule.getParcel().getId() : null;

        return new IrrigationScheduleResponse(
                schedule.getId(),
                schedule.getIntervalDays(),
                schedule.getWaterAmount(),
                schedule.isActive(),
                parcelId
        );
    }
}
