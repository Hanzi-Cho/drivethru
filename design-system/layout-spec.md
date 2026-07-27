# DriveThru Layout Spec

## 1. 기준 프레임

- concept frame: `1088 x 868`
- safe top bar height: `72`
- safe bottom nav height: `82`
- content area height: `714`
- outer horizontal padding: `24`
- outer top padding below status bar: `20`
- section gap: `16`
- card corner radius default: `16`

## 2. 글로벌 레이아웃 그리드

- 전체는 `12-column grid`로 해석한다.
- 기본 gutter는 `16`
- 좌우 margin은 `24`
- 좌측 rail이 있는 화면은 `rail + divider + content` 3구성으로 본다.
- 세로 구분선은 `1px` stroke로, `top bar 하단`부터 `bottom nav 상단`까지만 그린다.

## 3. 상단바

프레임:
- x: `0`
- y: `0`
- width: `1088`
- height: `72`
- horizontal padding: `24`

좌측 영역:
- prev icon frame: `24 x 24`
- prev와 타이틀 간격: `16`
- 앱 타이틀 기준 크기: `18`
- 앱 타이틀 weight: `700`

우측 영역:
- warning chip height: `36`
- warning chip horizontal padding: `18`
- warning chip radius: `18`
- status icon size: `18`
- icon-to-icon gap: `14`
- time left padding from divider area: `20`
- time font size: `14`
- time weight: `600`

정렬:
- 전체 baseline은 수직 중앙 정렬
- 타이틀과 우측 그룹은 동일 center line 위에 놓는다.

## 4. 하단 네비게이션

프레임:
- x: `0`
- y: `786`
- width: `1088`
- height: `82`

아이템:
- 4 equal slots
- active item width: `92`
- active item height: `52`
- active radius: `12`
- icon size: `22`
- icon-label gap: `6`
- label font size: `14`
- label weight active: `700`
- label weight inactive: `600`
- cart badge size: `18`
- cart badge offset: `+12 x, -8 y`

## 5. Menu 페이지

메인 프레임:
- top content origin: `x 24 / y 92`
- bottom content end: `y 778`

좌측 rail:
- width: `194`
- right divider gap: `20`
- divider x: `228`
- category button width: `172`
- category button height: `64`
- category button radius: `12`
- category button vertical gap: `14`
- category icon size: `20`
- category icon-to-label gap: `12`
- category label font size: `16`
- category label weight active: `700`
- category label weight inactive: `600`
- inactive opacity target: `0.64`

좌측 summary panel:
- anchor: left bottom
- width: `140`
- height: `108`
- radius: `14`
- padding: `14`
- label font size: `11`
- total font size: `24`
- count font size: `14`

우측 content:
- x: `248`
- width: `816`
- section title font size: `28`
- section title weight: `700`
- section title bottom gap: `14`
- section title trailing divider thickness: `1`
- section title and divider gap: `16`

메뉴 카드:
- width: full content
- height: `136`
- radius: `14`
- border: `1`
- internal padding: `14`
- thumbnail size: `110 x 110`
- thumbnail radius: `8`
- text block left gap from image: `18`
- CTA button width: `78`
- CTA button height: `64`
- CTA radius: `10`

메뉴 카드 열 구조:
- column 1: thumbnail
- column 2: title + description
- column 3: price
- column 4: add button

메뉴 카드 행 구조:
- row 1: title / price
- row 2: description
- row 3: reserved for option tag or blank

텍스트:
- title font size: `24`
- title weight: `700`
- description font size: `14`
- description line clamp: `2`
- price font size: `22`
- price weight: `700`
- button label font size: `16`
- button label weight: `700`

## 6. Cart 페이지

상단 안내 바:
- x: `24`
- y: `98`
- width: `1040`
- height: `72`
- radius: `12`
- horizontal padding: `22`
- left title font size: `20`
- left title weight: `700`
- right title font size: `14`
- right title weight: `700`
- right title opacity: `0.72`

본문 2열:
- left list width: `670`
- right payment width: `294`
- center gap: `20`

좌측 주문 리스트 카드:
- width: `670`
- height: `152`
- radius: `14`
- padding: `18`
- vertical gap: `14`
- thumbnail size: `112 x 112`

좌측 카드 열 구조:
- column 1: image
- column 2: title + options
- column 3: price
- column 4: quantity stepper

좌측 카드 행 구조:
- row 1: item title / price
- row 2: options chips
- row 3: remove action or reserved message

수량 stepper:
- width: `152`
- height: `56`
- radius: `8`
- internal columns: minus / count / plus
- each control min width: `44`
- count font size: `18`

우측 결제 패널:
- width: `294`
- min height: `480`
- radius: `16`
- padding: `24`
- amount row gap: `14`
- top title font size: `22`
- amount label font size: `14`
- amount value font size: `18`
- total label font size: `18`
- total value font size: `40`
- primary CTA height: `64`
- primary CTA radius: `12`
- secondary CTA height: `64`
- secondary CTA radius: `12`

## 7. Order 페이지

상단 완료 상태:
- icon circle size: `84`
- icon size: `36`
- circle radius: `42`
- state title font size: `36`
- state title weight: `700`
- message font size: `18`

결제 카드 영역:
- left card width: `440`
- left card height: `248`
- right amount card width: `308`
- right amount card height: `172`
- right action card width: `308`
- right action card height: `74`
- block gap: `20`

summary card:
- width: `768`
- min height: `260`
- radius: `16`
- padding: `20`

## 8. Setting 페이지

콘텐츠 origin:
- x: `76`
- y: `60`

title block:
- page title font size: `40`
- page subtitle font size: `16`
- subtitle top gap: `6`

2x2 option cards:
- card width: `314`
- card height: `114`
- gap x: `16`
- gap y: `14`
- radius: `12`
- padding: `16`
- leading icon circle size: `40`
- title font size: `18`
- subtitle font size: `14`
- chevron size: `16`

banner:
- y after cards: `20`
- width: `640`
- height: `150`
- radius: `12`
- text block bottom-left padding: `16`

## 9. 구분선 규칙

- section title trailing divider는 항상 title baseline 중앙에 맞춘다.
- rail divider와 card divider는 `color.bg.divider`를 사용한다.
- divider는 decorative가 아니라 grouping 용도이므로 opacity를 `0.72` 이하로 제한한다.

## 10. 적용 우선순위

- 가장 먼저 맞춰야 하는 값: frame, panel width, panel height, radius
- 그다음 맞춰야 하는 값: title size, price size, CTA size
- 마지막으로 미세 조정할 값: icon offset, chip opacity, badge 위치
