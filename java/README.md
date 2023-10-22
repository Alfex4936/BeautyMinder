# BeautyMinder 

Spring Boot v3.1

![image](https://github.com/Alfex4936/beautyMinder/assets/2356749/5caf6d91-ab5e-419d-8520-455c91ca59c9)

## Elasticsearch

N분 마다 전체 인덱싱하여 검색/랭킹에 이용
![image](https://github.com/LeeZEun/beautyMinder/assets/2356749/b80069c3-7b96-4c80-a2c3-8600d258819a)

# Login example
![demo](https://github.com/LeeZEun/beautyMinder/assets/2356749/88d8eb9b-1091-473d-96b5-a293f78ea337)

# Baumann Skin Type

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
