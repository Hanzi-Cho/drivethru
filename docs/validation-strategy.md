# DriveThru Validation Strategy

이 문서는 `car/drivethru`의 현재 아키텍처 수준에서 수행 가능한 검증 범위, 실패 시나리오, 혼합 센서 정책, CI/hook 권고안을 한 곳에 정리한 기준 문서다.

중요 전제:
- 이 문서는 `ISO 26262 인증 문서`가 아니다.
- 대신 `ISO 26262-inspired functional safety mindset`를 현재 포트폴리오/프로토타입 프로젝트에 맞게 적용한 검증 설계 문서다.
- 면접, README, 데모 영상, 향후 파괴식 검증의 공통 기준으로 사용한다.

## 1. 검증 목적

이 프로젝트에서 검증해야 하는 핵심 질문은 아래 4가지다.

1. 차량 상태가 바뀌면 UI가 안전하게 제한되는가
2. GPS/BLE/HAL 입력이 흔들려도 세션이 일관되게 유지되는가
3. 입력 실패 시 앱이 죽지 않고 fallback / degrade 되는가
4. 검증 결과를 문서, 로그, 테스트로 재현 가능하게 남길 수 있는가

## 2. 검증 레벨

현재 아키텍처에서 권장하는 검증 계층은 아래 5단계다.

### 2.1 Pure Logic

대상:
- `DriveThruSafetyPolicy`
- `CustomUiFlowCoordinator`
- `StopStatePolicy`
- `StoreResolver`
- tenant menu / store schema

특징:
- 가장 빠름
- 수천, 수만, 수백만 회 반복 검증 가능
- 상태 전이 불변식 검증에 적합

### 2.2 Contract / Adapter

대상:
- `VehicleSignalProvider`
- `EntryTriggerProvider`
- `CarDataFacade`
- `SafeCarDataFacade`

특징:
- fake / real provider가 같은 계약을 지키는지 확인
- fallback / source / status / diagnostics 일관성 검증

### 2.3 Android Runtime

대상:
- `DriveThruActivity`
- 위치/BLE 권한 요청
- lifecycle recreate
- background / foreground 전환

특징:
- JVM 테스트로 못 잡는 Android 런타임 제약 확인

### 2.4 AAOS Runtime

대상:
- `adb shell am broadcast`
- `adb emu geo fix`
- Car App / Compose Custom UI 연계
- diagnostics panel / state transition 표시

특징:
- 실제 시연용 증거 확보
- README / 영상용 근거 생산

### 2.5 Destructive / Failure Injection

대상:
- stale GPS
- wrong beacon
- duplicate beacon
- HAL exception
- permission revoke
- process restart
- signal conflict

특징:
- Amazon 스타일 파괴식 검증 사고방식 적용
- 정상 동작보다 실패 동작의 예측 가능성을 본다

## 3. ISO 26262-Inspired Checklist

이 체크리스트는 `ISO 26262 exact compliance checklist`가 아니라, 현재 프로젝트가 따라야 할 안전 사고방식 체크리스트다.

### 3.1 Item / Scope

- [ ] 시스템 아이템이 명확한가
- [ ] 시스템 경계가 정의되어 있는가
- [ ] OEM 권한 필요 기능과 일반 앱 가능 기능이 분리되어 있는가

현재 기준:
- 아이템: `차량 상태 기반 드라이브스루 주문 UX`
- 시스템 경계:
  - 앱 내부 상태 머신
  - GPS / BLE / CarProperty 입력
  - 실제 결제 / POS / OEM privileged control은 제외

### 3.2 Hazard Thinking

- [ ] 주행 중 full ordering UI 유지가 위험 시나리오로 정의되어 있는가
- [ ] stale sensor로 잘못된 매장 활성화가 위험 시나리오로 정의되어 있는가
- [ ] 센서 conflict로 세션이 흔들리는 상황이 정의되어 있는가
- [ ] fallback 시 사용자 오인 가능성이 정의되어 있는가

### 3.3 Safety Goals

