# 공통 API 응답 및 예외 처리 가이드

## 1. 목적

이 프로젝트의 REST API는 성공과 실패 여부에 관계없이 가능한 한 동일한 JSON 구조를 사용한다.

```json
{
  "code": "OK",
  "message": "요청을 성공적으로 처리했습니다.",
  "data": {}
}
```

각 필드의 의미는 다음과 같다.

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `code` | `String` | 실제 HTTP 상태의 이름이다. 예: `OK`, `CREATED`, `BAD_REQUEST`, `NOT_FOUND` |
| `message` | `String` | 클라이언트에 전달할 처리 결과 메시지이다. |
| `data` | 제네릭 또는 `null` | 성공 시 응답 데이터이며, 실패 시에는 `null`이다. |

`code`는 별도의 비즈니스 코드가 아니라 `HttpStatus.name()` 값이다. 실제 HTTP 상태와 본문의 `code`가 항상 같도록 공통 팩토리 메서드를 사용한다.

## 2. 관련 파일

| 파일 | 역할 |
| --- | --- |
| `../src/main/java/global/dto/response/ApiResponse.java` | 공통 응답 형식과 성공·실패 응답 생성 메서드 |
| `../src/main/java/global/exception/GlobalExceptionHandler.java` | Controller 계층에서 발생한 예외를 공통 오류 응답으로 변환 |
| `../src/main/java/com/example/meditation/MeditationApplication.java` | `global` 패키지를 컴포넌트 스캔 대상으로 등록 |
| 각 Controller | 성공 시 `ApiResponse.success(...)` 사용 |
| 각 Service | 처리할 수 없는 상황에서 적절한 예외 발생 |

## 3. 공통 성공 응답 사용법

Controller는 `ResponseEntity.ok(...)`나 `new ApiResponse<>(...)`를 직접 작성하지 않고 `ApiResponse.success(...)`를 사용한다.

### 3.1 조회 성공: 200 OK

```java
@GetMapping
public ResponseEntity<ApiResponse<List<FacilitySummaryResponse>>> getFacilities(
        @RequestParam(required = false) FacilityCategory category
) {
    return ApiResponse.success(
            HttpStatus.OK,
            "편의시설 목록 조회에 성공했습니다.",
            facilityService.getFacilities(category)
    );
}
```

실제 HTTP 응답:

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "code": "OK",
  "message": "편의시설 목록 조회에 성공했습니다.",
  "data": [
    {
      "facilityId": 1,
      "name": "명상실",
      "category": "WELLNESS",
      "description": "조용한 명상 공간",
      "imageUrl": "/images/meditation-room.jpg"
    }
  ]
}
```

목록 조회 결과가 없으면 `data`는 `null` 대신 빈 배열을 반환한다.

```json
{
  "code": "OK",
  "message": "편의시설 목록 조회에 성공했습니다.",
  "data": []
}
```

### 3.2 생성 성공: 201 Created

```java
@PostMapping
public ResponseEntity<ApiResponse<FacilityResponse>> createFacility(
        @Valid @RequestBody FacilityCreateRequest request
) {
    FacilityResponse response = facilityService.createFacility(request);

    return ApiResponse.success(
            HttpStatus.CREATED,
            "편의시설 생성에 성공했습니다.",
            response
    );
}
```

응답 예시:

```http
HTTP/1.1 201 Created
```

```json
{
  "code": "CREATED",
  "message": "편의시설 생성에 성공했습니다.",
  "data": {
    "facilityId": 10,
    "name": "요가실"
  }
}
```

### 3.3 수정 성공: 200 OK

수정된 데이터를 본문으로 반환하면 `200 OK`를 사용한다.

```java
return ApiResponse.success(
        HttpStatus.OK,
        "편의시설 수정에 성공했습니다.",
        facilityService.updateFacility(facilityId, request)
);
```

### 3.4 삭제 성공

현재 `ApiResponse` 구조로 메시지를 반환하려면 `200 OK`를 사용한다.

```java
facilityService.deleteFacility(facilityId);

