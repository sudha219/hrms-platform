# Multi-Tenant HRMS Backend (Spring Boot 3 + Java 21)

This backend implements the complete Human Resource Management System (HRMS) specifications from the PRD with multi-company logical tenant isolation, Role-Based Access Control (RBAC), and 2-step OTP authentication.

---

## 👥 Seed Accounts & Credentials

| Role | Email | Password | Tenant Code | Scope / Capabilities |
| :--- | :--- | :--- | :--- | :--- |
| **Super Admin** | `superadmin@hrms.com` | `Admin@123` | Platform | Provision new companies & assign company admins |
| **Company Admin** | `admin@acme.com` | `AcmeAdmin@123` | `ACME` | Manage employees, departments, payroll, attendance |
| **Manager** | `manager@acme.com` | `Manager@123` | `ACME` | Review & approve team leave requests |
| **Employee** | `employee@acme.com` | `Employee@123` | `ACME` | Clock in/out, view balance, apply leave, payslips |

---

## 🚀 Running the Project

### Prerequisites
* Java 21+ (Java 23 detected)
* Maven 3.9+ or Maven Wrapper

### Start the Application
```bash
mvn spring-boot:run
```

Once started:
* **Base API URL:** `http://localhost:8080`
* **Swagger UI / OpenAPI 3.0 Documentation:** `http://localhost:8080/swagger-ui.html`
* **H2 Database Console:** `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:hrmsdb`, User: `sa`, Password: empty)

---

## 🔑 Authentication Flow (2FA OTP)

### Step 1: Sign in with credentials
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "employee@acme.com",
    "password": "Employee@123"
  }'
```
**Response:**
```json
{
  "success": true,
  "message": "OTP sent to your registered email address.",
  "data": {
    "otpRequired": true,
    "tempToken": "eyJhbGciOiJIUzI1NiJ9...",
    "email": "employee@acme.com"
  }
}
```
*(In development mode, check the server console logs for `[MOCK EMAIL DELIVERY] Sent OTP code XXXXXX`)*

### Step 2: Verify OTP and acquire JWT Access Token
```bash
curl -X POST http://localhost:8080/api/v1/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "tempToken": "<tempToken-from-step-1>",
    "otp": "123456"
  }'
```
**Response:**
```json
{
  "success": true,
  "message": "Authentication successful.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "userId": "...",
    "email": "employee@acme.com",
    "companyId": "...",
    "companyName": "Acme Corporation",
    "role": "EMPLOYEE"
  }
}
```

---

## 📡 API Endpoint Overview

### 1. Platform Super Admin (`/api/v1/platform/**`)
* `GET /api/v1/platform/companies` — List all company tenants
* `POST /api/v1/platform/companies` — Provision a new company tenant + initial admin
* `PATCH /api/v1/platform/companies/{id}/status` — Change company status (`ACTIVE`, `SUSPENDED`, `ARCHIVED`)

### 2. Employees & Directory (`/api/v1/employees/**`)
* `GET /api/v1/employees/me` — Current employee self-profile
* `GET /api/v1/employees` — Paginated and searchable company employee directory
* `POST /api/v1/employees` — Create an employee profile and optional login credentials

### 3. Attendance Engine (`/api/v1/attendance/**`)
* `POST /api/v1/attendance/check-in` — Clock in (web/mobile)
* `POST /api/v1/attendance/check-out` — Clock out and calculate duration
* `GET /api/v1/attendance/my-records` — Personal attendance log
* `GET /api/v1/attendance/company-records` — Company-wide attendance records
* `POST /api/v1/attendance/{recordId}/correct` — HR correction with audit justification

### 4. Leave Management (`/api/v1/leaves/**`)
* `GET /api/v1/leaves/balances` — Retrieve remaining leave balances
* `POST /api/v1/leaves/requests` — Submit a leave request (balance and overlap checked)
* `GET /api/v1/leaves/my-requests` — Personal leave request history
* `GET /api/v1/leaves/team-requests` — Manager view for pending subordinate requests
* `PATCH /api/v1/leaves/requests/{id}/action` — Manager approval or rejection

### 5. Payroll & Payslips (`/api/v1/payroll/**`)
* `POST /api/v1/payroll/runs/generate` — Generate draft payroll calculation
* `PATCH /api/v1/payroll/runs/{id}/approve` — Authorize & lock payroll period
* `GET /api/v1/payroll/payslips/my-payslips` — Employee secure personal payslip view
