package irrigation_system.backend.service;

import irrigation_system.backend.model.IrrigationHistory;

import java.util.List;

public interface IrrigationHistoryService {

    IrrigationHistory addManualIrrigation(Long parcelId, double waterAmount);

    List<IrrigationHistory> getHistoryByParcel(Long parcelId);
}