- [ ] 차량이 안전 상태가 아니면 full ordering UI를 유지하지 않는다
- [ ] speed threshold 초과 시 주문 세션을 종료한다
- [ ] DRIVE 전환 시 draft는 보존하되 UI는 STOP_STATE로 내린다
- [ ] HAL unavailable이어도 앱이 crash 하지 않는다
- [ ] 잘못된 beacon / GPS 입력으로 store activation thrash가 발생하지 않는다

### 3.4 Functional Safety Concept

- [ ] 센서별 신뢰도와 우선순위가 정의되어 있는가
- [ ] conflict resolution 정책이 정의되어 있는가
- [ ] fallback / degrade / abort 정책이 구분되어 있는가

현재 권장 구분:
- `fallback`: HAL read 실패 -> fake source 사용
- `degrade`: GPS stale / BLE timeout -> STORE_READY 또는 STANDBY 유지
- `abort`: speed threshold 초과 / EXIT -> 세션 종료

### 3.5 Technical Safety Concept

- [ ] `VehicleSignalProvider`가 UI와 분리되어 있는가
- [ ] `EntryTriggerProvider`가 UI와 분리되어 있는가
- [ ] `SafeCarDataFacade`로 primary failure isolation이 있는가
- [ ] diagnostics / status source 노출이 가능한가

### 3.6 Verification Evidence

- [ ] 단위 테스트가 존재하는가
- [ ] 상태 전이 테스트가 존재하는가
- [ ] fallback 테스트가 존재하는가
- [ ] 에뮬레이터 validation playbook이 존재하는가
- [ ] README / 영상에 시나리오 증거를 남길 수 있는가

### 3.7 Gaps To Close

현재 부족한 부분:
- [ ] hazard table 정량화 미흡
- [ ] safety requirement traceability 표 미흡
- [ ] 100ms guard 실제 계측 리포트 없음
- [ ] lifecycle / process death / permission revoke 자동 검증 부족
- [ ] chaos / fuzz / soak test 자동화 부족

## 4. Mixed Sensor Conflict Policy

GPS와 BLE가 서로 다른 매장을 가리키는 상황은 면접과 실제 검증에서 반드시 다뤄야 하는 핵심 시나리오다.

### 4.1 정책 목표

- UI가 매장 A와 B 사이에서 흔들리지 않아야 한다
- 세션 시작 이후 active store가 임의로 바뀌면 안 된다
- 센서 신뢰도 차이를 정책으로 설명 가능해야 한다

### 4.2 권장 우선순위

세션 시작 전:
- `BEACON > GPS`

이유:
- beacon은 근접성이 더 높고 menu board / pickup window 같은 근거리 판별에 적합
- GPS는 접근 감지에는 좋지만 fine-grained lane resolution은 약함

세션 시작 후:
- `active store lock`

이유:
- 주문 도중 sensor jitter로 store가 바뀌면 UX와 주문 무결성이 깨진다

### 4.3 세부 정책

1. 세션이 없을 때
- GPS가 먼저 `APPROACHING` 또는 `IN_ZONE`를 만들 수 있다
- 이후 BLE가 다른 매장을 가리키면 BLE를 우선한다

2. 세션이 시작된 후
- GPS/BLE가 다른 매장을 가리켜도 현재 `activeStore`를 유지한다
- diagnostics에 `sensor_conflict`를 남긴다
- status message는 “Sensor conflict detected. Active store locked.” 같은 메시지로 표준화한다

3. 세션 종료 후
- lock 해제
- 다음 진입 이벤트부터 다시 arbitration 수행

### 4.4 구현 권고

- `SensorConfidencePolicy`
- `SensorConflictRecord`
- `activeStoreLockedUntilSessionEnd`
- `lastAcceptedTriggerSource`

### 4.5 검증 포인트

- GPS는 A, BLE는 B -> 세션 전에는 BLE 우선
- 세션 중 GPS는 B, BLE는 A -> active store 유지
- duplicate conflict 반복 -> UI thrash 금지
- conflict 발생 시 diagnostics/log event 생성

## 5. Failure Scenario Matrix

아래 표는 현재 프로젝트에서 실제로 검증 가치가 높은 failure scenario 목록이다.

