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
      VehicleSignal.kt
      VehicleSignalSnapshot.kt
      VehicleMotionState.kt
      Store.kt
      MenuItem.kt
      OrderDraft.kt
    state/
      DriveThruStateStore.kt
      SafetyCriticalStateMachine.kt
      CriticalCommandStateMachine.kt
    navigation/
      DriveThruNavigator.kt
  data/
    vehicle/
      VehicleSignalProvider.kt
      VehicleSignalLatencyTracker.kt
      GearStateDataSource.kt
      FakeGearStateDataSource.kt
      CarPropertyGearStateDataSource.kt
      CarPropertyVehicleSignalProvider.kt
      LegacyHidlVehicleSignalAdapter.kt
    geofence/
      EntryTriggerDataSource.kt
      FakeEntryTriggerDataSource.kt
    menu/
      MenuRepository.kt
      FakeMenuRepository.kt
    order/
      OrderRepository.kt
      FakeOrderRepository.kt
      CriticalCommandJournal.kt
      ExternalCommandGateway.kt
      FakeExternalCommandGateway.kt
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
- 최신 AAOS는 AIDL Vehicle HAL 축으로 설명하고, HIDL은 레거시 비교용 어댑터로 한정하는 편이 구조적으로 정확합니다.

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
- 전체 메뉴가 열린 뒤라도 `gear != PARK` 또는 `speed > 0.5 m/s`가 감지되면 100ms 이내 안전 화면으로 강등

## 6. 차량 신호 추상화 원칙

여기서 추상화해야 하는 것은 `HIDL` 자체가 아니라 "차량 신호 공급원"입니다.

```text
DriveThruStateStore / SafetyCriticalStateMachine
  <- VehicleSignalProvider
      <- CarPropertyVehicleSignalProvider
          <- CarPropertyManager
              <- CarService
                  <- AIDL Vehicle HAL (primary)
                  <- HIDL Vehicle HAL (legacy/reference)
```

핵심 원칙:
- 앱은 `CarPropertyManager` 아래 전송 세부사항을 직접 알지 않습니다.
- 앱은 `VehicleSignalSnapshot(gear, speed, timestamp)`만 소비합니다.
- AIDL/HIDL 차이는 플랫폼 실험 단계에서만 드러나고, 앱 상태 머신은 동일하게 유지합니다.

## 7. 소프트 실시간 기능 정의

이 프로젝트에서 구현 가치가 가장 큰 실시간 기능은 주문 추천보다 `Safety UI Downgrade Guard`입니다.

트리거:
- `gear != PARK`
- `speed > 0.5 m/s`

보장 목표:
- 위험 신호 수신 후 100ms 이내에 `FullMenuScreen` 상호작용을 종료하고 `SimplifiedMenuScreen` 또는 `SafetyRestricted` 화면으로 전환

설계 포인트:
- 차량 신호 콜백에서는 상태 계산, 타임스탬프 기록, 화면 전환 요청만 수행
- 네트워크, 메뉴 fetch, 음성 처리 같은 비실시간 작업은 같은 경로에 두지 않음
- deadline 초과 시 더 제한적인 화면으로 fail-safe 전환

검증 포인트:
- Fake provider와 실제 provider 모두에서 동일한 상태 머신을 통과
- `SystemClock.elapsedRealtimeNanos()` 기준 latency 로그 보관
- 시연 시 "신호 주입 -> 화면 차단"을 로그와 함께 보여줄 수 있어야 함

## 8. 중요 명령 watchdog 원칙

차량 앱이나 임베디드 연동에서는 "명령을 보냈다"보다 "명령 상태가 모호하지 않다"가 더 중요합니다.

권장 흐름:

```text
UI action
  -> CriticalCommandStateMachine
      -> CriticalCommandJournal append
      -> ExternalCommandGateway.send(commandId, deadlineMs)
      -> ACK received ? commit : timeout -> fail-safe
```

