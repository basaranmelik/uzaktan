@echo off
chcp 65001 >nul
title GUZEM - Uygulama Başlatılıyor

echo.
echo  ╔══════════════════════════════════════╗
echo  ║        GUZEM Platform Başlatılıyor   ║
echo  ╚══════════════════════════════════════╝
echo.

:: Docker yüklü mü kontrol et
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [HATA] Docker bulunamadı. Lütfen Docker Desktop'ı yükleyin:
    echo        https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

:: Docker çalışıyor mu kontrol et
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [HATA] Docker Desktop çalışmıyor. Lütfen başlatın ve tekrar deneyin.
    pause
    exit /b 1
)

echo [1/3] Eski containerlar durduruluyor...
docker compose down >nul 2>&1

echo [2/3] Uygulama derleniyor ve başlatılıyor...
echo       (İlk çalıştırmada bu adım 5-10 dakika sürebilir)
echo.
docker compose up --build -d

if %errorlevel% neq 0 (
    echo.
    echo [HATA] Başlatılamadı. Hata detayları:
    docker compose logs --tail=30
    pause
    exit /b 1
)

echo.
echo [3/3] Uygulama hazır olana kadar bekleniyor...
:waitloop
docker compose ps | find "guzem-app" | find "Up" >nul 2>&1
if %errorlevel% neq 0 (
    timeout /t 5 /nobreak >nul
    goto waitloop
)

echo.
echo  ✓ GUZEM başarıyla başlatıldı!
echo.
echo  Tarayıcıda açmak için: http://localhost:8080
echo.
echo  Durdurmak için: docker compose down
echo.

:: Tarayıcıyı otomatik aç
start http://localhost:8080

pause