| ID | 범주 | 시나리오 | 입력/주입 방법 | 기대 결과 | 검증 레벨 | 현재 상태 |
| --- | --- | --- | --- | --- | --- | --- |
| F-01 | GPS | GPS 접근 이벤트 지연 | delayed fake GPS event / emulator geo fix 지연 | `APPROACHING` 유지, premature `ORDERING_READY` 금지 | Logic / AAOS | 부분 가능 |
| F-02 | GPS | GPS 오차 반경이 커서 store 중심점에서 흔들림 | tenant geofence 오차 주입 | `APPROACHING`/`IN_ZONE` 단계가 보수적으로 유지 | Logic | 미구현 |
| F-03 | GPS | background 위치 제한으로 update 빈도 저하 | runtime permission / background 제한 | status degrade, stale location 처리 | Android Runtime | 미구현 |
| F-04 | BLE | 잘못된 beacon id 검출 | fake scan result | store load 금지 | Logic / Android | 미구현 |
| F-05 | BLE | 중복 beacon scan 폭주 | repeated scan callback | 세션 중복 시작 금지 | Logic / Android | 미구현 |
| F-06 | BLE | beacon 미검출 지속 | timeout | GPS fallback 또는 standby 유지 | Logic | 미구현 |
| F-07 | HAL | `CarPropertyManager` read exception | fake primary exception | fallback source 사용, UI 유지 | Contract / AAOS | 구현됨 |
| F-08 | HAL | HAL unavailable 연속 발생 | repeated exception | diagnostics에 fallback 지속 표시 | Contract | 부분 구현 |
| F-09 | Vehicle | 주문 중 `DRIVE` 전환 | inject drive | `STOP_STATE`, draft 보존 | Logic / AAOS | 구현됨 |
| F-10 | Vehicle | speed threshold 초과 | inject high speed | session abort, standby 복귀 | Logic / AAOS | 구현됨 |
| F-11 | Session | `EXIT` 이벤트 | inject exit | active store 제거, draft 제거, standby | Logic / AAOS | 구현됨 |
| F-12 | Conflict | GPS는 A, BLE는 B | fake mixed triggers | 세션 전 BLE 우선 | Logic | 미구현 |
| F-13 | Conflict | 세션 중 다른 매장 감지 | fake mixed triggers | active store lock 유지 | Logic | 미구현 |
| F-14 | Lifecycle | activity recreate | config change / recreate | view state 일관성 유지 | Android Runtime | 미구현 |
| F-15 | Permission | 위치/BLE 권한 거부 | runtime permission deny | provider start 실패해도 crash 없음 | Android Runtime | 부분 구현 |
| F-16 | Process | process restart | force-stop / relaunch | session recovery 또는 명확한 reset | Android Runtime / AAOS | 미구현 |

## 6. Validation Matrix

README와 영상에는 아래 검증 매트릭스 중 통과된 항목만 선별해 넣는다.

| Feature | Input | Expected State | Evidence | Automation Target |
| --- | --- | --- | --- | --- |
| GPS approach | `inject-store-approach-gps.ps1` | `STORE_READY` 또는 접근 상태 | 화면 캡처, diagnostics | PowerShell smoke |
| GPS ready + PARK | `inject-store-ready-gps.ps1` + `inject-gear-park.ps1` | `FULL_MENU` | 영상, UI 상태 표시 | PowerShell smoke |
| Beacon ready | `inject-beacon-menu-board.ps1` | beacon source 기반 store activation | diagnostics, UI | Fake scan / runtime |
| DRIVE during order | `inject-gear-drive.ps1` | `STOP_STATE`, draft 보존 | 테스트, 영상 | unit + smoke |
| High speed abort | `inject-high-speed-abort.ps1` | `STANDBY`, session close | 테스트, 영상 | unit + smoke |
| Store exit | `inject-store-exit.ps1` | store/draft clear | 테스트, 영상 | unit + smoke |
| HAL fallback | fake exception in primary facade | source=`FAKE`, status=`FALLBACK` | diagnostics, unit test | unit |
| Mixed sensor conflict | fake GPS/BLE disagreement | policy-consistent arbitration | logs, unit test | unit |
| Permission denied | deny location/BLE permission | no crash, degraded mode | runtime log | Android runtime |

