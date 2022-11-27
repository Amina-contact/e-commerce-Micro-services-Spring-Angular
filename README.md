<h3>E-commerce Application |  Micro-Services With (Spring Cloud Config, Consul Discovery, Consul Config,Vault)</h3>
<h3>I. Backend</h3>
<img src="https://github.com/Amina-contact/e-commerce-Micro-services-Spring-Angular/blob/master/pictures/Capture.JPG">
<h2>1. Configuration service</h2>
<li>Dependencies</strong>:</li>
<pre class="notranslate"><code>- Config Server
- Spring boot Actuator
- Consul Discovery
</code></pre>
<li>Application</strong>:</li>
<pre class="notranslate"><code>
@SpringBootApplication
@EnableConfigServer
public class ConfigServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServiceApplication.class, args);
    }
}
</code></pre>
<h2>2. Gateway service</h2>
<li>Dependencies</strong>:</li>
<pre class="notranslate"><code>
- Spring Cloud Gateway
- Consul Discovery 
- Spring boot Actuator
- Spring cloud Config
</code></pre>
<h2>3. Customer service</h2>
<li>Dependencies</strong>:</li>
<pre class="notranslate"><code>
- Spring Web
- Spring Data Jpa
- H2 Database
- Lombok
- Rest Repositories
- Consul Discovery 
- Config client // to find its configuration in Config service
- Spring boot Actuator
</code></pre>
<li>Customer_entity</strong>:</li>
<pre class="notranslate"><code>
@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Customer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
}
</code></pre>
<li>Customer_repository</strong>:</li>
<pre class="notranslate"><code>
@RepositoryRestResource
public interface CustomerRepository extends JpaRepository<Customer,Long> {

}
</code></pre>
<li>Customer_Projection</strong>:</li>
<pre class="notranslate"><code>
@Projection(name = "fullCustomer",types = Customer.class)
public interface CustomerProjection {
    public Long getId();
    public String getName();
    public String getEmail();
}
</code></pre>
<li>Adding some customers in starting Application:</strong>:</li>
<pre class="notranslate"><code>
@SpringBootApplication
public class CustomerServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(CustomerServiceApplication.class, args);
    }
    @Bean
    CommandLineRunner start(CustomerRepository customerRepository){
      return args -> {
        customerRepository.saveAll(List.of(
                Customer.builder().name("Amina").email("amina@gmail.com").build(),
                Customer.builder().name("Safae").email("safae@gmail.com").build(),
                Customer.builder().name("Mohammed").email("mohammed@gmail.com").build()
        ));
        customerRepository.findAll().forEach(System.out::println);
      };
    }
}
</code></pre>
<img src="https://github.com/Amina-contact/e-commerce-Micro-services-Spring-Angular/blob/master/pictures/s2.JPG">
<h2>2. Gateway service</h2>
<li>Testing</strong>:</li>
<pre class="notranslate"><code>http://localhost:8081/customers </code></pre>
<img src="https://github.com/Amina-contact/e-commerce-Micro-services-Spring-Angular/blob/master/pictures/s3.JPG">
<h2>4. Inventory service</h2>
<li>Dependencies</strong>:</li>
<pre class="notranslate"><code>
- Spring Web
- Spring Data Jpa
- H2 Database
- Lombok
- Rest Repositories
- Consul Discovery
- Config client 
- Spring boot Actuator
</code></pre>
<li>Product_entity</strong>:</li>
<pre class="notranslate"><code>
@Entity
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double price;
    private int quantity;
}
</code></pre>
<li>Product_Repository</strong>:</li>
<pre class="notranslate"><code>
@RepositoryRestResource
public interface ProductRepository extends JpaRepository<Product,Long> {

}
</code></pre>
<li>Product_Projection</strong>:</li>
<pre class="notranslate"><code>
@Projection(name = "fullProduct",types = Product.class)
public interface ProductProjection {
    public Long getId();
    public String getName();
    public double getPrice();
    public int getQuantity();
}
</code></pre>
<li>Adding some customers in starting Application:</strong>:</li>
<pre class="notranslate"><code>
@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
    @Bean
    CommandLineRunner start(ProductRepository productRepository){
        return args -> {
            Random random=new Random();
            for (int i=1;i<10;i++){
                productRepository.saveAll(List.of(
                        Product.builder().name("Computer "+i)
                                .price(1200+Math.random()*10000)
                                .quantity(1+ random.nextInt(200))
                                .build()
                ));
            }
        };
    }
}
</code></pre>
<h2>5. Order service</h2>
<li>Dependencies</strong>:</li>
<pre class="notranslate"><code>
- Spring Web
- Spring Data Jpa
- H2 Database
- Lombok
- Rest Repositories
- Consul Discovery 
- Config client 
- Spring boot Actuator
</code></pre>
<li>Product_entity</strong>:</li>
<pre class="notranslate"><code>
@Entity
@Table(name="orders")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date createdAt;
    private OrderStatus status;
    private Long customerId;
    //Est un attribut n'est pas persistant
    @Transient
    private Customer customer;
    @OneToMany(mappedBy = "order")
    private List<ProductItem> productItems;
    public double getTotal(){
        double somme=0;
        for (ProductItem pi:productItems) {
            somme+= pi.getAmount();
        }
        return somme;
    }
}
</code></pre>
<li>Enums</strong>:</li>
<pre class="notranslate"><code>
public enum OrderStatus {
    CREATED, PENDING, DELIVERED, CANCELED
}
</code></pre>
<li>ProductItem_entity</strong>:</li>
<pre class="notranslate"><code>
@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    @Transient
    private Product product;
    private double price;
    private int quantity;
    //La remise
    private double discount;
    @ManyToOne
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Order order;
    public double getAmount(){
       return price*quantity*(1-discount);
    }
}
</code></pre>
<li>Order_Repository</strong>:</li>
<pre class="notranslate"><code>
@RepositoryRestResource
public interface OrderRepository extends JpaRepository<Order,Long> {
    //Pour qu'il soit accessible via Rest
    @RestResource(path = "/byCustomerId")
    List<Order> findByCustomerId(@Param("customerId") Long customerId);
}
</code></pre>
<li>ProductItem_Repository</strong>:</li>
<pre class="notranslate"><code>
@RepositoryRestResource
public interface ProductItemRepository extends JpaRepository<ProductItem,Long> {
}
</code></pre>
<li>Order_Projection</strong>:</li>
<pre class="notranslate"><code>
@Projection(name = "fullOrder",types = Order.class)
public interface OrderProjection {
    Long getId();
    Date getCreatedAt();
    Long getCustomerId();
    OrderStatus getStatus();
}
</code></pre>

