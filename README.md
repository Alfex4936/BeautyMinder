# BeautyMinder 💄✨

BeautyMinder is the culmination of our capstone design class, commenced in September 2023.

A comprehensive cosmetics management app, meticulously tuned to individual skin types as determined through the Baumann skin type assessment.

# Team Members 👥
| Name                                                          | Role           | Major                     |
|---------------------------------------------------------------|----------------|---------------------------|
| **Jieun Lee** ([LeeZEun](https://github.com/LeeZEun))         | Frontend        | Software Engineering      |
| **Suji Bae** ([Bae-suji](https://github.com/Bae-suji))        | Frontend         | Software Engineering      |
| **Yoon Wook Cho** ([yoonwook](https://github.com/yoonwook))   | Frontend         | Software Engineering      |
| **Heesang Kwak** ([KWAKMANBO](https://github.com/KWAKMANBO))  | Frontend         | Software Engineering      |
| **Seok Won Choi** ([Alfex4936](https://github.com/Alfex4936)) | Backend          | Software Engineering      |

# Technology Stack 🛠️

> [!IMPORTANT]
> Spring Boot v3.1.3, ELK

| Area                 | Technology                                                                                                                                                                      |
|----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Frontend** | [Flutter](https://flutter.dev/)                                                                                                                                                 |
| **Backend**    | [AWS EC2](https://aws.amazon.com/ko/ec2/) (Docker: Spring Boot+Redis+Logstash+FastAPI)                                                                                          |
| **Database**         | MongoDB (hosted on [Atlas](https://www.mongodb.com/ko-kr/atlas/database)), [AWS S3](https://aws.amazon.com/ko/s3/)                                                              |
| **Real-Time Metrics** | [Redis](https://redis.io/)                                                                                                                                                      |
| **WebSocket**        | [STOMP](https://github.com/stomp-js/stompjs)                                                                                                                                    |
| **Search Engine**    | Elasticsearch ([AWS OpenSearch](https://aws.amazon.com/ko/what-is/opensearch/))                                                                                                 |
| **Log analysis**     | [Logstash](https://www.elastic.co/kr/logstash), Kibana ([AWS OpenSearch Dashboard](https://docs.aws.amazon.com/ko_kr/opensearch-service/latest/developerguide/dashboards.html)) |
| **Text Summarization** | [GPT API](https://platform.openai.com/docs/guides/text-generation)                                                                                                              |
| **Image OCR**        | [Google Cloud Vision](https://cloud.google.com/vision?hl=ko)                                                                                                                    |
| **Notification Svcs** | [Naver Cloud SMS API](https://api.ncloud-docs.com/docs/ai-application-service-sens-smsv2), SMTP Protocol                                                                        |
| **DevOps**           | [JUnit5](https://junit.org/junit5/), [Locust](https://locust.io/), [GitHub Actions](https://github.com/features/actions)                                                        |

# System Architecture 🏗️
![sa](https://github.com/beminder/BeautyMinder/assets/2356749/dec95e25-5b29-4a6e-a962-274c845f263b)

# Baumann Skin Type Survey
![types](https://cdn.shopify.com/s/files/1/0740/5984/1838/files/img_1_-_16-baumann-skin-types_800x.png?v=1689709313)

1. **Individual Question Score Calculation:**

For each question in the survey, a score is calculated based on the selected option. The scoring function can be represented as:

$$
\text{{score}}(q_i) =
\begin{cases}
1.0, & \text{if option 1 is selected;} \\
2.0, & \text{if option 2 is selected;} \\
3.0, & \text{if option 3 is selected;} \\
4.0, & \text{if option 4 is selected;} \\
2.5, & \text{if option 5 is selected (special case);} \\
0.0, & \text{if the choice is invalid or for specific two-choice questions.}
\end{cases}
$$

For certain two-choice questions, a different rule applies:

$$
\text{{score}}(q_i) =
\begin{cases}
0.0, & \text{if option 1 is selected;} \\
5.0, & \text{if option 2 is selected.}
\end{cases}
$$

2. **Aggregate Category Scores:**

For each category (A, B, C, D), the total score is the sum of the individual question scores within that category. If \(n\) is the number of questions in a category, and \(q_i\) represents each question:

$$
\text{{Total Score for a Category}} = \sum_{i=1}^{n} \text{{score}}(q_i)
$$

3. **Moisture Score Calculation:**

The moisture score is calculated specifically from certain questions, represented as \(m_i\). If we assume there are \(k\) questions contributing to the moisture score:

$$
\text{{Moisture Score}} = \left( \frac{\sum_{i=1}^{k} \text{{score}}(m_i)}{16} \right) \times 100
$$

4. **Skin Type Determination:**

Each category score is compared to a threshold to determine the skin type descriptor. This can be represented with the following conditions:

$$
\text{{Skin Type}} =
\begin{cases}
O, & \text{if A score} \geq 22; \\
D, & \text{otherwise;}
\end{cases}
\]
\[
\begin{cases}
R, & \text{if B score} \geq 32; \\
S, & \text{otherwise;}
\end{cases}
\]
\[
\begin{cases}
P, & \text{if C score} \geq 28.5; \\
N, & \text{otherwise;}
\end{cases}
\]
\[
\begin{cases}
W, & \text{if D score} \geq 42.5; \\
T, & \text{otherwise.}
\end{cases}
$$

5. **Result Compilation:**

The final skin type is a string concatenation of the individual skin type descriptors from each category, represented as:

$$
\text{{Final Skin Type}} = \text{{Skin Type from A}} + \text{{Skin Type from B}} + \text{{Skin Type from C}} + \text{{Skin Type from D}}
$$

# Elasticsearch

> [!IMPORTANT]
> 은전한닢 + N-gram 

![image](https://github.com/LeeZEun/beautyMinder/assets/2356749/b80069c3-7b96-4c80-a2c3-8600d258819a)


# Email verification
![email](https://github.com/beminder/BeautyMinder/assets/2356749/5c674c89-769f-420d-8190-af488e3425fa)
