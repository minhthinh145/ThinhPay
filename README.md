# ThinhPay

Ứng dụng thanh toán với Spring Boot + Angular

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

