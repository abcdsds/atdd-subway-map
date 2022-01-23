package nextstep.subway.applicaion;

import nextstep.subway.applicaion.dto.LineCreateRequest;
import nextstep.subway.applicaion.dto.LineCreateResponse;
import nextstep.subway.applicaion.dto.LineResponse;
import nextstep.subway.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class LineService {
    private LineRepository lineRepository;
    private SectionRepository sectionRepository;
    private StationRepository stationRepository;

    public LineService(LineRepository lineRepository, SectionRepository sectionRepository, StationRepository stationRepository) {
        this.lineRepository = lineRepository;
        this.sectionRepository = sectionRepository;
        this.stationRepository = stationRepository;
    }

    @Transactional(readOnly = true)
    public boolean existsLineByName(String name) {
        return lineRepository.existsLineByName(name);
    }

    public LineCreateResponse saveLine(LineCreateRequest request) {
        Line line = lineRepository.save(new Line(request.getName(), request.getColor()));
        Station upStation = stationRepository.findById(request.getUpStationId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상행 지하철역 입니다."));
        Station downStation = stationRepository.findById(request.getDownStationId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 하행 지하철역 입니다."));

        Section section = Section.createOf(line, upStation, downStation, request.getDistance());
        line.updateSection(section);

        return new LineCreateResponse(
                line.getId(),
                line.getName(),
                line.getColor(),
                line.getCreatedDate(),
                line.getModifiedDate()
        );
    }

    @Transactional(readOnly = true)
    public List<LineResponse> findAllLines() {
        List<Line> lines = lineRepository.findAll();
        return lines.stream()
                .map(this::createLineResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LineResponse findLineById(Long id) {
        Line line = lineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 아이디를 입력했습니다."));
        return createLineResponse(line);
    }

    public void updateLineById(Long id, LineCreateRequest lineCreateRequest) {
        Line line = lineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 아이디를 입력했습니다."));
        line.update(lineCreateRequest.getName(), lineCreateRequest.getColor());
//        Line updateLine =
//                new Line(line.getId(), lineCreateRequest.getName(), lineCreateRequest.getColor());
//
//        lineRepository.save(updateLine);
    }

    public void deleteLineById(Long id) {
        lineRepository.deleteById(id);
    }

    private LineResponse createLineResponse(Line line) {
        return new LineResponse(
                line.getId(),
                line.getName(),
                line.getColor(),
                Collections.EMPTY_LIST,
                line.getCreatedDate(),
                line.getModifiedDate());
    }
}
