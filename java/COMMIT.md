# 📝 일반

- 🔥 [Clean] 불필요한 코드나 파일을 제거할 때
- 🌟 [Feature] 새로운 기능 추가할 때
- 🐛 [Fix] 버그 수정할 때
- 🚀 [Performance] 성능 향상을 위한 코드 변경할 때
- git commit -m "🚀 [Redis] Implement Redis Pipelining and Dynamic Scheduling for Realtime Ranking" -m ""

Description:
- Incorporated Redis pipelining in processKeywordEvents method to optimize Redis interactions and improve performance.
- Introduced dynamic scheduling for processKeywordEvents method to adjust the execution frequency based on event volume, enhancing system responsiveness to varying load conditions.
- Updated cron expression dynamically in processKeywordEvents method to either increase or decrease the frequency of method execution depending on the volume of events.
- This change aims to enhance the performance and adaptability of the realtime ranking system to varying load conditions."

# 🏗️ 구조

- 🎨 [Style] 코드 형식을 변경할 때 (들여쓰기, 세미콜론 추가/제거 등)
- 🏷️: 타입 추가/변경할 때
- 🚧: 작업 중인 코드를 커밋할 때

# 🛠️ 리팩터링

- ♻️ [Refactor] 코드 리팩터링할 때
- 🛠: 코드 수정이나 버그 수정이 아닌 기술적 작업을 했을 때
- ✅ [Test] 테스트 추가 혹은 수정할 때

# 📦 의존성

- ⬆️: 의존성 패키지를 업그레이드할 때
- ⬇️: 의존성 패키지를 다운그레이드할 때
- 📦: 새로운 패키지를 추가할 때

# 📖 문서

- 📚: 문서 추가/수정할 때
- 💡: 주석 추가/수정할 때

# 🌐 네트워크

- 🌐 [API] 네트워크 레이어 혹은 API 변경할 때

# 🔒 보안

- 🔒: 보안 관련 변경할 때

# 🌈 UI/디자인

- 🎨: UI 혹은 디자인 변경할 때

# 🚂 배포/빌드

- 🚂: 배포 혹은 빌드 설정 변경할 때

# 🚇 CI/CD

- 🚇: CI/CD 설정을 변경할 때
