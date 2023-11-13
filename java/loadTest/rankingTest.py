from locust import HttpUser, task, between, events
import random
import numpy as np
import logging

keywords = [
    "메이크업",  # Makeup
    "페이스마스크",  # Face Mask
    "썬크림",  # Sunscreen
    "립스틱",  # Lipstick
    "아이섀도우",  # Eyeshadow
    "블러셔",  # Blusher
    "페이스파우더",  # Face Powder
    "토너",  # Toner
    "비타민",  # Vitamin
    "에센스",  # Essence
    "세럼",  # Serum
    "아이라이너",  # Eyeliner
    "마스카라",  # Mascara
    "콘실러",  # Concealer
    "프라이머",  # Primer
    "네일",  # Nail Polish
    "세트",  # Cosmetic Set
    "향수"  # Perfume
]

# Generate random numbers
random_numbers = np.random.rand(len(keywords))

# Normalize to ensure the sum of probabilities equals 1
keyword_probabilities = random_numbers / np.sum(random_numbers)

class MyUser(HttpUser):
    wait_time = between(5, 10)  # Wait time between requests in seconds

    # Define your keyword search counts here
    keyword_search_counts = {
        "스킨케어": 100,
        "메이크업": 300,
        "페이스 마스크": 200,
        "에센스": 10,
        "세럼": 30,
        "아이라이너": 40,
        "마스카라": 1,
        "콘실러": 400,
        "프라이머": 150,
        "네일 폴리시": 210,
        "화장품 세트": 151,
        "향수": 149,
    }

    keyword_search_counts2 = {
        "모이스처라이저": 500,
        "메이크업": 5,
        "페이스 마스크": 200,
        "마스카라": 200,
    }

    keywords = keywords
    keyword_probabilities = keyword_probabilities
    keyword_counts = {keyword: 0 for keyword in keywords}

    def weighted_choice(self):
        keyword = np.random.choice(self.keywords, p=self.keyword_probabilities)
        self.keyword_counts[keyword] += 1
        return keyword

    def search_keyword(self, keyword):
        if self.keyword_counts[keyword] < self.keyword_search_counts[keyword]:
            self.client.get(f"/search/test?anything={keyword}")
            self.keyword_counts[keyword] += 1

    @task
    def search(self):
        keyword = self.weighted_choice()
        self.client.get(f"/search/test?anything={keyword}")

    # @task
    def search_n(self):
        # Go through each keyword and perform the search the specified number of times
        for keyword in self.keyword_search_counts.keys():
            self.search_keyword(keyword)

def print_keyword_counts(environment, **kwargs):
    for keyword, count in MyUser.keyword_counts.items():
        logging.info(f'{keyword}: {count}')
    with open('keyword_counts.txt', 'w') as file:
        for keyword, count in MyUser.keyword_counts.items():
            file.write(f'{keyword}: {count}\n')

# Hook to print keyword counts when test stops
events.test_stop.add_listener(print_keyword_counts)


if __name__ == "__main__":
    import os
    os.system("locust -f rankingTest.py --headless -u 100 -r 20 --run-time 30m --host http://localhost:8080")

