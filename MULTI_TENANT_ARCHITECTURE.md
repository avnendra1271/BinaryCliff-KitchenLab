# Multi-Tenant Architecture in KitchenLab

## 🏢 Overview

KitchenLab implements a sophisticated multi-tenant architecture that allows a single application instance to serve multiple restaurants while maintaining complete data isolation and security. This design enables cost-effective scaling, centralized maintenance, and independent restaurant operations.

## 🎯 Business Benefits

### For Restaurant Owners
- **Data Isolation**: Complete separation of restaurant data
- **Custom Branding**: Individual restaurant identity
- **Independent Operations**: No interference from other restaurants
- **Scalable Pricing**: Pay-as-you-grow model

### For System Administrators
- **Centralized Maintenance**: Single codebase to manage
- **Cost Efficiency**: Shared infrastructure reduces costs
- **Easy Onboarding**: Quick restaurant setup
- **Unified Analytics**: Cross-restaurant insights (for super admins)

## 🏗️ Technical Architecture

### 1. Tenant Identification Strategy

#### UUID-Based Tenant Identification
Each restaurant is assigned a unique UUID (Universally Unique Identifier) that serves as the primary tenant identifier:

```java
// Default Restaurant ID (can be configured per deployment)
private static final UUID DEFAULT_RESTAURANT_ID = 
    UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
```

#### Tenant ID Extraction Methods

The system supports multiple methods for tenant identification:

```java
private UUID extractTenantId(HttpServletRequest httpRequest) {
    // Method 1: Request Parameter
    String restaurantIdParam = httpRequest.getParameter("restaurantId");
    if (restaurantIdParam != null) {
        return UUID.fromString(restaurantIdParam);
    }
    
    // Method 2: HTTP Header (Preferred for API calls)
    String restaurantIdHeader = httpRequest.getHeader("X-Restaurant-ID");
    if (restaurantIdHeader != null) {
        return UUID.fromString(restaurantIdHeader);
    }
    
    // Method 3: Subdomain (for web interfaces)
    String serverName = httpRequest.getServerName();
    if (serverName.contains(".")) {
        String subdomain = serverName.split("\\.")[0];
        // Map subdomain to restaurant UUID
        return mapSubdomainToRestaurantId(subdomain);
    }
    
    return null;
}
```

### 2. Data Isolation Layer

#### Base Entity Design

All entities extend a common base class that includes the restaurant ID:

```java
@MappedSuperclass
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    // Getters and setters...
}
```

#### Entity Examples

```java
@Entity
@Table(name = "menu_categories")
public class MenuCategory extends BaseEntity {
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // Restaurant-specific fields and methods...
}

@Entity
@Table(name = "menu_items")
public class MenuItem extends BaseEntity {
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "price", nullable = false)
    private BigDecimal price;
    
    @Column(name = "category_id")
    private UUID categoryId;
    
    @Column(name = "is_available")
    private Boolean isAvailable = true;
    
    // Restaurant-specific menu item details...
}
```

### 3. Tenant Context Management

#### Thread-Local Context

The system uses ThreadLocal storage to maintain tenant context throughout request processing:

```java
public class TenantContext {
    
    private static final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();
    
    public static void setCurrentTenant(UUID restaurantId) {
        currentTenant.set(restaurantId);
    }
    
    public static UUID getCurrentTenant() {
        return currentTenant.get();
    }
    
    public static void clearCurrentTenant() {
        currentTenant.remove();
    }
}
```

