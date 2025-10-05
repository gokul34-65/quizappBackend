# Quiz Application - Spring Boot Backend

A full-stack quiz application where users answer AI-generated questions, track their streaks, and compete on leaderboards. Questions are generated in real-time using Google's Gemini API.

## 🚀 Features

- **User Authentication**: Secure registration and login with BCrypt password hashing
- **AI Question Generation**: Real-time questions using Google Gemini Pro API
- **Streak Tracking**: Persistent storage of user performance
- **Leaderboards**: Global rankings by highest streak
- **Game History**: View all past games for each user
- **Category-Based Quizzes**: 8 different categories (Science, History, Sports, etc.)
- **RESTful API**: Clean, standard API design with proper error handling

## 🛠️ Tech Stack

- **Backend**: Spring Boot 3.5.6, Java 17, Maven
- **Database**: PostgreSQL with JPA/Hibernate
- **Security**: Spring Security Crypto (BCrypt)
- **API Integration**: Google Gemini Pro API
- **HTTP Client**: WebClient (WebFlux)

## 📋 Prerequisites

Before running this application, make sure you have:

1. **Java 17** or higher
2. **Maven 3.6+**
3. **PostgreSQL 12+** running on your system
4. **Google Gemini API Key** (get it from [Google AI Studio](https://makersuite.google.com/app/apikey))

## 🗄️ Database Setup

1. Create a PostgreSQL database:
```sql
CREATE DATABASE quizdb;
```

2. The application will automatically create the required tables on first run using JPA/Hibernate.

## ⚙️ Configuration

1. **Clone and navigate to the project directory:**
```bash
cd quiz-app
```

2. **Update `src/main/resources/application.properties`:**
```properties
# Update these values according to your setup
spring.datasource.password=your_postgresql_password
gemini.api.key=your_gemini_api_key_here
```

3. **Install dependencies and run:**
```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login user

### Quiz
- `POST /api/quiz/generate` - Generate AI question for a category

### Streaks
- `POST /api/streaks/save` - Save game streak
- `GET /api/streaks/user/{userId}` - Get user's game history
- `GET /api/streaks/leaderboard?limit=10` - Get top players
- `GET /api/streaks/highest/{userId}` - Get user's best score

## 🔧 Example API Usage

### Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}'
```

### Login User
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}'
```

### Generate Question
```bash
curl -X POST http://localhost:8080/api/quiz/generate \
  -H "Content-Type: application/json" \
  -d '{"category": "Science"}'
```

### Save Streak
```bash
curl -X POST http://localhost:8080/api/streaks/save \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "streakCount": 15, "category": "Science"}'
```

## 🏗️ Project Structure

```
src/main/java/com/saanya/quiz_app/
├── config/
│   └── CorsConfig.java          # CORS and bean configuration
├── controller/
│   ├── AuthController.java      # Authentication endpoints
│   ├── QuizController.java      # Quiz generation endpoints
│   └── StreakController.java    # Streak management endpoints
├── dto/
│   ├── LoginRequest.java        # Login input DTO
│   ├── RegisterRequest.java     # Registration input DTO
│   ├── UserResponse.java        # User data response DTO
│   ├── QuizRequest.java         # Quiz generation request DTO
│   ├── StreakRequest.java       # Streak save request DTO
│   ├── StreakResponse.java      # Streak save response DTO
│   ├── LeaderboardEntry.java    # Leaderboard data DTO
│   └── StreakHistory.java       # Game history DTO
├── exception/
│   └── GlobalExceptionHandler.java # Global error handling
├── model/
│   ├── User.java                # User entity
│   ├── Streak.java              # Streak entity
│   └── Question.java            # Question DTO
├── repository/
│   ├── UserRepository.java      # User data access
│   └── StreakRepository.java    # Streak data access
├── service/
│   ├── AuthService.java         # Authentication logic
│   ├── GeminiService.java       # AI question generation
│   └── StreakService.java       # Streak management logic
└── QuizAppApplication.java      # Main application class
```

## 🔒 Security Features

- **Password Hashing**: BCrypt with salt
- **Input Validation**: Automatic validation of all inputs
- **CORS Protection**: Configured for frontend communication
- **Error Handling**: Graceful error responses without exposing internals

## 🚨 Important Notes

1. **API Key Security**: Never commit your Gemini API key to version control
2. **Database Password**: Update the PostgreSQL password in `application.properties`
3. **CORS**: Backend is configured to allow requests from `http://localhost:3000` (React frontend)
4. **Database Tables**: Tables are auto-created on first run (`ddl-auto=update`)

## 🐛 Troubleshooting

### Common Issues:

1. **Database Connection Error**: 
   - Ensure PostgreSQL is running
   - Check database credentials in `application.properties`
   - Verify database `quizdb` exists

2. **Gemini API Error**:
   - Verify API key is correct
   - Check internet connection
   - Ensure API key has proper permissions

3. **Port Already in Use**:
   - Change `server.port` in `application.properties`
   - Or kill the process using port 8080

## 🔄 Next Steps

To complete the full-stack application:

1. **Frontend Setup**: Create a React.js frontend that consumes these APIs
2. **Deployment**: Deploy to cloud platforms like AWS, Heroku, or DigitalOcean
3. **Testing**: Add unit and integration tests
4. **Monitoring**: Add logging and monitoring capabilities

## 📄 License

This project is for educational purposes. Feel free to use and modify as needed.

---

**Happy Coding! 🎉**
