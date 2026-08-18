# DriveThru IVI Demo Runbook

이 문서는 `car/drivethru`를 AAOS 에뮬레이터에서 시연할 때 필요한 내용을 한 문서에 모은 실행 가이드다.

사용 목적:
- 면접 직전 빠른 리허설
- README 상단 링크용 요약 문서
- 짧은 시간만 보는 면접관용 핵심 스크립트
- 자세히 보는 면접관용 단계별 설명 / 스크린샷 포인트 정리

---

## 1. 최상단 핵심 시연 스크립트

아래 2개만 숙지해도 1~2분 안에 프로젝트 핵심을 설명할 수 있다.

### 스크립트 A: 가장 추천하는 메인 시연

한 줄 요약:
- `매장 접근 -> 안전 상태 확인 -> 주문 UI 진입 -> 주문 중 차량 출발 -> STOP_STATE -> 복귀 또는 종료`

발표 스크립트:
1. "이 프로젝트는 일반 커머스 앱이 아니라 차량 상태를 반영하는 AAOS 드라이브스루 UX입니다."
2. "차량이 매장 근처에 접근하면 GPS 또는 beacon 이벤트로 매장을 해석하고, 안전 상태면 주문 UI를 엽니다."
3. "정차 상태에서는 full ordering UI가 열리고, 주문 도중 차량이 움직이면 STOP_STATE로 즉시 내려가면서 draft는 보존됩니다."
4. "속도가 임계치를 넘거나 매장을 벗어나면 세션을 종료하고 standby로 복귀합니다."
5. "즉, 이 프로젝트의 핵심은 메뉴 UI보다 차량 상태 기반 safety gating과 fallback 구조입니다."

### 스크립트 B: 기술 깊이 강조 시연

한 줄 요약:
- `HAL 실패 fallback + diagnostics + template/custom UI 비교`

발표 스크립트:
1. "앱은 CarPropertyManager를 직접 신뢰하지 않고 SafeCarDataFacade를 통해 fallback 가능한 구조로 읽습니다."
2. "HAL 또는 property read가 실패해도 fake source로 degrade 하며 UI 전체가 죽지 않습니다."
3. "왼쪽은 custom compose UI 실험 경로, 오른쪽 template mode는 Car App Library 제약 비교 경로입니다."
4. "즉, 앱 UX와 플랫폼 제약을 둘 다 보여주는 AAOS 포트폴리오 프로젝트입니다."

---

## 2. 영상에서 바로 보여줄 순서

가장 추천하는 90초 시연 순서:

1. 앱 실행 후 `DriveThru IVI Studio` 메인 화면 노출
2. `GPS approach`
3. `PARK + stop`
4. `GPS ready`
5. 메뉴 1~2개 추가
6. `Review current draft`
7. `DRIVE`
8. `STOP_STATE` 확인
9. 다시 `PARK + stop`
10. `Resume / keep ordering`
11. `Exit zone`
12. `STANDBY` 복귀

면접관이 아주 짧게 본다면 여기까지만 보여도 충분하다.

---

## 3. 시작 전 준비

### 3.1 에뮬레이터 준비

```powershell
cd D:\agentproject\car\drivethru
adb devices
```

확인할 것:
- AAOS 에뮬레이터가 `device` 상태인지
- 다른 에뮬레이터와 헷갈리지 않는지

### 3.2 앱 설치

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\install-automotive.ps1
```

### 3.3 앱 실행

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\run-automotive.ps1
```

커스텀 UI를 직접 띄우고 싶으면:

```powershell
adb shell am start -n com.hanzi.drivethru/.app.DriveThruActivity
```

### 3.4 첫 실행 권한

반드시 허용:
- 위치 권한
- BLE scan / connect 권한

이유:
- `AndroidGeofenceEntryTriggerProvider`
- `BleBeaconEntryTriggerProvider`
가 런타임 시작 시 provider를 올리기 때문

---

## 4. 메인 화면에서 보게 되는 것

앱이 열리면 기본적으로 `DriveThruActivity` 기반 `DriveThru IVI Studio` 화면이 보인다.

