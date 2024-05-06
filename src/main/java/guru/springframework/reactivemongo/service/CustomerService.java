package guru.springframework.reactivemongo.service;

import guru.springframework.reactivemongo.model.CustomerDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {

    Flux<CustomerDTO> listCustomers();

    Mono<CustomerDTO> saveCustomer(Mono<CustomerDTO> CustomerDTO);

    Mono<CustomerDTO> saveCustomer(CustomerDTO CustomerDTO);

    Mono<CustomerDTO> getById(String id);

    Mono<CustomerDTO> updateCustomer(String CustomerId, CustomerDTO CustomerDTO);

    Mono<CustomerDTO> patchCustomer(String CustomerId, CustomerDTO CustomerDTO);

    Mono<Void> deleteCustomerById(String id);

}
