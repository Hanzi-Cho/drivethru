# DriveThru AAOS 현재 화면과 디자인 작업 인계

2026-11-06에 소스 커밋 [`d9ef12e`](https://github.com/Hanzi-Cho/drivethru/commit/d9ef12efaef8d424efea335debcd529836c2bf0e)를 빌드해 Android Automotive OS 15 Google Play 에뮬레이터에서 실행했다. 화면 PNG는 모두 1408×792 원본이다. 기본 160 dpi에서 잘리는 영역을 별도로 기록했고, Compose 세부 상태는 96 dpi로 바꿔 관찰했다.

## Product Brief 초안

- **대상과 상황** — 드라이브스루 차로에 진입해 주문 지점에서 정차한 운전자.
- **할 일** — 감지된 매장을 확인하고, 정차 상태에서 상품과 총액을 검토한다. 차가 움직이면 현재 초안을 보존하고, 다시 정차한 뒤 운전자가 명시적으로 이어간다.
- **핵심 흐름** — 대기 → 매장 감지 → PARK에서 메뉴 활성화 → 상품 담기 → 초안 검토. 중간에 DRIVE로 전환하면 주문 중단 → PARK → 명시적 재개. 매장을 이탈하면 대기로 돌아간다.
- **별도 검증 경로** — Car App Library의 Menu → Cart → Order 흐름으로 호스트 제약을 확인한다. 현재 빌드에서는 상품이 있는 Cart에서 종료된다. 음성 주문과 모바일 인계는 후속 설계 범위다.
- **시안 비교 기준** — 기본 160 dpi에서 핵심 정보와 조작이 처음부터 보이는지, 정차와 주행 사이에서 주문 가능 여부를 즉시 이해하는지, 초안 보존과 재개가 명확한지 비교한다.

## Compose 화면과 상태

| 화면 | 진입 및 실제 관찰 | 현재 수준 | 캡처 |
| --- | --- | --- | --- |
| 기본 해상도 | 앱 실행 및 PARK 후 메뉴 활성화. 헤더와 데모 도구가 세로 공간을 차지한다. | 160 dpi에서는 대기 본문과 메뉴가 가려지고 진단도 잘린다. | [기본 대기](screenshots/01-enhanced-native-density.png) · [기본 메뉴](screenshots/00-enhanced-menu-native-density.png) |
| 대기 `STANDBY` | 앱 시작 후 매장 밖. 모드, 기어, 속도와 진단이 표시된다. | 대기 안내는 96 dpi에서 확인했다. | [02](screenshots/02-enhanced-standby.png) |
| 매장 감지 `STORE_READY` | DRIVE에서 에뮬레이터 GPS를 매장 중심으로 옮기면 매장명과 PARK 안내가 표시된다. | `Open full menu`가 먼저 보이고 실제 메뉴 진입에는 안전 조건이 필요하다. | [03](screenshots/03-enhanced-store-ready.png) |
| Enhanced 메뉴 `FULL_MENU` | `PARK + stop` 후 카테고리, 메뉴, 주문 요약이 표시된다. `Add`로 Big Mac Set 1개를 담으면 총액이 8,900원으로 갱신된다. | 오른쪽 주문 요약이 즉시 갱신된다. 96 dpi에서도 목록과 CTA 라벨 하단 일부가 잘린다. | [04](screenshots/04-enhanced-menu.png) · [05](screenshots/05-enhanced-item-added.png) |
| Enhanced 초안 검토 `CART_REVIEW` | `Review current draft`를 누르면 헤더에 검토 안내가 표시된다. | 메뉴와 요약 레이아웃이 유지돼 상태 전환을 본문에서 알아보기 어렵다. | [06](screenshots/06-enhanced-cart-review.png) |
| 주문 중단과 복귀 `STOP_STATE` | DRIVE로 바꾸면 초안 1개가 유지된다. PARK에서 `Continue ordering`을 누르면 동일 초안과 총액으로 복귀한다. | PARK 전환 직후에는 중단 사유가 `UNKNOWN`으로 표시된다. | [중단](screenshots/07-enhanced-stop.png) · [PARK](screenshots/08-enhanced-stop-parked.png) · [재개](screenshots/09-enhanced-resumed.png) |
| Classic 메뉴 | 안전 상태에서 `CLASSIC_CUSTOM`을 선택하면 단일 목록과 `Review cart` 버튼이 표시된다. | Enhanced와 같은 주문 상태를 목록 중심으로 보여준다. | [11](screenshots/11-classic-menu.png) |
| Classic 초안 검토 | 검토 상태에서 `CLASSIC_CUSTOM`을 선택하면 항목, 수량, 총액과 돌아가기·종료 버튼이 보인다. | 검토 화면이 별도 패널로 구분된다. | [10](screenshots/10-classic-cart-review.png) |
| 매장 이탈 | `Exit zone`을 누르면 `OUTSIDE`와 대기 안내가 표시된다. | 주문 세션 종료가 확인된다. | [12](screenshots/12-classic-exit.png) |

## Car App Library 화면

| 화면 | 진입 및 실제 관찰 | 현재 수준 | 캡처 |
| --- | --- | --- | --- |
| Menu | Compose에서 `CAR_TEMPLATE`을 누르면 호스트가 열린다. Big Mac Set 행을 누르면 주문 요약이 0원에서 8,900원, 0개에서 1개로 바뀐다. | 호스트 탭과 항목 추가 동작을 확인했다. | [기본](screenshots/13-template-menu.png) · [추가 후](screenshots/17-template-item-added.png) |
| Cart · 0원 | 이 실행에서 메뉴 요약이 0개일 때 Cart 탭에 상품 행 없이 모의 결제 수단과 `Pay now`가 보였다. | 소스상 시드 항목 ID와 현재 메뉴 조회 결과가 어긋나면 항목이 0개여도 내부 카트 존재 조건이 참이 될 수 있다. | [14](screenshots/14-template-cart-empty.png) |
| Cart · 상품 있음 | Big Mac Set을 담고 Cart 탭을 열면 앱이 종료되고 Compose로 돌아간다. | `PaneTemplate`에서 클릭 리스너가 달린 장바구니 행을 거부했다. 코드 위치는 아래에 있다. | [종료 후 화면](screenshots/18-template-cart-crash-return.png) |
| Order · 주문 없음 | Order 탭에 완료 주문 없음과 `Open cart`가 표시된다. | 영수증을 만드는 모의 주문 코드는 있다. 상품이 있는 Cart에서 종료돼 영수증 화면은 확인하지 못했다. | [15](screenshots/15-template-order-empty.png) |
| Setting | 결제 수단, 주문 내역, 즐겨찾기 주소, 계정 관리 목록이 보인다. | 메뉴 행은 설명용이며 소스에 세부 화면 이동 핸들러가 없다. | [16](screenshots/16-template-settings.png) |

## 관찰 범위와 재현

- 루트에서 `gradlew.bat :app:automotive:assembleDebug`로 빌드하고 AAOS에 디버그 APK를 설치했다. Compose는 `adb -s <AAOS_SERIAL> shell am start -n com.hanzi.drivethru/.app.DriveThruActivity`로 진입했다.
- GPS 입력은 `adb -s <AAOS_SERIAL> emu geo fix 127.0276 37.4979`로 재현했다. 앱의 GPS 접근 버튼으로 별도의 `APPROACHING` 화면을 관찰하지 못했다. 코드에서 GPS 제공자가 접근으로 분류하는 반경은 매장 판정 반경보다 넓다.
- 차량 신호는 Compose 데모 컨트롤과 에뮬레이터 폴백을 사용했다. 실차 센서, 물리 BLE, 주행 안전 인증, 접근성 보조기기, 실제 결제와 모바일 인계는 별도 검증 범위다. 스크린샷의 결제 수단은 [FakePaymentMethodRepository](../../../app/automotive/app/src/main/java/com/hanzi/drivethru/data/payment/FakePaymentMethodRepository.kt)의 데모 값이다.
- 96 dpi 관찰은 `adb -s <AAOS_SERIAL> shell wm density 96`으로 재현한다. 기본 밀도 복원은 `adb -s <AAOS_SERIAL> shell wm density reset`이다.

## 다음 PC에서 이어가기

1. DriveThru 저장소에서 `git fetch origin design/aaos-product-design`을 실행한다. 처음 작업을 가져올 때 `git switch -c design/aaos-product-design FETCH_HEAD`로 작업 브랜치를 연다.
2. 해당 체크아웃을 Codex 프로젝트로 열고 Product Design을 활성화한다.
3. 아래 프롬프트로 화면과 결정을 불러온다. 앱을 다시 빌드하지 않아도 기록된 화면을 검토할 수 있다.

> @Product Design, `docs/product-design/aaos/README.md`와 연결된 스크린샷을 DriveThru AAOS의 현재 기준으로 읽어줘. `design-system/`의 기존 요구사항과 토큰을 함께 참고해줘. 필요하면 이 자료를 이 PC의 Product Design 컨텍스트에도 등록해줘. 탐색에서는 세 시안을 빠르게 비교하고, 선택 이후에는 화면 간 일관성과 정밀도를 우선해줘.

시안 이미지, 선택 기록, 상세 설계, 프로토타입 코드와 검증 결과는 후속 단계에서 같은 브랜치에 버전 관리한다.

## 코드 및 디자인 참조

- Compose 화면과 모드 선택은 [DriveThruActivity.kt](../../../app/automotive/app/src/main/java/com/hanzi/drivethru/app/DriveThruActivity.kt)에 있다.
- 안전 조건, 매장 활성화, 메뉴·카트·중단 상태 전이는 [CustomUiFlowCoordinator.kt](../../../app/automotive/app/src/main/java/com/hanzi/drivethru/feature/customui/CustomUiFlowCoordinator.kt)에 있다.
- 호스트 탭은 [DriveThruTabRootScreen.kt](../../../app/automotive/app/src/main/java/com/hanzi/drivethru/feature/root/DriveThruTabRootScreen.kt)에 있다. [CartTemplateRenderer.kt](../../../app/automotive/app/src/main/java/com/hanzi/drivethru/feature/cart/CartTemplateRenderer.kt#L56)에서 `PaneTemplate`을 생성하고 [buildCartRow](../../../app/automotive/app/src/main/java/com/hanzi/drivethru/feature/cart/CartTemplateRenderer.kt#L76)에서 클릭 리스너를 붙인다.
- 시각 요구사항과 토큰은 [design-system](../../../design-system/README.md)에 있다. 토큰의 amber 중심 인터랙션과 현재 Compose의 보라색 CTA는 별도로 비교한다.
