package webapp.service;

import webapp.data.entity.Role;

public interface RoleService {
    Role findByName(String name);
}
