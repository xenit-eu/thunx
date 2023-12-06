package com.contentgrid.thunx.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.contentgrid.spring.test.fixture.invoicing.InvoicingApplication;
import com.contentgrid.spring.test.fixture.invoicing.model.Customer;
import com.contentgrid.spring.test.fixture.invoicing.model.Invoice;
import com.contentgrid.spring.test.fixture.invoicing.model.Order;
import com.contentgrid.spring.test.fixture.invoicing.model.PromotionCampaign;
import com.contentgrid.spring.test.fixture.invoicing.model.ShippingAddress;
import com.contentgrid.spring.test.fixture.invoicing.repository.CustomerRepository;
import com.contentgrid.spring.test.fixture.invoicing.repository.InvoiceRepository;
import com.contentgrid.spring.test.fixture.invoicing.repository.OrderRepository;
import com.contentgrid.spring.test.fixture.invoicing.repository.PromotionCampaignRepository;
import com.contentgrid.spring.test.fixture.invoicing.repository.ShippingAddressRepository;
import com.contentgrid.thunx.encoding.json.ExpressionJsonConverter;
import com.contentgrid.thunx.predicates.model.BooleanOperation;
import com.contentgrid.thunx.predicates.model.Comparison;
import com.contentgrid.thunx.predicates.model.LogicalOperation;
import com.contentgrid.thunx.predicates.model.Scalar;
import com.contentgrid.thunx.predicates.model.SymbolicReference;
import com.contentgrid.thunx.predicates.model.ThunkExpression;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@Transactional
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@SpringBootTest(classes = InvoicingApplication.class)
class ThunxDemoApplicationTests {

    static final String INVOICE_1 = "I-2022-0001";
    static final String INVOICE_2 = "I-2022-0002";

    static final String ORG_XENIT_VAT = "BE0887582365";
    static final String ORG_INBEV_VAT = "BE0417497106";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    CustomerRepository customers;

    @Autowired
    InvoiceRepository invoices;

    @Autowired
    OrderRepository orders;

    @Autowired
    ShippingAddressRepository shippingAddresses;

    @Autowired
    PromotionCampaignRepository promos;

    // invoice.customer.vat = BE0887582365
    final Comparison POLICY_INVOICES_XENIT = Comparison.areEqual(
            SymbolicReference.of("entity", path -> path.string("counterparty").string("vat")),
            Scalar.of("BE0887582365"));

    // invoice.draft = true
    final Comparison POLICY_INVOICES_DRAFT = Comparison.areEqual(
            SymbolicReference.of("entity", path -> path.string("draft")),
            Scalar.of(true));

    final Comparison POLICY_CUSTOMERS = Comparison.areEqual(
            SymbolicReference.of("entity", path -> path.string("vat")),
            Scalar.of("BE0887582365"));

    // See ACC-554 - we cannot express this policy yet
    // everyone can create an order,
    // but only customer.name='AB Inbev' can add promo GORILLA
    final BooleanOperation POLICY_CREATE_ORDER = LogicalOperation.disjunction(

    );

    final Comparison POLICY_ORDERS_XENIT = Comparison.areEqual(
            SymbolicReference.of("entity", path -> path.string("customer").string("vat")),
            Scalar.of("BE0887582365")
    );

    static String PROMO_XMAS, PROMO_CYBER, PROMO_GORILLA;

    UUID ORDER_1;
    UUID ORDER_3;