#### Tenant Filter Implementation

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantFilter implements Filter {
    
    private static final Logger log = LoggerFactory.getLogger(TenantFilter.class);
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        try {
            UUID restaurantId = extractTenantId(httpRequest);
            
            // Skip tenant filtering for authentication endpoints
            String requestURI = httpRequest.getRequestURI();
            if (requestURI != null && (requestURI.contains("/api/auth/") || 
                                      requestURI.contains("/auth/login"))) {
                log.debug("Skipping tenant filter for authentication endpoint: {}", requestURI);
                chain.doFilter(request, response);
                return;
            }
            
            if (restaurantId != null) {
                // Set tenant context
                TenantContext.setCurrentTenant(restaurantId);
                
                // Set as request attribute for access
                httpRequest.setAttribute("restaurantId", restaurantId);
                
                log.debug("Tenant context set: {} for request: {}", 
                         restaurantId, httpRequest.getRequestURI());
            } else {
                log.warn("No tenant identifier found in request: {}", httpRequest.getRequestURI());
            }
            
            chain.doFilter(request, response);
            
        } catch (Exception e) {
            log.error("Error in tenant filter", e);
            throw e;
        } finally {
            // Always clear tenant context
            TenantContext.clearCurrentTenant();
        }
    }
}
```

### 4. Repository-Level Filtering

#### Custom Repository Base

```java
@NoRepositoryBean
public interface TenantAwareRepository<T extends BaseEntity, ID> 
    extends JpaRepository<T, ID> {
    
    @Override
    default List<T> findAll() {
        UUID currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null) {
            return findAllByRestaurantId(currentTenant);
        }
        return Collections.emptyList();
    }
    
    List<T> findAllByRestaurantId(UUID restaurantId);
    
    @Override
    default Optional<T> findById(ID id) {
        UUID currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null) {
            return findByIdAndRestaurantId(id, currentTenant);
        }
        return Optional.empty();
    }
    
    Optional<T> findByIdAndRestaurantId(ID id, UUID restaurantId);
    
    @Override
    default void deleteById(ID id) {
        UUID currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null) {
            deleteByIdAndRestaurantId(id, currentTenant);
        }
    }
    
    void deleteByIdAndRestaurantId(ID id, UUID restaurantId);
}
```

#### Specific Repository Implementation

```java
@Repository
public interface MenuCategoryRepository extends TenantAwareRepository<MenuCategory, UUID> {
    
    // Additional tenant-specific queries
    List<MenuCategory> findByRestaurantIdAndIsActive(UUID restaurantId, Boolean isActive);
    
    @Query("SELECT mc FROM MenuCategory mc WHERE mc.restaurantId = :restaurantId ORDER BY mc.displayOrder")
    List<MenuCategory> findByRestaurantIdOrderByDisplayOrder(@Param("restaurantId") UUID restaurantId);
    
    @Query("SELECT mc FROM MenuCategory mc WHERE mc.restaurantId = :restaurantId AND mc.name LIKE %:searchTerm%")
    List<MenuCategory> findByRestaurantIdAndNameContaining(@Param("restaurantId") UUID restaurantId, 
                                                           @Param("searchTerm") String searchTerm);
}
```

### 5. Service Layer Tenant Awareness

#### Service Implementation Example

```java
@Service
@Transactional
public class MenuServiceImpl implements MenuService {
    
    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;
    
    @Override
    public MenuCategoryDTO createCategory(CreateMenuCategoryDTO categoryDTO) {
        UUID currentRestaurantId = TenantContext.getCurrentTenant();
        
        if (currentRestaurantId == null) {
            throw new TenantNotSetException("No restaurant context set");
        }
        
        MenuCategory category = MenuCategory.builder()
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .displayOrder(categoryDTO.getDisplayOrder())
                .restaurantId(currentRestaurantId)
                .isActive(true)
                .build();
        
        MenuCategory savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }
    
