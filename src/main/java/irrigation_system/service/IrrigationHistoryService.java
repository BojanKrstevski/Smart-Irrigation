package irrigation_system.service;

import irrigation_system.model.IrrigationHistory;

import java.util.List;

public interface IrrigationHistoryService {

    IrrigationHistory addManualIrrigation(Long parcelId, double waterAmount);

    List<IrrigationHistory> getHistoryByParcel(Long parcelId);
}