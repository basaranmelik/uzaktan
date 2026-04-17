# Docker Compose Setup

Bu dokümantasyon, Uzaktan Eğitim Platformunu Docker Compose ile ayağa kaldırmak için gerekli bilgileri içerir.

## 📋 Gereksinimler

- Docker (20.10+)
- Docker Compose (2.0+)
- 4GB+ RAM
- 2GB disk alanı

## 🚀 Hızlı Başlangıç

### Seçenek 1: Otomatik Script (Kolay)

```bash
chmod +x docker-start.sh
./docker-start.sh
```

Bu script şunları yapar:
- Gerekli dizinleri oluşturur (uploads, logs)
- Docker Compose'u başlatır
- Tüm servisleri kontrol eder
- Logları gösterir

### Seçenek 2: Manuel Komutlar

```bash
# Başlat
docker-compose up -d

# Logları göster
docker-compose logs -f app

# Durdur
docker-compose down

# Temiz durdur (volumes silme)
docker-compose down -v
```

## 📍 Erişim Adresleri

| Servis | Adres | Credential |
|--------|-------|-----------|
| Web Uygulaması | http://localhost:8080 | - |
| SQL Server | localhost:1433 | sa / verYs3cret |
| Database | GuzDB | - |

## 📁 Dizin Yapısı

```
.
├── compose.yaml              # Docker Compose konfigürasyonu
├── Dockerfile               # Spring Boot image tanımı
├── init-db.sql             # Database initialization script
├── .dockerignore            # Docker ignore dosyası
├── docker-start.sh         # Başlangıç scripti
├── .env.docker             # Ortam değişkenleri
├── uploads/                # Yüklenen dosyalar (otomatik oluşturulur)
├── logs/                   # Uygulama logları (otomatik oluşturulur)
└── src/main/resources/
    └── application-docker.properties  # Docker profili konfigürasyonu
```

## 🔧 Ortam Değişkenleri

### Docker Compose tarafından sağlanan:

```yaml
SPRING_PROFILES_ACTIVE=docker
SPRING_DATASOURCE_URL=jdbc:sqlserver://sqlserver:1433;databaseName=GuzDB;...
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=verYs3cret
```

### Özel değişkenler (.env.docker'de):

```bash
MAIL_PASSWORD=          # Gmail App Password (opsiyonel)
ZOOM_ACCOUNT_ID=        # Zoom Account ID (opsiyonel)
ZOOM_CLIENT_ID=         # Zoom Client ID (opsiyonel)
ZOOM_CLIENT_SECRET=     # Zoom Client Secret (opsiyonel)
```

## 📊 Servis Yapısı

```
┌─────────────────────────────────────────┐
│      Docker Compose Network             │
│  (uzaktan-network - bridge)             │
│                                         │
│  ┌──────────────────┐                  │
│  │ SQL Server       │                  │
│  │ (sqlserver)      │                  │
│  │ Port: 1433       │                  │
│  └──────────────────┘                  │
│           ▲                             │
│           │ (JDBC)                      │
│           │                             │
│  ┌──────────────────┐                  │
│  │ Spring Boot App  │                  │
│  │ (app)            │                  │
│  │ Port: 8080       │                  │
│  └──────────────────┘                  │
└─────────────────────────────────────────┘
         ▲
         │ (Host Network)
         │
    http://localhost:8080
    localhost:1433
```

## 🔍 Sık Kullanılan Komutlar

### Servislerin Durumunu Kontrol Et

```bash
docker-compose ps
```

**Çıkış:**
```
NAME                  COMMAND                  SERVICE      STATUS        PORTS
uzaktan-app          "java -jar app.jar"      app          Up            0.0.0.0:8080->8080/tcp
uzaktan-sqlserver    "/opt/mssql/bin/sqlse"  sqlserver    Up            0.0.0.0:1433->1433/tcp
```

### Belirli Servisin Loglarını Göster

```bash
# Sadece uygulamanın logları
docker-compose logs -f app

# Sadece SQL Server logları
docker-compose logs -f sqlserver

# Son 100 satırı göster
docker-compose logs --tail=100 app
```

