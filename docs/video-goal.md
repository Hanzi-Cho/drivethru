# DriveThru IVI Video Goal

## 1. 문서 목적

이 문서는 `car/drivethru`의 최종 포트폴리오 영상을 어떤 구조로 만들지 고정하는 목표 문서입니다.

핵심 목표:
- 면접관이 한 번에 이해할 수 있는 시연 구성을 만든다.
- 왼쪽에는 차량 외부 상황 시뮬레이터, 오른쪽에는 AAOS 에뮬레이터 화면을 동시에 보여준다.
- "차량 접근 감지 -> 주문 앱 진입 -> 차량 UI 주문 -> 결제 완료 UI -> 매장 이탈 후 종료" 흐름을 1개의 짧은 영상으로 설명한다.
- 실제 AAOS 권한 제약 때문에 불가능한 부분은 대체 연출안으로 바꾼다.

---

## 2. 최종 영상 한 줄 목표

> 왼쪽 시뮬레이터 또는 GPS 기반 진입 감지로 차량이 특정 드라이브스루 매장 근처에 접근하면, 시스템이 어떤 가게 주문 기능을 사용할지 해석하고, 안전 상태에서는 Custom UI 주문 앱이 자동 실행되며, 주문 중 주행이 시작되면 `STOP_STATE`로 세션을 보존하고, 매장을 벗어나면 세션이 종료되는 데모 영상을 만든다.

---

## 3. 화면 구성 목표

영상은 좌우 2분할 구성을 기본으로 한다.

### 왼쪽 패널: DriveThru Zone Simulator

포함 요소:
- 상공에서 본 단순 지도
- 드라이브스루 건물
- 건물을 감싸는 도로
- 도로 위 차량 1대
- `Move Forward` 원형 버튼
- 필요 시 `Move Back`, `Reset`, `Set PARK`, `Set DRIVE` 버튼
- 건물 입구 쪽의 `NFC Tag` 표식
- NFC 인식 범위를 원형 오버레이로 표시
- 차량과 NFC 원형 범위가 겹치면 이벤트 발생

시각적 의도:
- 너무 사실적인 지도보다 "이벤트 관계가 즉시 이해되는 단순 도식"이 더 좋다.
- 네모 건물, 선형 도로, 원형 인식 범위, 아이콘 차량 수준으로 단순화한다.

### 오른쪽 패널: AAOS Emulator

포함 요소:
- 기본 대기 화면 또는 네비게이션 유사 화면
- 드라이브스루 진입 후 주문 앱 화면
- `WaitingForEntry`
- `SimplifiedMenu` 또는 `FullMenu`
- `OrderReview`
- 결제 완료 UI
- 이탈 후 세션 종료 화면 또는 홈 복귀 연출

---

## 4. 핵심 시나리오

### 시나리오 A: 가장 현실적인 포트폴리오 버전

1. 오른쪽 AAOS는 대기 상태를 보여준다.
2. 왼쪽 시뮬레이터에서 차량이 도로를 따라 전진한다.
3. 차량이 건물 입구 근처 NFC 범위 또는 geofence 범위와 겹친다.
4. 시스템이 `storeId`와 `store capability`를 해석한다.
5. 주문 기능 번들 또는 메뉴 데이터를 preload 한다.
6. 차량 속도와 기어 상태를 확인한다.
7. 안전 상태가 확보되면 AAOS `Custom UI` 주문 앱이 열린다.
8. 사용자가 메뉴를 선택하고 주문 검토 화면으로 간다.
9. 결제 화면에서 지정 카드로 결제 UI를 보여준다.
10. 주문 중 차량이 다시 움직이면 `STOP_STATE`로 전환한다.
11. 이때 앱은 세션을 잃지 않고 백그라운드 또는 제한 상태로 내려간다.
12. 다시 안전 상태가 되면 세션을 resume 하거나, 매장 이탈 시 세션 종료 UI를 보여준다.

### 시나리오 B: 이상적인 연출 버전

1. AAOS에 네비게이션 또는 홈 유사 화면이 떠 있다.
2. 차량이 NFC 범위에 들어간다.
3. 기존 화면이 자동으로 내려가고 드라이브스루 앱이 전면에 뜬다.
4. 주문 및 결제가 완료된다.
5. 차량이 매장을 벗어나면 앱이 자동으로 사라진다.

이 버전은 연출로는 좋지만, 일반 앱 권한으로는 제약이 크다.

---

## 5. 구현 가능 여부 판단

## 5.1 가능한 것

- 왼쪽에 웹 또는 데스크톱 기반 `DriveThru Zone Simulator` 만들기
- 차량과 NFC 인식 범위의 충돌 판정 구현
- 충돌 시 오른쪽 앱으로 이벤트 보내기
- AAOS 앱 내부에서 주문 플로우 전환
- `DRIVE` / `PARK` / `STOP_STATE`에 따라 UI 분기
- 결제 완료 UI만 구현
- 매장 이탈 시 세션 종료 처리
- GPS/매장 proximity/차량 상태를 추상화한 아키텍처 설계
- `Car App Library` 프로토타입과 `Custom UI` 목표 구현을 분리한 구조 설계

