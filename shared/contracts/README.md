# Shared Contracts

이 디렉터리는 업체별 설정 파일이 따라야 할 공통 스키마를 둡니다.

- `store.config.schema.json`
  - 업체/매장 단위 설정
  - geofence, beacon, menu source mode, asset/API 경로 정의
- `menu.schema.json`
  - 메뉴 항목, 가격, 이미지, 옵션 그룹 참조 정의
- `order.schema.json`
  - 옵션 그룹, 선택 제한, 추가 가격 정의
- `resolver-map.schema.json`
  - 위치/비콘 이벤트를 어떤 `storeId`에 매핑할지 정의

런타임 기준 tenant 데이터 기본 경로:
- `app/automotive/app/src/main/assets/tenants/`

예시 브랜드:
- `mcdonalds`
- `burger-king`
- `gimbap-heaven`
