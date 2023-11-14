import os
import re

import numpy as np
import pymongo
import torch
from dotenv import load_dotenv
from sentence_transformers import SentenceTransformer, util
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.metrics.pairwise import cosine_similarity


def clean_text(text):
    # This regex pattern keeps
    # Hangul characters, Latin alphabets, spaces, and the punctuation marks "!" and "?"
    clean_text_pattern = re.compile(
        "[^"
        "\uAC00-\uD7A3"  # Hangul characters
        "a-zA-Z"  # Latin alphabets
        " !?"  # Spaces and the punctuation marks "!" and "?"
        "]+"
    )
    return clean_text_pattern.sub(r"", text)


def fetch_reviews_from_mongodb():
    load_dotenv()

    client = pymongo.MongoClient(os.getenv("MONGODB_URI"))
    db = client[os.getenv("DB_NAME")]  # type: ignore
    reviews_collection = db[os.getenv("COL_NAME")]  # type: ignore
    review_texts = [
        clean_text(review["content"].strip()) for review in reviews_collection.find()
    ]
    return review_texts


def main():
    # Baumann skin type descriptions
    baumann_descriptions = {
        "O": (
            "이 타입의 피부는 유분이 많아서 기름진 느낌을 주고, 지성 피부에 해당한다. "
            "화장품이나 기초 화장품을 선택할 때 오일 프리 제품을 선호하며, 유분기가 많아 블랙헤드나 화이트헤드가 잘 생기고, 모공이 크게 느껴질 수 있다. "
            "지성 피부를 위한 제품은 모공을 축소시키고 피부의 유분을 조절하는데 도움이 된다. "
            "또한, 이 타입의 피부는 화장이 잘 무너지고 피부가 번들거리는 경향이 있다."
        ),
        "D": (
            "이 타입의 피부는 건조하며 수분이 부족해서 피부가 갈라질 수 있다. "
            "수분크림이나 보습제를 사용하여 피부의 수분을 유지하는 것이 중요하며, 피부가 건조해서 갈라짐과 가려움이 발생할 수 있다. "
            "건조한 피부를 위한 제품은 피부의 수분을 재충전하고 보호막을 형성하여 수분을 유지하는데 도움이 된다. "
            "이 타입의 피부는 환경 변화에 따라 피부 상태가 쉽게 변할 수 있다."
        ),
        "R": (
            "이 타입의 피부는 붉은 기를 가지고 있어 자주 발적하거나 민감하게 반응할 수 있다. "
            "순한 성분의 화장품을 사용하여 피부를 진정시키는 것이 좋으며, 피부가 붉고 발열이 있을 때 특정 제품이나 환경 변화에 민감하게 반응할 수 있다. "
            "붉은 피부를 위한 제품은 피부를 진정시키고 빨간 기를 줄이는데 도움이 된다. "
            "이 타입의 피부는 또한 염증과 함께 트러블이 발생할 수 있으며, 자외선에 민감할 수 있다."
        ),
        "S": (
            "이 타입의 피부는 민감하며 자극에 쉽게 반응해서 빨갛게 트러블이 잘 생긴다. "
            "민감한 피부를 가진 사람들은 자극적인 성분을 피하며, 피부가 가렵거나 따가울 때 순한 성분의 제품을 사용하는 것이 좋다. "
            "민감한 피부를 위한 제품은 피부를 진정시키고 자극을 줄이는데 도움이 된다. "
            "이 타입의 피부는 피부 보호 막이 약할 수 있어 외부 환경으로부터의 보호가 필요하다."
        ),
        "P": (
            "이 타입의 피부는 색소침착이나 잡티가 더 잘 발생하며, 피부 톤이 불균일할 수 있다. "
            "밝은 피부 톤을 위해 피부 미백 제품을 사용하며, 잡티, 색소침착, 피부 톤의 불균일함이 문제가 될 수 있다. "
            "색소침착 피부를 위한 제품은 피부 미백과 톤 개선에 도움이 된다. "
            "이 타입의 피부는 피부 노화와 함께 색소 침착이 더욱 진해질 수 있다."
        ),
        "N": (
            "이 타입의 피부는 색소침착이 적고 잡티가 거의 없어 비색소 피부에 해당한다. "
            "이러한 피부 타입은 자연스러운 미백 제품을 사용하여 피부 톤을 유지할 수 있으며, 피부가 밝고 색소침착이 거의 없어 깨끗한 피부 톤을 가지고 있다. "
            "이 타입의 피부는 화장품 선택이 다양하며, 피부가 깨끗하고 투명한 느낌을 준다."
        ),
        "T": (
            "이 타입의 피부는 탄력이 있고 주름이 적으며 타이트하고 탄탄한 느낌을 준다. "
            "탄력 유지를 위해 콜라겐 크림이나 세럼을 사용할 수 있으며, 피부가 탄탄하고 주름이나 느슨함이 적어 탄력이 뛰어나다. "
            "탄력 있는 피부를 위한 제품은 피부의 탄력을 개선하고 주름을 줄이는 데 도움이 된다. "
            "이 타입의 피부는 피부 노화와 함께 탄력 손실이 더욱 진해질 수 있다."
        ),
        "W": (
            "이 타입의 피부는 수분과 유분이 잘 균형을 이루고 있어 피부 텍스처가 부드럽고 매끈하다. "
            "이 타입의 피부는 다양한 화장품을 쉽게 시도할 수 있으며, 피부가 부드럽고 유연하며 유분과 수분의 균형이 잘 맞춰져 있다. "
            "잘 균형잡힌 피부를 위한 제품은 피부의 수분과 유분 균형을 유지하고 텍스처를 개선하는 데 도움이 된다. "
            "이 타입의 피부는 환경 변화에 따라 유분과 수분의 균형이 쉽게 깨질 수 있다."
        ),
    }

    review_texts = fetch_reviews_from_mongodb()
    # review_texts = [
    #     "믿고쓰는 에스트라 여름이라 외출하고 오면 피부가 붉어지고 엄청 예민해지는데 이제품 사용하고 편안해져 전 진짜 좋았어요 향도 강하지 않고 수분가득 촉촉촉 예민한 피부로 토너 찾으시는분 이제품 추천드리고싶어요 신랑과 같이 쓰는데 진짜 만족하고 있어요"
    # ]

    embedder = SentenceTransformer("jhgan/ko-sroberta-multitask")

    # Keyword-based similarity
    vectorizer = CountVectorizer()
    baumann_keywords_matrix = vectorizer.fit_transform(
        [" ".join(desc.split()) for desc in baumann_descriptions.values()]
    )
    review_keywords_matrix = vectorizer.transform(review_texts)

    keyword_similarity_scores = cosine_similarity(
        review_keywords_matrix, baumann_keywords_matrix
    )

    # Average embeddings for description
    baumann_sentence_embeddings = [
        embedder.encode(desc.split(". "), convert_to_tensor=True)
        for desc in baumann_descriptions.values()
    ]
    baumann_avg_embeddings = [
        torch.mean(embedding, dim=0) for embedding in baumann_sentence_embeddings
    ]

    review_embeddings = embedder.encode(review_texts, convert_to_tensor=True)

    # Semantic similarity scores
    semantic_similarity_scores = util.pytorch_cos_sim(
        review_embeddings, torch.stack(baumann_avg_embeddings)
    )

    # Combine keyword and semantic similarity scores (adjust weights as necessary)
    combined_similarity_scores = (
        0.1 * keyword_similarity_scores + 0.9 * semantic_similarity_scores.numpy()
    )

    K = 0.5  # threshold

    # Output similarity scores for each review and Baumann skin type
    for idx, review_text in enumerate(review_texts):
        # Filter similarities above threshold K
        similarities_above_threshold = [
            f"{baumann_type}>{score:.2f}"
            for baumann_type, score in zip(
                baumann_descriptions.keys(), combined_similarity_scores[idx]
            )
            if score > K
        ]
        # Join the filtered similarities into a single string
        similarities_str = " , ".join(similarities_above_threshold)
        # Print the review text and the similarities string
        print(
            f"\nReview: {review_text}\nSimilarities: {similarities_str if similarities_str else 'None above threshold'}"
        )


