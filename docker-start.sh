#!/bin/bash

# Docker Compose ile Uzaktan Eğitim Platformu Başlatma Scripti

set -e

echo "🚀 Uzaktan Eğitim Platformu Docker Compose Başlatılıyor..."
echo ""

# Renkler
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Gerekli dizinleri oluştur
echo "${BLUE}📁 Gerekli dizinler oluşturuluyor...${NC}"
mkdir -p uploads logs
chmod 777 uploads logs

# Docker Compose başlat
echo "${BLUE}🐳 Docker Compose başlatılıyor...${NC}"
docker-compose up -d

# Çıkış kodunu kontrol et
if [ $? -ne 0 ]; then
    echo "${RED}❌ Docker Compose başlatılamadı!${NC}"
    exit 1
fi

echo ""
echo "${GREEN}✅ Docker Compose başarıyla başlatıldı!${NC}"
echo ""

# Servislerin durumunu kontrol et
echo "${BLUE}📊 Servis durumu kontrol ediliyor...${NC}"
sleep 5

echo ""
echo "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo "${GREEN}✨ Uzaktan Eğitim Platformu Hazır!${NC}"
echo "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "${BLUE}🌐 Web Adresi:${NC}  http://localhost:8080"
echo "${BLUE}🗄️  Database:${NC}    localhost:1433"
echo "   ${YELLOW}Sa / verYs3cret${NC}"
echo ""
echo "${BLUE}📝 Loglar görüntülemek için:${NC}"
echo "   docker-compose logs -f app"
echo ""
echo "${BLUE}🛑 Durdur:${NC}"
echo "   docker-compose down"
echo ""

# Logları göster
echo "${BLUE}📋 Uygulama logları (Ctrl+C ile çık):${NC}"
echo ""
docker-compose logs -f app
