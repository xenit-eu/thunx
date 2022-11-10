package com.example.demo.repository;

import com.example.demo.model.Company;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface CompanyRepository extends JpaRepository<Company, UUID>, QuerydslPredicateExecutor<Company> {

    Optional<Company> findFirstByName(String name);

    Optional<Company> findByVat(String vat);
}
