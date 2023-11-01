# BeautyMinder 💄✨

BeautyMinder is the culmination of our capstone design class, commenced in September 2023.

It manifests as a cosmetic recommendation app, meticulously tuned to individual skin types as determined through the Baumann skin type assessment.

# Team Members 👥
| Name          | Student ID | Role             | Major                     |
|---------------|------------|------------------|---------------------------|
| **Jieun Lee** ([LeeZEun](https://github.com/LeeZEun)) | 202020719  | Frontend         | Software Engineering      |
| **Suji Bae** ([Bae-suji](https://github.com/Bae-suji))  | 201620984  | Frontend         | Software Engineering      |
| **Yoon Wook Cho** ([yoonwook](https://github.com/yoonwook))|201720730| Frontend         | Software Engineering      |
| **Heesang Kwak** ([KWAKMANBO](https://github.com/KWAKMANBO))|202022311 | Frontend         | Software Engineering      |
| **Seok Won Choi** ([Alfex4936](https://github.com/Alfex4936))|201720710| Backend          | Software Engineering      |

# Technology Stack 🛠️

| Area                  | Technology                                      |
|-----------------------|-------------------------------------------------|
| **Frontend Framework**| Flutter                                         |
| **Backend Server**    | AWS EC2 (Docker: Spring Boot v3.1+Redis+Logstash)            |
| **Database**          | MongoDB (hosted on Atlas)|
| **Real-Time Metrics** | Redis                                           |
| **Search Engine**     | Elasticsearch (AWS OpenSearch)                                   |
| **Data Visualization**| Kibana (AWS OpenSearch Dashboard)                                          |
| **Text Summarization**| GPT-4 API                                        |
| **Notification Svcs** | Naver Cloud SMS API, SMTP Protocol              |
| **CI/CD**             | GitHub Actions                                  |

# System Architecture 🏗️
![System Architecture](https://github.com/Alfex4936/beautyMinder/assets/2356749/5caf6d91-ab5e-419d-8520-455c91ca59c9)

## Detailed Breakdown 🔍
- **Redis**: Harnesses real-time metrics like product click counters, search hit counters, and favorite counters, utilizing pipeline/batch methods for data collection.
- **MongoDB**: The cornerstone for data persistence.
- **Elasticsearch**: The search conduit within the app, empowering users to delve into product data based on cosmetic name, brand name, category, keywords, and review texts.
- **Kibana**: The lens to our data, illustrating the narrative encoded in Elasticsearch data.
- **GPT-4 API**: Our text maestro, summarizing reviews for each cosmetic product, categorized by high and low ratings for a nuanced understanding of user feedback.

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