return ApiResponse.success(
        HttpStatus.OK,
        "편의시설 삭제에 성공했습니다.",
        null
);
```

`204 No Content`는 HTTP 규칙상 응답 본문을 보내지 않는 상태이므로 `ApiResponse` 본문을 함께 반환하지 않는다.

```java
facilityService.deleteFacility(facilityId);
return ResponseEntity.noContent().build();
```

프로젝트에서 삭제 응답을 어떤 방식으로 사용할지는 API 설계 시 하나로 통일한다.

## 4. 성공 응답 작성 규칙

1. 실제 HTTP 상태와 본문의 `code`를 따로 만들지 않는다.
2. 반드시 같은 `HttpStatus`를 처리하는 `ApiResponse.success(...)`를 사용한다.
3. Controller는 요청과 응답 조립만 담당하고 비즈니스 로직은 Service에 둔다.
4. JPA Entity를 API 응답으로 직접 반환하지 않고 Response DTO로 변환한다.
5. 목록 조회 결과가 없으면 빈 목록을 반환한다.
6. 응답 메시지는 사용자가 이해할 수 있는 완전한 문장으로 작성한다.
7. Java 소스 파일 인코딩은 UTF-8로 유지한다.

다음과 같이 실제 상태와 본문 코드를 따로 작성하지 않는다.

```java
// 사용 금지: 실제 상태와 body의 의미가 서로 달라질 수 있다.
return ResponseEntity.ok(
        new ApiResponse<>("CREATED", "생성 성공", response)
);
```

## 5. 공통 예외 처리 흐름

오류 처리 흐름은 다음과 같다.

```text
HTTP 요청
→ Controller
→ Service
→ 예외 발생
→ GlobalExceptionHandler가 예외 감지
→ ApiResponse.error(status, message)
→ 동일한 HTTP 상태와 code를 가진 JSON 반환
```

Controller에서 모든 예외를 `try-catch`로 감쌀 필요가 없다. Service는 의미 있는 예외를 던지고, `GlobalExceptionHandler`가 이를 공통 형식으로 변환한다.

## 6. 현재 처리하는 예외

| 예외 | HTTP 상태 | 발생 사례 |
| --- | --- | --- |
| `ResponseStatusException` | 예외에 지정한 상태 | 존재하지 않는 리소스 조회 등 |
| `MethodArgumentNotValidException` | `400 BAD_REQUEST` | `@Valid` DTO 검증 실패 |
| `MethodArgumentTypeMismatchException` | `400 BAD_REQUEST` | enum, 숫자, 날짜 등의 파라미터 변환 실패 |
| `MissingServletRequestParameterException` | `400 BAD_REQUEST` | 필수 요청 파라미터 누락 |
| `HttpMessageNotReadableException` | `400 BAD_REQUEST` | 잘못된 JSON 문법 또는 enum 값 |
| `IllegalArgumentException` | `400 BAD_REQUEST` | 잘못된 메서드 인자 또는 비즈니스 입력 |
| 그 밖의 `Exception` | `500 INTERNAL_SERVER_ERROR` | 예상하지 못한 서버 오류 |

예외 응답에서 `data`는 항상 `null`이다.

```json
{
  "code": "BAD_REQUEST",
  "message": "Invalid value for parameter 'category': INVALID",
  "data": null
}
```

## 7. Service에서 예외 발생시키기

### 7.1 리소스를 찾지 못한 경우

현재 프로젝트에서는 `ResponseStatusException`을 사용할 수 있다.

```java
public RoomDetailResponse getRoom(Long roomId) {
    Room room = roomRepository.findDetailById(roomId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "객실을 찾을 수 없습니다: " + roomId
            ));

    return toDetailResponse(room);
}
```

응답 예시:

```http
HTTP/1.1 404 Not Found
```

```json
{
  "code": "NOT_FOUND",
  "message": "객실을 찾을 수 없습니다: 999",
  "data": null
}
```

### 7.2 잘못된 입력인 경우

```java
if (request.maxGuests() < request.standardGuests()) {
    throw new IllegalArgumentException(
            "최대 인원은 기준 인원보다 작을 수 없습니다."
    );
}
```

현재 `IllegalArgumentException`은 모두 `400 BAD_REQUEST`로 처리된다. 존재하지 않는 데이터에 `IllegalArgumentException`을 사용하면 400이 되므로, 404가 필요하면 `ResponseStatusException(HttpStatus.NOT_FOUND, ...)`을 사용한다.

### 7.3 예외를 무시하지 않기

다음처럼 모든 예외를 잡아서 성공 응답으로 바꾸지 않는다.

```java
// 사용 금지
try {
    return facilityService.getFacility(facilityId);
} catch (Exception exception) {
    return null;
}
```

처리할 수 없는 예외는 공통 예외 처리기까지 전달한다.

## 8. DTO 검증 사용법

요청 DTO에는 Jakarta Validation 애노테이션을 선언한다.

```java
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FacilityCreateRequest(
        @NotBlank(message = "편의시설 이름은 필수입니다.")
        @Size(max = 100, message = "편의시설 이름은 100자 이하여야 합니다.")
        String name,

        @NotNull(message = "편의시설 카테고리는 필수입니다.")
        FacilityCategory category,

        @Size(max = 255, message = "설명은 255자 이하여야 합니다.")
        String description
) {}
```

Controller의 `@RequestBody` 앞에는 반드시 `@Valid`를 붙인다.

```java
@PostMapping
public ResponseEntity<ApiResponse<FacilityResponse>> createFacility(
        @Valid @RequestBody FacilityCreateRequest request
) {
    return ApiResponse.success(
            HttpStatus.CREATED,
            "편의시설 생성에 성공했습니다.",
            facilityService.createFacility(request)
    );
}
```

검증 실패 응답 예시:

```json
{
  "code": "BAD_REQUEST",
  "message": "name: 편의시설 이름은 필수입니다., category: 편의시설 카테고리는 필수입니다.",
  "data": null
}
```

## 9. 요청 파라미터 변환 오류

다음 API는 `category` 문자열을 `FacilityCategory` enum으로 변환한다.

```java
@RequestParam(required = false) FacilityCategory category
```

정상 요청:

```http
GET /api/facilities?category=WELLNESS
```

잘못된 요청:

```http
GET /api/facilities?category=INVALID
```

잘못된 enum 값은 `MethodArgumentTypeMismatchException`으로 처리되어 다음 응답을 반환한다.

```json
{
  "code": "BAD_REQUEST",
  "message": "Invalid value for parameter 'category': INVALID",
  "data": null
}
```

## 10. 새로운 예외를 공통 처리기에 추가하는 방법

도메인별 예외가 필요하면 전용 예외 클래스를 만들고 `GlobalExceptionHandler`에 정확한 핸들러를 추가한다.

예외 클래스 예시:

```java
package room.exception;

