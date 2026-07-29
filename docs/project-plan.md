# DriveThru IVI - 차량 UI 드라이브스루 주문 시스템

> Android Automotive OS 포트폴리오 프로젝트 기획서
> 개발자: 조재현 (Hanzi-Cho)
> 목적: IVI 도메인 학습

---

## 1. 프로젝트 개요

### 1.1 배경 및 동기

현재 드라이브스루 주문은 마이크 음성 인식이나 스마트폰 앱 선주문 방식에 의존합니다. 운전 중 스마트폰 조작은 위험하고, 음성 인식은 주변 소음에 취약합니다.

이 프로젝트는 차량이 드라이브스루 구역에 진입하는 순간 차량 디스플레이(CID)가 자동으로 터치 메뉴판으로 전환되어, 운전자가 익숙한 터치 UX로 안전하게 주문하는 시나리오를 AAOS 기반으로 구현합니다.

이번 프로젝트는 아래 2단계 전략으로 진행합니다.

1. 현재 단계
   - `Car App Library` 기반으로 상태 전이, 탭 구조, 데이터 흐름, 안전 제한 로직을 먼저 검증한다.
2. 목표 단계
   - `AAOS Activity + Compose Custom UI`로 전환하여, OEM 스타일의 자유 레이아웃 주문 경험을 구현한다.

즉 현재 구현은 템플릿 기반 프로토타입이고, 최종 목표는 OEM 앱 수준의 커스텀 UI 주문 시나리오다.

### 1.2 포트폴리오 목적 및 어필 포인트

| 어필 포인트 | 구체적 내용 |
| --- | --- |
| AAOS 도메인 이해 | Car App Library 프로토타이핑, Custom UI 전환, CarPropertyManager, VehicleHAL 구조 이해 및 적용 |
| Driver Distraction 인지 | 주행/정차 상태에 따른 UI 분기, AAOS 가이드라인 실제 적용 |
| 리눅스 임베디드 연결 | Yocto 경험을 라즈베리파이 HAL 연동으로 확장해 플랫폼 레이어 이해 증명 |
| 실동작 데모 | 에뮬레이터 영상으로 면접 현장에서 바로 보여줄 수 있는 결과물 |
| 자기주도 학습 | 스스로 IVI 도메인을 탐구하고 구현한 학습 증명 |

### 1.3 범위 설정 원칙

이 프로젝트의 목적은 IVI 도메인을 학습하는 것입니다. 결제 완성, 실제 POS 연동, 상용 수준의 백엔드는 의도적으로 제외합니다.

포함 범위:
- 근처 드라이브스루 접근 감지
- 어떤 매장의 주문 기능을 로드할지 판단하는 매장 해석 계층
- 차량 상태와 안전 상태에 따라 Custom UI를 열거나 제한하는 정책
- 주문 중 주행 시작 시 세션을 끊지 않고 `STOP_STATE`로 내리는 정책
- HAL / CarProperty / GPS 데이터를 추상화한 상태 계층

제외 범위:
- 실제 상용 결제 승인
- 실제 POS 주문 접수
- OEM 시스템 권한이 필요한 모든 기능의 완전한 실차 검증

### 1.4 현재 구현 단계와 목표 구현 단계

현재 구현 단계:
- `Car App Library` 기반 4개 메인 destination 프로토타입
- 메뉴, 카트, 주문, 설정 흐름의 정보 구조 검증
- 상태 저장소와 데이터 분리 구조 검증

목표 구현 단계:
- `AAOS Custom UI` 기반 자유 레이아웃 주문 UI
- GPS/Geofence/매장 판별 후 주문 앱 자동 실행
- 차량 정지 가능 상태에서만 full ordering UI 진입
- 주행 시작 시 `STOP_STATE`로 내려가 세션 유지
- 추후 OEM 권한이 가능한 환경에서는 실제 차량 신호 연동 검증

---

## 2. 시스템 아키텍처

### 2.1 전체 구성도