### Servisleri Yeniden Başlat

```bash
# Tüm servisleri yeniden başlat
docker-compose restart

# Sadece uygulamayı yeniden başlat
docker-compose restart app
```

### Uygulamaya Shell Erişimi

```bash
docker-compose exec app /bin/bash
```

### SQL Server'a Bağlan

```bash
docker-compose exec sqlserver /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P verYs3cret
```

## 🛠️ Troubleshooting

### Uygulama başlamıyor

```bash
# Logları kontrol et
docker-compose logs -f app

# Volume temizle ve yeniden başlat
docker-compose down -v
docker-compose up -d
```

### Database bağlantı hatası

```bash
# SQL Server sağlıklı mı kontrol et
docker-compose ps

# SQL Server loglarını kontrol et
docker-compose logs sqlserver

# Manual bağlantı testi
docker-compose exec sqlserver /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P verYs3cret -Q "SELECT 1"
```

### Port zaten kullanımda

```bash
# Port 8080 kullanımda
lsof -i :8080

# Farklı port kullan (compose.yaml'ı düzenle)
# ports: "9090:8080"
```

## 📈 Performans İpuçları

1. **RAM Tahsisi**: Docker Desktop'ta minimum 4GB RAM ayırın
2. **Disk Alanı**: Video dosyaları için yeterli disk alanı bırakın
3. **Network**: Eğer yavaş, DNS'i kontrol edin
4. **Database**: Büyük dosyalar için SSD kullanın

## 🔐 Güvenlik Notları

⚠️ **Varsayılan Kimlik Bilgileri**:
- Bu setup **geliştirme ortamı** içindir
- Production'da kimlik bilgilerini değiştirin
- .env dosyaları Git'e eklemeyin

### Production için:

```bash
# Güçlü password oluştur
openssl rand -base64 32

# compose.yaml'ı güncelle
MSSQL_SA_PASSWORD=<güçlü-password>
SPRING_DATASOURCE_PASSWORD=<güçlü-password>
```

## 📝 Loglar ve Veri

### Loglar
```bash
# İçinde saklanır: ./logs/
# Container'da: /app/logs/

# Logları temizle
rm -rf logs/*

# Docker volume'ü kontrol et
docker volume ls
docker volume inspect uzaktan_sqlserver_data
```

### Yüklenen Dosyalar
```bash
# İçinde saklanır: ./uploads/
# Container'da: /app/uploads/

# Disk kullanımı
du -sh uploads/
```

## 🌐 Network Konfigürasyonu

- **Network Adı**: `uzaktan-network`
- **Network Tipi**: Bridge
- **DNS**: Otomatik (Docker tarafından)
- **Hostname**: `sqlserver` ve `app`

## 🔄 CI/CD Entegrasyonu

### GitHub Actions Örneği

```yaml
name: Build Docker Image

on: [push]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: docker/setup-buildx-action@v2
      - uses: docker/build-push-action@v4
        with:
          context: .
          push: true
          tags: myregistry/uzaktan:latest
```

## 📚 Ek Kaynaklar

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [SQL Server Container Documentation](https://learn.microsoft.com/en-us/sql/linux/quickstart-install-connect-docker)

## 💡 Kullanışlı Aliases

`.bash_profile` veya `.zshrc`'ye ekle:

```bash
# Docker Compose komutları
alias dc='docker-compose'
alias dcup='docker-compose up -d'
alias dcdown='docker-compose down'
alias dclogs='docker-compose logs -f'
alias dcps='docker-compose ps'
alias dcrestart='docker-compose restart'
```

Sonra kullanım:
```bash
dcup
dclogs app
dcdown
```

## 🎯 Sonraki Adımlar

1. ✅ http://localhost:8080 adresine gidin
2. ✅ Varsayılan hesapla giriş yapın
3. ✅ Platform kullanımına başlayın
4. ✅ Logları izleyin

---

**Son Güncelleme**: 2026-04-17
**Sürüm**: 1.0
