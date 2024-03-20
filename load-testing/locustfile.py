import random
import uuid
from locust import HttpUser, task, between


class CoreBankingUser(HttpUser):
    wait_time = between(1, 5)
    account_ids = []

    @task(10)  # 0.1% chance of creating an account
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
        response = self.client.post("/api/accounts", json=account_data)
        if response.status_code == 200:
            account_id = response.json()["accountId"]
            self.account_ids.append(account_id)

    @task(4795)  # 47.95% chance of getting an account
    def get_account(self):
        if self.account_ids:
            account_id = random.choice(self.account_ids)
            self.client.get(f"/api/accounts/{account_id}")

    @task(400)  # 4% chance of creating a transaction
    def create_transaction(self):
        if self.account_ids:
            account_id = random.choice(self.account_ids)
            transaction_data = {
                "accountId": account_id,
                "amount": 100,
                "currency": "USD",
                "direction": "IN",
                "description": "Load test transaction"
            }
            self.client.post("/api/transactions", json=transaction_data)

    @task(4795)  # 47.95% chance of getting transactions
    def get_transactions(self):
        if self.account_ids:
            account_id = random.choice(self.account_ids)
            self.client.get(f"/api/transactions/{account_id}")
