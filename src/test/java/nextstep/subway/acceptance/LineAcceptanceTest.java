package nextstep.subway.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.applicaion.dto.LineCreateResponse;
import nextstep.subway.applicaion.dto.LineResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static nextstep.subway.acceptance.LineSteps.*;
import static nextstep.subway.acceptance.StationSteps.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("지하철 노선 관리 기능")
class LineAcceptanceTest extends AcceptanceTest {

    /**
     * When 지하철 노선 생성을 요청 하면
     * Then 지하철 노선 생성이 성공한다.
     */
    @DisplayName("지하철 노선 생성")
    @Test
    void createLine() {
        // given
        지하철역들_생성_요청(10);
        String bgRed600 = "bg-red-600";
        String 신분당선 = "신분당선";
        String upStationId = "1";
        String downStationId = "4";
        String distance = "10";

        // when
        ExtractableResponse<Response> createResponse = 지하철_노선_생성_요청2(
                신분당선,
                bgRed600,
                upStationId,
                downStationId,
                distance);

        // then
        assertThat(createResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        LineResponse lineResponse = createResponse.body().as(LineResponse.class);
        assertAll(
                () -> assertNotNull(lineResponse.getId()),
                () -> assertEquals(lineResponse.getName(), 신분당선),
                () -> assertEquals(lineResponse.getColor(), bgRed600),
                () -> assertNotNull(lineResponse.getCreatedDate()),
                () -> assertNotNull(lineResponse.getModifiedDate()));
    }

    /**
     * Given 지하철 노선의 상행 또는 하행이 존재하지 않는 역이고
     * When 지하철 노선 생성을 요청 하면
     * Then 지하철 노선 생성이 실패한다.
     */
    @DisplayName("지하철 역이 존재하지 않는 지하철 역인 지하철 노선 생성")
    @CsvSource(delimiter = ':', value = {"1:2", "2:1"})
    @ParameterizedTest
    void createLineWithNotExistUpStation(String upStationId, String downStationId) {
        //given
        지하철역들_생성_요청(1);
        String bgRed600 = "bg-red-600";
        String 신분당선 = "신분당선";
        String distance = "10";

        // when
        ExtractableResponse<Response> createResponse = 지하철_노선_생성_요청2(
                신분당선,
                bgRed600,
                upStationId,
                downStationId,
                distance);

        //then
        //todo advicecontroller로  400 badrequest로 반환하도록 리팩토링
        assertThat(createResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }


    /**
     * Given 지하철역 생성을 요청 하고
     * When 같은 이름으로 지하철역 생성을 요청 하면
     * Then 지하철역 생성이 실패한다.
     */
    @DisplayName("지하철 노선 중복이름 생성")
    @Test
    void createDuplicateNameLine() {
        // given
        지하철역들_생성_요청(10);
        String bgRed600 = "bg-red-600";
        String 신분당선 = "신분당선";
        String upStationId = "1";
        String downStationId = "4";
        String distance = "10";
        지하철_노선_생성_요청2(
                신분당선,
                bgRed600,
                upStationId,
                downStationId,
                distance);

        // when
        ExtractableResponse<Response> createResponse = 지하철_노선_생성_요청2(
                신분당선,
                bgRed600,
                upStationId,
                downStationId,
                distance);

        // then
        assertThat(createResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Given 지하철 노선 생성을 요청 하고
     * Given 새로운 지하철 노선 생성을 요청 하고
     * When 지하철 노선 목록 조회를 요청 하면
     * Then 두 노선이 포함된 지하철 노선 목록을 응답받는다
     */
    @DisplayName("지하철 노선 목록 조회")
    @Test
    void getLines() {
        // given
        Map<String, String> createParams = getParams("신분당선", "bg-red-600");
        ExtractableResponse<Response> createResponse1 = 지하철_노선_생성_요청(createParams);

        Map<String, String> createParams2 = getParams("2호선", "bg-green-600");
        ExtractableResponse<Response> createResponse2 = 지하철_노선_생성_요청(createParams2);

        //when
        LineCreateResponse firstLine = createResponse1.body().as(LineCreateResponse.class);
        LineCreateResponse secondLine = createResponse2.body().as(LineCreateResponse.class);
        String url = LineSteps.DEFAULT_PATH;
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(url);

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<LineResponse> lineResponses = response.body().jsonPath().getList(".", LineResponse.class);

        LineResponse firstLineResponse = lineCreateResponseConvertToLineResponse(firstLine);
        LineResponse secondLineResponse = lineCreateResponseConvertToLineResponse(secondLine);
        assertThat(lineResponses).contains(firstLineResponse, secondLineResponse);
    }

    /**
     * Given 지하철 노선 생성을 요청 하고
     * When 생성한 지하철 노선 조회를 요청 하면
     * Then 생성한 지하철 노선을 응답받는다
     */
    @DisplayName("지하철 노선 조회")
    @Test
    void getLine() {
        // given
        Map<String, String> createParams = getParams("신분당선", "bg-red-600");
        ExtractableResponse<Response> createResponse = 지하철_노선_생성_요청(createParams);

        // when
        LineCreateResponse lineCreateResponse = createResponse.body().as(LineCreateResponse.class);
        String uri = createResponse.header("Location");
        ExtractableResponse<Response> response = 지하철_노선_조회_요청(uri);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        LineResponse createLineResponse = lineCreateResponseConvertToLineResponse(lineCreateResponse);
        LineResponse lineResponse = response.body().as(LineResponse.class);

        assertThat(createLineResponse).isEqualTo(lineResponse);
    }

    /**
     * Given 지하철 노선 생성을 요청 하고
     * When 지하철 노선의 정보 수정을 요청 하면
     * Then 지하철 노선의 정보 수정은 성공한다.
     */
    @DisplayName("지하철 노선 수정")
    @Test
    void updateLine() {
        // given
        Map<String, String> createParams = getParams("신분당선", "bg-red-600");
        ExtractableResponse<Response> createResponse = 지하철_노선_생성_요청(createParams);

        // when
        Map<String, String> updateParams = getParams("구분당선", "bg-blue-600");
        String uri = createResponse.header("Location");
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .body(updateParams)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .put(uri)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    /**
     * Given 지하철 노선 생성을 요청 하고
     * When 생성한 지하철 노선 삭제를 요청 하면
     * Then 생성한 지하철 노선 삭제가 성공한다.
     */
    @DisplayName("지하철 노선 삭제")
    @Test
    void deleteLine() {
        // given
        Map<String, String> createParams = getParams("신분당선", "bg-red-600");
        ExtractableResponse<Response> createResponse = 지하철_노선_생성_요청(createParams);

        // when
        String uri = createResponse.header("Location");
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when()
                .delete(uri)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }
}