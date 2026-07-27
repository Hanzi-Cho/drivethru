# DriveThru Component Spec

## 1. App Top Bar

구성:
- leading back button
- app title
- driving caution chip
- signal icons
- current time

상태:
- `default`
- `park`
- `driving-warning`
- `low-connectivity`

규칙:
- red chip은 `driving-warning`에서만 사용
- 시간은 우측 끝 고정
- 아이콘들은 같은 baseline 정렬
- 상태 정보는 실제 값이 없어도 placeholder 영역을 유지
- prev 버튼 hit area는 아이콘보다 크게 잡는다

## 2. Bottom Navigation

아이템:
- `Menu`
- `Cart`
- `Order`
- `Setting`

상태:
- `default`
- `active`
- `badge`
- `disabled`

규칙:
- active는 filled rounded rect
- badge는 `Cart` 우선
- 라벨은 항상 노출
- active item 내부는 아이콘 위, 라벨 아래 단일 column 정렬
- inactive item도 동일한 레이아웃 축을 유지

## 3. Category Rail

용도:
- `Menu` 페이지 왼쪽 카테고리 이동

상태:
- `default`
- `active`
- `disabled`

규칙:
- 아이콘 + 텍스트 조합
- active는 filled
- inactive는 outline 또는 surface 차등
- rail item은 고정 height를 사용하고, 텍스트 길이가 길어도 1줄 유지
- rail과 content 사이 divider는 rail component의 일부가 아니라 page layout의 일부다

## 4. Menu Card

필수 요소:
- thumbnail
- title
- description
- price
- CTA button

상태:
- `default`
- `pressed`
- `unavailable`
- `quick-order`

규칙:
- 가격은 우측 정렬
- CTA는 항상 동일 크기
- description은 2줄 이하 권장

셀 구조:
- row 1 / col 1: thumbnail
- row 1 / col 2-3: title
- row 1 / col 4: price
- row 2 / col 2-3: description
- row 1-2 / col 5: CTA

텍스트 규칙:
- title: `24 / 700`
- description: `14 / 400`
- price: `22 / 700`
- CTA: `16 / 700`

## 5. Order Summary Mini Panel

필수 요소:
- label
- total amount
- total item count

상태:
- `empty`
- `filled`

규칙:
- `Menu` 페이지 좌하단 고정
- empty여도 panel은 유지
- amount와 count는 서로 다른 baseline 그룹으로 본다
- amount가 dominant, count는 supporting 정보다

## 6. Cart Item Row

필수 요소:
- image
- name
- options chips
- price
- quantity stepper
- remove action

상태:
- `default`
- `editing`
- `removing`

규칙:
- 수량 조절 버튼은 큰 터치 타깃 유지
- 옵션은 1줄 우선, 넘치면 축약

셀 구조:
- col 1: image
- col 2: title
- col 2 row 2: option chips
- col 3: price
- col 4: quantity stepper
- optional bottom trailing: remove action

타이포:
- item name: `20 / 700`
- option chip label: `12 / 600`
- price: `18 / 700`
- count: `18 / 600`

## 7. Payment Summary Panel

필수 요소:
- subtotal
- discount
- total
- primary payment CTA
- secondary edit CTA
- default card preview

상태:
- `ready`
- `processing`
- `done`
- `error`

규칙:
- total은 가장 큰 타입 스케일
- CTA는 한눈에 보이도록 high contrast
- amount rows는 label/value 2열 구조를 사용한다
- total block은 구분선 아래 독립 블록으로 둔다

타이포:
- panel title: `22 / 700`
- amount label: `14 / 500`
- amount value: `18 / 600`
- total value: `40 / 700`
- CTA label: `18 / 700`

## 8. Payment Card Preview

필수 요소:
- card label
- masked number
- holder name
- payment method icon

상태:
- `default`
- `selected`
- `expired`

규칙:
- 실제 카드 네트워크를 암시하되 과도한 브랜드 충돌은 피함

셀 구조:
- row 1: default card label + payment glyph
- row 2: card product name
- row 3: masked card number
- row 4: card holder + optional thumbnail

타이포:
- eyebrow: `12 / 600`
- card name: `20 / 700`
- masked number: `22 / 500`
- holder label: `11 / 600`

## 9. Info Message Card

용도:
- 도우말
- 픽업 안내
- 주의 문구

상태:
- `info`
- `warning`
- `success`

규칙:
- red 배경 대신 dark surface + colored accent line 우선
- 긴 문구는 3줄 이내 권장

셀 구조:
- col 1: state icon
- col 2 row 1: title
- col 2 row 2: message

타이포:
- title: `16 / 700`
- message: `14 / 500`

## 10. Setting Option Card

필수 요소:
- leading icon container
- title
- subtitle
- trailing chevron

상태:
- `default`
- `focused`
- `pressed`
- `disabled`

규칙:
- 2행 정보 구조를 유지한다
- title은 1줄, subtitle은 1줄 또는 2줄까지 허용
- chevron은 항상 우상단 또는 우중앙 trailing 정렬

타이포:
- title: `18 / 700`
- subtitle: `14 / 400`

## 11. Page-Level Data Slots

### Menu Page
- section
  - `categoryId`
  - `categoryName`
- item
  - `imageUrl`
  - `name`
  - `description`
  - `priceText`
  - `actionLabel`

### Cart Page
- cart line
  - `imageUrl`
  - `name`
  - `selectedOptions[]`
  - `unitPriceText`
  - `quantity`
  - `lineTotalText`

### Order Page
- receipt summary
  - `statusTitle`
  - `statusMessage`
  - `paymentMethodName`
  - `maskedCardNumber`
  - `paidAmountText`
  - `orderedItems[]`

### Setting Page
- setting entry
  - `icon`
  - `title`
  - `subtitle`
  - `destinationId`

## 12. Data Flow Mapping

컴포넌트는 아래 상태와 연결된다.

```text
DriveThruState
  -> top bar state
  -> bottom nav active item
  -> page layout
  -> message card tone

VehicleSignalSnapshot
  -> driving warning chip
  -> menu restriction mode

OrderDraft
  -> order summary panel
  -> cart item rows
  -> payment summary panel

PaymentState
  -> payment CTA state
  -> order completion state
```
