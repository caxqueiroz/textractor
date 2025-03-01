# Textractor

Textractor is a Java-based document processing application that extracts text from various document formats using OCR (Optical Character Recognition) technology. The application leverages the ABBYY FineReader Engine for high-quality text extraction and provides a scalable architecture for processing documents at scale.

## Architecture Overview

Textractor follows a microservices-based architecture with the following key components:

### Core Components

1. **OCR Engine Layer**
   - Integrates with ABBYY FineReader Engine for text extraction
   - Manages engine resources efficiently through a pool mechanism
   - Processes documents asynchronously using Redis as a message broker

2. **Database Layer**
   - Stores application profiles and processed file information
   - Uses Spring Data JDBC for database operations
   - Supports both production databases and H2 for testing

3. **Service Layer**
   - Coordinates operations between the OCR engine and database
   - Implements business logic for document processing
   - Manages application profiles and processed files

4. **API Layer**
   - Exposes REST endpoints for client applications
   - Handles document upload and processing requests
   - Provides status updates and results retrieval

## Key Classes

### OCR Engine

- **AbbyyEngine**: Main service that initializes the OCR engine and subscribes to Redis topics for processing requests
- **AbbyyEnginePool**: Manages a pool of OCR engine instances for efficient resource utilization
- **FileProcessing**: Data model representing a document processing request

### Database

- **AppProfileRepository**: Repository for application profiles, storing configuration for different client applications
- **ProcessedFilesRepository**: Repository for tracking processed files and their results
- **AppProfile**: Entity representing an application profile with configuration settings
- **ProcessedFile**: Entity representing a processed document with metadata and results

### Services

- **AppProfileService**: Service for managing application profiles
- **ProcessedFilesService**: Service for tracking and managing processed files
- **OcrService**: Service that coordinates the OCR processing workflow

## Data Flow

1. Client applications submit documents for processing through the API
2. Documents are stored temporarily and a processing request is published to Redis
3. The AbbyyEngine service receives the request and assigns it to an available engine in the pool
4. The document is processed, and the results are stored in the database
5. The client application can retrieve the processing results through the API

## Configuration

The application uses Spring Boot for configuration management:

- **OcrConfig**: Configuration properties for the OCR engine
- **RedisConfig**: Configuration for Redis messaging
- **DatabaseConfig**: Configuration for database connections

## Testing

The application includes comprehensive unit and integration tests:

- **AbbyyEngineTest**: Tests for the AbbyyEngine service
- **AbbyyEnginePoolTest**: Tests for the engine pool management
- **AppProfileRepositoryTest**: Tests for the application profile repository
- **ProcessedFilesRepositoryTest**: Tests for the processed files repository

## Getting Started

### Prerequisites

- Java 11 or higher
- Redis server
- PostgreSQL database (or H2 for development)
- ABBYY FineReader Engine (licensed)

### Setup

1. Clone the repository
2. Configure the application properties in `application.properties` or `application.yml`
3. Place the ABBYY FineReader Engine libraries in the specified location
4. Build the application using Maven: `mvn clean package`
5. Run the application: `java -jar textractor.jar`

### Configuration Properties

Key configuration properties include:

```properties
# OCR Engine Configuration
textractor.ocr.lib-folder=/path/to/abbyy/lib
textractor.ocr.license-path=/path/to/abbyy/license
textractor.ocr.customer-project-id=your-project-id
textractor.ocr.license-password=your-license-password

# Redis Configuration
textractor.redis.host=localhost
textractor.redis.port=6379
textractor.redis.ocr.topic=ocr-processing-topic

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/textractor
spring.datasource.username=your-username
spring.datasource.password=your-password

## Development Guidelines

- Follow the existing code structure and patterns
- Write unit tests for all new functionality
- Use H2 database for testing to ensure database compatibility
- Use lenient mocking in tests to avoid unnecessary stubbing warnings
- Ensure proper resource cleanup in the OCR engine components

