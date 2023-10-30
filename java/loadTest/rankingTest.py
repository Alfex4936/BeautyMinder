from locust import HttpUser, task, between
import random

class MyUser(HttpUser):
    wait_time = between(5, 10)  # Wait time between requests in seconds

    keywords = ['스킨케어', '비타민', '독도', '바나나', '키위', '사과', '레몬']

    @task
    def search(self):
        keyword = random.choice(self.keywords)
        self.client.get(f"/search?anything={keyword}")

# Optionally, you can specify command-line options in your script like so:
if __name__ == "__main__":
    import os
    os.system("locust -f rankingTest.py --headless -u 100 -r 20 --run-time 10m --host http://localhost:8080")