```text
[차량 레이어]
  AAOS Emulator / AAOS Device
  ├── Phase A: Car App Library prototype
  │     └── state flow / safety gating / data flow validation
  └── Phase B: Custom UI Ordering App
        ├── GPS / geofence proximity detection
        ├── store resolver
        ├── CarProperty / HAL vehicle state monitor
        ├── ordering session controller
        └── Compose-based custom ordering UI

[플랫폼 이벤트 레이어]
  Vehicle HAL / Car Service / CarPropertyManager
  Android location / geofence provider
  Debug simulator bridge

[주문 데이터 레이어]
  store metadata
  menu bundle / quick-order bundle
  session cache
  payment mock state

[매장 / 시뮬레이터 레이어]
  web dashboard / drive-thru zone simulator
  └── entry, exit, park, drive, stop 이벤트 공급
```

### 2.2 기술 스택

| 레이어 | 기술 | 역할 |
| --- | --- | --- |
| 차량 OS | Android Automotive OS (API 33) | 차량 전용 안드로이드 플랫폼 |
| UI 프레임워크 A | Car App Library 1.4+ | 초기 템플릿 기반 프로토타입 |
| UI 프레임워크 B | Android Activity + Compose | 최종 Custom UI 주문 경험 |
| 진입 감지 | Google Maps Geofencing API | GPS 좌표 기반 진입 이벤트 |
| 차량 상태 | CarPropertyManager | 기어, 속도 등 차량 속성 감지 |
| 차량 HAL 인터페이스 | AIDL Vehicle HAL 우선, HIDL 구조 비교 학습 | 플랫폼 신호 추상화 및 레거시 HAL 이해 |
| 위치/신호 추상화 | VehicleSignalProvider / StoreResolver | GPS, 기어, 속도, 정지 상태 판단 |
| 세션 정책 | OrderingSessionController / StopStatePolicy | 주문 중 주행 시 세션 유지 및 백그라운드 정책 |
| 실시간 DB | Firebase Realtime Database 또는 mock | 메뉴, 주문 상태 동기화 실험 |
| 언어 | Kotlin | AAOS 앱 및 직원 앱 개발 |
| 에뮬레이터 | Android Studio AVD (Automotive) | 실기기 없이 전 기능 시연 |
| 임베디드 (Phase 5) | Raspberry Pi 4 + AOSP/Linux | HAL 연동 및 디바이스 노드 실습 |

### 2.3 핵심 아키텍처 결론

- `Car App Library`는 현재 단계의 구조 검증용이다.
- 최종 목표 UI는 `Custom UI`다.
- 매장 접근 판단과 차량 안전 상태 판단은 UI 밖의 상태 계층에서 처리한다.
- 주문 중 차량이 다시 움직이면 세션을 종료하지 않고 `STOP_STATE`로 내려간다.
- `STOP_STATE`에서는 화면이 백그라운드로 빠지거나 제한 모드로 전환되더라도 주문 draft와 세션 데이터는 유지한다.

---

## 3. 단계별 구현 계획

### Phase 1 - AAOS 에뮬레이터 환경 구축 (1~2주)

목표: Android Studio에 AAOS AVD를 띄우고, Car App Library 앱이 차량 에뮬레이터 화면에 표시되는 것을 확인합니다.

#### 상세 작업

- [ ] Android Studio Hedgehog 이상 설치 확인
- [ ] SDK Manager에서 API 33 `Android Automotive with Google APIs` 이미지 다운로드
- [ ] AVD Manager에서 Automotive 카테고리 AVD 2종 생성
- [ ] `Automotive 1024p landscape` 생성
- [ ] `Automotive 1080p portrait` 생성
- [ ] `build.gradle`에 Car App Library 의존성 추가
- [ ] `CarAppService`, `Session`, `Screen` 기본 구조 작성
- [ ] `AndroidManifest.xml`에 `TEMPLATE_HOST_CATEGORY` 선언 및 `CarAppService` 등록
- [ ] 에뮬레이터 실행 후 차량 화면에 앱 진입 확인

#### `build.gradle` 핵심 의존성

```kotlin
dependencies {
    implementation("androidx.car.app:app:1.4.0")
    implementation("androidx.car.app:app-automotive:1.4.0")
}
```

#### `AndroidManifest.xml` 핵심 선언

```xml
<service
    android:name=".DriveThruCarAppService"
    android:exported="true">
    <intent-filter>
        <action android:name="androidx.car.app.CarAppService" />
        <category android:name="androidx.car.app.category.POI" />
    </intent-filter>
</service>
```

