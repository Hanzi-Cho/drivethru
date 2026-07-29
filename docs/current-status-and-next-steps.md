# DriveThru Current Status And Next Steps

## 1. 문서 목적

이 문서는 `car/drivethru` 프로젝트의 현재 진행 상태, 이미 결정된 방향, 남은 구현 작업을 짧게 정리해 다음 작업 시작점을 고정하기 위한 기록이다.

작성 기준일:
- `2026-07-29`

## 2. 현재 프로젝트 상태

현재 프로젝트는 두 층으로 나뉘어 진행 중이다.

1. 현재 구현 층
   - `Car App Library` 기반 AAOS 프로토타입
   - 4개 메인 destination(`Menu`, `Cart`, `Order`, `Setting`) 구조 검증
   - 상태 저장소, 가짜 데이터소스, 템플릿 렌더러 분리
2. 목표 구현 층
   - `AAOS Activity + Compose Custom UI`
   - OEM 스타일의 자유 레이아웃 주문 경험
   - 안전 상태에서만 full ordering UI 실행
   - 주행 시작 시 `STOP_STATE`로 세션 보존

그리고 장기적으로는 아래 플랫폼 확장 층까지 올라가는 것이 목표다.

3. 플랫폼 확장 층
   - `CarPropertyManager -> Car Service -> VHAL -> Fake Vehicle Hardware -> ECU Simulator`
   - 앱 개발을 넘어 Binder IPC, Framework API, 시스템 서비스, 차량 신호 모델링까지 다루는 구조

## 3. 현재까지 완료된 것

### 3.1 앱 구조

- `CarAppService`, `Session`, root tab screen 기반 기본 진입 구조 구성
- `DriveThruStateStore` 중심의 단방향 상태 흐름 구성
- `Menu`, `Cart`, `Order`, `Setting` 화면을 템플릿 렌더러로 분리
- 메뉴/결제수단/설정/상태 데이터를 fake repository로 분리

### 3.2 UI 및 디자인 시스템

- `design-system/` 경로 신설
- 요구사항, 컴포넌트, 토큰, 레이아웃 스펙 문서화
- 첨부 시안 PNG를 `design-system/reference-images/`에 보관
- Car App Library 제약과 Custom UI 목표를 문서상에서 분리 정리

### 3.3 시나리오 및 문서

- 포트폴리오 영상 목표 문서 작성
- `Car App Library`는 현재 프로토타입이고, 최종 목표는 `Custom UI`라는 방향 반영
- GPS/매장 해석/차량 상태/세션 보존/`STOP_STATE` 정책을 프로젝트 문서에 반영

### 3.4 안정성 수정

- 빈 장바구니 상태에서 `Cart` 접근 시 empty-state 템플릿으로 분기하도록 수정
- 최근 기준 `:app:automotive:assembleDebug` 빌드 성공 확인

## 4. 현재 남아 있는 핵심 한계

### 4.1 Car App Library 한계

- 현재 `Menu` 화면은 텍스트 중심 리스트 구조 이상으로 깊게 커스터마이즈할 수 없다
- 카드 배치, 자유 레이아웃, 상세 패딩/라운드, 브랜드형 시안 재현은 사실상 불가능하다
- 따라서 첨부 시안 수준의 구현은 `Custom UI`로 가야 한다

### 4.2 플랫폼 권한 한계

- 일반 앱 수준에서는 다른 앱을 마음대로 내리거나 강제 종료하는 시나리오는 보장되지 않는다
- 일반 앱 수준에서는 VHAL 주입과 일부 차량 상태 제어가 제한된다
- 최종 시나리오는 OEM-style 포트폴리오 방향으로 설명해야 한다

### 4.3 현재 깊이의 한계

- 아직은 `Car App Library`와 앱 상태 관리 중심이라서 플랫폼 레이어 실력이 충분히 드러나지 않는다
- 현재 구조만으로는 "AAOS 앱 개발"은 보여줄 수 있어도 "차량 프레임워크/서비스 설계"까지는 보여주기 어렵다
- 포트폴리오 깊이를 키우려면 `CarPropertyManager` 사용을 넘어 `Fake VHAL`, `vendor property`, `custom manager/service` 단계로 확장해야 한다

## 5. 현재 아키텍처 판단

이 프로젝트에서 중요한 것은 "UI를 예쁘게 그리는 것"보다 아래를 구조적으로 설명하는 것이다.

- 어떤 매장 근처에 접근했는가
- 어떤 매장의 주문 기능을 열어야 하는가
- 차량이 현재 full ordering UI를 보여줘도 안전한 상태인가
- 주문 중 주행이 시작되면 어떻게 안전하게 세션을 보존하는가

