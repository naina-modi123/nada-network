# Nada Network API

A lightweight REST API built using Java 21 to manage people, mutual connections, and shortest connection paths.

## Features

- Create people with unique IDs.
- Connect two people through mutual connections.
- Retrieve a person's direct contacts.
- Find the shortest connection path with a maximum of three hops.
- Validate requests involving invalid people or connections.
- Support Unicode names, including Hindi characters.
- Automated tests for three-hop and four-hop path limits.

## Technology Stack

- Java 21
- Maven
- Java HTTP Server (`com.sun.net.httpserver.HttpServer`)
- JUnit 5
- JSON-based REST API

## Prerequisites

- Java 21 or compatible JDK
- Apache Maven 3.9 or later

Verify the installations:

```powershell
java -version
mvn -version
```

## Run Locally

From the project root, compile and package the application:

```powershell
mvn clean package
```

Start the server:

```powershell
java -cp target/classes Main
```

The server runs at `http://localhost:8080`.

Keep the server running in a separate terminal while executing the integration tests.

## API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/people` | Create a person |
| POST | `/knows` | Create a mutual connection |
| GET | `/people/{id}/contacts` | Retrieve direct contacts |
| GET | `/path?from={id}&to={id}` | Find a path of up to three hops |

## Example Requests

### Create a person

`POST /people`

```json
{
  "name": "Asha"
}
```

### Create a mutual connection

`POST /knows`

```json
{
  "a": 1,
  "b": 2
}
```

### Retrieve direct contacts

`GET /people/1/contacts`

### Find a connection path

`GET /path?from=1&to=2`

The path response indicates whether the destination is reachable within the three-hop limit.

## Running Tests

**Important:** Start the API server first in a separate terminal. The integration tests send HTTP requests to `localhost:8080`.

Run the automated tests:

```powershell
mvn clean test
```

The tests verify that:

- A path of exactly three hops is accepted.
- A path of four hops is rejected.

The current test suite contains two tests.

## Current Limitations

- People and connections are stored in memory and reset when the server restarts.
- Data is not persisted in a database.
- Automated coverage currently focuses on the shortest-path hop limit; other endpoints and invalid-input cases need additional automated tests.

## Future Improvements

- Add automated tests for all API endpoints and request-validation cases.
- Use a JSON parser for reliable request validation and response encoding.
- Improve error handling and code organization.
- Add persistent storage if required.

## AI Tools

AI assistance was used for implementation guidance, debugging, and testing suggestions. The code and test results should be understood and reviewed before submission.