<li>Connecting Order service with other services using OpenFeign</strong>:</li>
<img src="https://github.com/Amina-contact/e-commerce-Micro-services-Spring-Angular/blob/master/pictures/s5.JPG">
<li>To communicate with the Customer_Service</strong>:</li>
<pre class="notranslate"><code>
@FeignClient(name = "customer-service")
public interface CustomerRestClientService {
    @GetMapping("/customers/{id}?projection=fullCustomer")
    public Customer customerById(@PathVariable Long id);
    @GetMapping("/customers?projection=fullCustomer")
    public PagedModel<Customer> allCustomers();
}
</code></pre>

<li>To communicate with the Inventory_Service</strong>:</li>
<pre class="notranslate"><code>
@FeignClient(name = "inventory-service")
public interface InventoryRestClientService {
    @GetMapping("/products/{id}?projection=fullProduct")
    public Product productById(@PathVariable Long id);
    @GetMapping("/products?projection=fullProduct")
    public PagedModel<Product> allProducts();
}
</code></pre>
<li>Adding some customers in starting Application</strong>:</li>
<pre class="notranslate"><code>
    @Bean
    CommandLineRunner start(OrderRepository orderRepository,
                            ProductItemRepository productItemRepository,
                            CustomerRestClientService customerRestClientService,
                            InventoryRestClientService inventoryRestClientService){
        return args -> {
            List<Customer> customers=customerRestClientService.allCustomers().getContent().stream().toList();
            List<Product> products=inventoryRestClientService.allProducts().getContent().stream().toList();
            Long customerId=1L;
            Random random=new Random();
            Customer customer=customerRestClientService.customerById(customerId);
            for (int i=0;i<20;i++){
                Order order=Order.builder()
                        .customerId(customers.get(random.nextInt(customers.size())).getId())
                        .status(Math.random()>0.5? OrderStatus.PENDING:OrderStatus.CREATED)
                        .createdAt(new Date())
                        .build();
               Order savedOrder= orderRepository.save(order);
               for (int j=0;j<products.size();j++){
                   if (Math.random()>0.70){
                       ProductItem productItem= ProductItem.builder()
                               .order(savedOrder)
                               .productId(products.get(j).getId())
                               .price(products.get(j).getPrice())
                               .quantity(1+ random.nextInt(10))
                               .discount(Math.random())
                               .build();
                       productItemRepository.save(productItem);
                   }
               }
            }
        };
    }
