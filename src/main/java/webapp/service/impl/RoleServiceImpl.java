package webapp.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import webapp.data.entity.Role;
import webapp.data.exception.RoleNotFoundException;
import webapp.sec.repository.RoleRepository;
import webapp.service.RoleService;


@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository repository;

    @Override
    public Role findByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new RoleNotFoundException(name));
    }
}
