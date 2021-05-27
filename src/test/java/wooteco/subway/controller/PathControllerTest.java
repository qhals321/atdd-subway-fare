package wooteco.subway.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import wooteco.auth.util.JwtTokenProvider;
import wooteco.subway.TestDataLoader;
import wooteco.auth.service.AuthService;
import wooteco.subway.domain.Line;
import wooteco.subway.domain.Section;
import wooteco.subway.service.FareCalculator;
import wooteco.subway.service.PathService;
import wooteco.subway.web.dto.response.PathResponse;
import wooteco.subway.web.api.PathController;
import wooteco.subway.domain.Station;
import wooteco.subway.web.dto.response.StationResponse;

@WebMvcTest(controllers = PathController.class)
@ActiveProfiles("test")
@AutoConfigureRestDocs
class PathControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PathService pathService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("구간 검색 - 성공")
    public void findPath() throws Exception {
        //given
        final TestDataLoader testDataLoader = new TestDataLoader();
        final Line 신분당선 = testDataLoader.신분당선();
        final int totalDistance =
            신분당선.getSections()
                .getSections()
                .stream()
                .mapToInt(Section::getDistance)
                .sum();
        final FareCalculator fareCalculator = new FareCalculator();

        final List<Station> stations = Arrays
            .asList(testDataLoader.강남역(), testDataLoader.판교역(), testDataLoader.정자역());

        final int fare = fareCalculator.calculateFare(totalDistance);
        final PathResponse pathResponse =
            new PathResponse(StationResponse.listOf(stations), totalDistance, fare);

        final Long source = testDataLoader.강남역().getId();
        final Long target = testDataLoader.정자역().getId();

        given(pathService.findPath(source, target)).willReturn(pathResponse);

        mockMvc.perform(get("/api/paths?source=" + source + "&target=" + target))
            .andExpect(status().isOk())
            .andExpect(jsonPath("stations[*].name").value(
                Matchers.containsInRelativeOrder("강남역","판교역","정자역")))
            .andDo(document("path-find",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint())));
    }
}