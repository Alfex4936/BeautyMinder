# BeautyMinder 

> [!IMPORTANT]
> Spring Boot v3.1.3

![image](https://github.com/Alfex4936/beautyMinder/assets/2356749/5caf6d91-ab5e-419d-8520-455c91ca59c9)

## Elasticsearch

N분 마다 전체 인덱싱하여 검색/랭킹에 이용
![image](https://github.com/LeeZEun/beautyMinder/assets/2356749/b80069c3-7b96-4c80-a2c3-8600d258819a)

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

# OCR
https://github.com/beminder/BeautyMinder/assets/2356749/585bbfd4-1767-479a-99fb-fc40734a3c47

# ElasticSearch Korean Indexing

![product keyword kibana](https://github.com/LeeZEun/beautyMinder/assets/2356749/22723ed1-2398-4e77-aff0-7f41516c17ad)

리뷰 텍스트를 검색하고 싶어서 사용하게 되었다.

AWS OpenSearch는 기본으로 [`은전한닢`](https://aws.amazon.com/ko/blogs/korea/amazon-elasticsearch-service-now-supports-korean-language-plugin/)을 사용한다.

어절까지 break하게 하였다.

## 분석기 설정/매핑

```curl
GET /reviews/_analyze
{
  "text": "얇고 밀착력 좋아 붙이고 있기는 좋아요. 커팅이 되어있어 눈밑 붙이기도 편해요. 하지만 눈 가까이 닦기에는 따갑네요",
  "field": "content"
}
```

## 결과

```json
{
  "tokens": [
    {
      "token": "얇",
      "start_offset": 0,
      "end_offset": 1,
      "type": "V",
      "position": 0
    },
    {
      "token": "얇고",
      "start_offset": 0,
      "end_offset": 2,
      "type": "EOJ",
      "position": 0
    },
    {
      "token": "밀착",
      "start_offset": 3,
      "end_offset": 5,
      "type": "N",
      "position": 1
    },
    {
      "token": "밀착력",
      "start_offset": 3,
      "end_offset": 6,
      "type": "EOJ",
      "position": 1,
      "positionLength": 2
    },
    {
      "token": "력",
      "start_offset": 5,
      "end_offset": 6,
      "type": "N",
      "position": 2
    },
    {
      "token": "좋",
      "start_offset": 7,
      "end_offset": 8,
      "type": "V",
      "position": 3
    },
    {
      "token": "좋아",
      "start_offset": 7,
      "end_offset": 9,
      "type": "EOJ",
      "position": 3
    },
    {
      "token": "붙이",
      "start_offset": 10,
      "end_offset": 12,
      "type": "V",
      "position": 4
    },
    {
      "token": "붙이고",
      "start_offset": 10,
      "end_offset": 13,
      "type": "EOJ",
      "position": 4
    },
    {
      "token": "있",
      "start_offset": 14,
      "end_offset": 15,
      "type": "V",
      "position": 5
    },
    {
      "token": "있기는",
      "start_offset": 14,
      "end_offset": 17,
      "type": "EOJ",
      "position": 5
    },
    {
      "token": "좋",
      "start_offset": 18,
      "end_offset": 19,
      "type": "V",
      "position": 6
    },
    {
      "token": "좋아요",
      "start_offset": 18,
      "end_offset": 21,
      "type": "EOJ",
      "position": 6
    },
    {
      "token": "커팅",
      "start_offset": 23,
      "end_offset": 25,
      "type": "N",
      "position": 7
    },
    {
      "token": "커팅이",
      "start_offset": 23,
      "end_offset": 26,
      "type": "EOJ",
      "position": 7
    },
    {
      "token": "되",
      "start_offset": 27,
      "end_offset": 28,
      "type": "V",
      "position": 8
    },
    {
      "token": "되어",
      "start_offset": 27,
      "end_offset": 29,
      "type": "EOJ",
      "position": 8
    },
    {
      "token": "있",
      "start_offset": 29,
      "end_offset": 30,
      "type": "V",
      "position": 9
    },
    {
      "token": "있어",
      "start_offset": 29,
      "end_offset": 31,
      "type": "EOJ",
      "position": 9
    },
    {
      "token": "눈",
      "start_offset": 32,
      "end_offset": 33,
      "type": "N",
      "position": 10
    },
    {
      "token": "밑",
      "start_offset": 33,
      "end_offset": 34,
      "type": "N",
      "position": 11
    },
    {
      "token": "붙이",
      "start_offset": 35,
      "end_offset": 37,
      "type": "V",
      "position": 12
    },
    {
      "token": "붙이기도",
      "start_offset": 35,
      "end_offset": 39,
      "type": "EOJ",
      "position": 12
    },
    {
      "token": "편하",
      "start_offset": 40,
      "end_offset": 42,
      "type": "V",
      "position": 13
    },
    {
      "token": "편해요",
      "start_offset": 40,
      "end_offset": 43,
      "type": "EOJ",
      "position": 13
    },
    {
      "token": "하지만",
      "start_offset": 45,
      "end_offset": 48,
      "type": "M",
      "position": 14
    },
    {
      "token": "눈",
      "start_offset": 49,
      "end_offset": 50,
      "type": "N",
      "position": 15
    },
    {
      "token": "가까이",
      "start_offset": 51,
      "end_offset": 54,
      "type": "M",
      "position": 16
    },
    {
      "token": "닦",
      "start_offset": 55,
      "end_offset": 56,
      "type": "V",
      "position": 17
    },
    {
      "token": "닦기에는",
      "start_offset": 55,
      "end_offset": 59,
      "type": "EOJ",
      "position": 17
    },
    {
      "token": "따갑",
      "start_offset": 60,
      "end_offset": 62,
      "type": "V",
      "position": 18
    },
    {
      "token": "따갑네요",
      "start_offset": 60,
      "end_offset": 64,
      "type": "EOJ",
      "position": 18
    }
  ]
}
```

```curl
PUT /reviews
{
  "settings": {
    "index": {
      "similarity": {
        "less_length_norm_BM25": {
          "type": "BM25",
          "b": 0.2
        }
      },
      "max_ngram_diff": 2,
      "analysis": {
        "char_filter": {
          "whitespace_remove": {
            "type": "pattern_replace",
            "pattern": " ",
            "replacement": ""
          }
        },
        "filter": {
          "ngram": {
            "type": "ngram",
            "min_gram": 1,
            "max_gram": 3,
            "token_chars": [
              "letter",
              "digit",
              "punctuation",
              "symbol"
            ]
          },
          "synonyms_filter": {
            "updateable": true,
            "lenient": true,
            "type": "synonym",
            "synonyms": [
              ...
            ]
          },
          "korean_stop": {
            "type": "stop",
            "stopwords": [
              "이",
              "그",
              "저",
              "것",
              "도"
            ]
          },
          "lowercase": {
            "type": "lowercase"
          }
        },
        "tokenizer": {
          "seunjeon_tokenizer": {
            "type": "seunjeon_tokenizer",
            "index_eojeol": true,
            "decompound": true,
            "pos_tagging": false,
            "index_poses": [
              "UNK",
              "EP",
              "I",
              "M",
              "N",
              "SL",
              "SH",
              "SN",
              "V",
              "VCP",
              "XP",
              "XS",
              "XR"
            ]
          }
        },
        "analyzer": {
          "seunjeon": {
            "type": "custom",
            "tokenizer": "seunjeon_tokenizer"
          },
          "seunjeon_search": {
            "tokenizer": "seunjeon_tokenizer",
            "filter": [
              "synonyms_filter",
              "lowercase",
              "trim",
              "korean_stop"
            ]
          },
          "ngram": {
            "char_filter": [
              "whitespace_remove"
            ],
            "tokenizer": "ngram",
            "filter": [
              "lowercase"
            ]
          },
          "keyword": {
            "tokenizer": "keyword",
            "filter": [
              "lowercase"
            ]
          }
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "content": {
        "type": "text",
        "analyzer": "seunjeon",
        "search_analyzer": "seunjeon_search",
        "similarity": "less_length_norm_BM25"
      },
      "userName": {
        "type": "text",
        "analyzer": "seunjeon"
      },
      "cosmeticName": {
        "type": "text",
        "analyzer": "seunjeon"
      },
      "rating": {
        "type": "integer"
      }
    }
  }
}
```

# ELK

Spring boot의 실시간 로그가 Logstash에 스트리밍되어 ElasticSearch에 전달됩니다.

로그 필터링 = Spring Boot REST API 요청 + Spring Boot ERROR

![logstash](https://github.com/LeeZEun/beautyMinder/assets/2356749/ac44a1c8-7f66-4441-8b13-9153eff03a58)