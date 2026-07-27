# Vehicle UI Development Guide

## 1. 문서 목적

이 문서는 AAOS 차량 애플리케이션 UI를 일반 Android 앱과 어떻게 다르게 설계해야 하는지 설명하고, `car/drivethru` 프로젝트 기준으로 어떤 파일 구조, 상태 구조, 데이터 전이 구조를 가져가야 하는지 정리한 가이드입니다.

대상:
- Android Automotive OS 포트폴리오를 만들고 싶은 개발자
- Car App Library 기반 화면 구조를 처음 설계하는 개발자
- `DriveThru IVI`에서 UI, 상태, 차량 신호, 데이터소스 계층을 정리하고 싶은 현재 작업자

---

## 2. 차량 앱 UI는 일반 앱과 무엇이 다른가

## 2.1 가장 큰 차이: "자유 레이아웃"보다 "제약된 템플릿"

일반 Android 앱은 보통 아래처럼 작업합니다.

- XML Layout 또는 Compose로 화면을 자유롭게 그림
- 색상, 간격, 컴포넌트 배치, 애니메이션을 앱이 직접 제어
- 화면 전환도 Activity, Fragment, NavHost 등으로 앱이 주도

반면 Car App Library 기반 차량 앱은 다릅니다.

- 앱이 픽셀 단위 레이아웃을 완전히 직접 그리지 않음
- `Screen`이 `Template`을 반환하는 구조를 사용
- 실제 렌더링은 차량의 `Host`가 담당
- 텍스트 길이, 리스트 수, 액션 수, 화면 흐름에 제약이 있음
- Driver Distraction을 줄이기 위한 정책이 구조 자체에 녹아 있음

