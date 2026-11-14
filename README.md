# FridgeManager

FridgeManager is a smart fridge management system built with Java, Spring Boot and React. It helps users track the contents of their fridge, monitor expiration dates, and manage grocery lists efficiently.

## Status

This project currently in development, so some features may not work correctly or may not be finished. Also UI will be revamped after finishing most features.

## Features
- User authentication and role management
- Add, update, and remove food items from the fridge
- Track expiration dates
- Manage your grocery lists and add items from list to fridge when purchased
- RESTful API for frontend integration

## Technologies Used
- Java 17+
- Spring Boot
- PostgreSQL
- Hibernate (JPA)
- REST API
- React

## Backend history pagination

The item history endpoints accept `offset` (default `0`) and `limit` (default `50`, maximum `100`):

- `/api/fridge/items/{id}/history`
- `/api/grocery/{listId}/history`
- `/api/grocery/items/{id}/history`

These parameters page through JaVers snapshots, newest first. A snapshot can contain several field changes, so the
number of returned changes can exceed `limit`. Advance `offset` by `limit` to read the next page.

`/api/fridge/items` accepts zero-based `page` (default `0`), `size` (default `20`), and `sort`
(default `expirationDate,asc`). It returns a Spring Data page with `content`, `totalElements`, and `totalPages`.

`/api/fridge/activity` (also `/api/fridge/logs`) accepts zero-based `page` (default `0`) and `size`
(default `50`, maximum `100`). It returns a Spring Data slice: read entries from `content` and use `last`
to determine whether another page exists. Activity is always ordered by timestamp descending. Offsets
(`page * effective size`) above `10000` return HTTP 400.

Activity pages count visible activity entries. The service scans audit snapshots in batches as needed,
without a fixed history scan cutoff. For example, request `/api/fridge/activity?page=1&size=50` for the second page.
