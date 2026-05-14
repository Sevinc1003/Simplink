# Simplink - URL Shortener API

Simplink is a purpose-driven backend service designed to shorten URLs, manage links, track access data, and provide reliable redirection.

## Live Application
The application is successfully deployed and running on Render. You can access the live version here:
Live Demo: https://simplink-1ckl.onrender.com/

## Technologies Used
* Backend: Java, Spring Boot
* Database: MySQL
* Testing: H2 Database (In-memory)
* Infrastructure & Deployment: Docker, GitHub Actions, Render
* Project Management: Jira

## Core Features
* URL Shortening: Converts long and complex URLs into short, unique links using a Base62 encoding utility (Base62Util).
* Link Management & Tracking: Manages the generated short links and records the specific IP addresses from which these links are accessed, utilizing an ipConfig configuration.
* Redirection: Instantly redirects the user to the original destination address when the short link is utilized.
* Integration Testing: The project includes integration tests that run on a temporary H2 database to verify system components without affecting the main database.

## API Endpoints

Below is the list of available REST API endpoints and their functionalities:

| HTTP Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/urls` | Accepts an original URL in the request body and returns the generated short URL. |
| **GET** | `/{shortCode}` | Redirects the user to the original URL associated with the short code and logs the click/IP data. |
| **GET** | `/urls/{shortCode}/ip-logs` | Retrieves the list of IP addresses and click logs for a specific short code. |
| **GET** | `/urls/{shortCode}/clicks` | Retrieves the total number of times a specific short link has been clicked. |
| **PUT** | `/api/urls/{id}` | Updates the original URL for a specific database ID. Returns the updated URL details. |
| **DELETE** | `/api/urls/{id}` | Deletes the URL record and its associated data from the database using its ID. |

## Project Tracking
The development process, tasks, and project updates are managed and tracked using Jira. You can view the project board here:
[Simplink Jira Board](https://sevincquliyeva000.atlassian.net/?continue=https%3A%2F%2Fsevincquliyeva000.atlassian.net%2Fwelcome%2Fsoftware%3FprojectId%3D10001&atlOrigin=eyJpIjoiZGUwMWEyNWUyMTViNDEzOWI1NTVmMmEzMDJjYjFhYTIiLCJwIjoiamlyYS1zb2Z0d2FyZSJ9)

## Local Setup

To test or develop the project on your local machine, follow these steps:

### 1. Clone the Repository
```bash
git clone [https://github.com/Sevinc1003/Simplink.git](https://github.com/Sevinc1003/Simplink.git)
cd Simplink