</code></pre>
<img src="https://github.com/Amina-contact/e-commerce-Micro-services-Spring-Angular/blob/master/pictures/s6.JPG">
<li>Testing Projection & Gateway-Service</strong>:</li>
<pre class="notranslate"><code>http://localhost:9999/order-service/fullOrder/1</code></pre>
<img src="https://github.com/Amina-contact/e-commerce-Micro-services-Spring-Angular/blob/master/pictures/s7.JPG">
<li>Rest Controller</strong>:</li>
<pre class="notranslate"><code>
@RestController
public class OrderRestController {
    private OrderRepository orderRepository;
    private ProductItemRepository productItemRepository;
    private CustomerRestClientService customerRestClientService;
    private InventoryRestClientService inventoryRestClientService;

    public OrderRestController(OrderRepository orderRepository, ProductItemRepository productItemRepository, CustomerRestClientService customerRestClientService, InventoryRestClientService inventoryRestClientService) {
        this.orderRepository = orderRepository;
        this.productItemRepository = productItemRepository;
        this.customerRestClientService = customerRestClientService;
        this.inventoryRestClientService = inventoryRestClientService;
    }
    @GetMapping("/fullOrder/{id}")
    public Order getOrder(@PathVariable Long id){
        Order order=orderRepository.findById(id).get();
        Customer customer=customerRestClientService.customerById(order.getCustomerId());
        order.setCustomer(customer);
        order.getProductItems().forEach(pi-> {
            Product product=inventoryRestClientService.productById(pi.getProductId());
            pi.setProduct(product);
        });
        return order;
    }
}
</code></pre>
<li>Consul Service</strong>:</li>
<img src="https://github.com/Amina-contact/e-commerce-Micro-services-Spring-Angular/blob/master/pictures/s1.JPG">
<h2>5. Sharing secrets using Vault</h2>
<li>Access Vault via Service.</strong>:</li>
<pre class="notranslate"><code>
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-vault-config</artifactId>
</dependency>
</code></pre>
<pre class="notranslate"><code>
spring.application.name=billing-service
server.port=8084
spring.config.import=optional:consul:, vault://
spring.cloud.vault.token=hvs.9cVOSi5HTfKYgw08oZcVZJyF
spring.cloud.vault.scheme=http
spring.cloud.vault.kv.enabled=true
management.endpoints.web.exposure.include=*
</code></pre>
<h3>II. FrontEnd</h3>
<li>Create  Angular App</strong>:</li>
<pre class="notranslate"><code> ng new ecommerce-Front</code></pre>
<li>Installing Bootstrap and Bootstrap-icons</strong>:</li>
<pre class="notranslate"><code> npm install --save bootstrap bootstrap-icons</code></pre>
<li>Adding the installed boostrap css files to <code>angular.json</code></strong>:</li> 
<img src="https://github.com/Amina-contact/e-commerce-Micro-services-Spring-Angular/blob/master/pictures/a1.JPG">
<li>Generating the component products </strong>:</li>
<pre class="notranslate"><code>ng g c products</code></pre>
<img src="https://github.com/Amina-contact/e-commerce-Micro-services-Spring-Angular/blob/master/pictures/a2.JPG">
<li>Adding <code>HttpClientModule</code> to the imports in the <code>app.module.ts</code></strong>:</li>
<img src="https://github.com/Amina-contact/e-commerce-Micro-services-Spring-Angular/blob/master/pictures/a3.JPG">
<li>Adding <code>ProductComponent</code> to the routes in the <code>app-routing.module.ts</code></strong>:</li>
<img src="https://github.com/Amina-contact/e-commerce-Micro-services-Spring-Angular/blob/master/pictures/a4.JPG">
<li>Fixing the CORS problem <code>By adding  application.yaml in the gateway-service to  allow the GET POST PUT DELETE requests from the localhost domain:4200 </code></strong></li>
<img src="https://github.com/Amina-contact/e-commerce-Micro-services-Spring-Angular/blob/master/pictures/a5.JPG">
<li>Testing Projection & Gateway-Service</strong>:</li>
<pre class="notranslate">
<code>
In the same way we will add the other componenents :
 ng g c component_name
 Add  route in app.routing.modules.ts
 Edit the HTML and CSS
</code>
</pre>
<li>List Products</strong>:</li>
<img src="https://github.com/Amina-contact/e-commerce-Micro-services-Spring-Angular/blob/master/pictures/a11.JPG">
<li>List Customers</strong>:</li>
<img src="https://github.com/Amina-contact/e-commerce-Micro-services-Spring-Angular/blob/master/pictures/a12.JPG">
<li>Orders List for a Customer</strong>:</li>
<img src="https://github.com/Amina-contact/e-commerce-Micro-services-Spring-Angular/blob/master/pictures/a13.JPG">
<li>Order Details</strong>:</li>
<img src="https://github.com/Amina-contact/e-commerce-Micro-services-Spring-Angular/blob/master/pictures/a14.JPG">