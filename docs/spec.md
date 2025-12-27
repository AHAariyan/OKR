# OKR System — Product & Engineering Specification (Source of Truth)
Version: 1.0
Owner: Super Admin (single tenant initially)
Tech Stack:
- Frontend: Next.js (TypeScript, App Router)
- Backend: Java Spring Boot (Java 21)
- DB: PostgreSQL
- Auth: Email + PIN + Password (JWT)
- Deployment: Docker Compose (dev), production-ready structure

---

## 0) Absolute Rules (Non-Negotiable)
1. **One application** (no separate admin/user panel). Visibility and actions are **permission-based**.
2. **UI/UX must replicate the attached Google Sheet screenshots**:
   - Dashboard card layout and order
   - Sheet-like detail page layout (header band + columns + objective blocks + KR rows)
   - Progress color scale & confidence labels
3. **Backend is the source of truth** for permissions and calculations.
4. System hierarchy is fixed: **Company → Team → Department → Personal**.
5. Dashboard card order is fixed for all roles:
   - **Company card first**
   - then Team cards (as permitted)
   - then Department card (user’s department if permitted)
   - then Personal card (user)
6. Blocking a user:
   - user cannot log in
   - historical OKR data remains visible (for reporting)

---

## 1) Product Requirements Document (PRD)

### 1.1 Product Summary
Build a web OKR system that mirrors a Google Sheets-based OKR tracker. Users manage OKRs via a sheet-like UI. Permissions control which scopes they can view or manage.

### 1.2 Key Entities
- Company: OnnoRokom Innovation (single tenant now, multi-tenant-ready design preferred)
- Team: root unit under company (e.g., Software Engineering, Marketing, Sales)
- Department: under a Team (e.g., SQA under Software Engineering)
- Employee/User: belongs to exactly one Team and one Department
- Cycle: OKR period (Q1/Q2/Q3/Q4) with start/end dates
- Sheet: OKR sheet for a scope + cycle (COMPANY / TEAM / DEPARTMENT / PERSONAL)
- Objective: objective block
- Key Result: rows under objective

### 1.3 Roles & Access (RBAC + Scope)

#### Roles
- SUPER_ADMIN: can do anything/everything across the company
- ORG_LEADERSHIP: company-wide **view-only** (Chairman/CEO/MD/CTO)
- TEAM_ADMIN: manages own team; can create/manage team OKR + department OKRs under that team + personal OKR; can manage departments under the team; can view full team progress
- DEPARTMENT_ADMIN: manages own department OKR + personal OKR; can view company+team read-only
- MEMBER (Employee): manages personal OKR + department OKR; can view company+team read-only

#### Scope Types
- COMPANY
- TEAM
- DEPARTMENT
- PERSONAL

### 1.4 User Flows (Visual/Behavioral)

#### Chairman / ORG_LEADERSHIP flow
- Login → Dashboard
- Dashboard shows **Company card first**, then all Team cards
- Clicking any card opens corresponding sheet
- **Everything read-only**, no editing, no management actions

#### Team Admin flow
- Login → Dashboard
- Dashboard: Company card (read-only) → their Team card (editable) → their Team’s Department cards (editable) → Personal card (editable)
- Company sheet read-only
- Team & Dept sheets editable

#### Department Admin flow
- Login → Dashboard
- Dashboard: Company (read-only) → Team (read-only) → Department (editable) → Personal (editable)
- Can edit department and personal sheets only

#### Member flow
- Login → Dashboard
- Dashboard: Company (read-only) → Team (read-only) → Department (editable) → Personal (editable)
- Can edit department and personal sheets only

### 1.5 Admin Capabilities (Within Same App)
Super Admin-only menu items/pages:
- Team management (CRUD)
- Department management (CRUD)
- Employee management (CRUD), block/unblock, reset password, assign roles/scope
- Cycle management (CRUD)
- Override OKRs for anyone (create/edit/delete/hold)

---

## 2) UI Requirements (Must Match Screenshots)

### 2.1 Dashboard Page
- Title: “OKR Dashboard”
- Card grid layout similar to screenshot
- Card order: Company first, then permitted teams, then department, then personal
- Each card shows:
  - Days remaining (format: `X/TotalDays`)
  - Time progress (show 2 values if screenshot shows duplicates)
  - Overall progress (show 2 values if screenshot shows duplicates)
  - Objectives progress list (left objective names; right beige column with stacked percent values)

Card click behavior:
- Clicking card header opens the sheet page for that scope + active cycle

### 2.2 Sheet Detail Page (Sheet-like)
Must have:
- Beige header band with:
  - Title (Company name / Team name / Department name / Person name)
  - Left: Cycle, Start Date, End Date
  - Right: Days remaining, Time progress, Overall progress
- A grid/table with exact columns:
  Metric | Start Value | Target Value | Current Value | Progress | Owner | Confidence Level | Deadline | Aligned Projects / Tasks | Comments
- Objectives grouped as blocks:
  - Objective title row
  - Objective progress displayed aligned in Progress column
  - Under it: 5 KR rows (or N, but seeded as 5)

