import uuid
from locust import HttpUser, task, between


class CoreBankingUser(HttpUser):
    wait_time = between(1, 5)

    @task
    def create_account(self):
        account_data = {
            "customerId": 1,
            "reference": str(uuid.uuid4()),
            "country": "USA",
            "balances": [
                {
                    "currency": "USD",
                    "availableAmount": 1000
                }
            ]
        }
        self.client.post("/api/accounts", json=account_data)

    @task
    def get_account(self):
        account_id = 1
        self.client.get(f"/api/accounts/{account_id}")

    @task
    def create_transaction(self):
        transaction_data = {
            "accountId": 1,
            "amount": 100,
            "currency": "USD",
            "direction": "IN",
            "description": "Load test transaction"
        }
        self.client.post("/api/transactions", json=transaction_data)

    @task
    def get_transactions(self):
        account_id = 1
        self.client.get(f"/api/transactions/{account_id}")
