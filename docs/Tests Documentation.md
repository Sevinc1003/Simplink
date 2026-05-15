
## Integration Testing

The project includes comprehensive integration tests to ensure end-to-end correctness of the system.

The following critical flows are tested:

#### Authentication
- User registration and JWT token generation
- Access control verification for protected endpoints

#### URL Shortening
- Authenticated users can create short URLs
- Short URL format validation
- Unauthorized users are rejected

#### Redirection Flow
- Short code correctly redirects to original URL
- HTTP `302 FOUND` response validation
- Location header verification

#### Analytics
- Click tracking functionality
- IP log creation after redirection
- Retrieval of click count (owner-only access)
- Retrieval of IP logs (owner-only access)

#### Authorization
- Only URL owner can:
  - Update URL
  - Delete URL
  - Access analytics endpoints
- Other users receive `403 FORBIDDEN`


### Advanced Testing Tools

- **Awaitility**
  - Used to wait for asynchronous IP logging completion

- **MockMvc**
  - Simulates full HTTP layer without starting server


### Example Tested Flow

1. User registers and receives JWT token  
2. User creates a short URL  
3. Short URL is accessed via browser  
4. System:
   - Redirects to original URL (`302`)
   - Logs IP address asynchronously  
5. Owner retrieves analytics (click count + IP logs)