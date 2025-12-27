-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. COMPANIES
CREATE TABLE companies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. TEAMS
CREATE TABLE teams (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES companies(id),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. DEPARTMENTS
CREATE TABLE departments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    team_id UUID NOT NULL REFERENCES teams(id),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. USERS
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    team_id UUID REFERENCES teams(id),
    department_id UUID REFERENCES departments(id),
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255), -- User display name
    password_hash VARCHAR(255) NOT NULL, -- In real app, this would be hashed. For seed/MVP using simple text or placeholder.
    pin VARCHAR(6) NOT NULL,
    is_blocked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. ROLE ASSIGNMENTS
-- Scope types: COMPANY, TEAM, DEPARTMENT, PERSONAL
CREATE TABLE role_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id),
    role VARCHAR(50) NOT NULL, -- SUPER_ADMIN, ORG_LEADERSHIP, TEAM_ADMIN, DEPARTMENT_ADMIN, MEMBER
    scope_type VARCHAR(50) NOT NULL,
    scope_id UUID, -- Can be null for SUPER_ADMIN if global, or specific ID
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. CYCLES
CREATE TABLE cycles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL, -- e.g. "Q1 2024"
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. OKR SHEETS
CREATE TABLE okr_sheets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cycle_id UUID NOT NULL REFERENCES cycles(id),
    scope_type VARCHAR(50) NOT NULL, -- COMPANY, TEAM, DEPARTMENT, PERSONAL
    scope_id UUID NOT NULL, -- Link to Company, Team, Dept, or User ID
    is_held BOOLEAN DEFAULT FALSE,
    computed_overall_progress DOUBLE PRECISION DEFAULT 0.0,
    computed_time_progress DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. OBJECTIVES
