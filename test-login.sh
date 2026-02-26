#!/bin/bash
# 测试登录自动注册功能

echo "=== 测试登录自动注册功能 ==="

# 测试新用户注册
echo "1. 测试新用户 'chen' 注册..."
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"chen","password":"123"}'

echo -e "\n\n2. 测试已有用户登录..."
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"chen","password":"123"}'

echo -e "\n\n3. 测试另一个新用户 'testuser' 注册..."
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

echo -e "\n\n=== 测试完成 ==="