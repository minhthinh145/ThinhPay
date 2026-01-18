# ThinhPay

Ứng dụng thanh toán với Spring Boot + Angular

**Status**: 🚀 Phase 4 Complete - Monitoring Ready!  
**Progress**: 40% Overall | IAM: 100% | Core Banking: 95%

---

## 🎯 Recent Updates (2026-01-18)

### ✅ Phase 4 Complete - Monitoring & Health Checks
- Spring Boot Actuator integrated
- Health check endpoints working
- Prometheus metrics exposed
- Custom health indicators (DB, Redis)
- Production monitoring ready

### ✅ Phase 3 Complete - Rate Limiting
- Bucket4j integration working
- Endpoint-specific rate limits configured
- HTTP 429 responses verified
- 10 bugs fixed during implementation

### 🔧 Current Features
- ✅ User registration with OTP
- ✅ JWT authentication & token rotation
- ✅ Multi-currency accounts & transfers
- ✅ Rate limiting on auth endpoints
- ✅ Swagger UI documentation
- ✅ Health checks & monitoring
- ✅ Prometheus metrics
- ✅ Double-entry ledger system

---

## 📊 Monitoring

### Health Check:
```bash
curl http://localhost:8080/actuator/health
```

### Prometheus Metrics:
```bash
curl http://localhost:8080/actuator/prometheus
```

### Application Info:
```bash
curl http://localhost:8080/actuator/info
```

---

## Khởi động Project

### Development (Khuyên dùng cho dev - có hot reload)
```bash
docker-compose -f docker-compose.dev.yaml up
```

** Lưu ý:** Khi dev, **BẮT BUỘC** phải dùng file `docker-compose.dev.yaml` để có hot reload!

- **Frontend**: http://localhost:4200 (Angular dev server - tự động reload)
- **Backend**: http://localhost:8080 (Spring DevTools - tự động reload)
- **PgAdmin**: http://localhost:5050

### Production
```bash
docker-compose up -d
```

## Cấu trúc

- `backend/` - Spring Boot API
- `frontend/` - Angular UI
- `docker-compose.dev.yaml` - Development environment
- `docker-compose.yaml` - Production environment


```bash
# Dừng containers
docker-compose down

# Xem logs
docker-compose logs -f

# Rebuild
docker-compose up --build
```

## Development cục bộ

### Backend
```bash
cd backend
./mvnw spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm start
```

