# AOSP AAOS Vehicle Reference

`car/drivethru`에서 VHAL, Car Service, `CarPropertyManager` 연동 구조를 확인할 때 참고하는 최소 AOSP 체크아웃 경로입니다.

## 체크아웃 위치

- `D:\agentproject\study\aosp-aaos-vehicle-reference\packages-services-Car`
  - `car-lib/src/android/car/VehiclePropertyIds.java`
  - `car-lib/src/android/car/hardware/property/CarPropertyManager.java`
  - `service/src/com/android/car/CarShellCommand.java`
  - `service/src/com/android/car/hal/PropertyHalService.java`
- `D:\agentproject\study\aosp-aaos-vehicle-reference\hardware-interfaces-automotive-vehicle`
  - `automotive/vehicle/2.0/default/impl/vhal_v2_0/DefaultConfig.h`
  - `automotive/vehicle/2.0/default/impl/vhal_v2_0/DefaultVehicleHal.cpp`
  - `automotive/vehicle/aidl/impl/current/default_config/config/DefaultProperties.json`
  - `automotive/vehicle/aidl/android/hardware/automotive/vehicle/IVehicle.aidl`

## 이 프로젝트에서의 사용 방식

- 실제 HAL을 직접 수정하지 않고, 앱 내부 `SafeCarDataFacade`와 `FakeCarDataFacade`를 통해 fallback 가능한 구조를 유지합니다.
- 에뮬레이터 검증은 `adb shell am broadcast` 기반 inject를 우선 사용하고, 가능할 때만 `cmd car_service inject-vhal-event`를 병행합니다.
- GPS, 비콘, 속도, 주차 상태 입력은 앱의 `DriveThruDebugInjectReceiver`가 받아 `CustomUiFlowCoordinator`로 전달합니다.