핵심 아키텍처 축:
- `VehicleSignalProvider`
- `StoreResolver`
- `OrderingSessionController`
- `StopStatePolicy`

그리고 장기 확장 관점의 핵심 축은 아래다.

- `CarPropertyVehicleSignalProvider`
- `Vendor DriveThru Properties`
- `DriveThruManager`
- `DriveThruService`
- `FakeVehicleHardware`
- `Python ECU Simulator`

## 6. 목표 구조 재정의

현재는 대략 아래 수준이다.

```text
DriveThru App
  └─ GPS / Geofence / Compose UI
```

장기 목표는 아래 구조다.

```text
DriveThru App
    │
DriveThruManager / CarPropertyManager
    │
DriveThruService or Custom Car Service
    │
Vehicle HAL
    │
Fake Vehicle Hardware
    │
CAN / ECU Simulator
```

이 구조가 되면 단순 앱이 아니라 아래 역량을 같이 보여줄 수 있다.

- 앱 개발
- Binder IPC 이해
- Framework API 설계
- Car Service 수정
- VHAL 구현
- 차량 신호 모델링
- 권한 / SELinux 이해
- 시스템 이미지 빌드 및 테스트

## 7. 현실적인 발전 단계

### 7.1 단계 1: 앱에서 표준 차량 속성 사용

먼저 앱이 아래 표준 차량 상태를 `CarPropertyManager`로 읽게 한다.

- 차량 속도
- 기어 상태
- 주차 브레이크
- 점화 상태
- 문 상태

핵심 산출물:
- `VehicleState` 도메인 모델
- `DriveThruSafetyPolicy`
- `canShowOrderingUi()` 같은 안전 노출 정책 함수

이 단계의 포지션:
- "Car API를 사용하는 앱 개발자" 수준

### 7.2 단계 2: Fake VHAL 시나리오 추가

여기부터 플랫폼 프로젝트 색깔이 생긴다.

예시 시나리오:
- 주행 중: `speed=50`, `gear=D`, `parkingBrake=false`
- 드라이브스루 진입: `speed=5`, `gear=D`
- 주문 위치 정차: `speed=0`, `gear=D`, `parkingBrake=false`
- 안전 주문 상태: `speed=0`, `gear=P`, `parkingBrake=true`

검증하고 싶은 전체 흐름:

```text
FakeVehicleHardware
  -> VHAL
  -> Car Service
  -> CarPropertyManager callback
  -> App state change
  -> UI restriction change
```

이 단계의 핵심 메시지:
- 앱에서 값을 흉내내는 게 아니라 `Fake VHAL`에서 신호를 발생시켜 end-to-end로 검증한다

### 7.3 단계 3: Vendor Property 추가

여기가 이 프로젝트의 강한 발전 지점이다.

후보 속성:
- `VENDOR_DRIVE_THRU_ZONE`
- `VENDOR_DRIVE_THRU_ORDER_STATE`
- `VENDOR_VEHICLE_HEIGHT`
- `VENDOR_PICKUP_WINDOW_SIDE`

예시 zone:
- `0 = OUTSIDE`
- `1 = ENTRANCE`
- `2 = MENU_BOARD`
- `3 = PAYMENT_WINDOW`
- `4 = PICKUP_WINDOW`

이 단계에서 할 일:
- vendor property ID 정의
- 타입 / 접근 모드 / subscribe 여부 정의
- FakeVehicleHardware 저장 및 이벤트 구현
- 앱 또는 manager에서 구독

중요한 판단:
- vendor property는 "무조건 많이 만들기"보다 도메인 API 필요성이 있는 경우에만 추가해야 한다

### 7.4 단계 4: Custom Manager와 Car Service 추가

앱이 raw property ID를 직접 다루지 않게 하는 단계다.

목표:
- `DriveThruManager`
- `DriveThruService`
- 도메인 수준 API 제공

예:
- `getCurrentZone()`
- `registerZoneCallback()`
- `isOrderingAllowed()`

이 단계가 중요한 이유:
- 여러 속성을 한 도메인 정책으로 중앙 관리 가능
- 여러 앱이 같은 정책을 재구현하지 않아도 됨
- raw vendor property 노출을 줄일 수 있음

Custom Manager가 타당한 조건:
- 여러 VHAL 속성을 조합해야 함
- OEM 정책을 중앙에서 적용해야 함
- 여러 앱이 같은 기능을 사용함
- 비동기 이벤트와 생명주기 관리가 필요함
- 별도 권한 체계가 필요함

