package guru.springframework.reactivemongo.service;

import guru.springframework.reactivemongo.mappers.CustomerMapper;
import guru.springframework.reactivemongo.model.CustomerDTO;
import guru.springframework.reactivemongo.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;


    @Override
    public Flux<CustomerDTO> listCustomers() {
        return customerRepository.findAll()
                .map(customerMapper::customerToCustomerDTO);
    }

    @Override
    public Mono<CustomerDTO> saveCustomer(Mono<CustomerDTO> customerDTO) {
        return customerDTO.map(customerMapper::customerDTOToCustomer)
                .flatMap(customerRepository::save)
                .map(customerMapper::customerToCustomerDTO);
    }

    @Override
    public Mono<CustomerDTO> saveCustomer(CustomerDTO customerDTO) {
        return customerRepository.save(customerMapper.customerDTOToCustomer(customerDTO))
                .map(customerMapper::customerToCustomerDTO);
    }

    @Override
    public Mono<CustomerDTO> getById(String id) {
        return customerRepository.findById(id).map(customerMapper::customerToCustomerDTO);
    }

    @Override
    public Mono<CustomerDTO> updateCustomer(String customerId, CustomerDTO customerDTO) {

        return customerRepository.findById(customerId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .map(foundCustomer -> {
                    foundCustomer.setCustomerName(customerDTO.getCustomerName());
                    return foundCustomer;
                })
                .flatMap(customerRepository::save)
                .map(customerMapper::customerToCustomerDTO);
    }

    @Override
    public Mono<CustomerDTO> patchCustomer(String customerId, CustomerDTO customerDTO) {
        return customerRepository.findById(customerId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .map(foundCustomer -> {
                    if (StringUtils.hasText(customerDTO.getCustomerName())){
                        foundCustomer.setCustomerName(customerDTO.getCustomerName());
                    }

                    return foundCustomer;
                })
                .flatMap(customerRepository::save)
                .map(customerMapper::customerToCustomerDTO);
    }

    @Override
    public Mono<Void> deleteCustomerById(String id) {
        return customerRepository.deleteById(id);
    }
}