#### 확인 지표

- [ ] 에뮬레이터에서 커스텀 화면 렌더링 확인
- [ ] 두 해상도(landscape / portrait)에서 레이아웃 정상 표시

### Phase 2 - GPS Geofence 진입 감지 + 기어 상태 UI 분기 (2~3주)

목표: `adb`로 GPS 좌표를 주입했을 때 차량 화면이 자동으로 드라이브스루 주문 화면으로 전환됩니다. 기어 상태에 따라 UI가 분기됩니다.

#### 구현 핵심

- [ ] `GeofencingClient`로 드라이브스루 매장 좌표 반경 50m 등록
- [ ] `GeofencingEvent` 수신 시 `CarAppService`에 화면 전환 이벤트 전달
- [ ] `CarPropertyManager.GEAR_SELECTION` 리스너 등록
- [ ] P단 (값: 4)일 때 전체 메뉴 UI 표시
- [ ] 비P단 (D단 값: 8)일 때 즐겨찾기 1-tap 간소화 UI 표시

#### `adb` VHAL 시뮬레이션 명령어

```bash
# GPS 좌표 주입 (드라이브스루 매장 위치로 설정)
adb shell cmd car_service inject-vhal-event 0x11500005 37.4979:127.0276

# 기어 P단으로 변경 -> 전체 메뉴 UI 활성화
adb shell cmd car_service inject-vhal-event 0x11400400 4

# 기어 D단으로 변경 -> 간소화 UI 전환
adb shell cmd car_service inject-vhal-event 0x11400400 8

# 속도 주입 (m/s, 0 = 정차)
adb shell cmd car_service inject-vhal-event 0x11600207 0
```

#### `CarPropertyManager` 기어 감지 코드 구조

```kotlin
private fun observeGearState() {
    val car = Car.createCar(context)
    val carPropertyManager = car.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager

    carPropertyManager.registerCallback(
        object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(event: CarPropertyValue<*>) {
                val gear = event.value as Int
                if (gear == VehicleGear.GEAR_PARK) {
                    screenManager.push(FullMenuScreen(carContext))
                } else {
                    screenManager.push(SimplifiedMenuScreen(carContext))
                }
            }

            override fun onErrorEvent(propId: Int, zone: Int) {}
        },
        VehiclePropertyIds.GEAR_SELECTION,
        CarPropertyManager.SENSOR_RATE_ONCHANGE
    )
}
```

#### 확인 지표

- [ ] 좌표 주입 후 화면 전환 동작 확인
- [ ] P단일 때 전체 메뉴 UI, D단일 때 간소화 UI 분기 확인
- [ ] 데모 영상 V1 녹화 (3분 이내 시연)

### Phase 2.5 - 차량 신호 추상화 + 안전 UI 실시간 가드 (1~2주)

목표: 차량 신호 수집 계층을 앱 로직에서 분리하고, 주행 위험 상태 진입 시 주문 UI를 100ms 이내에 안전 모드로 강등하는 소프트 실시간 기능을 구현합니다.

#### 왜 이 단계를 추가하나

- 현재 문서에는 `CarPropertyManager` 연동 계획은 있지만, HAL 전송 계층 차이를 어떻게 추상화할지 비어 있습니다.
- AAOS 최신 흐름에서는 Vehicle HAL이 AIDL 기반으로 이동하는 추세이며, HIDL은 레거시 호환 관점에서 이해하는 편이 정확합니다.
- "실시간 보장"은 Android 앱에서 하드 리얼타임보다 "위험 신호 도착 후 허용 지연 안에 안전 상태로 전이"라는 소프트 리얼타임 안전 요구로 정의하는 편이 실제 면접과 구현 모두에 유리합니다.

#### 구현 핵심

