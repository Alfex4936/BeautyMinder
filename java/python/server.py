# server.py
import json
import logging
import os
import pickle
import re
import time

import tensorflow as tf
from fastapi import FastAPI
from korcen import korcen
from openai import OpenAI
from pydantic import BaseModel, ConfigDict
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

client = OpenAI(
    api_key=os.environ.get("OPENAI_API_KEY"),
)

SYSTEM_ROLE = os.environ.get("SYSTEM_ROLE")


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


class ReviewRequest(BaseModel):
    # id: PyObjectId = Field(alias="id")
    content: str

    model_config = ConfigDict(arbitrary_types_allowed=True, populate_by_name=True)


def wait_for_run_completion(client, thread_id, run_id, sleep_interval=5):
    """
    Waits for a run to complete and prints the elapsed time.:param client: The OpenAI client object.
    :param thread_id: The ID of the thread.
    :param run_id: The ID of the run.
    :param sleep_interval: Time in seconds to wait between checks.
    """
    while True:
        try:
            run = client.beta.threads.runs.retrieve(thread_id=thread_id, run_id=run_id)
            if run.completed_at:
                elapsed_time = run.completed_at - run.created_at
                formatted_elapsed_time = time.strftime(
                    "%H:%M:%S", time.gmtime(elapsed_time)
                )
                logging.info(f"Run completed in {formatted_elapsed_time}")
                break
        except Exception as e:
            logging.error(f"An error occurred while retrieving the run: {e}")
            break
        logging.info("Waiting for run to complete...")
        time.sleep(sleep_interval)


def assistant():
    message = client.beta.threads.messages.create(
        thread_id=thread.id, role="user", content=review_text
    )
    run = client.beta.threads.runs.create(
        thread_id=thread.id,
        assistant_id="asst_YVHnRUKNjbo1mODGiCEuNIPX",
    )
    wait_for_run_completion(client, thread.id, run.id)
    messages = client.beta.threads.messages.list(thread_id=thread.id)

    last_message = messages.data[0]
    response = last_message.content[0].text.value


def process_review(review):
    review_text = review.content

    # Calculate offensiveness probability
    overall_offensiveness_probability = round(
        (korcen.check(review_text) + predict_text(review_text)) / 2, 2  # 0.00
    )
    is_filtered = bool(overall_offensiveness_probability >= 0.8)

    logging.info(f"FASTAPI: dealing with {review_text[:12]}")
    # Filter similarities above threshold K and create a dictionary with the results
    response = client.chat.completions.create(
        response_format={"type": "json_object"},
        model="gpt-3.5-turbo-1106",
        messages=[
            {
                "role": "system",
                "content": SYSTEM_ROLE,
            },
            {
                "role": "user",
                "content": clean_text(review_text),  # less tokens I hope
            },
        ],
        temperature=1,
        max_tokens=128,
        top_p=1,
        frequency_penalty=0,
        presence_penalty=0,
    )
    logging.info(f"FASTAPI: done with {review_text[:12]}")

    nlpanalysis = json.loads(response.choices[0].message.content)

    return {
        "isFiltered": is_filtered,
        "nlpAnalysis": nlpanalysis["nlpAnalysis"],
    }


@app.post("/process-review")
async def process_review_endpoint(review_request: ReviewRequest):
    nlp_result = process_review(review_request)
    return nlp_result


@app.get("/health")
def health_check():
    return {"status": "ok"}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("__main__:app", host="0.0.0.0", port=9000, reload=True)
