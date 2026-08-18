# DriveThru Logging And Error Handling Policy

이 문서는 `car/drivethru` 전체에 적용할 logging, diagnostics, error handling, user-facing degrade 정책의 기준 문서다.

## 1. 목적

이 프로젝트의 로그는 단순 디버깅용이 아니라 아래 목적을 가진다.

1. 영상 / README용 검증 증거
2. failure scenario 재현 근거
3. sensor conflict / fallback / abort 정책의 설명 수단
4. 향후 custom service / VHAL 확장 시 observability 기반

## 2. 기본 원칙

### 2.1 로그는 구조화한다

가급적 아래 형태를 유지한다.

- `component`
- `event`
- `source`
- `status`
- `storeId`
- `triggerSource`
- `sessionId`
- `reason`
- `timestamp`

예시:

```text
component=EntryTriggerProvider event=store_detected source=GPS_GEOFENCE status=OK storeId=mcdonalds_gangnam_001
component=VehicleSignalProvider event=fallback_engaged source=CAR_PROPERTY_MANAGER status=FALLBACK reason=IllegalStateException
component=SafetyPolicy event=session_aborted source=VEHICLE_SPEED status=ABORT reason=SPEED_THRESHOLD
```

### 2.2 로그 레벨을 명확히 나눈다

- `DEBUG`
  - raw signal, state transition trace, repeated scan
- `INFO`
  - store activation, session start, session close, UI mode switch
- `WARN`
  - stale GPS, unknown beacon, permission denied, fallback engaged
- `ERROR`
  - unrecoverable exception, corrupted tenant config, impossible state

### 2.3 민감 정보는 남기지 않는다

남기지 말아야 할 것:
- 사용자 결제 정보
- 실사용자 식별자
- 불필요하게 정밀한 개인 위치 이력

남겨도 되는 것:
- 테스트 store id
- geofence stage
- beacon logical id
- speed / gear / parking state

## 3. Component Logging Policy

### 3.1 Entry Trigger

반드시 남길 이벤트:
- provider start / stop
- GPS update received
- beacon detected
- store resolved
- sensor conflict
- stale signal dropped
- duplicate signal suppressed

### 3.2 Vehicle Signal

반드시 남길 이벤트:
- snapshot read success
- fallback engaged
- property unavailable
- speed threshold abort
- gear transition causing STOP_STATE

### 3.3 Session / UI

반드시 남길 이벤트:
- session start
- session resumed
- STOP_STATE entered
- session aborted
- session closed
- payment mock completed

## 4. Error Handling Policy

### 4.1 Recoverable

예:
- GPS update missing
- beacon mismatch
- HAL read exception
- permission denied

정책:
- crash 금지
- degrade / fallback 사용
- diagnostics와 status message에 이유 노출

### 4.2 Non-Recoverable

예:
- tenant config schema 자체가 깨짐
- required menu data가 전혀 없음
- impossible state transition

정책:
- 안전한 기본 상태로 강등
- session close 또는 standby 복귀
- `ERROR` 로그 남김

## 5. User-Facing Degrade Policy

### 5.1 Fallback

사용자 메시지 예시:
- `Vehicle data unavailable. Using simulator fallback.`
- `Location signal delayed. Waiting for stable entry confirmation.`

### 5.2 Degrade

사용자 메시지 예시:
- `Entry signal is unstable. Limited mode is active.`
- `Vehicle is not in a safe state to continue ordering.`

### 5.3 Abort

사용자 메시지 예시:
- `Vehicle exceeded the safety speed threshold. Session closed.`
- `Vehicle left the store zone. Returning to standby.`

## 6. Diagnostics Panel Policy

diagnostics에는 최소 아래가 보여야 한다.

- signal type
- raw value
- source
- status
- timestamp
- detail

추천 추가 항목:
- `conflict=true/false`
- `fallbackActive=true/false`
- `lastStableStoreId`
- `lastAcceptedTriggerSource`

## 7. Exception Handling Guidelines

### 7.1 금지

- provider 내부 exception을 상위 UI까지 그대로 전파
- 로그 없이 삼키기
- fallback 가능한 상황에서 앱 종료

### 7.2 권장

- `runCatching { ... }`
- fallback object 사용
- detail reason 문자열 기록
- source/status를 diagnostics에 반영

## 8. Traceability Recommendation

향후에는 각 주요 에러/로그가 아래와 매핑되도록 정리하는 것을 권장한다.

- requirement id
- failure scenario id
- test case id
- log event name

예시:

| Requirement | Failure ID | Test ID | Log Event |
| --- | --- | --- | --- |
| Safe full menu exposure | F-09 | T-SAFE-DRIVE-001 | `stop_state_entered` |
| HAL fallback continuity | F-07 | T-HAL-FALLBACK-001 | `fallback_engaged` |
| Mixed sensor lock | F-12 | T-CONFLICT-001 | `sensor_conflict_detected` |

## 9. Hook / Workflow Recommendation For Quality Gates

문서 정책만 두지 말고 아래 자동화와 연결하는 것을 권장한다.

- `pre-commit`
  - unit test
  - tenant JSON lint
- `pre-push`
  - assembleDebug
- GitHub Actions
  - unit test
  - assemble
  - markdown link validation

이유:
- logging / error policy는 “문서만 있는 상태”보다 “깨지면 커밋이 막히는 상태”가 훨씬 강하다

## 10. 향후 보강 포인트

- structured logger wrapper 도입
- event taxonomy enum 도입
- session id / correlation id 도입
- README와 영상에서 실제 log sample 공개
- chaos run 요약 리포트 자동 생성