- [ ] 앱 레이어에 `VehicleSignalProvider` 또는 `VehicleSignalGateway` 추상화 추가
- [ ] `CarPropertyManager` 기반 구현체와 Fake 구현체를 같은 인터페이스 뒤로 정렬
- [ ] 플랫폼 문서에서 `AIDL Vehicle HAL -> CarService -> CarPropertyManager` 경로를 기본 경로로 명시
- [ ] 레거시 학습용으로 `HIDL Vehicle HAL -> CarService` 경로 차이와 비교 포인트 정리
- [ ] `GEAR_SELECTION`, `PERF_VEHICLE_SPEED`를 하나의 `VehicleSignalSnapshot` 모델로 통합
- [ ] `speed > 0.5 m/s` 또는 `gear != PARK` 이벤트 수신 시 전체 메뉴를 즉시 닫고 `SimplifiedMenu` 또는 `SafetyRestricted` 화면으로 100ms 이내 전환
- [ ] 상태 전환 시각을 `SystemClock.elapsedRealtimeNanos()` 기준으로 로깅해 지연 시간 측정
- [ ] 100ms 초과 시 fail-safe 로그와 통계 카운터 기록

#### 실시간 보장 대상 기능

기능 이름: `Safety UI Downgrade Guard`

정의:
- 운전자가 전체 메뉴를 보고 있는 도중 차량이 주행 가능 상태로 바뀌면, 앱은 전체 메뉴 상호작용을 계속 허용하면 안 됩니다.
- 따라서 `VehicleSignal` 변화 감지 후 100ms 이내에 전체 메뉴를 닫고 안전 모드 UI로 강등하는 것을 보장 목표로 둡니다.

보장 방식:
- 앱 메인 스레드에 무거운 작업을 두지 않음
- 차량 신호 콜백에서는 상태 계산과 화면 전환 이벤트만 수행
- 메뉴 로딩, Firebase, 이미지 로딩 등 비실시간 작업은 별도 경로로 분리
- 요구 시간을 넘기면 "기능 실패"가 아니라 "안전 우선 강제 차단"으로 동작

#### 확인 지표

- [ ] Fake 데이터소스에서 `PARK -> DRIVE` 전환 시 100ms 이내 안전 화면 전환 로그 확인
- [ ] 가능하면 VHAL 또는 에뮬레이터 신호로도 동일 시나리오 검증
- [ ] 로그에 마지막 신호 시각, UI 차단 시각, 총 지연 시간이 남음
- [ ] 전체 메뉴가 열린 상태에서 속도 신호가 올라가면 추가 입력 없이 자동 차단됨

#### 추가 필수 실시간 기능

기능 이름: `Critical Command Watchdog`

대상:
- 주문 확정 직전 외부 IO 장치, POS 브로커, 다른 프로세스에 보내는 "단일 중요 명령"

보장 목표:
- 명령 발행 후 정해진 deadline 안에 `ACK`를 못 받으면 성공으로 간주하지 않음
- timeout, 프로세스 다운, Binder disconnect, IO 무응답 시 즉시 `PendingRecovery` 또는 `FailedSafe` 상태로 전환
- 전원 복구 후 중복 실행 없이 마지막 명령 상태를 복구 가능해야 함
- 차량이 비상 모드 또는 restricted mode로 전환되면 미완료 명령은 재시도하지 않고 차단 상태로 봉인

기본 정책:
- 명령 전송 전 `commandId`, `issuedAt`, `target`, `deadlineMs`를 로컬 영속 저장
- `ACK` 수신 전까지 UI에는 "완료"가 아니라 "확인 중"만 표시
- timeout 시 자동 무한 재시도 금지, 1회 제한 재시도 후 운영자 확인 필요 상태로 전환
- 프로세스 재시작 또는 전원 복구 후 `in-flight` 명령이 있으면 상태 재조회 후 `commit` 또는 `cancel` 중 하나로 수렴
- emergency mode 진입 시 신규 중요 명령 발행 금지

#### 자동차 도메인 실시간 테스트 계획

- 응답 지연/타임아웃:
  - Fake IO 또는 테스트 프로세스가 `ACK`를 50ms, 200ms, 500ms, timeout으로 지연 응답
  - deadline 내 성공, deadline 초과 시 `PendingRecovery` 전환, 중복 실행 방지 확인
- 프로세스 비정상 종료:
  - 명령 발행 직후 대상 프로세스 kill
  - binder disconnect 또는 channel close 감지 후 fail-safe 상태 전환 확인
- 전원 상실:
  - 명령 발행 후 `ACK` 직전 강제 프로세스 종료 또는 에뮬레이터 재시작
  - 재부팅 후 로컬 journal 기반으로 마지막 `in-flight` 명령 복구 및 재조회 수행 여부 확인
