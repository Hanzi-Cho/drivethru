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

## 6. 다음 할 일

### 6.1 최우선

- `feature/customui/` 패키지 신설
- `DriveThruActivity` 생성
- Compose 기반 custom UI shell 구성

### 6.2 차량 상태 계층

- `VehicleSignalProvider` 인터페이스 정의
- `CarPropertyVehicleSignalProvider` 초안 작성
- `VehicleSignalSnapshot`을 기반으로 `PARK`, `STOPPED`, `MOVING` 추상화

### 6.3 매장 접근 계층

- `StoreResolver` 인터페이스 정의
- geofence/proximity/debug event로부터 `storeId`, `capabilities`, `menuSource` 해석
- 어떤 매장 주문 기능을 열지 결정하는 정책 문서화

### 6.4 세션/안전 정책

- `OrderingSessionController` 정의
- `StopStatePolicy` 정의
- 주문 중 주행 시작 시:
  - 앱을 바로 종료하지 않음
  - 세션과 draft 유지
  - 제한 상태 또는 백그라운드 상태로 전환
  - 안전 상태 복귀 시 resume 또는 close

### 6.5 시연 연결

- 왼쪽 시뮬레이터와 오른쪽 AAOS 앱 이벤트 브리지 정리
- `EntryDetected`, `ExitDetected`, `GearChanged`, `MotionChanged` 디버그 입력 경로 구성
- 최종 영상용 상태 라벨, 로그, 전환 흐름 정리

## 7. 권장 다음 구현 순서

1. `DriveThruActivity`와 `feature/customui` shell 생성
2. `VehicleSignalProvider`, `StoreResolver`, `OrderingSessionController`, `StopStatePolicy` 인터페이스 정의
3. 현재 `Car App Library` 프로토타입과 Custom UI 경로를 공존시키는 app shell 정리
4. STOP STATE 세션 보존 정책을 코드에 먼저 반영
5. 이후 geofence/debug bridge 연결

## 8. 한 줄 결론

현재 프로젝트는 `Car App Library`로 구조를 검증하는 단계까지 왔고, 다음 핵심 단계는 `Custom UI + Vehicle/Store/Session state architecture`를 실제 코드 구조로 옮기는 것이다.
