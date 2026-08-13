# AAOS Emulator Validation Playbook

`car/drivethru`의 현재 GPS/BLE/vehicle inject 구조를 AAOS 에뮬레이터에서 검증하는 표준 순서입니다.

## 1. 준비

```powershell
cd D:\agentproject\car\drivethru
adb devices
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\install-automotive.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\run-automotive.ps1
```

확인 포인트:
- `adb devices`에 AAOS 에뮬레이터가 `device` 상태로 보여야 합니다.
- 앱 최초 실행 시 위치/BLE 권한을 허용합니다.

## 2. 핵심 시나리오

### 시나리오 A: GPS 진입 후 메뉴 진입

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\inject-store-approach-gps.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\inject-gear-park.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\inject-store-ready-gps.ps1
```

검증 기준:
- `STORE_READY` 또는 `FULL_MENU`로 진입
- `McDonald's Gangnam DT` 메뉴 로드
- zone stage가 `APPROACHING`에서 `ORDERING_READY`로 전이

### 시나리오 B: 비콘만으로 주문 UI 진입

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\inject-gear-park.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\inject-beacon-menu-board.ps1
```

검증 기준:
- `entryTriggerEvent.source == BEACON`
- GPS 없이도 매장 식별 및 메뉴 로드
- FULL_MENU 진입 가능

### 시나리오 C: 주문 중 차량 출발

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\inject-gear-drive.ps1
```

검증 기준:
- `STOP_STATE` 전이
- draft 보존
- resume 전까지 주문 차단

### 시나리오 D: 고속 중단

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\inject-high-speed-abort.ps1
```

검증 기준:
- 세션 종료
- STANDBY 복귀
- status message에 safety speed threshold 문구 표시

### 시나리오 E: 매장 이탈

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\inject-store-exit.ps1
```

검증 기준:
- active store 제거
- order draft 제거
- 대기 화면 복귀

## 3. 에뮬레이터 GPS 직접 이동

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\emu-geo-fix-mcd-gangnam.ps1
```

또는 수동:

```powershell
adb emu geo fix 127.0276 37.4979
```

## 4. 브라우저 시뮬레이터 사용

`dashboard/web`에서 시뮬레이터를 띄우면 각 시나리오에 대응하는 payload와 `adb shell am broadcast` 명령을 바로 확인할 수 있습니다.

```powershell
cd D:\agentproject\car\drivethru\dashboard\web
python -m http.server 4173
```

브라우저:
- `http://localhost:4173`

## 5. 현재 한계

- 브라우저는 직접 `adb`를 실행하지 않으므로 현재는 명령 생성기 역할만 합니다.
- BLE는 실제 beacon 송신 장치 또는 mock peripheral 없이는 실환경 검증이 제한됩니다.
- `CarPropertyManager` 실차량 값과 geofence/BLE 자동 수신을 동시에 보려면 AAOS 에뮬레이터보다 실장비 환경이 더 유리합니다.