    @Override
    public List<MenuCategoryDTO> getActiveCategories() {
        UUID currentRestaurantId = TenantContext.getCurrentTenant();
        
        if (currentRestaurantId == null) {
            throw new TenantNotSetException("No restaurant context set");
        }
        
        List<MenuCategory> categories = categoryRepository
                .findByRestaurantIdAndIsActive(currentRestaurantId, true);
        
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
```

## 🔐 Security Implications

### 1. Data Isolation Guarantees

- **Database Level**: All queries automatically filtered by restaurant ID
- **Application Level**: Tenant context validation in all service methods
- **API Level**: Tenant ID validation and authentication
- **Session Level**: Thread-local context prevents cross-tenant data access

### 2. Tenant Validation

```java
@Component
public class TenantValidator {
    
    public void validateTenantAccess(UUID requestedRestaurantId) {
        UUID currentRestaurantId = TenantContext.getCurrentTenant();
        
        if (currentRestaurantId == null) {
            throw new TenantNotSetException("No tenant context available");
        }
        
        if (!currentRestaurantId.equals(requestedRestaurantId)) {
            throw new TenantAccessException(
                String.format("Access denied: Cannot access restaurant %s from tenant %s", 
                              requestedRestaurantId, currentRestaurantId));
        }
    }
}
```

### 3. Authentication Integration

```java
@Service
public class AuthService {
    
    public LoginResponse login(LoginRequest request) {
        // Authenticate user
        Admin admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }
        
        // Check if user has access to requested restaurant
        if (request.getRestaurantId() != null) {
            validateRestaurantAccess(admin, request.getRestaurantId());
        }
        
        // Generate JWT with restaurant context
        String token = jwtTokenUtil.generateToken(admin, request.getRestaurantId());
        
        return LoginResponse.builder()
                .token(token)
                .restaurantId(admin.getRestaurantId())
                .role(admin.getRole())
                .build();
    }
}
```

## 📊 Database Schema Considerations

### 1. Tenant-Aware Table Design

```sql
-- All tables include restaurant_id for tenant isolation
CREATE TABLE menu_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    
    CONSTRAINT fk_menu_categories_restaurant 
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
);

CREATE TABLE menu_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id UUID NOT NULL,
    category_id UUID,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    is_available BOOLEAN DEFAULT true,
    is_vegetarian BOOLEAN DEFAULT false,
    is_popular BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    
    CONSTRAINT fk_menu_items_restaurant 
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id),
    CONSTRAINT fk_menu_items_category 
        FOREIGN KEY (category_id) REFERENCES menu_categories(id)
);
```

### 2. Indexing Strategy

```sql
-- Composite indexes for tenant-aware queries
CREATE INDEX idx_menu_categories_restaurant_active 
    ON menu_categories(restaurant_id, is_active);

CREATE INDEX idx_menu_items_restaurant_available 
    ON menu_items(restaurant_id, is_available);

CREATE INDEX idx_menu_items_restaurant_category 
    ON menu_items(restaurant_id, category_id);

CREATE INDEX idx_orders_restaurant_status 
    ON orders(restaurant_id, status);
```

## 🚀 Performance Optimization

### 1. Database Connection Pooling

```properties
# Multi-tenant connection pool configuration
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.connection-timeout=20000
```

### 2. Query Optimization

```java
@Entity
@Table(name = "menu_items")
@NamedQueries({
    @NamedQuery(
        name = "MenuItem.findByRestaurantAndAvailable",
        query = "SELECT mi FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.isAvailable = true ORDER BY mi.name"
    ),
    @NamedQuery(
        name = "MenuItem.findByRestaurantAndCategory",
        query = "SELECT mi FROM MenuItem mi WHERE mi.restaurantId = :restaurantId AND mi.categoryId = :categoryId ORDER BY mi.displayOrder"
    )
})
public class MenuItem extends BaseEntity {
    // Entity implementation
}
```

### 3. Caching Strategy

```java
@Service
public class MenuService {
    
    @Cacheable(value = "menu-categories", key = "#restaurantId")
    public List<MenuCategoryDTO> getActiveCategories(UUID restaurantId) {
        // Implementation with caching
    }
    
    @CacheEvict(value = "menu-categories", key = "#restaurantId")
    public void clearCategoryCache(UUID restaurantId) {
        // Cache eviction
    }
}
```

## 🔧 Configuration and Deployment

### 1. Tenant Configuration

```properties
# Multi-tenant configuration
restaurant.default-id=123e4567-e89b-12d3-a456-426614174000
restaurant.tenant-strategy=header # header, parameter, subdomain
restaurant.isolation-level=row-level # row-level, database-level

