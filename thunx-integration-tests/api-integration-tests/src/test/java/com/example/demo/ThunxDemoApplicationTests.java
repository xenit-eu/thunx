package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.contentgrid.thunx.encoding.json.ExpressionJsonConverter;
import com.contentgrid.thunx.predicates.model.Comparison;
import com.contentgrid.thunx.predicates.model.Scalar;
import com.contentgrid.thunx.predicates.model.SymbolicReference;
import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.example.demo.model.Company;
import com.example.demo.model.Invoice;
import com.example.demo.repository.CompanyRepository;
import com.example.demo.repository.InvoiceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import javax.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ThunxDemoApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    CompanyRepository companies;

    @Autowired
    InvoiceRepository invoices;

    // invoice.counterparty.vat = BE0887582365
    final Comparison POLICY_INVOICES = Comparison.areEqual(
            SymbolicReference.of("entity", path -> path.string("counterparty").string("vat")),
            Scalar.of("BE0887582365"));

    final Comparison POLICY_COMPANY = Comparison.areEqual(
            SymbolicReference.of("entity", path -> path.string("vat")),
            Scalar.of("BE0887582365"));

    ThunxDemoApplicationTests() {
    }

    @BeforeEach
    void setupTestData() {
        // class is annotated with @Transactional, any change gets rolled back at the end of every test
        var xenit = companies.save(new Company(null, "XeniT", "BE0887582365"));
        var inbev = companies.save(new Company(null, "AB InBev", "BE0417497106"));

        invoices.saveAll(List.of(
                new Invoice(null, "I-2022-0001", xenit),
                new Invoice(null, "I-2022-0002", inbev)
        ));
    }

    @Nested
    class CollectionResource {

        @Test
        void listInvoices_without_abacContextHeader() throws Exception {
            mockMvc.perform(get("/invoices")
                            .contentType("application/json"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.invoices.length()").value(2));
        }

        @Test
        void listInvoices_appliesFilter_andReturn_http200_ok() throws Exception {
            mockMvc.perform(get("/invoices")
                            .header("X-ABAC-Context", headerEncode(POLICY_INVOICES))
                            .contentType("application/json"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.invoices.length()").value(1))
                    .andExpect(jsonPath("$._embedded.invoices[0].number").value("I-2022-0001"));
        }

        @Test
        void createInvoice_policyOk_shouldReturn_http201_created() throws Exception {
            mockMvc.perform(post("/invoices")
                            .header("X-ABAC-Context", headerEncode(POLICY_INVOICES))
                            .content("{ \"number\": \"I-2022-0003\", \"counterparty\": \"/companies/BE0887582365\" }")
                            .contentType("application/json"))
                    .andDo(print())
                    .andExpect(status().isCreated());
        }

        @Test
        void createInvoice_policyNotOk_shouldReturn_http404_notFound() throws Exception {
            mockMvc.perform(post("/invoices")
                            .header("X-ABAC-Context", headerEncode(POLICY_INVOICES))
                            .content("{ \"number\": \"I-2022-0003\", \"counterparty\": \"/companies/BE0417497106\" }")
                            .contentType("application/json"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @Disabled("replaces test above, once agreement is made")
        void createInvoice_policyNotOk_shouldReturn_http403_forbidden() throws Exception {
            mockMvc.perform(post("/invoices")
                            .header("X-ABAC-Context", headerEncode(POLICY_INVOICES))
                            .content("{ \"number\": \"I-2022-0003\", \"counterparty\": \"/companies/BE0417497106\" }")
                            .contentType("application/json"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class ItemResource {
        @Test
        void getInvoices_without_abacContextHeader() throws Exception {
            var invoiceId = invoices.findAll().get(0).getId();

            mockMvc.perform(get("/invoices/" + invoiceId)
                            .contentType("application/json"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.number").value("I-2022-0001"));
        }

        @Test
        void getInvoices_withMatching_abacContextHeader() throws Exception {
            var invoiceId = invoices.findAll().get(0).getId();

            mockMvc.perform(get("/invoices/" + invoiceId)
                            .header("X-ABAC-Context", headerEncode(POLICY_INVOICES))
                            .contentType("application/json"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.number").value("I-2022-0001"));
        }

        @Test
        void getInvoices_withMismatching_abacContextHeader() throws Exception {
            var invoiceId = invoices.findAll().get(1).getId();

            mockMvc.perform(get("/invoices/" + invoiceId)
                            .header("X-ABAC-Context", headerEncode(POLICY_INVOICES))
                            .contentType("application/json"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class AssociationResource {

    }

    private static String headerEncode(ThunkExpression<Boolean> expression) {
        var bytes = new ExpressionJsonConverter().encode(expression).getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(bytes);
    }

    static class InvoicePostModel {
        String number;
        String counterparty;
    }
}
