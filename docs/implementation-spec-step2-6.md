# DriveThru IVI Step 2-6 Implementation Spec

## 1. 문서 목적

이 문서는 `car/drivethru`의 Step 2~6 구현 범위를 고정하는 실행 명세입니다.

목적:
- 현재 단계에서 무엇을 구현할지 범위를 명확히 고정
- 각 Step의 요구사항을 코드 관점으로 분해
- 어떤 방식으로 검증할지 미리 정의
- "완료" 판단을 감각이 아니라 통과 기준으로 판정

이 문서는 [architecture.md](architecture.md)와 [project-plan.md](project-plan.md)를 구현 가능한 작업 단위로 압축한 문서입니다.

## 2. 현재 전제

현재 이미 확보된 상태:
- AAOS 앱 기본 골격 존재
- `CarAppService`, `Session`, 첫 화면 렌더링 가능
- 에뮬레이터에 설치 및 실행 가능한 최소 빌드 성공

이번 문서의 범위:
- Firebase 연동 전
- Geofence 실제 연동 전
- `CarPropertyManager` 실제 연동 전
- 가짜 데이터소스와 시뮬레이션 가능한 데모 플로우 완성까지

## 3. Step 요약

| Step | 이름 | 핵심 결과 |
| --- | --- | --- |
| Step 2 | 차량 상태 모델 및 데이터소스 추상화 | `GearState`, `GearStateDataSource`, `FakeGearStateDataSource` 도입 |
| Step 3 | Driver Distraction UI 분기 | `SimplifiedMenu`와 `FullMenu`가 기어 상태 기준으로 분리 |
| Step 4 | 정적 메뉴 및 주문 초안 | `FakeMenuRepository`, `OrderDraft`, 기본 주문 흐름 구현 |
| Step 5 | ADB/VHAL 실행 스크립트 정리 | 실행/설치/향후 VHAL 주입 스크립트 표준화 |
| Step 6 | 실행 문서와 데모 시나리오 정리 | README와 docs 기준으로 재현 가능한 개발/시연 루틴 고정 |

## 4. Step 2 명세

### 4.1 구현 목표

차량 기어 상태를 UI와 분리된 독립 상태로 모델링하고, 이후 실제 차량 API로 교체 가능한 구조를 만든다.

### 4.2 구현 대상

예상 파일:
- `core/model/GearState.kt`
- `data/vehicle/GearStateDataSource.kt`
- `data/vehicle/FakeGearStateDataSource.kt`
- `core/state/DriveThruStateStore.kt`

### 4.3 요구사항

- 기어 상태는 최소 아래 값을 표현할 수 있어야 한다.
  - `UNKNOWN`
  - `PARK`
  - `DRIVE`
  - `REVERSE`
  - `NEUTRAL`
- 앱 상태 저장소는 현재 기어 상태를 보유해야 한다.
- 화면 로직은 직접 정수값을 해석하지 않고 `GearState`만 사용해야 한다.
- 데이터소스는 인터페이스를 통해 주입 가능해야 한다.
- 가짜 데이터소스는 화면 또는 디버그 액션을 통해 상태 변경이 가능해야 한다.

### 4.4 비기능 요구사항

- 실제 `CarPropertyManager` 의존성은 아직 추가하지 않는다.
- 구현은 이후 실제 데이터소스로 교체 가능해야 한다.
- 상태 변경은 화면 전환 테스트에 충분히 빠르게 반영되어야 한다.

### 4.5 검증 방법

- 코드 검증:
  - UI 레이어에서 기어 정수값 하드코딩이 남아 있지 않은지 확인
  - 상태 저장소가 `GearState`를 직접 보유하는지 확인
- 실행 검증:
  - 앱 실행 후 디버그 액션으로 `PARK`, `DRIVE` 상태를 수동 변경
  - 상태 변경 시 다음 화면 분기가 가능해야 함

### 4.6 통과 기준

- `GearState` enum 또는 sealed model이 존재한다.
- `GearStateDataSource` 인터페이스가 존재한다.
- `FakeGearStateDataSource`를 통해 앱 상태를 수동 변경할 수 있다.
- 이후 Step 3 화면 분기에 필요한 상태 입력이 모두 준비된다.

## 5. Step 3 명세

### 5.1 구현 목표

Driver Distraction 대응의 핵심인 `PARK` 대 `비PARK` UI 분기를 실제 화면 구조로 고정한다.

### 5.2 구현 대상

예상 파일:
- `feature/menu/SimplifiedMenuScreen.kt`
- `feature/menu/FullMenuScreen.kt`
- `feature/status/WaitingForEntryScreen.kt`
- 필요 시 `app/DriveThruSession.kt`