상단:
- `DriveThru IVI Studio`
- status message
- `Mode`
- `Gear`
- `Speed`
- `Zone Stage`

중단:
- `UI mode`
  - `CAR_TEMPLATE`
  - `CLASSIC_CUSTOM`
  - `ENHANCED_CUSTOM`

- `Simulation controls`
  - `GPS approach`
  - `GPS ready`
  - `Beacon ready`
  - `Exit zone`
  - `PARK + stop`
  - `DRIVE`
  - `Speed 0.0`
  - `Speed 3.5`

하단:
- diagnostics panel
- source/status/raw vehicle data
- 현재 화면 destination에 따라 메뉴/카트/정지 상태 UI

기본 시작 상태:
- `ENHANCED_CUSTOM`
- `STANDBY`

---

## 5. 단계별 시연 스텝

## Step 1. 메인 화면 설명

할 일:
- 앱 메인 화면을 잠깐 보여준다

말할 내용:
- "이건 custom compose 기반 실험 UI입니다."
- "상단에서 차량 상태와 zone stage를 한눈에 보고, 아래에서 GPS/BLE/기어를 시뮬레이션합니다."

이해해야 할 코드:
- [DriveThruActivity.kt](/D:/agentproject/car/drivethru/app/automotive/app/src/main/java/com/hanzi/drivethru/app/DriveThruActivity.kt)
  - `DriveThruCustomUiApp`
  - `ModeSelector`
  - `DebugControlPanel`
  - `DiagnosticsPanel`

스크린샷 포인트:
- 메인 첫 화면 전체
- 헤더 + mode selector + simulation controls가 다 보이게

## Step 2. GPS 접근

직접 버튼:
- `GPS approach`

또는 명령:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\inject-store-approach-gps.ps1
```

기대 결과:
- 접근 상태 반영
- 매장 감지 준비 상태
- status message가 매장 감지 흐름으로 바뀜

이해해야 할 코드:
- [CustomUiFlowCoordinator.kt](/D:/agentproject/car/drivethru/app/automotive/app/src/main/java/com/hanzi/drivethru/feature/customui/CustomUiFlowCoordinator.kt)
  - `simulateGpsTrigger`
  - `syncEntryTrigger`
- [AndroidGeofenceEntryTriggerProvider.kt](/D:/agentproject/car/drivethru/app/automotive/app/src/main/java/com/hanzi/drivethru/data/entry/AndroidGeofenceEntryTriggerProvider.kt)
  - geofence distance -> stage 계산

스크린샷 포인트:
- `APPROACHING` stage가 보이는 순간

## Step 3. 안전 상태 만들기

직접 버튼:
- `PARK + stop`

또는 명령:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\inject-gear-park.ps1
```

기대 결과:
- `Gear PARK`
- `Speed 0.0`

이해해야 할 코드:
- [DriveThruSafetyPolicy.kt](/D:/agentproject/car/drivethru/app/automotive/app/src/main/java/com/hanzi/drivethru/core/state/DriveThruSafetyPolicy.kt)
- [CarDataVehicleSignalProvider.kt](/D:/agentproject/car/drivethru/app/automotive/app/src/main/java/com/hanzi/drivethru/data/vehicle/CarDataVehicleSignalProvider.kt)

스크린샷 포인트:
- 헤더에 `Gear PARK`, `Speed 0.0`

## Step 4. 메뉴 보드 도달

직접 버튼:
- `GPS ready`
또는
- `Beacon ready`

명령:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\inject-store-ready-gps.ps1
```

또는:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\inject-beacon-menu-board.ps1
```

기대 결과:
- 매장 활성화
- `FULL_MENU` 또는 `STORE_READY -> FULL_MENU`
- 메뉴가 렌더링됨

이해해야 할 코드:
- [TenantStoreResolver.kt](/D:/agentproject/car/drivethru/app/automotive/app/src/main/java/com/hanzi/drivethru/data/store/TenantStoreResolver.kt)
- [TenantCatalogRepository.kt](/D:/agentproject/car/drivethru/app/automotive/app/src/main/java/com/hanzi/drivethru/data/tenant/TenantCatalogRepository.kt)
- [CompositeEntryTriggerProvider.kt](/D:/agentproject/car/drivethru/app/automotive/app/src/main/java/com/hanzi/drivethru/data/entry/CompositeEntryTriggerProvider.kt)

