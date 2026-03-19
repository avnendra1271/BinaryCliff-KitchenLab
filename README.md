# KitchenLab - Restaurant Management System

## 🍴 Overview

KitchenLab is a comprehensive, industry-standard restaurant management system built with Spring Boot and modern web technologies. It provides complete functionality for restaurant operations including menu management, order processing, analytics, and multi-tenant support.

## 🏗️ Architecture

### Technology Stack
- **Backend**: Spring Boot 4.0.3
- **Database**: PostgreSQL
- **Frontend**: HTML5, TailwindCSS, JavaScript
- **Authentication**: JWT-based security
- **ORM**: Spring Data JPA with Hibernate
- **Multi-tenancy**: UUID-based restaurant isolation

### Key Features
- ✅ Multi-tenant architecture (restaurant isolation)
- ✅ JWT-based authentication and authorization
- ✅ Comprehensive menu management system
- ✅ Order processing and management
- ✅ Analytics and reporting
- ✅ Role-based access control (Super Admin, Restaurant Admin, Manager)
- ✅ Real-time order notifications
- ✅ Responsive admin dashboard
- ✅ Customer-facing menu interface

## 📁 Project Structure

```
BinaryCliff-KitchenLab/
├── src/
│   ├── main/
│   │   ├── java/com/binarycliff/kitchenlab/
│   │   │   ├── auth/                    # Authentication & Authorization
│   │   │   │   ├── controller/         # Auth controllers
│   │   │   │   ├── entity/            # Admin entities
│   │   │   │   ├── repository/        # Auth repositories
│   │   │   │   ├── service/           # Auth services
│   │   │   │   └── config/            # Security configuration
│   │   │   ├── common/                # Shared components
│   │   │   │   ├── entity/            # Base entities
│   │   │   │   ├── exception/         # Global exceptions
│   │   │   │   └── dto/               # Common DTOs
│   │   │   ├── menu/                  # Menu Management Module
│   │   │   │   ├── controller/        # Menu REST controllers
│   │   │   │   ├── entity/            # Menu entities (Category, Item)
│   │   │   │   ├── repository/        # Menu repositories
│   │   │   │   ├── service/           # Menu service layer
│   │   │   │   └── dto/               # Menu DTOs
│   │   │   ├── order/                 # Order Management Module
│   │   │   ├── tenant/                # Multi-tenant support
│   │   │   ├── controller/            # Main controllers
│   │   │   └── config/               # Application configuration
│   │   └── resources/
│   │       ├── templates/            # Thymeleaf templates
│   │       │   ├── admin/            # Admin interface pages
│   │       │   ├── auth/             # Authentication pages
│   │       │   └── customer/        # Customer interface pages
│   │       ├── static/               # Static assets (CSS, JS, images)
│   │       └── application.properties # Application configuration
│   └── test/                         # Test packages
├── pom.xml                          # Maven configuration
└── README.md                        # This file
```

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6 or higher

### Database Setup

1. **Create Database**
```sql
CREATE DATABASE kitchenlab;
CREATE USER postgres WITH PASSWORD 'pqsoft123';
GRANT ALL PRIVILEGES ON DATABASE kitchenlab TO postgres;
```

2. **Default Configuration**
- **Database URL**: `jdbc:postgresql://localhost:5432/kitchenlab`
- **Username**: `postgres`
- **Password**: `pqsoft123`

### Application Configuration

The application uses the following key configurations:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/kitchenlab
spring.datasource.username=postgres
spring.datasource.password=pqsoft123

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# Multi-tenant Configuration
restaurant.default-id=123e4567-e89b-12d3-a456-426614174000

# Security Configuration
jwt.secret=your-secret-key
jwt.expiration=86400000

# Application Port
server.port=8080
```

### Running the Application

1. **Clone and Build**
```bash
git clone <repository-url>
cd BinaryCliff-KitchenLab
mvn clean install
```

2. **Run Application**
```bash
mvn spring-boot:run
```

3. **Access Application**
- **Admin Dashboard**: `http://localhost:8080/admin/kitchenlab-admin`
- **Customer Interface**: `http://localhost:8080/indexai`
- **API Base**: `http://localhost:8080/api`

## 🔐 Authentication & Authorization

### Default Users

#### Super Admin
- **Username**: `admin`
- **Password**: `admin123`
- **Role**: `SUPER_ADMIN`
- **Access**: All restaurants, system configuration