핵심 원칙:
- 외부 프로세스나 IO 응답이 늦으면 성공 처리하지 않음
- `in-flight` 명령은 journal에 먼저 기록
- 전원 복구 후 journal을 읽어 마지막 명령을 `commit`, `cancel`, `manual-review` 중 하나로 수렴
- emergency mode에서는 신규 중요 명령 발행을 막음
- UI는 `submitted`와 `acknowledged`를 분리해서 보여줌

실패 대응 기본값:
- timeout: 제한 횟수 재시도 후 `PendingRecovery`
- binder disconnect / process death: 즉시 `FailedSafe`
- power loss during in-flight: reboot 후 recovery check
- emergency mode entered: 재시도 중단, 사용자 액션 차단

## 9. 다음 구현 우선순위

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

### 추천 Step 5

차량 신호 통합 추상화
- `VehicleSignalProvider` 도입
- `gear`와 `speed`를 하나의 스냅샷으로 통합
- Fake/실제 provider를 같은 상태 머신에 연결

### 추천 Step 6

안전 UI 실시간 가드
- `SafetyCriticalStateMachine` 추가
- 전체 메뉴 열린 상태에서 위험 신호 감지 시 100ms 이내 차단
- latency 측정 로그 추가

### 추천 Step 7

중요 명령 watchdog
- `CriticalCommandStateMachine` 추가
- `CriticalCommandJournal`로 in-flight 명령 영속화
- `ExternalCommandGateway` 뒤에 Fake/실제 프로세스 연결
- timeout, process death, reboot recovery, emergency mode 차단 처리

## 10. 지금 Firebase보다 먼저 해야 하는 이유

Firebase는 데모 완성에는 중요하지만, 지금 붙이면 아래 리스크가 있습니다.

- 차량 앱 상태 설계 없이 데이터 연동부터 시작하게 됨
- AAOS 제약보다 CRUD 구현 비중이 커짐
- 포트폴리오 메시지가 "차량 앱"보다 "실시간 앱"으로 흐려짐

면접 시그널 관점에서는 먼저 아래를 보여주는 편이 더 강합니다.

- Car App Library 구조를 이해하고 있다
- Driver Distraction을 상태 모델로 설계했다
- 실제 차량 속성 연동 포인트를 추상화했다
- 안전 요구를 소프트 실시간 상태 머신으로 설계했다
- IO timeout, power loss, emergency mode까지 포함한 fail-safe 설계를 설명할 수 있다

## 11. 추천 구현 순서

1. `app/automotive` 단일 앱 생성
2. `DriveThruStateStore`와 3개 핵심 화면 구현
3. 가짜 기어 상태 + 가짜 메뉴로 데모 흐름 완성
4. `VehicleSignalProvider`로 기어/속도 신호 통합
5. `Safety UI Downgrade Guard`로 100ms 안전 차단 구현
6. `CriticalCommandWatchdog`로 ACK timeout, recovery, emergency mode 차단 구현
7. `adb`/VHAL 주입으로 실제 상태 연동
8. 그 다음에 Firebase 메뉴/주문 연동
9. 마지막에 `dashboard/web` 추가

## 12. 이번 주 체크포인트 제안

이번 주 목표는 "차량 화면에서 DriveThru 도메인 플로우가 보이는 상태"입니다.

완료 조건:
- 앱이 AAOS 에뮬레이터에서 단독 실행된다
- `WaitingForEntry`, `SimplifiedMenu`, `FullMenu` 3개 화면이 분기된다
- 기어 상태 변경에 따라 화면이 바뀐다
- 하드코딩 메뉴로 주문 초안까지 이동된다
- 위험 신호 주입 후 100ms 이내 안전 화면 차단 로그가 남는다
- 중요 명령 timeout 또는 프로세스 kill 후에도 상태가 모호하지 않게 복구된다

이 상태가 되면 그 다음부터는 외부 연동을 붙여도 프로젝트 중심축이 안 흔들립니다.
