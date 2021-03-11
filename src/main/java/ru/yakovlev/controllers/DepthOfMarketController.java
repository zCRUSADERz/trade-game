package ru.yakovlev.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.val;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yakovlev.model.PriceLevelInfo;
import ru.yakovlev.repositories.OrderRepository;

/**
 * Depth of market controller.
 *
 * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
 * @since 0.10.0
 */
@RestController
@AllArgsConstructor
public class DepthOfMarketController {
    private final OrderRepository orderRepository;

    @GetMapping("/depthOfMarket")
    public ResponseEntity<RepresentationModel<CollectionModel<PriceLevelInfo>>> depthOfMarket() {
        val builder = HalModelBuilder.halModelOf(new DepthOfMarket(this.orderRepository.depthOfMarket()));
        val self = linkTo(methodOn(DepthOfMarketController.class).depthOfMarket()).withSelfRel();
        builder.link(self);
        val model = builder.<CollectionModel<PriceLevelInfo>>build();
        return ResponseEntity.ok(model);
    }

    @Value
    private static class DepthOfMarket {
        List<PriceLevelInfo> priceLevelInfos;
    }
}