### 5.3 요구사항

- `WaitingForEntry` 화면에서 데모 매장 진입이 가능해야 한다.
- `DRIVE`, `REVERSE`, `NEUTRAL`, `UNKNOWN` 상태에서는 간소화 메뉴만 보여야 한다.
- `PARK` 상태에서는 전체 메뉴 화면으로 진입 가능해야 한다.
- 간소화 메뉴는 1-tap 또는 즐겨찾기 성격의 최소 메뉴만 노출해야 한다.
- 전체 메뉴는 카테고리 또는 더 많은 메뉴 항목을 보여야 한다.
- 화면은 뒤로 가기 또는 상태 변경으로 서로 전환 가능해야 한다.

### 5.4 비기능 요구사항

- 화면 전환 로직은 상태 저장소를 기준으로 결정되어야 한다.
- 분기 조건은 화면 곳곳에 분산되지 않고 상태 또는 navigator 수준에서 읽히는 구조가 바람직하다.
- "왜 이 화면이 노출되는지"가 코드상 명확해야 한다.

### 5.5 검증 방법

- 실행 검증:
  - 앱 시작
  - 데모 매장 진입
  - 기본 기어 상태가 `DRIVE`이면 간소화 메뉴 노출 확인
  - 기어 상태를 `PARK`로 바꾸면 전체 메뉴 진입 확인
  - 다시 `DRIVE`로 바꾸면 간소화 메뉴 복귀 확인
- 코드 검증:
  - `PARK` 여부로 분기되는 조건이 한 군데 또는 제한된 구조 안에 모여 있는지 확인

### 5.6 통과 기준

- `SimplifiedMenuScreen`와 `FullMenuScreen`가 모두 존재한다.
- `PARK`와 비PARK 상태에 따라 보이는 화면이 달라진다.
- 이 분기가 에뮬레이터 데모에서 수동 재현 가능하다.

## 6. Step 4 명세

### 6.1 구현 목표

앱을 단순 화면 데모가 아니라 "주문 가능한 도메인 흐름"으로 발전시킨다.

### 6.2 구현 대상

예상 파일:
- `core/model/MenuItem.kt`
- `core/model/OrderDraft.kt`
- `data/menu/MenuRepository.kt`
- `data/menu/FakeMenuRepository.kt`
- `feature/order/OrderReviewScreen.kt`
- `core/state/DriveThruStateStore.kt`

### 6.3 요구사항

- 메뉴 데이터는 화면 내부 문자열이 아니라 저장소에서 공급되어야 한다.
- 메뉴 항목은 최소 아래 속성을 가진다.
  - `id`
  - `name`
  - `price`
  - `category`
  - `available`
- 간소화 메뉴는 저장소 데이터의 일부만 보여야 한다.
- 전체 메뉴는 저장소 데이터의 더 넓은 범위를 보여야 한다.
- 사용자는 메뉴 선택 후 주문 초안 상태로 이동할 수 있어야 한다.
- 주문 초안은 선택 항목과 수량 또는 최소한 선택 목록을 보유해야 한다.

### 6.4 비기능 요구사항

- `FakeMenuRepository`만으로도 Step 4 전체 흐름 검증이 가능해야 한다.
- `OrderDraft`는 이후 Firebase 주문 모델로 확장 가능해야 한다.
- 현재는 결제, POS, 실주문 전송은 구현하지 않는다.

### 6.5 검증 방법

- 코드 검증:
  - 메뉴 데이터가 `Repository`에서 공급되는지 확인
  - 주문 초안이 화면 로컬 변수 아닌 앱 상태로 유지되는지 확인
- 실행 검증:
  - 간소화 메뉴에서 1개 이상 선택
  - 전체 메뉴에서 1개 이상 선택
  - 주문 확인 화면 또는 요약 화면으로 이동
  - 선택한 항목이 화면에 반영되는지 확인

### 6.6 통과 기준

- `FakeMenuRepository`가 존재한다.
- `MenuItem`, `OrderDraft` 도메인 모델이 존재한다.
- 메뉴 선택 후 주문 초안 화면 또는 요약 상태로 진입 가능하다.
- 앱이 "차량 메뉴 탐색 -> 선택 -> 검토" 흐름을 갖는다.

## 7. Step 5 명세

### 7.1 구현 목표

실행, 설치, 향후 VHAL 시뮬레이션을 반복 가능한 명령 세트로 정리한다.

### 7.2 구현 대상

