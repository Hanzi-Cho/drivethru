# DriveThru Zone Simulator

이 디렉터리는 AAOS 앱과 나란히 띄우는 좌우 분할 시연용 웹 시뮬레이터 골격입니다.

## 포함 파일

- [index.html](/D:/agentproject/car/drivethru/dashboard/web/index.html)
  - 매장 접근, 메뉴보드 도달, 비콘 인식, 이탈, PARK, DRIVE, 고속 중단 시나리오 버튼 제공
- [app.js](/D:/agentproject/car/drivethru/dashboard/web/app.js)
  - 버튼 입력을 앱 inject payload와 `adb shell am broadcast` 명령으로 변환
- [styles.css](/D:/agentproject/car/drivethru/dashboard/web/styles.css)
  - 시연용 레이아웃과 패널 스타일

## 실행

```powershell
cd D:\agentproject\car\drivethru\dashboard\web
python -m http.server 4173
```

브라우저에서 `http://localhost:4173`을 열면 됩니다.

## 역할

- 현재 단계에서는 브라우저가 직접 `adb`를 실행하지는 않습니다.
- 대신 앱이 이해하는 inject payload와 `adb shell am broadcast` 명령을 즉시 생성해 줍니다.
- 이후 단계에서 local bridge server나 desktop helper를 붙이면 이 UI에서 앱 inject를 직접 트리거할 수 있습니다.