- 비상 모드 전환:
  - 명령 대기 중 `gear != PARK`, 속도 상승, 시스템 restricted mode 플래그 주입
  - 미완료 명령 재시도 중단, 신규 명령 차단, 사용자에게 안전 제한 상태 표시 확인
- 로그/판정:
  - `issue time`, `ack time`, `timeout time`, `recovery result`를 모두 로그로 남김
  - 테스트 통과 기준은 "성공률"보다 "모호한 상태 없이 commit/cancel/fail-safe 중 하나로 수렴"임

### Phase 3 - Firebase 실시간 메뉴 동기화 (3~4주)

목표: Firebase에서 메뉴 데이터를 가져와 차량 화면에 표시하고, 직원이 품절 처리하면 차량 화면에 2초 이내 반영됩니다.

#### Firebase 데이터 구조

```json
{
  "stores": {
    "store_001": {
      "name": "맥도날드 강남점",
      "location": { "lat": 37.4979, "lng": 127.0276 },
      "menu": {
        "item_001": {
          "name": "빅맥 세트",
          "price": 8500,
          "available": true,
          "category": "burger"
        },
        "item_002": {
          "name": "아이스 아메리카노",
          "price": 2500,
          "available": true,
          "category": "drink"
        }
      }
    }
  },
  "orders": {
    "order_001": {
      "storeId": "store_001",
      "items": { "item_001": 2, "item_002": 1 },
      "status": "pending",
      "timestamp": 1700000000000
    }
  }
}
```

#### 구현 항목

- [ ] Firebase 프로젝트 생성 및 `google-services.json` 추가
- [ ] Geofence 진입 시 해당 `storeId`로 메뉴 데이터 구독 시작
- [ ] `ListTemplate` 또는 `GridTemplate`으로 메뉴 차량 화면 렌더링
- [ ] `available: false` 변경 시 차량 화면 즉시 비활성화
- [ ] 주문 버튼 클릭 시 `orders`에 데이터 기록
- [ ] 직원 웹 대시보드 (React 단일 페이지) 구현
- [ ] 메뉴 품절/재고 토글 기능 추가
- [ ] 신규 주문 실시간 알림 기능 추가

#### 확인 지표

- [ ] 직원 대시보드 품절 처리 후 차량 화면 2초 이내 반영
- [ ] 차량 주문 후 Firebase `orders`에 데이터 기록 확인

### Phase 4 - Adaptive UI + 데모 완성 (4~5주)

목표: 두 가지 차량 디스플레이 비율에서 최적화된 UI가 표시되며 최종 데모 영상을 완성합니다.

#### Adaptive UI 구현

- [ ] `sw800dp` (landscape)에서 메뉴 그리드 3열 구성
- [ ] `sw480dp` (portrait)에서 메뉴 리스트 1열 구성
- [ ] `CarAppService`에서 화면 크기 정보를 받아 적절한 Template 선택

#### 최종 데모 시나리오

| 순서 | 장면 | 보여주는 것 |
| --- | --- | --- |
| 1 | `adb` 좌표 주입 | Geofence 진입 이벤트 발생 |
| 2 | D단 상태 | 간소화 UI (Driver Distraction 준수) |
| 3 | P단 전환 | 전체 메뉴 UI 활성화 |
| 4 | 메뉴 선택 | Firebase에 주문 기록 |
| 5 | 직원 화면 확인 | 주문 실시간 수신 |
| 6 | 품절 처리 | 차량 화면 즉시 반영 |
| 7 | 해상도 전환 | portrait ↔ landscape UI 비교 |

#### 확인 지표

- [ ] 7단계 시나리오를 끊김 없이 시연하는 3~5분 영상 완성
- [ ] GitHub README에 아키텍처 다이어그램, 기술 스택, 데모 영상 링크 포함

### Phase 5 - 라즈베리파이 HAL 연동 (선택, 5~12주)

목표: 라즈베리파이 4에 커스텀 리눅스 또는 AOSP를 올리고, 실제 `/dev` 노드를 통해 Android HAL 레이어까지 연결합니다.

