# Baumann Skin Type Analysis from Reviews

Python v3.13 NLP

> using `SentenceTransformer("jhgan/ko-sroberta-multitask")`

This project consists of a text analysis pipeline designed to process user reviews and classify them based on Baumann Skin Types.

a categorization of skin types based on 8 characteristics:

```
O (Oily), D (Dry), S (Sensitive), R (Resistant)
P (Pigment), N (Non-Pigment), W (Wrinkle), T (Tight)
```

This project attempts to deduce skin type classifications from user reviews using both keyword-based and semantic similarity measures.

## Sample example

`TYPE>K` indicates a potential match for that skin type based on both semantic and keyword analysis conducted on the review.

```python
Review = {review_text}
    - Similarities: {Baumann_type}>{score} , {Baumann_type}>{score} , ...


Review = """
 앞뒷면 똑같이 매끄럽고 잘 늘어나고 팩하기에 너무 좋네요! 닦토로도 나쁘지 않아요 계속 각질제거 패드만 쓰다가 써보니 피부에 닿는 느낌이 부드럽고 자극이 없어서 편안해요! 촉촉하고 진정효과도 좋아서 이거 쓴 뒤로 붉은기 없어요다만 뚜껑 안쪽에 있는 집게 홀더는 거의 필요 없는 수준이네요  처음에 몇 번껴놓고 닫으면 다시 패드 위에 얹어져있어서 그냥 꺼내놓고 써요 여튼 글로우픽 리뷰가 좋길래 사봤는데 만족합니다!
 """
    - Similarities: N>0.54 , W>0.56

Review = """
 이전 잇츠스킨 감초패드보다 닦는 용도로는 좋으나 붙이는 용도로는 패드 밀착력이 떨어진다 감초패드만의 매력이 떨어지는 제품이랄까 ? 잇츠스킨 패드만의 매력은 젤리패드에서 더 느껴지는 듯패드는 다른 제품들에 비해 좀 부드럽고 얇음스킨케어 첫단계에 닦토하기에 좋음에센스는 적당히 촉촉하고 끈적이지 않고 흡수가 잘 되는 타입이라 마음에 든다
 """
    - Similarities: S>0.50 , W>0.54
```