public class FacilityNotFoundException extends RuntimeException {

    public FacilityNotFoundException(Long facilityId) {
        super("편의시설을 찾을 수 없습니다: " + facilityId);
    }
}
```

Service 사용 예시:

```java
Facility facility = facilityRepository.findById(facilityId)
        .orElseThrow(() -> new FacilityNotFoundException(facilityId));
```

공통 처리기 등록 예시:

```java
@ExceptionHandler(FacilityNotFoundException.class)
public ResponseEntity<ApiResponse<Void>> handleFacilityNotFoundException(
        FacilityNotFoundException exception
) {
    return ApiResponse.error(HttpStatus.NOT_FOUND, exception.getMessage());
}
```

구체적인 예외 핸들러는 마지막의 `Exception.class` 핸들러보다 우선 적용된다.

## 11. 예외 메시지 작성 규칙

1. 클라이언트가 문제를 해결하는 데 필요한 내용만 제공한다.
2. DB 쿼리, 테이블명, 파일 경로, 스택 트레이스를 응답에 노출하지 않는다.
3. 비밀번호, 토큰, 개인정보를 메시지에 포함하지 않는다.
4. 예상하지 못한 500 오류에는 내부 예외 메시지를 그대로 반환하지 않는다.
5. 개발자가 필요한 상세 원인은 서버 로그에 기록한다.

현재 알 수 없는 예외는 다음 고정 메시지를 반환한다.

```json
{
  "code": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred.",
  "data": null
}
```

## 12. Spring Security 오류 주의사항

`GlobalExceptionHandler`는 Spring MVC의 Controller 처리 과정에서 발생한 예외를 담당한다. Spring Security 필터에서 Controller 진입 전에 발생하는 인증·인가 오류는 현재 공통 처리 대상이 아니다.

- 인증되지 않은 요청: 일반적으로 `401 Unauthorized`
- 권한이 부족한 요청: 일반적으로 `403 Forbidden`

401/403 응답까지 `ApiResponse` 구조로 통일하려면 Spring Security의 `AuthenticationEntryPoint`와 `AccessDeniedHandler`를 별도로 구현해야 한다. Controller에서 Security 예외를 `try-catch`로 처리하지 않는다.

## 13. 새 API 작성 체크리스트

- [ ] Controller 반환 타입이 `ResponseEntity<ApiResponse<T>>`인가?
- [ ] 성공 응답에 `ApiResponse.success(HttpStatus, message, data)`를 사용했는가?
- [ ] 실제 HTTP 의미에 맞는 상태를 선택했는가?
- [ ] Entity 대신 Response DTO를 반환하는가?
- [ ] 요청 DTO에 필요한 Validation 애노테이션을 선언했는가?
- [ ] Controller의 요청 DTO에 `@Valid`를 붙였는가?
- [ ] 리소스가 없을 때 404 예외를 던지는가?
- [ ] 잘못된 입력은 400으로 처리되는가?
- [ ] 목록 결과가 없을 때 빈 배열을 반환하는가?
- [ ] 응답 메시지에 내부 구현이나 민감정보가 노출되지 않는가?
- [ ] 성공과 오류 사례를 Postman 또는 테스트 코드로 확인했는가?

## 14. 요약 예제

```java
@RestController
@RequestMapping("/api/facilities")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    @GetMapping("/{facilityId}")
    public ResponseEntity<ApiResponse<FacilityResponse>> getFacility(
            @PathVariable Long facilityId
    ) {
        return ApiResponse.success(
                HttpStatus.OK,
                "편의시설 조회에 성공했습니다.",
                facilityService.getFacility(facilityId)
        );
    }
}
```

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FacilityService {

    private final FacilityRepository facilityRepository;

    public FacilityResponse getFacility(Long facilityId) {
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "편의시설을 찾을 수 없습니다: " + facilityId
                ));

        return FacilityResponse.from(facility);
    }
}
```

성공하면 `200 OK`와 `code: "OK"`가 반환되고, 시설이 없으면 `404 Not Found`와 `code: "NOT_FOUND"`가 반환된다.
