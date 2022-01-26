package nextstep.subway.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public class Line extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String color;
    @OneToMany(mappedBy = "line", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();

    protected Line() {
    }

    public Line(Long id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public Line(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }

    public void update(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public void updateSection(Section section) {
        this.sections = new ArrayList<>();
        this.sections.add(section);
    }

    public void addSection(Section section) {
        validationSectionByStation(section);
        this.sections.add(section);
    }

    private void validationSectionByStation(Section newSection) {
        Section lastSection = this.sections.get(this.sections.size() - 1);
        lastSection.validationUpStation(newSection);
        this.sections.forEach(section -> section.validationDownStation(newSection));
    }

    public void deleteSection(Long stationId) {
        validationDeleteStationCount();
        Section lastSection = this.sections.get(this.sections.size() - 1);
        lastSection.validationDeleteStation(stationId);
        this.sections.remove(lastSection);
    }

    private void validationDeleteStationCount() {
        if (this.sections.size() == 1) {
            throw new IllegalArgumentException("구간이 하나인 경우 삭제할 수 없습니다.");
        }
    }
}