### 7.5 단계 5: 하드웨어 시뮬레이터 연결

Fake VHAL 값을 개발자 UI에서 손으로만 바꾸지 않고, 별도 ECU simulator와 연결하는 단계다.

권장 구조:

```text
Python ECU Simulator
    │ TCP / Unix Socket / gRPC
    ▼
Custom Vehicle Hardware
    │
DefaultVehicleHal
    ▼
Car Service
    ▼
DriveThru App
```

이 단계의 가치:
- 기존 Android ↔ 장비/FW/HW 협업 경험을 자동차 플랫폼 구조로 연결할 수 있다

### 7.6 단계 6: 권한, SELinux, 테스트

플랫폼 개발자로 보이려면 기능 구현만으로는 부족하다.

포함해야 할 것:
- signature / privileged permission
- 서비스 권한 검사
- SELinux 접근 통제
- VHAL / Service / E2E 테스트
- 잘못된 타입, area ID, callback lifecycle, reconnect, recovery 케이스 검증

이 단계의 메시지:
- "동작하는 기능"을 넘어 "통제되고 검증된 차량 서비스"를 만들었다

## 8. 다음 할 일

### 8.1 최우선

- `feature/customui/` 패키지 신설
- `DriveThruActivity` 생성
- Compose 기반 custom UI shell 구성

### 8.2 차량 상태 계층

- `VehicleSignalProvider` 인터페이스 정의
- `CarPropertyVehicleSignalProvider` 초안 작성
- `VehicleSignalSnapshot`을 기반으로 `PARK`, `STOPPED`, `MOVING` 추상화

### 8.3 매장 접근 계층

- `StoreResolver` 인터페이스 정의
- geofence/proximity/debug event로부터 `storeId`, `capabilities`, `menuSource` 해석
- 어떤 매장 주문 기능을 열지 결정하는 정책 문서화

### 8.4 세션/안전 정책

- `OrderingSessionController` 정의
- `StopStatePolicy` 정의
- 주문 중 주행 시작 시:
  - 앱을 바로 종료하지 않음
  - 세션과 draft 유지
  - 제한 상태 또는 백그라운드 상태로 전환
  - 안전 상태 복귀 시 resume 또는 close

### 8.5 플랫폼 확장 준비

- `VehicleState` 도메인 모델 정식 정의
- `DriveThruSafetyPolicy` 도입
- 표준 property 목록과 사용 목적 문서화
- Fake VHAL 확장 포인트 조사
- vendor property 후보 목록 구체화

### 8.6 시연 연결

- 왼쪽 시뮬레이터와 오른쪽 AAOS 앱 이벤트 브리지 정리
- `EntryDetected`, `ExitDetected`, `GearChanged`, `MotionChanged` 디버그 입력 경로 구성
- 최종 영상용 상태 라벨, 로그, 전환 흐름 정리

## 9. 권장 다음 구현 순서

1. `DriveThruActivity`와 `feature/customui` shell 생성
2. `VehicleSignalProvider`, `StoreResolver`, `OrderingSessionController`, `StopStatePolicy` 인터페이스 정의
3. 현재 `Car App Library` 프로토타입과 Custom UI 경로를 공존시키는 app shell 정리
4. STOP STATE 세션 보존 정책을 코드에 먼저 반영
5. `DriveThruSafetyPolicy`와 표준 property 읽기 구조 반영
6. 이후 geofence/debug bridge 연결
7. Fake VHAL 확장 가능성 검토 후 플랫폼 브랜치 시작
8. vendor property와 custom manager는 Fake VHAL 단계 성공 후 진입

## 10. 추천 프로젝트 범위

### 최소 완성본

1. AAOS 앱 + Custom UI shell
2. 표준 차량 속성 읽기
3. 안전 정책 분리
4. geofence/store resolver
5. STOP_STATE 세션 보존
6. 디버그 시뮬레이터 연동

### 강한 포트폴리오 버전

1. Fake VHAL 차량 시나리오
2. vendor property 1~3개
3. `DriveThruManager`
4. `DriveThruService`
5. Binder callback
6. signature permission
7. Python ECU simulator
8. VHAL/Service/E2E 테스트

## 11. 한 줄 결론

현재 프로젝트는 `Car App Library`로 구조를 검증하는 단계까지 왔고, 다음 핵심 단계는 `Custom UI + Vehicle/Store/Session architecture`를 구현한 뒤, 장기적으로 `Fake VHAL + vendor property + custom manager/service`까지 확장해 차량 플랫폼 프로젝트로 발전시키는 것이다.
