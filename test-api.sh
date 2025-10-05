#!/bin/bash

# Quiz Application API Test Script
# Make sure the Spring Boot application is running on localhost:8080

BASE_URL="http://localhost:8080/api"

echo "🧪 Testing Quiz Application API Endpoints"
echo "=========================================="

# Test 1: Register a new user
echo "1. Testing user registration..."
REGISTER_RESPONSE=$(curl -s -X POST $BASE_URL/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}')

echo "Register Response: $REGISTER_RESPONSE"

# Extract user ID from response (assuming it returns JSON with id field)
USER_ID=$(echo $REGISTER_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
echo "User ID: $USER_ID"

echo ""

# Test 2: Login user
echo "2. Testing user login..."
LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}')

echo "Login Response: $LOGIN_RESPONSE"
echo ""

# Test 3: Generate a question (this will fail without proper Gemini API key)
echo "3. Testing question generation..."
QUESTION_RESPONSE=$(curl -s -X POST $BASE_URL/quiz/generate \
  -H "Content-Type: application/json" \
  -d '{"category": "Science"}')

echo "Question Response: $QUESTION_RESPONSE"
echo ""

# Test 4: Save a streak
echo "4. Testing streak save..."
STREAK_RESPONSE=$(curl -s -X POST $BASE_URL/streaks/save \
  -H "Content-Type: application/json" \
  -d "{\"userId\": $USER_ID, \"streakCount\": 5, \"category\": \"Science\"}")

echo "Streak Response: $STREAK_RESPONSE"
echo ""

# Test 5: Get user streak history
echo "5. Testing user streak history..."
HISTORY_RESPONSE=$(curl -s -X GET $BASE_URL/streaks/user/$USER_ID)

echo "History Response: $HISTORY_RESPONSE"
echo ""

# Test 6: Get leaderboard
echo "6. Testing leaderboard..."
LEADERBOARD_RESPONSE=$(curl -s -X GET $BASE_URL/streaks/leaderboard?limit=5)

echo "Leaderboard Response: $LEADERBOARD_RESPONSE"
echo ""

# Test 7: Get highest streak
echo "7. Testing highest streak..."
HIGHEST_RESPONSE=$(curl -s -X GET $BASE_URL/streaks/highest/$USER_ID)

echo "Highest Streak Response: $HIGHEST_RESPONSE"
echo ""

echo "✅ API Testing Complete!"
echo "Note: Question generation will fail without a valid Gemini API key."
echo "Make sure to update application.properties with your API key."