예상 파일:
- `tools/adb/run-automotive.ps1`
- `tools/adb/install-automotive.ps1`
- `tools/adb/inject-gear-park.ps1`
- `tools/adb/inject-gear-drive.ps1`
- 필요 시 `tools/adb/README.md`

### 7.3 요구사항

- 최소한 아래 동작은 스크립트로 재현 가능해야 한다.
  - APK 설치
  - 앱 실행
  - 향후 기어 주입용 플레이스홀더 또는 실제 명령 저장
- 스크립트는 루트 기준 어느 경로에서 실행하는지 문서화되어야 한다.
- 사람이 매번 긴 명령을 외우지 않아도 재실행 가능해야 한다.

### 7.4 비기능 요구사항

- 스크립트는 PowerShell 기준으로 동작해야 한다.
- 실 장비보다 에뮬레이터를 우선 타깃으로 한다.
- 명령 실패 시 어떤 전제가 필요한지 메시지로 드러나는 편이 좋다.

### 7.5 검증 방법

- 실행 검증:
  - 스크립트 실행 후 APK 설치 성공 확인
  - 실행 스크립트 후 앱이 포그라운드로 올라오는지 확인
- 문서 검증:
  - 새 세션에서 README만 보고 스크립트 경로를 찾을 수 있는지 확인

### 7.6 통과 기준

- 설치/실행 스크립트가 존재한다.
- 최소 한 번 실제 실행해 성공 여부를 검증한다.
- VHAL 기어 주입 스크립트는 실제 사용 가능하거나, 최소한 후속 연결 지점이 명확히 남아 있다.

## 8. Step 6 명세

### 8.1 구현 목표

현재 구현 범위와 실행 루틴을 README와 docs 기준으로 재현 가능하게 만든다.

### 8.2 구현 대상

예상 파일:
- `README.md`
- 필요 시 `docs/architecture.md`
- 필요 시 추가 실행 가이드 문서

### 8.3 요구사항

- README에서 아래가 한 번에 보여야 한다.
  - 프로젝트 목적
  - 현재 구현 범위
  - 실행 방법
  - 데모 시나리오
  - 다음 단계
- docs에는 Step 2~6 명세 문서가 유지되어야 한다.
- 개발자 관점에서 "오늘 어디까지 됐는지" 판단 가능해야 한다.

### 8.4 비기능 요구사항

- README는 포트폴리오 소개와 개발 실행 가이드 사이 균형을 유지해야 한다.
- 구현되지 않은 기능은 구현된 것처럼 쓰지 않는다.
- Step 7 이후와 현재 범위를 명확히 구분해야 한다.

### 8.5 검증 방법

- 문서 검증:
  - README만 읽고 프로젝트 목적과 현재 구현 범위를 이해할 수 있는지 확인
  - Step 2~6 문서를 보고 다음 구현 항목을 바로 선택할 수 있는지 확인
- 실행 검증:
  - README 명령만으로 빌드/설치/실행 재현 가능한지 확인

### 8.6 통과 기준

- README가 현재 코드 상태와 일치한다.
- 실행 루틴과 데모 시나리오가 문서화되어 있다.
- 다음 구현자가 Step 2~6 명세를 보고 바로 작업할 수 있다.

## 9. 전체 검증 루프

각 Step 완료 시 아래 순서로 검증한다.

1. 코드 구조 확인
2. `gradlew` 빌드 확인
3. 에뮬레이터 설치 확인
4. 수동 실행 또는 스크립트 실행 확인
5. 화면 전환 및 상태 변화 확인
6. README 또는 docs와 실제 구현 상태 일치 여부 확인

## 10. Step 2~6 완료 정의

Step 2~6이 모두 끝났다고 판단하려면 아래가 모두 참이어야 한다.

- 앱이 루트 프로젝트 기준으로 빌드된다.
- `WaitingForEntry`, `SimplifiedMenu`, `FullMenu`가 모두 존재한다.
- 기어 상태 변경으로 화면 분기가 가능하다.
- 메뉴 데이터가 저장소를 통해 공급된다.
- 주문 초안까지 이동하는 흐름이 존재한다.
- 설치/실행 스크립트가 준비되어 있다.
- README와 docs가 현재 구현 범위를 정확히 설명한다.

## 11. Step 7 이후 경계

아래부터는 Step 2~6 범위를 벗어난다.

- `CarPropertyManager` 실제 연동
- Geofence 실제 연동
- Firebase 실시간 DB 연동
- 직원 웹 대시보드 실구현
- 라즈베리파이 HAL 연동

즉 Step 2~6은 "로컬 데모 가능한 AAOS 주문 앱 골격"까지가 목표이고, Step 7부터는 "실제 차량 신호 및 외부 시스템 연결" 단계다.
