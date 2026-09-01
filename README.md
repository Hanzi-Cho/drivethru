# DriveThru IVI

AAOS 기반 드라이브스루 주문 포트폴리오 프로젝트입니다.

빠르게 볼 문서:
- 1분 시연 스크립트 + 단계별 에뮬레이터 실행 + 스크린샷 포인트: [docs/demo-runbook.md](docs/demo-runbook.md)
- 검증 기준 / failure scenario / 혼합 센서 정책: [docs/validation-strategy.md](docs/validation-strategy.md)

면접에서 가장 짧게 보여줄 핵심:
1. `GPS approach -> PARK -> GPS ready -> FULL_MENU`
2. `주문 중 DRIVE -> STOP_STATE`
3. `고속 또는 EXIT -> STANDBY 복귀`

현재 상태:
- 기획 문서 작성 완료
- Android Automotive 에뮬레이터 및 `showcase.automotive` 샘플 실행 확인
- `drivethru` 초기 커밋 완료
- 루트 Gradle 프로젝트 기준 `app.automotive` 빌드/설치/실행 확인
- Step 2~5 범위의 상태 모델, 메뉴 분기, 주문 초안, ADB 스크립트 골격 구현 완료
- 비콘/지오펜스 매장 진입 시 메뉴 데이터를 Room DB(SSOT)로 로컬 우선 캐싱하고, 서버 동기화는 suspend + IO 디스패처로 백그라운드에서 처리하도록 전환 완료 ([docs/architecture.md](docs/architecture.md) 참고)

문서:
- 면접/시연용 단일 실행 가이드: [docs/demo-runbook.md](docs/demo-runbook.md)
- 프로젝트 기획: [docs/project-plan.md](docs/project-plan.md)
- 현재 권장 구조 및 구현 순서: [docs/architecture.md](docs/architecture.md)
- Step 2~6 구현 명세: [docs/implementation-spec-step2-6.md](docs/implementation-spec-step2-6.md)
- 검증 전략 / 실패 시나리오 / 혼합 센서 정책: [docs/validation-strategy.md](docs/validation-strategy.md)
- logging / diagnostics / error handling 정책: [docs/logging-error-handling-policy.md](docs/logging-error-handling-policy.md)
- Voice AI 확장 기획: [docs/voice-ai-integration-plan.md](docs/voice-ai-integration-plan.md)
- ADB 실행 스크립트 안내: [tools/adb/README.md](tools/adb/README.md)

핵심 목표:
- AAOS Car App Library 기반 차량 주문 UX 구현
- Driver Distraction 대응 UI 분기
- 직원 화면과의 실시간 메뉴/주문 동기화
- 면접에서 바로 시연 가능한 3~5분 데모 완성

현재 구현 범위:
- `WaitingForEntry` 화면 렌더링
- `FakeGearStateDataSource` 기반 `DRIVE` / `PARK` 상태 전환
- 비PARK 상태 `SimplifiedMenu`
- `PARK` 상태 `FullMenu`
- 매장 진입 시 Room에 캐시된 메뉴를 즉시 노출하고, 서버(`TenantCatalogRepository`) 응답이 오면 Room을 갱신해 화면에 자동 반영 (오프라인/네트워크 지연 시에도 최근 방문 메뉴로 폴백, 캐시가 없으면 `FakeMenuRepository`로 폴백)
- 메뉴 선택 후 `OrderReviewScreen` 진입

실행 방법:
```powershell
cmd /c gradlew.bat :app:automotive:installDebug
adb shell monkey -p com.hanzi.drivethru -c android.intent.category.LAUNCHER 1
```

또는 스크립트 사용:
```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\install-automotive.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\adb\run-automotive.ps1
```

현재 데모 시나리오:
1. 앱 실행 후 `WaitingForEntry` 화면 확인
2. `Set DRIVE` 또는 `Set PARK`로 가짜 기어 상태 변경
3. `Enter demo store`로 매장 진입
4. `DRIVE`면 간소화 메뉴, `PARK`면 전체 메뉴 확인
5. 메뉴 1개 선택 후 주문 검토 화면 진입

현재 검증 기준:
- 루트 프로젝트에서 `:app:automotive:assembleDebug` 성공
- `installDebug` 성공
- `adb shell monkey ...`로 실행 성공
- 에뮬레이터에서 `com.hanzi.drivethru`가 포그라운드 실행 상태

다음 단계:
- Step 6 기준으로 문서와 구현 상태를 계속 동기화
- 이후 Step 7에서 실제 `CarPropertyManager` 연동 검토
- Geofence 및 Firebase 연동은 그 다음 단계에서 진행
- Voice AI 확장 시 `Upstage Solar` 기반 텍스트 챗 -> STT/TTS 순으로 단계적 검증
