package irrigation_system.backend.controller;

import irrigation_system.backend.dto.ParcelRequest;
import irrigation_system.backend.dto.ParcelResponse;
import irrigation_system.backend.mapper.DtoMapper;
import irrigation_system.backend.service.ParcelService;
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
@RequestMapping("/api/parcels")
public class ParcelController {

    private final ParcelService parcelService;

    public ParcelController(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<ParcelResponse> createParcel(@PathVariable Long userId, @RequestBody ParcelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DtoMapper.toParcelResponse(parcelService.createParcel(userId, request)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ParcelResponse>> getUserParcels(@PathVariable Long userId) {
        return ResponseEntity.ok(parcelService.getUserParcels(userId).stream()
                .map(DtoMapper::toParcelResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParcelResponse> getParcelById(@PathVariable Long id) {
        return ResponseEntity.ok(DtoMapper.toParcelResponse(parcelService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParcelResponse> updateParcel(@PathVariable Long id, @RequestBody ParcelRequest request) {
        return ResponseEntity.ok(DtoMapper.toParcelResponse(parcelService.updateParcel(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParcel(@PathVariable Long id) {
        parcelService.deleteParcel(id);
        return ResponseEntity.noContent().build();
    }
}
