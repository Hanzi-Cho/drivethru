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
- `inject-gear-park.ps1`
  - VHAL 기어 상태를 `PARK`로 주입
- `inject-gear-drive.ps1`
  - VHAL 기어 상태를 `DRIVE`로 주입

## 전제 조건

- `adb devices`에서 에뮬레이터가 `device` 상태여야 함
- Android Automotive 에뮬레이터가 이미 실행 중이어야 함
- `gradlew.bat`가 루트 프로젝트에서 정상 동작해야 함

## 현재 제약

- 현재 사용 중인 Google Play Automotive 이미지에서는 `inject-vhal-event`가 `user build` 제약으로 막힐 수 있습니다.
- 이 경우 Step 2~4 검증은 앱 내부 `Set DRIVE`, `Set PARK` 디버그 액션으로 진행합니다.
