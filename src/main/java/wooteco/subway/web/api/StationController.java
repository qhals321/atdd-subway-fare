package wooteco.subway.web.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wooteco.subway.service.StationService;
import wooteco.subway.web.dto.request.StationRequest;
import wooteco.subway.web.dto.response.StationResponse;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/stations")
public class StationController {
    private StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @PostMapping
    public ResponseEntity<StationResponse> createStation(@RequestBody StationRequest stationRequest) {
        StationResponse station = stationService.saveStation(stationRequest);
        return ResponseEntity.created(URI.create("/api/stations/" + station.getId())).body(station);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StationResponse>> showStations() {
        return ResponseEntity.ok().body(stationService.findAllStationResponses());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStation(@PathVariable Long id) {
        stationService.deleteStationById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<StationResponse> updateStation(@PathVariable Long id, @RequestBody StationRequest stationRequest) {
        return ResponseEntity.ok(stationService.updateStation(id, stationRequest.getName()));
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity handleSQLException(SQLException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
