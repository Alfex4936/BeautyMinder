from locust import HttpUser, task, between, events
import random
import numpy as np
import os
import logging
from dotenv import load_dotenv
from pymongo import MongoClient

load_dotenv()

# Connect to MongoDB
client = MongoClient(os.environ.get("MONGODB_URI"))
db = client[os.environ.get("DB_NAME")]
cosmetics_collection = db[os.environ.get("COL_NAME")]

# Retrieve all cosmetic IDs
cosmetic_ids = cosmetics_collection.find({}, {"_id": 1})
cosmetic_ids_list = [str(cosmetic["_id"]) for cosmetic in cosmetic_ids]

class MyUser(HttpUser):
    wait_time = between(5, 10)  # Wait time between requests in seconds

    @task
    def hit_or_click_cosmetic(self):
        cosmetic_id = random.choice(cosmetic_ids_list)
        action = random.choice(["hit", "click"])
        self.client.post(f"/cosmetic/{action}/{cosmetic_id}")


if __name__ == "__main__":
    import os
    os.system("locust -f clickTest.py --headless -u 100 -r 20 --run-time 30m --host http://localhost:8080")

