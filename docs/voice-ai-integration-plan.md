# DriveThru IVI Voice AI Integration Plan

## 1. 배경

DriveThru IVI는 현재 AAOS 기반 터치 주문 UX와 차량 상태 기반 UI 분기를 중심으로 설계되어 있습니다. 여기에 음성 챗봇 계층을 추가하면 `AAOS + 차량 API + AI 연동` 서사를 한 번에 보여줄 수 있습니다.

특히 현대자동차 AAOS, 차량 API, SDV IVI 맥락을 공부하면서 한국어 음성 대화형 인터페이스까지 구현하면 포트폴리오 밀도가 크게 올라갑니다.

## 2. 제안 방향

핵심 방향:
- 기존 `DriveThru IVI` 프로젝트에 음성 챗봇 기능을 확장한다.
- LLM은 Upstage `Solar` 계열 모델을 우선 검토한다.
- STT/TTS는 현실적으로 Android 기본 음성 API 또는 한국어 특화 대안을 조합한다.

이 방향의 장점:
- AAOS 도메인 이해와 AI API 연동 경험을 하나의 프로젝트로 묶을 수 있다.
- "차량 내 AI 어시스턴트 구현 경험"이라는 면접용 서사가 분명해진다.
- 단순 챗봇이 아니라 메뉴 주문, 추천, 상태 동기화까지 연결되는 IVI 유스케이스가 된다.

## 3. 포트폴리오 어필 포인트

| 레이어 | 기술 | 어필 포인트 |
| --- | --- | --- |
| Android / AAOS | Car App Library, CarService, CarAudioManager | 차량 플랫폼 이해 |
| 음성 파이프라인 | STT -> LLM -> TTS | AI 통합 실무 감각 |
| LLM API | Upstage Solar | 국내 AI 기업 연동 실적 |
| IVI 컨텍스트 | 드라이브스루 메뉴 주문 | 유스케이스 구체성 |

현대차, 현대모비스, 현대오토에버 계열 인터뷰에서는 아래 메시지로 연결하기 좋습니다.

> AAOS 기반 IVI 주문 앱에 차량 상태 인지 UI와 한국어 음성 챗봇을 결합해 SDV 인포테인먼트 UX를 직접 설계하고 구현했다.

## 4. 목표 사용자 경험

예시 시나리오:
1. 사용자가 드라이브스루 매장 진입 후 음성 대화 모드를 시작한다.
2. "헤이 드라이브스루, 세트 메뉴 추천해줘"처럼 한국어로 질의한다.
3. STT가 발화를 텍스트로 변환한다.
4. Solar LLM이 현재 매장 메뉴, 주문 초안, 차량 상태를 반영해 답변한다.
5. TTS가 응답을 읽어준다.
6. 사용자가 "그거 담아줘"라고 말하면 기존 터치 주문 상태와 동기화된다.

## 5. 아키텍처 구상

```text
[마이크 입력]
     ↓
CarAudioManager / Audio Focus
     ↓
[STT]
  Android SpeechRecognizer
  또는 한국어 특화 외부 STT
     ↓
[Solar LLM]
  메뉴 컨텍스트 + 주문 상태 + 시스템 프롬프트
     ↓
[TTS]
  Android TextToSpeech
  또는 외부 TTS
     ↓
CarAudioManager 출력
     ↓
[UI 동기화]
  기존 DriveThru 주문 화면 / 상태 저장소 반영
```

## 6. Upstage API 검토 포인트

현재 문서화된 제품군 기준으로 보면 Upstage는 아래 축이 핵심입니다.

- Solar LLM: `solar-pro`, `solar-mini` 등 채팅 완성 계열
- Document AI: OCR, 파싱
- Embeddings: 검색 및 RAG

실행 판단:
- LLM은 Upstage Solar를 중심으로 가져간다.
- STT/TTS는 Upstage 단독 풀스택 전제보다 별도 조합 가능성을 열어둔다.
- 즉, 현실적인 1차 구현은 `STT + Solar + TTS` 조합형 파이프라인이다.

## 7. 현실적인 1차 기술 조합