스크린샷 포인트:
- 매장명과 메뉴 리스트가 보이는 full menu 상태

## Step 5. 메뉴 담기

할 일:
- 메뉴 1~2개 `Add`

기대 결과:
- 우측 summary panel에 항목 반영
- total 금액 증가

이해해야 할 코드:
- [OrderingSessionController.kt](/D:/agentproject/car/drivethru/app/automotive/app/src/main/java/com/hanzi/drivethru/core/state/OrderingSessionController.kt)
- `CustomUiFlowCoordinator.addMenuItem`

스크린샷 포인트:
- 메뉴 리스트와 summary panel이 동시에 보이는 화면

## Step 6. 카트 리뷰

할 일:
- `Review current draft`

기대 결과:
- `CART_REVIEW`
- 담은 항목과 총액 확인

이해해야 할 코드:
- `CustomUiFlowCoordinator.openCartReview`
- `CartReviewPanel`

스크린샷 포인트:
- 카트 리뷰 화면

## Step 7. 주문 중 차량 출발

직접 버튼:
- `DRIVE`

명령:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\inject-gear-drive.ps1
```

기대 결과:
- `STOP_STATE`
- draft 보존
- 즉시 resume 불가

이해해야 할 코드:
- [StopStatePolicy.kt](/D:/agentproject/car/drivethru/app/automotive/app/src/main/java/com/hanzi/drivethru/core/state/StopStatePolicy.kt)
- `CustomUiFlowCoordinator.syncVehicleSignal`

스크린샷 포인트:
- STOP_STATE 전체 화면

## Step 8. 다시 정차 후 복귀

할 일:
- `PARK + stop`
- `Resume / keep ordering`

기대 결과:
- 이전 ordering destination으로 복귀

스크린샷 포인트:
- STOP_STATE에서 ordering으로 복귀한 직후

## Step 9. 매장 이탈 또는 고속 종료

직접 버튼:
- `Exit zone`

명령:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\inject-store-exit.ps1
```

