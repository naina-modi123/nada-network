# Nada Network API

A lightweight REST API built using Java to manage people and their mutual connections.

## Features

- Create people with unique IDs.
- Connect two existing people.
- Retrieve a person's direct contacts.
- Find the shortest connection path with a maximum of three hops.
- Validate requests involving invalid people or connections.
- Support Unicode names, including Hindi characters.

## Technology Stack

- Java 21
- Java HTTP Server (`com.sun.net.httpserver.HttpServer`)
- JSON-based REST API

## Run Locally

Compile the application:

```powershell
javac Main.java
```

Start the server:

```powershell
java Main
```

The server runs at `http://localhost:8080`.

## API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/people` | Create a person |
| POST | `/knows` | Create a mutual connection |
| GET | `/people/{id}/contacts` | Retrieve direct contacts |
| GET | `/path?from={id}&to={id}` | Find a path of up to three hops |

## Example Requests

Create a person:

```json
{"name":"Asha"}
```

Create a connection:

```json
{"a":1,"b":2}
```

Find a path:

```text
GET /path?from=1&to=2
```

## Current Limitations

- People and connections are stored in memory and reset when the server restarts.
- Data is not persisted in a database.
- Automated tests and complete request-validation coverage should be added before final submission.


## Running Tests

Make sure the API server is running on port 8080 in a separate terminal.

Compile the application and test:
```powershell
javac Main.java tests\PathTest.java
```

Run the automated tests:
```powershell
java -cp ".;tests" tests.PathTest
```

The tests verify that a path of exactly three hops is accepted and a path of four hops is rejected.


## Future Improvements

- Add automated tests for all API endpoints and validation cases.
- Use a JSON parser for reliable request validation and response encoding.
- Improve error responses and code organization.

## AI Tools

AI assistance was used during development for implementation guidance, debugging, and testing suggestions. All generated code should be reviewed and understood before submission.