CREATE TABLE objectives (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sheet_id UUID NOT NULL REFERENCES okr_sheets(id),
    title TEXT NOT NULL,
    owner_user_id UUID REFERENCES users(id),
    sort_order INTEGER DEFAULT 0,
    computed_progress DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 9. KEY RESULTS
CREATE TABLE key_results (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    objective_id UUID NOT NULL REFERENCES objectives(id),
    title TEXT,
    metric_type VARCHAR(50) DEFAULT 'PERCENTAGE', -- PERCENTAGE, NUMERIC, CURRENCY (Simplified to PERCENTAGE mostly per spec)
    start_value DOUBLE PRECISION DEFAULT 0.0,
    target_value DOUBLE PRECISION DEFAULT 100.0,
    current_value DOUBLE PRECISION DEFAULT 0.0,
    computed_progress DOUBLE PRECISION DEFAULT 0.0,
    owner_user_id UUID REFERENCES users(id),
    confidence_level VARCHAR(50) DEFAULT 'ON_TRACK', -- OFF_TRACK, AT_RISK, ON_TRACK, COMPLETED
    deadline DATE,
    aligned_projects TEXT,
    comments TEXT,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 10. AUDIT LOG
CREATE TABLE audit_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    actor_user_id UUID REFERENCES users(id),
    entity_type VARCHAR(50) NOT NULL, -- OBJECTIVE, KEY_RESULT, SHEET
    entity_id UUID NOT NULL,
    field_name VARCHAR(50) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    occurred_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- SEED DATA

-- 1. Company
INSERT INTO companies (id, name) VALUES 
('11111111-1111-1111-1111-111111111111', 'OnnoRokom Innovation');

-- 2. Teams
INSERT INTO teams (id, company_id, name) VALUES
('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'Software Engineering'),
('22222222-2222-2222-2222-333333333333', '11111111-1111-1111-1111-111111111111', 'Sales');

-- 3. Departments
INSERT INTO departments (id, team_id, name) VALUES
('33333333-3333-3333-3333-333333333333', '22222222-2222-2222-2222-222222222222', 'SQA'),
('33333333-3333-3333-3333-444444444444', '22222222-2222-2222-2222-222222222222', 'Backend');

-- 4. Users
-- Super Admin (No specific team/dept strictly required but schema enforces FKs? Spec says user belongs to exactly one team/dept. Let's assign root or dummy for super admin if needed, or make cols nullable. Spec "Employee/User: belongs to exactly one Team and one Department". I will attach Super Admin to Engineering for now to satisfy constraints if strict, but let's assume they are flexible or part of "Board" team. I'll create a Board team for Leadership.)

INSERT INTO teams (id, company_id, name) VALUES ('22222222-2222-2222-2222-999999999999', '11111111-1111-1111-1111-111111111111', 'Board');
INSERT INTO departments (id, team_id, name) VALUES ('33333333-3333-3333-3333-999999999999', '22222222-2222-2222-2222-999999999999', 'Executive');

-- Users
-- Password hash is BCrypt hash of 'secret' for all users
-- Hash generated using: BCryptPasswordEncoder().encode("secret")
INSERT INTO users (id, team_id, department_id, email, name, password_hash, pin, is_blocked) VALUES
('44444444-4444-4444-4444-111111111111', '22222222-2222-2222-2222-999999999999', '33333333-3333-3333-3333-999999999999', 'admin@onnorokom.com', 'Super Admin', '$2a$10$yu3pbZkMeO/6TfnKjrTnlus68rut5YHBuxdWe42DaEFBBOjBKCX02', '1234', FALSE),
('44444444-4444-4444-4444-222222222222', '22222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-444444444444', 'eng.lead@onnorokom.com', 'Engineering Lead', '$2a$10$yu3pbZkMeO/6TfnKjrTnlus68rut5YHBuxdWe42DaEFBBOjBKCX02', '1234', FALSE), -- Team Admin
('44444444-4444-4444-4444-333333333333', '22222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333', 'qa.lead@onnorokom.com', 'QA Lead', '$2a$10$yu3pbZkMeO/6TfnKjrTnlus68rut5YHBuxdWe42DaEFBBOjBKCX02', '1234', FALSE), -- Dept Admin
('44444444-4444-4444-4444-444444444444', '22222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333', 'qa.member@onnorokom.com', 'QA Member', '$2a$10$yu3pbZkMeO/6TfnKjrTnlus68rut5YHBuxdWe42DaEFBBOjBKCX02', '1234', FALSE); -- Member

-- 5. Roles
INSERT INTO role_assignments (user_id, role, scope_type, scope_id) VALUES
('44444444-4444-4444-4444-111111111111', 'SUPER_ADMIN', 'COMPANY', '11111111-1111-1111-1111-111111111111'),
('44444444-4444-4444-4444-222222222222', 'TEAM_ADMIN', 'TEAM', '22222222-2222-2222-2222-222222222222'),
('44444444-4444-4444-4444-333333333333', 'DEPARTMENT_ADMIN', 'DEPARTMENT', '33333333-3333-3333-3333-333333333333'),
('44444444-4444-4444-4444-444444444444', 'MEMBER', 'PERSONAL', '44444444-4444-4444-4444-444444444444');

-- 6. Cycle
INSERT INTO cycles (id, name, start_date, end_date, is_active) VALUES
('55555555-5555-5555-5555-555555555555', 'Q1 2024', '2024-01-01', '2024-03-31', TRUE);

-- 7. Sheets (Examples)
-- Company Sheet
INSERT INTO okr_sheets (id, cycle_id, scope_type, scope_id) VALUES
('66666666-6666-6666-6666-111111111111', '55555555-5555-5555-5555-555555555555', 'COMPANY', '11111111-1111-1111-1111-111111111111');

-- Team Sheet (Eng)
INSERT INTO okr_sheets (id, cycle_id, scope_type, scope_id) VALUES
('66666666-6666-6666-6666-222222222222', '55555555-5555-5555-5555-555555555555', 'TEAM', '22222222-2222-2222-2222-222222222222');

-- Dept Sheet (SQA)
INSERT INTO okr_sheets (id, cycle_id, scope_type, scope_id) VALUES
('66666666-6666-6666-6666-333333333333', '55555555-5555-5555-5555-555555555555', 'DEPARTMENT', '33333333-3333-3333-3333-333333333333');

-- Personal Sheet (Admin user)
INSERT INTO okr_sheets (id, cycle_id, scope_type, scope_id) VALUES
('66666666-6666-6666-6666-444444444444', '55555555-5555-5555-5555-555555555555', 'PERSONAL', '44444444-4444-4444-4444-111111111111');

-- 8. Objectives
-- Company Objectives
INSERT INTO objectives (id, sheet_id, title, owner_user_id, sort_order, computed_progress) VALUES
('77777777-7777-7777-7777-111111111111', '66666666-6666-6666-6666-111111111111', 'Increase Revenue by 25%', '44444444-4444-4444-4444-111111111111', 1, 0.45),
('77777777-7777-7777-7777-222222222222', '66666666-6666-6666-6666-111111111111', 'Improve Customer Satisfaction', '44444444-4444-4444-4444-111111111111', 2, 0.70);

-- Team Objectives (Engineering)
INSERT INTO objectives (id, sheet_id, title, owner_user_id, sort_order, computed_progress) VALUES
('77777777-7777-7777-7777-333333333333', '66666666-6666-6666-6666-222222222222', 'Deliver MVP by End of Q1', '44444444-4444-4444-4444-222222222222', 1, 0.60),
('77777777-7777-7777-7777-444444444444', '66666666-6666-6666-6666-222222222222', 'Reduce Technical Debt', '44444444-4444-4444-4444-222222222222', 2, 0.35);

-- Department Objectives (SQA)
INSERT INTO objectives (id, sheet_id, title, owner_user_id, sort_order, computed_progress) VALUES
('77777777-7777-7777-7777-555555555555', '66666666-6666-6666-6666-333333333333', 'Achieve 95% Test Coverage', '44444444-4444-4444-4444-333333333333', 1, 0.80),
('77777777-7777-7777-7777-666666666666', '66666666-6666-6666-6666-333333333333', 'Reduce Bug Escape Rate', '44444444-4444-4444-4444-333333333333', 2, 0.55);

-- Personal Objectives (Admin)
INSERT INTO objectives (id, sheet_id, title, owner_user_id, sort_order, computed_progress) VALUES
('77777777-7777-7777-7777-777777777777', '66666666-6666-6666-6666-444444444444', 'Improve Leadership Skills', '44444444-4444-4444-4444-111111111111', 1, 0.50),
('77777777-7777-7777-7777-888888888888', '66666666-6666-6666-6666-444444444444', 'Complete Strategic Planning', '44444444-4444-4444-4444-111111111111', 2, 0.30);

-- 9. Key Results
-- Company KRs for Objective 1 (Increase Revenue)
INSERT INTO key_results (id, objective_id, title, start_value, target_value, current_value, computed_progress, owner_user_id, confidence_level, deadline, aligned_projects, comments, sort_order) VALUES
('88888888-8888-8888-8888-111111111111', '77777777-7777-7777-7777-111111111111', 'Close 50 new enterprise deals', 0, 50, 22, 0.44, '44444444-4444-4444-4444-111111111111', 'ON_TRACK', '2024-03-31', 'Sales CRM Project', 'Good progress, Q1 pipeline looks healthy', 1),
('88888888-8888-8888-8888-222222222222', '77777777-7777-7777-7777-111111111111', 'Increase MRR to $500K', 350000, 500000, 420000, 0.47, '44444444-4444-4444-4444-111111111111', 'AT_RISK', '2024-03-31', NULL, 'Need to accelerate in March', 2);

-- Company KRs for Objective 2 (Customer Satisfaction)
INSERT INTO key_results (id, objective_id, title, start_value, target_value, current_value, computed_progress, owner_user_id, confidence_level, deadline, aligned_projects, comments, sort_order) VALUES
('88888888-8888-8888-8888-333333333333', '77777777-7777-7777-7777-222222222222', 'Achieve NPS score of 60+', 45, 60, 55, 0.67, '44444444-4444-4444-4444-111111111111', 'ON_TRACK', '2024-03-31', 'Customer Success Initiative', NULL, 1),
('88888888-8888-8888-8888-444444444444', '77777777-7777-7777-7777-222222222222', 'Reduce churn rate to <5%', 8, 5, 6, 0.67, '44444444-4444-4444-4444-111111111111', 'ON_TRACK', '2024-03-31', NULL, 'Churn prevention program working well', 2);

-- Team KRs for Objective 1 (Deliver MVP)
INSERT INTO key_results (id, objective_id, title, start_value, target_value, current_value, computed_progress, owner_user_id, confidence_level, deadline, aligned_projects, comments, sort_order) VALUES
('88888888-8888-8888-8888-555555555555', '77777777-7777-7777-7777-333333333333', 'Complete 80% of sprint stories', 0, 80, 55, 0.69, '44444444-4444-4444-4444-222222222222', 'ON_TRACK', '2024-03-15', 'Sprint Board', NULL, 1),
('88888888-8888-8888-8888-666666666666', '77777777-7777-7777-7777-333333333333', 'Pass all integration tests', 0, 100, 45, 0.45, '44444444-4444-4444-4444-222222222222', 'AT_RISK', '2024-03-20', 'CI/CD Pipeline', 'Some flaky tests need fixing', 2);

-- Team KRs for Objective 2 (Reduce Technical Debt)
INSERT INTO key_results (id, objective_id, title, start_value, target_value, current_value, computed_progress, owner_user_id, confidence_level, deadline, aligned_projects, comments, sort_order) VALUES
('88888888-8888-8888-8888-777777777777', '77777777-7777-7777-7777-444444444444', 'Refactor 20 legacy modules', 0, 20, 7, 0.35, '44444444-4444-4444-4444-222222222222', 'OFF_TRACK', '2024-03-31', 'Tech Debt Tracker', 'Need more resources', 1);

-- SQA KRs for Objective 1 (Test Coverage)
INSERT INTO key_results (id, objective_id, title, start_value, target_value, current_value, computed_progress, owner_user_id, confidence_level, deadline, aligned_projects, comments, sort_order) VALUES
('88888888-8888-8888-8888-888888888888', '77777777-7777-7777-7777-555555555555', 'Increase unit test coverage to 95%', 70, 95, 90, 0.80, '44444444-4444-4444-4444-333333333333', 'ON_TRACK', '2024-03-31', NULL, 'Great progress!', 1),
('88888888-8888-8888-8888-999999999999', '77777777-7777-7777-7777-555555555555', 'Add E2E tests for critical flows', 0, 15, 12, 0.80, '44444444-4444-4444-4444-444444444444', 'ON_TRACK', '2024-03-25', 'Playwright Project', NULL, 2);

-- SQA KRs for Objective 2 (Bug Escape Rate)
INSERT INTO key_results (id, objective_id, title, start_value, target_value, current_value, computed_progress, owner_user_id, confidence_level, deadline, aligned_projects, comments, sort_order) VALUES
('88888888-8888-8888-8888-aaaaaaaaaaaa', '77777777-7777-7777-7777-666666666666', 'Reduce P1 bugs escaping to prod', 10, 2, 5, 0.63, '44444444-4444-4444-4444-333333333333', 'AT_RISK', '2024-03-31', 'Bug Tracking', 'Need to improve code review process', 1),
('88888888-8888-8888-8888-bbbbbbbbbbbb', '77777777-7777-7777-7777-666666666666', 'Implement automated regression suite', 0, 100, 45, 0.45, '44444444-4444-4444-4444-444444444444', 'AT_RISK', '2024-03-31', NULL, NULL, 2);

-- Personal KRs for Objective 1 (Leadership Skills)
INSERT INTO key_results (id, objective_id, title, start_value, target_value, current_value, computed_progress, owner_user_id, confidence_level, deadline, aligned_projects, comments, sort_order) VALUES
('88888888-8888-8888-8888-cccccccccccc', '77777777-7777-7777-7777-777777777777', 'Complete 3 leadership courses', 0, 3, 2, 0.67, '44444444-4444-4444-4444-111111111111', 'ON_TRACK', '2024-03-31', 'Learning Portal', 'Good progress on courses', 1),
('88888888-8888-8888-8888-dddddddddddd', '77777777-7777-7777-7777-777777777777', 'Conduct 10 1-on-1 sessions', 0, 10, 3, 0.30, '44444444-4444-4444-4444-111111111111', 'AT_RISK', '2024-03-31', NULL, 'Need to schedule more sessions', 2);

-- Personal KRs for Objective 2 (Strategic Planning)
INSERT INTO key_results (id, objective_id, title, start_value, target_value, current_value, computed_progress, owner_user_id, confidence_level, deadline, aligned_projects, comments, sort_order) VALUES
('88888888-8888-8888-8888-eeeeeeeeeeee', '77777777-7777-7777-7777-888888888888', 'Draft Q2 strategic plan', 0, 100, 25, 0.25, '44444444-4444-4444-4444-111111111111', 'OFF_TRACK', '2024-03-15', 'Strategy Docs', 'Behind schedule', 1),
('88888888-8888-8888-8888-ffffffffffff', '77777777-7777-7777-7777-888888888888', 'Present to board', 0, 1, 0, 0.00, '44444444-4444-4444-4444-111111111111', 'AT_RISK', '2024-03-31', NULL, 'Scheduled for end of quarter', 2);

-- Personal Sheet (QA Member - for testing DEPARTMENT_ADMIN permissions)
INSERT INTO okr_sheets (id, cycle_id, scope_type, scope_id) VALUES
('66666666-6666-6666-6666-555555555555', '55555555-5555-5555-5555-555555555555', 'PERSONAL', '44444444-4444-4444-4444-444444444444');

-- Personal Objectives (QA Member)
INSERT INTO objectives (id, sheet_id, title, owner_user_id, sort_order, computed_progress) VALUES
('77777777-7777-7777-7777-999999999999', '66666666-6666-6666-6666-555555555555', 'Improve Testing Skills', '44444444-4444-4444-4444-444444444444', 1, 0.40);

-- Personal KRs for QA Member
INSERT INTO key_results (id, objective_id, title, start_value, target_value, current_value, computed_progress, owner_user_id, confidence_level, deadline, aligned_projects, comments, sort_order) VALUES
('88888888-8888-8888-8888-111111111122', '77777777-7777-7777-7777-999999999999', 'Complete ISTQB certification', 0, 100, 40, 0.40, '44444444-4444-4444-4444-444444444444', 'ON_TRACK', '2024-03-31', 'Learning Portal', 'Studying for exam', 1),
('88888888-8888-8888-8888-222222222233', '77777777-7777-7777-7777-999999999999', 'Automate 10 test cases', 0, 10, 4, 0.40, '44444444-4444-4444-4444-444444444444', 'AT_RISK', '2024-03-31', NULL, 'Need to speed up', 2);

-- Data is ready for Phase A verification
