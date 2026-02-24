package com.farmiq.services;

import com.farmiq.dao.RoleDAO;
import com.farmiq.dao.PermissionDAO;
import com.farmiq.models.Role;
import com.farmiq.models.Permission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public class RoleService {
    private static final Logger logger = LogManager.getLogger(RoleService.class);
    private final RoleDAO roleDAO;
    private final PermissionDAO permissionDAO;

    public RoleService() {
        this.roleDAO = new RoleDAO();
        this.permissionDAO = new PermissionDAO();
    }

    public List<Role> getAllRoles() throws SQLException {
        List<Role> roles = roleDAO.findAll();
        for (Role role : roles) role.setPermissions(roleDAO.loadRolePermissions(role.getId()));
        return roles;
    }

    public Role getRoleById(int id) throws SQLException {
        Role role = roleDAO.findById(id);
        if (role != null) role.setPermissions(roleDAO.loadRolePermissions(id));
        return role;
    }

    public Role getRoleByName(String name) throws SQLException {
        Role role = roleDAO.findByName(name);
        if (role != null) role.setPermissions(roleDAO.loadRolePermissions(role.getId()));
        return role;
    }

    public boolean assignPermissionToRole(int roleId, int permissionId) throws SQLException {
        return roleDAO.assignPermission(roleId, permissionId);
    }

    public boolean removePermissionFromRole(int roleId, int permissionId) throws SQLException {
        return roleDAO.removePermission(roleId, permissionId);
    }

    public List<Permission> getRolePermissions(int roleId) throws SQLException {
        return roleDAO.loadRolePermissions(roleId);
    }
}