    @BeforeEach
    void setupTestData() {

        PROMO_XMAS = promos.save(new PromotionCampaign("XMAS", "Happy Holidays")).getPromoCode();
        PROMO_GORILLA = promos.save(new PromotionCampaign("GORILLA", "Huge Customers")).getPromoCode();

        var address_northPole = shippingAddresses.save(new ShippingAddress("123 Elf Road", "88888", "North Pole"));

        var xenit = customers.save(
                new Customer(null, "XeniT", ORG_XENIT_VAT, null, null, null, new HashSet<>(), new HashSet<>()));
        var inbev = customers.save(
                new Customer(null, "AB InBev", ORG_INBEV_VAT, null, null, null, new HashSet<>(), new HashSet<>()));

        ORDER_1 = orders.save(new Order(xenit, address_northPole, Set.of())).getId();
        var order2 = orders.save(new Order(xenit));
        var order3 = orders.save(new Order(inbev));
        ORDER_3 = order3.getId();

        invoices.saveAll(List.of(
                new Invoice(INVOICE_1, true, false, xenit,
                        new HashSet<>(List.of(orders.getReferenceById(ORDER_1), order2))),
                new Invoice(INVOICE_2, false, true, inbev, new HashSet<>(List.of(order3)))
        ));
    }

    @Nested
    class CollectionResource {

        @Nested
        @DisplayName("GET /{repository}/")
        class Get {

