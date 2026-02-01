curl -X POST \
        -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsInNpZ25fdHlwZSI6IlNJR04ifQ.eyJhcGlfa2V5IjoiZmViYzI3ODk4MDNhNDNiNDhhZTRkMDJmN2I4MzQyZDgiLCJleHAiOjE3Njk5NTY0MzA4MjEsInRpbWVzdGFtcCI6MTc2OTk1NDYzMDgyNH0.tVZUEiSEZNS9GWQvBZ-7dlPwRznG3ZebWAmWPfUpX4U" \
        -H "Content-Type: application/json" \
        -H "User-Agent: Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)" \
        -d '{
          "model":"glm-4",
          "stream": "true",
          "messages": [
              {
                  "role": "user",
                  "content": "你好，你是哪个模型"
              }
          ]
        }' \
  https://open.bigmodel.cn/api/paas/v4/chat/completions
  