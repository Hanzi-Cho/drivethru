# ADB Scripts

이 디렉터리는 `car/drivethru` 루트 기준 AAOS 에뮬레이터 검증 루틴을 고정하는 스크립트 모음입니다.

## 실행 위치

아래 스크립트는 어느 경로에서 실행해도 내부에서 저장소 루트로 이동합니다.

권장 예시:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\install-automotive.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\run-automotive.ps1
```

## 스크립트 목록

- `install-automotive.ps1`
  - `:app:automotive:installDebug` 실행
- `run-automotive.ps1`
  - 설치된 앱 실행
- `inject-debug-event.ps1`
  - 단일 broadcast로 속도, 기어, parking, GPS, beacon, zone stage를 주입
- `inject-gear-park.ps1`
  - 앱 디버그 브로드캐스트로 `PARK` 상태를 주입
- `inject-gear-drive.ps1`
  - 앱 디버그 브로드캐스트로 `DRIVE` 상태를 주입
- `inject-store-approach-gps.ps1`
  - McDonald's Gangnam DT 지오펜스 접근 상태를 GPS로 주입
- `inject-store-ready-gps.ps1`
  - 메뉴보드 도달 상태를 GPS로 주입
- `inject-beacon-menu-board.ps1`
  - 매장 비콘 인식 상태를 주입
- `inject-store-exit.ps1`
  - 매장 이탈을 주입해 세션을 종료
- `inject-high-speed-abort.ps1`
  - 안전 속도 임계치 초과를 주입해 세션을 강제 종료
- `emu-geo-fix-mcd-gangnam.ps1`
  - 에뮬레이터 GPS를 강남 DT 좌표로 이동

## 전제 조건

- `adb devices`에서 에뮬레이터가 `device` 상태여야 함
- Android Automotive 에뮬레이터가 이미 실행 중이어야 함
- `gradlew.bat`가 루트 프로젝트에서 정상 동작해야 함

## 기본 예시

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\inject-store-approach-gps.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\inject-gear-park.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\inject-store-ready-gps.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\inject-high-speed-abort.ps1
```

## 입력 규약

- 공통 액션: `com.hanzi.drivethru.action.INJECT_DEBUG_EVENT`
- 주요 extra
  - `source`: `gps`, `beacon`, `vehicle`
  - `stage`: `APPROACHING`, `IN_ZONE`, `ORDERING_READY`, `EXIT`
  - `lane_point`: `ENTRANCE`, `MENU_BOARD`, `PAYMENT_WINDOW`, `PICKUP_WINDOW`
  - `gear`: `PARK`, `DRIVE`, `REVERSE`, `NEUTRAL`
  - `parking`: `true|false`
  - `speed_mps`: 예) `0.0`, `3.5`, `8.2`
  - `latitude`, `longitude`
  - `beacon_id`

## 현재 제약

- Google Play Automotive 이미지에서는 `cmd car_service inject-vhal-event`가 `user build` 제약으로 막힐 수 있습니다.
- 그래서 기본 검증 경로는 앱 브로드캐스트 inject이며, 에뮬레이터 GPS 반영은 `adb emu geo fix`를 보조적으로 사용합니다.