## 5.2 제한이 있는 것

### 실제 `CarPropertyManager` 값을 일반 앱이 직접 밀어넣는 것

일반 앱은 보통 실제 차량 속성 레이어를 직접 쓸 수 없다.

현재 프로젝트 환경에서도 이미 확인된 제약:
- Google Play Automotive 에뮬레이터의 `user build`에서는 `adb shell cmd car_service inject-vhal-event ...`가 막힐 수 있다.

따라서 영상용 구현 기본값은 아래가 더 현실적이다.

- `CarPropertyManager` 직접 주입 대신
- 앱 내부 `VehicleSignalProvider` 추상화에
- 시뮬레이터 이벤트를 연결한다

즉, 면접에서는 이렇게 설명하면 된다.

> 실제 양산 차량 속성 주입 권한은 없어서, 앱 내부 신호 추상화 계층을 만들고 시뮬레이터가 동일 의미의 차량 이벤트를 공급하도록 설계했다.

### 다른 앱이나 네비게이션 화면을 임의로 내리는 것

일반 Android와 AAOS 모두에서, 서드파티 앱이 다른 앱을 마음대로 종료하거나 전면에서 끌어내리는 것은 기본적으로 기대하면 안 된다.

공식 제약 근거:
- Car App Library는 호스트 안에서 제한된 템플릿 흐름으로 동작한다.
- Android는 백그라운드에서 Activity를 마음대로 시작하는 것을 제한한다.

따라서 권장 연출은 아래 둘 중 하나다.

1. 같은 앱 내부에서 `Navigation-like standby screen -> DriveThru screen`으로 전환
2. 사용자 액션 또는 명시적 테스트 버튼으로 주문 앱 진입을 연출

포트폴리오 기준 추천안:
- "기존 네비가 자동으로 내려간다"를 실제 OS 제어처럼 만들기보다
- "차량 진입 이벤트를 수신한 후 드라이브스루 태스크로 전환된다"는 내 앱 기준 시나리오로 표현

### 실제 결제 수행

실결제는 현재 프로젝트 범위를 넘는다.

따라서 영상에서는 아래까지만 구현한다.
- 저장된 카드 UI
- `Pay with default card`
- 결제 진행 애니메이션
- `Payment Complete`

즉, 결제는 "실결제"가 아니라 "결제 UX mock"으로 명시한다.

---

## 6. 권장 기술 구조

## 6.1 왼쪽 시뮬레이터

권장 위치:
- `dashboard/web`

권장 역할:
- 단순 2D 탑다운 시뮬레이터
- 차량 좌표 관리
- NFC 인식 반경 오버랩 판정
- `Enter store zone`, `Exit store zone`, `Set PARK`, `Set DRIVE` 이벤트 송신

권장 UI 요소:
- 직사각형 건물
- 도로 경로
- 원형 NFC zone
- 차량 아이콘
- 이동 버튼
- 상태 패널
  - `carX`
  - `insideNfcZone`
  - `gearState`
  - `currentStoreId`

## 6.2 오른쪽 AAOS 앱

권장 역할:
- 시뮬레이터 이벤트를 받아 앱 상태 전이
- 주문 화면 렌더링
- 안전 상태에 따른 메뉴 제한
- 결제 완료 UI
- 이탈 시 세션 종료
- 주문 중 주행 시 `STOP_STATE` 세션 보호

필요한 상태:
- `Idle`
- `ApproachingStore`
- `StoreResolved`
- `ReadyToLaunchCustomUi`
- `CustomUiOrdering`
- `ReviewingOrder`
- `PaymentPending`
- `PaymentComplete`
- `StopState`
- `SessionClosed`

## 6.3 이벤트 브리지

가장 현실적인 연결 방식:

1. 왼쪽 웹 시뮬레이터
2. 로컬 브리지
3. AAOS 앱 내부 Fake/Debug provider

후보:
- 로컬 HTTP 서버
- WebSocket
- `adb shell am broadcast`
- deeplink
- 파일 폴링은 비추천

현재 포트폴리오용 추천:
- `adb shell am broadcast` 또는 간단한 로컬 HTTP 브리지

이유:
- 구현이 빠르다
- 영상 시연 시 이벤트 흐름이 명확하다
- 나중에 `VehicleSignalProvider` 뒤에 끼우기 쉽다

---

## 7. 권장 이벤트 모델

시뮬레이터는 실제 VHAL 값을 직접 건드리기보다, 앱이 이해할 수 있는 도메인 이벤트를 보낸다.

예시:

```text
DriveThruEntryDetected(storeId=store_001, source=NFC_SIMULATOR)
DriveThruExitDetected(storeId=store_001)
GearStateChanged(PARK)
GearStateChanged(DRIVE)
PaymentRequested(method=DEFAULT_CARD_UI_ONLY)
PaymentCompleted(orderId=demo_order_001)
```

이 이벤트들을 앱 내부에서 아래로 변환한다.

```text
Simulator Event
  -> DebugSignalGateway
  -> VehicleSignalProvider / EntryTriggerDataSource
  -> DriveThruStateStore
  -> Screen transition
```

---

