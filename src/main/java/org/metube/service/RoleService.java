package org.metube.service;

import org.metube.entity.Role;
import org.metube.entity.User;

import java.util.List;

public interface RoleService {

    Role findByName(String name);

    List<Role> findAll();

    Role findById(Integer roleId);
}
