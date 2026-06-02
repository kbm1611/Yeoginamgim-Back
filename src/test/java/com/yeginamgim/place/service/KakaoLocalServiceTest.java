package com.yeginamgim.place.service;

import com.yeginamgim.place.dto.request.PlaceSearchRequest;
import com.yeginamgim.global.exception.KakaoLocalApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
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
}