## 8. NFC 처리 해석

이 프로젝트에서 NFC는 실제 리더 하드웨어 시연이 아니라 "매장 진입 감지 토큰" 역할로 해석한다.

즉:
- 차량이 NFC 원형 범위와 겹침
- 시뮬레이터가 `store detected` 이벤트 발생
- 앱이 이를 매장 접근 이벤트로 해석

이 접근의 장점:
- 영상에서 매우 직관적이다
- Geofence보다 좁은 범위 연출이 가능하다
- "주문 가능한 정확한 위치에 들어왔다"는 메시지가 강하다

주의:
- 실제 양산 차량 NFC 연동으로 오해되지 않게
- 문서와 영상 설명에서 `NFC-style proximity simulation` 또는 `entry trigger simulation`이라고 표현하는 편이 안전하다

---

## 9. 네비게이션 화면 처리 권장안

권장안은 "실제 다른 앱 제어"가 아니라 "같은 데모 환경 안의 대기 화면 전환"이다.

### 추천 연출

- 대기 화면 이름을 `Navigation Standby` 또는 `Driving Home`
- 차량 진입 전에는 그 화면을 보여준다
- 진입 이벤트 수신 시 `DriveThru order available` 같은 전환 UI를 1초 정도 보여준다
- 이후 주문 화면으로 전환한다

이렇게 하면:
- 권한 문제를 피할 수 있다
- OS 해킹처럼 보이지 않는다
- 오히려 상태 전이 설계가 더 잘 보인다

---

## 10. 결제 UI 범위

이번 버전의 결제는 UI까지만 구현한다.

포함:
- 저장 카드 썸네일
- 카드 별칭
- 총 결제 금액
- `Confirm payment`
- `Processing`
- `Payment complete`

제외:
- 실카드 토큰화
- 외부 PG 호출
- NFC 결제
- 영수증 서버 연동

면접 설명 문장:

> 본 데모의 결제는 차량 내 결제 UX 흐름을 보여주기 위한 mock UI이며, 실제 결제 네트워크 및 OEM wallet 연동은 범위에서 제외했다.

---

## 11. 이탈 후 종료 처리

차량이 건물 영역을 벗어나면 아래 중 하나로 처리한다.

### 추천 기본안

- `SessionClosed` 화면 표시
- 1~2초 후 `WaitingForEntry` 또는 대기 화면으로 복귀

### 대체안

- `DriveThru session ended`
- 주문 완료 카드 유지
- 사용자가 `Done` 눌러 종료

영상용으로는 자동 복귀가 더 깔끔하다.

---

## 12. 실제 구현 우선순위

### Phase V1

- `docs/video-goal.md` 확정
- 왼쪽 시뮬레이터 와이어프레임 구현
- 앱에 `EntryDetected`, `ExitDetected`, `GearStateChanged` 디버그 입력 경로 추가

### Phase V2

- 좌우 화면 동시 시연 가능하게 브리지 연결
- `WaitingForEntry -> SimplifiedMenu / FullMenu` 전환
- 주문 검토 화면 연결

### Phase V3

- 결제 완료 UI 추가
- 이탈 시 세션 종료 처리
- 영상 녹화용 애니메이션, 상태 라벨, 디버그 로그 정리

### Phase V4

- 가능하면 `userdebug` 환경 또는 다른 에뮬레이터에서 VHAL 주입 재검토
- 되면 `Simulator Event -> actual VHAL injection` 실험 추가
- 안 되면 현재 구조를 최종안으로 고정

---

## 13. 최종 권장 결론

가장 좋은 포트폴리오 방향은 아래다.

- 왼쪽은 "실차 외부 환경을 이해시키는 시뮬레이터"
- 오른쪽은 "AAOS 앱 상태 전이와 주문 UX"
- 둘 사이 연결은 "실제 CarProperty 직접 제어"가 아니라 "동일 의미의 차량 이벤트 브리지"

즉, 영상은 아래 메시지를 전달해야 한다.

> 차량이 드라이브스루 주문 가능 구역에 진입하면, 차량용 앱이 안전 정책을 반영해 적절한 주문 UI를 띄우고, 주문과 결제 UX를 마친 뒤 구역 이탈 시 세션을 자동 종료하도록 설계했다.

이 메시지가 현대 계열 Android Framework / IVI 면접에서 가장 설득력이 높다.

---

## 14. 참고 제약

관련 공식 문서:
- [Android for Cars overview](https://developer.android.com/training/cars)
- [Use the Android for Cars App Library](https://developer.android.com/training/cars/apps/library)
- [Template restrictions](https://developer.android.com/training/cars/apps/library/template-restrictions)
- [Activity start restrictions](https://developer.android.com/guide/components/activities/secure-bal)

정리:
- Car App Library는 호스트 제약 안에서 동작한다.
- 일반 앱은 다른 앱을 마음대로 내리거나 차량 속성을 자유롭게 조작할 수 있다고 가정하면 안 된다.
- 따라서 포트폴리오 시연은 "실제 플랫폼 권한의 모사"가 아니라 "플랫폼 제약을 이해한 상태 전이 설계"를 보여주는 방향이 더 강하다.