### 권장안

- STT:
  - Android `SpeechRecognizer`
  - 또는 Naver Clova Speech
- LLM:
  - Upstage `Solar API`
- TTS:
  - Android `TextToSpeech`
  - 또는 Naver Clova Voice

### 이유

- 한국어 음성 품질은 Android 기본 API만으로 부족할 수 있다.
- 하지만 포트폴리오 핵심 차별점은 `AAOS + 차량 상태 + Solar LLM 연동`에 있다.
- 음성 입출력은 1차 버전에서 안정성과 구현 속도를 우선하는 편이 좋다.

## 8. 프로젝트 단계 확장안

기존 Phase 4까지의 흐름 뒤에 아래 단계를 추가하는 것이 자연스럽습니다.

### Phase 5 - Voice AI Integration

#### 5-1. 음성 입력 및 오디오 포커스
- AAOS 마이크 권한 처리
- `CarAudioManager` 또는 관련 오디오 포커스 전략 정리
- 주행 중 음성 인터랙션 제약 검토

#### 5-2. 텍스트 챗봇 우선 연결
- 음성 이전에 텍스트 기반 Solar API 연동부터 검증
- AAOS 에뮬레이터에서 메뉴 컨텍스트를 넣어 응답 품질 확인

#### 5-3. IVI 도메인 시스템 프롬프트 설계
- 현재 매장 메뉴
- 품절 상태
- 주문 초안
- 운전 중 / 정차 상태
- 답변 톤과 주문 유도 정책

#### 5-4. TTS 및 UI 동기화
- LLM 응답을 음성으로 재생
- "담아줘", "빼줘", "다시 말해줘" 같은 액션을 기존 주문 상태와 동기화

#### 5-5. 선택 기능
- 웨이크 워드 트리거
- 추천 메뉴 랭킹
- 최근 주문 기반 개인화

## 9. 구현 우선순위

추천 순서:
1. Upstage 콘솔 가입 및 API 키 발급
2. Solar 모델 호출이 되는지 Kotlin 단에서 텍스트 요청 검증
3. 메뉴 컨텍스트 프롬프트 초안 작성
4. Android `SpeechRecognizer` 연결
5. `TextToSpeech` 연결
6. 주문 상태 동기화 액션 추가

## 10. 리스크와 대응

### 리스크 1. STT/TTS 품질 이슈

대응:
- 1차 버전은 Android 기본 음성 API로 빠르게 검증
- 품질이 낮으면 Clova 계열 등 한국어 특화 서비스로 교체 검토

### 리스크 2. AAOS 에뮬레이터 제약

대응:
- 텍스트 챗봇부터 먼저 붙여서 LLM 경로를 검증
- 이후 마이크 / 오디오 포커스는 실제 동작 가능한 범위까지 점진 확장

### 리스크 3. 프로젝트 범위 과확장

대응:
- "완전한 차량용 음성 비서"가 아니라 드라이브스루 주문 보조 챗봇으로 범위를 고정
- 포트폴리오 기준 완료 정의를 먼저 만든다

## 11. 완료 정의

아래 조건을 만족하면 Voice AI 단계의 1차 완료로 본다.

- AAOS 앱에서 텍스트 기반 Solar 챗 응답이 동작한다.
- 메뉴 컨텍스트를 반영한 답변이 나온다.
- 음성 입력 또는 버튼 기반 발화 시작이 가능하다.
- TTS로 응답을 재생할 수 있다.
- 최소 1개 이상의 주문 액션이 기존 주문 상태와 동기화된다.

## 12. 결론

`DriveThru IVI x Upstage Solar Voice Chatbot`은 자동차 도메인과 AI 도메인을 동시에 강하게 보여줄 수 있는 확장안입니다.

이 프로젝트는 단순히 "챗봇 붙여봤다" 수준이 아니라 아래 메시지를 만들 수 있어야 합니다.

> AAOS 기반 차량 주문 UX에 차량 상태 인지, Driver Distraction 대응, 한국어 LLM 연동, 음성 인터랙션까지 설계한 SDV IVI 프로토타입을 구현했다.
