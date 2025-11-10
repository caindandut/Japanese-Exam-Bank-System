# Japanese Exam Question Bank Manager

A comprehensive desktop application for managing Japanese Language Proficiency Test (JLPT) exam questions and generating practice tests. Built with Java Swing and MySQL.

## 📋 Features

- **Question Management**: Create, edit, and organize Japanese language exam questions with multiple-choice answers
- **Level Classification**: Support for all JLPT levels (N5, N4, N3, N2, N1)
- **Question Types**: Categorize questions by type (Vocabulary, Grammar, Reading, Listening, etc.)
- **Audio Support**: Attach audio files for listening comprehension questions
- **Exam Generation**: Create custom exams by selecting questions from the database
- **Export Functionality**: Export exams to PDF format with proper Japanese character rendering
- **Database-Driven**: Persistent storage using MySQL database

## 🛠️ Technology Stack

- **Language**: Java
- **GUI Framework**: Java Swing (Nimbus Look and Feel)
- **Database**: MySQL
- **Libraries**:
  - MySQL Connector (9.3.0)
  - Apache POI (5.4.1) - Excel document handling
  - Apache PDFBox (2.0.34) - PDF generation
  - JLayer (1.0.1) - Audio playback
  - Commons Libraries (IO, Codec, Compress, Collections)

## 📂 Project Structure

```
NganHangDeThiTiengNhat/
├── src/                          # Source code
│   └── com/exammanager/nganhangdethi/
│       ├── dao/                  # Data Access Objects
│       ├── gui/                  # GUI components
│       ├── main/                 # Main application entry point
│       ├── model/                # Data models
│       ├── service/              # Business logic layer
│       └── utils/                # Utility classes
├── database/                     # Database schema and SQL scripts
├── lib/                          # External JAR dependencies
├── resources/                    # Application resources
│   └── font/                     # Japanese font files
├── data/                         # Application data
│   └── audio/                    # Audio files for listening questions
├── bin/                          # Compiled classes
└── README.md
```

## 🚀 Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- MySQL Server 5.7 or higher
- IDE (Eclipse, IntelliJ IDEA, or NetBeans recommended)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/NganHangDeThiTiengNhat.git
   cd NganHangDeThiTiengNhat
   ```

2. **Set up the database**
   - Start your MySQL server
   - Import the database schema:
     ```bash
     mysql -u root -p < database/QuanLyDeThi.sql
     ```
   - Update database connection settings in `src/com/exammanager/nganhangdethi/dao/DatabaseConnector.java`

3. **Configure classpath**
   - Add all JAR files from the `lib/` directory to your project's classpath

4. **Run the application**
   - Compile and run `src/com/exammanager/nganhangdethi/main/App.java`
   - Or use your IDE's run configuration

### Database Configuration

Update the database connection parameters in `DatabaseConnector.java`:

```java
private static final String URL = "jdbc:mysql://localhost:3306/nganhangdethi";
private static final String USER = "your_username";
private static final String PASSWORD = "your_password";
```

## 💻 Usage

### Main Features

1. **Question Management**
   - Add new questions with multiple-choice answers
   - Mark correct answers
   - Attach audio files for listening questions
   - Edit or delete existing questions

2. **Level Management**
   - Organize questions by JLPT level (N5 to N1)
   - View questions filtered by level

3. **Question Type Management**
   - Categorize questions by type (Vocabulary, Grammar, Reading, Listening, etc.)
   - Create custom question types

4. **Exam Creation**
   - Select questions from the database
   - Generate custom exams with specific difficulty levels
   - Set exam duration

5. **Export Exams**
   - Export exams to PDF format
   - Proper rendering of Japanese characters using Noto Sans JP font

## 📊 Database Schema

The application uses the following main tables:

- **Levels**: Stores JLPT levels (N5, N4, N3, N2, N1)
- **QuestionTypes**: Question categories (Vocabulary, Grammar, Reading, Listening)
- **Questions**: Question content, level, type, and audio path
- **Choices**: Multiple-choice answers for each question
- **Exams**: Generated exam information
- **ExamQuestions**: Many-to-many relationship between exams and questions

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is open source and available under the [MIT License](LICENSE).

## 👥 Authors

- Your Name - Initial work

## 🙏 Acknowledgments

- Noto Sans JP font for proper Japanese character rendering
- Apache Software Foundation for POI and PDFBox libraries
- MySQL community for the excellent database system



**Note**: This application is designed for educational purposes to help manage Japanese language exam questions and generate practice tests for JLPT preparation.