> 이 단계는 포트폴리오 필수가 아닙니다. Phase 4만 완성해도 면접 포트폴리오로 충분합니다.

#### 5-A: 커스텀 리눅스 + 디바이스 노드 실습 (1~2주)

- [ ] 라즈베리파이 4에 Ubuntu Server 24.04 설치
- [ ] `/dev`, `/sys`, `/proc` 디렉토리 구조 탐색

```bash
ls /dev/
ls /sys/class/
dmesg | grep -E 'gpio|uart|i2c'
udevadm info /dev/ttyS0
cat /proc/devices
```

- [ ] I2C 센서 또는 USB GPS 모듈 연결 후 `/dev` 노드 직접 읽기

```bash
i2cdetect -y 1
i2cget -y 1 0x48 0x00
```

#### 5-B: AOSP 빌드 및 라즈베리파이 플래싱 (2~4주)

```bash
repo init -u https://android.googlesource.com/platform/manifest
repo sync -j4
source build/envsetup.sh
lunch aosp_rpi4-userdebug
make -j$(nproc)
```

#### 5-C: VehicleHAL 커스터마이징 (3~5주)

- [ ] `hardware/interfaces/automotive/vehicle/` 구조 파악
- [ ] `DefaultVehicleHal` 구현체 분석
- [ ] AIDL 차량 HAL 구조와 Binder 서비스 등록 흐름 정리
- [ ] 레거시 HIDL Vehicle HAL 예제와 인터페이스 차이 비교
- [ ] GPIO 값을 `VEHICLE_PROPERTY`로 매핑하는 커스텀 HAL 작성

```cpp
int32_t gearValue = readGpioPin(GPIO_GEAR_PIN);
setValue(VehicleProperty::GEAR_SELECTION, gearValue);
```

#### Phase 5 달성 목표

| 달성 항목 | 면접 어필 내용 |
| --- | --- |
| `/dev` 노드 구조 탐색 | 리눅스 디바이스 레이어를 직접 탐색해봤다 |
| I2C 센서 직접 읽기 | 드라이버 없이 하드웨어 데이터를 읽어봤다 |
| AOSP 빌드 성공 | AOSP 빌드 시스템을 이해하고 직접 빌드했다 |
| VehicleHAL 코드 분석 | HAL이 Android 프레임워크와 어떻게 연결되는지 안다 |
| AIDL/HIDL 비교 설명 | 최신 AAOS와 레거시 HAL 경로 차이를 구조적으로 설명할 수 있다 |

---

## 4. 전체 일정

| Phase | 이름 | 핵심 산출물 | 기간 |
| --- | --- | --- | --- |
| 1 | AAOS 에뮬레이터 환경 | AVD 2종 + Car App Library 앱 실행 | 1~2주 |
| 2 | Geofence + 기어 감지 | 좌표 주입 -> UI 전환 데모 영상 V1 | 2~3주 |
| 2.5 | 차량 신호 추상화 + 안전 UI 실시간 가드 | 100ms 안전 UI 강등 데모 + 지연 로그 | 1~2주 |
| 2.6 | 중요 명령 워치독 + 복구 전략 | ACK timeout 대응, 전원 복구, 비상 모드 차단 데모 | 1~2주 |
| 3 | Firebase 실시간 동기화 | 메뉴 연동 + 직원 대시보드 | 3~4주 |
| 4 | Adaptive UI + 데모 완성 | 최종 데모 영상 + GitHub README | 4~5주 |
| 5 | 라즈베리파이 HAL 연동 | AOSP 빌드 + HAL 커스터마이징 | 5~12주 |

---

## 5. 에뮬레이터 vs 실기기

