# Batch Data Insert & Concurrency Tool

This project provides a user interface to execute DDL statements, detect created tables.  
And perform bulk data insertion for testing high-volume queries.

## Features

- Execute DDL statements from UI
- Detect table creation automatically
- Bulk insert realistic test data using Faker library
- Configure batch size and concurrency for optimal performance

## Prerequisites

Before starting the application, please ensure that:

- The database **schema name** matches your local setup.
- The configured **database username** and **password** are correct.
- Update `application.yml` file with the correct password and schema name.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/{ database_name }
    username: { your_username }
    password: { your_password }
```

Failing to update these settings may result in connection errors.

## Parameters

- `batchSize`: Number of records inserted per batch
- `concurrency`: Number of concurrent insertion threads