#### Restaurant Admin
- **Username**: `restaurant_admin`
- **Password**: `admin123`
- **Role**: `RESTAURANT_ADMIN`
- **Access**: Single restaurant operations

### Creating Admin Users

Use the test endpoints to create admin users:

```bash
# Create Super Admin
GET http://localhost:8080/api/test/create-admin

# Create Restaurant Admin
GET http://localhost:8080/api/test/create-restaurant-admin

# Reset Admin Password
GET http://localhost:8080/api/test/reset-admin-password
```

### JWT Authentication

The application uses JWT tokens for authentication:

1. **Login Endpoint**
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

2. **Token Usage**
Include JWT token in Authorization header:
```http
Authorization: Bearer <jwt-token>
```

## 📋 Menu Management Module

### Features

#### Categories
- Create, update, delete menu categories
- Category ordering and display management
- Category images and descriptions
- Soft delete functionality

#### Menu Items
- Comprehensive item management
- Pricing and availability control
- Dietary information (vegetarian, allergens)
- Popular items marking
- Discount management
- Preparation time tracking
- Spice level indicators
- Calorie information

#### Search & Filtering
- Full-text search across menu items
- Category-based filtering
- Status-based filtering (available, out of stock)
- Dietary preference filtering

### API Endpoints

#### Category Management
```http
POST   /api/menu/categories              # Create category
GET    /api/menu/categories              # Get all categories
GET    /api/menu/categories/{id}         # Get category by ID
PUT    /api/menu/categories/{id}         # Update category
DELETE /api/menu/categories/{id}         # Delete category
PATCH  /api/menu/categories/{id}/toggle  # Toggle category status
```

#### Item Management
```http
POST   /api/menu/items                   # Create menu item
GET    /api/menu/items                   # Get all items
GET    /api/menu/items/{id}              # Get item by ID
PUT    /api/menu/items/{id}              # Update item
DELETE /api/menu/items/{id}              # Delete item
PATCH  /api/menu/items/{id}/availability # Toggle availability
PATCH  /api/menu/items/{id}/popularity   # Toggle popularity
```

#### Search & Analytics
```http
GET    /api/menu/search                  # Search menu items
GET    /api/menu/statistics              # Get menu statistics
GET    /api/menu/complete                # Get complete menu structure
```

## 🎨 Frontend Interfaces

### Admin Dashboard (`kitchenlab-admin.html`)

**Features:**
- Modern, responsive design with TailwindCSS
- Real-time order notifications
- Category and item management
- Drag-and-drop interface support
- Bulk import/export functionality
- Print-ready menu layouts
- Analytics dashboard
- User profile management

**Key Components:**
- Sidebar navigation
- Search and filter controls
- Modal dialogs for CRUD operations
- Toast notification system
- Responsive grid/list views

### Customer Interface (`indexai.html`)

**Features:**
- Clean, customer-friendly design
- Interactive menu browsing
- Item search and filtering
- Detailed item views
- Shopping cart functionality
- Order placement
- Real-time order tracking

## 🏢 Multi-Tenant Architecture

### Restaurant Isolation

The system supports multiple restaurants through UUID-based tenant isolation:

```java
// Base entity with restaurant ID
@Entity
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;
}
```

### Tenant Filtering

Automatic tenant filtering through custom filter:

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantFilter implements Filter {
    // Extracts restaurant ID from headers/parameters
    // Sets tenant context for request scope
    // Filters all database queries by restaurant
}
```

### Usage

Include restaurant ID in requests:
```http
X-Restaurant-ID: 123e4567-e89b-12d3-a456-426614174000
```

## 📊 Analytics & Reporting

### Menu Statistics
- Total items count
- Available vs unavailable items
- Popular items tracking
- Category-wise distribution
- Order frequency analysis

### Order Analytics
- Order volume trends
- Peak hours analysis
- Popular items by orders
- Revenue tracking
- Customer behavior insights

## 🔧 Configuration Details

### Database Schema

The application uses JPA/Hibernate for automatic schema management:

```properties
spring.jpa.hibernate.ddl-auto=update
```

**Key Tables:**
- `admins` - User management
- `admin_permissions` - Role-based permissions
- `menu_categories` - Menu categories
- `menu_items` - Menu items
- `orders` - Order management
- `order_items` - Order-item relationships

### Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // JWT authentication filter
    // Role-based authorization
    // Public endpoint configuration
    // CORS configuration
}
```

## 🧪 Testing

### Test Endpoints

