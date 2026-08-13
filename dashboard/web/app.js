const scenarios = {
  approach: {
    label: "GPS Approach",
    payload: {
      source: "gps",
      stage: "APPROACHING",
      lane_point: "ENTRANCE",
      latitude: 37.4979,
      longitude: 127.0276
    },
    geoFix: "adb emu geo fix 127.0276 37.4979",
    validation: [
      "앱 상태가 STANDBY에서 STORE_READY로 전이되는지 확인",
      "헤더 zone stage가 APPROACHING으로 보이는지 확인",
      "매장명이 McDonald's Gangnam DT로 표시되는지 확인"
    ],
    markerClass: "state-approach"
  },
  ready: {
    label: "GPS Menu Board",
    payload: {
      source: "gps",
      stage: "ORDERING_READY",
      lane_point: "MENU_BOARD",
      latitude: 37.4979,
      longitude: 127.0276
    },
    geoFix: "adb emu geo fix 127.0276 37.4979",
    validation: [
      "PARK 상태면 FULL_MENU까지 자동 진입하는지 확인",
      "메뉴 리스트가 로드되는지 확인",
      "entry trigger source가 GPS_GEOFENCE로 남는지 확인"
    ],
    markerClass: "state-ready"
  },
  beacon: {
    label: "Beacon Ready",
    payload: {
      source: "beacon",
      stage: "ORDERING_READY",
      lane_point: "MENU_BOARD",
      beacon_id: "beacon-mcd-001"
    },
    geoFix: "위치 이동 불필요",
    validation: [
      "entry trigger source가 BEACON으로 표시되는지 확인",
      "GPS 없이도 매장 메뉴가 로드되는지 확인",
      "Beacon ready 이후 주문 UI로 진입 가능한지 확인"
    ],
    markerClass: "state-beacon"
  },
  exit: {
    label: "Exit Zone",
    payload: {
      source: "gps",
      stage: "EXIT"
    },
    geoFix: "adb emu geo fix 127.0200 37.4900",
    validation: [
      "세션이 종료되고 STANDBY로 복귀하는지 확인",
      "order draft와 active store가 비워지는지 확인",
      "상태 메시지가 store zone 이탈로 갱신되는지 확인"
    ],
    markerClass: "state-exit"
  },
  park: {
    label: "PARK + Stop",
    payload: {
      source: "vehicle",
      gear: "PARK",
      parking: true,
      speed_mps: 0.0
    },
    geoFix: "위치 이동 없음",
    validation: [
      "기어가 PARK로 표시되는지 확인",
      "STOP_STATE였다면 resume 가능 상태가 되는지 확인",
      "속도 0.0이 diagnostics에 반영되는지 확인"
    ],
    markerClass: "state-park"
  },
  drive: {
    label: "DRIVE",
    payload: {
      source: "vehicle",
      gear: "DRIVE",
      parking: false
    },
    geoFix: "위치 이동 없음",
    validation: [
      "주문 중이면 STOP_STATE로 전이되는지 확인",
      "order draft가 보존되는지 확인",
      "헤더 기어가 DRIVE로 갱신되는지 확인"
    ],
    markerClass: "state-drive"
  },
  highSpeed: {
    label: "High Speed Abort",
    payload: {
      source: "vehicle",
      gear: "DRIVE",
      parking: false,
      speed_mps: 8.2
    },
    geoFix: "위치 이동 없음",
    validation: [
      "안전 속도 임계치 초과 후 세션이 종료되는지 확인",
      "화면이 STANDBY로 복귀하는지 확인",
      "status message에 speed threshold 문구가 포함되는지 확인"
    ],
    markerClass: "state-highspeed"
  }
};

const payloadOutput = document.getElementById("payloadOutput");
const adbOutput = document.getElementById("adbOutput");
const geoOutput = document.getElementById("geoOutput");
const validationList = document.getElementById("validationList");
const carMarker = document.getElementById("carMarker");

function renderScenario(key) {
  const scenario = scenarios[key];
  payloadOutput.textContent = JSON.stringify(scenario.payload, null, 2);
  adbOutput.textContent = buildAdbCommand(scenario.payload);
  geoOutput.textContent = scenario.geoFix;
  validationList.innerHTML = scenario.validation.map((item) => `<li>${item}</li>`).join("");
  carMarker.className = `car-marker ${scenario.markerClass}`;
  carMarker.textContent = scenario.label;
}

function buildAdbCommand(payload) {
  const args = ["adb shell am broadcast -a com.hanzi.drivethru.action.INJECT_DEBUG_EVENT"];
  Object.entries(payload).forEach(([key, value]) => {
    if (typeof value === "string") {
      args.push(`--es ${key} ${value}`);
      return;
    }
    if (typeof value === "boolean") {
      args.push(`--ez ${key} ${value ? "true" : "false"}`);
      return;
    }
    args.push(`--ef ${key} ${value}`);
  });
  return args.join(" ");
}

document.querySelectorAll("[data-scenario]").forEach((element) => {
  element.addEventListener("click", () => {
    renderScenario(element.dataset.scenario);
  });
});

renderScenario("approach");
