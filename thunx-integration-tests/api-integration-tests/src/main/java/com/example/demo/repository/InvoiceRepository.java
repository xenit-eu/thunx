package com.example.demo.repository;

import com.example.demo.model.Company;
import com.example.demo.model.Invoice;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface InvoiceRepository extends JpaRepository<Invoice, UUID>, QuerydslPredicateExecutor<Invoice> {

}
