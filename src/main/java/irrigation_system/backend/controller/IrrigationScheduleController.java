package irrigation_system.backend.controller;

import irrigation_system.backend.dto.IrrigationScheduleRequest;
import irrigation_system.backend.dto.IrrigationScheduleResponse;
import irrigation_system.backend.mapper.DtoMapper;
import irrigation_system.backend.service.IrrigationScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class IrrigationScheduleController {

    private final IrrigationScheduleService irrigationScheduleService;

    public IrrigationScheduleController(IrrigationScheduleService irrigationScheduleService) {
        this.irrigationScheduleService = irrigationScheduleService;
    }

    @PostMapping("/parcel/{parcelId}")
    public ResponseEntity<IrrigationScheduleResponse> createSchedule(
            @PathVariable Long parcelId,
            @RequestBody IrrigationScheduleRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DtoMapper.toIrrigationScheduleResponse(irrigationScheduleService.createSchedule(parcelId, request)));
    }

    @GetMapping("/parcel/{parcelId}")
    public ResponseEntity<List<IrrigationScheduleResponse>> getSchedulesByParcel(@PathVariable Long parcelId) {
        return ResponseEntity.ok(irrigationScheduleService.getSchedulesByParcel(parcelId).stream()
                .map(DtoMapper::toIrrigationScheduleResponse)
                .toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<IrrigationScheduleResponse> updateSchedule(
            @PathVariable Long id,
            @RequestBody IrrigationScheduleRequest request
    ) {
        return ResponseEntity.ok(DtoMapper.toIrrigationScheduleResponse(
                irrigationScheduleService.updateSchedule(id, request)
        ));
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<Void> executeScheduledIrrigation(@PathVariable Long id) {
        irrigationScheduleService.executeScheduledIrrigation(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        irrigationScheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
}