            @Test
            void listInvoices_noPolicy() throws Exception {
                mockMvc.perform(get("/invoices")
                                .contentType("application/json"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$._embedded.item.length()").value(2));
            }

            @Test
            void listInvoices_policyOk_shouldReturn_http200_ok() throws Exception {
                mockMvc.perform(get("/invoices")
                                .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                .contentType("application/json"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$._embedded.item.length()").value(1))
                        .andExpect(jsonPath("$._embedded.item[0].number").value(INVOICE_1));
            }

            @Test
            void listInvoices_policyOk_withMatchingFilter_shouldReturn_http200_ok() throws Exception {
                mockMvc.perform(get("/invoices?number={number}", INVOICE_1)
                                .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                .contentType("application/json"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$._embedded.item.length()").value(1))
                        .andExpect(jsonPath("$._embedded.item[0].number").value(INVOICE_1));
            }

            @Test
            void listInvoices_policyOk_withNonMatchingFilter_shouldReturn_http200_ok() throws Exception {
                mockMvc.perform(get("/invoices?number={number}", INVOICE_2)
                                .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                .contentType("application/json"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$._embedded.item.length()").value(0));
            }
        }

        @Nested
        @DisplayName("HEAD /{repository}/")
        class Head {

            @Test
            void checkInvoiceCollection_shouldReturn_http204_noContent() throws Exception {
                mockMvc.perform(head("/invoices")
                                .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                .contentType("application/json"))
                        .andExpect(status().isNoContent());
            }
        }

        @Nested
        @DisplayName("POST /{repository}/")
        class Post {

            @Test
            void createInvoice_policyOk_shouldReturn_http201_created() throws Exception {
                var customerId = customers.findByVat(ORG_XENIT_VAT).orElseThrow().getId();
                mockMvc.perform(post("/invoices")
                                .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                .content("""
                                        {
                                            "number": "I-2022-0003",
                                            "counterparty": "/customers/%s"
                                        }
                                        """.formatted(customerId))
                                .contentType("application/json"))
                        .andExpect(status().isCreated());
            }

            @Test
            void createInvoice_policyFail_shouldReturn_http404_notFound() throws Exception {
                var customerId = customers.findByVat(ORG_INBEV_VAT).orElseThrow().getId();
                mockMvc.perform(post("/invoices")
                                .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                .content("""
                                        {
                                            "number": "I-2022-0003",
                                            "counterparty": "/customers/%s"
                                        }
                                        """.formatted(customerId))
                                .contentType("application/json"))
                        .andExpect(status().isNotFound());
            }
        }
    }

    @Nested
    class ItemResource {

        @Nested
        @DisplayName("GET /{repository}/{id}")
        class Get {

            @Test
            void getInvoice_policyNone_shouldReturn_http200_ok() throws Exception {

                mockMvc.perform(get("/invoices/" + invoiceIdByNumber(INVOICE_1))
                                .contentType("application/json"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.number").value(INVOICE_1));
            }

            @Test
            void getInvoice_policyOk_shouldReturn_http200_ok() throws Exception {
                mockMvc.perform(get("/invoices/" + invoiceIdByNumber(INVOICE_1))
                                .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                .contentType("application/json"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.number").value(INVOICE_1));
            }

            /**
             * This tests that a GET on an individual resource has the query parameters that match filter params ignored
             */
            @Test
            void getInvoice_policyOk_withFilter_ignored_shouldReturn_http200_ok() throws Exception {
                mockMvc.perform(get("/invoices/{id}?number={number}", invoiceIdByNumber(INVOICE_1), INVOICE_2)
                                .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                .contentType("application/json"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.number").value(INVOICE_1));
            }

            @Test
            void getInvoice_policyFail_shouldReturn_http404_notFound() throws Exception {
                mockMvc.perform(get("/invoices/" + invoiceIdByNumber(INVOICE_2))
                                .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                .contentType("application/json"))
                        .andExpect(status().isNotFound());
            }
        }

        @Nested
        @DisplayName("HEAD /{repository}/{id}")
        class Head {

            @Test
            void headInvoice_policyOk_shouldReturn_http204_noContent() throws Exception {
                mockMvc.perform(head("/invoices/" + invoiceIdByNumber(INVOICE_1))
                                .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                .contentType("application/json"))
                        .andExpect(status().isNoContent());
            }

            @Test
            void headInvoice_policyFail_shouldReturn_http404_notFound() throws Exception {

                mockMvc.perform(head("/invoices/" + invoiceIdByNumber(INVOICE_2))
                                .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                .contentType("application/json"))
                        .andExpect(status().isNotFound());
            }
        }

        @Nested
        @DisplayName("PUT /{repository}/{id}")
        class Put {

            @Test
            void putInvoice_policyOk_shouldReturn_http204_ok() throws Exception {
                mockMvc.perform(put("/invoices/" + invoiceIdByNumber(INVOICE_1))
                                .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                .contentType("application/json")
                                .content("""
                                        {
                                            "number": "%s",
                                            "paid": true
                                        }
                                        """.formatted(INVOICE_1)))
                        .andExpect(status().isNoContent());
            }

            @Test
            void putInvoice_policyFailsPreSave_shouldReturn_http404_notFound() throws Exception {
                mockMvc.perform(put("/invoices/" + invoiceIdByNumber(INVOICE_2))
                                .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                .contentType("application/json")
                                .content("""
                                        {
                                            "number": "%s",
                                            "paid": true,
                                            "counterparty": "/customers/%s"
                                        }
                                        """.formatted(INVOICE_2, customerIdByVat(ORG_INBEV_VAT))))
                        .andExpect(status().isNotFound());
            }

            @Test
            void putInvoice_policyFailsPostSave_shouldReturn_http404_notFound() throws Exception {
                // custom policy: user can see all paid invoices
                var policyPaidInvoices = Comparison.areEqual(
                        SymbolicReference.of("entity", path -> path.string("paid")),
                        Scalar.of(true));

                // check user has access to invoice-2
                mockMvc.perform(get("/invoices/" + invoiceIdByNumber(INVOICE_2))
                                .header("X-ABAC-Context", headerEncode(policyPaidInvoices))
                                .accept("application/json"))
                        .andExpect(status().isOk());

                mockMvc.perform(put("/invoices/" + invoiceIdByNumber(INVOICE_2))
                                .header("X-ABAC-Context", headerEncode(policyPaidInvoices))
                                .contentType("application/json")
                                .content("""
                                         {
                                            "number": "%s",
                                            "paid": false,
                                            "counterparty": "/customers/%s"
                                        }
                                        """.formatted(INVOICE_2, customerIdByVat(ORG_INBEV_VAT))))
                        .andExpect(status().isNotFound());
            }
        }

        @Nested
        @DisplayName("PATCH /{repository}/{id}")
        class Patch {

            @Test
            void patchInvoice_policyOk_shouldReturn_http204_ok() throws Exception {
                mockMvc.perform(patch("/invoices/" + invoiceIdByNumber(INVOICE_1))
                                .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                .contentType("application/json")
                                .content("""
                                        {
                                            "paid": true
                                        }
                                        """))
                        .andExpect(status().isNoContent());
            }

            @Test
            void patchInvoice_policyFailsPreSave_shouldReturn_http404_notFound() throws Exception {
                mockMvc.perform(patch("/invoices/" + invoiceIdByNumber(INVOICE_2))
                                .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                .contentType("application/json")
                                .content("""
                                        {
                                            "paid": true
                                        }
                                        """))
                        .andExpect(status().isNotFound());
            }

            @Test
            void patchInvoice_policyFailsPostSave_shouldReturn_http404_notFound() throws Exception {
                // custom policy: user can see all paid invoices
                var policyPaidInvoices = Comparison.areEqual(
                        SymbolicReference.of("entity", path -> path.string("paid")),
                        Scalar.of(true));

                // check user has access to invoice-2
                mockMvc.perform(get("/invoices/" + invoiceIdByNumber(INVOICE_2))
                                .header("X-ABAC-Context", headerEncode(policyPaidInvoices))
                                .accept("application/json"))
                        .andExpect(status().isOk());

                mockMvc.perform(patch("/invoices/" + invoiceIdByNumber(INVOICE_2))
                                .header("X-ABAC-Context", headerEncode(policyPaidInvoices))
                                .contentType("application/json")
                                .content("""
                                        {
                                            "number": "%s",
                                            "paid": false
                                        }
                                        """.formatted(INVOICE_2)))
                        .andExpect(status().isNotFound());
            }
        }

        @Nested
        @DisplayName("DELETE /{repository}/{id}")
        class Delete {

            @Test
            void deleteInvoice_policyOk_shouldReturn_http204_ok() throws Exception {
                mockMvc.perform(delete("/invoices/" + invoiceIdByNumber(INVOICE_1))
                                .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_DRAFT)))
                        .andExpect(status().isNoContent());

                assertThat(invoices.findByNumber(INVOICE_1)).isEmpty();
            }

            @Test
            void deleteInvoice_policyFail_shouldReturn_http404_notFound() throws Exception {
                mockMvc.perform(delete("/invoices/" + invoiceIdByNumber(INVOICE_2))
                                .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT)))
                        .andExpect(status().isNotFound());

                assertThat(invoices.findByNumber(INVOICE_2)).isPresent();
            }
        }
    }

    @Nested
    class AssociationResource {

        @Nested
        @DisplayName("GET /{repository}/{id}/{property}")
        class Get {

            @Nested
            class ToOne {

                @Test
                void getInvoiceCustomer_policyOk_shouldReturn_http302_redirect() throws Exception {

                    mockMvc.perform(get("/invoices/" + invoiceIdByNumber(INVOICE_1) + "/counterparty")
                                    .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                    .accept("application/json"))
                            .andExpect(status().isFound())
                            .andExpect(header().string(HttpHeaders.LOCATION,
                                    endsWith("/customers/" + customerIdByVat(ORG_XENIT_VAT))));
                }


                @Test
                void getInvoiceCustomer_policyFail_shouldReturn_http404_notFound() throws Exception {

                    mockMvc.perform(get("/invoices/" + invoiceIdByNumber(INVOICE_2) + "/counterparty")
                                    .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                    .accept("application/json"))
                            .andExpect(status().isNotFound());
                }
            }

            @Nested
            class ToMany {

                @Test
                void getInvoicesForCustomer_policyOk_shouldReturn_http302() throws Exception {

                    mockMvc.perform(get("/customers/" + customerIdByVat(ORG_XENIT_VAT) + "/invoices")
                                    .header("X-ABAC-Context", headerEncode(POLICY_CUSTOMERS))
                                    .accept("application/json"))
                            .andExpect(status().isFound());
                }
            }
        }

        @Nested
        @DisplayName("PUT /{repository}/{id}/{property}")
        class Put {

            @Nested
            class ToOne {

                @Test
                void putJson_policyOk_shouldReturn_http204() throws Exception {
                    // policy can access all _UN_paid invoices
                    var policyUnpaidInvoices = Comparison.areEqual(
                            SymbolicReference.of("entity", path -> path.string("paid")),
                            Scalar.of(false));

                    // fictive example: fix the customer
                    var correctCustomerId = customers.findByVat(ORG_INBEV_VAT).orElseThrow().getId();
                    mockMvc.perform(put("/invoices/" + invoiceIdByNumber(INVOICE_1) + "/counterparty")
                                    .header("X-ABAC-Context", headerEncode(policyUnpaidInvoices))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content("""
                                            {
                                                "_links": {
                                                    "customer" : {
                                                        "href": "/customers/%s"
                                                    }
                                                }
                                            }
                                            """.formatted(correctCustomerId)))
                            .andExpect(status().isNoContent());
                }

                @Test
                void putJson_policyFail_preSave_shouldReturn_http404() throws Exception {
                    // user does not have access to invoice pre-update, should fail
                    var customerXenit = customers.findByVat(ORG_XENIT_VAT).orElseThrow().getId();
                    mockMvc.perform(put("/invoices/" + invoiceIdByNumber(INVOICE_2) + "/counterparty")
                                    .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content("""
                                            {
                                                "_links": {
                                                    "customer" : {
                                                        "href": "/customers/%s"
                                                    }
                                                }
                                            }
                                            """.formatted(customerXenit)))
                            .andExpect(status().isNotFound());
                }

                @Test
                void putJson_policyFail_postSave_shouldReturn_http404() throws Exception {
                    // fictive example:
                    // invoice has customer-1 - access OK
                    // try to link with customer-2, but after update policies should fail
                    var customerInbev = customers.findByVat(ORG_INBEV_VAT).orElseThrow().getId();
                    mockMvc.perform(put("/invoices/" + invoiceIdByNumber(INVOICE_1) + "/counterparty")
                                    .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                    .accept(MediaType.APPLICATION_JSON)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                            {
                                                "_links": {
                                                    "customer" : {
                                                        "href": "/customers/%s"
                                                    }
                                                }
                                            }
                                            """.formatted(customerInbev)))
                            .andExpect(status().isNotFound());
                }
            }

            @Nested
            class ToMany {

                @Test
                void putJson_policyOk_shouldReplaceLinksAndReturn_http204_noContent() throws Exception {
                    var xenit = customers.findByVat(ORG_XENIT_VAT).orElseThrow();
                    var newOrderId = orders.save(new Order(xenit)).getId();
                    var invoiceNumber = invoiceIdByNumber(INVOICE_1);

                    // try to add order to invoice using PUT - should fail
                    mockMvc.perform(put("/invoices/%s/orders".formatted(invoiceNumber))
                                    .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_DRAFT))
                                    .accept(MediaType.APPLICATION_JSON)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                            {
                                                "_links": {
                                                    "orders" : {
                                                        "href": "/orders/%s"
                                                    }
                                                }
                                            }
                                            """.formatted(newOrderId)))
                            .andExpect(status().isNoContent());

                    // assert orders collection has been replaced
                    assertThat(invoices.findById(invoiceNumber)).hasValueSatisfying(invoice -> {
                        assertThat(invoice.getOrders()).singleElement().satisfies(order -> {
                            assertThat(order.getId()).isEqualTo(newOrderId);
                        });
                    });
                }

                @Test
                void putJson_policyFail_preSave_shouldReturn_http404_notFound() throws Exception {

                    // can only update invoices with .draft = true
                    var draftInvoicePolicy = Comparison.areEqual(
                            SymbolicReference.of("entity", path -> path.string("draft")),
                            Scalar.of(true));

                    var xenit = customers.findByVat(ORG_XENIT_VAT).orElseThrow();
                    var newOrderId = orders.save(new Order(xenit)).getId();

                    var invoice = invoices.findByNumber(INVOICE_2).orElseThrow();
                    var invoiceNumber = invoice.getId();
                    var oldOrderId = invoice.getOrders().stream().findFirst().map(Order::getId).orElseThrow();

                    // try to add order to invoice using PUT - should fail
                    mockMvc.perform(put("/invoices/%s/orders".formatted(invoiceNumber))
                                    .header("X-ABAC-Context", headerEncode(draftInvoicePolicy))
                                    .accept(MediaType.APPLICATION_JSON)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                            {
                                                "_links": {
                                                    "orders" : {
                                                        "href": "/orders/%s"
                                                    }
                                                }
                                            }
                                            """.formatted(newOrderId)))
                            .andExpect(status().isNotFound());

                    // assert orders collection has not been altered
                    assertThat(invoices.findById(invoiceNumber)).hasValueSatisfying(value -> {
                        assertThat(value.getOrders()).singleElement()
                                .satisfies(order -> assertThat(order.getId()).isEqualTo(oldOrderId));
                    });
                }

                @Test
                @Disabled("access policies that use -to-many relations are out of scope")
                void putJson_policyFail_postSave_shouldReturn_http404_notFound() throws Exception {

                }
            }
        }

        @Nested
        @DisplayName("POST /{repository}/{id}/{property}")
        class Post {

            @Nested
            class ToOne {

                @Test
                void postJson_policyOk_shouldReturn_http405_methodNotAllowed() throws Exception {

                    var correctCustomerId = customers.findByVat(ORG_INBEV_VAT).orElseThrow().getId();
                    mockMvc.perform(post("/invoices/" + invoiceIdByNumber(INVOICE_1) + "/counterparty")
                                    .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_DRAFT))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .content("""
                                            {
                                                "_links": {
                                                    "customer" : {
                                                        "href": "/customers/%s"
                                                    }
                                                }
                                            }
                                            """.formatted(correctCustomerId)))
                            .andExpect(status().isMethodNotAllowed());
                }
            }

            @Nested
            class ToMany {

                @Test
                void putJson_policyOk_shouldAppend_http204_noContent() throws Exception {
                    var xenit = customers.findByVat(ORG_XENIT_VAT).orElseThrow();
                    var newOrderId = orders.save(new Order(xenit)).getId();

                    var invoiceNumber = invoiceIdByNumber(INVOICE_1);

                    // add an order to an invoice
                    mockMvc.perform(post("/invoices/%s/orders".formatted(invoiceNumber))
                                    .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_DRAFT))
                                    .accept(MediaType.APPLICATION_JSON)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                            {
                                                "_links": {
                                                    "orders" : {
                                                        "href": "/orders/%s"
                                                    }
                                                }
                                            }
                                            """.formatted(newOrderId)))
                            .andExpect(status().isNoContent());

                    // assert orders collection has been augmented
                    assertThat(invoices.findById(invoiceNumber)).hasValueSatisfying(invoice -> {
                        assertThat(invoice.getOrders())
                                .hasSize(3)
                                .anyMatch(order -> order.getId().equals(newOrderId));

                    });
                }

                @Test
                void postJson_policyFail_preSave_shouldReturn_http404_notFound() throws Exception {
                    var xenit = customers.findByVat(ORG_XENIT_VAT).orElseThrow();
                    var newOrderId = orders.save(new Order(xenit)).getId();

                    var invoice = invoices.findByNumber(INVOICE_2).orElseThrow();
                    var invoiceNumber = invoice.getId();
                    var oldOrderId = invoice.getOrders().stream().findFirst().map(Order::getId).orElseThrow();

                    // try to add order to invoice using POST - should fail
                    mockMvc.perform(post("/invoices/%s/orders".formatted(invoiceNumber))
                                    .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_DRAFT))
                                    .accept(MediaType.APPLICATION_JSON)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                            {
                                                "_links": {
                                                    "orders" : {
                                                        "href": "/orders/%s"
                                                    }
                                                }
                                            }
                                            """.formatted(newOrderId)))
                            .andExpect(status().isNotFound());

                    // assert orders collection has not been altered
                    assertThat(invoices.findById(invoiceNumber)).hasValueSatisfying(value -> {
                        assertThat(value.getOrders()).singleElement()
                                .satisfies(order -> assertThat(order.getId()).isEqualTo(oldOrderId));
                    });
                }

                @Test
                @Disabled("ACC-554 access policies that use -to-many relations are not supported")
                void postJson_policyFail_postSave_shouldReturn_http403_forbidden() throws Exception {
                    // try to add promo 'GORILLA' to ORDER_1 from xenit
                    mockMvc.perform(post("/orders/%s/promos".formatted(ORDER_1))
                                    .header("X-ABAC-Context", headerEncode(POLICY_CREATE_ORDER)) // ACC-554
                                    .accept(MediaType.APPLICATION_JSON)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("""
                                            {
                                                "_links": {
                                                    "promos" : [
                                                        { "href": "/promotions/GORILLA" }
                                                    ]
                                                }
                                            }
                                            """))
                            .andExpect(status().isForbidden());

                    // assert orders collection has not been altered
                    assertThat(orders.findById(ORDER_1))
                            .hasValueSatisfying(order -> {
                                assertThat(order.getPromos()).isEmpty();
                            });
                }
            }

        }

        @Nested
        @DisplayName("DELETE /{repository}/{id}/{property}")
        class Delete {

            @Nested
            class ToOne {

                @Test
                void deleteToOneAssoc_policyOk_shouldReturn_http204() throws Exception {
                    // order-1 should have customer.name=xenit and a shipping address
                    var order = orders.findById(ORDER_1).orElseThrow();
                    assertThat(order.getCustomer()).isNotNull();
                    assertThat(order.getShippingAddress()).isNotNull();

                    // policy: allows access to delete the shipping address, when customer.name is XeniT
                    // (a better example would be when the order has not shipped yet, but :shrug")
                    var ordersWithoutInvoice = Comparison.areEqual(
                            SymbolicReference.of("entity", path -> path.string("customer").string("name")),
                            Scalar.of("XeniT")
                    );

                    // unlink the shipping address from the order
                    mockMvc.perform(delete("/orders/" + ORDER_1 + "/shippingAddress")
                                    .header("X-ABAC-Context", headerEncode(ordersWithoutInvoice))
                                    .accept("application/json"))
                            .andExpect(status().isNoContent());
                }


                @Test
                void deleteToOneAssoc_policyFail_preDelete_shouldReturn_http404() throws Exception {
                    // fictive example: dis-associate the customer from the order

                    // user does not have access to order-3
                    mockMvc.perform(delete("/orders/" + ORDER_3 + "/customer")
                                    .header("X-ABAC-Context", headerEncode(POLICY_ORDERS_XENIT))
                                    .accept("application/json"))
                            .andExpect(status().isNotFound());
                }

                @Test
                void deleteToOneAssoc_policyFail_postDelete_shouldReturn_http404() throws Exception {
                    // fictive example: dis-associate the customer from the order
                    // user has access to order-1 - as long as customer = xenit
                    mockMvc.perform(delete("/orders/" + ORDER_1 + "/customer")
                                    .header("X-ABAC-Context", headerEncode(POLICY_ORDERS_XENIT))
                                    .accept("application/json"))
                            .andExpect(status().isNotFound());
                }
            }

            @Nested
            class ToMany {

                @Test
                void deleteToManyAssoc_policyOk_shouldReturn_http405_methodNotAllowed() throws Exception {

                    // fictive example: dis-associate the customer from the invoice
                    // note that this would be classified as fraud in reality :grimacing:
                    mockMvc.perform(delete("/invoices/" + invoiceIdByNumber(INVOICE_1) + "/orders")
                                    .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                    .accept("application/json"))
                            .andExpect(status().isMethodNotAllowed());
                }
            }
        }
    }


    @Nested
    class AssociationItemResource {

        @Nested
        @DisplayName("GET /{repository}/{entityId}/{property}/{propertyId}")
        class Get {

            @Nested
            class ToMany {

                @Test
                void getInvoicesOrderItem_policyOk_shouldReturn_http302() throws Exception {
                    var invoice = invoices.findByNumber(INVOICE_1).orElseThrow();
                    var result = invoice.getOrders().stream().toList();
                    assertThat(result).hasSize(2);

                    var firstOrderId = result.get(0).getId();

                    mockMvc.perform(get("/invoices/{invoice}/orders/{order}", invoiceIdByNumber(INVOICE_1), firstOrderId)
                                    .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                    .accept("application/json"))
                            .andExpect(status().isFound())
                            .andExpect(header().string(HttpHeaders.LOCATION,
                                    endsWith("/orders/%s".formatted(firstOrderId))));
                }

                @Test
                void getInvoicesOrderItem_policyFail_shouldReturn_http404() throws Exception {
                    var invoice = invoices.findByNumber(INVOICE_2).orElseThrow();
                    var result = invoice.getOrders().stream().toList();
                    assertThat(result).hasSize(1);

                    var orderId = result.get(0).getId();

                    mockMvc.perform(get("/invoices/{invoice}/orders/{order}", invoiceIdByNumber(INVOICE_2), orderId)
                                    .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                    .accept("application/json"))
                            .andExpect(status().isNotFound());
                }
            }

            @Nested
            class ToOne {

                @Test
                void getInvoiceCustomerById_policyOk_shouldReturn_http302() throws Exception {
                    var invoice = invoices.findByNumber(INVOICE_1).orElseThrow();
                    var counterPartyId = invoice.getCounterparty().getId();

                    mockMvc.perform(get("/invoices/" + invoice.getId() + "/counterparty/" + counterPartyId)
                                    .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                    .accept("application/json"))
                            .andExpect(status().isFound())
                            .andExpect(header().string(HttpHeaders.LOCATION,
                                    endsWith("/customers/%s".formatted(counterPartyId))));
                }

                @Test
                void getInvoiceCustomerByWrongId_policyOk_shouldReturn_http404() throws Exception {
                    var invoice = invoices.findByNumber(INVOICE_1).orElseThrow();
                    var wrongCounterparty = customerIdByVat(ORG_INBEV_VAT);

                    mockMvc.perform(get("/invoices/" + invoice.getId() + "/counterparty/" + wrongCounterparty)
                                    .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                    .accept("application/json"))
                            .andExpect(status().isNotFound());
                }

                @Test
                void getInvoiceCustomerById_policyFail_shouldReturn_http404() throws Exception {
                    var invoice = invoices.findByNumber(INVOICE_2).orElseThrow();
                    var counterPartyId = invoice.getCounterparty().getId();

                    mockMvc.perform(get("/invoices/" + invoice.getId() + "/counterparty/" + counterPartyId)
                                    .header("X-ABAC-Context", headerEncode(POLICY_INVOICES_XENIT))
                                    .accept("application/json"))
                            .andExpect(status().isNotFound());
                }
            }
        }
    }

    private static String headerEncode(ThunkExpression<Boolean> expression) {

        var bytes = new ExpressionJsonConverter().encode(expression).getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private UUID invoiceIdByNumber(String number) {
        return invoices.findByNumber(number).map(Invoice::getId).orElseThrow();
    }

    private UUID customerIdByVat(String vat) {
        return customers.findByVat(vat).map(Customer::getId).orElseThrow();
    }
}