def test():
    # Loop through different weight combinations for keyword and semantic similarity scores
    for keyword_weight in np.arange(
        0.1, 1.0, 0.1
    ):  # Vary keyword weight from 0.1 to 0.9 in steps of 0.1
        semantic_weight = 1 - keyword_weight  # Ensure the weights sum to 1

        # Loop through different threshold values
        for K in np.arange(
            0.1, 1.0, 0.1
        ):  # Vary threshold from 0.1 to 0.9 in steps of 0.1
            # Combine keyword and semantic similarity scores based on the current weights
            combined_similarity_scores = (
                keyword_weight * keyword_similarity_scores
                + semantic_weight * semantic_similarity_scores.numpy()
            )

            # Output similarity scores for each review and Baumann skin type
            for idx, review_text in enumerate(review_texts):
                # Filter similarities above threshold K
                similarities_above_threshold = [
                    f"{baumann_type} = {score:.2f}"
                    for baumann_type, score in zip(
                        baumann_descriptions.keys(), combined_similarity_scores[idx]
                    )
                    if score > K
                    and baumann_type != "O"
                    and baumann_type != "N"
                    and baumann_type != "P"
                    and baumann_type != "T"
                ]
                # Join the filtered similarities into a single string
                similarities_str = " , ".join(similarities_above_threshold)
                if similarities_str:
                    print(
                        f"\nKeyword Weight: {keyword_weight}, Semantic Weight: {semantic_weight}, Threshold: {K}\n"
                    )
                    # Print the review text and the similarities string
                    print(
                        f"Review: {review_text[:6]}...\nSimilarities: {similarities_str}\n"
                    )


def origin():
    # Combine keyword and semantic similarity scores (adjust weights as necessary)
    combined_similarity_scores = (
        0.6 * keyword_similarity_scores + 0.4 * semantic_similarity_scores.numpy()
    )

    # Output similarity scores for each review and Baumann skin type
    for idx, review_text in enumerate(review_texts):
        # Filter similarities above threshold K
        similarities_above_threshold = [
            f"{baumann_type} = {score}"
            for baumann_type, score in zip(
                baumann_descriptions.keys(), combined_similarity_scores[idx]
            )
            # if score > K
        ]
        # Join the filtered similarities into a single string
        similarities_str = " , ".join(similarities_above_threshold)
        # Print the review text and the similarities string
        print(
            f"\nReview: {review_text}\nSimilarities: {similarities_str if similarities_str else 'None above threshold'}"
        )


if __name__ == "__main__":
    main()