### 2.3 Editing Rules
- Editable fields for authorized scopes:
  - Objective title (if manage scope)
  - KR fields: title, current value, owner, confidence, deadline, aligned tasks, comments
- Read-only for view-only scopes
- Inline edit behavior:
  - click cell → edit
  - enter/save → call API
  - optimistic UI allowed but must reconcile with API response
- “Hold” state:
  - if a sheet is held, edits are disabled for non-super-admin

### 2.4 Progress Color Scale (Progress cell background)
- 0–39%: light red
- 40–59%: light orange/pink
- 60–79%: light yellow
- 80–99%: light green
- 100%: stronger green

### 2.5 Confidence Labels (Dropdown)
Display values exactly:
- Off Track
- At Risk
- On track
- Completed

Backend stores enum; frontend displays above labels.

---

## 3) Functional Requirements

### 3.1 Authentication
- Login with Email + PIN + Password
- JWT issued on login
- Blocked users cannot log in
- Password reset via email token flow

### 3.2 Cycle Management
- Super Admin creates cycles (Q3 etc.)
- Dashboard uses “active cycle” (latest ACTIVE cycle) unless user selects another

### 3.3 OKR Management
Scopes:
- COMPANY sheet: managed by Super Admin only (unless changed later)
- TEAM sheet: Team Admin + Super Admin
- DEPARTMENT sheet: Department Admin + Members of department + Super Admin
- PERSONAL sheet: user + Super Admin

### 3.4 Calculations (Backend Source of Truth)
For Percentage metric:
- KR progress = (current - start) / (target - start)
- clamp to 0..1
Objective progress:
- average of KR progress
Sheet overall progress:
- average of objective progress

Cycle time:
- total_days = (end_date - start_date) + 1
- days_elapsed = clamp(today - start_date + 1, 0, total_days)
- days_remaining = clamp(end_date - today, 0, total_days)
- time_progress = days_elapsed / total_days

### 3.5 Dashboard Data
Dashboard returns cards in required order:
1) Company
2) Teams permitted
3) Department (user’s)
4) Personal

### 3.6 Auditing (Required)
- Every update to objective/KR fields is recorded:
  - actor, entity, field, old/new, timestamp

---

## 4) Non-Functional Requirements
- Security:
  - All authorization must be enforced server-side
  - No data leaks across team/department scopes
- Reliability:
  - KR update + recalculation must be transactional
- Performance:
  - Dashboard and sheet must load fast; computed progress should be stored/cached in DB
- Maintainability:
  - Clear separation of frontend/backed concerns
  - Typed DTOs
- Observability:
  - basic request logging + error logging in backend

---

## 5) System Architecture

### 5.1 High-Level
- Next.js → Spring Boot REST → PostgreSQL
- JWT for auth
- RBAC middleware in backend:
  - Determine user role + scope
  - Evaluate canView/canManage per requested sheet scope

### 5.2 Backend Modules
- auth: login, reset password
- org: teams, departments, employees
- okr: cycles, sheets, objectives, key results
- permission: role assignments, checks
- audit: audit log

### 5.3 Frontend Modules
- auth pages + guarded routes
- dashboard page
- sheet renderer (layout-locked)
- components: cards, progress cell, editable cells, dropdowns

---

## 6) Data Model (PostgreSQL) — Conceptual
Tables (minimum):
- companies (single row now)
- teams (company_id)
- departments (team_id)
- users (team_id, department_id, pin, status)
- role_assignments (user_id, role, scope_type, scope_id)
- cycles
- okr_sheets (cycle_id, scope_type, scope_id, held, computed_overall_progress, computed_time_progress)
- objectives (sheet_id, title, owner_user_id, sort_order, computed_progress)
- key_results (objective_id, title, metric_type, start_value, target_value, current_value, computed_progress, owner_user_id, confidence_level, deadline, aligned_projects, comments, sort_order)
- audit_log

---

## 7) API Contract (Minimum Required Endpoints)
Auth:
- POST /api/auth/login
- POST /api/auth/forgot-password
- POST /api/auth/reset-password

Dashboard/Sheets:
- GET /api/dashboard?cycleId=...
- GET /api/sheets/{sheetId}
- PATCH /api/objectives/{objectiveId}
- PATCH /api/key-results/{krId}

Admin (Super Admin-only):
- CRUD teams, departments, users
- assign roles
- cycles CRUD

All endpoints must enforce authorization.

---

## 8) Development Plan (Enforced Phases)
Phase A: DB schema + seeds + docker compose postgres
Phase B: Spring Boot auth + RBAC + core models
Phase C: Sheet APIs + recalculation + audit logging
Phase D: Next.js auth + dashboard + sheet renderer
Phase E: Permission-driven UI controls + tests

Do not start Phase D before Phase C passes basic API tests.

---

## 9) Definition of Done
- All 5 role flows behave exactly as described
- Dashboard card order correct (Company first always)
- Sheet layout matches screenshot structure
- Read-only vs editable behavior correct for each scope
- Calculations correct and consistent after refresh
- Blocked user cannot log in
- Audit log records updates
