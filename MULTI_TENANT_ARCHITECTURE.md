# Multi-Tenant Restaurant Platform Architecture

## Overview

This platform implements a **shared-schema with tenant IDs** multi-tenant architecture, allowing multiple independent restaurants to operate through a single application instance while maintaining complete data isolation.

## Architecture Components

### 1. Tenant Resolution Strategy

**Subdomain-based Resolution**: Each restaurant gets its own subdomain (e.g., `restaurant1.kitchenlab.com`).

- **Subdomain Extraction**: The `TenantInterceptor` extracts the subdomain from the request URL
- **Restaurant Lookup**: Resolves subdomain to restaurant entity and tenant ID
- **Context Setting**: Sets tenant context in `TenantContext` for the request duration

### 2. Database Schema

#### Shared Tables with Tenant Isolation

All data is stored in shared tables with `tenant_id` columns for isolation:

```sql
-- Core tenant table
restaurants (id, subdomain, name, ...)

-- Tenant-specific data with restaurant_id foreign key
admins (restaurant_id, username, ...)
menus (restaurant_id, name, ...)
orders (restaurant_id, customer_name, ...)
customers (restaurant_id, name, ...)
```

#### Key Tables

- **restaurants**: Master table for all restaurant tenants
- **admins**: Restaurant staff with tenant association
- **admin_permissions**: Role-based permissions per admin

### 3. Tenant Context Management

#### TenantContext Thread-Local Storage

```java
// Thread-safe context for current request
TenantContext.setCurrentTenant(restaurantId, subdomain);
UUID currentTenant = TenantContext.getCurrentTenant();
String currentSubdomain = TenantContext.getCurrentSubdomain();
TenantContext.clear(); // Cleanup after request
```

#### System Context

For platform-level operations (super admin), system context is used:

```java
TenantContext.setSystemContext();
boolean isSystem = TenantContext.isSystemContext();
```

### 4. Security & Authentication

#### Tenant-Aware Authentication

- **Tenant Isolation**: Users can only authenticate within their tenant
- **Super Admin Access**: System context allows super admin cross-tenant access
- **Role-Based Access**: Different roles per tenant (RESTAURANT_ADMIN, MANAGER, STAFF)

#### Security Flow

1. **Request Interception**: `TenantInterceptor` resolves tenant from subdomain
2. **Context Setting**: Tenant context set for request duration
3. **Authentication**: `CustomUserDetailsService` loads user within tenant scope
4. **Authorization**: Security rules enforce tenant boundaries

### 5. Data Access Patterns

#### Repository Layer

```java
// Tenant-aware queries
Optional<Admin> findByUsernameAndRestaurantId(String username, UUID restaurantId);
List<Admin> findByRestaurantId(UUID restaurantId);

// System-wide queries (super admin only)
List<Admin> findAll();
```

#### Entity Filtering

```java
@Entity
public class Admin implements TenantAware {
    @Column(name = "restaurant_id")
    private UUID restaurantId; // Tenant ID
    
    // TenantAware interface methods
    UUID getTenantId() { return restaurantId; }
    void setTenantId(UUID tenantId) { this.restaurantId = tenantId; }
}
```

## URL Structure

### Platform URLs

- **System Admin**: `admin.kitchenlab.com/admin/system/*`
- **Restaurant Login**: `restaurant.kitchenlab.com/auth/login`
- **Restaurant Admin**: `restaurant.kitchenlab.com/admin/*`
- **Customer Portal**: `restaurant.kitchenlab.com/*`

### Development URLs

- **Local Development**: `localhost:8080` (system context)
- **Restaurant Testing**: `restaurant.localhost:8080` (tenant context)

## Tenant Management

### Restaurant Creation

1. **System Admin** creates restaurant via `/admin/system/restaurants`
2. **Subdomain Assignment**: Unique subdomain assigned (e.g., "marios-pizza")
3. **Database Setup**: Restaurant record created with unique ID
4. **Admin Setup**: Restaurant admin user created with tenant association

### Restaurant Configuration

Each restaurant can configure:
- **Branding**: Logo, colors, theme
- **Operations**: Delivery radius, hours, fees
- **Menu**: Categories, items, pricing
- **Staff**: Users, roles, permissions

## Benefits

### Cost Efficiency

- **Shared Infrastructure**: Single deployment serves all restaurants
- **Resource Pooling**: Database, server, maintenance costs shared
- **Scalability**: Easy to add new restaurants without new deployments

### Data Isolation

- **Complete Separation**: Each restaurant's data is completely isolated
- **Security**: Tenants cannot access other tenants' data
- **Performance**: Queries filtered by tenant for optimal performance

### Centralized Management

- **Platform Control**: System admin manages all restaurants
- **Updates**: Single codebase update affects all tenants
- **Monitoring**: Centralized logging and monitoring

## Development Guidelines

### Adding New Entities

1. **Implement TenantAware** interface
2. **Add restaurant_id column** in migration
3. **Update repositories** with tenant-aware queries
4. **Add tenant filtering** in service layer
5. **Test tenant isolation** thoroughly

### Security Considerations

1. **Always validate tenant context** in service methods
2. **Use tenant-aware queries** to prevent data leakage
3. **Implement row-level security** for sensitive operations
4. **Audit cross-tenant access** for super admin operations

### Testing

1. **Unit Tests**: Test tenant isolation at repository level
2. **Integration Tests**: Test tenant resolution and context
3. **Security Tests**: Verify tenant boundaries are enforced
4. **Performance Tests**: Ensure tenant filtering doesn't impact performance

## Migration Strategy

### Existing Single-Tenant Migration

1. **Create restaurants table** with default restaurant
2. **Add restaurant_id columns** to existing tables
3. **Migrate existing data** to default restaurant
4. **Update application** to use tenant context
5. **Test thoroughly** before production deployment

This architecture provides a robust, scalable foundation for serving multiple restaurants while maintaining complete data isolation and security.
