from locust import HttpUser, between, task, tag

class KeywordSearcher(HttpUser):
    wait_time = between(1, 2)

    @task(50)
    def search_average(self):
        # Replace 'keyword' with the actual query parameter name your API expects
        self.client.get("/search?anything=average_keyword")

    @tag('peak')
    @task(5)
    def search_peak(self):
        # This task is weighted more to simulate peak volume
        self.client.get("/search?anything=peak_keyword")

    @task(10)
    def search_control(self):
        # This task has a lower weight to represent a control group with average search levels
        self.client.get("/search?anything=control_keyword")

if __name__ == "__main__":
    import os
    os.system("locust -f significantTest.py -u 20 -r 20 --headless --host http://localhost:8080")
