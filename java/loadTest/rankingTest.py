from locust import HttpUser, task, between, events
import random
import numpy as np


keywords = [
    "스킨케어",  # Skincare
    "메이크업",  # Makeup
    "페이스 마스크",  # Face Mask
    "썬크림",  # Sunscreen
    "립스틱",  # Lipstick
    "아이섀도우",  # Eyeshadow
    "블러셔",  # Blusher
    "페이스 파우더",  # Face Powder
    "클렌징",  # Cleansing
    "토너",  # Toner
    "모이스처라이저",  # Moisturizer
    "에센스",  # Essence
    "세럼",  # Serum
    "아이라이너",  # Eyeliner
    "마스카라",  # Mascara
    "콘실러",  # Concealer
    "프라이머",  # Primer
    "네일 폴리시",  # Nail Polish
    "화장품 세트",  # Cosmetic Set
    "향수"  # Perfume
]

# Generate random numbers
random_numbers = np.random.rand(len(keywords))

# Normalize to ensure the sum of probabilities equals 1
keyword_probabilities = random_numbers / np.sum(random_numbers)

class MyUser(HttpUser):
    wait_time = between(5, 10)  # Wait time between requests in seconds

    keywords = keywords
    keyword_probabilities = keyword_probabilities
    keyword_counts = {keyword: 0 for keyword in keywords}

    def weighted_choice(self):
        keyword = np.random.choice(self.keywords, p=self.keyword_probabilities)
        self.keyword_counts[keyword] += 1
        return keyword

    @task
    def search(self):
        keyword = self.weighted_choice()
        self.client.get(f"/search?anything={keyword}")

def print_keyword_counts(environment, **kwargs):
    for keyword, count in MyUser.keyword_counts.items():
        print(f'{keyword}: {count}')

# Hook to print keyword counts when test stops
events.test_stop.add_listener(print_keyword_counts)


if __name__ == "__main__":
    import os
    os.system("locust -f rankingTest.py --headless -u 100 -r 20 --run-time 10m --host http://localhost:8080")