또는 고속 중단:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\inject-high-speed-abort.ps1
```

기대 결과:
- 세션 종료
- `STANDBY` 복귀

이해해야 할 코드:
- `CustomUiFlowCoordinator.closeSession`
- `DriveThruSafetyPolicy.shouldAbortOrderingSession`

스크린샷 포인트:
- standby 복귀 화면
- speed threshold 문구가 보이면 더 좋음

---

## 6. Car App Template 비교 시연

이 프로젝트는 custom UI만 있는 게 아니라 Car App Library 제약 환경도 같이 보여줄 수 있다.

할 일:
- 상단 `UI mode`에서 `CAR_TEMPLATE`
- `Launch template app host`

보여줄 흐름:
1. `Menu` 탭에서 메뉴 추가
2. `Cart` 탭에서 `Pay now`
3. `Order` 탭에서 완료 상태 확인
4. `Setting` 탭 확인

이해해야 할 코드:
- [DriveThruSession.kt](/D:/agentproject/car/drivethru/app/automotive/app/src/main/java/com/hanzi/drivethru/app/DriveThruSession.kt)
- [DriveThruTabRootScreen.kt](/D:/agentproject/car/drivethru/app/automotive/app/src/main/java/com/hanzi/drivethru/feature/root/DriveThruTabRootScreen.kt)
- [MenuTemplateRenderer.kt](/D:/agentproject/car/drivethru/app/automotive/app/src/main/java/com/hanzi/drivethru/feature/menu/MenuTemplateRenderer.kt)
- [CartTemplateRenderer.kt](/D:/agentproject/car/drivethru/app/automotive/app/src/main/java/com/hanzi/drivethru/feature/cart/CartTemplateRenderer.kt)
- [OrderTemplateRenderer.kt](/D:/agentproject/car/drivethru/app/automotive/app/src/main/java/com/hanzi/drivethru/feature/order/OrderTemplateRenderer.kt)
- [DriveThruStateStore.kt](/D:/agentproject/car/drivethru/app/automotive/app/src/main/java/com/hanzi/drivethru/core/state/DriveThruStateStore.kt)

짧은 설명 문장:
- "Template mode는 AAOS host 제약 안에서 주문/결제 mock 흐름을 보여주고, custom mode는 OEM-style 자유 레이아웃과 safety gating 실험 경로를 보여줍니다."

스크린샷 포인트:
- `Menu` 탭
- `Cart` 탭 `Pay now`
- `Order complete` 화면

---

## 7. 내 이해용 짧은 대화형 스크립트

아래는 혼자 리허설할 때 읽기 좋은 짧은 대화형 스크립트다.

### 버전 A: 면접관이 "이 서비스가 뭐예요?"라고 물었을 때

Q. 이 서비스는 뭐죠?

A.
"차량이 드라이브스루 매장 근처에 접근했을 때, GPS나 beacon으로 매장을 해석하고 차량 상태가 안전하면 주문 UI를 열어주는 AAOS 드라이브스루 UX입니다. 핵심은 메뉴 화면 자체보다 차량 속도, 기어, 위치 상태를 기반으로 주문 가능 여부를 제어하는 safety-aware 상태머신입니다."

### 버전 B: "일반 앱이랑 뭐가 다르죠?"라고 물었을 때

A.
"일반 커머스 앱은 주문 화면을 항상 열 수 있지만 차량 앱은 주행 중 제한이 필요합니다. 이 프로젝트는 DRIVE 전환 시 STOP_STATE로 내려가고, 속도 임계치 초과 시 세션을 종료하며, HAL unavailable 상황에서도 fallback으로 앱이 유지되도록 설계했습니다."

### 버전 C: "왜 template와 custom UI가 둘 다 있죠?"라고 물었을 때

A.
"Car App Library는 실제 AAOS 제약 환경을 설명하기 좋고, custom UI는 OEM 스타일 자유 레이아웃과 richer UX를 보여주기 좋습니다. 둘을 같이 두어서 플랫폼 제약 이해와 제품 UX 실험을 동시에 보여주려는 의도입니다."

---

## 8. 스크린샷 찍으면 좋은 포인트

면접관은 보통 길게 안 보므로, 우선순위를 둬야 한다.

### 최우선 5장

1. 메인 `DriveThru IVI Studio` 첫 화면
- header + mode + simulation controls + diagnostics

2. `FULL_MENU`
- 매장명 + 메뉴 리스트 + summary panel

3. `CART_REVIEW`
- 주문 항목과 총액

4. `STOP_STATE`
- 이유와 draft 보존 메시지

5. `Order complete` 또는 `Template Order`
- 결제 완료 mock 증거

### 있으면 좋은 추가 5장

6. `APPROACHING` 상태
7. `BEACON` source 기반 진입
8. `FALLBACK / FAKE` diagnostics
9. `CAR_TEMPLATE Menu`
10. `CAR_TEMPLATE Cart / Pay now`

---

## 9. README에 이 문서를 어떻게 놓으면 좋은가

README 상단 구조 추천:

1. 프로젝트 한 줄 설명
2. 1분 시연 스크립트 A
3. 30초 기술 깊이 스크립트 B
4. 짧은 영상 embed 또는 링크
5. 대표 스크린샷 3~5장
6. 자세한 실행/검증/아키텍처 문서 링크

즉, 이 문서는 README에서 아래처럼 연결하면 좋다.

- `Quick Demo Script`
- `Step-by-Step Emulator Demo`
- `Screenshot Checklist`

---

## 10. 관련 문서

- [AAOS Emulator Validation Playbook](/D:/agentproject/car/drivethru/docs/emulator-validation-playbook.md)
- [DriveThru IVI Video Goal](/D:/agentproject/car/drivethru/docs/video-goal.md)
- [DriveThru IVI Architecture](/D:/agentproject/car/drivethru/docs/architecture.md)
- [Validation Strategy](/D:/agentproject/car/drivethru/docs/validation-strategy.md)
- [Logging And Error Handling Policy](/D:/agentproject/car/drivethru/docs/logging-error-handling-policy.md)