Use these endpoints for testing and verification:

```bash
# Test application health
GET http://localhost:8080/api/test/health

# Test menu functionality
GET http://localhost:8080/api/test/test-menu

# Test database connectivity
GET http://localhost:8080/api/test/test-menu-db

# Create test users
GET http://localhost:8080/api/test/create-admin
GET http://localhost:8080/api/test/create-restaurant-admin
```

### Integration Testing

The application includes comprehensive integration tests for:
- Authentication flows
- Menu management operations
- Multi-tenant data isolation
- API endpoint functionality

## 🚀 Deployment

### Production Configuration

For production deployment:

1. **Environment Variables**
```bash
DATABASE_URL=jdbc:postgresql://your-db-host:5432/kitchenlab
DB_USERNAME=your-db-user
DB_PASSWORD=your-db-password
JWT_SECRET=your-production-secret
```

2. **Application Properties**
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
logging.level.root=WARN
```

### Docker Support

```dockerfile
FROM openjdk:17-jre-slim
COPY target/kitchenlab-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🔄 API Integration

### External Services

The application is designed to integrate with:
- Payment gateways
- SMS/email notification services
- Third-party delivery services
- Inventory management systems
- Accounting software

### Webhook Support

Configurable webhooks for:
- Order status updates
- Inventory alerts
- Customer notifications
- Analytics events

## 🛠️ Development Guide

### Adding New Features

1. **Create Entity**
```java
@Entity
@Table(name = "new_feature")
public class NewFeature extends BaseEntity {
    // Entity fields
}
```

2. **Create Repository**
```java
@Repository
public interface NewFeatureRepository extends JpaRepository<NewFeature, UUID> {
    // Custom queries
}
```

3. **Create Service**
```java
@Service
@Transactional
public class NewFeatureService {
    // Business logic
}
```

4. **Create Controller**
```java
@RestController
@RequestMapping("/api/new-feature")
public class NewFeatureController {
    // REST endpoints
}
```

### Code Standards

- **Java**: Follow Google Java Style Guide
- **Database**: Use snake_case for column names
- **API**: RESTful principles with proper HTTP methods
- **Frontend**: Mobile-first responsive design
- **Security**: Principle of least privilege

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection Errors**
   - Verify PostgreSQL is running
   - Check database credentials
   - Ensure database exists

2. **Authentication Issues**
   - Verify JWT secret configuration
   - Check token expiration
   - Validate user credentials

3. **Multi-tenant Issues**
   - Ensure restaurant ID header is included
   - Verify tenant context is set
   - Check data isolation

4. **Frontend Issues**
   - Clear browser cache
   - Check console for JavaScript errors
   - Verify static resource loading

### Debug Mode

Enable debug logging:
```properties
logging.level.com.binarycliff.kitchenlab=DEBUG
spring.jpa.show-sql=true
```

## 📈 Performance Optimization

### Database Optimization
- Indexed queries on frequently accessed fields
- Connection pooling configuration
- Query optimization with JPA hints

### Caching Strategy
- Redis integration for session management
- Application-level caching for menu data
- CDN for static assets

### Frontend Optimization
- Lazy loading for large menus
- Image optimization and compression
- Minified CSS/JavaScript bundles

## 🔒 Security Considerations

### Implementation Details
- Password hashing with BCrypt
- JWT token expiration and refresh
- SQL injection prevention
- XSS protection in templates
- CSRF token validation

### Best Practices
- Regular security audits
- Dependency vulnerability scanning
- Secure password policies
- Role-based access control
- Audit logging for sensitive operations

## 📞 Support & Maintenance

### Monitoring
- Application health checks
- Performance metrics collection
- Error tracking and alerting
- Database performance monitoring

### Backup Strategy
- Regular database backups
- Configuration backups
- Disaster recovery procedures
- Data retention policies

## 🤝 Contributing

### Development Workflow
1. Fork the repository
2. Create feature branch
3. Implement changes with tests
4. Submit pull request
5. Code review and merge

### Guidelines
- Follow coding standards
- Write comprehensive tests
- Update documentation
- Ensure backward compatibility

## 📄 License

This project is proprietary software of BinaryCliff Technologies. All rights reserved.

---

## 📞 Contact

**BinaryCliff Technologies**
- Email: support@binarycliff.com
- Website: www.binarycliff.com
- Support: 24/7 technical support available

---

*Last Updated: March 20, 2026*
*Version: 1.0.0*
