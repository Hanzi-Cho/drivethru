# VHAL / CarService Data Sources

## 1. 문서 목적

이 문서는 `car/drivethru`가 앱 데모를 넘어 `CarService`, `VHAL`, `CAN -> VehicleProperty` 변환 실험까지 확장될 때 활용할 수 있는 가상/실차 데이터 소스를 정리하기 위한 참고 자료다.

핵심 질문:
- VHAL 또는 CarService에서 활용할 차량 데이터는 어디서 구할 수 있는가
- AOSP 내부 Mock만으로 어디까지 검증할 수 있는가
- 실차 CAN 데이터셋은 어떤 용도로 써야 하는가

## 2. 우선 활용할 소스

### 2.1 AOSP DefaultConfig / FakeVehicleHal

가장 먼저 써야 하는 입력 소스는 AOSP 자체 Mock 구현이다.

주요 위치:
- `packages/services/Car/`
- `hardware/interfaces/automotive/vehicle/`

활용 포인트:
- 속도, HVAC, 기어, 배터리, Door, Seat 등 표준 VehicleProperty 시뮬레이션
- `adb shell cmd car_service inject-vhal-event ...`를 통한 실시간 이벤트 주입
- 앱 / CarPropertyManager / CarService 경계 검증의 가장 빠른 시작점

추천 용도:
- `CarPropertyManager` 구독 흐름 검증
- `VehicleSignalProvider`와 안전 상태 머신 검증
- `Safety UI Downgrade Guard` 데모 입력

### 2.2 Android Emulator + `cmd car_service`

별도 외부 데이터셋 없이도 가장 빠른 시나리오 재현이 가능하다.

예시:
```bash
adb shell cmd car_service inject-vhal-event 0x11400400 4
adb shell cmd car_service inject-vhal-event 0x11600207 0
```

추천 용도:
- `PARK`, `DRIVE`, 속도 변화, Door / HVAC 등 이벤트 기반 UI 차단 테스트
- shell 기반 시나리오 자동화

## 3. Linux / Standalone Mock VHAL

### 3.1 `ndravr/android-vhal-linux-mock`

특징:
- AIDL VHAL 사양 기반 Standalone Mock / Stub
- Linux 환경에서 QNX, 실제 CAN 없이도 VHAL 이벤트 흐름 테스트 가능

활용 포인트:
- HAL 계층만 따로 떼어낸 실험
- SocketCAN이나 사용자 정의 replay 입력을 태우는 중간 계층 프로토타이핑

추천 용도:
- `Fake VHAL -> CarService` 수준 실험
- vendor property 추가 전 구조 검증

## 4. 실차 CAN 버스 데이터셋

### 4.1 CANdid

특징:
- 실제 차량에서 수집된 주석 포함 CAN 트래픽 데이터셋
- 조향, 가속, 브레이크, HVAC 조작 등 다양한 시나리오 포함

활용 포인트:
- `CAN ID / payload -> VehicleProperty` 파서 검증
- replay 기반 VHAL 입력 테스트

추천 용도:
- `SocketCAN / vcan0 -> parser -> VHAL` 파이프라인
- 속도, 브레이크, 차문 등 실차 패턴 기반 변환 로직 검증

### 4.2 `commaai/opendbc`

특징:
- 현대, 기아, 도요타, 테슬라 등 다양한 차종 DBC 파일 제공
- CAN ID, scaling, offset 정보 포함

활용 포인트:
- Raw CAN 로그를 사람이 해석 가능한 Vehicle Signal로 변환하는 기준
- 실차 로그와 함께 사용할 때 가치가 큼

추천 용도:
- DBC 기반 decoder 작성
- vendor property 후보 정의 전 표준 신호 모델링

### 4.3 OVMS / Open Vehicles

특징:
- OBD2 / CAN 기반 차량 로그, 오픈 차량 모니터링 데이터 흐름

활용 포인트:
- EV / 내연기관 계열 telemetry 흐름 참고
- CarService 상단 서비스나 dashboard에서 장기 telemetry 시나리오 실험 가능

## 5. 생성 / 자동화 도구

### 5.1 `feevlic/vhal-mcp-server`

특징:
- VHAL property 구조, AIDL/HAL 코드 생성 및 디버깅 보조

활용 포인트:
- 커스텀 vendor property 정의 자동화
- 실험용 VHAL 스캐폴드 생성

### 5.2 Synthetic Vehicle Telemetry Datasets

특징:
- Kaggle 류의 synthetic tabular telemetry 데이터

활용 포인트:
- VHAL 자체보다 상위 서비스 / AI 분석 / 예측 시나리오 실험
- 배터리, mileage, maintenance signal 등의 시계열 검증

## 6. 이 프로젝트에 맞는 추천 사용 순서

1. AOSP Emulator + `inject-vhal-event`
- 앱 / CarPropertyManager / 상태 머신 먼저 검증

2. Standalone Mock VHAL 또는 `FakeVehicleHardware`
- CarService, vendor property, callback 구조 검증

3. CANdid + opendbc + `vcan0` replay
- 실제 신호 변환 파이프라인 검증

4. 필요 시 synthetic telemetry
- 상위 analytics / AI 보조 기능 실험

## 7. DriveThru 적용 관점

`car/drivethru`에서는 아래 매핑이 가장 중요하다.

- 속도 -> `STOP_STATE` 진입 조건
- 기어 -> full ordering 허용 여부
- Door / parking brake / ignition -> 향후 안전 정책 확장 조건
- geofence / beacon / simulator event -> `StoreResolver` 입력

즉 이 프로젝트에서 실차 데이터의 목적은 "차량 앱처럼 보이는 UI"가 아니라 아래를 증명하는 데 있다.

- Vehicle Signal 추상화가 실제 데이터 입력원 교체에도 유지되는가
- `CAN -> VHAL -> CarService -> App` 경계가 설계상 분리되어 있는가
- 앱이 플랫폼 오류나 잘못된 데이터에도 fail-safe로 동작하는가
