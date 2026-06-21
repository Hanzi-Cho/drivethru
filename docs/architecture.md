# DriveThru IVI Architecture

## 1. 현재 판단

`docs/project-plan.md`의 방향은 좋지만, 지금 바로 Firebase나 Geofence부터 붙이면 샘플 조립 프로젝트처럼 보일 위험이 있습니다.

이 프로젝트의 첫 인상은 아래 두 가지로 결정됩니다.

1. 차량 상태에 따라 화면이 안전하게 분기되는가
2. 기능이 늘어나도 구조가 흔들리지 않는가

그래서 첫 구현 우선순위는 "인프라 연동"보다 "차량 앱 상태 모델과 화면 전환 구조"입니다.

## 2. 권장 저장소 구조

초기에는 모노레포처럼 운영하되, 앱 경계를 분명히 나누는 구성이 가장 좋습니다.

```text
car/drivethru/
  README.md
  docs/
    project-plan.md
    architecture.md
  app/
    automotive/
      app/
      build.gradle.kts
      settings.gradle.kts
      gradle.properties
  dashboard/
    web/
  shared/
    contracts/
      menu.schema.json
      order.schema.json
  tools/
    adb/
      inject-drive-thru.ps1
      inject-gear-park.ps1
      inject-gear-drive.ps1
```

## 3. Android 앱 내부 구조

AAOS 앱은 처음부터 화면 파일 위주가 아니라 상태 중심으로 나누는 편이 낫습니다.

```text
app/automotive/app/src/main/java/.../drivethru/
  app/
    DriveThruCarAppService.kt
    DriveThruSession.kt
  core/
    model/
      DriveThruState.kt
      GearState.kt
      VehicleMotionState.kt
      Store.kt
      MenuItem.kt
      OrderDraft.kt
    state/
      DriveThruStateStore.kt
    navigation/
      DriveThruNavigator.kt
  data/
    vehicle/
      GearStateDataSource.kt
      FakeGearStateDataSource.kt
      CarPropertyGearStateDataSource.kt
    geofence/
      EntryTriggerDataSource.kt
      FakeEntryTriggerDataSource.kt
    menu/
      MenuRepository.kt
      FakeMenuRepository.kt
    order/
      OrderRepository.kt
      FakeOrderRepository.kt
  feature/
    home/
      DriveThruHomeScreen.kt
    menu/
      FullMenuScreen.kt
      SimplifiedMenuScreen.kt
    order/
      OrderReviewScreen.kt
    status/
      WaitingForEntryScreen.kt
      StoreUnavailableScreen.kt
  di/
    AppContainer.kt
```

## 4. 왜 이 구조가 맞는가

- `core/model`, `core/state`를 먼저 두면 Firebase 전환 전에도 앱 플로우를 완성할 수 있습니다.
- `data/*/Fake...`를 먼저 두면 에뮬레이터 데모와 UI 검증이 빨라집니다.
- `CarPropertyManager`, Geofence, Firebase는 모두 바깥 어댑터로 밀어낼 수 있어 면접에서 아키텍처 설명이 깔끔해집니다.
- 나중에 라즈베리파이 HAL 실험을 하더라도 `vehicle` 데이터소스만 교체하면 됩니다.

## 5. 화면 상태 모델

초기 버전의 상태는 복잡하게 만들 필요 없습니다. 아래 정도면 충분합니다.

```text
Idle
WaitingForEntry
StoreDetected(storeId)
BrowsingMenu(storeId, gearState)
ReviewingOrder(storeId, orderDraft)
OrderSubmitted(orderId)
StoreUnavailable
```

기어 상태는 별도 축으로 둡니다.

```text
UNKNOWN
PARK
DRIVE
REVERSE
NEUTRAL
MOVING
STOPPED
```

핵심 규칙:
- `PARK`일 때만 전체 메뉴 진입 허용
- 비P 상태에서는 즐겨찾기/최근 주문 중심 간소화 화면만 허용
- 매장 진입 전에는 주문 UI 자체를 노출하지 않음

## 6. 다음 구현 우선순위

지금 시점의 가장 좋은 다음 기능은 Phase 2 전체가 아니라, 그 안의 "상태 전환 가능한 차량 앱 골격"입니다.

### 추천 Step 1

AAOS 앱 기본 골격 생성
- `CarAppService`, `Session`, 첫 `Screen` 연결
- 하드코딩된 `DriveThruStateStore` 추가
- `WaitingForEntry -> SimplifiedMenu -> FullMenu` 수동 전환 가능하게 구성

### 추천 Step 2

기어 상태 추상화 도입
- `GearStateDataSource` 인터페이스 추가
- `FakeGearStateDataSource`로 버튼/디버그 액션 기반 상태 변경
- 이후 `CarPropertyGearStateDataSource`로 실제 연동 교체

### 추천 Step 3

정적 메뉴 도메인 연결
- `FakeMenuRepository`로 샘플 메뉴 노출
- `PARK`에서는 전체 메뉴
- `DRIVE`에서는 1-tap 메뉴만 노출

### 추천 Step 4

ADB/VHAL 연동 자동화
- `tools/adb/*.ps1` 스크립트 추가
- GPS/기어 주입 명령 표준화

## 7. 지금 Firebase보다 먼저 해야 하는 이유

Firebase는 데모 완성에는 중요하지만, 지금 붙이면 아래 리스크가 있습니다.

- 차량 앱 상태 설계 없이 데이터 연동부터 시작하게 됨
- AAOS 제약보다 CRUD 구현 비중이 커짐
- 포트폴리오 메시지가 "차량 앱"보다 "실시간 앱"으로 흐려짐

면접 시그널 관점에서는 먼저 아래를 보여주는 편이 더 강합니다.

- Car App Library 구조를 이해하고 있다
- Driver Distraction을 상태 모델로 설계했다
- 실제 차량 속성 연동 포인트를 추상화했다

## 8. 추천 구현 순서

1. `app/automotive` 단일 앱 생성
2. `DriveThruStateStore`와 3개 핵심 화면 구현
3. 가짜 기어 상태 + 가짜 메뉴로 데모 흐름 완성
4. `adb`/VHAL 주입으로 실제 상태 연동
5. 그 다음에 Firebase 메뉴/주문 연동
6. 마지막에 `dashboard/web` 추가

## 9. 이번 주 체크포인트 제안

이번 주 목표는 "차량 화면에서 DriveThru 도메인 플로우가 보이는 상태"입니다.

완료 조건:
- 앱이 AAOS 에뮬레이터에서 단독 실행된다
- `WaitingForEntry`, `SimplifiedMenu`, `FullMenu` 3개 화면이 분기된다
- 기어 상태 변경에 따라 화면이 바뀐다
- 하드코딩 메뉴로 주문 초안까지 이동된다

이 상태가 되면 그 다음부터는 외부 연동을 붙여도 프로젝트 중심축이 안 흔들립니다.
