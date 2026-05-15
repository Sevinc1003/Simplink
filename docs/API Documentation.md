


Below is the list of available REST API endpoints and their functionalities:

| HTTP Method | Endpoint | Description | Access Level |
| :--- | :--- | :--- |
| **POST** | `api/auth/register` |	Register a new account | Public |
| **GET** |	`api/auth/login` |	Login and receive a JWT token | Public |
| **POST** | `/urls` | Accepts an original URL in the request body and returns the generated short URL. | Authenticated User |
| **GET** | `/{shortCode}` | Redirects the user to the original URL associated with the short code and logs the click/IP data. | Public |
| **GET** | `/urls/{shortCode}/ip-logs` | Retrieves the list of IP addresses and click logs for a specific short code. |  Owner Only |
| **GET** | `/urls/{shortCode}/clicks` | Retrieves the total number of times a specific short link has been clicked. | Owner Only |
| **PUT** | `/api/urls/{id}` | Updates the original URL for a specific database ID. Returns the updated URL details. | Owner Only |
| **DELETE** | `/api/urls/{id}` | Deletes the URL record and its associated data from the database using its ID. | Owner Only |