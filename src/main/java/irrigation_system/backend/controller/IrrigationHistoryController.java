package irrigation_system.backend.controller;

import irrigation_system.backend.dto.AddIrrigationRequest;
import irrigation_system.backend.dto.IrrigationHistoryResponse;
import irrigation_system.backend.mapper.DtoMapper;
import irrigation_system.backend.model.IrrigationHistory;
import irrigation_system.backend.service.IrrigationHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/irrigation-history")
public class IrrigationHistoryController {

    private final IrrigationHistoryService irrigationHistoryService;

    public IrrigationHistoryController(IrrigationHistoryService irrigationHistoryService) {
        this.irrigationHistoryService = irrigationHistoryService;
    }

    @PostMapping("/parcel/{parcelId}")
    public ResponseEntity<IrrigationHistoryResponse> addManualIrrigation(
            @PathVariable Long parcelId,
            @RequestBody AddIrrigationRequest request
    ) {
        IrrigationHistory history = irrigationHistoryService.addManualIrrigation(parcelId, request.waterAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toIrrigationHistoryResponse(history));
    }

    @GetMapping("/parcel/{parcelId}")
    public ResponseEntity<List<IrrigationHistoryResponse>> getHistoryByParcel(@PathVariable Long parcelId) {
        return ResponseEntity.ok(irrigationHistoryService.getHistoryByParcel(parcelId).stream()
                .map(DtoMapper::toIrrigationHistoryResponse)
                .toList());
    }
}
