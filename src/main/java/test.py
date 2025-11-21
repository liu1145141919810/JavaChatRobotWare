import requests

api_key = "d96c7eea-0c30-44dc-8b0f-8d96c4d0fcd8"
model = "doubao-seed-1.6-250615"  # 注意模型名称是否需要严格匹配（如大小写、空格等）
url = "https://ark.cn-beijing.volces.com/api/v3/chat/completions"  # 如 https://api.example.com/v1/chat/completions

headers = {
    "Authorization": f"Bearer {api_key}",
    "Content-Type": "application/json"
}
data = {
    "model": model,
    "messages": [{"role": "user", "content": "介绍本大模型"}]
}

response = requests.post(url, headers=headers, json=data)
print(response.json())