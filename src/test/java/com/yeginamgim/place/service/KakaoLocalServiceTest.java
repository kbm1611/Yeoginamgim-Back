package com.yeginamgim.place.service;

import com.yeginamgim.place.dto.request.PlaceSearchRequest;
import com.yeginamgim.global.exception.KakaoLocalApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class KakaoLocalServiceTest {

    @Test
    void keywordSearchReturnsEmptyWhenApiKeyIsBlank() {
        KakaoLocalService kakaoLocalService = new KakaoLocalService("", RestClient.builder().build());

        assertThat(kakaoLocalService.searchByKeyword(PlaceSearchRequest.builder()
                .query("cafe")
                .build())).isEmpty();
    }

    @Test
    void keywordSearchThrowsBadGatewayWhenKakaoFails() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://dapi.kakao.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KakaoLocalService kakaoLocalService = new KakaoLocalService("test-key", builder.build());

        server.expect(requestTo("https://dapi.kakao.com/v2/local/search/keyword.json?query=cafe&page=1&size=10"))
                .andExpect(header("Authorization", "KakaoAK test-key"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> kakaoLocalService.searchByKeyword(PlaceSearchRequest.builder()
                .query("cafe")
                .build()))
                .isInstanceOf(KakaoLocalApiException.class)
                .hasMessage("카카오 Local API 호출에 실패했습니다.");

        server.verify();
    }

    @Test
    void categorySearchThrowsBadGatewayWhenKakaoFails() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://dapi.kakao.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KakaoLocalService kakaoLocalService = new KakaoLocalService("test-key", builder.build());

        server.expect(requestTo("https://dapi.kakao.com/v2/local/search/category.json?category_group_code=CE7&y=37.4979&x=127.0276&radius=1000&page=1&size=10"))
                .andExpect(header("Authorization", "KakaoAK test-key"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> kakaoLocalService.searchByCategory(PlaceSearchRequest.builder()
                .category("cafe")
                .latitude(37.4979)
                .longitude(127.0276)
                .radius(1000)
                .build()))
                .isInstanceOf(KakaoLocalApiException.class)
                .hasMessage("카카오 Local API 호출에 실패했습니다.");

        server.verify();
    }

    @ParameterizedTest
    @MethodSource("supportedKakaoCategoryCodes")
    void categorySearchUsesOfficialKakaoCategoryCodes(String categoryCode) {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://dapi.kakao.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KakaoLocalService kakaoLocalService = new KakaoLocalService("test-key", builder.build());

        server.expect(request -> {
                    assertThat(request.getURI().getPath()).isEqualTo("/v2/local/search/category.json");
                    String query = request.getURI().getRawQuery();
                    assertThat(query).contains("category_group_code=" + categoryCode);
                    assertThat(query).contains("y=37.4979");
                    assertThat(query).contains("x=127.0276");
                    assertThat(query).contains("radius=1000");
                    assertThat(query).contains("page=1");
                    assertThat(query).contains("size=10");
                })
                .andExpect(header("Authorization", "KakaoAK test-key"))
                .andRespond(withSuccess("{\"documents\":[]}", MediaType.APPLICATION_JSON));

        assertThat(kakaoLocalService.searchByCategory(PlaceSearchRequest.builder()
                .category(categoryCode.toLowerCase())
                .latitude(37.4979)
                .longitude(127.0276)
                .radius(1000)
                .build())).isEmpty();

        server.verify();
    }

    @Test
    void categorySearchAcceptsKoreanCategoryLabel() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://dapi.kakao.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KakaoLocalService kakaoLocalService = new KakaoLocalService("test-key", builder.build());

        server.expect(request -> {
                    assertThat(request.getURI().getPath()).isEqualTo("/v2/local/search/category.json");
                    assertThat(request.getURI().getRawQuery()).contains("category_group_code=MT1");
                })
                .andExpect(header("Authorization", "KakaoAK test-key"))
                .andRespond(withSuccess("{\"documents\":[]}", MediaType.APPLICATION_JSON));

        assertThat(kakaoLocalService.searchByCategory(PlaceSearchRequest.builder()
                .category("\uB300\uD615\uB9C8\uD2B8")
                .latitude(37.4979)
                .longitude(127.0276)
                .radius(1000)
                .build())).isEmpty();

        server.verify();
    }

    @ParameterizedTest
    @MethodSource("removedKeywordCategories")
    void categorySearchReturnsEmptyForRemovedKeywordCategories(String category) {
        KakaoLocalService kakaoLocalService = new KakaoLocalService("test-key", RestClient.builder().build());

        assertThat(kakaoLocalService.searchByCategory(PlaceSearchRequest.builder()
                .category(category)
                .latitude(37.4979)
                .longitude(127.0276)
                .radius(1000)
                .build())).isEmpty();
    }

    private static Stream<String> supportedKakaoCategoryCodes() {
        return Stream.of(
                "MT1",
                "CS2",
                "PS3",
                "SC4",
                "AC5",
                "PK6",
                "OL7",
                "SW8",
                "BK9",
                "CT1",
                "AG2",
                "PO3",
                "AT4",
                "AD5",
                "FD6",
                "CE7",
                "HP8",
                "PM9"
        );
    }

    private static Stream<String> removedKeywordCategories() {
        return Stream.of("library", "park");
    }
}
