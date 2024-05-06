package guru.springframework.reactivemongo.service;

import guru.springframework.reactivemongo.domain.Beer;
import guru.springframework.reactivemongo.mappers.BeerMapper;
import guru.springframework.reactivemongo.mappers.BeerMapperImpl;
import guru.springframework.reactivemongo.model.BeerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
public class BeerServiceImplTest {

    @Autowired
    BeerService beerService;

    @Autowired
    BeerMapper beerMapper;

    BeerDTO beerDTO;

    @BeforeEach
    void setUp() {
        beerDTO = beerMapper.beerToBeerDTO(getTestBeer());
    }

    public static Beer getTestBeer() {
        return Beer.builder()
                .beerName("Space Dust")
                .beerStyle("IPA")
                .price(BigDecimal.TEN)
                .quantityOnHands(12)
                .upc("123z")
                .build();
    }

    public BeerDTO getSavedBeerDto() {
        return beerService.saveBeer(Mono.just(getTestBeerDTO())).block();
    }

    public static BeerDTO getTestBeerDTO() {
        return new BeerMapperImpl().beerToBeerDTO(getTestBeer());
    }

    @Test
    void saveBeerUsingSubscriber() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<BeerDTO> atomicDTO = new AtomicReference<>();
        Mono<BeerDTO> savedMono = beerService.saveBeer(Mono.just(beerDTO));

        savedMono.subscribe(savedDTO ->  {
            System.out.println(savedDTO);
            atomicBoolean.set(true);
            atomicDTO.set(savedDTO);
        });

        await().untilTrue(atomicBoolean);

        BeerDTO persistedDTO = atomicDTO.get();
        assertThat(persistedDTO).isNotNull();
        assertThat(persistedDTO.getId()).isNotNull();
    }

    @Test
    void saveBeerUsingBlocker()  {
        BeerDTO savedBeer = beerService.saveBeer(Mono.just(beerDTO)).block();

        assertThat(savedBeer).isNotNull();
        assertThat(savedBeer.getId()).isNotNull();
    }

    @Test
    void updateBeerUsingSubscriber()  {
        final String newName = "New name Beer";
        BeerDTO savedBeerDto = getSavedBeerDto();
        savedBeerDto.setBeerName(newName);

        AtomicReference<BeerDTO> atomicDTO = new AtomicReference<>();

        beerService.updateBeer(savedBeerDto.getId(), savedBeerDto).subscribe(savedDTO ->  {
            System.out.println(savedDTO);
            atomicDTO.set(savedDTO);
        });

        await().until(() -> atomicDTO.get() != null);

        BeerDTO updatedDTO = atomicDTO.get();
        assertThat(updatedDTO).isNotNull();
        assertThat(updatedDTO.getId()).isEqualTo(savedBeerDto.getId());
        assertThat(updatedDTO.getBeerName()).isEqualTo(newName);
    }

    @Test
    void updateUsingBlocker() {
        final String beerName = "New beer Name";
        BeerDTO savedBeerDto = getSavedBeerDto();
        savedBeerDto.setBeerName(beerName);

        BeerDTO updatedBeer = beerService.updateBeer(savedBeerDto.getId(), savedBeerDto).block();

        assertThat(updatedBeer).isNotNull();
        assertThat(updatedBeer.getId()).isEqualTo(savedBeerDto.getId());
        assertThat(updatedBeer.getBeerName()).isEqualTo(beerName);
    }

    @Test
    void getBeerById() {
        BeerDTO savedBeer = getSavedBeerDto();
        AtomicReference<BeerDTO> atomicBeer = new AtomicReference<>();

        beerService.getById(savedBeer.getId()).subscribe(
                atomicBeer::set
        );
        await().until(() -> atomicBeer.get() != null);

        assertThat(savedBeer.getId()).isEqualTo(atomicBeer.get().getId());
    }

    @Test
    void testDeleteBeer() {
        BeerDTO beerToDelete = getSavedBeerDto();

        beerService.deleteBeerById(beerToDelete.getId()).block();

        BeerDTO deletedBeer = beerService.getById(beerToDelete.getId()).block();
        assertThat(deletedBeer).isNull();
    }

    @Test
    void findFirstByBeerByName() {
        BeerDTO savedBeer = getSavedBeerDto();
        AtomicReference<BeerDTO> atomicBeer = new AtomicReference<>();

        beerService.findFirstByBeerName(savedBeer.getBeerName()).subscribe(
                atomicBeer::set
        );
        await().until(() -> atomicBeer.get() != null);

        assertThat(savedBeer.getBeerName()).isEqualTo(atomicBeer.get().getBeerName());
    }

    @Test
    void findBeerByStyle() {
        BeerDTO savedBeer = getSavedBeerDto();
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        beerService.findByBeerStyle(savedBeer.getBeerStyle()).subscribe(
                dto -> {
                    System.out.println(dto);
                    atomicBoolean.set(true);
                }
        );

        await().untilTrue(atomicBoolean);
    }


}