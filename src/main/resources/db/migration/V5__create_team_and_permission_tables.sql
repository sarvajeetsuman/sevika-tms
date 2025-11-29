-- V5: Create team management and permission tables

-- Create teams table
CREATE TABLE teams (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    owner_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_teams_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_teams_owner_id ON teams(owner_id);
CREATE INDEX idx_teams_name ON teams(name);

-- Create team_members table (junction table)
CREATE TABLE team_members (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER', 'VIEWER')),
    joined_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_team_members_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_team_members_team_user UNIQUE (team_id, user_id)
);

CREATE INDEX idx_team_members_team_id ON team_members(team_id);
CREATE INDEX idx_team_members_user_id ON team_members(user_id);
CREATE INDEX idx_team_members_role ON team_members(role);

-- Create project_permissions table
CREATE TABLE project_permissions (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    team_id UUID,
    user_id UUID,
    permission VARCHAR(20) NOT NULL CHECK (permission IN ('READ', 'WRITE', 'DELETE', 'ADMIN')),
    granted_at TIMESTAMP NOT NULL,
    granted_by UUID NOT NULL,
    CONSTRAINT fk_project_permissions_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_permissions_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_permissions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_permissions_granter FOREIGN KEY (granted_by) REFERENCES users(id),
    CONSTRAINT uk_project_permissions UNIQUE (project_id, team_id, user_id),
    CONSTRAINT chk_project_permissions_target CHECK (
        (team_id IS NOT NULL AND user_id IS NULL) OR 
        (team_id IS NULL AND user_id IS NOT NULL)
    )
);

CREATE INDEX idx_project_permissions_project_id ON project_permissions(project_id);
CREATE INDEX idx_project_permissions_team_id ON project_permissions(team_id);
CREATE INDEX idx_project_permissions_user_id ON project_permissions(user_id);

-- Create task_permissions table
CREATE TABLE task_permissions (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL,
    team_id UUID,
    user_id UUID,
    permission VARCHAR(20) NOT NULL CHECK (permission IN ('READ', 'WRITE', 'DELETE', 'ADMIN')),
    granted_at TIMESTAMP NOT NULL,
    granted_by UUID NOT NULL,
    CONSTRAINT fk_task_permissions_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_permissions_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_permissions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_permissions_granter FOREIGN KEY (granted_by) REFERENCES users(id),
    CONSTRAINT uk_task_permissions UNIQUE (task_id, team_id, user_id),
    CONSTRAINT chk_task_permissions_target CHECK (
        (team_id IS NOT NULL AND user_id IS NULL) OR 
        (team_id IS NULL AND user_id IS NOT NULL)
    )
);

CREATE INDEX idx_task_permissions_task_id ON task_permissions(task_id);
CREATE INDEX idx_task_permissions_team_id ON task_permissions(team_id);
CREATE INDEX idx_task_permissions_user_id ON task_permissions(user_id);
