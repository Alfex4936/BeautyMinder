# server.py
import os
import pickle
import re

import tensorflow as tf
import torch
from fastapi import FastAPI
from korcen import korcen
from pydantic import BaseModel, ConfigDict
from sentence_transformers import SentenceTransformer, util
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from tensorflow.keras.preprocessing.sequence import pad_sequences

os.environ["TF_CPP_MIN_LOG_LEVEL"] = "3"  # or any {'0', '1', '2'}
tf.keras.utils.disable_interactive_logging()

app = FastAPI()

# mongo_client = AsyncIOMotorClient("mongodb://localhost:27017")
# db = mongo_client.your_db_name
maxlen = 1000  # 모델마다 값이 다름

model_path = "vdcnn_model_with_kogpt2.h5"
tokenizer_path = "tokenizer_with_kogpt2.pickle"

model = tf.keras.models.load_model(model_path)
with open(tokenizer_path, "rb") as f:
    tokenizer = pickle.load(f)

embedder = SentenceTransformer("./sroberta/")
# embedder.save("sroberta/")
vectorizer = CountVectorizer()

# Baumann skin type descriptions
BAUMANN_DESCRIPTION = {
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


def predict_text(text):
    encoded_sentence = tokenizer.encode_plus(
        text, max_length=maxlen, padding="max_length", truncation=True
    )["input_ids"]
    sentence_seq = pad_sequences([encoded_sentence], maxlen=maxlen, truncating="post")
    prediction = model.predict(sentence_seq)[0][0]
    return prediction


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


# def validate_object_id(v: Any) -> ObjectId:
#     if isinstance(v, ObjectId):
#         return v
#     if ObjectId.is_valid(v):
#         return ObjectId(v)
#     raise ValueError("Invalid ObjectId")


# PyObjectId = Annotated[
#     Union[str, ObjectId],
#     AfterValidator(validate_object_id),
#     PlainSerializer(lambda x: str(x), return_type=str),
#     WithJsonSchema({"type": "string"}, mode="serialization"),
# ]


class ReviewRequest(BaseModel):
    # id: PyObjectId = Field(alias="id")
    content: str

    model_config = ConfigDict(arbitrary_types_allowed=True, populate_by_name=True)
    # arbitary_types_allowed = True

    # @classmethod
    # def __get_pydantic_json_schema__(
    #     cls, core_schema: CoreSchema, handler: GetJsonSchemaHandler
    # ) -> Dict[str, Any]:
    #     json_schema = super().__get_pydantic_json_schema__(core_schema, handler)
    #     json_schema = handler.resolve_ref_schema(json_schema)

    #     # Customize the schema for the 'id' field
    #     id_field = json_schema.get("properties", {}).get("_id", {})
    #     id_field.update(type="string")
    #     return json_schema


def process_review(review):
    # print(f"What is {review}")
    review_text = [review.content]  # array form for nlp work

    # Keyword-based similarity

    baumann_keywords_matrix = vectorizer.fit_transform(
        [" ".join(desc.split()) for desc in BAUMANN_DESCRIPTION.values()]
    )
    review_keywords_matrix = vectorizer.transform(review_text)

    keyword_similarity_scores = cosine_similarity(
        review_keywords_matrix, baumann_keywords_matrix
    )

    # Average embeddings for description
    baumann_sentence_embeddings = [
        embedder.encode(desc.split(". "), convert_to_tensor=True)
        for desc in BAUMANN_DESCRIPTION.values()
    ]
    baumann_avg_embeddings = [
        torch.mean(embedding, dim=0) for embedding in baumann_sentence_embeddings
    ]

    review_embeddings = embedder.encode(review_text, convert_to_tensor=True)

    # Semantic similarity scores
    semantic_similarity_scores = util.pytorch_cos_sim(
        review_embeddings, torch.stack(baumann_avg_embeddings)
    )

    # Combine keyword and semantic similarity scores (adjust weights as necessary)
    combined_similarity_scores = (
        0.1 * keyword_similarity_scores + 0.9 * semantic_similarity_scores.numpy()
    )

    K = 0.5  # threshold

    # Calculate offensiveness probability
    overall_offensiveness_probability = round(
        (korcen.check(review_text[0]) + predict_text(review_text[0])) / 2, 2  # 0.00
    )
    is_filtered = bool(overall_offensiveness_probability > 0.8)

    # Filter similarities above threshold K and create a dictionary with the results
    nlpanalysis = {
        baumann_type: float(f"{score:.2f}") if score > K else float("0.00")
        for baumann_type, score in zip(
            BAUMANN_DESCRIPTION.keys(), combined_similarity_scores[0]
        )
    }

    return {
        "isFiltered": is_filtered,
        "nlpAnalysis": nlpanalysis,
    }


async def update_db():
    update_result = await db.reviews.update_one(
        {"_id": review_request.id},
        {
            "$set": {
                "isFiltered": is_filtered,
                "nlpAnalysis": nlpanalysis,
            }
        },
        upsert=True,
    )


@app.post("/process-review")
async def process_review_endpoint(review_request: ReviewRequest):
    # This is where you'll call your NLP processing function
    nlp_result = process_review(review_request)

    # update the MongoDB model with the NLP results
    # update_db()
    # if update_result.modified_count == 0:
    #     raise HTTPException(status_code=500, detail="Failed to update the review")

    return nlp_result


@app.get("/health")
def health_check():
    return {"status": "ok"}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("__main__:app", host="0.0.0.0", port=9000, reload=True)