## 7. Amazon-Style Destructive Validation

이 프로젝트에서 말하는 “아마존식 파괴 검증”은 인프라를 무조건 크게 터뜨리는 것이 아니라, 시스템이 흔들릴 때 상태가 예측 가능한지를 보는 방식이다.

작게 재현 가능한 추천 방식:

### 7.1 Event Storm

- 랜덤 event sequence 생성
- 이벤트 종류:
  - `GPS_APPROACH`
  - `GPS_READY`
  - `BEACON_READY`
  - `PARK`
  - `DRIVE`
  - `SPEED_HIGH`
  - `SPEED_ZERO`
  - `EXIT`

검증 불변식:
- `speed >= threshold`면 최종 상태는 `STANDBY`
- `DRIVE` 상태에서 `FULL_MENU` 유지 금지
- `EXIT` 후 `activeStore == null`
- `STOP_STATE` 후 draft 손실 금지

### 7.2 Fault Injection

- `CarPropertyManager` exception
- stale GPS timestamp
- unknown beacon id
- duplicate beacon burst
- permission denied

### 7.3 Soak / Repetition

- 10,000 ~ 1,000,000회의 pure logic 반복
- 실제 AAOS emulator는 적은 수의 고가치 시나리오만 반복

권장 이유:
- 실무에서도 “수천만 번”은 순수 로직이나 parser/state machine에 적용하고
- 디바이스/E2E는 훨씬 적은 수의 시나리오를 반복하는 방식이 일반적이다

## 8. Hooks / CI / Workflow 권고

현재 저장소에는 `.husky`와 `.github/workflows`가 아직 없다. 지금 단계에서는 아래 정도를 추가하는 것을 권장한다.

### 8.1 Git Hook 권고

권장 최소 훅:
- `pre-commit`
  - `./gradlew.bat :app:automotive:testDebugUnitTest`
  - markdown lint 또는 broken link check
- `pre-push`
  - `./gradlew.bat :app:automotive:assembleDebug`

효과:
- 빌드가 깨진 커밋을 줄임
- 문서 프로젝트라도 README / docs 링크 손상을 빠르게 감지

### 8.2 GitHub Actions 권고

권장 최소 workflow:
- PR / push on main
  - JDK 17 setup
  - Gradle cache
  - `:app:automotive:testDebugUnitTest`
  - `:app:automotive:assembleDebug`

추가 후보:
- markdown link check
- static JSON schema validation
- tenant config smoke validation

### 8.3 지금 바로 필요한가

권고 수준:
- `pre-commit`: 높음
- `pre-push`: 높음
- GitHub Actions basic CI: 높음
- instrumentation emulator CI: 중간
- full AAOS emulator E2E CI: 낮음, 후순위

## 9. 향후 구현 우선순위

### 9.1 즉시 구현 가치가 큰 것

1. mixed sensor conflict policy code화
2. stale GPS / BLE timeout 정책 추가
3. diagnostics에 conflict / stale / fallback reason taxonomy 추가
4. validation matrix에 대응되는 unit tests 확장

### 9.2 포트폴리오 임팩트가 큰 것

1. `dashboard/web`에서 scenario -> adb inject 자동 브리지
2. README에 검증 매트릭스와 통과 증거 반영
3. 영상에서 정상/실패 플로우를 모두 보여주기

### 9.3 장기적으로 깊이를 만드는 것

1. lifecycle / process restart 검증
2. 100ms safety guard 계측
3. vendor property / custom manager / fake VHAL 확장

## 10. 검증 완료 정의

이 문서 기준 “한 단계 검증 완료”는 아래 4가지가 동시에 만족될 때로 본다.

- 코드 테스트가 존재한다
- 수동 또는 반자동 실행 절차가 존재한다
- 기대 상태가 문서화되어 있다
- 실패 시 어떤 degrade / fallback / abort가 일어나는지 설명 가능하다