# Tenant validation
restaurant.strict-validation=true
restaurant.cross-tenant-access=false
```

### 2. Environment-Specific Setup

```yaml
# application-dev.yml
restaurant:
  default-id: 123e4567-e89b-12d3-a456-426614174000
  tenant-strategy: parameter
  debug-mode: true

# application-prod.yml
restaurant:
  tenant-strategy: header
  strict-validation: true
  audit-logging: true
```

## 📈 Monitoring and Analytics

### 1. Tenant-Specific Metrics

```java
@Component
public class TenantMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public void recordMenuAccess(UUID restaurantId) {
        Counter.builder("menu.access")
                .tag("restaurant", restaurantId.toString())
                .register(meterRegistry)
                .increment();
    }
    
    public void recordOrderCreation(UUID restaurantId, BigDecimal amount) {
        Counter.builder("orders.created")
                .tag("restaurant", restaurantId.toString())
                .register(meterRegistry)
                .increment();
        
        Gauge.builder("orders.revenue")
                .tag("restaurant", restaurantId.toString())
                .register(meterRegistry, amount, BigDecimal::doubleValue);
    }
}
```

### 2. Audit Logging

```java
@Component
public class TenantAuditLogger {
    
    @EventListener
    public void handleTenantAccessEvent(TenantAccessEvent event) {
        AuditLog log = AuditLog.builder()
                .restaurantId(event.getRestaurantId())
                .userId(event.getUserId())
                .action(event.getAction())
                .resource(event.getResource())
                .timestamp(LocalDateTime.now())
                .build();
        
        auditLogRepository.save(log);
    }
}
```

## 🔄 Best Practices

### 1. Development Guidelines

- **Always validate tenant context** in service methods
- **Use tenant-aware repositories** for data access
- **Implement proper error handling** for tenant violations
- **Test tenant isolation** thoroughly
- **Document tenant-specific behavior**

### 2. Security Considerations

- **Never trust client-provided tenant IDs** without validation
- **Implement proper authentication** for tenant access
- **Use HTTPS** for all tenant communications
- **Regular security audits** of tenant isolation
- **Monitor for cross-tenant access attempts**

### 3. Performance Considerations

- **Optimize database queries** with proper indexing
- **Implement caching** for frequently accessed tenant data
- **Monitor resource usage** per tenant
- **Implement rate limiting** per tenant
- **Consider database partitioning** for large deployments

## 🚨 Common Pitfalls and Solutions

### 1. Tenant Context Leaks

**Problem**: Thread-local context not properly cleaned up

**Solution**: Always clear context in finally blocks
```java
try {
    TenantContext.setCurrentTenant(restaurantId);
    // Business logic
} finally {
    TenantContext.clearCurrentTenant();
}
```

### 2. Cross-Tenant Data Access

**Problem**: Queries not properly filtered by tenant

**Solution**: Use tenant-aware repositories and validate context
```java
public List<MenuItem> getMenuItems() {
    UUID restaurantId = TenantContext.getCurrentTenant();
    if (restaurantId == null) {
        throw new TenantNotSetException("No tenant context");
    }
    return menuItemRepository.findByRestaurantId(restaurantId);
}
```

### 3. Authentication Issues

**Problem**: Users accessing wrong tenant data

**Solution**: Validate tenant access during authentication
```java
public LoginResponse login(LoginRequest request) {
    Admin admin = authenticateUser(request);
    validateRestaurantAccess(admin, request.getRestaurantId());
    return generateToken(admin, request.getRestaurantId());
}
```

## 🎯 Future Enhancements

### 1. Advanced Features

- **Database-per-tenant isolation** for enterprise clients
- **Dynamic tenant provisioning** with automated setup
- **Tenant-specific configurations** and customizations
- **Cross-tenant analytics** for super admin insights
- **Tenant health monitoring** and alerting

### 2. Scalability Improvements

- **Horizontal scaling** with load balancers
- **Database sharding** for large tenant counts
- **Multi-region deployment** for global availability
- **Automated tenant migration** tools

---

This multi-tenant architecture provides a robust, secure, and scalable foundation for serving multiple restaurants while maintaining complete data isolation and operational independence.
