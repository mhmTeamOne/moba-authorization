# MOBA Authorization - Docker Setup

This document describes how to run the MOBA Authorization system using Docker containers.

## 🏗️ Architecture

The Docker setup includes:

- **PostgreSQL 15** - Database for application data and Keycloak
- **Keycloak 23** - Identity and Access Management
- **MOBA Application** - Your Quarkus application
- **Redis** - Caching and session storage (optional)
- **Nginx** - Reverse proxy and load balancer (optional, production profile)

## 📋 Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- 4GB+ RAM available for containers
- Ports 5432, 8080, 8081, 6379 available

## 🚀 Quick Start

### 1. Setup Environment

```bash
# Make scripts executable (if needed)
chmod +x scripts/docker-setup.sh scripts/docker-manage.sh

# Run setup script
./scripts/docker-setup.sh
```

### 2. Configure Environment Variables

Edit the `.env` file created by the setup script:

```bash
# Essential variables to update:
SENDGRID_API_KEY=SG.your-actual-sendgrid-api-key-here
DB_PASSWORD=your-secure-database-password
KEYCLOAK_ADMIN_PASSWORD=your-secure-keycloak-admin-password
JWT_SECRET=your-256-bit-jwt-secret-key
OIDC_SECRET=your-keycloak-client-secret
```

### 3. Start Services

```bash
# Start all services in background
./scripts/docker-manage.sh start -d

# Or use docker-compose directly
docker-compose up -d
```

### 4. Verify Services

```bash
# Check service status
./scripts/docker-manage.sh status

# View logs
./scripts/docker-manage.sh logs
```

## 📚 Available Scripts

### Setup Script: `scripts/docker-setup.sh`

Initializes the Docker environment:
- Creates necessary directories
- Generates configuration files
- Sets up database initialization scripts
- Creates Keycloak realm configuration

### Management Script: `scripts/docker-manage.sh`

```bash
# Start services
./scripts/docker-manage.sh start [-d]

# Stop services  
./scripts/docker-manage.sh stop

# Show logs
./scripts/docker-manage.sh logs [service-name]

# Check status
./scripts/docker-manage.sh status

# Build application
./scripts/docker-manage.sh build

# Open shell in app container
./scripts/docker-manage.sh shell

# Connect to database
./scripts/docker-manage.sh db

# Clean up resources
./scripts/docker-manage.sh clean

# Reset all data (WARNING: destructive)
./scripts/docker-manage.sh reset
```

## 🔧 Configuration Files

### Environment Variables (`.env`)

Based on your `application.properties`, the following variables are available:

```env
# Database
DB_NAME=moba
DB_USERNAME=postgres
DB_PASSWORD=secure_password

# Keycloak
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin_password

# Email (SendGrid)
SENDGRID_API_KEY=SG.your-key-here
SENDGRID_FROM_EMAIL=your-email@domain.com
SENDGRID_FROM_NAME=MOBA

# Security
JWT_SECRET=your-jwt-secret
OIDC_SECRET=your-oidc-secret

# Application
QUARKUS_PROFILE=prod
```

### Docker Compose Services

| Service | Port | Description |
|---------|------|-------------|
| `postgres` | 5432 | PostgreSQL database |
| `keycloak` | 8080 | Keycloak identity provider |
| `moba-app` | 8081 | MOBA Authorization application |
| `redis` | 6379 | Redis cache (optional) |
| `nginx` | 80, 443 | Reverse proxy (production profile) |

## 🐳 Docker Images

### Application Dockerfile Options

1. **Native Compilation** (`Dockerfile`)
   - Smaller image size (~50MB)
   - Faster startup time
   - Longer build time

2. **JVM Runtime** (`Dockerfile.jvm`)
   - Larger image size (~200MB)
   - Faster build time
   - Uses uber-jar packaging

To use JVM version, modify `docker-compose.yml`:

```yaml
moba-app:
  build:
    context: .
    dockerfile: Dockerfile.jvm  # Change this line
```

## 🌐 Service Access

After starting services:

| Service | URL | Credentials |
|---------|-----|-------------|
| MOBA Application | http://localhost:8081 | - |
| Keycloak Admin | http://localhost:8080/admin | admin / (from .env) |
| Database | localhost:5432 | postgres / (from .env) |
| Redis | localhost:6379 | - |

### API Endpoints

