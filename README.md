# Smart Parking for College

A Java web application for viewing campus parking spaces and managing parking reservations.

## Technology stack

- Java 17
- Maven
- Jakarta Servlet API 6.0
- Apache Tomcat 10.1
- MySQL with JDBC
- HTML, CSS, and JavaScript

## Project layout

```text
src/
└── main/
    ├── java/com/college/parking/
    └── webapp/
        ├── css/
        ├── js/
        └── WEB-INF/
pom.xml
```

## Requirements

Install JDK 17, Maven, MySQL, and Apache Tomcat 10.1.

## Build

From the project root, run:

```powershell
mvn validate
mvn package
```

The WAR file is created at `target/smart-parking.war`.

## Database credentials

Never commit database passwords or other secrets. Configure credentials locally through environment variables when database integration is added.

## Current status

Initial project structure and starter login/signup interface. Authentication and database integration will be implemented and tested in later steps.
