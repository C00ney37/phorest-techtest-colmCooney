package com.colmcooney.fruitmachine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI fruitMachineOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Fruit Machine API")
                .version("v1")
                .description("""
                        A virtual fruit machine. Set up a machine with its own float, then play it.
                        All money is in minor units (e.g. cents).

                        Each play is paid for with the play cost, or with a free play if the player has one. \
                        The best prize the spin qualifies for is paid: a jackpot (every slot the same colour) \
                        pays the entire float, a full house (every slot a different colour) pays half the float, \
                        and a small prize (a run of adjacent slots of the same colour, at least the machine's \
                        adjacent match length) pays five times the play cost. If the float cannot cover a prize, \
                        what is left is paid and the shortfall is credited as free plays."""));
    }
}