| 기능 | 에뮬레이터 | 라즈베리파이 | 비고 |
| --- | --- | --- | --- |
| Car App Library UI 개발 | Yes | Yes | 에뮬레이터로 충분 |
| GPS Geofence (좌표 주입) | Yes | Yes | `adb inject` 활용 |
| CarPropertyManager 기어 감지 | Yes | Yes | VHAL inject 활용 |
| 차량 신호 추상화 레이어 | Yes | Yes | 앱 레이어에서 공통 구조 검증 가능 |
| 안전 UI 100ms 강등 | Yes | Yes | 에뮬레이터 로그 기반 소프트 실시간 검증 |
| 중요 명령 watchdog | Yes | Yes | Fake IO 또는 로컬 서비스로 timeout/복구 검증 |
| Firebase 실시간 동기화 | Yes | Yes | 네트워크 연결 필요 |
| Adaptive UI 다해상도 | Yes | Yes | AVD 2종으로 검증 |
| `/dev` 노드 직접 접근 | No | Yes | Phase 5 필수 환경 |
| VehicleHAL 커스터마이징 | No | Yes | AOSP 빌드 필요 |
| 실제 CAN 버스 통신 | No | No | 실차 환경 필요 |
| OEM ccOS 연동 | No | No | 재직자 환경 필요 |

---

## 6. 면접 활용 전략

### 6.1 프로젝트 소개 한 줄

> "드라이브스루 진입 시 차량 CID가 자동으로 터치 메뉴판으로 전환되는 AAOS 기반 주문 시스템을 개발했습니다. CarPropertyManager로 기어 상태를 감지해 Driver Distraction 가이드라인을 준수하는 UI 분기를 구현했고, Firebase Realtime Database로 직원 화면과 실시간 양방향 동기화를 구현했습니다."

### 6.2 Phase별 예상 질문

| Phase | 예상 질문 | 답변 포인트 |
| --- | --- | --- |
| 1~2 | AAOS와 Android Auto의 차이가 뭔가요? | 실행 환경, 스마트폰 의존성, Car App Library 동작 방식 |
| 2 | Driver Distraction은 어떻게 처리했나요? | `CarPropertyManager.GEAR_SELECTION`으로 P단 감지 후 UI 분기 |
| 2.5 | 실시간 보장은 무엇을 구현했나요? | 주행 위험 신호 수신 후 100ms 이내 전체 메뉴를 차단하는 Safety UI Guard |
| 2.6 | IO 응답이 늦거나 전원이 꺼지면 어떻게 했나요? | 중요 명령을 journal에 기록하고 ACK watchdog, recovery, emergency mode 차단으로 모호한 상태를 제거 |
| 3 | 실시간 동기화를 왜 Firebase로 했나요? | 개발 속도, 오프라인 캐시, 구독 모델의 차량 환경 적합성 |
| 4 | 다해상도 대응은 어떻게 했나요? | Resource Qualifiers + Car App Library Template 선택 분기 |
| 5 | HAL이 뭔지 설명해보세요 | AIDL/HIDL VehicleHAL -> CarService -> CarPropertyManager 흐름 |

### 6.3 구현하지 않은 부분에 대한 답변

> "이번 프로젝트의 목적은 AAOS 플랫폼 레이어를 이해하고 Driver Distraction 가이드라인을 실제로 적용해보는 것이었습니다. 결제 연동은 Android Auto Intent 또는 Bluetooth RFCOMM으로 스마트폰 결제 앱에 딥링크를 보내는 방식으로 확장 가능한 구조로 설계했으며, 이번 버전에서는 의도적으로 IVI 레이어에 집중했습니다."

---

## 7. 학습 리소스

### 공식 문서

- [AAOS 개발자 가이드](https://developer.android.com/training/cars)
- [Car App Library 레퍼런스](https://developer.android.com/reference/androidx/car/app)
- [CarPropertyManager API](https://developer.android.com/reference/android/car/hardware)
- [Driver Distraction 가이드라인](https://developer.android.com/training/cars/design)

### AOSP 소스 (Phase 5)

- VehicleHAL: `hardware/interfaces/automotive/vehicle`
- CarService: `packages/services/Car`
- [Android-RPi 포트](https://github.com/raspberry-vanilla/android_local_manifest)

### 권장 학습 순서

1. [Car App Library 공식 샘플](https://github.com/android/car-samples) 클론 및 실행
2. AAOS AVD에서 샘플 앱 동작 확인 후 커스터마이징 시작
3. Binder IPC 동작 원리 학습
4. AOSP 소스에서 `CarService` -> `VehicleHAL` 호출 경로 직접 추적
5. 라즈베리파이 `/dev` 노드 실습 -> AOSP 빌드 -> HAL 커스터마이징 (Phase 5)

---

*DriveThru IVI - Hanzi-Cho*
