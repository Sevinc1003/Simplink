# Simplink - URL Shortener API

Simplink is a purpose-driven backend service designed to shorten URLs, manage links, track access data, and provide reliable redirection.

## Technologies Used
* Backend: Java, Spring Boot
* Database: MySQL
* Testing: H2 Database (In-memory)
* Infrastructure & Deployment: Docker, GitHub Actions, Render

## Core Features
* URL Shortening: Converts long and complex URLs into short, unique links using a Base62 encoding utility (Base62Util).
* Link Management & Tracking: Manages the generated short links and records the specific IP addresses from which these links are accessed.
* Redirection: Instantly redirects the user to the original destination address when the short link is utilized.
* Integration Testing: The project includes integration tests that run on a temporary H2 database to verify system components without affecting the main database.

## Local Setup

To test or develop the project on your local machine, follow these steps:

### 1. Clone the Repository
```bash
git clone [https://github.com/Sevinc1003/Simplink.git](https://github.com/Sevinc1003/Simplink.git)
cd Simplink