# Shell To VHAL Command Flow

## 1. 문서 목적

이 문서는 `adb shell cmd car_service inject-vhal-event ...` 명령이 어떤 경로로 CarService와 VHAL까지 도달하는지, 그리고 왜 shell이 "외부에서 내부 시스템으로 명령을 주입하는 껍질 레이어"라고 볼 수 있는지 아키텍처 관점에서 정리한다.

이 문서는 `car/drivethru`에서 다음 설명 포인트를 뒷받침한다.
- shell 기반 차량 이벤트 주입이 어떤 레이어를 통과하는가
- 개발자용 디버그 입력과 실제 플랫폼 입력은 어떻게 대응되는가
- 앱 레이어에서 shell 자체를 직접 쓰는 것이 아니라, 그 결과 이벤트를 `VehicleSignalProvider`로 해석하는 이유는 무엇인가

## 2. Shell 개념 정리

이 문서에서 shell은 단순히 bash 같은 CLI를 의미하는 것이 아니라, 외부 사용자 또는 개발자가 내부 시스템 상태를 제어할 수 있도록 열어둔 "명령 주입 인터페이스 레이어"로 해석한다.

핵심 역할:
- 외부 입력 문자열 또는 명령을 수신
- 문법과 인자를 해석
- 하위 시스템이 이해하는 IPC 또는 syscall로 번역
- 내부 시스템에 안전한 방식으로 전달

즉 shell은 "커널이나 서비스 자체"가 아니라, 외부와 내부를 잇는 표준화된 접점이다.

## 3. 전체 흐름

```text
Host PC
  -> adb client
  -> adbd on device
  -> /system/bin/cmd
  -> Binder IPC
  -> system_server
  -> CarService shell command handler
  -> CarProperty / Property HAL service
  -> IVehicle / VHAL daemon
  -> property event callback
  -> CarPropertyManager callback
  -> DriveThru app state update
```

## 4. 단계별 설명

### 4.1 Host PC -> `adbd`

개발자가 터미널에서 `adb shell cmd car_service inject-vhal-event ...`를 실행하면, 명령은 USB 또는 TCP를 통해 기기 내부 `adbd` 데몬으로 전달된다.

### 4.2 `adbd` -> `/system/bin/cmd`

`adbd`는 전달받은 명령을 실행하기 위해 Android 기기 안의 `cmd` 프로세스를 `fork/exec` 형태로 띄운다.

`cmd`는 자체 로직을 많이 가지기보다 "특정 시스템 서비스로 shell command를 전달하는 래퍼"에 가깝다.

### 4.3 `cmd` -> `system_server`

`cmd`는 Binder를 통해 system service로 명령을 넘긴다.

이 단계에서 전달되는 것:
- service 이름
- 인자 목록
- stdin / stdout / stderr file descriptor

### 4.4 `system_server` -> CarService shell handler

CarService는 자신에게 들어온 shell command를 파싱한다.

여기서 `inject-vhal-event` 같은 서브커맨드를 해석하고:
- property id
- area id
- value
- inject 방식
을 내부 서비스 레이어로 넘긴다.

### 4.5 CarService -> Property HAL / VHAL

CarService 또는 Property HAL 계층은 파싱된 값을 실제 차량 속성 이벤트처럼 취급하여 VHAL 또는 fake event generator에 전달한다.

이때 AIDL 기반 최신 구조에서는:
- Java Framework
- AIDL Binder
- Native VHAL daemon
으로 경계를 넘는다.

### 4.6 VHAL -> 상위 callback 역전파

VHAL이 해당 프로퍼티를 업데이트하면, 이를 property change event로 다시 상위 레이어에 notify 한다.

결과적으로:
- CarPropertyService
- CarPropertyManager callback
- 앱 레이어의 `VehicleSignalProvider`
까지 이벤트가 역으로 올라온다.

## 5. DriveThru 프로젝트에서의 의미

이 프로젝트에서는 shell 명령을 앱이 직접 처리하는 것이 목적이 아니다.

중요한 것은 다음 매핑이다.

```text
Shell / Simulator / CAN Replay
  -> VehicleSignalProvider
  -> DriveThruSafetyPolicy
  -> OrderingSessionController
  -> Custom UI transition
```

즉 shell은 "앱 기능"이 아니라 "플랫폼 디버그 입력원"이다.

따라서 `car/drivethru`에서 shell을 해석할 때 기본값은 아래와 같다.

- shell = 개발자나 테스트 도구가 차량 이벤트를 외부에서 주입하는 접점
- 앱은 shell 문법을 아는 것이 아니라, shell 결과로 발생한 의미 있는 차량 상태만 안다
- 실제 양산 환경에서는 shell 대신:
  - CarService
  - VHAL
  - CAN parser
  - geofence / beacon provider
  같은 실제 입력원이 같은 의미의 데이터를 공급한다

## 6. 포트폴리오 설명 포인트

면접에서는 아래 식으로 설명하는 것이 가장 깔끔하다.

> `adb shell cmd car_service ...`는 앱이 직접 이해하는 명령어가 아니라, Android 시스템 서비스와 VHAL에 차량 이벤트를 주입하기 위한 개발자용 shell entrypoint다. 앱 쪽에서는 이 하위 입력원을 `VehicleSignalProvider` 뒤에 추상화해서, shell이든 fake simulator든 실제 CarPropertyManager든 동일한 상태 머신으로 처리하도록 설계했다.

## 7. 이 문서를 왜 분리하나

이 흐름은 일반 앱 구조 문서에 묻히면 잘 안 보인다.

따라서 `docs/architecture/` 아래에 별도 보관해:
- shell 개념
- Binder IPC
- CarService 경계
- VHAL callback 역전파
를 따로 설명할 수 있게 유지하는 편이 좋다.