공식 문서 기준 핵심 포인트:
- Car App Library는 운전 중 주의 분산을 줄이기 위한 템플릿 세트를 제공한다. [Use the Android for Cars App Library](https://developer.android.com/training/cars/apps/library)
- 태스크 당 템플릿 수 제한이 있고, 마지막 템플릿 타입도 제약된다. [Template restrictions](https://developer.android.com/training/cars/apps/library/template-restrictions)
- 리스트 항목 수와 텍스트 길이도 차량/상태에 따라 제약된다. [List template](https://developer.android.com/design/ui/cars/guides/templates/list-template)

즉 차량 앱 UI 개발은 "화면을 예쁘게 직접 그리는 일"보다 아래에 가깝습니다.

- 상태를 안전하게 정의
- 각 상태에 맞는 템플릿 선택
- 주행/정차/접근 상태에 따라 허용되는 정보량을 다르게 구성
- Host가 안전하게 보여줄 수 있도록 콘텐츠 밀도를 조절

---

## 2.2 두 번째 차이: UI는 상태 머신의 출력물이다

일반 앱에서는 화면이 UI 로직의 중심이 되기 쉽습니다.

예:
- 버튼 클릭 -> 바로 Dialog
- RecyclerView 클릭 -> 바로 다음 Fragment

차량 앱에서는 이 방식이 점점 불리해집니다.

이유:
- 차량 상태가 항상 UI보다 우선
- 주행 중인지, 정차 중인지, 매장 접근 중인지가 더 중요
- 같은 화면이라도 차량 상태에 따라 다른 버전이 필요
- 임의의 시점에 안전 차단이 필요할 수 있음

그래서 차량 앱은 아래 방식이 좋습니다.

```text
Vehicle signal / Entry trigger / User input
  -> State store / State machine
  -> Screen chooses template
  -> Host renders template
```

즉 UI는 상태의 결과물이어야 합니다.

---

## 2.3 세 번째 차이: 색과 브랜딩도 "보조 요소"다

일반 모바일 앱은 브랜드 색, 배경, 카드, 그림자, 모션이 강하게 중요합니다.

차량 앱은 다릅니다.

- 정보 구조가 더 중요
- 큰 터치 타깃과 짧은 문구가 중요
- 주행 중 읽히는 순서가 중요
- 색은 강조용이지, 복잡한 브랜딩 캔버스가 아님

그래서 차량 앱에서 색을 쓴다면:
- 상태 강조
- 위험/안전 의미 전달
- 결제 완료/경고/진입 감지 같은 피드백 강화

위주로 써야 합니다.

---

## 3. 현재 프로젝트에서 UI를 어떻게 해석해야 하는가

`DriveThru IVI`는 지금 다음 구조로 이해하면 됩니다.

### UI 목적

- 차량이 주문 가능 구역에 들어왔는지 보여준다
- 주행 상태에 따라 메뉴 UI 범위를 다르게 보여준다
- 운전 중엔 간소화 메뉴만 허용한다
- 정차 상태에만 전체 메뉴를 보여준다
- 주문 검토와 결제 완료 흐름을 짧게 보여준다

즉 이 프로젝트의 UI는 "상점 앱 UI"가 아니라 아래에 더 가깝습니다.

> 차량 상태와 접근 이벤트에 의해 제어되는 안전 제한형 주문 태스크 UI

---

## 4. 어떤 방식으로 UI를 구현해야 하나

## 4.1 권장 방식

현재 프로젝트에서는 Car App Library의 `Screen + Template` 패턴을 중심으로 유지하는 것이 맞습니다.

핵심 원칙:
- 화면 1개당 템플릿 타입 1개를 기본값으로 둔다
- 템플릿 내부 데이터는 상태 저장소에서 읽는다
- 버튼 클릭 시 화면에서 직접 데이터 판단을 많이 하지 않는다
- 화면은 상태 전이를 요청하고, 실제 판단은 store/state machine이 담당한다

권장 흐름:

```text
Screen
  -> calls stateStore intent-like function
  -> stateStore updates domain state
  -> screen invalidates or pushes next screen
  -> onGetTemplate() re-renders from latest state
```

이 방식은 Car App Library의 refresh 모델과도 잘 맞습니다. 템플릿 refresh는 상태를 바꿨다고 자동 반영되지 않고 `invalidate()`가 필요할 수 있습니다. [Refresh the contents of a template](https://developer.android.com/training/cars/apps/library/refresh-template)

---

## 4.2 좋지 않은 방식

아래 패턴은 피하는 편이 좋습니다.

- `Screen` 안에서 `if` 분기와 데이터 조합을 과도하게 처리
- `Screen`이 `CarPropertyManager`를 직접 읽음
- 메뉴 문자열을 화면 클래스 안에 하드코딩
- `DRIVE/PARK` 숫자를 화면에서 직접 비교
- 주문 상태를 각 화면이 따로 들고 있음
- 위험 상태 차단 로직이 여러 화면에 흩어짐

이렇게 되면 차량 앱 특유의 "안전 상태 우선" 구조가 무너집니다.

---

## 5. 파일 계층 구조는 어떻게 짜야 하나

현재 프로젝트 기준 추천 구조는 아래입니다.

```text
app/automotive/app/src/main/java/com/hanzi/drivethru/
  app/
    DriveThruCarAppService.kt
    DriveThruSession.kt
  core/
    model/
      DriveThruState.kt
      GearState.kt
      VehicleSignal.kt
      VehicleSignalSnapshot.kt
      MenuItem.kt
      OrderDraft.kt
      PaymentState.kt
    state/
      DriveThruStateStore.kt
      SafetyCriticalStateMachine.kt
      PaymentStateMachine.kt
    navigation/
      DriveThruNavigator.kt
  data/
    vehicle/
      VehicleSignalProvider.kt
      FakeVehicleSignalProvider.kt
      CarPropertyVehicleSignalProvider.kt
      GearStateDataSource.kt
      FakeGearStateDataSource.kt
    entry/
      EntryTriggerDataSource.kt
      FakeEntryTriggerDataSource.kt
      NfcZoneEventGateway.kt
    menu/
      MenuRepository.kt
      FakeMenuRepository.kt
    payment/
      PaymentRepository.kt
      FakePaymentRepository.kt
  feature/
    standby/
      StandbyScreen.kt
    status/
      WaitingForEntryScreen.kt
      StoreDetectedScreen.kt
      SessionClosedScreen.kt
    menu/
      SimplifiedMenuScreen.kt
      FullMenuScreen.kt
    order/
      OrderReviewScreen.kt
    payment/
      PaymentMethodScreen.kt
      PaymentProcessingScreen.kt
      PaymentCompleteScreen.kt
  di/
    AppContainer.kt
```

역할은 아래처럼 나눕니다.

### `app/`

- Car App 진입점
- `Service`, `Session` 보관
- 앱 시작 시 의존성 조립

### `core/model/`

- UI에 종속되지 않는 순수 도메인 모델
- 기어 상태, 주문 초안, 세션 상태, 결제 상태

### `core/state/`

- 상태 전이 규칙
- 안전 차단 규칙
- 결제 처리 규칙
- 앱이 현재 어느 화면 흐름에 있는지 판단

### `data/`

- 실제 입력원 또는 외부 시스템 연결
- 차량 신호
- 매장 진입 이벤트
- 메뉴 공급
- 결제 mock 처리

### `feature/`

- 실제 `Screen` 클래스
- 한 상태를 보여주는 Template 구성 코드

---

## 6. 데이터 전이 구조는 어떻게 짜야 하나

차량 앱에서는 "누가 어떤 데이터를 언제 바꾸는지"가 명확해야 합니다.

권장 구조:

```text
Simulator / Vehicle API / User action
  -> DataSource or Gateway
  -> StateStore / StateMachine
  -> DriveThruState
  -> Screen.onGetTemplate()
  -> Host render
```

`DriveThru IVI` 기준으로 풀면:

```text
NFC zone overlap
  -> EntryTriggerDataSource emits store_001
  -> DriveThruStateStore.enterStore(store_001)
  -> currentState = StoreDetected or SimplifiedMenu
  -> WaitingForEntryScreen pushes next screen

Gear changed to PARK
  -> VehicleSignalProvider emits snapshot
  -> SafetyCriticalStateMachine evaluates
  -> currentState = FullMenu
  -> current screen invalidates or pushes FullMenuScreen

Gear changed to DRIVE while full menu open
  -> VehicleSignalProvider emits snapshot
  -> SafetyCriticalStateMachine evaluates risk
  -> currentState = SimplifiedMenu or SafetyRestricted
  -> UI downgraded immediately

User selects menu item
  -> stateStore.selectMenuItem(itemId)
  -> currentState = ReviewingOrder(orderDraft)
  -> push OrderReviewScreen
```

중요한 점:
- `Screen`은 상태를 표시하는 역할
- `DataSource`는 입력을 전달하는 역할
- `StateStore`는 진짜 판단을 하는 역할

---

## 7. UI 구조를 어떤 상태 단위로 끊어야 하나

추천 상태 모델:

```kotlin
sealed interface DriveThruState {
    data object Standby : DriveThruState
    data object WaitingForEntry : DriveThruState
    data class StoreDetected(
        val storeId: String,
        val storeName: String,
    ) : DriveThruState
    data class SimplifiedMenu(
        val storeId: String,
        val storeName: String,
        val gearState: GearState,
    ) : DriveThruState
    data class FullMenu(
        val storeId: String,
        val storeName: String,
        val gearState: GearState,
    ) : DriveThruState
    data class ReviewingOrder(
        val orderDraft: OrderDraft,
    ) : DriveThruState
    data class Paying(
        val orderDraft: OrderDraft,
        val paymentState: PaymentState,
    ) : DriveThruState
    data class PaymentComplete(
        val orderId: String,
    ) : DriveThruState
    data object SessionClosed : DriveThruState
}
```

이렇게 두면 좋은 이유:
- 화면별 책임이 명확함
- 주행 상태에 따른 분기 지점이 드러남
- 결제 mock을 추가해도 구조가 안 흔들림
- 영상 시연의 흐름이 상태 이름 자체로 설명 가능

---

## 8. 실제 화면 코드는 어떤 식으로 짜야 하나

## 8.1 기본 Screen 예시

아래는 접근 전 대기 화면 예시입니다.

```kotlin
class WaitingForEntryScreen(
    carContext: CarContext,
    private val stateStore: DriveThruStateStore,
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        return MessageTemplate.Builder(
            "Drive-thru zone nearby. Waiting for store entry trigger."
        )
            .setTitle("DriveThru IVI")
            .addAction(
                Action.Builder()
                    .setTitle("Enter Demo Store")
                    .setOnClickListener {
                        stateStore.enterStore(
                            storeId = "store_001",
                            storeName = "Demo Burger"
                        )
                        screenManager.push(
                            stateStore.createNextScreen(carContext)
                        )
                    }
                    .build()
            )
            .setHeaderAction(Action.APP_ICON)
            .build()
    }
}
```

포인트:
- 화면은 직접 조건을 많이 들고 있지 않음
- 버튼은 "의도"만 전달
- 다음 상태는 stateStore가 결정

---

## 8.2 PARK / DRIVE 분기 예시

```kotlin
fun createMenuState(
    storeId: String,
    storeName: String,
    gearState: GearState,
): DriveThruState {
    return if (gearState == GearState.PARK) {
        DriveThruState.FullMenu(
            storeId = storeId,
            storeName = storeName,
            gearState = gearState,
        )
    } else {
        DriveThruState.SimplifiedMenu(
            storeId = storeId,
            storeName = storeName,
            gearState = gearState,
        )
    }
}
```

이 판단은 `Screen` 안이 아니라 `StateStore` 또는 `SafetyCriticalStateMachine`에 있어야 합니다.

---

## 8.3 ListTemplate 예시

```kotlin
class SimplifiedMenuScreen(
    carContext: CarContext,
    private val stateStore: DriveThruStateStore,
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val state = stateStore.currentState as DriveThruState.SimplifiedMenu

        val items = ItemList.Builder().apply {
            stateStore.getQuickOrderMenu(state.storeId).forEach { item ->
                addItem(
                    Row.Builder()
                        .setTitle(item.name)
                        .addText("${item.price} KRW")
                        .setOnClickListener {
                            stateStore.selectMenuItem(state.storeId, item.id)
                            screenManager.push(OrderReviewScreen(carContext, stateStore))
                        }
                        .build()
                )
            }
        }.build()

        return ListTemplate.Builder()
            .setTitle("${state.storeName} Quick Order")
            .setHeaderAction(Action.BACK)
            .addSectionedList(
                SectionedItemList.create(items, "Quick picks")
            )
            .build()
    }
}
```

포인트:
- 템플릿은 짧고 명확한 정보만 노출
- 운전 중에는 항목 수와 텍스트 길이를 줄임
- 긴 설명, 복잡한 옵션, 화려한 카드 UI는 피함

---

## 9. 색은 어디서 입히나

## 9.1 Car App Library의 색 제약 이해

Car App Library는 일반 View 시스템처럼 마음대로 배경을 칠하는 구조가 아닙니다.

즉:
- `ConstraintLayout` 배경색처럼 전면 도색하는 방식은 기대하기 어렵다
- Host가 렌더링을 담당하므로 전체 레이아웃 색 커스터마이징 자유도가 낮다
- 색은 아이콘, 텍스트 의미, 일부 강조 요소 수준에서 접근하는 편이 현실적이다

따라서 차량 앱 색 전략은 "브랜딩"보다 "상태 의미"가 중심이어야 합니다.

예:
- 안전 상태: 중립 톤
- 제한 상태: amber/red 계열 의미
- 결제 완료: green 계열 의미

---

## 9.2 이 프로젝트에서 색을 적용하는 현실적인 방법

### 방법 A: 문자열과 아이콘 의미로 표현

- `Driving mode: limited menu only`
- `Parked: full menu available`
- `Payment complete`

처럼 문구와 구조로 상태를 분명히 보여주는 것이 우선입니다.

### 방법 B: 아이콘 tint, 브랜드 아이콘, 앱 아이콘

- 런처 아이콘
- 리스트 아이템 아이콘
- 상태 아이콘

위주로 색 의미를 넣습니다.

### 방법 C: 왼쪽 외부 시뮬레이터에서 더 강한 색 사용

`dashboard/web` 시뮬레이터는 일반 웹 UI이므로 색 표현 자유도가 큽니다.

추천:
- 도로: 짙은 회색
- NFC zone: 반투명 하늘색 또는 민트
- 위험 상태: amber
- 주문 가능 상태: green
- 결제 완료: blue-green

즉, 오른쪽 AAOS 앱은 절제하고, 왼쪽 시뮬레이터에서 시각적 설명력을 높이는 편이 좋습니다.

---

## 10. 리소스 파일은 어떻게 관리해야 하나

권장 구조:

```text
app/automotive/app/src/main/res/
  values/
    strings.xml
    colors.xml
    dimens.xml
  drawable/
    ic_drive.xml
    ic_park.xml
    ic_payment.xml
  mipmap-*/
    ic_launcher.*
```

### `strings.xml`

- 모든 화면 제목
- 액션 문구
- 주문/결제 상태 문구

### `colors.xml`

Car App Library에서 직접 많이 쓰지 못하더라도 아래는 유지할 가치가 있습니다.

```xml
<resources>
    <color name="brand_primary">#145A4A</color>
    <color name="status_safe">#2E7D32</color>
    <color name="status_warning">#ED6C02</color>
    <color name="status_danger">#C62828</color>
    <color name="status_info">#1565C0</color>
</resources>
```

이유:
- 앱 아이콘, 향후 hybrid 화면, 테스트 UI, 문서화 기준값으로 재사용 가능

---

## 11. 어떤 기능별로 어떤 파일을 둬야 하나

## 11.1 매장 접근 감지

역할:
- 차량이 주문 가능한 위치에 들어왔는지 판단

파일:
- `data/entry/EntryTriggerDataSource.kt`
- `data/entry/FakeEntryTriggerDataSource.kt`
- `core/state/DriveThruStateStore.kt`

흐름:

```text
EntryTriggerDataSource
  -> onStoreEntered(storeId)
  -> stateStore.enterStore(storeId)
  -> WaitingForEntry -> SimplifiedMenu/FullMenu
```

---

## 11.2 차량 상태 분기

역할:
- `PARK`, `DRIVE`, 속도 상태에 따라 허용 UI를 결정

파일:
- `data/vehicle/VehicleSignalProvider.kt`
- `data/vehicle/FakeVehicleSignalProvider.kt`
- `core/model/VehicleSignalSnapshot.kt`
- `core/state/SafetyCriticalStateMachine.kt`

흐름:

```text
VehicleSignalProvider
  -> VehicleSignalSnapshot
  -> SafetyCriticalStateMachine
  -> FullMenu / SimplifiedMenu / SafetyRestricted
```

---

## 11.3 메뉴 표시

역할:
- 간소화 메뉴와 전체 메뉴를 상태에 맞게 렌더링

파일:
- `data/menu/MenuRepository.kt`
- `data/menu/FakeMenuRepository.kt`
- `feature/menu/SimplifiedMenuScreen.kt`
- `feature/menu/FullMenuScreen.kt`

흐름:

```text
stateStore currentState
  -> Screen selects menu scope
  -> MenuRepository provides items
  -> ListTemplate renders
```

---

## 11.4 주문 초안

역할:
- 사용자가 선택한 메뉴를 저장하고 검토 화면으로 이동

파일:
- `core/model/OrderDraft.kt`
- `core/state/DriveThruStateStore.kt`
- `feature/order/OrderReviewScreen.kt`

흐름:

```text
menu item click
  -> stateStore.selectMenuItem(itemId)
  -> ReviewingOrder(orderDraft)
  -> OrderReviewScreen
```

---

## 11.5 결제 UI mock

역할:
- 실결제 없이 차량 내 결제 흐름을 설명

파일:
- `core/model/PaymentState.kt`
- `data/payment/FakePaymentRepository.kt`
- `core/state/PaymentStateMachine.kt`
- `feature/payment/PaymentMethodScreen.kt`
- `feature/payment/PaymentProcessingScreen.kt`
- `feature/payment/PaymentCompleteScreen.kt`

흐름:

```text
confirm payment
  -> PaymentStateMachine.requestPayment()
  -> Processing
  -> Complete
  -> PaymentCompleteScreen
```

---

## 11.6 세션 종료

역할:
- 차량이 구역을 벗어나면 주문 세션 정리

파일:
- `data/entry/EntryTriggerDataSource.kt`
- `core/state/DriveThruStateStore.kt`
- `feature/status/SessionClosedScreen.kt`

흐름:

```text
exit event
  -> stateStore.closeSession()
  -> SessionClosed
  -> WaitingForEntry or Standby
```

---

## 12. 문구와 정보량은 어떻게 조절해야 하나

차량 UI에서는 예쁜 설명보다 짧고 앞부분이 중요한 문구가 우선입니다.

예:

- 나쁨:
  - `차량이 현재 이동 중 상태이므로 전체 메뉴를 사용할 수 없고 간소화된 주문 옵션만 제공합니다.`

- 좋음:
  - `Driving mode. Quick order only.`

원칙:
- 제목은 짧게
- 첫 줄에 핵심
- 두 번째 줄은 보조 설명
- 긴 설명은 parked 상태 또는 별도 세부 흐름에서만

---

## 13. 일반 앱처럼 Compose로 화려하게 만들면 안 되나

할 수는 있지만, 현재 `DriveThru IVI`의 핵심 포인트에는 맞지 않습니다.

이유:
- 지금 프로젝트의 강점은 "차량 상태와 안전 분기"
- 자유 레이아웃 UI는 Car App Library 템플릿 앱의 본질과 거리가 있음
- 포트폴리오 메시지가 "UI 예쁘게 만든 앱"으로 흐려질 수 있음

더 좋은 전략:
- 오른쪽 AAOS 앱은 템플릿 제약 안에서 정교하게
- 왼쪽 웹 시뮬레이터는 자유 UI로 설명력을 강화

이 조합이 가장 설득력 있습니다.

---

## 14. 현재 프로젝트에 바로 적용할 다음 단계

우선순위 추천:

1. `DriveThruState`에 `Standby`, `PaymentComplete`, `SessionClosed` 추가
2. `VehicleSignalProvider` 추상화 도입
3. `SafetyCriticalStateMachine`로 `PARK/DRIVE` 분기 책임 이동
4. `EntryTriggerDataSource` 도입
5. 결제 mock 상태와 화면 추가
6. `dashboard/web` 시뮬레이터와 이벤트 브리지 연결

이 순서가 좋은 이유:
- UI가 상태 중심 구조로 바뀜
- 왼쪽 시뮬레이터와 오른쪽 앱 연결이 쉬워짐
- 영상 시나리오와 코드 구조가 일치함

---

## 15. 최종 정리

차량 애플리케이션 UI는 일반 모바일 앱처럼 "예쁜 화면을 자유롭게 조립하는 문제"가 아니라 아래 문제에 더 가깝습니다.

- 지금 차량이 어떤 상태인가
- 지금 사용자에게 무엇을 보여줘도 안전한가
- 어떤 템플릿으로 정보를 가장 짧고 확실하게 보여줄 것인가
- 신호가 바뀌면 얼마나 빨리 제한 모드로 내려갈 수 있는가

따라서 `DriveThru IVI`에서는 다음 원칙을 유지하는 것이 맞습니다.

- `Screen`은 얇게 유지
- 상태 판단은 `StateStore`와 `StateMachine`에 집중
- 차량 신호와 매장 진입 이벤트는 `data` 계층으로 격리
- 메뉴, 주문, 결제는 도메인 모델로 분리
- 색과 구조는 절제하고 정보 전달력을 우선

이 구조가 현대 계열 Android Framework / IVI 포지션에도 가장 잘 맞습니다.
