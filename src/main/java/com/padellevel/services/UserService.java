package com.padellevel.services;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.padellevel.data.Role;
import com.padellevel.data.User;
import com.padellevel.data.UserRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private List<User> users;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public Optional<User> get(Long id) {
        return repository.findById(id);
    }

    public User update(User entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<User> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<User> list(Pageable pageable, Specification<User> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public User save(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setHashedPassword(passwordEncoder.encode(user.getPassword()));
            user.setPassword(null); // Clear the transient password field after encoding
        }
        return repository.save(user);
    }

    public void delete(User user) {
        repository.delete(user);
    }

    public List<User> searchUsers(String searchTerm) {
        return repository.searchUsers(searchTerm);
    }

    public List<User> findByRole(Role role) {
        return repository.findAll().stream()
                .filter(u -> u.getRoles().contains(role))
                .collect(Collectors.toList());
    }

}
