package com._2.hungryworker.domain.restaurant.client;

import com._2.hungryworker.domain.restaurant.client.dto.GoogleTextSearchRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GooglePlacesClient {

    private static final String PLACES_API_URL =
            "https://places.googleapis.com/v1/places:searchText";

    private final RestClient restClient;
    private final String apiKey;

    public GooglePlacesClient(
            @Value("${google.maps.api-key}") String apiKey
    ) {
        this.restClient = RestClient.create();
        this.apiKey = apiKey;
    }

    public GoogleTextSearchResponse searchText(
            String keyword,
            Double lat,
            Double lng,
            Integer radiusMeters
    ) {
        GoogleTextSearchRequest request =
                GoogleTextSearchRequest.of(keyword, lat, lng, radiusMeters);

        return restClient.post()
                .uri(PLACES_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Goog-Api-Key", apiKey)
                .header(
                        "X-Goog-FieldMask",
                        "places.id,places.displayName,places.formattedAddress,places.location"
                )
                .body(request)
                .retrieve()
                .body(GoogleTextSearchResponse.class);
    }

    public record GoogleTextSearchResponse(
            List<Place> places
    ) {
    }

    public record Place(
            String id,
            DisplayName displayName,
            String formattedAddress,
            Location location
    ) {
    }

    public record DisplayName(
            String text
    ) {
    }

    public record Location(
            double latitude,
            double longitude
    ) {
    }
}