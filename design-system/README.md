# DriveThru Design System

## 목적

이 디렉터리는 `car/drivethru`의 차량용 UI 기준을 한곳에 고정하는 디자인 시스템 경로입니다.

목표:
- 화면 요구사항을 페이지별로 명확히 정리
- 차량 UI에 맞는 색/간격/타이포 토큰을 고정
- AAOS 앱과 왼쪽 시뮬레이터가 같은 시각 언어를 공유
- 추후 Figma, web simulator, Android resource로 쉽게 변환 가능하게 유지

## 파일 구성

- `requirements.md`
  - 글로벌 레이아웃, 상단바, 하단바, 각 페이지 요구사항
- `component-spec.md`
  - 반복 컴포넌트 구조, 셀 단위 정보 구조, 상태 정의
- `layout-spec.md`
  - 시안 재현을 위한 프레임, 패딩, 반경, 열/행, 구분선 규칙
- `tokens.json`
  - 컬러, 타이포, 간격, 반경, elevation, 아이콘 크기 토큰
- `reference-images.md`
  - 프로젝트 내부에 보관한 시안 이미지 목록과 대응 관계
- `reference-images/`
  - 원본 시안 PNG 레퍼런스 보관 경로

## 색상 정책

이번 버전은 "맥도날드 같은 강한 레드 브랜딩"을 그대로 쓰지 않고 아래 원칙을 따릅니다.

- 기본 배경: dark neutral
- 주요 인터랙션: warm yellow / amber 계열
- 보조 강조: teal 계열 유지 가능
- 위험/주행 중 주의: red 전용

이유:
- 차량 UI에서 전면 red 사용은 피로도와 경고 의미 충돌 위험이 큼
- red는 경고와 안전 제한 의미에 남겨두는 편이 더 적절함
- yellow/amber는 fast-food 인상을 주면서도 경고 색과 역할 분리가 쉬움

즉:
- 브랜드 포인트는 `yellow`
- 안전 경고는 `red`
- 현재 teal 계열은 정보/선택/보조 상태로 유지 가능

## 해상도 기준

우선 기준 프레임:
- Primary concept frame: `1088 x 868`
- AAOS emulator content frame: `1408 x 696`
- Full display reference: `1408 x 792`

현재 데모 우선순위:
1. Landscape 먼저 확정
2. Portrait는 정보량 축소 버전으로 파생

해석 원칙:
- `1088 x 868`은 첨부 시안의 비율과 카드 밀도를 고정하는 디자인 기준 프레임
- `1408 x 696`은 실제 AAOS 콘텐츠 영역 기준
- 구현 시에는 토큰 값 유지, 여백만 비례 확장

## 구현 연결

이 토큰은 아래 대상으로 연결될 수 있습니다.

- `dashboard/web` 시뮬레이터 CSS 변수
- Android `colors.xml`, `dimens.xml`, `strings.xml`
- 디자인 툴의 local variables

## 주의

Car App Library 기반 실제 AAOS 앱은 모든 색과 레이아웃을 자유롭게 그릴 수 없습니다.
따라서 이 디자인 시스템은 두 층으로 해석합니다.

- `Simulator / concept UI`
  - 자유로운 색, 카드, 레이아웃 가능
- `AAOS host UI`
  - 템플릿 제약 안에서 의미와 정보 구조를 유지

핵심은 픽셀 복제가 아니라 정보 계층과 상태 의미를 일치시키는 것입니다.

## 운영 원칙

- 시안 재현 우선순위는 `정보 구조 > 대비 > 간격 리듬 > 색감 > 픽셀 정확도` 순서다.
- 자유형 UI를 그릴 수 있는 시뮬레이터와 커스텀 AAOS UI에서는 본 경로의 수치를 그대로 사용한다.
- Car App Library 구현에서는 본 경로를 "정보 구조 명세"로 사용하고, host 제한으로 바뀌는 부분은 별도 주석을 남긴다.
