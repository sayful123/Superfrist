# Super Frist VPN - Complete System Documentation & Deployment Guide

Welcome to **Super Frist VPN**, an enterprise-grade Virtual Private Network ecosystem engineered with an Android mobile client, high-throughput WireGuard/OpenVPN architecture, a secure REST API backend, PostgreSQL database, and an administrative control panel.

---

## 🔐 Credentials & Quick Test Data

### Administrator Credentials
- **Portal URL**: `http://localhost:3000/admin` (or tap the shield/admin icon in the Android app)
- **Admin Email**: `admin@superfrist.vpn`
- **Admin Password**: `Admin@2026!`

### Pre-Seeded Activation Codes
| Activation Code | Validity | Status | Description / Testing Purpose |
| :--- | :--- | :--- | :--- |
| `VPN-8F7K-29MX-QP4A` | 30 Days | ACTIVE | Standard 1-Month Production Pass |
| `VPN-FAST-7777-UAE1` | 60 Days | ACTIVE | VIP High-Speed Pass (UAE Priority) |
| `VPN-PRO1-9999-YEAR` | 365 Days | ACTIVE | Annual Pass (365 Days) |
| `VPN-EXPR-1111-TEST` | 7 Days | EXPIRED | Tests "Activation Code Expired" error flow |
| `VPN-DISA-2222-TEST` | 30 Days | DISABLED | Tests "Activation Code Disabled" error flow |
| `VPN-USED-3333-TEST` | 30 Days | USED | Tests "Code Already Activated on Another Device" |

---

## 🏗️ System Architecture

```
                                    +-----------------------------------------+
                                    |        Super Frist Admin Console        |
                                    |  (Web Panel / In-App Administrator UI)  |
                                    +--------------------+--------------------+
                                                         |
                                                         v
+-----------------------------+      HTTPS / REST        +--------------------+
|     Android VPN Client      | <=====================>  |  Node.js API Core  |
|  (Jetpack Compose + MVVM)   |                          |  (Express/JWT/TS)  |
+--------------+--------------+                          +---------+----------+
               |                                                   |
               | WireGuard TUN                                     v
               v (Encrypted)                             +--------------------+
+-----------------------------+                          |     PostgreSQL     |
|   Global VPN Gateway Nodes  |                          |  (Database Engine) |
|  (AE, SG, DE, NL, US, UK)   |                          +--------------------+
+-----------------------------+
```

---

## 📱 1. Android VPN Application

### Key Features
- **Activation Flow**: Onboarding screen with single-input activation code formatter (`VPN-XXXX-XXXX-XXXX`), paste button, and interactive test code helper drawer.
- **VPN Dashboard**:
  - Central illuminated cyber power button with pulsating radar rings.
  - Connection statuses: `DISCONNECTED`, `CONNECTING`, `CONNECTED`, `DISCONNECTING`.
  - Remaining validity countdown card (e.g. `29 Days 12 Hours`).
  - Active tunnel duration stopwatch timer (`00:00:00`).
  - Live telemetry monitor: Virtual IP (`10.8.0.2`), ping latency with jitter simulation, download & upload speeds.
- **Global Server Selection**:
  - UAE - Dubai 🇦🇪 (host: `ae-dxb.vpn.superfrist.net`, 32 ms)
  - Singapore 🇸🇬 (host: `sg-sin.vpn.superfrist.net`, 48 ms)
  - Germany - Frankfurt 🇩🇪 (host: `de-fra.vpn.superfrist.net`, 65 ms)
  - Netherlands - Amsterdam 🇳🇱 (host: `nl-ams.vpn.superfrist.net`, 68 ms)
  - United States - New York 🇺🇸 (host: `us-nyc.vpn.superfrist.net`, 110 ms)
  - United Kingdom - London 🇬🇧 (host: `uk-lon.vpn.superfrist.net`, 72 ms)
- **Security & Settings**:
  - Android system Kill Switch support toggle.
  - Auto-connect on app launch toggle.
  - Protocol switcher: WireGuard / OpenVPN.
  - Connection notifications with Disconnect action.
  - In-app administrator portal.
  - Local Cluster Mode / Remote API Server mode toggle.

---

## 🖥️ 2. Backend & Web Admin Panel

### Running with Docker Compose
To launch the PostgreSQL database, API server, and web Admin Panel in a single command:

```bash
cd backend
docker-compose up --build -d
```

Access the services:
- **API Gateway**: `http://localhost:3000/api`
- **Web Admin Panel**: `http://localhost:3000/admin`
- **Health Check**: `http://localhost:3000/health`

### Running Locally without Docker

1. **Install Dependencies**:
```bash
cd backend
npm install
```

2. **Database Setup**:
Ensure PostgreSQL is running on `localhost:5432` with database `superfrist_vpn`, then execute `schema.sql`:
```bash
psql -U superfrist_user -d superfrist_vpn -f schema.sql
```

3. **Start the API Server**:
```bash
npm run build
npm start
```

---

## 🔑 3. Activation Code Lifecycle & Rules

1. **First App Launch**:
   - Device generates an anonymous random persistent device ID (`DEV-XXXXXXXXXXXX`).
   - User inputs an activation code (e.g. `VPN-8F7K-29MX-QP4A`).
   - Server checks existence, validity, expiry, and device authorization.
   - Upon success, device is locked to the code and a signed session token is created.
   - Main VPN dashboard opens with the full validity countdown displayed.
2. **Returning User**:
   - App automatically verifies stored session on launch against the server/DB.
   - If valid, directly displays the VPN dashboard without asking for the code again.
3. **Expiry / Expiration**:
   - When the subscription expires, the active VPN connection is automatically terminated and the activation prompt is displayed with "Activation Code Expired".
4. **Device Transfer / Reset**:
   - Administrators can reset the device binding from the Admin Panel with one click, allowing the code to be used on a new device.
