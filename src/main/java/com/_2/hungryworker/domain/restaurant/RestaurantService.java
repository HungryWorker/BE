package com._2.hungryworker.domain.restaurant;

import com._2.hungryworker.domain.restaurant.dto.RestaurantCreateRequest;
import com._2.hungryworker.domain.restaurant.dto.RestaurantCreateRequest.MenuItem;
import com._2.hungryworker.domain.restaurant.dto.RestaurantDetailResponse;
import com._2.hungryworker.global.file.FileStorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * RestaurantCreatePopup(FE)의 "등록" 버튼 처리를 담당한다.
 * 카테고리/태그는 Category/Tag 테이블을 재사용(없으면 새로 생성)하고,
 * 메뉴는 JSON 문자열로 받아 파싱하며, 사진은 로컬 디스크에 저장한다.
 */
@Service
@Transactional(readOnly = true)
public class RestaurantService {

    private static final String RESTAURANT_IMAGE_SUB_DIR = "restaurants";
    private static final int MIN_PHOTO_COUNT = 2;

    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RestaurantService(RestaurantRepository restaurantRepository,
                             CategoryRepository categoryRepository,
                             TagRepository tagRepository,
                             FileStorageService fileStorageService) {
        this.restaurantRepository = restaurantRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public RestaurantDetailResponse registerRestaurant(RestaurantCreateRequest request,
                                                       List<MultipartFile> photos) {
        List<MultipartFile> validPhotos = filterValidPhotos(photos);
        if (validPhotos.size() < MIN_PHOTO_COUNT) {
            throw new IllegalArgumentException("사진을 최소 " + MIN_PHOTO_COUNT + "장 업로드해주세요.");
        }

        List<MenuItem> menuItems = parseMenus(request.menusJson());
        if (menuItems.isEmpty()) {
            throw new IllegalArgumentException("메뉴를 최소 1개 이상 입력해주세요.");
        }

        Restaurant restaurant = findOrCreateRestaurant(request);

        restaurant.updateBreakTime(normalize(request.breakTime()));

        addCategories(restaurant, request.categories());
        addMenus(restaurant, menuItems);
        addTags(restaurant, request.tags());
        attachPhotos(restaurant, validPhotos);

        Restaurant saved = restaurantRepository.save(restaurant);
        return RestaurantDetailResponse.from(saved);
    }

    private Restaurant findOrCreateRestaurant(RestaurantCreateRequest request) {
        if (request.restaurantId() != null) {
            return restaurantRepository.findById(request.restaurantId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "존재하지 않는 식당입니다. id=" + request.restaurantId()));
        }

        if (!StringUtils.hasText(request.name())) {
            throw new IllegalArgumentException("식당 이름을 입력해주세요.");
        }
        if (request.latitude() == null || request.longitude() == null) {
            throw new IllegalArgumentException("식당 위치(위도/경도)를 확인할 수 없습니다.");
        }

        Restaurant restaurant = Restaurant.builder()
                // 검색을 거치지 않고 직접 등록하는 식당은 Google Place ID가 없으므로
                // (google_place_id는 not-null unique) 임시 식별자를 만들어 채운다.
                .googlePlaceId("manual-" + UUID.randomUUID())
                .name(request.name().trim())
                .address(request.address())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build();

        return restaurantRepository.save(restaurant);
    }

    private void addCategories(Restaurant restaurant, List<String> categories) {
        if (categories == null) {
            return;
        }
        for (String rawName : categories) {
            String name = normalize(rawName);
            if (name == null) {
                continue;
            }
            Category category = categoryRepository.findByName(name)
                    .orElseGet(() -> categoryRepository.save(Category.builder().name(name).build()));
            restaurant.addCategory(category);
        }
    }

    private void addTags(Restaurant restaurant, List<String> tags) {
        if (tags == null) {
            return;
        }
        for (String rawName : tags) {
            String name = normalize(rawName);
            if (name == null) {
                continue;
            }
            Tag tag = tagRepository.findByName(name)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(name).build()));
            restaurant.addTag(tag);
        }
    }

    private void addMenus(Restaurant restaurant, List<MenuItem> menuItems) {
        for (MenuItem item : menuItems) {
            restaurant.addMenu(item.name().trim(), item.price());
        }
    }

    private void attachPhotos(Restaurant restaurant, List<MultipartFile> photos) {
        for (MultipartFile photo : photos) {
            String storedUrl = fileStorageService.store(photo, RESTAURANT_IMAGE_SUB_DIR);
            restaurant.addImage(storedUrl);
        }
    }

    private List<MultipartFile> filterValidPhotos(List<MultipartFile> photos) {
        if (photos == null) {
            return List.of();
        }
        return photos.stream()
                .filter(photo -> photo != null && !photo.isEmpty())
                .toList();
    }

    private List<MenuItem> parseMenus(String menusJson) {
        if (!StringUtils.hasText(menusJson)) {
            return List.of();
        }
        try {
            List<MenuItem> items = objectMapper.readValue(menusJson, new TypeReference<List<MenuItem>>() {
            });
            return items.stream()
                    .filter(item -> item != null
                            && StringUtils.hasText(item.name())
                            && item.price() != null
                            && item.price() > 0)
                    .toList();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("메뉴 형식이 올바르지 않습니다.");
        }
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}