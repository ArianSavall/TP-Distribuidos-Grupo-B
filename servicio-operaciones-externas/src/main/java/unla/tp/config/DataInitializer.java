package unla.tp.config;

import unla.tp.entities.DenunciaPolicial;
import unla.tp.entities.TitularidadVehiculo;
import unla.tp.repositories.IDenunciaPolicialRepository;
import unla.tp.repositories.ITitularidadVehiculoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final IDenunciaPolicialRepository denunciaRepository;
    private final ITitularidadVehiculoRepository titularidadRepository;

    @Override
    public void run(String... args) {
        if (denunciaRepository.count() == 0) {
            denunciaRepository.saveAll(List.of(
                DenunciaPolicial.builder()
                    .patente("ROB999")
                    .motivo("Denuncia de robo automotor - Comisaría 1ra")
                    .fechaDenuncia(LocalDateTime.now().minusDays(10))
                    .activa(true)
                    .build(),
                DenunciaPolicial.builder()
                    .patente("AF999ZZ")
                    .motivo("Pedido de secuestro judicial activo")
                    .fechaDenuncia(LocalDateTime.now().minusDays(3))
                    .activa(true)
                    .build()
            ));
            log.info("Cargadas patentes denunciadas de prueba: ROB999, AF999ZZ");
        }

        if(titularidadRepository.count() == 0){
            titularidadRepository.saveAll(List.of(
                TitularidadVehiculo.builder()
                            .patente("ROB999")
                            .clienteId(9L)
                            .documento("12345678")
                            .build(),
                    TitularidadVehiculo.builder()
                            .patente("EXT456")
                            .clienteId(14L)
                            .documento("44888666")
                            .build()
            ));
            log.info("Cargadas titularidades de prueba: ROB999, EXT456");
        }
    }
}