- **Health Check**: `GET http://localhost:8081/q/health`
- **User Registration**: `POST http://localhost:8081/user/registration`
- **Keycloak-First Registration**: `POST http://localhost:8081/user/register-keycloak-first`
- **Email Test**: `POST http://localhost:8081/email/test?to=test@example.com`

## 🔍 Debugging

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f moba-app
docker-compose logs -f keycloak
docker-compose logs -f postgres
```

### Access Containers

```bash
# Application container
docker exec -it moba-authorization /bin/sh

# Database container
docker exec -it moba-postgres psql -U postgres -d moba

# Keycloak container
docker exec -it moba-keycloak /bin/bash
```

### Common Issues

1. **Port conflicts**: Change ports in `docker-compose.yml`
2. **Memory issues**: Increase Docker memory limit
3. **Permission issues**: Check file ownership and permissions
4. **Network issues**: Verify no firewall blocking container communication

## 🔒 Security Considerations

### For Development
- Default passwords are acceptable
- HTTP connections are fine
- Self-signed certificates okay

### For Production
- **Change all default passwords**
- **Use strong secrets (256-bit minimum)**
- **Enable HTTPS with valid certificates**
- **Restrict network access**
- **Enable container security scanning**

### Environment Variables Security

```bash
# Good for development
JWT_SECRET=mySecretKeyThatShouldBeExternalizedInProductionEnvironment

# Better for production
JWT_SECRET=$(openssl rand -base64 32)
```

## 📈 Monitoring

### Health Checks

All services include health checks:
- **PostgreSQL**: Database connectivity
- **Keycloak**: HTTP endpoint availability
- **MOBA App**: Application health endpoint
- **Redis**: Redis ping command

### Metrics

Access application metrics:
- **Quarkus Metrics**: `http://localhost:8081/q/metrics`
- **Keycloak Metrics**: Enabled in container config

## 🧪 Testing

### Run Application Tests

```bash
# Using management script
./scripts/docker-manage.sh test

# Or manually
docker-compose -f docker-compose.test.yml up --abort-on-container-exit
```

### Manual Testing

```bash
# Test email functionality
curl -X POST "http://localhost:8081/email/test?to=your-email@example.com"

# Test user registration
curl -X POST "http://localhost:8081/user/registration" \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","username":"testuser","password":"password123","firstName":"Test","lastName":"User"}'
```

## 🔄 Data Management

### Backup Data

```bash
# Database backup
docker exec moba-postgres pg_dump -U postgres moba > backup.sql

# Keycloak backup
docker exec moba-keycloak /opt/keycloak/bin/kc.sh export --realm moba --file /tmp/realm-backup.json
docker cp moba-keycloak:/tmp/realm-backup.json ./keycloak-backup.json
```

### Restore Data

```bash
# Database restore
docker exec -i moba-postgres psql -U postgres moba < backup.sql

# Keycloak restore
docker cp ./keycloak-backup.json moba-keycloak:/tmp/realm-backup.json
docker exec moba-keycloak /opt/keycloak/bin/kc.sh import --file /tmp/realm-backup.json
```

## 🚀 Production Deployment

For production deployment:

1. **Use production profile**:
   ```bash
   QUARKUS_PROFILE=prod docker-compose --profile production up -d
   ```

2. **Configure external database**:
   - Use managed PostgreSQL service
   - Update connection strings

3. **Set up proper networking**:
   - Use Docker networks
   - Configure firewall rules

4. **Enable monitoring**:
   - Add Prometheus/Grafana
   - Set up log aggregation

5. **Implement CI/CD**:
   - Use GitHub Actions workflow (provided)
   - Automated testing and deployment

## 📝 Troubleshooting

### Container Won't Start

```bash
# Check container logs
docker-compose logs service-name

# Check resource usage
docker stats

# Verify environment variables
docker-compose config
```

### Database Connection Issues

```bash
# Test database connectivity
docker exec moba-postgres pg_isready -U postgres

# Check database logs
docker-compose logs postgres

# Verify application can connect
docker exec moba-authorization curl -f http://localhost:8081/q/health
```

### Email Not Working

1. Verify SendGrid API key in `.env`
2. Check sender email is verified in SendGrid  
3. Review application logs for email errors
4. Test with direct API call

---

## 📞 Support

For issues:
1. Check logs using management script
2. Verify configuration matches `application.properties`
3. Test individual services separately
4. Review Docker and system resources

This Docker setup provides a complete containerized environment for the MOBA Authorization system based on your application.properties configuration. 